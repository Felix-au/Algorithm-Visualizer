package com.algorithmvisualizer.algorithm;

import java.util.*;

public class LinkedListSolverV2 {
    public enum ListType { SLL, DLL, CSLL, CDLL }

    public static class NodeData {
        public long id;
        public int value;
        public Long nextId;
        public Long prevId;
    }

    public static class StepPayload {
        public int cursorIndex;
        public Long cursorNodeId;
        public int index;
        public Long nodeId;
        public Integer value;
        public String message;
        public Long headId;
        public Long tailId;
        public ListType listType;
    }

    public enum StepType {
        INIT, RESET, SET_TYPE,
        FIND_TAIL_START, FIND_TAIL_CHECK, FIND_TAIL_FOUND,
        MOVE_TO_INDEX_START, MOVE_TO_INDEX_STEP, MOVE_TO_INDEX_DONE,
        // Insert Start
        INS_START_PREP, INS_START_DONE,
        // Insert End
        INS_END_PREP, INS_END_DONE,
        // Insert At Index
        INS_AT_INDEX_PREP, INS_AT_INDEX_DONE,
        // Insert After
        INS_AFTER_PREP, INS_AFTER_DONE,
        // Delete Start/End/Index/ByValue
        DEL_START_PREP, DEL_START_REMOVE, DEL_START_DONE,
        DEL_END_PREP, DEL_END_DONE,
        DEL_AT_INDEX_PREP, DEL_AT_INDEX_DONE,
        DEL_BY_VALUE_PREP, DEL_BY_VALUE_DONE,
        // Traversal/Search
        TRAVERSE_START, TRAVERSE_VISIT, TRAVERSE_DONE,
        SEARCH_START, SEARCH_COMPARE, SEARCH_FOUND, SEARCH_NOT_FOUND,
        // Generic granular steps
        NODE_CREATE, NODE_REMOVE,
        SET_NEXT, CLEAR_NEXT,
        UPDATE_HEAD, UPDATE_TAIL,
        CLEAR, ALERT_BOUNDS, ALERT_EMPTY
    }

    public interface StepListener { void onStep(StepType type, StepPayload p); }

    private StepListener listener;

    private final Map<Long, NodeData> nodes = new LinkedHashMap<>();
    private Long headId = null;
    private Long tailId = null;
    private ListType type = ListType.SLL;
    private long idSeq = 3200L;

    private enum Op { NONE, SET_TYPE, INS_START, INS_END, INS_AT_INDEX, INS_AFTER_VALUE, DEL_START, DEL_END, DEL_AT_INDEX, DEL_BY_VALUE, TRAVERSE, SEARCH, CLEAR }
    private static class Pending { final Op op; final Integer value; final Object param; Pending(Op o, Integer v, Object p){op=o;value=v;param=p;} }
    private final Deque<Pending> queue = new ArrayDeque<>();

    private Op current = Op.NONE; private Integer currentValue; private Object currentParam; private int phase = 0; private Long cursorNodeId = null; private int cursorIndex = -1; private Long prevNodeId = null; private Long savedSuccId = null; private boolean wasTailFlag = false;
    private Set<Long> cycleGuard = new HashSet<>();

    public void setStepListener(StepListener l){ this.listener = l; }

    public void queueSetType(ListType t){ queue.add(new Pending(Op.SET_TYPE, null, t)); }
    public void queueInsertStart(int value){ queue.add(new Pending(Op.INS_START, value, null)); }
    public void queueInsertEnd(int value){ queue.add(new Pending(Op.INS_END, value, null)); }
    public void queueInsertAtIndex(int index, int value){ queue.add(new Pending(Op.INS_AT_INDEX, value, index)); }
    public void queueInsertAfterValue(int afterValue, int value){ queue.add(new Pending(Op.INS_AFTER_VALUE, value, afterValue)); }
    public void queueDeleteStart(){ queue.add(new Pending(Op.DEL_START, null, null)); }
    public void queueDeleteEnd(){ queue.add(new Pending(Op.DEL_END, null, null)); }
    public void queueDeleteAtIndex(int index){ queue.add(new Pending(Op.DEL_AT_INDEX, null, index)); }
    public void queueDeleteByValue(int value){ queue.add(new Pending(Op.DEL_BY_VALUE, value, null)); }
    public void queueTraverse(){ queue.add(new Pending(Op.TRAVERSE, null, null)); }
    public void queueSearch(int value){ queue.add(new Pending(Op.SEARCH, value, null)); }
    public void queueClear(){ queue.add(new Pending(Op.CLEAR, null, null)); }

