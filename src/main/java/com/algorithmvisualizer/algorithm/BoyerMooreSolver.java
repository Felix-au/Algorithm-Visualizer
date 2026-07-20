package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Boyer-Moore string search solver that emits fine-grained steps to drive visualization.
 * Implements the Bad Character heuristic for educational visualization.
 *
 * Comparisons proceed right-to-left within the pattern, and on mismatch
 * the pattern is shifted right using the bad character rule.
 *
 * Time Complexity: O(n/m) best, O(n*m) worst
 * Space Complexity: O(m + σ)
 */
public class BoyerMooreSolver {

    // ── Step Types ──────────────────────────────────────────────────
    public enum StepType {
        INIT,               // Show text + pattern, build bad char table
        ALIGN_PATTERN,      // Position pattern at new alignment
        COMPARE_CHAR,       // Highlight text[i] vs pattern[j]
        CHAR_MATCH,         // Characters match, move left
        CHAR_MISMATCH,      // Characters don't match, compute shift
        SHIFT_PATTERN,      // Shift pattern right (show skip distance)
        PATTERN_FOUND,      // Full pattern matched at current alignment
        COMPLETE            // Algorithm finished
    }

    // ── Listener ────────────────────────────────────────────────────
    public interface StepListener {
        /**
         * @param type      step category
         * @param textIdx   current index in text being compared
         * @param patIdx    current index in pattern being compared
         * @param shift     shift amount (meaningful for SHIFT_PATTERN)
         * @param meta      extra info (badCharTable, foundPositions, etc.)
         */
        void onStep(StepType type, int textIdx, int patIdx, int shift, Map<String, Object> meta);
    }

    // ── Snapshot ────────────────────────────────────────────────────
    public static class State {
        public char[] text;
        public char[] pattern;
        public int[] badCharTable;
        public int alignment;       // current alignment of pattern[0] in text
        public int patternPos;      // current comparison position in pattern (right→left)
        public Phase phase;
        public boolean done;
        public List<Integer> foundPositions;
        public int comparisons;
        public int shifts;
    }

    // ── Phase machine ───────────────────────────────────────────────
    private enum Phase {
        INIT,
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
    private int[] badCharTable = new int[256]; // ASCII
    private int alignment = 0;
    private int patternPos = 0;
    private Phase phase = Phase.INIT;
    private boolean done = false;
    private List<Integer> foundPositions = new ArrayList<>();
    private int comparisons = 0;
    private int shifts = 0;

    private StepListener listener;

    // ── Constructor ─────────────────────────────────────────────────
    public BoyerMooreSolver(String text, String pattern) {
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
        buildBadCharTable();
        reset();
    }

    public void setStepListener(StepListener l) {
        this.listener = l;
    }

    public void reset() {
        alignment = 0;
        patternPos = pattern.length - 1;
        phase = Phase.INIT;
        done = (text.length == 0 || pattern.length == 0 || pattern.length > text.length);
        foundPositions = new ArrayList<>();
        comparisons = 0;
        shifts = 0;
    }

    // ── Getters ─────────────────────────────────────────────────────
    public boolean isDone() { return done; }
    public char[] getText() { return text.clone(); }
    public char[] getPattern() { return pattern.clone(); }
    public int getAlignment() { return alignment; }
    public int getPatternPos() { return patternPos; }
    public int getComparisons() { return comparisons; }
    public int getShifts() { return shifts; }
    public List<Integer> getFoundPositions() { return new ArrayList<>(foundPositions); }
    public int[] getBadCharTable() { return badCharTable.clone(); }

    public String getTextString() { return new String(text); }
    public String getPatternString() { return new String(pattern); }

    // ── Bad Character Table ─────────────────────────────────────────
    private void buildBadCharTable() {
        Arrays.fill(badCharTable, -1);
        for (int i = 0; i < pattern.length; i++) {
            badCharTable[pattern[i]] = i;
        }
    }

