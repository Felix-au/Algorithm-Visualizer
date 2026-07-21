package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Step-driven Huffman Coding solver.
 * Uses an event-queue + cursor + replay architecture (same as
 * BinaryTreeSolver).
 * Emits granular step events for rich visualization.
 */
public class HuffmanCodingSolverImpl {

    // ─── Listener ────────────────────────────────────────────────────
    public interface StepListener {
        void onStep(StepType type, StepPayload p);
    }

    // ─── Step Types (23 across 6 phases) ─────────────────────────────
    public enum StepType {
        // Phase 1: Frequency Analysis
        INIT,
        COUNT_CHAR,
        FREQ_UPDATE,
        FREQ_DONE,

        // Phase 2: Priority Queue / Leaf Creation
        CREATE_LEAF,
        ADD_TO_QUEUE,
        QUEUE_SORTED,

        // Phase 3: Tree Construction
        EXTRACT_MIN_1,
        EXTRACT_MIN_2,
        MERGE_NODES,
        INSERT_MERGED,
        TREE_GROWING,
        TREE_COMPLETE,

        // Phase 4: Code Generation
        TRAVERSE_START,
        TRAVERSE_LEFT,
        TRAVERSE_RIGHT,
        LEAF_REACHED,
        CODE_ASSIGNED,

        // Phase 5: Encoding
        ENCODE_START,
        ENCODE_CHAR,
        APPEND_CODE,
        ENCODE_COMPLETE,

        // Phase 6: Statistics
        SHOW_STATS,
        COMPLETE
    }

    // ─── Step Payload ────────────────────────────────────────────────
    public static class StepPayload {
        public Character character;
        public Integer frequency;
        public Integer nodeId;
        public Integer leftId;
        public Integer rightId;
        public Integer parentId;
        public String code; // binary code string, e.g. "010"
        public String message;
        public Integer queueSize;
        // For stats
        public Integer originalBits;
        public Integer encodedBits;
        public Double compressionRatio;
    }

    // ─── Internal tree node ──────────────────────────────────────────
    public static class HNode {
        public int id;
        public Character ch; // null for internal nodes
        public int freq;
        public int left = -1;
        public int right = -1;
    }

    // ─── Event queue ─────────────────────────────────────────────────
    private static class Event {
        final StepType t;
        final StepPayload p;

        Event(StepType t, StepPayload p) {
            this.t = t;
            this.p = p;
        }
    }

    private final List<Event> events = new ArrayList<>();
    private int cursor = 0;
    private boolean prepared = false;
    private StepListener listener;

    // Exposed state for controller
    private final List<HNode> allNodes = new ArrayList<>();
    private final Map<Character, Integer> freqMap = new LinkedHashMap<>();
    private final Map<Character, String> codeMap = new LinkedHashMap<>();
    private String inputText = "";
    private String encodedText = "";
    private int rootId = -1;

    // ─── Public API ──────────────────────────────────────────────────

    public void setStepListener(StepListener l) {
        this.listener = l;
    }

    public void encode(String text) {
        reset();
        if (text == null || text.isEmpty()) {
            emit(StepType.INIT, msg("No input text provided"));
            emit(StepType.COMPLETE, msg("Nothing to encode"));
            prepared = true;
            return;
        }
        this.inputText = text;

        // Phase 1: Frequency analysis
        emitFrequencyAnalysis(text);

        // Phase 2: Create leaf nodes + priority queue
        emitLeafCreation();

        // Phase 3: Build Huffman tree
        emitTreeConstruction();

        // Phase 4: Generate codes via traversal
        emitCodeGeneration();

        // Phase 5: Encode the input text
        emitEncoding(text);

        // Phase 6: Statistics
        emitStatistics(text);

        emit(StepType.COMPLETE, msg("Huffman coding complete!"));
        prepared = true;
    }