    public boolean hasPending(){ return current != Op.NONE || !queue.isEmpty(); }

    public boolean step(){
        if (current == Op.NONE){
            if (queue.isEmpty()) return false;
            Pending p = queue.removeFirst();
            current = p.op; currentValue = p.value; currentParam = p.param; phase = 0; cursorNodeId = null; cursorIndex = -1;
        }
        switch (current){
            case SET_TYPE: {
                if (phase == 0){ type = (ListType) currentParam; emit(StepType.SET_TYPE, -1, null, null, msg("set type: "+type)); finish(); return true; }
                break;
            }
            case INS_START: {
                int v = currentValue != null ? currentValue : 0;
                if (phase == 0){ emit(StepType.INS_START_PREP, 0, null, v, msg("insert at start")); phase=1; return true; }
                if (phase == 1){ NodeData n = makeNode(v); emit(StepType.NODE_CREATE, 0, n.id, v, msg("create node")); prevNodeId = null; cursorNodeId = n.id; phase=2; return true; }
                if (phase == 2){ NodeData n = get(cursorNodeId); n.nextId = headId; if (type == ListType.DLL || type == ListType.CDLL){ n.prevId = null; if (headId != null) get(headId).prevId = n.id; } emit(StepType.SET_NEXT, 0, n.id, null, msg("set new.next = oldHead")); phase=3; return true; }
                if (phase == 3){ headId = cursorNodeId; emit(StepType.UPDATE_HEAD, 0, headId, null, msg("update head")); phase=4; return true; }
                if (phase == 4){ if (tailId == null){ tailId = headId; emit(StepType.UPDATE_TAIL, 0, tailId, null, msg("update tail")); } if (isCircular()) linkCircular(); emit(StepType.INS_START_DONE, 0, headId, v, msg("done")); finish(); return true; }
            }
            case INS_END: {
                int v = currentValue != null ? currentValue : 0;
                if (phase == 0){ emit(StepType.FIND_TAIL_START, -1, null, null, msg("find tail")); cursorNodeId = headId; cursorIndex = 0; cycleGuard = new HashSet<>(); phase=1; return true; }
                if (phase == 1){
                    if (headId == null){ emit(StepType.FIND_TAIL_FOUND, -1, null, null, msg("empty -> no tail")); phase=2; return true; }
                    if (cursorNodeId != null){
                        Long nx = get(cursorNodeId).nextId;
                        if (nx != null){
                            if (Objects.equals(nx, cursorNodeId) || cycleGuard.contains(cursorNodeId)){
                                emit(StepType.FIND_TAIL_FOUND, cursorIndex, cursorNodeId, null, msg("loop detected -> treat as tail")); phase=2; return true;
                            }
                            emit(StepType.FIND_TAIL_CHECK, cursorIndex, cursorNodeId, null, msg("next != null -> move next"));
                            cycleGuard.add(cursorNodeId);
                            cursorNodeId = nx; cursorIndex++; return true;
                        } else { emit(StepType.FIND_TAIL_FOUND, cursorIndex, cursorNodeId, null, msg("next == null -> tail")); phase=2; return true; }
                    }
                }
                if (phase == 2){ emit(StepType.INS_END_PREP, -1, null, v, msg("insert at end")); NodeData n = makeNode(v); prevNodeId = tailId; cursorNodeId = n.id; emit(StepType.NODE_CREATE, -1, n.id, v, msg("create node")); phase=3; return true; }
                if (phase == 3){ if (headId == null){ headId = cursorNodeId; emit(StepType.UPDATE_HEAD, -1, headId, null, msg("update head")); phase=4; return true; } else { get(tailId).nextId = cursorNodeId; if (type == ListType.DLL || type == ListType.CDLL) get(cursorNodeId).prevId = tailId; emit(StepType.SET_NEXT, -1, tailId, null, msg("set tail.next = new")); phase=4; return true; } }
                if (phase == 4){ if (tailId == null || !Objects.equals(tailId, cursorNodeId)){ tailId = cursorNodeId; emit(StepType.UPDATE_TAIL, -1, tailId, null, msg("update tail")); } if (isCircular()) linkCircular(); emit(StepType.INS_END_DONE, -1, tailId, v, msg("done")); finish(); return true; }
            }
            case DEL_START: {
                if (phase == 0){ if (headId == null){ emit(StepType.ALERT_EMPTY, -1, null, null, msg("empty")); finish(); return true; } emit(StepType.DEL_START_PREP, 0, headId, get(headId).value, msg("delete start")); phase=1; return true; }
                if (phase == 1){ Long old = headId; Long newHead = get(headId).nextId; 
                    // remove old head first (visual: removed head)
                    nodes.remove(old);
                    emit(StepType.DEL_START_REMOVE, 0, old, null, msg("removed"));
                    // then update head pointer
                    headId = newHead; if (type == ListType.DLL || type == ListType.CDLL){ if (headId != null) get(headId).prevId = null; }
                    emit(StepType.UPDATE_HEAD, 0, headId, null, msg("update head"));
                    if (headId == null){ tailId = null; emit(StepType.UPDATE_TAIL, 0, null, null, msg("update tail")); }
                    if (isCircular()) linkCircular();
                    phase=2; return true; }
                if (phase == 2){ emit(StepType.DEL_START_DONE, 0, headId, null, msg("done")); finish(); return true; }
            }
            case DEL_END: {
                if (phase == 0){ if (headId == null){ emit(StepType.ALERT_EMPTY, -1, null, null, msg("empty")); finish(); return true; } emit(StepType.FIND_TAIL_START, -1, null, null, msg("find tail for delete")); cursorNodeId = headId; cursorIndex = 0; cycleGuard = new HashSet<>(); phase=1; return true; }
                if (phase == 1){
                    Long nx = get(cursorNodeId).nextId;
                    if (nx != null){
                        if (Objects.equals(nx, cursorNodeId) || cycleGuard.contains(cursorNodeId)){
                            emit(StepType.FIND_TAIL_FOUND, cursorIndex, cursorNodeId, null, msg("loop detected -> treat as tail")); phase=2; return true;
                        }
                        emit(StepType.FIND_TAIL_CHECK, cursorIndex, cursorNodeId, null, msg("next != null -> move next"));
                        cycleGuard.add(cursorNodeId);
                        prevNodeId = cursorNodeId; cursorNodeId = nx; cursorIndex++; return true;
                    } else { emit(StepType.FIND_TAIL_FOUND, cursorIndex, cursorNodeId, null, msg("tail")); phase=2; return true; }
                }
                if (phase == 2){ emit(StepType.DEL_END_PREP, cursorIndex, cursorNodeId, get(cursorNodeId).value, msg("delete end")); phase=3; return true; }
                if (phase == 3){ if (Objects.equals(headId, tailId)) { // single node
                        Long old = tailId; emit(StepType.NODE_REMOVE, cursorIndex, old, null, msg("remove node")); nodes.remove(old); headId = null; tailId = null; phase=5; return true; }
                    Long prev = prevNodeId != null ? prevNodeId : findPrev(tailId); if (prev != null){ int tailVal = get(tailId)!=null? get(tailId).value : 0; get(prev).nextId = null; emit(StepType.CLEAR_NEXT, cursorIndex-1, prev, tailVal, msg("set prev.next = null")); prevNodeId = prev; phase=4; return true; } else { phase=4; return true; } }
                if (phase == 4){ Long old = tailId; emit(StepType.NODE_REMOVE, cursorIndex, old, null, msg("remove tail")); nodes.remove(old); tailId = prevNodeId; if (isCircular()) linkCircular(); phase=5; return true; }
                if (phase == 5){ emit(StepType.UPDATE_TAIL, cursorIndex-1, tailId, null, msg("update tail")); emit(StepType.DEL_END_DONE, cursorIndex-1, tailId, null, msg("done")); finish(); return true; }
            }
            case INS_AT_INDEX: {
                int v = currentValue != null ? currentValue : 0;
                int target = (currentParam instanceof Integer) ? (Integer) currentParam : 0;
                if (target < 0){ emit(StepType.ALERT_BOUNDS, target, null, v, msg("Invalid index.")); finish(); return true; }
                if (phase == 0){
                    if (target == 0){ emit(StepType.INS_AT_INDEX_PREP, 0, null, v, msg("insert at index 0")); phase=2; return true; }
                    emit(StepType.MOVE_TO_INDEX_START, 0, headId, null, msg("move to index")); cursorNodeId = headId; prevNodeId = null; cursorIndex = 0; phase=1; return true; }
                if (phase == 1){
                    if (cursorNodeId == null){ emit(StepType.ALERT_BOUNDS, target, null, v, msg("Index out of range.")); finish(); return true; }
                    emit(StepType.MOVE_TO_INDEX_STEP, cursorIndex, cursorNodeId, null, msg("step"));
                    if (cursorIndex == target - 1){ emit(StepType.MOVE_TO_INDEX_DONE, cursorIndex, cursorNodeId, null, msg("reached prev")); phase=2; return true; }
                    prevNodeId = cursorNodeId; cursorNodeId = get(cursorNodeId).nextId; cursorIndex++; return true; }
                if (phase == 2){ NodeData n = makeNode(v); cursorIndex = target; emit(StepType.NODE_CREATE, target, n.id, v, msg("create node")); prevNodeId = (target == 0 ? null : (cursorNodeId != null ? cursorNodeId : prevNodeId)); cursorNodeId = n.id; phase=3; return true; }
                if (phase == 3){ if (target == 0){ get(cursorNodeId).nextId = headId; emit(StepType.SET_NEXT, 0, cursorNodeId, null, msg("set new.next = oldHead")); phase=4; return true; } else { Long prev = prevNodeId; Long succ = get(prev).nextId; get(cursorNodeId).nextId = succ; emit(StepType.SET_NEXT, target, cursorNodeId, null, msg("set new.next = prev.next")); phase=4; return true; } }
                if (phase == 4){ if (target == 0){ headId = cursorNodeId; emit(StepType.UPDATE_HEAD, 0, headId, null, msg("update head")); if (tailId == null) { tailId = headId; emit(StepType.UPDATE_TAIL, 0, tailId, null, msg("update tail")); } } else { Long prev = prevNodeId; get(prev).nextId = cursorNodeId; emit(StepType.SET_NEXT, target-1, prev, null, msg("link prev.next = new")); if (Objects.equals(tailId, prev) && get(cursorNodeId).nextId == null){ tailId = cursorNodeId; emit(StepType.UPDATE_TAIL, target, tailId, null, msg("update tail")); } }
                    if (isCircular()) linkCircular(); emit(StepType.INS_AT_INDEX_DONE, target, cursorNodeId, v, msg("done")); finish(); return true; }
            }
            case INS_AFTER_VALUE: {
                int v = currentValue != null ? currentValue : 0;
                int targetVal = (currentParam instanceof Integer)? (Integer) currentParam : 0;
                if (phase == 0){ emit(StepType.SEARCH_START, -1, null, targetVal, msg("search value")); cursorNodeId = headId; cursorIndex = 0; phase=1; return true; }
                if (phase == 1){ if (cursorNodeId == null){ emit(StepType.ALERT_BOUNDS, -1, null, targetVal, msg("Value "+targetVal+" not found.")); finish(); return true; }
                    int cv = get(cursorNodeId).value; emit(StepType.SEARCH_COMPARE, cursorIndex, cursorNodeId, cv, msg("compare")); if (cv == targetVal){ prevNodeId = cursorNodeId; emit(StepType.INS_AFTER_PREP, cursorIndex, cursorNodeId, v, msg("insert after value")); phase=2; return true; } prevNodeId = cursorNodeId; cursorNodeId = get(cursorNodeId).nextId; cursorIndex++; return true; }
                if (phase == 2){ NodeData n = makeNode(v); Long prev = prevNodeId; Long succ = get(prev).nextId; cursorNodeId = n.id; emit(StepType.NODE_CREATE, cursorIndex, n.id, v, msg("create node")); phase=3; return true; }
                if (phase == 3){ Long prev = prevNodeId; Long succ = get(prev).nextId; get(cursorNodeId).nextId = succ; emit(StepType.SET_NEXT, cursorIndex, cursorNodeId, null, msg("set new.next = prev.next")); phase=4; return true; }
                if (phase == 4){ Long prev = prevNodeId; get(prev).nextId = cursorNodeId; emit(StepType.SET_NEXT, cursorIndex, prev, null, msg("link prev.next = new")); if (Objects.equals(tailId, prev)) { tailId = cursorNodeId; emit(StepType.UPDATE_TAIL, cursorIndex, tailId, null, msg("update tail")); } if (isCircular()) linkCircular(); emit(StepType.INS_AFTER_DONE, cursorIndex, null, v, msg("done")); finish(); return true; }
            }
            case DEL_AT_INDEX: {
                int target = (currentParam instanceof Integer) ? (Integer) currentParam : 0;
                if (target < 0){ emit(StepType.ALERT_BOUNDS, target, null, null, msg("Invalid index.")); finish(); return true; }
                if (phase == 0){ if (headId == null){ emit(StepType.ALERT_EMPTY, -1, null, null, msg("List is empty.")); finish(); return true; } if (target==0){ emit(StepType.DEL_AT_INDEX_PREP, 0, headId, get(headId).value, msg("delete index 0")); phase=2; return true; } emit(StepType.MOVE_TO_INDEX_START, 0, headId, null, msg("move to index")); cursorNodeId = headId; cursorIndex = 0; phase=1; return true; }
                if (phase == 1){ if (cursorNodeId == null){ emit(StepType.ALERT_BOUNDS, target, null, null, msg("Index out of range.")); finish(); return true; } emit(StepType.MOVE_TO_INDEX_STEP, cursorIndex, cursorNodeId, null, msg("step")); if (cursorIndex == target-1){ emit(StepType.MOVE_TO_INDEX_DONE, cursorIndex, cursorNodeId, null, msg("reached prev")); phase=2; return true; } cursorNodeId = get(cursorNodeId).nextId; cursorIndex++; return true; }
                if (phase == 2){ if (target==0){ Long old=headId; headId=get(headId).nextId; emit(StepType.UPDATE_HEAD, 0, headId, null, msg("update head")); emit(StepType.NODE_REMOVE, 0, old, null, msg("remove node")); nodes.remove(old); if (headId==null) { tailId=null; emit(StepType.UPDATE_TAIL, 0, null, null, msg("update tail")); } if (isCircular()) linkCircular(); emit(StepType.DEL_AT_INDEX_DONE, 0, headId, null, msg("done")); finish(); return true; }
                    Long prev = cursorNodeId; Long curr = get(prev).nextId; if (curr==null){ emit(StepType.ALERT_BOUNDS, target, null, null, msg("Index out of range.")); finish(); return true; }
                    emit(StepType.DEL_AT_INDEX_PREP, target, curr, get(curr).value, msg("delete at index"));
                    Long succ = get(curr).nextId; savedSuccId = succ; wasTailFlag = Objects.equals(tailId, curr); cursorNodeId = curr; int currVal = get(curr).value; get(prev).nextId = null; emit(StepType.CLEAR_NEXT, target-1, prev, currVal, msg("set prev.next = null")); prevNodeId = prev; phase=3; return true; }
                if (phase == 3){ Long removed = cursorNodeId; emit(StepType.NODE_REMOVE, cursorIndex, removed, null, msg("remove node")); nodes.remove(removed); if (wasTailFlag){ tailId = prevNodeId; emit(StepType.UPDATE_TAIL, cursorIndex-1, tailId, null, msg("update tail")); if (isCircular()) linkCircular(); emit(StepType.DEL_AT_INDEX_DONE, -1, headId, null, msg("done")); finish(); return true; } phase=4; return true; }
                if (phase == 4){ Long prev = prevNodeId; get(prev).nextId = savedSuccId; emit(StepType.SET_NEXT, target-1, prev, null, msg("link prev.next = succ")); if (isCircular()) linkCircular(); emit(StepType.DEL_AT_INDEX_DONE, -1, headId, null, msg("done")); finish(); return true; }
            }
            case DEL_BY_VALUE: {
                int targetVal = currentValue != null ? currentValue : 0;
                if (phase == 0){ if (headId == null){ emit(StepType.ALERT_EMPTY, -1, null, null, msg("List is empty.")); finish(); return true; } emit(StepType.SEARCH_START, -1, null, targetVal, msg("search value")); cursorNodeId = headId; prevNodeId = null; cursorIndex = 0; phase=1; return true; }
                if (phase == 1){ if (cursorNodeId == null){ emit(StepType.ALERT_BOUNDS, -1, null, targetVal, msg("Value "+targetVal+" not found.")); finish(); return true; } int cv = get(cursorNodeId).value; emit(StepType.SEARCH_COMPARE, cursorIndex, cursorNodeId, cv, msg("compare")); if (cv == targetVal){ emit(StepType.DEL_BY_VALUE_PREP, cursorIndex, cursorNodeId, cv, msg("delete by value")); phase=2; return true; } prevNodeId = cursorNodeId; cursorNodeId = get(cursorNodeId).nextId; cursorIndex++; return true; }
                if (phase == 2){ if (prevNodeId == null){ // deleting head
                        Long old = headId; headId = get(headId).nextId; emit(StepType.UPDATE_HEAD, 0, headId, null, msg("update head")); emit(StepType.NODE_REMOVE, 0, old, targetVal, msg("remove node")); nodes.remove(old); if (headId == null){ tailId = null; emit(StepType.UPDATE_TAIL, 0, null, null, msg("update tail")); } if (isCircular()) linkCircular(); emit(StepType.DEL_BY_VALUE_DONE, -1, null, targetVal, msg("done")); finish(); return true; }
                    Long toDel = cursorNodeId; Long succ = get(cursorNodeId).nextId; savedSuccId = succ; wasTailFlag = Objects.equals(tailId, toDel); int delVal = get(toDel).value; get(prevNodeId).nextId = null; emit(StepType.CLEAR_NEXT, cursorIndex-1, prevNodeId, delVal, msg("set prev.next = null")); phase=3; return true; }
                if (phase == 3){ Long toDel = cursorNodeId; emit(StepType.NODE_REMOVE, cursorIndex, toDel, targetVal, msg("remove node")); nodes.remove(toDel); if (wasTailFlag){ tailId = prevNodeId; emit(StepType.UPDATE_TAIL, cursorIndex-1, tailId, null, msg("update tail")); if (isCircular()) linkCircular(); emit(StepType.DEL_BY_VALUE_DONE, -1, null, targetVal, msg("done")); finish(); return true; } phase=4; return true; }
                if (phase == 4){ get(prevNodeId).nextId = savedSuccId; emit(StepType.SET_NEXT, cursorIndex-1, prevNodeId, null, msg("link prev.next = succ")); if (isCircular()) linkCircular(); emit(StepType.DEL_BY_VALUE_DONE, -1, null, targetVal, msg("done")); finish(); return true; }
            }
            case TRAVERSE: {
                if (phase == 0){ emit(StepType.TRAVERSE_START, -1, null, null, msg("traverse")); cursorNodeId = headId; cursorIndex = 0; phase=1; return true; }
                if (cursorNodeId != null){ emit(StepType.TRAVERSE_VISIT, cursorIndex, cursorNodeId, get(cursorNodeId).value, msg("visit")); cursorNodeId = get(cursorNodeId).nextId; cursorIndex++; return true; }
                emit(StepType.TRAVERSE_DONE, -1, null, null, msg("done")); finish(); return true;
            }
            case SEARCH: {
                if (phase == 0){ emit(StepType.SEARCH_START, -1, null, currentValue, msg("search value")); cursorNodeId = headId; cursorIndex = 0; phase=1; return true; }
                if (cursorNodeId != null){ int v = get(cursorNodeId).value; emit(StepType.SEARCH_COMPARE, cursorIndex, cursorNodeId, v, msg("compare")); if (v == currentValue){ emit(StepType.SEARCH_FOUND, cursorIndex, cursorNodeId, v, msg("found")); finish(); return true; } cursorNodeId = get(cursorNodeId).nextId; cursorIndex++; return true; }
                emit(StepType.SEARCH_NOT_FOUND, -1, null, currentValue, msg("not found")); finish(); return true;
            }
            case CLEAR: {
                if (phase == 0){ nodes.clear(); headId=null; tailId=null; emit(StepType.CLEAR, -1, null, null, msg("clear")); finish(); return true; }
            }
            default: return false;
        }
        return false;
    }

