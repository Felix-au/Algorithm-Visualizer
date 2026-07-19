package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

public class StackCode implements AlgorithmCode {
    private static final int CAPACITY = 10;

    @Override
    public String getAlgorithmName() {
        return "Stack";
    }

    @Override
    public String getJavaCode() {
        return "public class Main {\n" +
                "    static class ArrayStack {\n" +
                "        int[] a = new int[" + CAPACITY + "];\n" +
                "        int top = -1;\n" +
                "\n" +
                "        boolean isEmpty() { return top < 0; }\n" +
                "        boolean isFull()  { return top + 1 >= a.length; }\n" +
                "\n" +
                "        void push(int x) {\n" +
                "            if (isFull()) throw new RuntimeException(\"overflow\");\n" +
                "            a[++top] = x;\n" +
                "        }\n" +
                "\n" +
                "        int pop() {\n" +
                "            if (isEmpty()) throw new RuntimeException(\"underflow\");\n" +
                "            return a[top--];\n" +
                "        }\n" +
                "\n" +
                "        int peek() {\n" +
                "            if (isEmpty()) throw new RuntimeException(\"underflow\");\n" +
                "            return a[top];\n" +
                "        }\n" +
                "\n" +
                "        int search(int x) {\n" +
                "            for (int i = top; i >= 0; i--)\n" +
                "                if (a[i] == x) return top - i + 1;\n" +
                "            return -1;\n" +
                "        }\n" +
                "\n" +
                "        boolean searchAndPop(int x) {\n" +
                "            int[] auxS = new int[top + 1];\n" +
                "            int auxTop = -1;\n" +
                "            boolean found = false;\n" +
                "            while (!isEmpty()) {\n" +
                "                int v = pop();\n" +
                "                if (!found && v == x) {\n" +
                "                    found = true;\n" +
                "                } else {\n" +
                "                    auxS[++auxTop] = v;\n" +
                "                }\n" +
                "            }\n" +
                "            while (auxTop >= 0) push(auxS[auxTop--]);\n" +
                "            return found;\n" +
                "        }\n" +
                "\n" +
                "        void reverse() {\n" +
                "            int[] auxQ = new int[top + 1];\n" +
                "            int size = 0;\n" +
                "            while (!isEmpty()) auxQ[size++] = pop();\n" +
                "            for (int i = 0; i < size; i++) push(auxQ[i]);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        ArrayStack stack = new ArrayStack();\n" +
                "        stack.push(10);\n" +
                "        stack.push(20);\n" +
                "        stack.push(30);\n" +
                "        System.out.println(\"Peek: \" + stack.peek());\n" +
                "        System.out.println(\"Pop: \" + stack.pop());\n" +
                "        System.out.println(\"Search 20: \" + stack.search(20));\n" +
                "        stack.reverse();\n" +
                "        System.out.println(\"After reverse, peek: \" + stack.peek());\n" +
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
                "    int top;\n" +
                "} ArrayStack;\n" +
                "\n" +
                "void initStack(ArrayStack* s) {\n" +
                "    s->top = -1;\n" +
                "}\n" +
                "\n" +
                "int isEmpty(ArrayStack* s) { return s->top < 0; }\n" +
                "int isFull(ArrayStack* s)  { return s->top + 1 >= CAPACITY; }\n" +
                "\n" +
                "void push(ArrayStack* s, int x) {\n" +
                "    if (isFull(s)) {\n" +
                "        printf(\"Stack overflow\\n\");\n" +
                "        return;\n" +
                "    }\n" +
                "    s->a[++s->top] = x;\n" +
                "}\n" +
                "\n" +
                "int pop(ArrayStack* s) {\n" +
                "    if (isEmpty(s)) {\n" +
                "        printf(\"Stack underflow\\n\");\n" +
                "        return -1;\n" +
                "    }\n" +
                "    return s->a[s->top--];\n" +
                "}\n" +
                "\n" +
                "int peek(ArrayStack* s) {\n" +
                "    if (isEmpty(s)) {\n" +
                "        printf(\"Stack underflow\\n\");\n" +
                "        return -1;\n" +
                "    }\n" +
                "    return s->a[s->top];\n" +
                "}\n" +
                "\n" +
                "int search(ArrayStack* s, int x) {\n" +
                "    for (int i = s->top; i >= 0; i--)\n" +
                "        if (s->a[i] == x) return s->top - i + 1;\n" +
                "    return -1;\n" +
                "}\n" +
                "\n" +
                "int searchAndPop(ArrayStack* s, int x) {\n" +
                "    int auxS[CAPACITY];\n" +
                "    int auxTop = -1;\n" +
                "    int found = 0;\n" +
                "    while (!isEmpty(s)) {\n" +
                "        int v = pop(s);\n" +
                "        if (!found && v == x) {\n" +
                "            found = 1;\n" +
                "        } else {\n" +
                "            auxS[++auxTop] = v;\n" +
                "        }\n" +
                "    }\n" +
                "    while (auxTop >= 0) push(s, auxS[auxTop--]);\n" +
                "    return found;\n" +
                "}\n" +
                "\n" +
                "void reverse(ArrayStack* s) {\n" +
                "    int auxQ[CAPACITY];\n" +
                "    int size = 0;\n" +
                "    while (!isEmpty(s)) auxQ[size++] = pop(s);\n" +
                "    for (int i = 0; i < size; i++) push(s, auxQ[i]);\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "    ArrayStack s;\n" +
                "    initStack(&s);\n" +
                "    push(&s, 10);\n" +
                "    push(&s, 20);\n" +
                "    push(&s, 30);\n" +
                "    printf(\"Peek: %d\\n\", peek(&s));\n" +
                "    printf(\"Pop: %d\\n\", pop(&s));\n" +
                "    printf(\"Search 20: %d\\n\", search(&s, 20));\n" +
                "    reverse(&s);\n" +
                "    printf(\"After reverse, peek: %d\\n\", peek(&s));\n" +
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
                "class ArrayStack {\n" +
                "public:\n" +
                "    int a[CAPACITY];\n" +
                "    int top;\n" +
                "\n" +
                "    ArrayStack() : top(-1) {}\n" +
                "\n" +
                "    bool isEmpty() { return top < 0; }\n" +
                "    bool isFull()  { return top + 1 >= CAPACITY; }\n" +
                "\n" +
                "    void push(int x) {\n" +
                "        if (isFull()) throw runtime_error(\"overflow\");\n" +
                "        a[++top] = x;\n" +
                "    }\n" +
                "\n" +
                "    int pop() {\n" +
                "        if (isEmpty()) throw runtime_error(\"underflow\");\n" +
                "        return a[top--];\n" +
                "    }\n" +
                "\n" +
                "    int peek() {\n" +
                "        if (isEmpty()) throw runtime_error(\"underflow\");\n" +
                "        return a[top];\n" +
                "    }\n" +
                "\n" +
                "    int search(int x) {\n" +
                "        for (int i = top; i >= 0; i--)\n" +
                "            if (a[i] == x) return top - i + 1;\n" +
                "        return -1;\n" +
                "    }\n" +
                "\n" +
                "    bool searchAndPop(int x) {\n" +
                "        int auxS[CAPACITY];\n" +
                "        int auxTop = -1;\n" +
                "        bool found = false;\n" +
                "        while (!isEmpty()) {\n" +
                "            int v = pop();\n" +
                "            if (!found && v == x) {\n" +
                "                found = true;\n" +
                "            } else {\n" +
                "                auxS[++auxTop] = v;\n" +
                "            }\n" +
                "        }\n" +
                "        while (auxTop >= 0) push(auxS[auxTop--]);\n" +
                "        return found;\n" +
                "    }\n" +
                "\n" +
                "    void reverse() {\n" +
                "        int auxQ[CAPACITY];\n" +
                "        int size = 0;\n" +
                "        while (!isEmpty()) auxQ[size++] = pop();\n" +
                "        for (int i = 0; i < size; i++) push(auxQ[i]);\n" +
                "    }\n" +
                "};\n" +
                "\n" +
                "int main() {\n" +
                "    ArrayStack stack;\n" +
                "    stack.push(10);\n" +
                "    stack.push(20);\n" +
                "    stack.push(30);\n" +
                "    cout << \"Peek: \" << stack.peek() << endl;\n" +
                "    cout << \"Pop: \" << stack.pop() << endl;\n" +
                "    cout << \"Search 20: \" << stack.search(20) << endl;\n" +
                "    stack.reverse();\n" +
                "    cout << \"After reverse, peek: \" << stack.peek() << endl;\n" +
                "    return 0;\n" +
                "}\n";
    }

