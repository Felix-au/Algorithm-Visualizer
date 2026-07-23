package com.algorithmvisualizer.algorithm;

import java.util.*;

public class TowerOfHanoiSolver {

    public interface StepListener { void onStep(StepType type, StepPayload p); }

    public enum StepType {
        INIT,
        PREP,
        CALL_START,
        BASE_CASE,
        CALL_END,
        MOVE_PREP,
        LIFT_DISK,
        MOVE_HORIZONTAL,
        DROP_DISK,
        MOVE_COMMIT,
        DONE,
        ALERT
    }

    public static class StepPayload {
        public Integer n;
        public Integer disk;
        public Integer fromPeg; // 0=A,1=B,2=C
        public Integer toPeg;
        public Integer auxPeg;
        public Integer depth;
        public Integer moveIndex;
        public String message;
    }

    private StepListener listener;

    public void setStepListener(StepListener l){ this.listener = l; }

    private static class Event { final StepType t; final StepPayload p; Event(StepType t, StepPayload p){ this.t=t; this.p=p; } }
    private final List<Event> events = new ArrayList<>();
    private int cursor = 0;
    private boolean prepared = false;

    private int N = 0;

    public void queueSolve(int n){
        this.N = n;
        events.clear();
        cursor = 0;
        prepared = true;
        // Build full event script
        emit(StepType.INIT, payload(msg("init")));
        emit(StepType.PREP, payloadN(n, msg("prepare for N="+n)));
        build(n, 0, 2, 1, 0, new int[]{0}); // from A(0) to C(2) using B(1)
        emit(StepType.DONE, payload(msg("done")));
    }

    public boolean hasPending(){ return prepared && cursor < events.size(); }

    public boolean step(){
        if (!hasPending()) return false;
        Event e = events.get(cursor++);
        if (listener != null) listener.onStep(e.t, e.p);
        if (e.t == StepType.DONE) { prepared = false; }
        return true;
    }

    private void build(int n, int from, int to, int aux, int depth, int[] moveCounter){
        StepPayload call = payloadCall(n, from, to, aux, depth, msg("call T("+n+","+name(from)+"→"+name(to)+")"));
        emit(StepType.CALL_START, call);
        if (n == 1){
            emit(StepType.BASE_CASE, payloadCall(n, from, to, aux, depth, msg("base case")));
            int disk = 1; // smallest disk id for base move; actual disk id is n in canonical view
            int moveNo = ++moveCounter[0];
            moveSequence(n, from, to, aux, depth, moveNo);
        } else {
            build(n-1, from, aux, to, depth+1, moveCounter);
            int moveNo = ++moveCounter[0];
            moveSequence(n, from, to, aux, depth, moveNo);
            build(n-1, aux, to, from, depth+1, moveCounter);
        }
        emit(StepType.CALL_END, payloadCall(n, from, to, aux, depth, msg("return")));
    }

    private void moveSequence(int disk, int from, int to, int aux, int depth, int moveNo){
        emit(StepType.MOVE_PREP, payloadMove(disk, from, to, aux, depth, moveNo, msg("move disk "+disk+" "+name(from)+"→"+name(to))));
        emit(StepType.LIFT_DISK, payloadMove(disk, from, to, aux, depth, moveNo, msg("lift")));
        emit(StepType.MOVE_HORIZONTAL, payloadMove(disk, from, to, aux, depth, moveNo, msg("slide")));
        emit(StepType.DROP_DISK, payloadMove(disk, from, to, aux, depth, moveNo, msg("drop")));
        emit(StepType.MOVE_COMMIT, payloadMove(disk, from, to, aux, depth, moveNo, msg("commit move")));
    }

    private void emit(StepType t, StepPayload p){ events.add(new Event(t, p)); }

    private StepPayload payload(){ return new StepPayload(); }
    private StepPayload payload(String m){ StepPayload p = new StepPayload(); p.message=m; return p; }
    private StepPayload payloadN(int n, String m){ StepPayload p = new StepPayload(); p.n=n; p.message=m; return p; }
    private StepPayload payloadMove(int disk, int from, int to, int aux, int depth, int moveIndex, String m){
        StepPayload p = new StepPayload(); p.disk=disk; p.fromPeg=from; p.toPeg=to; p.auxPeg=aux; p.depth=depth; p.moveIndex=moveIndex; p.message=m; return p;
    }
    private StepPayload payloadCall(int n, int from, int to, int aux, int depth, String m){
        StepPayload p = new StepPayload(); p.n=n; p.fromPeg=from; p.toPeg=to; p.auxPeg=aux; p.depth=depth; p.message=m; return p;
    }

    private static String name(int peg){ return peg==0?"A": peg==1?"B":"C"; }
    private static String msg(String s){ return s; }
}
