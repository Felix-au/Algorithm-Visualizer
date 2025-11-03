package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Stack solver for interactive data structure operations.
 * Emits step events for UI rendering.
 */
public class StackSolver {

    public enum StepType {
        INIT,
        RESET,
        PUSH_START, PUSH_SET, PUSH_DONE, OVERFLOW,
        POP_START, POP_REMOVE, POP_DONE, UNDERFLOW,
        PEEK,
        SEARCH_START, SEARCH_COMPARE, SEARCH_FOUND, SEARCH_NOT_FOUND,
        SP_AUX_PUSH, SP_AUX_POP, SP_MOVE_BACK_START, SP_MOVE_BACK_STEP, SP_DONE,
        // Queue support (for reverse)
        Q_ENQ, Q_DEQ,
        REVERSE_START, REVERSE_PHASE2_START, REVERSE_DONE,
        CLEAR
    }

    public interface StepListener {
        void onStep(StepType type, int index, Integer value);
    }

    public static class State {
        public int capacity;
        public int top; // -1 when empty; next insert index is top+1
        public int[] data;
        public int[] aux; // used by search&pop only (opaque to UI if not shown)
        public int[] queue; public int qSize;
        public boolean done;
        public String lastOp;
        public Integer lastValue;
        // op state
        public int phase;
        public int searchIndex;
        public int auxTopWorking;
        public String currentOp;
        public Integer currentValue;
        public List<String> queueTypes;
        public List<Integer> queueValues;
    }

    private int capacity;
    private int top;
    private int[] data;
    private int[] aux;
    private int[] queue; private int qSize = 0;
    private boolean done = false;
    private String lastOp = "";
    private Integer lastValue = null;

    private StepListener listener;

    // Step-wise operation state
    private enum OpType { NONE, PUSH, POP, PEEK, SEARCH, SEARCH_AND_POP, REVERSE, CLEAR }
    private static class PendingOp {
        final OpType type; final Integer value;
        PendingOp(OpType t, Integer v) { this.type = t; this.value = v; }
    }
    private Deque<PendingOp> opQueue = new ArrayDeque<>();
    private OpType currentOp = OpType.NONE;
    private Integer currentValue = null;
    private int phase = 0;           // per-op phase
    private int searchIndex = -1;    // for search
    private int auxTopWorking = -1;  // for search&pop aux working top

    public StackSolver(int capacity, int[] initial) {
        setCapacity(capacity);
        initData(initial);
    }

    public void setStepListener(StepListener l) { this.listener = l; }

    public void setCapacity(int cap) {
        this.capacity = Math.max(1, cap);
        this.data = new int[this.capacity];
        this.aux = new int[this.capacity];
        this.queue = new int[this.capacity]; this.qSize = 0;
        this.top = -1;
        this.done = false;
        emit(StepType.INIT, -1, null);
    }

    public void initData(int[] items) {
        top = -1;
        if (items != null) {
            int n = Math.min(items.length, capacity);
            for (int i = 0; i < n; i++) {
                data[i] = items[i];
                top = i;
            }
        }
        emit(StepType.RESET, top, null);
    }

    public boolean isEmpty() { return top < 0; }
    public boolean isFull() { return top + 1 >= capacity; }
    public int size() { return top + 1; }
    public int capacity() { return capacity; }
    public int topIndex() { return top; }
    public int[] getData() {
        return Arrays.copyOf(data, capacity);
    }
    public int[] getAuxArray() { return Arrays.copyOf(aux, capacity); }
    public int getAuxTopWorking() { return auxTopWorking; }
    public int[] getQueueArray() { return Arrays.copyOf(queue, capacity); }
    public int getQueueSize() { return qSize; }

    public boolean hasPending() {
        return currentOp != OpType.NONE || !opQueue.isEmpty();
    }

