package com.algorithmvisualizer.algorithm;

import java.util.*;

public class QueueSolver {
    public enum StepType {
        INIT, RESET,
        ENQ_START, ENQ_DONE, OVERFLOW,
        DEQ_START, DEQ_REMOVE, DEQ_DONE, UNDERFLOW,
        SEARCH_START, SEARCH_COMPARE, SEARCH_FOUND, SEARCH_NOT_FOUND,
        SDQ_AUX_ENQ, SDQ_AUX_DEQ, SDQ_MOVE_BACK_START, SDQ_MOVE_BACK_STEP, SDQ_DONE,
        REV_START, REV_PHASE2_START, REV_DONE, AUXS_PUSH, AUXS_POP,
        CLEAR
    }

    public interface StepListener {
        void onStep(StepType type, int index, Integer value);
    }

    public static class State {
        public int capacity;
        public int size;
        public int[] data;
        public int[] auxQueue; public int auxQSize;
        public int[] auxStack; public int auxTop;
        public String currentOp;
        public Integer currentValue;
        public int phase;
        public int searchIndex;
        public boolean removed;
        public List<String> queueTypes;
        public List<Integer> queueValues;
    }

    private int capacity;
    private int size;
    private int[] data;

    private int[] auxQueue; private int auxQSize;
    private int[] auxStack; private int auxTop;

    private StepListener listener;

    private enum OpType { NONE, ENQUEUE, DEQUEUE, SEARCH, SEARCH_AND_DEQUEUE, REVERSE, CLEAR }
    private static class PendingOp {
        final OpType type; final Integer value;
        PendingOp(OpType t, Integer v) { this.type = t; this.value = v; }
    }

    private Deque<PendingOp> opQueue = new ArrayDeque<>();
    private OpType currentOp = OpType.NONE;
    private Integer currentValue = null;
    private int phase = 0;
    private int searchIndex = 0;
    private boolean removed = false;

    public QueueSolver(int capacity, StepListener listener) {
        setCapacity(capacity);
        this.listener = listener;
    }

    public void setStepListener(StepListener l) { this.listener = l; }

    private void emit(StepType t, int idx, Integer value) { if (listener != null) listener.onStep(t, idx, value); }

    public void setCapacity(int cap) {
        this.capacity = Math.max(1, cap);
        this.data = new int[this.capacity];
        this.size = 0;
        this.auxQueue = new int[this.capacity]; this.auxQSize = 0;
        this.auxStack = new int[this.capacity]; this.auxTop = -1;
        this.phase = 0; this.searchIndex = 0; removed = false;
        emit(StepType.INIT, -1, null);
    }

    public int capacity() { return capacity; }
    public int size() { return size; }
    public int[] getData() { return Arrays.copyOf(data, capacity); }
    public int[] getAuxQueue() { return Arrays.copyOf(auxQueue, capacity); }
    public int getAuxQSize() { return auxQSize; }
    public int[] getAuxStack() { return Arrays.copyOf(auxStack, capacity); }
    public int getAuxTop() { return auxTop; }

    public boolean hasPending() { return currentOp != OpType.NONE || !opQueue.isEmpty(); }

    public void queueEnqueue(int value) { opQueue.add(new PendingOp(OpType.ENQUEUE, value)); }
    public void queueDequeue() { opQueue.add(new PendingOp(OpType.DEQUEUE, null)); }
    public void queueSearch(int value) { opQueue.add(new PendingOp(OpType.SEARCH, value)); }
    public void queueSearchAndDequeue(int value) { opQueue.add(new PendingOp(OpType.SEARCH_AND_DEQUEUE, value)); }
    public void queueReverse() { opQueue.add(new PendingOp(OpType.REVERSE, null)); }
    public void queueClear() { opQueue.add(new PendingOp(OpType.CLEAR, null)); }