    public boolean step() {
        if (!hasPending())
            return false;
        Event e = events.get(cursor++);
        if (listener != null)
            listener.onStep(e.t, e.p);
        if (e.t == StepType.COMPLETE)
            prepared = false;
        return true;
    }

    public boolean hasPending() {
        return prepared && cursor < events.size();
    }

    public int getCursor() {
        return cursor;
    }

    public int getEventCount() {
        return events.size();
    }

    public void setCursor(int c) {
        cursor = Math.max(0, Math.min(c, events.size()));
        prepared = cursor < events.size();
    }

    public void replayTo(int end) {
        if (listener == null)
            return;
        int to = Math.max(0, Math.min(end, events.size()));
        for (int i = 0; i < to; i++) {
            Event e = events.get(i);
            listener.onStep(e.t, e.p);
        }
    }

    public void reset() {
        events.clear();
        cursor = 0;
        prepared = false;
        allNodes.clear();
        freqMap.clear();
        codeMap.clear();
        inputText = "";
        encodedText = "";
        rootId = -1;
    }

    // ─── Exposed read-only state ─────────────────────────────────────

    public List<HNode> getAllNodes() {
        return Collections.unmodifiableList(allNodes);
    }

    public Map<Character, Integer> getFreqMap() {
        return Collections.unmodifiableMap(freqMap);
    }

    public Map<Character, String> getCodeMap() {
        return Collections.unmodifiableMap(codeMap);
    }

    public String getInputText() {
        return inputText;
    }

    public String getEncodedText() {
        return encodedText;
    }

    public int getRootId() {
        return rootId;
    }

    // ─── Phase 1: Frequency Analysis ─────────────────────────────────

    private void emitFrequencyAnalysis(String text) {
        emit(StepType.INIT, msg("Analyzing character frequencies in: \"" + text + "\""));

        Map<Character, Integer> freq = new LinkedHashMap<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            freq.merge(c, 1, Integer::sum);

            StepPayload sp = new StepPayload();
            sp.character = c;
            sp.frequency = freq.get(c);
            sp.message = "Scanning character '" + displayChar(c) + "' at index " + i;
            emit(StepType.COUNT_CHAR, sp);

            StepPayload up = new StepPayload();
            up.character = c;
            up.frequency = freq.get(c);
            up.message = "'" + displayChar(c) + "' → freq " + freq.get(c);
            emit(StepType.FREQ_UPDATE, up);
        }

        this.freqMap.putAll(freq);