    // Queue operations (do not execute immediately)
    public void queuePush(int value) { opQueue.add(new PendingOp(OpType.PUSH, value)); }
    public void queuePop() { opQueue.add(new PendingOp(OpType.POP, null)); }
    public void queuePeek() { opQueue.add(new PendingOp(OpType.PEEK, null)); }
    public void queueSearch(int value) { opQueue.add(new PendingOp(OpType.SEARCH, value)); }
    public void queueSearchAndPop(int value) { opQueue.add(new PendingOp(OpType.SEARCH_AND_POP, value)); }
    public void queueReverse() { opQueue.add(new PendingOp(OpType.REVERSE, null)); }
    public void queueClear() { opQueue.add(new PendingOp(OpType.CLEAR, null)); }

    // Execute a single visualization step; returns true if a step was emitted
    public boolean step() {
        // Initialize current operation if none
        if (currentOp == OpType.NONE) {
            if (opQueue.isEmpty()) return false;
            PendingOp op = opQueue.removeFirst();
            currentOp = op.type;
            currentValue = op.value;
            phase = 0;
            searchIndex = -1;
            auxTopWorking = -1;
        }

        switch (currentOp) {
            case PUSH: {
                int value = currentValue != null ? currentValue : 0;
                if (phase == 0) {
                    if (isFull()) { emit(StepType.OVERFLOW, -1, value); finishOp(); return true; }
                    emit(StepType.PUSH_START, top + 1, value); phase = 1; return true;
                } else if (phase == 1) {
                    data[++top] = value; emit(StepType.PUSH_SET, top, value); phase = 2; return true;
                } else {
                    emit(StepType.PUSH_DONE, top, value); finishOp(); return true;
                }
            }
            case POP: {
                if (phase == 0) {
                    if (isEmpty()) { emit(StepType.UNDERFLOW, -1, null); finishOp(); return true; }
                    emit(StepType.POP_START, top, data[top]); phase = 1; return true;
                } else if (phase == 1) {
                    int val = data[top]; emit(StepType.POP_REMOVE, top, val); top--; phase = 2; return true;
                } else {
                    emit(StepType.POP_DONE, top, null); finishOp(); return true;
                }
            }
            case PEEK: {
                if (isEmpty()) { emit(StepType.UNDERFLOW, -1, null); finishOp(); return true; }
                emit(StepType.PEEK, top, data[top]); finishOp(); return true;
            }
            case SEARCH: {
                if (phase == 0) { emit(StepType.SEARCH_START, -1, currentValue); searchIndex = top; phase = 1; return true; }
                if (searchIndex < 0) { emit(StepType.SEARCH_NOT_FOUND, -1, currentValue); finishOp(); return true; }
                int i = searchIndex; emit(StepType.SEARCH_COMPARE, i, data[i]);
                if (data[i] == currentValue) { emit(StepType.SEARCH_FOUND, i, data[i]); finishOp(); }
                searchIndex--; return true;
            }
            case SEARCH_AND_POP: {
                // phases: 0=start; 1=compare; 2=pop_start; 3=pop_remove_to_aux; 4=found; 5=pop_target_start; 6=pop_target_remove; 7=move_back_start; 8=move_back_step loop; 9=done
                if (phase == 0) { emit(StepType.SEARCH_START, -1, currentValue); phase = 1; return true; }
                if (phase == 1) {
                    if (isEmpty()) { emit(StepType.SEARCH_NOT_FOUND, -1, currentValue); phase = 7; return true; }
                    if (data[top] == currentValue) { emit(StepType.SEARCH_FOUND, top, data[top]); phase = 5; return true; }
                    emit(StepType.SEARCH_COMPARE, top, data[top]); phase = 2; return true; }
                if (phase == 2) { emit(StepType.POP_START, top, data[top]); phase = 3; return true; }
                if (phase == 3) { int val = data[top]; emit(StepType.POP_REMOVE, top, val); top--; aux[++auxTopWorking] = val; emit(StepType.SP_AUX_PUSH, auxTopWorking, val); phase = 1; return true; }
                if (phase == 5) { emit(StepType.POP_START, top, data[top]); phase = 6; return true; }
                if (phase == 6) { int val = data[top]; emit(StepType.POP_REMOVE, top, val); top--; phase = 7; return true; }
                if (phase == 7) { emit(StepType.SP_MOVE_BACK_START, -1, null); phase = 8; return true; }
                if (phase == 8) {
                    if (auxTopWorking >= 0) {
                        int val = aux[auxTopWorking--];
                        emit(StepType.SP_AUX_POP, auxTopWorking + 1, val);
                        data[++top] = val; emit(StepType.SP_MOVE_BACK_STEP, top, val); return true;
                    } else { emit(StepType.SP_DONE, top, null); finishOp(); return true; }
                }
                return false;
            }
            case REVERSE: {
                // Phase 0: start; 1: pop->enqueue loop; 2: phase2 start; 3: dequeue->push loop; 4: done
                if (phase == 0) { emit(StepType.REVERSE_START, -1, null); phase = 1; return true; }
                if (phase == 1) {
                    if (top >= 0) {
                        emit(StepType.POP_START, top, data[top]);
                        int val = data[top];
                        emit(StepType.POP_REMOVE, top, val); top--;
                        queue[qSize] = val; emit(StepType.Q_ENQ, qSize, val); qSize++;
                        return true;
                    } else { phase = 2; return true; }
                }
                if (phase == 2) { emit(StepType.REVERSE_PHASE2_START, -1, null); phase = 3; return true; }
                if (phase == 3) {
                    if (qSize > 0) {
                        int val = queue[0];
                        // shift left
                        for (int i = 1; i < qSize; i++) queue[i-1] = queue[i];
                        qSize--;
                        emit(StepType.Q_DEQ, 0, val);
                        emit(StepType.PUSH_START, top + 1, val);
                        data[++top] = val; emit(StepType.PUSH_SET, top, val);
                        emit(StepType.PUSH_DONE, top, val);
                        return true;
                    } else { phase = 4; return true; }
                }
                if (phase == 4) { emit(StepType.REVERSE_DONE, -1, null); finishOp(); return true; }
                return false;
            }
            case CLEAR: {
                emit(StepType.CLEAR, -1, null); top = -1; finishOp(); return true;
            }
            default: return false;
        }
    }

