package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

public class BinaryTreeCode implements AlgorithmCode {

    private int[] treeNodes;

    public BinaryTreeCode() {
        // Default tree values
        this.treeNodes = new int[]{8, 3, 10, 1, 6, 14, 4, 7, 13};
    }

    /**
     * Update parameters for dynamic code generation
     */
    public void updateParameters(int[] treeNodes) {
        this.treeNodes = treeNodes != null ? treeNodes : new int[]{8, 3, 10, 1, 6, 14, 4, 7, 13};
    }

    @Override
    public String getAlgorithmName() {
        return "Binary Tree";
    }

    @Override
    public String getJavaCode() {
        String arrayStr = arrayToString(treeNodes, ", ");
        return "import java.util.*;\n" +
                "\n" +
                "public class Main {\n" +
                "    static class Node {\n" +
                "        int key;\n" +
                "        Node left, right;\n" +
                "        Node(int k) { key = k; }\n" +
                "    }\n" +
                "\n" +
                "    static class BinaryTree {\n" +
                "        Node root;\n" +
                "\n" +
                "        void buildLevelOrder(int[] arr) {\n" +
                "            if (arr == null || arr.length == 0) { root = null; return; }\n" +
                "            List<Node> nodes = new ArrayList<>();\n" +
                "            for (int v : arr) nodes.add(new Node(v));\n" +
                "            for (int i = 0; i < nodes.size(); i++) {\n" +
                "                int li = 2*i + 1, ri = 2*i + 2;\n" +
                "                if (li < nodes.size()) nodes.get(i).left = nodes.get(li);\n" +
                "                if (ri < nodes.size()) nodes.get(i).right = nodes.get(ri);\n" +
                "            }\n" +
                "            root = nodes.get(0);\n" +
                "        }\n" +
                "\n" +
                "        List<Integer> inorder() {\n" +
                "            List<Integer> out = new ArrayList<>();\n" +
                "            inorder(root, out);\n" +
                "            return out;\n" +
                "        }\n" +
                "        void inorder(Node n, List<Integer> out) {\n" +
                "            if (n == null) return;\n" +
                "            inorder(n.left, out);\n" +
                "            out.add(n.key);\n" +
                "            inorder(n.right, out);\n" +
                "        }\n" +
                "\n" +
                "        List<Integer> preorder() {\n" +
                "            List<Integer> out = new ArrayList<>();\n" +
                "            preorder(root, out);\n" +
                "            return out;\n" +
                "        }\n" +
                "        void preorder(Node n, List<Integer> out) {\n" +
                "            if (n == null) return;\n" +
                "            out.add(n.key);\n" +
                "            preorder(n.left, out);\n" +
                "            preorder(n.right, out);\n" +
                "        }\n" +
                "\n" +
                "        List<Integer> postorder() {\n" +
                "            List<Integer> out = new ArrayList<>();\n" +
                "            postorder(root, out);\n" +
                "            return out;\n" +
                "        }\n" +
                "        void postorder(Node n, List<Integer> out) {\n" +
                "            if (n == null) return;\n" +
                "            postorder(n.left, out);\n" +
                "            postorder(n.right, out);\n" +
                "            out.add(n.key);\n" +
                "        }\n" +
                "\n" +
                "        List<Integer> levelOrder() {\n" +
                "            List<Integer> out = new ArrayList<>();\n" +
                "            if (root == null) return out;\n" +
                "            Queue<Node> q = new ArrayDeque<>();\n" +
                "            q.add(root);\n" +
                "            while (!q.isEmpty()) {\n" +
                "                Node cur = q.remove();\n" +
                "                out.add(cur.key);\n" +
                "                if (cur.left != null) q.add(cur.left);\n" +
                "                if (cur.right != null) q.add(cur.right);\n" +
                "            }\n" +
                "            return out;\n" +
                "        }\n" +
                "\n" +
                "        int height() { return height(root); }\n" +
                "        int height(Node n) {\n" +
                "            if (n == null) return -1;\n" +
                "            return 1 + Math.max(height(n.left), height(n.right));\n" +
                "        }\n" +
                "\n" +
                "        void insertLevelOrder(int key) {\n" +
                "            Node nn = new Node(key);\n" +
                "            if (root == null) { root = nn; return; }\n" +
                "            Queue<Node> q = new ArrayDeque<>();\n" +
                "            q.add(root);\n" +
                "            while (!q.isEmpty()) {\n" +
                "                Node cur = q.remove();\n" +
                "                if (cur.left == null) { cur.left = nn; return; }\n" +
                "                else if (cur.right == null) { cur.right = nn; return; }\n" +
                "                else {\n" +
                "                    q.add(cur.left);\n" +
                "                    q.add(cur.right);\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        void deleteByValue(int key) {\n" +
                "            if (root == null) return;\n" +
                "            Node target = null, deepest = null, parentOfDeepest = null;\n" +
                "            Queue<Pair> q = new ArrayDeque<>();\n" +
                "            q.add(new Pair(root, null));\n" +
                "            while (!q.isEmpty()) {\n" +
                "                Pair cur = q.remove();\n" +
                "                Node c = cur.node, p = cur.parent;\n" +
                "                if (target == null && c.key == key) target = c;\n" +
                "                deepest = c;\n" +
                "                parentOfDeepest = p;\n" +
                "                if (c.left != null) q.add(new Pair(c.left, c));\n" +
                "                if (c.right != null) q.add(new Pair(c.right, c));\n" +
                "            }\n" +
                "            if (target == null) return;\n" +
                "            if (target == deepest) {\n" +
                "                unlinkDeepest(parentOfDeepest, deepest);\n" +
                "                return;\n" +
                "            }\n" +
                "            target.key = deepest.key;\n" +
                "            unlinkDeepest(parentOfDeepest, deepest);\n" +
                "        }\n" +
                "\n" +
                "        void unlinkDeepest(Node parent, Node deepest) {\n" +
                "            if (parent == null) {\n" +
                "                if (root.left == null && root.right == null) root = null;\n" +
                "                else if (root.right != null) root.right = null;\n" +
                "                else root.left = null;\n" +
                "                return;\n" +
                "            }\n" +
                "            if (parent.left == deepest) parent.left = null;\n" +
                "            else if (parent.right == deepest) parent.right = null;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    static class Pair {\n" +
                "        Node node, parent;\n" +
                "        Pair(Node n, Node p) { node = n; parent = p; }\n" +
                "    }\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        BinaryTree tree = new BinaryTree();\n" +
                "        int[] arr = {" + arrayStr + "};\n" +
                "        tree.buildLevelOrder(arr);\n" +
                "        System.out.println(\"Inorder: \" + tree.inorder());\n" +
                "        System.out.println(\"Preorder: \" + tree.preorder());\n" +
                "        System.out.println(\"Postorder: \" + tree.postorder());\n" +
                "        System.out.println(\"Level Order: \" + tree.levelOrder());\n" +
                "        System.out.println(\"Height: \" + tree.height());\n" +
                "    }\n" +
                "}\n";
    }

