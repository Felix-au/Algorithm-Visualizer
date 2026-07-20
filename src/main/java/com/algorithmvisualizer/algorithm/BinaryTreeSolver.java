package com.algorithmvisualizer.algorithm;

import java.util.*;

/**
 * Step-driven Binary Search Tree builder and traversal generator.
 * Emits granular events for UI to visualize.
 */
public class BinaryTreeSolver {

    public interface StepListener { void onStep(StepType type, StepPayload p); }

    public enum StepType {
        INIT,
        START_BUILD,
        COMPARE_AT,
        MOVE_LEFT,
        MOVE_RIGHT,
        ATTACH_NODE,
        LINK_EDGE,
        UNLINK_EDGE,
        REMOVE_NODE,
        UPDATE_NODE_KEY,
        BACKTRACK,
        RELAYOUT,
        START_TRAVERSAL,
        VISIT,
        OUTPUT_APPEND,
        HEIGHT_COMPUTE,
        DONE,
        ALERT
    }

    public static class StepPayload {
        public Integer key;
        public Integer nodeId;
        public Integer parentId;
        public Integer leftId;
        public Integer rightId;
        public Integer direction; // -1 left, +1 right
        public Integer depth;
        public String message;
    }

    private StepListener listener;
    public void setStepListener(StepListener l){ this.listener = l; }

    private static class Event { final StepType t; final StepPayload p; Event(StepType t, StepPayload p){ this.t=t; this.p=p; } }
    private final List<Event> events = new ArrayList<>();
    private int cursor = 0; private boolean prepared = false;
    // temp state used during queueHeight to emit running tree height
    private int rollingMaxHeight = -1;

    // Internal BST model
    private static class Node { int id; int key; int left=-1; int right=-1; int parent=-1; }
    private final Map<Integer, Node> nodes = new HashMap<>();
    private int root = -1; private int nextId = 0;

    public void reset(){ events.clear(); cursor=0; prepared=false; nodes.clear(); root=-1; nextId=0; }

    public void queueBuild(List<Integer> values){
        reset();
        emit(StepType.INIT, msg("init"));
        emit(StepType.START_BUILD, msg("start build"));
        if (values == null || values.isEmpty()){
            emit(StepType.DONE, msg("build done")); prepared = true; return;
        }
        // Create root
        int id0 = nextId++;
        Node rootNode = putNode(id0, values.get(0));
        root = id0;
        StepPayload attRoot = payload(rootNode, "attach root");
        emit(StepType.ATTACH_NODE, attRoot);
        emit(StepType.RELAYOUT, msg("relayout"));
        // Level-wise attach remaining values
        Queue<Integer> q = new ArrayDeque<>(); q.offer(root);
        int i = 1;
        while (i < values.size()){
            int parentId = q.poll();
            Node parent = nodes.get(parentId);
            // Left
            if (i < values.size()){
                int lid = nextId++;
                Node ln = putNode(lid, values.get(i++)); ln.parent = parentId; parent.left = lid;
                StepPayload cmp = payload(parent, "parent "+parent.key); emit(StepType.COMPARE_AT, cmp);
                StepPayload mvL = new StepPayload(); mvL.nodeId = parentId; mvL.direction = -1; mvL.message = "attach left"; events.add(new Event(StepType.MOVE_LEFT, mvL));
                StepPayload att = payload(ln, "attach left"); att.parentId = parentId; emit(StepType.ATTACH_NODE, att);
                // highlight newly attached node
                emit(StepType.VISIT, payload(ln, "new"));
                StepPayload link = new StepPayload(); link.parentId = parentId; link.nodeId = lid; link.direction = -1; emit(StepType.LINK_EDGE, link);
                emit(StepType.RELAYOUT, msg("relayout"));
                q.offer(lid);
            }
            // Right
            if (i < values.size()){
                int rid = nextId++;
                Node rn = putNode(rid, values.get(i++)); rn.parent = parentId; parent.right = rid;
                StepPayload cmp = payload(parent, "parent "+parent.key); emit(StepType.COMPARE_AT, cmp);
                StepPayload mvR = new StepPayload(); mvR.nodeId = parentId; mvR.direction = +1; mvR.message = "attach right"; events.add(new Event(StepType.MOVE_RIGHT, mvR));
                StepPayload att = payload(rn, "attach right"); att.parentId = parentId; emit(StepType.ATTACH_NODE, att);
                // highlight newly attached node
                emit(StepType.VISIT, payload(rn, "new"));
                StepPayload link = new StepPayload(); link.parentId = parentId; link.nodeId = rid; link.direction = +1; emit(StepType.LINK_EDGE, link);
                emit(StepType.RELAYOUT, msg("relayout"));
                q.offer(rid);
            }
            // Done processing this parent
            String why = (parent.left != -1 && parent.right != -1) ? "full" : "one child free";
            StepPayload bt = payload(parent, why); emit(StepType.BACKTRACK, bt);
        }
        emit(StepType.DONE, msg("build done"));
        prepared = true;
    }

