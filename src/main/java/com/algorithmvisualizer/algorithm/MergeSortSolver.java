package com.algorithmvisualizer.algorithm;

import java.util.Arrays;

/**
 * Merge Sort solver with tree-based visualization support.
 * Uses bottom-up iterative approach with pre-built tree structure.
 */
public class MergeSortSolver {

    public enum StepType {
        // Tree building phase
        INIT,                       // Start merge sort, show initial array
        BUILD_TREE_START,           // Start building tree structure
        DIVIDE_NODE,                // Divide a node into left and right children
        BUILD_TREE_COMPLETE,        // Tree structure complete
        
        // Merge phase (bottom-up)
        MERGE_LEVEL_START,          // Start merging at a specific level (size 1, 2, 4...)
        MERGE_START,                // Start merging two sorted subarrays
        MERGE_COMPARE,              // Compare elements from left and right
        MERGE_TAKE_LEFT,            // Take element from left subarray
        MERGE_TAKE_RIGHT,           // Take element from right subarray
        MERGE_ADD_ELEMENT,          // Add element to building result (granular)
        MERGE_COPY_REMAINING_LEFT,  // Copy remaining left elements
        MERGE_COPY_REMAINING_RIGHT, // Copy remaining right elements
        MERGE_COMPLETE,             // Finished merging this range
        MERGE_LEVEL_COMPLETE,       // Finished all merges at this level
        
        // Completion
        DONE                        // Entire array sorted
    }

    public interface StepListener {
        void onStep(StepType type, State state);
    }

    /**
     * Tree node representing a subarray in the merge sort tree
     */
    public static class TreeNode {
        public int[] data;              // Subarray contents (final merged result)
        public int[] buildingData;      // Partial merge result (being built)
        public int left;                // Left index in original array
        public int right;               // Right index in original array
        public int level;               // Depth in tree (0 = root)
        public TreeNode leftChild;      // Left child node
        public TreeNode rightChild;     // Right child node
        public boolean isMerged;        // Has this node been merged?
        public boolean isActive;        // Is this node currently being processed?
        public boolean isDivided;       // Has this node been divided into children?
        public boolean isBuilding;      // Is this node currently being built (merge in progress)?
        
        // Visual properties (set by renderer)
        public double x, y;             // Position on canvas
        public double width, height;    // Node dimensions
        
        public TreeNode(int[] data, int left, int right, int level) {
            this.data = data;
            this.buildingData = null;
            this.left = left;
            this.right = right;
            this.level = level;
            this.isMerged = false;
            this.isActive = false;
            this.isDivided = false;
            this.isBuilding = false;
        }
    }

    /**
     * Complete state snapshot for step-back support
     */
    public static class State {
        public int[] array;              // Current array state
        public int[] tempArray;          // Temporary merge array
        
        // Current operation context
        public int left;                 // Left boundary of current range
        public int right;                // Right boundary of current range
        public int mid;                  // Midpoint of current range
        
        // Merge operation state
        public int leftPointer;          // Pointer in left subarray
        public int rightPointer;         // Pointer in right subarray
        public int mergePointer;         // Pointer in merged result
        
        // Tree tracking
        public TreeNode root;            // Root of merge sort tree
        public TreeNode currentNode;     // Currently processing node
        public int currentSize;          // Current merge size (1, 2, 4, 8...)
        public int currentLevel;         // Current level in tree (0 = root)
        public int mergeIndex;           // Index within current level
        
        // Phase tracking
        public String phase;             // "BUILD_TREE" or "MERGE"
        
        // Completion
        public boolean done;
        