    private void finishOp() {
        currentOp = OpType.NONE; currentValue = null; phase = 0; searchIndex = -1; auxTopWorking = -1;
    }

    // Operations
    public void push(int value) {
        lastOp = "PUSH"; lastValue = value;
        if (isFull()) { emit(StepType.OVERFLOW, -1, value); return; }
        emit(StepType.PUSH_START, top + 1, value);
        data[++top] = value;
        emit(StepType.PUSH_SET, top, value);
        emit(StepType.PUSH_DONE, top, value);
    }

    public Integer pop() {
        lastOp = "POP"; lastValue = null;
        if (isEmpty()) { emit(StepType.UNDERFLOW, -1, null); return null; }
        emit(StepType.POP_START, top, data[top]);
        int val = data[top];
        emit(StepType.POP_REMOVE, top, val);
        top--;
        emit(StepType.POP_DONE, top, val);
        return val;
    }

    public Integer peek() {
        lastOp = "PEEK"; lastValue = null;
        if (isEmpty()) { emit(StepType.UNDERFLOW, -1, null); return null; }
        emit(StepType.PEEK, top, data[top]);
        return data[top];
    }

    public int search(int target) {
        lastOp = "SEARCH"; lastValue = target;
        emit(StepType.SEARCH_START, -1, target);
        for (int i = top; i >= 0; i--) {
            emit(StepType.SEARCH_COMPARE, i, data[i]);
            if (data[i] == target) {
                emit(StepType.SEARCH_FOUND, i, data[i]);
                return top - i + 1; // 1-based position from top, like java.util.Stack
            }
        }
        emit(StepType.SEARCH_NOT_FOUND, -1, target);
        return -1;
    }

