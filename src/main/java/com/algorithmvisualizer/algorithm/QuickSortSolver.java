package com.algorithmvisualizer.algorithm;

import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

/**
 * Quick Sort solver with tree-based visualization and granular partition steps.
 * Shows i, j pointers, comparisons, swaps, and pivot placement.
 */
public class QuickSortSolver {

    public enum PivotStrategy {
        FIRST,              // Use first element as pivot
        LAST,               // Use last element as pivot (default)
        MIDDLE,             // Use middle element as pivot
        RANDOM,             // Use random element as pivot
        MEDIAN_OF_THREE     // Use median of first, middle, last
    }

    public enum StepType {
        // Initialization
        INIT,                       // Start quick sort
        
        // Partition phase (very granular)
        PARTITION_START,            // Start partitioning a range
        SELECT_PIVOT,               // Select and highlight pivot
        INIT_POINTERS,              // Initialize i and j pointers
        MOVE_J,                     // Move j pointer
        COMPARE,                    // Compare arr[j] with pivot
        INCREMENT_I,                // Increment i pointer
        SWAP,                       // Swap arr[i] and arr[j]
        PLACE_PIVOT,                // Place pivot in final position
        PARTITION_COMPLETE,         // Partition done, pivot sorted
        
        // Recursion
        PUSH_RIGHT,                 // Push right subarray to stack
        PUSH_LEFT,                  // Push left subarray to stack
        POP_RANGE,                  // Pop range from stack
        BACKTRACK,                  // Return from recursion
        
        // Completion
        DONE                        // All elements sorted
    }

    public interface StepListener {
        void onStep(StepType type, State state);
    }

    /**
     * Tree node representing a partition call in quick sort
     */
    public static class TreeNode {
        public int[] data;              // Subarray contents
        public int left;                // Left index in original array
        public int right;               // Right index in original array
        public int pivotIndex;          // Pivot index in this partition
        public int pivotValue;          // Pivot value
        public int level;               // Recursion depth
        public TreeNode leftChild;      // Left partition (< pivot)
        public TreeNode rightChild;     // Right partition (> pivot)
        public boolean isPartitioned;   // Has this been partitioned?
        public boolean isActive;        // Currently processing?
        public boolean isPivotSorted;   // Is pivot in final position?
        public boolean isLeaf;          // Single element (sorted)
        
        // Visual properties
        public double x, y;
        public double width, height;
        
        public TreeNode(int[] data, int left, int right, int level) {
            this.data = data;
            this.left = left;
            this.right = right;
            this.level = level;
            this.isPartitioned = false;
            this.isActive = false;
            this.isPivotSorted = false;
            this.isLeaf = (left == right);
        }
    }

    /**
     * Complete state snapshot for step-back support
     */
    public static class State {
        public int[] array;              // Current array state
        
        // Current partition context
        public int left;                 // Left boundary
        public int right;                // Right boundary
        public int pivotIndex;           // Pivot index
        public int pivotValue;           // Pivot value
        
        // Partition pointers
        public int i;                    // Partition pointer i
        public int j;                    // Partition pointer j
        
        // Tree tracking
        public TreeNode root;            // Root of recursion tree
        public TreeNode currentNode;     // Currently processing node
        
        // Phase tracking
        public String phase;             // "PARTITION" or "RECURSE" or "DONE"
        public int recursionDepth;       // Current recursion depth
        
        // Completion
        public boolean done;
        
        // Deep copy constructor
        public State(State other) {
            this.array = other.array != null ? Arrays.copyOf(other.array, other.array.length) : null;
            this.left = other.left;
            this.right = other.right;
            this.pivotIndex = other.pivotIndex;
            this.pivotValue = other.pivotValue;
            this.i = other.i;
            this.j = other.j;
            this.root = copyTree(other.root);
            this.currentNode = other.currentNode != null ? findNode(this.root, other.currentNode.left, other.currentNode.right) : null;
            this.phase = other.phase;
            this.recursionDepth = other.recursionDepth;
            this.done = other.done;
        }
        
        public State() {}
        