    public boolean step() {
        if (currentOp == OpType.NONE) {
            if (opQueue.isEmpty()) return false;
            PendingOp op = opQueue.removeFirst();
            currentOp = op.type;
            currentValue = op.value;
            phase = 0; searchIndex = 0; removed = false;
        }

        switch (currentOp) {
            case ENQUEUE: {
                if (phase == 0) {
                    if (size >= capacity) { emit(StepType.OVERFLOW, -1, null); finish(); return true; }
                    emit(StepType.ENQ_START, size, currentValue);
                    data[size] = currentValue; size++;
                    emit(StepType.ENQ_DONE, size - 1, currentValue);
                    finish();
                    return true;
                }
                return false;
            }
            case DEQUEUE: {
                if (phase == 0) {
                    if (size == 0) { emit(StepType.UNDERFLOW, -1, null); finish(); return true; }
                    emit(StepType.DEQ_START, 0, data[0]);
                    int val = data[0];
                    emit(StepType.DEQ_REMOVE, 0, val);
                    // shift left
                    for (int i = 1; i < size; i++) data[i - 1] = data[i];
                    size--;
                    emit(StepType.DEQ_DONE, -1, val);
                    finish();
                    return true;
                }
                return false;
            }
            case SEARCH: {
                if (phase == 0) { emit(StepType.SEARCH_START, -1, currentValue); phase = 1; return true; }
                if (phase == 1) {
                    if (searchIndex < size) {
                        emit(StepType.SEARCH_COMPARE, searchIndex, data[searchIndex]);
                        if (data[searchIndex] == currentValue) { emit(StepType.SEARCH_FOUND, searchIndex, data[searchIndex]); finish(); return true; }
                        searchIndex++; return true;
                    } else { emit(StepType.SEARCH_NOT_FOUND, -1, null); finish(); return true; }
                }
                return false;
            }
            case SEARCH_AND_DEQUEUE: {
                // Phase 0: start; 1: drain main -> auxQueue until found; 2: move back auxQueue -> main; 3: done
                if (phase == 0) { emit(StepType.SEARCH_START, -1, currentValue); phase = 1; return true; }
                if (phase == 1) {
                    if (size > 0) {
                        // highlight the current front being compared
                        emit(StepType.SEARCH_COMPARE, 0, data[0]);
                        if (!removed && data[0] == currentValue) {
                            emit(StepType.SEARCH_FOUND, 0, data[0]);
                        }
                        emit(StepType.DEQ_START, 0, data[0]);
                        int val = data[0];
                        emit(StepType.DEQ_REMOVE, 0, val);
                        for (int i = 1; i < size; i++) data[i - 1] = data[i];
                        size--;
                        if (!removed && val == currentValue) {
                            removed = true; // drop it
                            return true;
                        } else {
                            auxQueue[auxQSize] = val; emit(StepType.SDQ_AUX_ENQ, auxQSize, val); auxQSize++; return true;
                        }
                    } else { phase = 2; emit(StepType.SDQ_MOVE_BACK_START, -1, null); return true; }
                }
                if (phase == 2) {
                    if (auxQSize > 0) {
                        int val = auxQueue[0];
                        // aux dequeue
                        for (int i = 1; i < auxQSize; i++) auxQueue[i - 1] = auxQueue[i];
                        auxQSize--;
                        emit(StepType.SDQ_AUX_DEQ, 0, val);
                        // main enqueue
                        emit(StepType.SDQ_MOVE_BACK_STEP, size, val);
                        data[size] = val; size++;
                        return true;
                    } else { emit(StepType.SDQ_DONE, -1, null); finish(); return true; }
                }
                return false;
            }
            case REVERSE: {
                // Phase 0: drain main -> aux stack; 1: phase2 start; 2: pop aux stack -> main; 3: done
                if (phase == 0) {
                    emit(StepType.REV_START, -1, null); phase = 1; return true; }
                if (phase == 1) {
                    if (size > 0) {
                        emit(StepType.DEQ_START, 0, data[0]);
                        int val = data[0];
                        emit(StepType.DEQ_REMOVE, 0, val);
                        for (int i = 1; i < size; i++) data[i - 1] = data[i];
                        size--;
                        auxStack[++auxTop] = val; emit(StepType.AUXS_PUSH, auxTop, val);
                        return true;
                    } else { phase = 2; emit(StepType.REV_PHASE2_START, -1, null); return true; }
                }
                if (phase == 2) {
                    if (auxTop >= 0) {
                        int val = auxStack[auxTop--]; emit(StepType.AUXS_POP, auxTop + 1, val);
                        emit(StepType.ENQ_START, size, val);
                        data[size] = val; size++;
                        emit(StepType.ENQ_DONE, size - 1, val);
                        return true;
                    } else { phase = 3; return true; }
                }
                if (phase == 3) { emit(StepType.REV_DONE, -1, null); finish(); return true; }
                return false;
            }
            case CLEAR: {
                emit(StepType.CLEAR, -1, null); size = 0; auxQSize = 0; auxTop = -1; finish(); return true;
            }
            default: return false;
        }
    }

    private void finish() { currentOp = OpType.NONE; currentValue = null; phase = 0; searchIndex = 0; removed = false; }

    public State snapshot() {
        State s = new State();
        s.capacity = capacity;
        s.size = size;
        s.data = Arrays.copyOf(data, data.length);
        s.auxQueue = Arrays.copyOf(auxQueue, auxQueue.length);
        s.auxQSize = auxQSize;
        s.auxStack = Arrays.copyOf(auxStack, auxStack.length);
        s.auxTop = auxTop;
        s.currentOp = currentOp.name();
        s.currentValue = currentValue;
        s.phase = phase;
        s.searchIndex = searchIndex;
        s.removed = removed;
        s.queueTypes = new ArrayList<>();
        s.queueValues = new ArrayList<>();
        for (PendingOp p : opQueue) { s.queueTypes.add(p.type.name()); s.queueValues.add(p.value); }
        return s;
    }

    public void restore(State s) {
        if (s == null) return;
        capacity = s.capacity;
        data = Arrays.copyOf(s.data, s.data.length);
        size = s.size;
        auxQueue = Arrays.copyOf(s.auxQueue, s.auxQueue.length);
        auxQSize = s.auxQSize;
        auxStack = Arrays.copyOf(s.auxStack, s.auxStack.length);
        auxTop = s.auxTop;
        currentOp = s.currentOp != null ? OpType.valueOf(s.currentOp) : OpType.NONE;
        currentValue = s.currentValue;
        phase = s.phase;
        searchIndex = s.searchIndex;
        removed = s.removed;
        opQueue.clear();
        if (s.queueTypes != null) {
            for (int i = 0; i < s.queueTypes.size(); i++) {
                OpType t = OpType.valueOf(s.queueTypes.get(i));
                Integer v = (s.queueValues != null && i < s.queueValues.size()) ? s.queueValues.get(i) : null;
                opQueue.add(new PendingOp(t, v));
            }
        }
    }
}