    @Override
    public String getCCode() {
        String arrayStr = arrayToString(treeNodes, ", ");
        return "#include <stdio.h>\n" +
                "#include <stdlib.h>\n" +
                "\n" +
                "typedef struct Node {\n" +
                "    int key;\n" +
                "    struct Node *left, *right;\n" +
                "} Node;\n" +
                "\n" +
                "typedef struct {\n" +
                "    Node** items;\n" +
                "    int front, rear, size, capacity;\n" +
                "} Queue;\n" +
                "\n" +
                "Node* createNode(int k) {\n" +
                "    Node* n = (Node*)malloc(sizeof(Node));\n" +
                "    n->key = k;\n" +
                "    n->left = n->right = NULL;\n" +
                "    return n;\n" +
                "}\n" +
                "\n" +
                "Queue* createQueue(int cap) {\n" +
                "    Queue* q = (Queue*)malloc(sizeof(Queue));\n" +
                "    q->items = (Node**)malloc(cap * sizeof(Node*));\n" +
                "    q->front = q->size = 0;\n" +
                "    q->rear = cap - 1;\n" +
                "    q->capacity = cap;\n" +
                "    return q;\n" +
                "}\n" +
                "\n" +
                "int isEmpty(Queue* q) { return q->size == 0; }\n" +
                "int isFull(Queue* q) { return q->size == q->capacity; }\n" +
                "\n" +
                "void enqueue(Queue* q, Node* n) {\n" +
                "    if (isFull(q)) return;\n" +
                "    q->rear = (q->rear + 1) % q->capacity;\n" +
                "    q->items[q->rear] = n;\n" +
                "    q->size++;\n" +
                "}\n" +
                "\n" +
                "Node* dequeue(Queue* q) {\n" +
                "    if (isEmpty(q)) return NULL;\n" +
                "    Node* n = q->items[q->front];\n" +
                "    q->front = (q->front + 1) % q->capacity;\n" +
                "    q->size--;\n" +
                "    return n;\n" +
                "}\n" +
                "\n" +
                "void inorder(Node* n) {\n" +
                "    if (n == NULL) return;\n" +
                "    inorder(n->left);\n" +
                "    printf(\"%d \", n->key);\n" +
                "    inorder(n->right);\n" +
                "}\n" +
                "\n" +
                "void preorder(Node* n) {\n" +
                "    if (n == NULL) return;\n" +
                "    printf(\"%d \", n->key);\n" +
                "    preorder(n->left);\n" +
                "    preorder(n->right);\n" +
                "}\n" +
                "\n" +
                "void postorder(Node* n) {\n" +
                "    if (n == NULL) return;\n" +
                "    postorder(n->left);\n" +
                "    postorder(n->right);\n" +
                "    printf(\"%d \", n->key);\n" +
                "}\n" +
                "\n" +
                "void levelOrder(Node* root) {\n" +
                "    if (root == NULL) return;\n" +
                "    Queue* q = createQueue(100);\n" +
                "    enqueue(q, root);\n" +
                "    while (!isEmpty(q)) {\n" +
                "        Node* cur = dequeue(q);\n" +
                "        printf(\"%d \", cur->key);\n" +
                "        if (cur->left) enqueue(q, cur->left);\n" +
                "        if (cur->right) enqueue(q, cur->right);\n" +
                "    }\n" +
                "    free(q->items);\n" +
                "    free(q);\n" +
                "}\n" +
                "\n" +
                "int height(Node* n) {\n" +
                "    if (n == NULL) return -1;\n" +
                "    int hl = height(n->left);\n" +
                "    int hr = height(n->right);\n" +
                "    return 1 + (hl > hr ? hl : hr);\n" +
                "}\n" +
                "\n" +
                "Node* buildLevelOrder(int* arr, int n) {\n" +
                "    if (n == 0) return NULL;\n" +
                "    Node** nodes = (Node**)malloc(n * sizeof(Node*));\n" +
                "    for (int i = 0; i < n; i++)\n" +
                "        nodes[i] = createNode(arr[i]);\n" +
                "    for (int i = 0; i < n; i++) {\n" +
                "        int li = 2*i + 1, ri = 2*i + 2;\n" +
                "        if (li < n) nodes[i]->left = nodes[li];\n" +
                "        if (ri < n) nodes[i]->right = nodes[ri];\n" +
                "    }\n" +
                "    Node* root = nodes[0];\n" +
                "    free(nodes);\n" +
                "    return root;\n" +
                "}\n" +
                "\n" +
                "void insertLevelOrder(Node** root, int key) {\n" +
                "    Node* nn = createNode(key);\n" +
                "    if (*root == NULL) { *root = nn; return; }\n" +
                "    Queue* q = createQueue(100);\n" +
                "    enqueue(q, *root);\n" +
                "    while (!isEmpty(q)) {\n" +
                "        Node* cur = dequeue(q);\n" +
                "        if (cur->left == NULL) { cur->left = nn; break; }\n" +
                "        else if (cur->right == NULL) { cur->right = nn; break; }\n" +
                "        else {\n" +
                "            enqueue(q, cur->left);\n" +
                "            enqueue(q, cur->right);\n" +
                "        }\n" +
                "    }\n" +
                "    free(q->items);\n" +
                "    free(q);\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "    int arr[] = {" + arrayStr + "};\n" +
                "    int n = " + treeNodes.length + ";\n" +
                "    Node* root = buildLevelOrder(arr, n);\n" +
                "    printf(\"Inorder: \"); inorder(root); printf(\"\\n\");\n" +
                "    printf(\"Preorder: \"); preorder(root); printf(\"\\n\");\n" +
                "    printf(\"Postorder: \"); postorder(root); printf(\"\\n\");\n" +
                "    printf(\"Level Order: \"); levelOrder(root); printf(\"\\n\");\n" +
                "    printf(\"Height: %d\\n\", height(root));\n" +
                "    return 0;\n" +
                "}\n";
    }