        // Deep copy tree structure
        private static TreeNode copyTree(TreeNode node) {
            if (node == null) return null;
            TreeNode copy = new TreeNode(
                node.data != null ? Arrays.copyOf(node.data, node.data.length) : null,
                node.left, node.right, node.level
            );
            copy.pivotIndex = node.pivotIndex;
            copy.pivotValue = node.pivotValue;
            copy.isPartitioned = node.isPartitioned;
            copy.isActive = node.isActive;
            copy.isPivotSorted = node.isPivotSorted;
            copy.isLeaf = node.isLeaf;
            copy.x = node.x;
            copy.y = node.y;
            copy.width = node.width;
            copy.height = node.height;
            copy.leftChild = copyTree(node.leftChild);
            copy.rightChild = copyTree(node.rightChild);
            return copy;
        }
        
        // Find node in tree by range
        private static TreeNode findNode(TreeNode node, int left, int right) {
            if (node == null) return null;
            if (node.left == left && node.right == right) return node;
            
            TreeNode found = findNode(node.leftChild, left, right);
            if (found != null) return found;
            
            return findNode(node.rightChild, left, right);
        }
    }

    // Solver state
    private int[] array;
    private TreeNode root;
    private TreeNode currentNode;
    private PivotStrategy pivotStrategy;
    private Random random;
    
    // Partition state
    private int left, right;
    private int pivotIndex, pivotValue;
    private int i, j;
    
    // Recursion management (iterative with explicit stack)
    private Stack<int[]> rangeStack;  // Stack of [left, right] ranges
    private int recursionDepth;
    
    // Phase tracking
    private String phase;  // "PARTITION" or "RECURSE" or "DONE"
    private String partitionPhase;  // Detailed partition phase
    private boolean done;
    
    // Step listener
    private StepListener stepListener;

    public QuickSortSolver(int[] initialArray, PivotStrategy strategy) {
        this.pivotStrategy = strategy != null ? strategy : PivotStrategy.LAST;
        this.random = new Random();
        setArray(initialArray);
    }

    public void setArray(int[] initialArray) {
        if (initialArray == null) initialArray = new int[0];
        this.array = Arrays.copyOf(initialArray, initialArray.length);
        reset();
    }

    public void setStepListener(StepListener listener) {
        this.stepListener = listener;
    }

    public void reset() {
        int n = array.length;
        done = n <= 1;
        
        if (n <= 1) {
            phase = "DONE";
            root = null;
            currentNode = null;
            if (n == 1) {
                root = new TreeNode(Arrays.copyOf(array, 1), 0, 0, 0);
                root.isLeaf = true;
                root.isPivotSorted = true;
            }
            emit(StepType.DONE);
            return;
        }
        
        // Initialize
        phase = "INIT";
        partitionPhase = "START";
        recursionDepth = 0;
        
        // Create root node
        root = new TreeNode(Arrays.copyOf(array, n), 0, n - 1, 0);
        currentNode = root;
        
        // Initialize stack with full range
        rangeStack = new Stack<>();
        rangeStack.push(new int[]{0, n - 1});
        
        // Will start partition on first step
        left = -1;
        right = -1;
        
        emit(StepType.INIT);
    }

