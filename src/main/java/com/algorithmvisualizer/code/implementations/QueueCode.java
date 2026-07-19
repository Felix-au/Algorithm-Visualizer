package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

public class QueueCode implements AlgorithmCode {
    private static final int CAPACITY = 10;

    @Override
    public String getAlgorithmName() {
        return "Queue";
    }

    @Override
    public String getJavaCode() {
        return "public class Main {\n" +
                "    static class ArrayQueue {\n" +
                "        int[] a = new int[" + CAPACITY + "];\n" +
                "        int size = 0;\n" +
                "\n" +
                "        boolean isEmpty() { return size == 0; }\n" +
                "        boolean isFull()  { return size >= a.length; }\n" +
                "\n" +
                "        void enqueue(int x) {\n" +
                "            if (isFull()) throw new RuntimeException(\"overflow\");\n" +
                "            a[size++] = x;\n" +
                "        }\n" +
                "\n" +
                "        int dequeue() {\n" +
                "            if (isEmpty()) throw new RuntimeException(\"underflow\");\n" +
                "            int val = a[0];\n" +
                "            for (int i = 1; i < size; i++) a[i-1] = a[i];\n" +
                "            size--;\n" +
                "            return val;\n" +
                "        }\n" +
                "\n" +
                "        int search(int x) {\n" +
                "            for (int i = 0; i < size; i++)\n" +
                "                if (a[i] == x) return i;\n" +
                "            return -1;\n" +
                "        }\n" +
                "\n" +
                "        boolean searchAndDequeue(int x) {\n" +
                "            int n = size;\n" +
                "            int[] auxQ = new int[n];\n" +
                "            int k = 0;\n" +
                "            boolean removed = false;\n" +
                "            for (int i = 0; i < n; i++) {\n" +
                "                int v = dequeue();\n" +
                "                if (!removed && v == x) {\n" +
                "                    removed = true;\n" +
                "                } else {\n" +
                "                    auxQ[k++] = v;\n" +
                "                }\n" +
                "            }\n" +
                "            for (int i = 0; i < k; i++) enqueue(auxQ[i]);\n" +
                "            return removed;\n" +
                "        }\n" +
                "\n" +
                "        void reverse() {\n" +
                "            int[] auxS = new int[size];\n" +
                "            int top = -1;\n" +
                "            while (!isEmpty()) auxS[++top] = dequeue();\n" +
                "            while (top >= 0) enqueue(auxS[top--]);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        ArrayQueue queue = new ArrayQueue();\n" +
                "        queue.enqueue(10);\n" +
                "        queue.enqueue(20);\n" +
                "        queue.enqueue(30);\n" +
                "        System.out.println(\"Dequeue: \" + queue.dequeue());\n" +
                "        System.out.println(\"Search 20: \" + queue.search(20));\n" +
                "        queue.reverse();\n" +
                "        System.out.println(\"After reverse, dequeue: \" + queue.dequeue());\n" +
                "    }\n" +
                "}\n";
    }