    // ── Step ────────────────────────────────────────────────────────
    public void step() {
        if (done) return;

        switch (phase) {
            case INIT: {
                phase = Phase.ALIGNING;
                Map<String, Object> meta = new HashMap<>();
                meta.put("textLength", text.length);
                meta.put("patternLength", pattern.length);
                emit(StepType.INIT, -1, -1, 0, meta);
                return;
            }

            case ALIGNING: {
                if (alignment > text.length - pattern.length) {
                    // No more alignments possible
                    done = true;
                    phase = Phase.DONE;
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("totalFound", foundPositions.size());
                    meta.put("foundPositions", new ArrayList<>(foundPositions));
                    emit(StepType.COMPLETE, -1, -1, 0, meta);
                    return;
                }
                patternPos = pattern.length - 1;
                phase = Phase.COMPARING;
                emit(StepType.ALIGN_PATTERN, alignment, -1, alignment, null);
                return;
            }

            case COMPARING: {
                int textIdx = alignment + patternPos;
                phase = Phase.COMPARING; // stay in COMPARING after emit
                comparisons++;
                Map<String, Object> meta = new HashMap<>();
                meta.put("textChar", String.valueOf(text[textIdx]));
                meta.put("patternChar", String.valueOf(pattern[patternPos]));
                emit(StepType.COMPARE_CHAR, textIdx, patternPos, 0, meta);
                // Decide match/mismatch on next step
                if (text[textIdx] == pattern[patternPos]) {
                    phase = Phase.MATCHED_CHAR;
                } else {
                    phase = Phase.MISMATCHED;
                }
                return;
            }

            case MATCHED_CHAR: {
                int textIdx = alignment + patternPos;
                if (patternPos == 0) {
                    // Entire pattern matched!
                    phase = Phase.FOUND;
                    emit(StepType.CHAR_MATCH, textIdx, patternPos, 0, null);
                } else {
                    patternPos--;
                    phase = Phase.COMPARING;
                    emit(StepType.CHAR_MATCH, textIdx, patternPos + 1, 0, null);
                }
                return;
            }

            case MISMATCHED: {
                int textIdx = alignment + patternPos;
                char badChar = text[textIdx];
                int lastOccurrence = badCharTable[badChar];
                int shift = Math.max(1, patternPos - lastOccurrence);

                Map<String, Object> meta = new HashMap<>();
                meta.put("badChar", String.valueOf(badChar));
                meta.put("lastOccurrence", lastOccurrence);
                meta.put("shift", shift);
                emit(StepType.CHAR_MISMATCH, textIdx, patternPos, shift, meta);
                phase = Phase.SHIFTING;
                return;
            }

            case SHIFTING: {
                int textIdx = alignment + patternPos;
                char badChar = text[textIdx];
                int lastOccurrence = badCharTable[badChar];
                int shift = Math.max(1, patternPos - lastOccurrence);

                alignment += shift;
                shifts++;
                phase = Phase.ALIGNING;
                emit(StepType.SHIFT_PATTERN, alignment, -1, shift, null);
                return;
            }

            case FOUND: {
                foundPositions.add(alignment);
                emit(StepType.PATTERN_FOUND, alignment, -1, 0, null);
                // Move past this occurrence to find more
                alignment += 1;
                shifts++;
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
        s.badCharTable = badCharTable.clone();
        s.alignment = alignment;
        s.patternPos = patternPos;
        s.phase = phase;
        s.done = done;
        s.foundPositions = new ArrayList<>(foundPositions);
        s.comparisons = comparisons;
        s.shifts = shifts;
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        this.text = s.text.clone();
        this.pattern = s.pattern.clone();
        this.badCharTable = s.badCharTable.clone();
        this.alignment = s.alignment;
        this.patternPos = s.patternPos;
        this.phase = s.phase != null ? s.phase : Phase.DONE;
        this.done = s.done;
        this.foundPositions = new ArrayList<>(s.foundPositions);
        this.comparisons = s.comparisons;
        this.shifts = s.shifts;
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
        int textIdx = alignment + patternPos;
        return "alignment=" + alignment + ", patternPos=" + patternPos +
                (textIdx >= 0 && textIdx < text.length ?
                    ", text[" + textIdx + "]='" + text[textIdx] + "'" : "") +
                (patternPos >= 0 && patternPos < pattern.length ?
                    ", pattern[" + patternPos + "]='" + pattern[patternPos] + "'" : "");
    }
}