    public void queueInorder(){
        events.clear(); cursor=0; prepared=false;
        events.add(new Event(StepType.START_TRAVERSAL, msg("Inorder:")));
        inorderScript(root, 0);
        events.add(new Event(StepType.DONE, msg("traversal done")));
        prepared = true;
    }

    public void queuePreorder(){
        events.clear(); cursor=0; prepared=false;
        events.add(new Event(StepType.START_TRAVERSAL, msg("Preorder:")));
        preorderScript(root, 0);
        events.add(new Event(StepType.DONE, msg("traversal done")));
        prepared = true;
    }

    public void queuePostorder(){
        events.clear(); cursor=0; prepared=false;
        events.add(new Event(StepType.START_TRAVERSAL, msg("Postorder:")));
        postorderScript(root, 0);
        events.add(new Event(StepType.DONE, msg("traversal done")));
        prepared = true;
    }

    public void queueLevelOrder(){
        events.clear(); cursor=0; prepared=false;
        events.add(new Event(StepType.START_TRAVERSAL, msg("Level Order:")));
        levelOrderScript(root);
        events.add(new Event(StepType.DONE, msg("traversal done")));
        prepared = true;
    }

    public void queueInsert(int key){
        events.clear(); cursor=0; prepared=false;
        insertScript(key);
        events.add(new Event(StepType.DONE, msg("insert done")));
        prepared = true;
    }

    public void queueDelete(int key){
        events.clear(); cursor=0; prepared=false;
        if (root < 0){ events.add(new Event(StepType.ALERT, msg("tree empty"))); events.add(new Event(StepType.DONE, msg("delete done"))); prepared=true; return; }
        // Find key node and deepest node via level order
        Integer keyNodeId = null; Integer deepestId = null; Integer deepestParent = null;
        Queue<Integer> q = new ArrayDeque<>(); q.offer(root);
        Map<Integer,Integer> parentMap = new HashMap<>(); parentMap.put(root, -1);
        while (!q.isEmpty()){
            int u = q.poll(); Node n = nodes.get(u);
            StepPayload cmp = payload(n, "visit "+n.key); cmp.key = key; events.add(new Event(StepType.COMPARE_AT, cmp));
            if (n.key == key && keyNodeId == null) keyNodeId = u;
            if (n.left >= 0){ parentMap.put(n.left, u); q.offer(n.left); }
            if (n.right >= 0){ parentMap.put(n.right, u); q.offer(n.right); }
            deepestId = u;
            events.add(new Event(StepType.BACKTRACK, payload(n, "scan done")));
        }
        if (keyNodeId == null){ events.add(new Event(StepType.ALERT, msg("key not found"))); events.add(new Event(StepType.DONE, msg("delete done"))); prepared=true; return; }
        // Replace key at keyNode with deepest key
        Node keyNode = nodes.get(keyNodeId);
        Node deepNode = nodes.get(deepestId);
        // highlight chosen nodes
        events.add(new Event(StepType.COMPARE_AT, payload(keyNode, "target "+keyNode.key)));
        events.add(new Event(StepType.COMPARE_AT, payload(deepNode, "deepest "+deepNode.key)));
        keyNode.key = deepNode.key;
        StepPayload upd = new StepPayload(); upd.nodeId = keyNodeId; upd.key = keyNode.key; events.add(new Event(StepType.UPDATE_NODE_KEY, upd));
        // Delete deepest node
        deepestParent = parentMap.getOrDefault(deepestId, -1);
        if (deepestParent >= 0){
            Node p = nodes.get(deepestParent);
            StepPayload unlink = new StepPayload(); unlink.parentId = deepestParent; unlink.nodeId = deepestId; events.add(new Event(StepType.UNLINK_EDGE, unlink));
            if (p.left == deepestId) p.left = -1; else p.right = -1;
        } else {
            // deepest is root
            root = -1;
        }
        nodes.remove(deepestId);
        StepPayload remove = new StepPayload(); remove.nodeId = deepestId; events.add(new Event(StepType.REMOVE_NODE, remove));
        // granular rebuild walk (visual only)
        if (root >= 0){
            Queue<Integer> rq = new ArrayDeque<>(); rq.offer(root);
            while (!rq.isEmpty()){
                int x = rq.poll(); Node nx = nodes.get(x); if (nx==null) continue;
                events.add(new Event(StepType.COMPARE_AT, payload(nx, "rebuild "+nx.key)));
                if (nx.left >= 0) rq.offer(nx.left);
                if (nx.right >= 0) rq.offer(nx.right);
                events.add(new Event(StepType.BACKTRACK, payload(nx, "rebuild backtrack")));
            }
        }
        events.add(new Event(StepType.RELAYOUT, msg("relayout")));
        events.add(new Event(StepType.DONE, msg("delete done")));
        prepared = true;
    }