    @Override
    public String getCppCode() {
        String arrayStr = arrayToString(treeNodes, ", ");
        return "#include <iostream>\n" +
                "#include <vector>\n" +
                "#include <queue>\n" +
                "using namespace std;\n" +
                "\n" +
                "class Node {\n" +
                "public:\n" +
                "    int key;\n" +
                "    Node *left, *right;\n" +
                "    Node(int k) : key(k), left(nullptr), right(nullptr) {}\n" +
                "};\n" +
                "\n" +
                "class BinaryTree {\n" +
                "public:\n" +
                "    Node* root;\n" +
                "    BinaryTree() : root(nullptr) {}\n" +
                "\n" +
                "    void buildLevelOrder(vector<int> arr) {\n" +
                "        if (arr.empty()) { root = nullptr; return; }\n" +
                "        vector<Node*> nodes;\n" +
                "        for (int v : arr) nodes.push_back(new Node(v));\n" +
                "        for (int i = 0; i < nodes.size(); i++) {\n" +
                "            int li = 2*i + 1, ri = 2*i + 2;\n" +
                "            if (li < nodes.size()) nodes[i]->left = nodes[li];\n" +
                "            if (ri < nodes.size()) nodes[i]->right = nodes[ri];\n" +
                "        }\n" +
                "        root = nodes[0];\n" +
                "    }\n" +
                "\n" +
                "    vector<int> inorder() {\n" +
                "        vector<int> out;\n" +
                "        inorder(root, out);\n" +
                "        return out;\n" +
                "    }\n" +
                "    void inorder(Node* n, vector<int>& out) {\n" +
                "        if (n == nullptr) return;\n" +
                "        inorder(n->left, out);\n" +
                "        out.push_back(n->key);\n" +
                "        inorder(n->right, out);\n" +
                "    }\n" +
                "\n" +
                "    vector<int> preorder() {\n" +
                "        vector<int> out;\n" +
                "        preorder(root, out);\n" +
                "        return out;\n" +
                "    }\n" +
                "    void preorder(Node* n, vector<int>& out) {\n" +
                "        if (n == nullptr) return;\n" +
                "        out.push_back(n->key);\n" +
                "        preorder(n->left, out);\n" +
                "        preorder(n->right, out);\n" +
                "    }\n" +
                "\n" +
                "    vector<int> postorder() {\n" +
                "        vector<int> out;\n" +
                "        postorder(root, out);\n" +
                "        return out;\n" +
                "    }\n" +
                "    void postorder(Node* n, vector<int>& out) {\n" +
                "        if (n == nullptr) return;\n" +
                "        postorder(n->left, out);\n" +
                "        postorder(n->right, out);\n" +
                "        out.push_back(n->key);\n" +
                "    }\n" +
                "\n" +
                "    vector<int> levelOrder() {\n" +
                "        vector<int> out;\n" +
                "        if (root == nullptr) return out;\n" +
                "        queue<Node*> q;\n" +
                "        q.push(root);\n" +
                "        while (!q.empty()) {\n" +
                "            Node* cur = q.front();\n" +
                "            q.pop();\n" +
                "            out.push_back(cur->key);\n" +
                "            if (cur->left) q.push(cur->left);\n" +
                "            if (cur->right) q.push(cur->right);\n" +
                "        }\n" +
                "        return out;\n" +
                "    }\n" +
                "\n" +
                "    int height() { return height(root); }\n" +
                "    int height(Node* n) {\n" +
                "        if (n == nullptr) return -1;\n" +
                "        return 1 + max(height(n->left), height(n->right));\n" +
                "    }\n" +
                "\n" +
                "    void insertLevelOrder(int key) {\n" +
                "        Node* nn = new Node(key);\n" +
                "        if (root == nullptr) { root = nn; return; }\n" +
                "        queue<Node*> q;\n" +
                "        q.push(root);\n" +
                "        while (!q.empty()) {\n" +
                "            Node* cur = q.front();\n" +
                "            q.pop();\n" +
                "            if (cur->left == nullptr) { cur->left = nn; return; }\n" +
                "            else if (cur->right == nullptr) { cur->right = nn; return; }\n" +
                "            else {\n" +
                "                q.push(cur->left);\n" +
                "                q.push(cur->right);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    struct Pair { Node* node; Node* parent; };\n" +
                "\n" +
                "    void deleteByValue(int key) {\n" +
                "        if (root == nullptr) return;\n" +
                "        Node *target = nullptr, *deepest = nullptr, *parentOfDeepest = nullptr;\n" +
                "        queue<Pair> q;\n" +
                "        q.push({root, nullptr});\n" +
                "        while (!q.empty()) {\n" +
                "            Pair cur = q.front();\n" +
                "            q.pop();\n" +
                "            Node *c = cur.node, *p = cur.parent;\n" +
                "            if (target == nullptr && c->key == key) target = c;\n" +
                "            deepest = c;\n" +
                "            parentOfDeepest = p;\n" +
                "            if (c->left) q.push({c->left, c});\n" +
                "            if (c->right) q.push({c->right, c});\n" +
                "        }\n" +
                "        if (target == nullptr) return;\n" +
                "        if (target == deepest) {\n" +
                "            unlinkDeepest(parentOfDeepest, deepest);\n" +
                "            return;\n" +
                "        }\n" +
                "        target->key = deepest->key;\n" +
                "        unlinkDeepest(parentOfDeepest, deepest);\n" +
                "    }\n" +
                "\n" +
                "    void unlinkDeepest(Node* parent, Node* deepest) {\n" +
                "        if (parent == nullptr) {\n" +
                "            if (root->left == nullptr && root->right == nullptr) root = nullptr;\n" +
                "            else if (root->right) root->right = nullptr;\n" +
                "            else root->left = nullptr;\n" +
                "            return;\n" +
                "        }\n" +
                "        if (parent->left == deepest) parent->left = nullptr;\n" +
                "        else if (parent->right == deepest) parent->right = nullptr;\n" +
                "    }\n" +
                "};\n" +
                "\n" +
                "int main() {\n" +
                "    BinaryTree tree;\n" +
                "    vector<int> arr = {" + arrayStr + "};\n" +
                "    tree.buildLevelOrder(arr);\n" +
                "    cout << \"Inorder: \";\n" +
                "    for (int v : tree.inorder()) cout << v << \" \";\n" +
                "    cout << endl;\n" +
                "    cout << \"Preorder: \";\n" +
                "    for (int v : tree.preorder()) cout << v << \" \";\n" +
                "    cout << endl;\n" +
                "    cout << \"Postorder: \";\n" +
                "    for (int v : tree.postorder()) cout << v << \" \";\n" +
                "    cout << endl;\n" +
                "    cout << \"Level Order: \";\n" +
                "    for (int v : tree.levelOrder()) cout << v << \" \";\n" +
                "    cout << endl;\n" +
                "    cout << \"Height: \" << tree.height() << endl;\n" +
                "    return 0;\n" +
                "}\n";
    }

