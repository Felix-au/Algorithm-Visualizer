package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * Huffman Coding algorithm code in Java, C, C++, and Python.
 */
public class HuffmanCodingCode implements AlgorithmCode {

    private String inputText = "hello world";

    public HuffmanCodingCode() {
    }

    public void updateParameters(String inputText) {
        if (inputText != null && !inputText.isEmpty()) {
            this.inputText = inputText;
        }
    }

    @Override
    public String getAlgorithmName() {
        return "Huffman Coding";
    }

    @Override
    public String getJavaCode() {
        return "import java.util.*;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    // Node for Huffman Tree\n" +
                "    static class Node implements Comparable<Node> {\n" +
                "        char ch;\n" +
                "        int freq;\n" +
                "        Node left, right;\n" +
                "\n" +
                "        Node(char ch, int freq) {\n" +
                "            this.ch = ch;\n" +
                "            this.freq = freq;\n" +
                "        }\n" +
                "\n" +
                "        Node(char ch, int freq, Node left, Node right) {\n" +
                "            this.ch = ch;\n" +
                "            this.freq = freq;\n" +
                "            this.left = left;\n" +
                "            this.right = right;\n" +
                "        }\n" +
                "\n" +
                "        @Override\n" +
                "        public int compareTo(Node other) {\n" +
                "            return this.freq - other.freq;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    // Build frequency map\n" +
                "    static Map<Character, Integer> buildFrequencyMap(String text) {\n" +
                "        Map<Character, Integer> freq = new HashMap<>();\n" +
                "        for (char c : text.toCharArray()) {\n" +
                "            freq.merge(c, 1, Integer::sum);\n" +
                "        }\n" +
                "        return freq;\n" +
                "    }\n" +
                "\n" +
                "    // Build Huffman Tree\n" +
                "    static Node buildTree(Map<Character, Integer> freq) {\n" +
                "        PriorityQueue<Node> pq = new PriorityQueue<>();\n" +
                "        for (var entry : freq.entrySet()) {\n" +
                "            pq.offer(new Node(entry.getKey(), entry.getValue()));\n" +
                "        }\n" +
                "        while (pq.size() > 1) {\n" +
                "            Node left = pq.poll();\n" +
                "            Node right = pq.poll();\n" +
                "            Node merged = new Node('\\0', left.freq + right.freq, left, right);\n" +
                "            pq.offer(merged);\n" +
                "        }\n" +
                "        return pq.poll();\n" +
                "    }\n" +
                "\n" +
                "    // Generate codes via tree traversal\n" +
                "    static void generateCodes(Node node, String prefix, Map<Character, String> codes) {\n" +
                "        if (node == null) return;\n" +
                "        if (node.left == null && node.right == null) {\n" +
                "            codes.put(node.ch, prefix.isEmpty() ? \"0\" : prefix);\n" +
                "            return;\n" +
                "        }\n" +
                "        generateCodes(node.left, prefix + \"0\", codes);\n" +
                "        generateCodes(node.right, prefix + \"1\", codes);\n" +
                "    }\n" +
                "\n" +
                "    // Encode text\n" +
                "    static String encode(String text, Map<Character, String> codes) {\n" +
                "        StringBuilder sb = new StringBuilder();\n" +
                "        for (char c : text.toCharArray()) {\n" +
                "            sb.append(codes.get(c));\n" +
                "        }\n" +
                "        return sb.toString();\n" +
                "    }\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        String text = \"" + inputText + "\";\n" +
                "        System.out.println(\"Input: \" + text);\n" +
                "\n" +
                "        // Step 1: Build frequency map\n" +
                "        Map<Character, Integer> freq = buildFrequencyMap(text);\n" +
                "        System.out.println(\"Frequencies: \" + freq);\n" +
                "\n" +
                "        // Step 2: Build Huffman tree\n" +
                "        Node root = buildTree(freq);\n" +
                "\n" +
                "        // Step 3: Generate codes\n" +
                "        Map<Character, String> codes = new HashMap<>();\n" +
                "        generateCodes(root, \"\", codes);\n" +
                "        System.out.println(\"Codes: \" + codes);\n" +
                "\n" +
                "        // Step 4: Encode\n" +
                "        String encoded = encode(text, codes);\n" +
                "        System.out.println(\"Encoded: \" + encoded);\n" +
                "        System.out.printf(\"Compression: %d bits -> %d bits (%.1f%% saved)%n\",\n" +
                "                text.length() * 8, encoded.length(),\n" +
                "                (1.0 - (double) encoded.length() / (text.length() * 8)) * 100);\n" +
                "    }\n" +
                "}\n";
    }

