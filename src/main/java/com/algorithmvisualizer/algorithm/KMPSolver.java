package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * KMP (Knuth-Morris-Pratt) string search solver that emits fine-grained steps
 * to drive visualization. Two phases:
 *   1. Build the LPS (Longest Proper Prefix which is also Suffix) / failure function
 *   2. Search text using the failure function to skip redundant comparisons
 *
 * Time Complexity: O(n + m)
 * Space Complexity: O(m)
 */
public class KMPSolver {

    // ── Step Types ──────────────────────────────────────────────────
    public enum StepType {
        INIT,                   // Show text + pattern, announce start
        BUILD_FAILURE_START,    // Begin failure function construction
        FAILURE_COMPARE,        // Compare pattern[i] with pattern[len]
        FAILURE_MATCH,          // Characters match in failure function build
        FAILURE_MISMATCH,       // Mismatch in failure function build
        FAILURE_SET_VALUE,      // Set lps[i] value
        FAILURE_COMPLETE,       // Failure function built
        ALIGN_PATTERN,          // Position pattern at current alignment
        COMPARE_CHAR,           // Compare text[i] with pattern[j]
        CHAR_MATCH,             // Characters match
        CHAR_MISMATCH,          // Mismatch → use failure function
        SHIFT_PATTERN,          // Shift pattern using failure function
        PATTERN_FOUND,          // Full match found
        COMPLETE                // Algorithm finished
    }

    // ── Listener ────────────────────────────────────────────────────
    public interface StepListener {
        /**
         * @param type      step category
         * @param textIdx   current index in text (or pattern index during LPS build)
         * @param patIdx    current index in pattern being compared
         * @param shift     shift amount or LPS value (context-dependent)
         * @param meta      extra info
         */
        void onStep(StepType type, int textIdx, int patIdx, int shift, Map<String, Object> meta);
    }

    // ── Snapshot ────────────────────────────────────────────────────
    public static class State {
        public char[] text;
        public char[] pattern;
        public int[] lps;
        public int textPos;         // i in search phase
        public int patternPos;      // j in search phase
        public int lpsI;            // i in LPS build phase
        public int lpsLen;          // len in LPS build phase
        public Phase phase;
        public boolean done;
        public List<Integer> foundPositions;
        public int comparisons;
    }

    // ── Phase machine ───────────────────────────────────────────────
    private enum Phase {
        INIT,
        BUILD_FAILURE_START,
        FAILURE_COMPARING,
        FAILURE_MATCHED,
        FAILURE_MISMATCHED,
        FAILURE_COMPLETE,
        ALIGNING,
        COMPARING,
        MATCHED_CHAR,
        MISMATCHED,
        SHIFTING,
        FOUND,
        DONE
    }

    // ── State ───────────────────────────────────────────────────────
    private char[] text = new char[0];
    private char[] pattern = new char[0];
    private int[] lps = new int[0];
    private int textPos = 0;
    private int patternPos = 0;
    private int lpsI = 1;       // index during LPS construction
    private int lpsLen = 0;     // length of previous longest prefix suffix
    private Phase phase = Phase.INIT;
    private boolean done = false;
    private List<Integer> foundPositions = new ArrayList<>();
    private int comparisons = 0;

    private StepListener listener;

    // ── Constructor ─────────────────────────────────────────────────
    public KMPSolver(String text, String pattern) {
        setText(text);
        setPattern(pattern);
    }

    // ── Configuration ───────────────────────────────────────────────
    public void setText(String t) {
        if (t == null) t = "";
        this.text = t.toCharArray();
        reset();
    }

    public void setPattern(String p) {
        if (p == null) p = "";
        this.pattern = p.toCharArray();
        this.lps = new int[this.pattern.length];
        reset();
    }

    public void setStepListener(StepListener l) {
        this.listener = l;
    }

    public void reset() {
        textPos = 0;
        patternPos = 0;
        lpsI = 1;
        lpsLen = 0;
        lps = new int[pattern.length];
        phase = Phase.INIT;
        done = (text.length == 0 || pattern.length == 0 || pattern.length > text.length);
        foundPositions = new ArrayList<>();
        comparisons = 0;
    }

    // ── Getters ─────────────────────────────────────────────────────
    public boolean isDone()               { return done; }
    public char[] getText()               { return text.clone(); }
    public char[] getPattern()            { return pattern.clone(); }
    public int[] getLPS()                  { return lps.clone(); }
    public int getTextPos()               { return textPos; }
    public int getPatternPos()            { return patternPos; }
    public int getComparisons()           { return comparisons; }
    public List<Integer> getFoundPositions() { return new ArrayList<>(foundPositions); }
    public String getTextString()         { return new String(text); }
    public String getPatternString()      { return new String(pattern); }