    public void queueHeight(){
        events.clear(); cursor=0; prepared=false;
        rollingMaxHeight = -1;
        events.add(new Event(StepType.HEIGHT_COMPUTE, msg("height start")));
        // show an initial tree height (0) so the UI displays it from the start
        StepPayload init = new StepPayload(); init.message = "treeHeight@0"; events.add(new Event(StepType.OUTPUT_APPEND, init));
        int h = heightScript(root, 0);
        StepPayload out = new StepPayload(); out.message = "height="+h; events.add(new Event(StepType.OUTPUT_APPEND, out));
        events.add(new Event(StepType.DONE, msg("height done")));
        prepared = true;
    }

    public boolean hasPending(){ return prepared && cursor < events.size(); }
    public boolean step(){ if (!hasPending()) return false; Event e = events.get(cursor++); if (listener!=null) listener.onStep(e.t, e.p); if (e.t==StepType.DONE) prepared=false; return true; }

    // Playback helpers for UI controllers
    public int getCursor(){ return cursor; }
    public int getEventCount(){ return events.size(); }
    public void setCursor(int c){ cursor = Math.max(0, Math.min(c, events.size())); prepared = cursor < events.size(); }
    /** Re-emit events [0, end) to the current listener without changing cursor */
    public void replayTo(int end){
        if (listener == null) return;
        int to = Math.max(0, Math.min(end, events.size()));
        for (int i = 0; i < to; i++){
            Event e = events.get(i);
            listener.onStep(e.t, e.p);
        }
    }

    // Script builders
    private void insertScript(int key){
        if (root < 0){
            int id = nextId++;
            Node n = putNode(id, key);
            root = id;
            emit(StepType.ATTACH_NODE, payload(n, "attach root"));
            emit(StepType.RELAYOUT, msg("relayout"));
            return;
        }
        Queue<Integer> q = new ArrayDeque<>(); q.offer(root);
        while (!q.isEmpty()){
            int u = q.poll(); Node c = nodes.get(u);
            emit(StepType.COMPARE_AT, payload(c, "at "+c.key));
            if (c.left != -1) { StepPayload mv = new StepPayload(); mv.nodeId=u; mv.direction=-1; mv.message="left occupied"; events.add(new Event(StepType.MOVE_LEFT, mv)); q.offer(c.left); }
            else {
                int id = nextId++; Node n = putNode(id, key); n.parent = u; c.left = id;
                emit(StepType.ATTACH_NODE, payload(n, "attach left"));
                StepPayload link = new StepPayload(); link.parentId = u; link.nodeId = id; link.direction = -1; emit(StepType.LINK_EDGE, link);
                emit(StepType.BACKTRACK, payload(c, "placed at left of "+c.key));
                emit(StepType.RELAYOUT, msg("relayout"));
                return;
            }
            if (c.right != -1) { StepPayload mv = new StepPayload(); mv.nodeId=u; mv.direction=+1; mv.message="right occupied"; events.add(new Event(StepType.MOVE_RIGHT, mv)); q.offer(c.right); }
            else {
                int id = nextId++; Node n = putNode(id, key); n.parent = u; c.right = id;
                emit(StepType.ATTACH_NODE, payload(n, "attach right"));
                StepPayload link = new StepPayload(); link.parentId = u; link.nodeId = id; link.direction = +1; emit(StepType.LINK_EDGE, link);
                emit(StepType.BACKTRACK, payload(c, "placed at right of "+c.key));
                emit(StepType.RELAYOUT, msg("relayout"));
                return;
            }
            // leaving this node without placing -> node is full
            emit(StepType.BACKTRACK, payload(c, "full"));
        }
    }