    @Override
    public String getPythonCode() {
        String arrayStr = arrayToString(treeNodes, ", ");
        return "from collections import deque\n" +
                "\n" +
                "class Node:\n" +
                "    def __init__(self, k):\n" +
                "        self.key = k\n" +
                "        self.left = None\n" +
                "        self.right = None\n" +
                "\n" +
                "class BinaryTree:\n" +
                "    def __init__(self):\n" +
                "        self.root = None\n" +
                "\n" +
                "    def build_level_order(self, arr):\n" +
                "        if not arr:\n" +
                "            self.root = None\n" +
                "            return\n" +
                "        nodes = [Node(v) for v in arr]\n" +
                "        for i in range(len(nodes)):\n" +
                "            li, ri = 2*i + 1, 2*i + 2\n" +
                "            if li < len(nodes):\n" +
                "                nodes[i].left = nodes[li]\n" +
                "            if ri < len(nodes):\n" +
                "                nodes[i].right = nodes[ri]\n" +
                "        self.root = nodes[0]\n" +
                "\n" +
                "    def inorder(self):\n" +
                "        out = []\n" +
                "        self._inorder(self.root, out)\n" +
                "        return out\n" +
                "\n" +
                "    def _inorder(self, n, out):\n" +
                "        if n is None:\n" +
                "            return\n" +
                "        self._inorder(n.left, out)\n" +
                "        out.append(n.key)\n" +
                "        self._inorder(n.right, out)\n" +
                "\n" +
                "    def preorder(self):\n" +
                "        out = []\n" +
                "        self._preorder(self.root, out)\n" +
                "        return out\n" +
                "\n" +
                "    def _preorder(self, n, out):\n" +
                "        if n is None:\n" +
                "            return\n" +
                "        out.append(n.key)\n" +
                "        self._preorder(n.left, out)\n" +
                "        self._preorder(n.right, out)\n" +
                "\n" +
                "    def postorder(self):\n" +
                "        out = []\n" +
                "        self._postorder(self.root, out)\n" +
                "        return out\n" +
                "\n" +
                "    def _postorder(self, n, out):\n" +
                "        if n is None:\n" +
                "            return\n" +
                "        self._postorder(n.left, out)\n" +
                "        self._postorder(n.right, out)\n" +
                "        out.append(n.key)\n" +
                "\n" +
                "    def level_order(self):\n" +
                "        out = []\n" +
                "        if self.root is None:\n" +
                "            return out\n" +
                "        q = deque([self.root])\n" +
                "        while q:\n" +
                "            cur = q.popleft()\n" +
                "            out.append(cur.key)\n" +
                "            if cur.left:\n" +
                "                q.append(cur.left)\n" +
                "            if cur.right:\n" +
                "                q.append(cur.right)\n" +
                "        return out\n" +
                "\n" +
                "    def height(self):\n" +
                "        return self._height(self.root)\n" +
                "\n" +
                "    def _height(self, n):\n" +
                "        if n is None:\n" +
                "            return -1\n" +
                "        return 1 + max(self._height(n.left), self._height(n.right))\n" +
                "\n" +
                "    def insert_level_order(self, key):\n" +
                "        nn = Node(key)\n" +
                "        if self.root is None:\n" +
                "            self.root = nn\n" +
                "            return\n" +
                "        q = deque([self.root])\n" +
                "        while q:\n" +
                "            cur = q.popleft()\n" +
                "            if cur.left is None:\n" +
                "                cur.left = nn\n" +
                "                return\n" +
                "            elif cur.right is None:\n" +
                "                cur.right = nn\n" +
                "                return\n" +
                "            else:\n" +
                "                q.append(cur.left)\n" +
                "                q.append(cur.right)\n" +
                "\n" +
                "    def delete_by_value(self, key):\n" +
                "        if self.root is None:\n" +
                "            return\n" +
                "        target = None\n" +
                "        deepest = None\n" +
                "        parent_of_deepest = None\n" +
                "        q = deque([(self.root, None)])\n" +
                "        while q:\n" +
                "            c, p = q.popleft()\n" +
                "            if target is None and c.key == key:\n" +
                "                target = c\n" +
                "            deepest = c\n" +
                "            parent_of_deepest = p\n" +
                "            if c.left:\n" +
                "                q.append((c.left, c))\n" +
                "            if c.right:\n" +
                "                q.append((c.right, c))\n" +
                "        if target is None:\n" +
                "            return\n" +
                "        if target == deepest:\n" +
                "            self._unlink_deepest(parent_of_deepest, deepest)\n" +
                "            return\n" +
                "        target.key = deepest.key\n" +
                "        self._unlink_deepest(parent_of_deepest, deepest)\n" +
                "\n" +
                "    def _unlink_deepest(self, parent, deepest):\n" +
                "        if parent is None:\n" +
                "            if self.root.left is None and self.root.right is None:\n" +
                "                self.root = None\n" +
                "            elif self.root.right:\n" +
                "                self.root.right = None\n" +
                "            else:\n" +
                "                self.root.left = None\n" +
                "            return\n" +
                "        if parent.left == deepest:\n" +
                "            parent.left = None\n" +
                "        elif parent.right == deepest:\n" +
                "            parent.right = None\n" +
                "\n" +
                "if __name__ == '__main__':\n" +
                "    tree = BinaryTree()\n" +
                "    arr = [" + arrayStr + "]\n" +
                "    tree.build_level_order(arr)\n" +
                "    print(f\"Inorder: {tree.inorder()}\")\n" +
                "    print(f\"Preorder: {tree.preorder()}\")\n" +
                "    print(f\"Postorder: {tree.postorder()}\")\n" +
                "    print(f\"Level Order: {tree.level_order()}\")\n" +
                "    print(f\"Height: {tree.height()}\")\n";
    }

    /**
     * Helper method to convert array to string
     */
    private String arrayToString(int[] arr, String delimiter) {
        if (arr == null || arr.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }
}