    public int getAlignment() {
        return textPos - patternPos;
    }

    // ── Step ────────────────────────────────────────────────────────
    public void step() {
        if (done) return;

        switch (phase) {
            case INIT: {
                phase = Phase.BUILD_FAILURE_START;
                Map<String, Object> meta = new HashMap<>();
                meta.put("textLength", text.length);
                meta.put("patternLength", pattern.length);
                emit(StepType.INIT, -1, -1, 0, meta);
                return;
            }

            case BUILD_FAILURE_START: {
                // lps[0] is always 0
                if (pattern.length <= 1) {
                    // Pattern too short for LPS build
                    phase = Phase.FAILURE_COMPLETE;
                    emit(StepType.BUILD_FAILURE_START, 0, 0, 0, null);
                    return;
                }
                lpsI = 1;
                lpsLen = 0;
                phase = Phase.FAILURE_COMPARING;
                emit(StepType.BUILD_FAILURE_START, 0, 0, 0, null);
                return;
            }

            case FAILURE_COMPARING: {
                if (lpsI >= pattern.length) {
                    // LPS construction complete
                    phase = Phase.FAILURE_COMPLETE;
                    step(); // immediately execute FAILURE_COMPLETE phase
                    return;
                }
                // Compare pattern[lpsI] with pattern[lpsLen]
                Map<String, Object> meta = new HashMap<>();
                meta.put("charI", String.valueOf(pattern[lpsI]));
                meta.put("charLen", String.valueOf(pattern[lpsLen]));
                meta.put("i", lpsI);
                meta.put("len", lpsLen);
                emit(StepType.FAILURE_COMPARE, lpsI, lpsLen, 0, meta);

                if (pattern[lpsI] == pattern[lpsLen]) {
                    phase = Phase.FAILURE_MATCHED;
                } else {
                    phase = Phase.FAILURE_MISMATCHED;
                }
                return;
            }

            case FAILURE_MATCHED: {
                lpsLen++;
                lps[lpsI] = lpsLen;
                Map<String, Object> meta = new HashMap<>();
                meta.put("i", lpsI);
                meta.put("value", lpsLen);
                emit(StepType.FAILURE_MATCH, lpsI, lpsLen - 1, lpsLen, meta);
                lpsI++;
                phase = Phase.FAILURE_COMPARING;
                return;
            }

            case FAILURE_MISMATCHED: {
                Map<String, Object> meta = new HashMap<>();
                meta.put("i", lpsI);
                meta.put("len", lpsLen);
                if (lpsLen != 0) {
                    int oldLen = lpsLen;
                    lpsLen = lps[lpsLen - 1];
                    meta.put("fallbackFrom", oldLen);
                    meta.put("fallbackTo", lpsLen);
                    emit(StepType.FAILURE_MISMATCH, lpsI, oldLen, lpsLen, meta);
                    phase = Phase.FAILURE_COMPARING;  // Re-compare with new len
                } else {
                    lps[lpsI] = 0;
                    meta.put("value", 0);
                    emit(StepType.FAILURE_SET_VALUE, lpsI, 0, 0, meta);
                    lpsI++;
                    phase = Phase.FAILURE_COMPARING;
                }
                return;
            }

            case FAILURE_COMPLETE: {
                // Transition to search phase
                textPos = 0;
                patternPos = 0;
                phase = Phase.ALIGNING;
                Map<String, Object> meta = new HashMap<>();
                meta.put("lps", lps.clone());
                emit(StepType.FAILURE_COMPLETE, -1, -1, 0, meta);
                return;
            }

            case ALIGNING: {
                if (textPos >= text.length) {
                    // Search complete
                    done = true;
                    phase = Phase.DONE;
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("totalFound", foundPositions.size());
                    meta.put("foundPositions", new ArrayList<>(foundPositions));
                    emit(StepType.COMPLETE, -1, -1, 0, meta);
                    return;
                }
                phase = Phase.COMPARING;
                int alignment = textPos - patternPos;
                emit(StepType.ALIGN_PATTERN, alignment, patternPos, alignment, null);
                return;
            }

            case COMPARING: {
                if (textPos >= text.length) {
                    // Ran out of text
                    phase = Phase.ALIGNING;
                    return;
                }
                comparisons++;
                Map<String, Object> meta = new HashMap<>();
                meta.put("textChar", String.valueOf(text[textPos]));
                meta.put("patternChar", String.valueOf(pattern[patternPos]));
                emit(StepType.COMPARE_CHAR, textPos, patternPos, 0, meta);

                if (text[textPos] == pattern[patternPos]) {
                    phase = Phase.MATCHED_CHAR;
                } else {
                    phase = Phase.MISMATCHED;
                }
                return;
            }

            case MATCHED_CHAR: {
                emit(StepType.CHAR_MATCH, textPos, patternPos, 0, null);
                textPos++;
                patternPos++;

                if (patternPos == pattern.length) {
                    // Full pattern matched
                    phase = Phase.FOUND;
                } else {
                    phase = Phase.COMPARING;
                }
                return;
            }

            case MISMATCHED: {
                Map<String, Object> meta = new HashMap<>();
                meta.put("textChar", String.valueOf(text[textPos]));
                meta.put("patternChar", String.valueOf(pattern[patternPos]));

                if (patternPos != 0) {
                    int oldPatPos = patternPos;
                    patternPos = lps[patternPos - 1];
                    int oldAlignment = textPos - oldPatPos;
                    int newAlignment = textPos - patternPos;
                    int shiftAmount = newAlignment - oldAlignment;
                    meta.put("lpsValue", lps[oldPatPos - 1]);
                    meta.put("oldPatPos", oldPatPos);
                    meta.put("newPatPos", patternPos);
                    meta.put("shift", shiftAmount);
                    emit(StepType.CHAR_MISMATCH, textPos, oldPatPos, shiftAmount, meta);
                    phase = Phase.SHIFTING;
                } else {
                    meta.put("shift", 1);
                    emit(StepType.CHAR_MISMATCH, textPos, patternPos, 1, meta);
                    textPos++;
                    phase = Phase.ALIGNING;
                }
                return;
            }

            case SHIFTING: {
                int alignment = textPos - patternPos;
                emit(StepType.SHIFT_PATTERN, alignment, patternPos, 0, null);
                phase = Phase.COMPARING;
                return;
            }

            case FOUND: {
                int matchPos = textPos - pattern.length;
                foundPositions.add(matchPos);
                // Compute shift info (same logic as mismatch — use lps[j-1])
                int oldPatPos = patternPos; // == pattern.length at this point
                int newPatPos = lps[patternPos - 1];
                int oldAlignment = textPos - oldPatPos;
                int newAlignment = textPos - newPatPos;
                int shiftAmount = newAlignment - oldAlignment;
                Map<String, Object> meta = new HashMap<>();
                meta.put("lpsValue", lps[oldPatPos - 1]);
                meta.put("oldPatPos", oldPatPos);
                meta.put("newPatPos", newPatPos);
                meta.put("shift", shiftAmount);
                meta.put("lpsIdx", oldPatPos - 1);
                emit(StepType.PATTERN_FOUND, matchPos, -1, shiftAmount, meta);
                // Use failure function to continue searching
                patternPos = newPatPos;
                phase = Phase.ALIGNING;
                return;
            }

            case DONE:
                return;
        }
    }

