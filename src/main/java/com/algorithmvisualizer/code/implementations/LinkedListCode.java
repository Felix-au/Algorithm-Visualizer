package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

public class LinkedListCode implements AlgorithmCode {

    @Override
    public String getAlgorithmName() {
        return "Linked List";
    }

    @Override
    public String getJavaCode() {
        return "public class Main {\n" +
                "    static class Node {\n" +
                "        int data;\n" +
                "        Node next;\n" +
                "        Node(int d) { data = d; }\n" +
                "    }\n" +
                "\n" +
                "    static class LinkedList {\n" +
                "        Node head;\n" +
                "\n" +
                "        void insertAtEnd(int data) {\n" +
                "            Node nn = new Node(data);\n" +
                "            if (head == null) {\n" +
                "                head = nn;\n" +
                "                return;\n" +
                "            }\n" +
                "            Node cur = head;\n" +
                "            while (cur.next != null) cur = cur.next;\n" +
                "            cur.next = nn;\n" +
                "        }\n" +
                "\n" +
                "        void insertAtBeginning(int data) {\n" +
                "            Node nn = new Node(data);\n" +
                "            nn.next = head;\n" +
                "            head = nn;\n" +
                "        }\n" +
                "\n" +
                "        void insertAtPosition(int data, int pos) {\n" +
                "            if (pos == 0) {\n" +
                "                insertAtBeginning(data);\n" +
                "                return;\n" +
                "            }\n" +
                "            Node nn = new Node(data);\n" +
                "            Node cur = head;\n" +
                "            for (int i = 0; i < pos - 1 && cur != null; i++)\n" +
                "                cur = cur.next;\n" +
                "            if (cur == null) throw new RuntimeException(\"Invalid position\");\n" +
                "            nn.next = cur.next;\n" +
                "            cur.next = nn;\n" +
                "        }\n" +
                "\n" +
                "        void deleteByValue(int data) {\n" +
                "            if (head == null) return;\n" +
                "            if (head.data == data) {\n" +
                "                head = head.next;\n" +
                "                return;\n" +
                "            }\n" +
                "            Node cur = head;\n" +
                "            while (cur.next != null && cur.next.data != data)\n" +
                "                cur = cur.next;\n" +
                "            if (cur.next != null) cur.next = cur.next.next;\n" +
                "        }\n" +
                "\n" +
                "        boolean search(int data) {\n" +
                "            Node cur = head;\n" +
                "            while (cur != null) {\n" +
                "                if (cur.data == data) return true;\n" +
                "                cur = cur.next;\n" +
                "            }\n" +
                "            return false;\n" +
                "        }\n" +
                "\n" +
                "        void reverse() {\n" +
                "            Node prev = null, cur = head, next;\n" +
                "            while (cur != null) {\n" +
                "                next = cur.next;\n" +
                "                cur.next = prev;\n" +
                "                prev = cur;\n" +
                "                cur = next;\n" +
                "            }\n" +
                "            head = prev;\n" +
                "        }\n" +
                "\n" +
                "        int size() {\n" +
                "            int count = 0;\n" +
                "            Node cur = head;\n" +
                "            while (cur != null) {\n" +
                "                count++;\n" +
                "                cur = cur.next;\n" +
                "            }\n" +
                "            return count;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        LinkedList list = new LinkedList();\n" +
                "        list.insertAtEnd(10);\n" +
                "        list.insertAtEnd(20);\n" +
                "        list.insertAtEnd(30);\n" +
                "        list.insertAtBeginning(5);\n" +
                "        System.out.println(\"Size: \" + list.size());\n" +
                "        System.out.println(\"Search 20: \" + list.search(20));\n" +
                "        list.reverse();\n" +
                "        list.deleteByValue(20);\n" +
                "        System.out.println(\"Size after delete: \" + list.size());\n" +
                "    }\n" +
                "}\n";
    }