    @Override
    public String getCCode() {
        return "#include <stdio.h>\n" +
                "#include <stdlib.h>\n" +
                "\n" +
                "#define CAPACITY " + CAPACITY + "\n" +
                "\n" +
                "typedef struct {\n" +
                "    int a[CAPACITY];\n" +
                "    int size;\n" +
                "} ArrayQueue;\n" +
                "\n" +
                "void initQueue(ArrayQueue* q) {\n" +
                "    q->size = 0;\n" +
                "}\n" +
                "\n" +
                "int isEmpty(ArrayQueue* q) { return q->size == 0; }\n" +
                "int isFull(ArrayQueue* q)  { return q->size >= CAPACITY; }\n" +
                "\n" +
                "void enqueue(ArrayQueue* q, int x) {\n" +
                "    if (isFull(q)) {\n" +
                "        printf(\"Queue overflow\\n\");\n" +
                "        return;\n" +
                "    }\n" +
                "    q->a[q->size++] = x;\n" +
                "}\n" +
                "\n" +
                "int dequeue(ArrayQueue* q) {\n" +
                "    if (isEmpty(q)) {\n" +
                "        printf(\"Queue underflow\\n\");\n" +
                "        return -1;\n" +
                "    }\n" +
                "    int val = q->a[0];\n" +
                "    for (int i = 1; i < q->size; i++)\n" +
                "        q->a[i-1] = q->a[i];\n" +
                "    q->size--;\n" +
                "    return val;\n" +
                "}\n" +
                "\n" +
                "int search(ArrayQueue* q, int x) {\n" +
                "    for (int i = 0; i < q->size; i++)\n" +
                "        if (q->a[i] == x) return i;\n" +
                "    return -1;\n" +
                "}\n" +
                "\n" +
                "int searchAndDequeue(ArrayQueue* q, int x) {\n" +
                "    int n = q->size;\n" +
                "    int auxQ[CAPACITY];\n" +
                "    int k = 0;\n" +
                "    int removed = 0;\n" +
                "    for (int i = 0; i < n; i++) {\n" +
                "        int v = dequeue(q);\n" +
                "        if (!removed && v == x) {\n" +
                "            removed = 1;\n" +
                "        } else {\n" +
                "            auxQ[k++] = v;\n" +
                "        }\n" +
                "    }\n" +
                "    for (int i = 0; i < k; i++) enqueue(q, auxQ[i]);\n" +
                "    return removed;\n" +
                "}\n" +
                "\n" +
                "void reverse(ArrayQueue* q) {\n" +
                "    int auxS[CAPACITY];\n" +
                "    int top = -1;\n" +
                "    while (!isEmpty(q)) auxS[++top] = dequeue(q);\n" +
                "    while (top >= 0) enqueue(q, auxS[top--]);\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "    ArrayQueue q;\n" +
                "    initQueue(&q);\n" +
                "    enqueue(&q, 10);\n" +
                "    enqueue(&q, 20);\n" +
                "    enqueue(&q, 30);\n" +
                "    printf(\"Dequeue: %d\\n\", dequeue(&q));\n" +
                "    printf(\"Search 20: %d\\n\", search(&q, 20));\n" +
                "    reverse(&q);\n" +
                "    printf(\"After reverse, dequeue: %d\\n\", dequeue(&q));\n" +
                "    return 0;\n" +
                "}\n";
    }

    @Override
    public String getCppCode() {
        return "#include <iostream>\n" +
                "#include <stdexcept>\n" +
                "using namespace std;\n" +
                "\n" +
                "const int CAPACITY = " + CAPACITY + ";\n" +
                "\n" +
                "class ArrayQueue {\n" +
                "public:\n" +
                "    int a[CAPACITY];\n" +
                "    int size;\n" +
                "\n" +
                "    ArrayQueue() : size(0) {}\n" +
                "\n" +
                "    bool isEmpty() { return size == 0; }\n" +
                "    bool isFull()  { return size >= CAPACITY; }\n" +
                "\n" +
                "    void enqueue(int x) {\n" +
                "        if (isFull()) throw runtime_error(\"overflow\");\n" +
                "        a[size++] = x;\n" +
                "    }\n" +
                "\n" +
                "    int dequeue() {\n" +
                "        if (isEmpty()) throw runtime_error(\"underflow\");\n" +
                "        int val = a[0];\n" +
                "        for (int i = 1; i < size; i++) a[i-1] = a[i];\n" +
                "        size--;\n" +
                "        return val;\n" +
                "    }\n" +
                "\n" +
                "    int search(int x) {\n" +
                "        for (int i = 0; i < size; i++)\n" +
                "            if (a[i] == x) return i;\n" +
                "        return -1;\n" +
                "    }\n" +
                "\n" +
                "    bool searchAndDequeue(int x) {\n" +
                "        int n = size;\n" +
                "        int auxQ[CAPACITY];\n" +
                "        int k = 0;\n" +
                "        bool removed = false;\n" +
                "        for (int i = 0; i < n; i++) {\n" +
                "            int v = dequeue();\n" +
                "            if (!removed && v == x) {\n" +
                "                removed = true;\n" +
                "            } else {\n" +
                "                auxQ[k++] = v;\n" +
                "            }\n" +
                "        }\n" +
                "        for (int i = 0; i < k; i++) enqueue(auxQ[i]);\n" +
                "        return removed;\n" +
                "    }\n" +
                "\n" +
                "    void reverse() {\n" +
                "        int auxS[CAPACITY];\n" +
                "        int top = -1;\n" +
                "        while (!isEmpty()) auxS[++top] = dequeue();\n" +
                "        while (top >= 0) enqueue(auxS[top--]);\n" +
                "    }\n" +
                "};\n" +
                "\n" +
                "int main() {\n" +
                "    ArrayQueue queue;\n" +
                "    queue.enqueue(10);\n" +
                "    queue.enqueue(20);\n" +
                "    queue.enqueue(30);\n" +
                "    cout << \"Dequeue: \" << queue.dequeue() << endl;\n" +
                "    cout << \"Search 20: \" << queue.search(20) << endl;\n" +
                "    queue.reverse();\n" +
                "    cout << \"After reverse, dequeue: \" << queue.dequeue() << endl;\n" +
                "    return 0;\n" +
                "}\n";
    }