    @Override
    public String getPythonCode() {
        return "class ArrayStack:\n" +
                "    def __init__(self):\n" +
                "        self.a = [0] * " + CAPACITY + "\n" +
                "        self.top = -1\n" +
                "\n" +
                "    def is_empty(self):\n" +
                "        return self.top < 0\n" +
                "\n" +
                "    def is_full(self):\n" +
                "        return self.top + 1 >= len(self.a)\n" +
                "\n" +
                "    def push(self, x):\n" +
                "        if self.is_full():\n" +
                "            raise Exception(\"overflow\")\n" +
                "        self.top += 1\n" +
                "        self.a[self.top] = x\n" +
                "\n" +
                "    def pop(self):\n" +
                "        if self.is_empty():\n" +
                "            raise Exception(\"underflow\")\n" +
                "        val = self.a[self.top]\n" +
                "        self.top -= 1\n" +
                "        return val\n" +
                "\n" +
                "    def peek(self):\n" +
                "        if self.is_empty():\n" +
                "            raise Exception(\"underflow\")\n" +
                "        return self.a[self.top]\n" +
                "\n" +
                "    def search(self, x):\n" +
                "        for i in range(self.top, -1, -1):\n" +
                "            if self.a[i] == x:\n" +
                "                return self.top - i + 1\n" +
                "        return -1\n" +
                "\n" +
                "    def search_and_pop(self, x):\n" +
                "        aux_s = []\n" +
                "        found = False\n" +
                "        while not self.is_empty():\n" +
                "            v = self.pop()\n" +
                "            if not found and v == x:\n" +
                "                found = True\n" +
                "            else:\n" +
                "                aux_s.append(v)\n" +
                "        while aux_s:\n" +
                "            self.push(aux_s.pop())\n" +
                "        return found\n" +
                "\n" +
                "    def reverse(self):\n" +
                "        aux_q = []\n" +
                "        while not self.is_empty():\n" +
                "            aux_q.append(self.pop())\n" +
                "        for v in aux_q:\n" +
                "            self.push(v)\n" +
                "\n" +
                "if __name__ == '__main__':\n" +
                "    stack = ArrayStack()\n" +
                "    stack.push(10)\n" +
                "    stack.push(20)\n" +
                "    stack.push(30)\n" +
                "    print(f\"Peek: {stack.peek()}\")\n" +
                "    print(f\"Pop: {stack.pop()}\")\n" +
                "    print(f\"Search 20: {stack.search(20)}\")\n" +
                "    stack.reverse()\n" +
                "    print(f\"After reverse, peek: {stack.peek()}\")\n";
    }
}