    /**
     * Execute one granular step of quick sort
     */
    public void step() {
        if (done) return;
        
        // Check if we need to pop a new range
        if ("RECURSE".equals(phase) || "INIT".equals(phase)) {
            if (rangeStack.isEmpty()) {
                done = true;
                phase = "DONE";
                emit(StepType.DONE);
                return;
            }
            
            int[] range = rangeStack.pop();
            left = range[0];
            right = range[1];
            
            if (left >= right) {
                // Single element or invalid range
                if (left == right) {
                    TreeNode leaf = findOrCreateNode(left, right);
                    if (leaf != null) {
                        leaf.isLeaf = true;
                        leaf.isPivotSorted = true;
                    }
                }
                emit(StepType.POP_RANGE);
                return;
            }
            
            // Start new partition
            phase = "PARTITION";
            partitionPhase = "START";
            currentNode = findOrCreateNode(left, right);
            if (currentNode != null) {
                currentNode.isActive = true;
            }
            
            emit(StepType.PARTITION_START);
            return;
        }
        
        // Partition phase - very granular steps
        if ("PARTITION".equals(phase)) {
            switch (partitionPhase) {
                case "START":
                    // Select pivot
                    pivotIndex = selectPivot(left, right);
                    pivotValue = array[pivotIndex];
                    if (currentNode != null) {
                        currentNode.pivotIndex = pivotIndex;
                        currentNode.pivotValue = pivotValue;
                    }
                    partitionPhase = "PIVOT_SELECTED";
                    emit(StepType.SELECT_PIVOT);
                    break;
                    
                case "PIVOT_SELECTED":
                    // Move pivot to end
                    if (pivotIndex != right) {
                        swap(pivotIndex, right);
                        pivotIndex = right;
                        emit(StepType.SWAP);
                    }
                    // Initialize pointers
                    i = left - 1;
                    j = left;
                    partitionPhase = "INIT_POINTERS";
                    emit(StepType.INIT_POINTERS);
                    break;
                    
                case "INIT_POINTERS":
                case "CONTINUE":
                    if (j < right) {
                        partitionPhase = "MOVE_J";
                        emit(StepType.MOVE_J);
                    } else {
                        partitionPhase = "PLACE_PIVOT";
                    }
                    break;
                    
                case "MOVE_J":
                    // Compare arr[j] with pivot
                    partitionPhase = "COMPARE";
                    emit(StepType.COMPARE);
                    break;
                    
                case "COMPARE":
                    if (array[j] <= pivotValue) {
                        // Need to swap
                        i++;
                        partitionPhase = "INCREMENT_I";
                        emit(StepType.INCREMENT_I);
                    } else {
                        // Just move j
                        j++;
                        partitionPhase = "CONTINUE";
                    }
                    break;
                    
                case "INCREMENT_I":
                    // Swap arr[i] and arr[j]
                    if (i != j) {
                        swap(i, j);
                        emit(StepType.SWAP);
                    }
                    j++;
                    partitionPhase = "CONTINUE";
                    break;
                    
                case "PLACE_PIVOT":
                    // Place pivot in final position
                    i++;
                    if (i != right) {
                        swap(i, right);
                        emit(StepType.SWAP);
                    }
                    pivotIndex = i;
                    
                    // Update tree node
                    if (currentNode != null) {
                        currentNode.pivotIndex = pivotIndex;
                        currentNode.isPartitioned = true;
                        currentNode.isPivotSorted = true;
                        currentNode.isActive = false;
                        currentNode.data = Arrays.copyOfRange(array, left, right + 1);
                    }
                    
                    partitionPhase = "COMPLETE";
                    emit(StepType.PLACE_PIVOT);
                    break;
                    
                case "COMPLETE":
                    emit(StepType.PARTITION_COMPLETE);
                    
                    // Create child nodes
                    if (pivotIndex - 1 > left) {
                        TreeNode leftChild = new TreeNode(
                            Arrays.copyOfRange(array, left, pivotIndex),
                            left, pivotIndex - 1, recursionDepth + 1
                        );
                        if (currentNode != null) {
                            currentNode.leftChild = leftChild;
                        }
                    }
                    
                    if (pivotIndex + 1 < right) {
                        TreeNode rightChild = new TreeNode(
                            Arrays.copyOfRange(array, pivotIndex + 1, right + 1),
                            pivotIndex + 1, right, recursionDepth + 1
                        );
                        if (currentNode != null) {
                            currentNode.rightChild = rightChild;
                        }
                    }
                    
                    // Push subproblems to stack (right first, then left for correct order)
                    if (pivotIndex + 1 < right) {
                        rangeStack.push(new int[]{pivotIndex + 1, right});
                        emit(StepType.PUSH_RIGHT);
                    }
                    if (pivotIndex - 1 > left) {
                        rangeStack.push(new int[]{left, pivotIndex - 1});
                        emit(StepType.PUSH_LEFT);
                    }
                    
                    phase = "RECURSE";
                    recursionDepth++;
                    break;
            }
        }
    }