    private void inorderScript(int node, int depth){
        if (node < 0) return;
        Node n = nodes.get(node);
        inorderScript(n.left, depth+1);
        StepPayload v = payload(n, "visit" ); v.depth = depth; events.add(new Event(StepType.VISIT, v));
        StepPayload out = new StepPayload(); out.key = n.key; events.add(new Event(StepType.OUTPUT_APPEND, out));
        inorderScript(n.right, depth+1);
    }

    private void preorderScript(int node, int depth){
        if (node < 0) return;
        Node n = nodes.get(node);
        StepPayload v = payload(n, "visit" ); v.depth = depth; events.add(new Event(StepType.VISIT, v));
        StepPayload out = new StepPayload(); out.key = n.key; events.add(new Event(StepType.OUTPUT_APPEND, out));
        preorderScript(n.left, depth+1);
        preorderScript(n.right, depth+1);
    }

    private void postorderScript(int node, int depth){
        if (node < 0) return;
        Node n = nodes.get(node);
        postorderScript(n.left, depth+1);
        postorderScript(n.right, depth+1);
        StepPayload v = payload(n, "visit" ); v.depth = depth; events.add(new Event(StepType.VISIT, v));
        StepPayload out = new StepPayload(); out.key = n.key; events.add(new Event(StepType.OUTPUT_APPEND, out));
    }

    private void levelOrderScript(int start){
        if (start < 0) return;
        Queue<Integer> q = new ArrayDeque<>();
        q.offer(start);
        while (!q.isEmpty()){
            int u = q.poll();
            Node n = nodes.get(u); if (n == null) continue;
            StepPayload v = payload(n, "visit"); events.add(new Event(StepType.VISIT, v));
            StepPayload out = new StepPayload(); out.key = n.key; events.add(new Event(StepType.OUTPUT_APPEND, out));
            if (n.left >= 0) q.offer(n.left);
            if (n.right >= 0) q.offer(n.right);
        }
    }

    private void deleteScript(int id){
        Node n = nodes.get(id); if (n == null) return;
        // Two children
        if (n.left >= 0 && n.right >= 0){
            // Find inorder successor: go right then all the way left
            int cur = n.right; int depth=0; int prev=-1;
            while (cur >= 0){
                Node c = nodes.get(cur);
                StepPayload cmp = payload(c, "successor step at "+c.key); cmp.depth = depth; events.add(new Event(StepType.COMPARE_AT, cmp));
                if (c.left >= 0){ events.add(new Event(StepType.MOVE_LEFT, payload(c, "go left"))); prev = cur; cur = c.left; depth++; }
                else break;
            }
            Node succ = nodes.get(cur);
            // Replace key in target node
            n.key = succ.key;
            StepPayload upd = new StepPayload(); upd.nodeId = id; upd.key = n.key; events.add(new Event(StepType.UPDATE_NODE_KEY, upd));
            // Delete successor node (it has no left child)
            if (succ.right >= 0){
                // Link prev (or n) to succ.right
                int parent = (cur == n.right) ? id : prev;
                Node parentNode = nodes.get(parent);
                if (parentNode.left == cur) parentNode.left = succ.right; else parentNode.right = succ.right;
                nodes.get(succ.right).parent = parent;
                StepPayload unlink = new StepPayload(); unlink.parentId = parent; unlink.nodeId = cur; events.add(new Event(StepType.UNLINK_EDGE, unlink));
                StepPayload link = new StepPayload(); link.parentId = parent; link.nodeId = succ.right; link.direction = (parentNode.left == succ.right) ? -1 : +1; events.add(new Event(StepType.LINK_EDGE, link));
            } else {
                // Simply unlink succ from its parent
                int parent = nodes.get(cur).parent;
                if (parent >= 0){
                    Node p = nodes.get(parent);
                    if (p.left == cur) p.left = -1; else p.right = -1;
                    StepPayload unlink = new StepPayload(); unlink.parentId = parent; unlink.nodeId = cur; events.add(new Event(StepType.UNLINK_EDGE, unlink));
                } else {
                    // succ is root's right and also root? shouldn't happen in two-children branch; ignore
                }
            }
            nodes.remove(cur);
            StepPayload remove = new StepPayload(); remove.nodeId = cur; events.add(new Event(StepType.REMOVE_NODE, remove));
            events.add(new Event(StepType.RELAYOUT, msg("relayout")));
            return;
        }
        // One or zero child
        int child = (n.left >= 0) ? n.left : n.right;
        int parent = n.parent;
        if (parent < 0){
            // removing root
            if (child >= 0) { nodes.get(child).parent = -1; }
            root = child;
            // no unlink event (no parent edge), but if child exists, relink from root implicitly
        } else {
            Node p = nodes.get(parent);
            // Unlink parent->id
            if (p.left == id) p.left = child; else p.right = child;
            StepPayload unlink = new StepPayload(); unlink.parentId = parent; unlink.nodeId = id; events.add(new Event(StepType.UNLINK_EDGE, unlink));
            if (child >= 0){
                nodes.get(child).parent = parent;
                StepPayload link = new StepPayload(); link.parentId = parent; link.nodeId = child; link.direction = (nodes.get(parent).left == child)? -1: +1; events.add(new Event(StepType.LINK_EDGE, link));
            }
        }
        nodes.remove(id);
        StepPayload remove = new StepPayload(); remove.nodeId = id; events.add(new Event(StepType.REMOVE_NODE, remove));
        events.add(new Event(StepType.RELAYOUT, msg("relayout")));
    }