        // Deep copy constructor
        public State(State other) {
            this.array = other.array != null ? Arrays.copyOf(other.array, other.array.length) : null;
            this.tempArray = other.tempArray != null ? Arrays.copyOf(other.tempArray, other.tempArray.length) : null;
            this.left = other.left;
            this.right = other.right;
            this.mid = other.mid;
            this.leftPointer = other.leftPointer;
            this.rightPointer = other.rightPointer;
            this.mergePointer = other.mergePointer;
            this.root = copyTree(other.root);
            this.currentNode = other.currentNode != null ? findNode(this.root, other.currentNode.left, other.currentNode.right) : null;
            this.currentSize = other.currentSize;
            this.currentLevel = other.currentLevel;
            this.mergeIndex = other.mergeIndex;
            this.phase = other.phase;
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
            copy.buildingData = node.buildingData != null ? Arrays.copyOf(node.buildingData, node.buildingData.length) : null;
            copy.isMerged = node.isMerged;
            copy.isActive = node.isActive;
            copy.isDivided = node.isDivided;
            copy.isBuilding = node.isBuilding;
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
    private int[] tempArray;
    private TreeNode root;
    private TreeNode currentNode;
    
    // Merge operation state
    private int left, right, mid;
    private int leftPointer, rightPointer, mergePointer;
    
    // Iteration state
    private int currentSize;        // Current merge size (1, 2, 4, 8...)
    private int currentLevel;       // Current tree level
    private int mergeIndex;         // Index within current level
    private int totalLevels;        // Total tree depth
    
    // Tree building state
    private java.util.Queue<TreeNode> divideQueue;  // Queue of nodes to divide
    private int currentDivideLevel;  // Current level being divided
    
    // Phase tracking
    private String phase;           // "BUILD_TREE" or "MERGE"
    private boolean done;
    
    // Step listener
    private StepListener stepListener;

    public MergeSortSolver(int[] initialArray) {
        setArray(initialArray);
    }

    public void setArray(int[] initialArray) {
        if (initialArray == null) initialArray = new int[0];
        this.array = Arrays.copyOf(initialArray, initialArray.length);
        this.tempArray = new int[initialArray.length];
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
            emit(StepType.DONE);
            return;
        }
        
        // Initialize tree building phase
        phase = "BUILD_TREE";
        emit(StepType.INIT);
        emit(StepType.BUILD_TREE_START);
        
        // Create root node only (not divided yet)
        root = new TreeNode(Arrays.copyOf(array, n), 0, n - 1, 0);
        root.isDivided = false;
        
        // Initialize divide queue with root
        divideQueue = new java.util.LinkedList<>();
        divideQueue.offer(root);
        currentDivideLevel = 0;
        
        totalLevels = calculateMaxDepth(n);
        
        // Don't transition to merge phase yet - tree building happens in step()
        currentNode = null;
    }

    /**
     * Calculate maximum tree depth for array of size n
     */
    private int calculateMaxDepth(int n) {
        if (n <= 1) return 0;
        return (int) Math.ceil(Math.log(n) / Math.log(2));
    }

    /**
     * Build merge sort tree structure (pre-processing) - OLD METHOD, kept for reference
     */
    private TreeNode buildTree(int[] arr, int left, int right, int level) {
        TreeNode node = new TreeNode(
            Arrays.copyOfRange(arr, left, right + 1),
            left, right, level
        );
        
        if (left < right) {
            int mid = left + (right - left) / 2;
            node.leftChild = buildTree(arr, left, mid, level + 1);
            node.rightChild = buildTree(arr, mid + 1, right, level + 1);
            node.isDivided = true;
        }
        
        return node;
    }

    /**
     * Calculate tree depth
     */
    private int calculateDepth(TreeNode node) {
        if (node == null) return 0;
        if (node.leftChild == null && node.rightChild == null) return node.level;
        return Math.max(calculateDepth(node.leftChild), calculateDepth(node.rightChild));
    }