    private void finish(){ current = Op.NONE; currentValue=null; currentParam=null; phase=0; cursorNodeId=null; cursorIndex=-1; prevNodeId=null; savedSuccId=null; wasTailFlag=false; }

    private NodeData makeNode(int value){
        NodeData n = new NodeData();
        n.id = idSeq; idSeq += 200;
        n.value = value;
        n.nextId = null;
        n.prevId = null;
        nodes.put(n.id, n);
        return n;
    }

    private NodeData get(Long id){ return id == null ? null : nodes.get(id); }

    private Long findPrev(Long id){
        if (id == null || headId == null || headId.equals(id)) return null;
        Long p = headId;
        while (p != null){
            if (Objects.equals(get(p).nextId, id)) return p;
            p = get(p).nextId;
        }
        return null;
    }

    private boolean isCircular(){ return type == ListType.CSLL || type == ListType.CDLL; }

    private void linkCircular(){
        if (headId == null){ tailId = null; return; }
        Long t = headId; while (get(t).nextId != null) t = get(t).nextId; tailId = t;
        if (isCircular()) get(tailId).nextId = headId; else get(tailId).nextId = get(tailId).nextId; // no-op for SLL/DLL
        if (type == ListType.CDLL && headId != null){ get(headId).prevId = tailId; }
        if (type == ListType.DLL || type == ListType.CDLL){
            Long p = null; Long c = headId;
            while (c != null && (isCircular() ? (p == null || !Objects.equals(c, headId)) : true)){
                NodeData nd = get(c);
                nd.prevId = p;
                if (nd.nextId == null && isCircular()) nd.nextId = headId;
                p = c; c = nd.nextId; if (isCircular() && Objects.equals(c, headId)) break;
            }
        }
        if (!isCircular() && headId != null){ get(tailId).nextId = null; }
    }