        StepPayload done = new StepPayload();
        done.message = "Frequency analysis complete: " + freq.size() + " unique characters";
        emit(StepType.FREQ_DONE, done);
    }

    // ─── Phase 2: Leaf Creation ──────────────────────────────────────

    private void emitLeafCreation() {
        int id = 0;
        List<int[]> queue = new ArrayList<>(); // [nodeId, freq] for sorting display

        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            HNode node = new HNode();
            node.id = id;
            node.ch = entry.getKey();
            node.freq = entry.getValue();
            allNodes.add(node);

            StepPayload sp = new StepPayload();
            sp.nodeId = id;
            sp.character = entry.getKey();
            sp.frequency = entry.getValue();
            sp.message = "Create leaf node for '" + displayChar(entry.getKey()) + "' (freq=" + entry.getValue() + ")";
            emit(StepType.CREATE_LEAF, sp);

            queue.add(new int[] { id, entry.getValue() });

            StepPayload aq = new StepPayload();
            aq.nodeId = id;
            aq.queueSize = queue.size();
            aq.message = "Added to priority queue (size=" + queue.size() + ")";
            emit(StepType.ADD_TO_QUEUE, aq);

            id++;
        }

        // Sort queue by frequency
        queue.sort(Comparator.comparingInt(a -> a[1]));

        StepPayload sorted = new StepPayload();
        sorted.queueSize = queue.size();
        sorted.message = "Priority queue sorted by frequency (" + queue.size() + " nodes)";
        emit(StepType.QUEUE_SORTED, sorted);
    }

    // ─── Phase 3: Tree Construction ──────────────────────────────────

    private void emitTreeConstruction() {
        // Build a working priority queue from allNodes
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        for (HNode n : allNodes) {
            pq.offer(new int[] { n.id, n.freq });
        }

        while (pq.size() > 1) {
            // Extract min 1
            int[] min1 = pq.poll();
            StepPayload e1 = new StepPayload();
            e1.nodeId = min1[0];
            e1.frequency = min1[1];
            HNode n1 = allNodes.get(min1[0]);
            e1.character = n1.ch;
            e1.queueSize = pq.size() + 1;
            e1.message = "Extract min: " + nodeLabel(n1) + " (freq=" + min1[1] + ")";
            emit(StepType.EXTRACT_MIN_1, e1);

            // Extract min 2
            int[] min2 = pq.poll();
            StepPayload e2 = new StepPayload();
            e2.nodeId = min2[0];
            e2.frequency = min2[1];
            HNode n2 = allNodes.get(min2[0]);
            e2.character = n2.ch;
            e2.queueSize = pq.size() + 2;
            e2.message = "Extract min: " + nodeLabel(n2) + " (freq=" + min2[1] + ")";
            emit(StepType.EXTRACT_MIN_2, e2);

            // Merge
            int newId = allNodes.size();
            int newFreq = min1[1] + min2[1];
            HNode merged = new HNode();
            merged.id = newId;
            merged.ch = null; // internal node
            merged.freq = newFreq;
            merged.left = min1[0];
            merged.right = min2[0];
            allNodes.add(merged);

            StepPayload mg = new StepPayload();
            mg.nodeId = newId;
            mg.leftId = min1[0];
            mg.rightId = min2[0];
            mg.frequency = newFreq;
            mg.message = "Merge " + nodeLabel(n1) + " + " + nodeLabel(n2) + " → internal node (freq=" + newFreq + ")";
            emit(StepType.MERGE_NODES, mg);

            // Insert back into queue
            pq.offer(new int[] { newId, newFreq });

            StepPayload ins = new StepPayload();
            ins.nodeId = newId;
            ins.frequency = newFreq;
            ins.queueSize = pq.size();
            ins.message = "Insert merged node back (queue size=" + pq.size() + ")";
            emit(StepType.INSERT_MERGED, ins);

            // Show tree growing
            StepPayload grow = new StepPayload();
            grow.nodeId = newId;
            grow.message = "Tree structure updated";
            emit(StepType.TREE_GROWING, grow);
        }

        // Root
        if (!pq.isEmpty()) {
            rootId = pq.poll()[0];
        }

        StepPayload tc = new StepPayload();
        tc.nodeId = rootId;
        tc.message = "Huffman tree complete! Root node ID=" + rootId;
        emit(StepType.TREE_COMPLETE, tc);
    }

    // ─── Phase 4: Code Generation ────────────────────────────────────

    private void emitCodeGeneration() {
        emit(StepType.TRAVERSE_START, msg("Generating Huffman codes via tree traversal…"));

        if (rootId >= 0) {
            // Special case: single character
            if (allNodes.get(rootId).ch != null) {
                char c = allNodes.get(rootId).ch;
                codeMap.put(c, "0");
                StepPayload sp = new StepPayload();
                sp.nodeId = rootId;
                sp.character = c;
                sp.code = "0";
                sp.message = "Single character → code '0'";
                emit(StepType.LEAF_REACHED, sp);

                StepPayload ca = new StepPayload();
                ca.character = c;
                ca.code = "0";
                ca.message = "'" + displayChar(c) + "' → 0";
                emit(StepType.CODE_ASSIGNED, ca);
            } else {
                generateCodes(rootId, "");
            }
        }
    }

    private void generateCodes(int nodeId, String prefix) {
        if (nodeId < 0)
            return;
        HNode node = allNodes.get(nodeId);

        if (node.ch != null) {
            // Leaf node: code is complete
            codeMap.put(node.ch, prefix);

            StepPayload lr = new StepPayload();
            lr.nodeId = nodeId;
            lr.character = node.ch;
            lr.code = prefix;
            lr.frequency = node.freq;
            lr.message = "🎯 Leaf reached: '" + displayChar(node.ch) + "' → code '" + prefix + "'";
            emit(StepType.LEAF_REACHED, lr);

            StepPayload ca = new StepPayload();
            ca.character = node.ch;
            ca.code = prefix;
            ca.message = "'" + displayChar(node.ch) + "' → " + prefix;
            emit(StepType.CODE_ASSIGNED, ca);
            return;
        }

        // Left child (add '0')
        if (node.left >= 0) {
            StepPayload tl = new StepPayload();
            tl.nodeId = nodeId;
            tl.leftId = node.left;
            tl.code = prefix + "0";
            tl.message = "Go left → append '0' (code so far: " + prefix + "0)";
            emit(StepType.TRAVERSE_LEFT, tl);
            generateCodes(node.left, prefix + "0");
        }

        // Right child (add '1')
        if (node.right >= 0) {
            StepPayload tr = new StepPayload();
            tr.nodeId = nodeId;
            tr.rightId = node.right;
            tr.code = prefix + "1";
            tr.message = "Go right → append '1' (code so far: " + prefix + "1)";
            emit(StepType.TRAVERSE_RIGHT, tr);
            generateCodes(node.right, prefix + "1");
        }
    }

    // ─── Phase 5: Encoding ───────────────────────────────────────────

    private void emitEncoding(String text) {
        emit(StepType.ENCODE_START, msg("Encoding the input text using Huffman codes…"));

        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String code = codeMap.getOrDefault(c, "?");

            StepPayload ec = new StepPayload();
            ec.character = c;
            ec.code = code;
            ec.message = "Encode '" + displayChar(c) + "' → " + code;
            emit(StepType.ENCODE_CHAR, ec);

            encoded.append(code);

            StepPayload ap = new StepPayload();
            ap.character = c;
            ap.code = code;
            ap.message = "Appended '" + code + "' → encoded length: " + encoded.length() + " bits";
            emit(StepType.APPEND_CODE, ap);
        }

        this.encodedText = encoded.toString();

        StepPayload done = new StepPayload();
        done.code = encodedText;
        done.message = "Encoding complete! Total length: " + encodedText.length() + " bits";
        emit(StepType.ENCODE_COMPLETE, done);
    }

    // ─── Phase 6: Statistics ─────────────────────────────────────────

    private void emitStatistics(String text) {
        int originalBits = text.length() * 8; // ASCII
        int encodedBits = encodedText.length();
        double ratio = originalBits > 0 ? (1.0 - (double) encodedBits / originalBits) * 100.0 : 0;

        StepPayload st = new StepPayload();
        st.originalBits = originalBits;
        st.encodedBits = encodedBits;
        st.compressionRatio = ratio;
        st.message = String.format("Original: %d bits | Encoded: %d bits | Compression: %.1f%%",
                originalBits, encodedBits, ratio);
        emit(StepType.SHOW_STATS, st);
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private void emit(StepType t, StepPayload p) {
        events.add(new Event(t, p));
    }

    private StepPayload msg(String m) {
        StepPayload p = new StepPayload();
        p.message = m;
        return p;
    }

    private String displayChar(char c) {
        if (c == ' ')
            return "⎵";
        if (c == '\n')
            return "↵";
        if (c == '\t')
            return "⇥";
        return String.valueOf(c);
    }

    private String nodeLabel(HNode n) {
        if (n.ch != null)
            return "'" + displayChar(n.ch) + "'";
        return "[" + n.freq + "]";
    }
}