    /**
     * Execute one step of the merge sort algorithm
     */
    public void step() {
        if (done) return;
        
        // Phase 1: Build tree (divide phase) - process entire level at once
        if ("BUILD_TREE".equals(phase)) {
            if (divideQueue.isEmpty()) {
                // Tree building complete
                emit(StepType.BUILD_TREE_COMPLETE);
                
                // Initialize merge phase
                phase = "MERGE";
                currentSize = 1;
                currentLevel = totalLevels;
                mergeIndex = 0;
                left = 0;
                right = -1;
                currentNode = null;
                return;
            }
            
            // Divide all nodes at current level in one step
            java.util.List<TreeNode> currentLevelNodes = new java.util.ArrayList<>();
            int queueSize = divideQueue.size();
            
            for (int i = 0; i < queueSize; i++) {
                TreeNode node = divideQueue.poll();
                currentLevelNodes.add(node);
                
                if (node.left < node.right) {
                    int mid = node.left + (node.right - node.left) / 2;
                    
                    // Create left child
                    node.leftChild = new TreeNode(
                        Arrays.copyOfRange(array, node.left, mid + 1),
                        node.left, mid, node.level + 1
                    );
                    node.leftChild.isDivided = false;
                    
                    // Create right child
                    node.rightChild = new TreeNode(
                        Arrays.copyOfRange(array, mid + 1, node.right + 1),
                        mid + 1, node.right, node.level + 1
                    );
                    node.rightChild.isDivided = false;
                    
                    node.isDivided = true;
                    
                    // Add children to queue for next level
                    divideQueue.offer(node.leftChild);
                    divideQueue.offer(node.rightChild);
                } else {
                    // Leaf node - mark as divided (no children to create)
                    node.isDivided = true;
                }
            }
            
            currentDivideLevel++;
            emit(StepType.DIVIDE_NODE);
            return;
        }
        
        // Phase 2: Merge phase (bottom-up)
        int n = array.length;
        
        // Check if we need to start a new merge level
        if (mergeIndex == 0 && left == 0) {
            if (currentSize >= n) {
                done = true;
                emit(StepType.DONE);
                return;
            }
            emit(StepType.MERGE_LEVEL_START);
        }
        
        // Check if we need to start a new merge operation
        if (right < left) {
            // Find next merge range
            left = mergeIndex * 2 * currentSize;
            if (left >= n) {
                // Finished this level
                emit(StepType.MERGE_LEVEL_COMPLETE);
                currentSize *= 2;
                currentLevel--;
                mergeIndex = 0;
                left = 0;
                right = -1;
                return;
            }
            
            mid = Math.min(left + currentSize - 1, n - 1);
            right = Math.min(left + 2 * currentSize - 1, n - 1);
            
            // Find corresponding tree node
            currentNode = findNodeByRange(root, left, right);
            if (currentNode != null) {
                currentNode.isActive = true;
                currentNode.isBuilding = true;
                currentNode.buildingData = new int[0]; // Start with empty building array
            }
            
            // Initialize merge pointers
            leftPointer = left;
            rightPointer = mid + 1;
            mergePointer = left;
            
            emit(StepType.MERGE_START);
            return;
        }
        
        // Perform merge operation
        if (leftPointer <= mid && rightPointer <= right) {
            // Both subarrays have elements
            emit(StepType.MERGE_COMPARE);
            
            if (array[leftPointer] <= array[rightPointer]) {
                tempArray[mergePointer] = array[leftPointer];
                leftPointer++;
                mergePointer++;
                emit(StepType.MERGE_TAKE_LEFT);
                // Update building data incrementally
                if (currentNode != null) {
                    int[] newBuilding = new int[mergePointer - left];
                    for (int i = 0; i < newBuilding.length; i++) {
                        newBuilding[i] = tempArray[left + i];
                    }
                    currentNode.buildingData = newBuilding;
                }
                emit(StepType.MERGE_ADD_ELEMENT);
            } else {
                tempArray[mergePointer] = array[rightPointer];
                rightPointer++;
                mergePointer++;
                emit(StepType.MERGE_TAKE_RIGHT);
                // Update building data incrementally
                if (currentNode != null) {
                    int[] newBuilding = new int[mergePointer - left];
                    for (int i = 0; i < newBuilding.length; i++) {
                        newBuilding[i] = tempArray[left + i];
                    }
                    currentNode.buildingData = newBuilding;
                }
                emit(StepType.MERGE_ADD_ELEMENT);
            }
        } else if (leftPointer <= mid) {
            // Only left subarray has elements
            tempArray[mergePointer] = array[leftPointer];
            leftPointer++;
            mergePointer++;
            emit(StepType.MERGE_COPY_REMAINING_LEFT);
            // Update building data incrementally
            if (currentNode != null) {
                int[] newBuilding = new int[mergePointer - left];
                for (int i = 0; i < newBuilding.length; i++) {
                    newBuilding[i] = tempArray[left + i];
                }
                currentNode.buildingData = newBuilding;
            }
            emit(StepType.MERGE_ADD_ELEMENT);
        } else if (rightPointer <= right) {
            // Only right subarray has elements
            tempArray[mergePointer] = array[rightPointer];
            rightPointer++;
            mergePointer++;
            emit(StepType.MERGE_COPY_REMAINING_RIGHT);
            // Update building data incrementally
            if (currentNode != null) {
                int[] newBuilding = new int[mergePointer - left];
                for (int i = 0; i < newBuilding.length; i++) {
                    newBuilding[i] = tempArray[left + i];
                }
                currentNode.buildingData = newBuilding;
            }
            emit(StepType.MERGE_ADD_ELEMENT);
        } else {
            // Merge complete - copy back to array
            for (int i = left; i <= right; i++) {
                array[i] = tempArray[i];
            }
            
            // Update tree node
            if (currentNode != null) {
                currentNode.data = Arrays.copyOfRange(array, left, right + 1);
                currentNode.buildingData = null;
                currentNode.isMerged = true;
                currentNode.isActive = false;
                currentNode.isBuilding = false;
            }
            
            emit(StepType.MERGE_COMPLETE);
            
            // Move to next merge in this level
            mergeIndex++;
            right = -1;
        }
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
        s.tempArray = Arrays.copyOf(tempArray, tempArray.length);
        s.left = left;
        s.right = right;
        s.mid = mid;
        s.leftPointer = leftPointer;
        s.rightPointer = rightPointer;
        s.mergePointer = mergePointer;
        s.root = State.copyTree(root);
        s.currentNode = currentNode != null ? State.findNode(s.root, currentNode.left, currentNode.right) : null;
        s.currentSize = currentSize;
        s.currentLevel = currentLevel;
        s.mergeIndex = mergeIndex;
        s.phase = phase;
        s.done = done;
        return s;
    }