    private void emit(StepType t, int idx, Long nodeId, Integer value, String message){
        if (listener == null) return;
        StepPayload p = new StepPayload();
        p.cursorIndex = cursorIndex;
        p.cursorNodeId = cursorNodeId;
        p.index = idx;
        p.nodeId = nodeId;
        p.value = value;
        p.message = message;
        p.headId = headId;
        p.tailId = tailId;
        p.listType = type;
        listener.onStep(t, p);
    }

    private String msg(String s){ return s; }

    public static class State {
        public ListType type;
        public Long headId;
        public Long tailId;
        public long idSeq;
        public List<NodeData> nodes;
        public String current;
        public Integer currentValue;
        public Object currentParam;
        public int phase;
        public Long cursorNodeId;
        public int cursorIndex;
        public Long prevNodeId;
        public List<String> qops;
        public List<Integer> qvals;
        public List<String> qparams;
    }

    public State snapshot(){
        State s = new State();
        s.type = type; s.headId = headId; s.tailId = tailId; s.idSeq = idSeq;
        s.nodes = new ArrayList<>();
        for (NodeData nd : nodes.values()){ NodeData c = new NodeData(); c.id=nd.id; c.value=nd.value; c.nextId=nd.nextId; c.prevId=nd.prevId; s.nodes.add(c);}        
        s.current = current.name(); s.currentValue=currentValue; s.currentParam=currentParam; s.phase=phase; s.cursorNodeId=cursorNodeId; s.cursorIndex=cursorIndex; s.prevNodeId=prevNodeId;
        s.qops = new ArrayList<>(); s.qvals = new ArrayList<>(); s.qparams = new ArrayList<>();
        for (Pending p: queue){ s.qops.add(p.op.name()); s.qvals.add(p.value); s.qparams.add(p.param!=null?p.param.toString():null);}        
        return s;
    }