    @Override
    public String getPythonCode() {
        return "class ArrayQueue:\n" +
                "    def __init__(self):\n" +
                "        self.a = [0] * " + CAPACITY + "\n" +
                "        self.size = 0\n" +
                "\n" +
                "    def is_empty(self):\n" +
                "        return self.size == 0\n" +
                "\n" +
                "    def is_full(self):\n" +
                "        return self.size >= len(self.a)\n" +
                "\n" +
                "    def enqueue(self, x):\n" +
                "        if self.is_full():\n" +
                "            raise Exception(\"overflow\")\n" +
                "        self.a[self.size] = x\n" +
                "        self.size += 1\n" +
                "\n" +
                "    def dequeue(self):\n" +
                "        if self.is_empty():\n" +
                "            raise Exception(\"underflow\")\n" +
                "        val = self.a[0]\n" +
                "        for i in range(1, self.size):\n" +
                "            self.a[i-1] = self.a[i]\n" +
                "        self.size -= 1\n" +
                "        return val\n" +
                "\n" +
                "    def search(self, x):\n" +
                "        for i in range(self.size):\n" +
                "            if self.a[i] == x:\n" +
                "                return i\n" +
                "        return -1\n" +
                "\n" +
                "    def search_and_dequeue(self, x):\n" +
                "        n = self.size\n" +
                "        aux_q = []\n" +
                "        removed = False\n" +
                "        for i in range(n):\n" +
                "            v = self.dequeue()\n" +
                "            if not removed and v == x:\n" +
                "                removed = True\n" +
                "            else:\n" +
                "                aux_q.append(v)\n" +
                "        for v in aux_q:\n" +
                "            self.enqueue(v)\n" +
                "        return removed\n" +
                "\n" +
                "    def reverse(self):\n" +
                "        aux_s = []\n" +
                "        while not self.is_empty():\n" +
                "            aux_s.append(self.dequeue())\n" +
                "        while aux_s:\n" +
                "            self.enqueue(aux_s.pop())\n" +
                "\n" +
                "if __name__ == '__main__':\n" +
                "    queue = ArrayQueue()\n" +
                "    queue.enqueue(10)\n" +
                "    queue.enqueue(20)\n" +
                "    queue.enqueue(30)\n" +
                "    print(f\"Dequeue: {queue.dequeue()}\")\n" +
                "    print(f\"Search 20: {queue.search(20)}\")\n" +
                "    queue.reverse()\n" +
                "    print(f\"After reverse, dequeue: {queue.dequeue()}\")\n";
    }
}