    /**
     * Restore from snapshot
     */
    public void restore(State s) {
        if (s == null) return;
        this.array = Arrays.copyOf(s.array, s.array.length);
        this.tempArray = Arrays.copyOf(s.tempArray, s.tempArray.length);
        this.left = s.left;
        this.right = s.right;
        this.mid = s.mid;
        this.leftPointer = s.leftPointer;
        this.rightPointer = s.rightPointer;
        this.mergePointer = s.mergePointer;
        this.root = State.copyTree(s.root);
        this.currentNode = s.currentNode != null ? State.findNode(this.root, s.currentNode.left, s.currentNode.right) : null;
        this.currentSize = s.currentSize;
        this.currentLevel = s.currentLevel;
        this.mergeIndex = s.mergeIndex;
        this.phase = s.phase;
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
    public int getCurrentSize() { return currentSize; }
    public int getCurrentLevel() { return currentLevel; }
    public int getLeft() { return left; }
    public int getRight() { return right; }
    public int getMid() { return mid; }
    public int getLeftPointer() { return leftPointer; }
    public int getRightPointer() { return rightPointer; }
    public int getMergePointer() { return mergePointer; }
    public String getPhase() { return phase; }
    public boolean isDone() { return done; }
    public int getTotalLevels() { return totalLevels; }

    public String getCurrentStepDescription() {
        if (done) return "Done. Array sorted (n=" + array.length + ")";
        if ("BUILD_TREE".equals(phase)) return "Building merge sort tree structure...";
        if (right < left) return "Preparing next merge operation...";
        if (leftPointer <= mid && rightPointer <= right) {
            return "Merging [" + left + ".." + right + "]: comparing indices " + leftPointer + " and " + rightPointer;
        }
        if (leftPointer <= mid) {
            return "Merging [" + left + ".." + right + "]: copying remaining left elements";
        }
        if (rightPointer <= right) {
            return "Merging [" + left + ".." + right + "]: copying remaining right elements";
        }
        return "Merge sort in progress...";
    }
}