    // ── Snapshot / Restore ───────────────────────────────────────────
    public State snapshot() {
        State s = new State();
        s.text = text.clone();
        s.pattern = pattern.clone();
        s.lps = lps.clone();
        s.textPos = textPos;
        s.patternPos = patternPos;
        s.lpsI = lpsI;
        s.lpsLen = lpsLen;
        s.phase = phase;
        s.done = done;
        s.foundPositions = new ArrayList<>(foundPositions);
        s.comparisons = comparisons;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        this.text = s.text.clone();
        this.pattern = s.pattern.clone();
        this.lps = s.lps.clone();
        this.textPos = s.textPos;
        this.patternPos = s.patternPos;
        this.lpsI = s.lpsI;
        this.lpsLen = s.lpsLen;
        this.phase = s.phase != null ? s.phase : Phase.DONE;
        this.done = s.done;
        this.foundPositions = new ArrayList<>(s.foundPositions);
        this.comparisons = s.comparisons;
    }

    // ── Emit ────────────────────────────────────────────────────────
    private void emit(StepType type, int textIdx, int patIdx, int shift, Map<String, Object> meta) {
        if (listener != null) {
            listener.onStep(type, textIdx, patIdx, shift, meta != null ? meta : new HashMap<>());
        }
    }

    // ── Description ─────────────────────────────────────────────────
    public String getCurrentStepDescription() {
        if (done) {
            if (foundPositions.isEmpty()) {
                return "Pattern not found in text.";
            }
            return "Found pattern at position(s): " + foundPositions;
        }
        if (phase == Phase.FAILURE_COMPARING || phase == Phase.FAILURE_MATCHED || phase == Phase.FAILURE_MISMATCHED) {
            return "Building LPS: i=" + lpsI + ", len=" + lpsLen;
        }
        return "textPos=" + textPos + ", patternPos=" + patternPos +
                (textPos >= 0 && textPos < text.length ?
                    ", text[" + textPos + "]='" + text[textPos] + "'" : "") +
                (patternPos >= 0 && patternPos < pattern.length ?
                    ", pattern[" + patternPos + "]='" + pattern[patternPos] + "'" : "");
    }
}