    @Override
    public String getCCode() {
        return "#include <stdio.h>\n" +
                "#include <stdlib.h>\n" +
                "#include <string.h>\n" +
                "\n" +
                "#define MAX_CHARS 256\n" +
                "#define MAX_CODE_LEN 256\n" +
                "\n" +
                "typedef struct Node {\n" +
                "    char ch;\n" +
                "    int freq;\n" +
                "    struct Node *left, *right;\n" +
                "} Node;\n" +
                "\n" +
                "// Min-heap (priority queue)\n" +
                "typedef struct {\n" +
                "    Node** data;\n" +
                "    int size, capacity;\n" +
                "} MinHeap;\n" +
                "\n" +
                "MinHeap* createHeap(int cap) {\n" +
                "    MinHeap* h = (MinHeap*)malloc(sizeof(MinHeap));\n" +
                "    h->data = (Node**)malloc(cap * sizeof(Node*));\n" +
                "    h->size = 0; h->capacity = cap;\n" +
                "    return h;\n" +
                "}\n" +
                "\n" +
                "void swap(Node** a, Node** b) { Node* t = *a; *a = *b; *b = t; }\n" +
                "\n" +
                "void heapifyUp(MinHeap* h, int i) {\n" +
                "    while (i > 0 && h->data[(i-1)/2]->freq > h->data[i]->freq) {\n" +
                "        swap(&h->data[(i-1)/2], &h->data[i]);\n" +
                "        i = (i-1)/2;\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "void heapifyDown(MinHeap* h, int i) {\n" +
                "    int smallest = i, l = 2*i+1, r = 2*i+2;\n" +
                "    if (l < h->size && h->data[l]->freq < h->data[smallest]->freq) smallest = l;\n" +
                "    if (r < h->size && h->data[r]->freq < h->data[smallest]->freq) smallest = r;\n" +
                "    if (smallest != i) { swap(&h->data[i], &h->data[smallest]); heapifyDown(h, smallest); }\n" +
                "}\n" +
                "\n" +
                "void push(MinHeap* h, Node* n) { h->data[h->size] = n; heapifyUp(h, h->size++); }\n" +
                "Node* pop(MinHeap* h) { Node* n = h->data[0]; h->data[0] = h->data[--h->size]; heapifyDown(h, 0); return n; }\n"
                +
                "\n" +
                "Node* newNode(char ch, int freq, Node* l, Node* r) {\n" +
                "    Node* n = (Node*)malloc(sizeof(Node));\n" +
                "    n->ch = ch; n->freq = freq; n->left = l; n->right = r;\n" +
                "    return n;\n" +
                "}\n" +
                "\n" +
                "char codes[MAX_CHARS][MAX_CODE_LEN];\n" +
                "\n" +
                "void generateCodes(Node* node, char* prefix, int depth) {\n" +
                "    if (!node) return;\n" +
                "    if (!node->left && !node->right) {\n" +
                "        prefix[depth] = '\\0';\n" +
                "        strcpy(codes[(unsigned char)node->ch], depth == 0 ? \"0\" : prefix);\n" +
                "        return;\n" +
                "    }\n" +
                "    prefix[depth] = '0'; generateCodes(node->left, prefix, depth+1);\n" +
                "    prefix[depth] = '1'; generateCodes(node->right, prefix, depth+1);\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "    char text[] = \"" + inputText + "\";\n" +
                "    int freq[MAX_CHARS] = {0};\n" +
                "    int len = strlen(text);\n" +
                "\n" +
                "    // Count frequencies\n" +
                "    for (int i = 0; i < len; i++) freq[(unsigned char)text[i]]++;\n" +
                "\n" +
                "    // Create leaf nodes\n" +
                "    int unique = 0;\n" +
                "    for (int i = 0; i < MAX_CHARS; i++) if (freq[i]) unique++;\n" +
                "\n" +
                "    MinHeap* heap = createHeap(unique);\n" +
                "    for (int i = 0; i < MAX_CHARS; i++)\n" +
                "        if (freq[i]) push(heap, newNode((char)i, freq[i], NULL, NULL));\n" +
                "\n" +
                "    // Build tree\n" +
                "    while (heap->size > 1) {\n" +
                "        Node* l = pop(heap); Node* r = pop(heap);\n" +
                "        push(heap, newNode('\\0', l->freq + r->freq, l, r));\n" +
                "    }\n" +
                "\n" +
                "    // Generate codes\n" +
                "    char prefix[MAX_CODE_LEN];\n" +
                "    generateCodes(heap->data[0], prefix, 0);\n" +
                "\n" +
                "    // Print codes and encode\n" +
                "    printf(\"Input: %s\\n\", text);\n" +
                "    printf(\"Codes:\\n\");\n" +
                "    for (int i = 0; i < MAX_CHARS; i++)\n" +
                "        if (freq[i]) printf(\"  '%c': %s\\n\", i, codes[i]);\n" +
                "\n" +
                "    printf(\"Encoded: \");\n" +
                "    int encodedLen = 0;\n" +
                "    for (int i = 0; i < len; i++) {\n" +
                "        printf(\"%s\", codes[(unsigned char)text[i]]);\n" +
                "        encodedLen += strlen(codes[(unsigned char)text[i]]);\n" +
                "    }\n" +
                "    printf(\"\\nCompression: %d bits -> %d bits (%.1f%% saved)\\n\",\n" +
                "           len * 8, encodedLen, (1.0 - (double)encodedLen / (len * 8)) * 100);\n" +
                "    return 0;\n" +
                "}\n";
    }

    @Override
    public String getCppCode() {
        return "#include <iostream>\n" +
                "#include <queue>\n" +
                "#include <unordered_map>\n" +
                "#include <string>\n" +
                "using namespace std;\n" +
                "\n" +
                "struct Node {\n" +
                "    char ch;\n" +
                "    int freq;\n" +
                "    Node *left, *right;\n" +
                "    Node(char c, int f, Node* l=nullptr, Node* r=nullptr)\n" +
                "        : ch(c), freq(f), left(l), right(r) {}\n" +
                "};\n" +
                "\n" +
                "struct Compare {\n" +
                "    bool operator()(Node* a, Node* b) { return a->freq > b->freq; }\n" +
                "};\n" +
                "\n" +
                "void generateCodes(Node* node, string prefix, unordered_map<char,string>& codes) {\n" +
                "    if (!node) return;\n" +
                "    if (!node->left && !node->right) {\n" +
                "        codes[node->ch] = prefix.empty() ? \"0\" : prefix;\n" +
                "        return;\n" +
                "    }\n" +
                "    generateCodes(node->left, prefix + \"0\", codes);\n" +
                "    generateCodes(node->right, prefix + \"1\", codes);\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "    string text = \"" + inputText + "\";\n" +
                "    cout << \"Input: \" << text << endl;\n" +
                "\n" +
                "    // Step 1: Count frequencies\n" +
                "    unordered_map<char,int> freq;\n" +
                "    for (char c : text) freq[c]++;\n" +
                "\n" +
                "    // Step 2: Build Huffman tree\n" +
                "    priority_queue<Node*, vector<Node*>, Compare> pq;\n" +
                "    for (auto& [c,f] : freq) pq.push(new Node(c, f));\n" +
                "\n" +
                "    while (pq.size() > 1) {\n" +
                "        Node* left = pq.top(); pq.pop();\n" +
                "        Node* right = pq.top(); pq.pop();\n" +
                "        pq.push(new Node('\\0', left->freq + right->freq, left, right));\n" +
                "    }\n" +
                "\n" +
                "    // Step 3: Generate codes\n" +
                "    unordered_map<char,string> codes;\n" +
                "    generateCodes(pq.top(), \"\", codes);\n" +
                "\n" +
                "    cout << \"Codes:\" << endl;\n" +
                "    for (auto& [c,code] : codes)\n" +
                "        cout << \"  '\" << c << \"': \" << code << endl;\n" +
                "\n" +
                "    // Step 4: Encode\n" +
                "    string encoded;\n" +
                "    for (char c : text) encoded += codes[c];\n" +
                "\n" +
                "    cout << \"Encoded: \" << encoded << endl;\n" +
                "    printf(\"Compression: %lu bits -> %lu bits (%.1f%% saved)\\n\",\n" +
                "           text.size() * 8, encoded.size(),\n" +
                "           (1.0 - (double)encoded.size() / (text.size() * 8)) * 100);\n" +
                "    return 0;\n" +
                "}\n";
    }

    @Override
    public String getPythonCode() {
        return "import heapq\n" +
                "from collections import Counter\n" +
                "\n" +
                "class Node:\n" +
                "    def __init__(self, ch, freq, left=None, right=None):\n" +
                "        self.ch = ch\n" +
                "        self.freq = freq\n" +
                "        self.left = left\n" +
                "        self.right = right\n" +
                "\n" +
                "    def __lt__(self, other):\n" +
                "        return self.freq < other.freq\n" +
                "\n" +
                "\n" +
                "def build_tree(text):\n" +
                "    freq = Counter(text)\n" +
                "    heap = [Node(ch, f) for ch, f in freq.items()]\n" +
                "    heapq.heapify(heap)\n" +
                "\n" +
                "    while len(heap) > 1:\n" +
                "        left = heapq.heappop(heap)\n" +
                "        right = heapq.heappop(heap)\n" +
                "        merged = Node(None, left.freq + right.freq, left, right)\n" +
                "        heapq.heappush(heap, merged)\n" +
                "\n" +
                "    return heap[0] if heap else None\n" +
                "\n" +
                "\n" +
                "def generate_codes(node, prefix='', codes=None):\n" +
                "    if codes is None:\n" +
                "        codes = {}\n" +
                "    if node is None:\n" +
                "        return codes\n" +
                "    if node.left is None and node.right is None:\n" +
                "        codes[node.ch] = prefix or '0'\n" +
                "        return codes\n" +
                "    generate_codes(node.left, prefix + '0', codes)\n" +
                "    generate_codes(node.right, prefix + '1', codes)\n" +
                "    return codes\n" +
                "\n" +
                "\n" +
                "def encode(text, codes):\n" +
                "    return ''.join(codes[ch] for ch in text)\n" +
                "\n" +
                "\n" +
                "if __name__ == '__main__':\n" +
                "    text = '" + inputText + "'\n" +
                "    print(f'Input: {text}')\n" +
                "\n" +
                "    # Step 1: Build frequency map\n" +
                "    freq = Counter(text)\n" +
                "    print(f'Frequencies: {dict(freq)}')\n" +
                "\n" +
                "    # Step 2: Build Huffman tree\n" +
                "    root = build_tree(text)\n" +
                "\n" +
                "    # Step 3: Generate codes\n" +
                "    codes = generate_codes(root)\n" +
                "    print(f'Codes: {codes}')\n" +
                "\n" +
                "    # Step 4: Encode\n" +
                "    encoded = encode(text, codes)\n" +
                "    print(f'Encoded: {encoded}')\n" +
                "\n" +
                "    original_bits = len(text) * 8\n" +
                "    encoded_bits = len(encoded)\n" +
                "    ratio = (1 - encoded_bits / original_bits) * 100\n" +
                "    print(f'Compression: {original_bits} bits -> {encoded_bits} bits ({ratio:.1f}% saved)')\n";
    }
}