    private int height(int node){ if (node<0) return 0; Node n = nodes.get(node); return 1 + Math.max(height(n.left), height(n.right)); }

    private int heightScript(int node, int depth){
        if (node < 0) return -1; // using -1 convention per snippet
        Node n = nodes.get(node);
        StepPayload cmp = payload(n, "height at "+n.key); cmp.depth = depth; emit(StepType.COMPARE_AT, cmp);
        int hl = heightScript(n.left, depth+1);
        StepPayload bkl = payload(n, "backtrack from left"); bkl.depth = depth; emit(StepType.BACKTRACK, bkl);
        int hr = heightScript(n.right, depth+1);
        StepPayload bkr = payload(n, "backtrack from right"); bkr.depth = depth; emit(StepType.BACKTRACK, bkr);
        int h = Math.max(hl, hr) + 1;
        // incremental update event for UI variables panel
        StepPayload up = new StepPayload(); up.nodeId = n.id; up.depth = depth; up.message = "height@"+h; emit(StepType.OUTPUT_APPEND, up);
        // update and emit rolling tree height (max seen so far from any node)
        if (h > rollingMaxHeight) {
            rollingMaxHeight = h;
            StepPayload th = new StepPayload(); th.message = "treeHeight@"+rollingMaxHeight; emit(StepType.OUTPUT_APPEND, th);
        }
        return h;
    }

    public void queueAllTraversals(){
        events.clear(); cursor=0; prepared=false;
        // Inorder
        events.add(new Event(StepType.START_TRAVERSAL, msg("Inorder:")));
        inorderScript(root, 0);
        // Preorder
        events.add(new Event(StepType.START_TRAVERSAL, msg("Preorder:")));
        preorderScript(root, 0);
        // Postorder
        events.add(new Event(StepType.START_TRAVERSAL, msg("Postorder:")));
        postorderScript(root, 0);
        // Level Order
        events.add(new Event(StepType.START_TRAVERSAL, msg("Level Order:")));
        levelOrderScript(root);
        // BFS alias
        events.add(new Event(StepType.START_TRAVERSAL, msg("BFS:")));
        levelOrderScript(root);
        // DFS alias (preorder)
        events.add(new Event(StepType.START_TRAVERSAL, msg("DFS:")));
        preorderScript(root, 0);
        events.add(new Event(StepType.DONE, msg("all traversals done")));
        prepared = true;
    }

    // Helpers
    private Node putNode(int id, int key){ Node n = new Node(); n.id=id; n.key=key; nodes.put(id,n); return n; }
    private StepPayload payload(Node n, String m){ StepPayload p = new StepPayload(); if (n!=null){ p.nodeId=n.id; p.key=n.key; p.leftId=n.left>=0?n.left:null; p.rightId=n.right>=0?n.right:null; } p.message=m; return p; }
    private StepPayload payload(Node n){ return payload(n, null); }
    private StepPayload payload(Integer key, String m){ StepPayload p = new StepPayload(); p.key = key; p.message=m; return p; }
    private StepPayload msg(String m){ StepPayload p = new StepPayload(); p.message=m; return p; }
    private void emit(StepType t, StepPayload p){ events.add(new Event(t, p)); }

    // Exposed read-only state for UI variables
    public Integer getRootId(){ return root >= 0 ? root : null; }
    public Integer getRootKey(){ Node r = nodes.get(root); return r != null ? r.key : null; }
}