    /**
     * Select pivot based on strategy
     */
    private int selectPivot(int left, int right) {
        switch (pivotStrategy) {
            case FIRST:
                return left;
            case MIDDLE:
                return left + (right - left) / 2;
            case RANDOM:
                return left + random.nextInt(right - left + 1);
            case MEDIAN_OF_THREE:
                return medianOfThree(left, right);
            case LAST:
            default:
                return right;
        }
    }

    /**
     * Find median of first, middle, last elements
     */
    private int medianOfThree(int left, int right) {
        int mid = left + (right - left) / 2;
        int a = array[left];
        int b = array[mid];
        int c = array[right];
        
        if ((a <= b && b <= c) || (c <= b && b <= a)) return mid;
        if ((b <= a && a <= c) || (c <= a && a <= b)) return left;
        return right;
    }

    /**
     * Swap two elements
     */
    private void swap(int i, int j) {
        if (i != j) {
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    /**
     * Find or create tree node for range
     */
    private TreeNode findOrCreateNode(int left, int right) {
        return findNodeByRange(root, left, right);
    }

    /**
     * Find tree node by range
     */
    private TreeNode findNodeByRange(TreeNode node, int left, int right) {
        if (node == null) return null;
        if (node.left == left && node.right == right) return node;
        
        TreeNode found = findNodeByRange(node.leftChild, left, right);
        if (found != null) return found;
        
        return findNodeByRange(node.rightChild, left, right);
    }

    /**
     * Create snapshot of current state
     */
    public State snapshot() {
        State s = new State();
        s.array = Arrays.copyOf(array, array.length);
        s.left = left;
        s.right = right;
        s.pivotIndex = pivotIndex;
        s.pivotValue = pivotValue;
        s.i = i;
        s.j = j;
        s.root = State.copyTree(root);
        s.currentNode = currentNode != null ? State.findNode(s.root, currentNode.left, currentNode.right) : null;
        s.phase = phase;
        s.recursionDepth = recursionDepth;
        s.done = done;
        return s;
    }

    /**
     * Restore from snapshot
     */
    public void restore(State s) {
        if (s == null) return;
        this.array = Arrays.copyOf(s.array, s.array.length);
        this.left = s.left;
        this.right = s.right;
        this.pivotIndex = s.pivotIndex;
        this.pivotValue = s.pivotValue;
        this.i = s.i;
        this.j = s.j;
        this.root = State.copyTree(s.root);
        this.currentNode = s.currentNode != null ? State.findNode(this.root, s.currentNode.left, s.currentNode.right) : null;
        this.phase = s.phase;
        this.recursionDepth = s.recursionDepth;
        this.done = s.done;
    }

    private void emit(StepType type) {
        if (stepListener != null) {
            State currentState = snapshot();
            stepListener.onStep(type, currentState);
        }
    }

    // Getters
    public int[] getArray() { return Arrays.copyOf(array, array.length); }
    public TreeNode getRoot() { return root; }
    public TreeNode getCurrentNode() { return currentNode; }
    public int getLeft() { return left; }
    public int getRight() { return right; }
    public int getPivotIndex() { return pivotIndex; }
    public int getPivotValue() { return pivotValue; }
    public int getI() { return i; }
    public int getJ() { return j; }
    public String getPhase() { return phase; }
    public int getRecursionDepth() { return recursionDepth; }
    public boolean isDone() { return done; }
    public PivotStrategy getPivotStrategy() { return pivotStrategy; }

    public String getCurrentStepDescription() {
        if (done) return "Done. Array sorted (n=" + array.length + ")";
        if ("INIT".equals(phase)) return "Initializing Quick Sort...";
        if ("PARTITION".equals(phase)) {
            return "Partitioning [" + left + ".." + right + "] around pivot=" + pivotValue;
        }
        if ("RECURSE".equals(phase)) return "Processing subproblems...";
        return "Quick Sort in progress...";
    }
}