    @Override
    public String getCCode() {
        return "#include <stdio.h>\n" +
                "#include <stdlib.h>\n" +
                "\n" +
                "typedef struct Node {\n" +
                "    int data;\n" +
                "    struct Node* next;\n" +
                "} Node;\n" +
                "\n" +
                "Node* createNode(int data) {\n" +
                "    Node* nn = (Node*)malloc(sizeof(Node));\n" +
                "    nn->data = data;\n" +
                "    nn->next = NULL;\n" +
                "    return nn;\n" +
                "}\n" +
                "\n" +
                "void insertAtEnd(Node** head, int data) {\n" +
                "    Node* nn = createNode(data);\n" +
                "    if (*head == NULL) {\n" +
                "        *head = nn;\n" +
                "        return;\n" +
                "    }\n" +
                "    Node* cur = *head;\n" +
                "    while (cur->next != NULL) cur = cur->next;\n" +
                "    cur->next = nn;\n" +
                "}\n" +
                "\n" +
                "void insertAtBeginning(Node** head, int data) {\n" +
                "    Node* nn = createNode(data);\n" +
                "    nn->next = *head;\n" +
                "    *head = nn;\n" +
                "}\n" +
                "\n" +
                "void insertAtPosition(Node** head, int data, int pos) {\n" +
                "    if (pos == 0) {\n" +
                "        insertAtBeginning(head, data);\n" +
                "        return;\n" +
                "    }\n" +
                "    Node* nn = createNode(data);\n" +
                "    Node* cur = *head;\n" +
                "    for (int i = 0; i < pos - 1 && cur != NULL; i++)\n" +
                "        cur = cur->next;\n" +
                "    if (cur == NULL) {\n" +
                "        printf(\"Invalid position\\n\");\n" +
                "        free(nn);\n" +
                "        return;\n" +
                "    }\n" +
                "    nn->next = cur->next;\n" +
                "    cur->next = nn;\n" +
                "}\n" +
                "\n" +
                "void deleteByValue(Node** head, int data) {\n" +
                "    if (*head == NULL) return;\n" +
                "    if ((*head)->data == data) {\n" +
                "        Node* temp = *head;\n" +
                "        *head = (*head)->next;\n" +
                "        free(temp);\n" +
                "        return;\n" +
                "    }\n" +
                "    Node* cur = *head;\n" +
                "    while (cur->next != NULL && cur->next->data != data)\n" +
                "        cur = cur->next;\n" +
                "    if (cur->next != NULL) {\n" +
                "        Node* temp = cur->next;\n" +
                "        cur->next = cur->next->next;\n" +
                "        free(temp);\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "int search(Node* head, int data) {\n" +
                "    Node* cur = head;\n" +
                "    while (cur != NULL) {\n" +
                "        if (cur->data == data) return 1;\n" +
                "        cur = cur->next;\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n" +
                "\n" +
                "void reverse(Node** head) {\n" +
                "    Node *prev = NULL, *cur = *head, *next;\n" +
                "    while (cur != NULL) {\n" +
                "        next = cur->next;\n" +
                "        cur->next = prev;\n" +
                "        prev = cur;\n" +
                "        cur = next;\n" +
                "    }\n" +
                "    *head = prev;\n" +
                "}\n" +
                "\n" +
                "int size(Node* head) {\n" +
                "    int count = 0;\n" +
                "    Node* cur = head;\n" +
                "    while (cur != NULL) {\n" +
                "        count++;\n" +
                "        cur = cur->next;\n" +
                "    }\n" +
                "    return count;\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "    Node* head = NULL;\n" +
                "    insertAtEnd(&head, 10);\n" +
                "    insertAtEnd(&head, 20);\n" +
                "    insertAtEnd(&head, 30);\n" +
                "    insertAtBeginning(&head, 5);\n" +
                "    printf(\"Size: %d\\n\", size(head));\n" +
                "    printf(\"Search 20: %d\\n\", search(head, 20));\n" +
                "    reverse(&head);\n" +
                "    deleteByValue(&head, 20);\n" +
                "    printf(\"Size after delete: %d\\n\", size(head));\n" +
                "    return 0;\n" +
                "}\n";
    }

    @Override
    public String getCppCode() {
        return "#include <iostream>\n" +
                "#include <stdexcept>\n" +
                "using namespace std;\n" +
                "\n" +
                "class Node {\n" +
                "public:\n" +
                "    int data;\n" +
                "    Node* next;\n" +
                "    Node(int d) : data(d), next(nullptr) {}\n" +
                "};\n" +
                "\n" +
                "class LinkedList {\n" +
                "public:\n" +
                "    Node* head;\n" +
                "    LinkedList() : head(nullptr) {}\n" +
                "\n" +
                "    void insertAtEnd(int data) {\n" +
                "        Node* nn = new Node(data);\n" +
                "        if (head == nullptr) {\n" +
                "            head = nn;\n" +
                "            return;\n" +
                "        }\n" +
                "        Node* cur = head;\n" +
                "        while (cur->next != nullptr) cur = cur->next;\n" +
                "        cur->next = nn;\n" +
                "    }\n" +
                "\n" +
                "    void insertAtBeginning(int data) {\n" +
                "        Node* nn = new Node(data);\n" +
                "        nn->next = head;\n" +
                "        head = nn;\n" +
                "    }\n" +
                "\n" +
                "    void insertAtPosition(int data, int pos) {\n" +
                "        if (pos == 0) {\n" +
                "            insertAtBeginning(data);\n" +
                "            return;\n" +
                "        }\n" +
                "        Node* nn = new Node(data);\n" +
                "        Node* cur = head;\n" +
                "        for (int i = 0; i < pos - 1 && cur != nullptr; i++)\n" +
                "            cur = cur->next;\n" +
                "        if (cur == nullptr) throw runtime_error(\"Invalid position\");\n" +
                "        nn->next = cur->next;\n" +
                "        cur->next = nn;\n" +
                "    }\n" +
                "\n" +
                "    void deleteByValue(int data) {\n" +
                "        if (head == nullptr) return;\n" +
                "        if (head->data == data) {\n" +
                "            Node* temp = head;\n" +
                "            head = head->next;\n" +
                "            delete temp;\n" +
                "            return;\n" +
                "        }\n" +
                "        Node* cur = head;\n" +
                "        while (cur->next != nullptr && cur->next->data != data)\n" +
                "            cur = cur->next;\n" +
                "        if (cur->next != nullptr) {\n" +
                "            Node* temp = cur->next;\n" +
                "            cur->next = cur->next->next;\n" +
                "            delete temp;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    bool search(int data) {\n" +
                "        Node* cur = head;\n" +
                "        while (cur != nullptr) {\n" +
                "            if (cur->data == data) return true;\n" +
                "            cur = cur->next;\n" +
                "        }\n" +
                "        return false;\n" +
                "    }\n" +
                "\n" +
                "    void reverse() {\n" +
                "        Node *prev = nullptr, *cur = head, *next;\n" +
                "        while (cur != nullptr) {\n" +
                "            next = cur->next;\n" +
                "            cur->next = prev;\n" +
                "            prev = cur;\n" +
                "            cur = next;\n" +
                "        }\n" +
                "        head = prev;\n" +
                "    }\n" +
                "\n" +
                "    int size() {\n" +
                "        int count = 0;\n" +
                "        Node* cur = head;\n" +
                "        while (cur != nullptr) {\n" +
                "            count++;\n" +
                "            cur = cur->next;\n" +
                "        }\n" +
                "        return count;\n" +
                "    }\n" +
                "};\n" +
                "\n" +
                "int main() {\n" +
                "    LinkedList list;\n" +
                "    list.insertAtEnd(10);\n" +
                "    list.insertAtEnd(20);\n" +
                "    list.insertAtEnd(30);\n" +
                "    list.insertAtBeginning(5);\n" +
                "    cout << \"Size: \" << list.size() << endl;\n" +
                "    cout << \"Search 20: \" << list.search(20) << endl;\n" +
                "    list.reverse();\n" +
                "    list.deleteByValue(20);\n" +
                "    cout << \"Size after delete: \" << list.size() << endl;\n" +
                "    return 0;\n" +
                "}\n";
    }

    @Override
    public String getPythonCode() {
        return "class Node:\n" +
                "    def __init__(self, data):\n" +
                "        self.data = data\n" +
                "        self.next = None\n" +
                "\n" +
                "class LinkedList:\n" +
                "    def __init__(self):\n" +
                "        self.head = None\n" +
                "\n" +
                "    def insert_at_end(self, data):\n" +
                "        nn = Node(data)\n" +
                "        if self.head is None:\n" +
                "            self.head = nn\n" +
                "            return\n" +
                "        cur = self.head\n" +
                "        while cur.next is not None:\n" +
                "            cur = cur.next\n" +
                "        cur.next = nn\n" +
                "\n" +
                "    def insert_at_beginning(self, data):\n" +
                "        nn = Node(data)\n" +
                "        nn.next = self.head\n" +
                "        self.head = nn\n" +
                "\n" +
                "    def insert_at_position(self, data, pos):\n" +
                "        if pos == 0:\n" +
                "            self.insert_at_beginning(data)\n" +
                "            return\n" +
                "        nn = Node(data)\n" +
                "        cur = self.head\n" +
                "        for i in range(pos - 1):\n" +
                "            if cur is None:\n" +
                "                raise Exception(\"Invalid position\")\n" +
                "            cur = cur.next\n" +
                "        if cur is None:\n" +
                "            raise Exception(\"Invalid position\")\n" +
                "        nn.next = cur.next\n" +
                "        cur.next = nn\n" +
                "\n" +
                "    def delete_by_value(self, data):\n" +
                "        if self.head is None:\n" +
                "            return\n" +
                "        if self.head.data == data:\n" +
                "            self.head = self.head.next\n" +
                "            return\n" +
                "        cur = self.head\n" +
                "        while cur.next is not None and cur.next.data != data:\n" +
                "            cur = cur.next\n" +
                "        if cur.next is not None:\n" +
                "            cur.next = cur.next.next\n" +
                "\n" +
                "    def search(self, data):\n" +
                "        cur = self.head\n" +
                "        while cur is not None:\n" +
                "            if cur.data == data:\n" +
                "                return True\n" +
                "            cur = cur.next\n" +
                "        return False\n" +
                "\n" +
                "    def reverse(self):\n" +
                "        prev = None\n" +
                "        cur = self.head\n" +
                "        while cur is not None:\n" +
                "            next_node = cur.next\n" +
                "            cur.next = prev\n" +
                "            prev = cur\n" +
                "            cur = next_node\n" +
                "        self.head = prev\n" +
                "\n" +
                "    def size(self):\n" +
                "        count = 0\n" +
                "        cur = self.head\n" +
                "        while cur is not None:\n" +
                "            count += 1\n" +
                "            cur = cur.next\n" +
                "        return count\n" +
                "\n" +
                "if __name__ == '__main__':\n" +
                "    lst = LinkedList()\n" +
                "    lst.insert_at_end(10)\n" +
                "    lst.insert_at_end(20)\n" +
                "    lst.insert_at_end(30)\n" +
                "    lst.insert_at_beginning(5)\n" +
                "    print(f\"Size: {lst.size()}\")\n" +
                "    print(f\"Search 20: {lst.search(20)}\")\n" +
                "    lst.reverse()\n" +
                "    lst.delete_by_value(20)\n" +
                "    print(f\"Size after delete: {lst.size()}\")\n";
    }
}