    public void restore(State s){
        type = s.type; headId = s.headId; tailId = s.tailId; idSeq = s.idSeq;
        nodes.clear(); for (NodeData nd : s.nodes){ NodeData c = new NodeData(); c.id=nd.id; c.value=nd.value; c.nextId=nd.nextId; c.prevId=nd.prevId; nodes.put(c.id, c);}        
        current = Op.NONE; currentValue=null; currentParam=null; phase=0; cursorNodeId=null; cursorIndex=-1; prevNodeId=null; queue.clear();
        if (s.qops != null){ for (int i=0;i<s.qops.size();i++){ Op op = Op.valueOf(s.qops.get(i)); Integer v = (s.qvals!=null && i<s.qvals.size())?s.qvals.get(i):null; String ps = (s.qparams!=null && i<s.qparams.size())?s.qparams.get(i):null; Object param=null; if (ps!=null){ if (op==Op.SET_TYPE) param = ListType.valueOf(ps); else { try { param = Integer.valueOf(ps);} catch(Exception ex){ param = ps; } } } queue.add(new Pending(op,v,param)); } }
        emit(StepType.RESET, -1, null, null, "reset");
    }

    public List<NodeData> getOrderedNodes(){
        List<NodeData> out = new ArrayList<>();
        Long p = headId; int guard=0; Set<Long> seen = new HashSet<>();
        while (p != null && !seen.contains(p) && guard < 10000){
            NodeData nd = get(p); if (nd == null) break; out.add(copy(nd)); seen.add(p); p = nd.nextId; guard++;
            if (isCircular() && Objects.equals(p, headId)) break;
        }
        return out;
    }

    private NodeData copy(NodeData nd){ NodeData c = new NodeData(); c.id=nd.id; c.value=nd.value; c.nextId=nd.nextId; c.prevId=nd.prevId; return c; }

    public int getSize(){ int n=0; Long p=headId; Set<Long> seen=new HashSet<>(); while(p!=null && !seen.contains(p)){ n++; seen.add(p); NodeData nd=get(p); if(nd==null) break; p=nd.nextId; if(isCircular()&&Objects.equals(p, headId)) break;} return n; }
    public Long getHeadId(){ return headId; }
    public Long getTailId(){ return tailId; }
    public ListType getType(){ return type; }
}