    /**
     * Search and Pop: find target, pop above items to aux, pop target, push back aux.
     */
    public boolean searchAndPop(int target) {
        lastOp = "SEARCH_AND_POP"; lastValue = target;
        emit(StepType.SEARCH_START, -1, target);
        int auxTop = -1;
        // Move above items to aux
        while (top >= 0 && data[top] != target) {
            emit(StepType.SEARCH_COMPARE, top, data[top]);
            // pop to aux
            if (isEmpty()) { emit(StepType.UNDERFLOW, -1, null); return false; }
            emit(StepType.POP_START, top, data[top]);
            int val = data[top];
            emit(StepType.POP_REMOVE, top, val);
            top--;
            // push to aux
            emit(StepType.SP_AUX_PUSH, ++auxTop, val);
            aux[auxTop] = val;
        }
        if (top < 0) {
            emit(StepType.SEARCH_NOT_FOUND, -1, target);
            // move back from aux without popping target (none)
            emit(StepType.SP_MOVE_BACK_START, -1, null);
            while (auxTop >= 0) {
                int val = aux[auxTop--];
                emit(StepType.SP_MOVE_BACK_STEP, top + 1, val);
                data[++top] = val;
            }
            emit(StepType.SP_DONE, top, null);
            return false;
        }
        // at this point data[top] == target
        emit(StepType.SEARCH_FOUND, top, data[top]);
        // pop target
        emit(StepType.POP_START, top, data[top]);
        int popped = data[top];
        emit(StepType.POP_REMOVE, top, popped);
        top--;
        // move back from aux
        emit(StepType.SP_MOVE_BACK_START, -1, null);
        while (auxTop >= 0) {
            int val = aux[auxTop--];
            emit(StepType.SP_MOVE_BACK_STEP, top + 1, val);
            data[++top] = val;
        }
        emit(StepType.SP_DONE, top, null);
        return true;
    }

    public void clear() {
        lastOp = "CLEAR"; lastValue = null;
        emit(StepType.CLEAR, -1, null);
        top = -1;
    }

    public State snapshot() {
        State s = new State();
        s.capacity = capacity;
        s.top = top;
        s.data = Arrays.copyOf(data, data.length);
        s.aux = Arrays.copyOf(aux, aux.length);
        s.queue = Arrays.copyOf(queue, queue.length);
        s.qSize = qSize;
        s.done = done;
        s.lastOp = lastOp;
        s.lastValue = lastValue;
        s.phase = phase;
        s.searchIndex = searchIndex;
        s.auxTopWorking = auxTopWorking;
        s.currentOp = currentOp.name();
        s.currentValue = currentValue;
        s.queueTypes = new ArrayList<>();
        s.queueValues = new ArrayList<>();
        for (PendingOp op : opQueue) { s.queueTypes.add(op.type.name()); s.queueValues.add(op.value); }
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        capacity = s.capacity;
        data = Arrays.copyOf(s.data, s.data.length);
        aux = Arrays.copyOf(s.aux, s.aux.length);
        queue = Arrays.copyOf(s.queue, s.queue.length);
        qSize = s.qSize;
        top = s.top;
        done = s.done;
        lastOp = s.lastOp;
        lastValue = s.lastValue;
        phase = s.phase;
        searchIndex = s.searchIndex;
        auxTopWorking = s.auxTopWorking;
        currentOp = s.currentOp != null ? OpType.valueOf(s.currentOp) : OpType.NONE;
        currentValue = s.currentValue;
        opQueue.clear();
        if (s.queueTypes != null) {
            for (int i = 0; i < s.queueTypes.size(); i++) {
                OpType t = OpType.valueOf(s.queueTypes.get(i));
                Integer v = (s.queueValues != null && i < s.queueValues.size()) ? s.queueValues.get(i) : null;
                opQueue.add(new PendingOp(t, v));
            }
        }
    }

    private void emit(StepType type, int index, Integer value) {
        if (listener != null) listener.onStep(type, index, value);
    }
}
