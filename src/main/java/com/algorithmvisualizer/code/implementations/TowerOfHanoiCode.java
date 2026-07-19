package com.algorithmvisualizer.code.implementations;

import com.algorithmvisualizer.code.AlgorithmCode;

/**
 * Tower of Hanoi algorithm code in multiple languages with dynamic parameter syncing
 */
public class TowerOfHanoiCode implements AlgorithmCode {
    
    private int numberOfDisks;
    
    /**
     * Constructor with default values
     */
    public TowerOfHanoiCode() {
        this.numberOfDisks = 4;
    }
    
    /**
     * Update parameters for dynamic code generation
     */
    public void updateParameters(int numberOfDisks) {
        this.numberOfDisks = numberOfDisks;
    }
    
    @Override
    public String getAlgorithmName() {
        return "Tower of Hanoi";
    }
    
    @Override
    public String getJavaCode() {
        return "public class Main {\n" +
               "    static int number_of_Disks = " + numberOfDisks + ";\n" +
               "\n" +
               "    static void hanoi(int n, char from, char to, char aux) {\n" +
               "        if (n == 1) {\n" +
               "            System.out.println(\"Move disk 1 from \" + from + \" to \" + to);\n" +
               "            return;\n" +
               "        }\n" +
               "        hanoi(n - 1, from, aux, to);\n" +
               "        System.out.println(\"Move disk \" + n + \" from \" + from + \" to \" + to);\n" +
               "        hanoi(n - 1, aux, to, from);\n" +
               "    }\n" +
               "\n" +
               "    public static void main(String[] args) {\n" +
               "        hanoi(number_of_Disks, 'A', 'C', 'B');\n" +
               "    }\n" +
               "}\n";
    }
    
    @Override
    public String getCCode() {
        return "#include <stdio.h>\n" +
               "\n" +
               "#define NUMBER_OF_DISKS " + numberOfDisks + "\n" +
               "\n" +
               "void hanoi(int n, char from, char to, char aux) {\n" +
               "    if (n == 1) {\n" +
               "        printf(\"Move disk 1 from %c to %c\\n\", from, to);\n" +
               "        return;\n" +
               "    }\n" +
               "    hanoi(n - 1, from, aux, to);\n" +
               "    printf(\"Move disk %d from %c to %c\\n\", n, from, to);\n" +
               "    hanoi(n - 1, aux, to, from);\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    hanoi(NUMBER_OF_DISKS, 'A', 'C', 'B');\n" +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getCppCode() {
        return "#include <iostream>\n" +
               "using namespace std;\n" +
               "\n" +
               "const int NUMBER_OF_DISKS = " + numberOfDisks + ";\n" +
               "\n" +
               "void hanoi(int n, char from, char to, char aux) {\n" +
               "    if (n == 1) {\n" +
               "        cout << \"Move disk 1 from \" << from << \" to \" << to << endl;\n" +
               "        return;\n" +
               "    }\n" +
               "    hanoi(n - 1, from, aux, to);\n" +
               "    cout << \"Move disk \" << n << \" from \" << from << \" to \" << to << endl;\n" +
               "    hanoi(n - 1, aux, to, from);\n" +
               "}\n" +
               "\n" +
               "int main() {\n" +
               "    hanoi(NUMBER_OF_DISKS, 'A', 'C', 'B');\n" +
               "    return 0;\n" +
               "}\n";
    }
    
    @Override
    public String getPythonCode() {
        return "NUMBER_OF_DISKS = " + numberOfDisks + "\n" +
               "\n" +
               "def hanoi(n, from_rod, to_rod, aux_rod):\n" +
               "    if n == 1:\n" +
               "        print(f\"Move disk 1 from {from_rod} to {to_rod}\")\n" +
               "        return\n" +
               "    hanoi(n - 1, from_rod, aux_rod, to_rod)\n" +
               "    print(f\"Move disk {n} from {from_rod} to {to_rod}\")\n" +
               "    hanoi(n - 1, aux_rod, to_rod, from_rod)\n" +
               "\n" +
               "hanoi(NUMBER_OF_DISKS, 'A', 'C', 'B')\n";
    }
}
