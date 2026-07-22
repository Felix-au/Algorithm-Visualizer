package com.algorithmvisualizer.visualization;

import com.algorithmvisualizer.algorithm.LinkedListSolverV2;
import com.algorithmvisualizer.algorithm.LinkedListSolverV2.ListType;
import com.algorithmvisualizer.algorithm.LinkedListSolverV2.NodeData;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.VLineTo;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;

import java.util.*;

public class LinkedListVisualizerV2 {
    private final VBox root;
    private final StackPane canvas;
    private final Pane arrowLayer;
    private final FlowPane nodeLayer; // wrapping layout; nodes can become unmanaged during drag

    private final Map<Long, NodeView> views = new LinkedHashMap<>();
    private final Set<Long> highlighted = new HashSet<>();
    private Long foundId = null;

    private ListType type = ListType.SLL;
    private Long headId = null;
    private java.util.List<Long> lastOrder = new java.util.ArrayList<>();
    private final Map<Long, LinkedListSolverV2.NodeData> latest = new LinkedHashMap<>();
    private final Map<Long, Color> linkHighlight = new HashMap<>();
    private final Set<Long> ephemeral = new HashSet<>();
    private final Map<Long, Long> ephemeralNext = new HashMap<>();

    private static class NodeView {
        final StackPane root;
        final Rectangle box;
        final Label valueLbl;
        final Label addrLbl;
        final Label dataHdr;
        final Label nextHdr;
        double pressX, pressY;
        NodeView() {
            box = new Rectangle(110, 44);
            box.setArcWidth(10); box.setArcHeight(10);
            box.setFill(Color.web("#4a90e2")); box.setStroke(Color.web("#2b4c7e")); box.setStrokeWidth(1.5);
            dataHdr = new Label("data"); dataHdr.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
            nextHdr = new Label("next"); nextHdr.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
            valueLbl = new Label(""); valueLbl.setTextFill(Color.WHITE); valueLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            addrLbl = new Label(""); addrLbl.setTextFill(Color.WHITE); addrLbl.setStyle("-fx-font-size: 12px;");
            StackPane valuePane = new StackPane(valueLbl); valuePane.setAlignment(Pos.CENTER);
            valuePane.setMinWidth(65); valuePane.setPrefWidth(65); valuePane.setMaxWidth(65);
            StackPane addrPane = new StackPane(addrLbl); addrPane.setAlignment(Pos.CENTER);
            addrPane.setMinWidth(44); addrPane.setPrefWidth(44); addrPane.setMaxWidth(44);
            javafx.scene.layout.Region divider = new javafx.scene.layout.Region(); divider.setMinWidth(1); divider.setMaxWidth(1); divider.setPrefWidth(1); divider.setStyle("-fx-background-color: white;");
            javafx.scene.layout.HBox valuesRow = new javafx.scene.layout.HBox(valuePane, divider, addrPane);
            valuesRow.setAlignment(Pos.CENTER_LEFT);
            valuesRow.setSpacing(0);
            javafx.scene.layout.HBox headerRow = new javafx.scene.layout.HBox(); headerRow.setSpacing(0); headerRow.setAlignment(Pos.CENTER_LEFT);
            StackPane dataHdrPane = new StackPane(dataHdr); dataHdrPane.setMinWidth(65); dataHdrPane.setPrefWidth(65); dataHdrPane.setMaxWidth(65); dataHdrPane.setAlignment(Pos.CENTER);
            StackPane nextHdrPane = new StackPane(nextHdr); nextHdrPane.setMinWidth(44); nextHdrPane.setPrefWidth(44); nextHdrPane.setMaxWidth(44); nextHdrPane.setAlignment(Pos.CENTER);
            javafx.scene.layout.Region hdrDivider = new javafx.scene.layout.Region(); hdrDivider.setMinWidth(1); hdrDivider.setMaxWidth(1); hdrDivider.setPrefWidth(1); hdrDivider.setStyle("-fx-background-color: transparent;");
            headerRow.getChildren().addAll(dataHdrPane, hdrDivider, nextHdrPane);
            VBox vbox = new VBox(4, headerRow, valuesRow); vbox.setAlignment(Pos.CENTER);
            StackPane content = new StackPane(box, vbox);
            root = new StackPane(content);
            root.setMinSize(110, 44); root.setPrefSize(110, 44);
        }
    }

    public LinkedListVisualizerV2(){
        root = new VBox(8);
        root.setAlignment(Pos.CENTER);
        canvas = new StackPane();
        arrowLayer = new Pane(); arrowLayer.setMouseTransparent(true);
        nodeLayer = new FlowPane(12, 12); nodeLayer.setAlignment(Pos.TOP_LEFT);
        nodeLayer.prefWrapLengthProperty().bind(canvas.widthProperty().subtract(24));
        canvas.getChildren().addAll(arrowLayer, nodeLayer);
        root.getChildren().add(canvas);
        canvas.widthProperty().addListener((o,ov,nv)-> scheduleLayout());
        canvas.heightProperty().addListener((o,ov,nv)-> scheduleLayout());
    }

    public Node getNode(){ return root; }

    public void setData(List<NodeData> ordered, Long headId, Long tailId, ListType type){
        Map<Long, Point2D> prevPos = capturePositions();
        this.type = type;
        this.headId = headId;
        latest.clear();
        lastOrder.clear();
        // create missing views
        Set<Long> present = new HashSet<>(views.keySet());
        for (int i=0;i<ordered.size();i++){
            NodeData nd = ordered.get(i);
            lastOrder.add(nd.id);
            latest.put(nd.id, nd);
            NodeView v = views.get(nd.id);
            if (v == null){ v = new NodeView(); views.put(nd.id, v); nodeLayer.getChildren().add(v.root); enableDrag(v.root); }
            v.valueLbl.setText(String.valueOf(nd.value));
            boolean isTail = (i == ordered.size()-1);
            String addr;
            if (type == ListType.CSLL || type == ListType.CDLL) {
                addr = (nd.nextId == null) ? String.valueOf(headId) : String.valueOf(nd.nextId);
            } else {
                addr = isTail ? "null" : (nd.nextId == null ? "null" : String.valueOf(nd.nextId));
            }
            v.addrLbl.setText(addr);
            if (!v.root.isManaged()) {
                // When unmanaged from previous drag, keep its translate, else align in HBox
            }
            ephemeral.remove(nd.id);
            ephemeralNext.remove(nd.id);
            present.remove(nd.id);
        }
        // remove extra (but keep ephemeral nodes)
        for (Long id : present){ if (!ephemeral.contains(id)) { NodeView v = views.remove(id); if (v != null) nodeLayer.getChildren().remove(v.root); } }
        // reorder children in FlowPane to follow the logical order exactly (so new head is placed first)
        ObservableList<Node> children = nodeLayer.getChildren();
        java.util.List<Node> newOrder = new java.util.ArrayList<>();
        for (NodeData nd : ordered){ NodeView v = views.get(nd.id); if (v!=null) newOrder.add(v.root); }
        for (Node n : new java.util.ArrayList<>(children)) { if (!newOrder.contains(n)) newOrder.add(n); }
        children.setAll(newOrder);
        nodeLayer.requestLayout();
        updateHighlights();
        scheduleLayout();
        Platform.runLater(() -> animateToNewPositions(prevPos));
    }

    public void ensureEphemeralNode(Long id, int value){
        if (id == null) return;
        if (!views.containsKey(id)){
            NodeView v = new NodeView();
            views.put(id, v);
            nodeLayer.getChildren().add(v.root);
            enableDrag(v.root);
        }
        NodeView nv = views.get(id);
        nv.valueLbl.setText(String.valueOf(value));
        nv.addrLbl.setText("…");
        ephemeral.add(id);
        pulse(id);
        scheduleLayout();
    }

    public void removeNodeView(Long id){
        if (id == null) return;
        NodeView v = views.remove(id);
        ephemeral.remove(id);
        if (v != null) nodeLayer.getChildren().remove(v.root);
        scheduleLayout();
    }

    // Ensure an entire chain starting at startId remains visible (using last known 'latest' pointers)
    public void ensureEphemeralChainFrom(Long startId){
        if (startId == null) return;
        Set<Long> seen = new HashSet<>();
        Long p = startId;
        Long last = null;
        while (p != null && !seen.contains(p)){
            LinkedListSolverV2.NodeData nd = latest.get(p);
            if (nd == null) break;
            ensureEphemeralNode(nd.id, nd.value);
            if (last != null) ephemeralNext.put(last, nd.id);
            seen.add(p);
            last = nd.id;
            p = nd.nextId;
            // stop if circular and back to head
            if (p != null && Objects.equals(p, headId) && (type == ListType.CSLL || type == ListType.CDLL)) break;
        }
        if (last != null) ephemeralNext.put(last, null);
    }

    // Ensure chain starting from the previous node's next (old layout) stays visible
    public void ensureEphemeralChainFromNextOf(Long prevId){
        if (prevId == null) return;
        LinkedListSolverV2.NodeData prev = latest.get(prevId);
        if (prev == null) return;
        ensureEphemeralChainFrom(prev.nextId);
    }

    private void updateHighlights(){
        for (Map.Entry<Long, NodeView> e : views.entrySet()){
            Long id = e.getKey(); NodeView v = e.getValue();
            if (Objects.equals(id, foundId)) v.box.setFill(Color.web("#27ae60"));
            else if (highlighted.contains(id)) v.box.setFill(Color.web("#f39c12"));
            else v.box.setFill(Color.web("#4a90e2"));
        }
    }

    public void highlightCompare(Long nodeId){ if (nodeId != null){ highlighted.clear(); highlighted.add(nodeId); updateHighlights(); } }
    public void highlightFound(Long nodeId){ foundId = nodeId; highlighted.remove(nodeId); updateHighlights(); pulse(nodeId); }
    public void clearHighlight(){ highlighted.clear(); foundId = null; linkHighlight.clear(); updateHighlights(); scheduleLayout(); }
    public void flashAlert(){ for (NodeView v: views.values()){ v.box.setStroke(Color.web("#e74c3c")); } Platform.runLater(() -> { for (NodeView v: views.values()){ v.box.setStroke(Color.web("#2b4c7e")); } }); }

    private void pulse(Long nodeId){ NodeView v = views.get(nodeId); if (v == null) return; FadeTransition ft = new FadeTransition(Duration.millis(200), v.root); ft.setFromValue(0.6); ft.setToValue(1.0); ft.setCycleCount(2); ft.setAutoReverse(true); ft.play(); }

    private void scheduleLayout(){ Platform.runLater(this::redrawArrows); }


    private void redrawArrows(){
        arrowLayer.getChildren().clear();
        // compute bounding box of nodes in canvas coordinates
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (NodeView v: views.values()){
            Bounds s = v.root.localToScene(v.root.getBoundsInLocal());
            Bounds b = canvas.sceneToLocal(s);
            minX = Math.min(minX, b.getMinX());
            minY = Math.min(minY, b.getMinY());
            maxX = Math.max(maxX, b.getMaxX());
            maxY = Math.max(maxY, b.getMaxY());
        }
        if (!Double.isFinite(minX)) { minX = 0; minY = 0; maxX = canvas.getWidth(); maxY = canvas.getHeight(); }

        // detect row bands (minY, maxY, midY)
        java.util.List<double[]> rows = new java.util.ArrayList<>();
        for (NodeView v: views.values()){
            Bounds s = v.root.localToScene(v.root.getBoundsInLocal());
            Bounds b = canvas.sceneToLocal(s);
            double cy = (b.getMinY()+b.getMaxY())/2.0;
            boolean placed=false;
            for (double[] rb : rows){
                if (Math.abs(cy - rb[2]) < 12){
                    rb[0] = Math.min(rb[0], b.getMinY());
                    rb[1] = Math.max(rb[1], b.getMaxY());
                    rb[2] = (rb[0]+rb[1])/2.0;
                    placed=true; break;
                }
            }
            if (!placed){ rows.add(new double[]{b.getMinY(), b.getMaxY(), cy}); }
        }
        rows.sort(java.util.Comparator.comparingDouble(a -> a[2]));

        // Draw arrows using next pointers from latest (reachable) or ephemeralNext (detached chains)
        Set<Long> fromIds = new LinkedHashSet<>(views.keySet());
        for (Long fromId : fromIds){
            Long nxt = null;
            LinkedListSolverV2.NodeData nd = latest.get(fromId);
            if (nd != null) nxt = nd.nextId;
            if (nxt == null) nxt = ephemeralNext.get(fromId);
            if (nxt == null) continue;
            NodeView a = views.get(fromId);
            NodeView b = views.get(nxt);
            if (a==null || b==null) continue;
            Bounds aS = a.root.localToScene(a.root.getBoundsInLocal());
            Bounds bS = b.root.localToScene(b.root.getBoundsInLocal());
            Bounds aL = canvas.sceneToLocal(aS);
            Bounds bL = canvas.sceneToLocal(bS);
            double sx = aL.getMaxX()-2; double sy = (aL.getMinY()+aL.getMaxY())/2.0;
            double ex = bL.getMinX()+2; double ey = (bL.getMinY()+bL.getMaxY())/2.0;
            Color color = linkHighlight.getOrDefault(fromId, Color.web("#2b4c7e"));
            // find source and target row indices
            int si=-1, ti=-1;
            for (int r=0;r<rows.size();r++){
                double[] rb = rows.get(r);
                if (sy >= rb[0]-2 && sy <= rb[1]+2) si=r;
                if (ey >= rb[0]-2 && ey <= rb[1]+2) ti=r;
            }
            if (si<0){ double best=Double.POSITIVE_INFINITY; for (int r=0;r<rows.size();r++){ double d=Math.abs(sy-rows.get(r)[2]); if (d<best){ best=d; si=r; } } }
            if (ti<0){ double best=Double.POSITIVE_INFINITY; for (int r=0;r<rows.size();r++){ double d=Math.abs(ey-rows.get(r)[2]); if (d<best){ best=d; ti=r; } } }
            double seamY;
            if (ti > si) {
                double srcBottom = rows.get(si)[1];
                double nextTop = (si+1 < rows.size()) ? rows.get(si+1)[0] : srcBottom + 16.0;
                double gap = Math.max(4.0, nextTop - srcBottom);
                double offset = Math.min(8.0, gap * 0.5);
                seamY = srcBottom + offset;
            } else if (ti < si) {
                double srcTop = rows.get(si)[0];
                double prevBottom = (si-1 >= 0) ? rows.get(si-1)[1] : srcTop - 16.0;
                double gap = Math.max(4.0, srcTop - prevBottom);
                double offset = Math.min(8.0, gap * 0.5);
                seamY = srcTop - offset;
            } else {
                seamY = sy;
            }
            addOrthogonalArrow(sx, sy, ex, ey, color, minX, minY, maxX, maxY, seamY);
        }
    }

    private List<Long> getOrder(){
        if (!lastOrder.isEmpty()) return new ArrayList<>(lastOrder);
        List<Long> ids = new ArrayList<>();
        for (Node child : nodeLayer.getChildren()){
            for (Map.Entry<Long,NodeView> e: views.entrySet()){ if (e.getValue().root==child) { ids.add(e.getKey()); break; } }
        }
        return ids;
    }

    private void addOrthogonalArrow(double sx, double sy, double ex, double ey, Color color, double minX, double minY, double maxX, double maxY, double seamY){
        Path path = new Path();
        path.getElements().add(new MoveTo(sx, sy));
        double margin = 12.0;
        double rightPillar = maxX + margin;
        double leftPillar = minX - margin;

        if (Math.abs(ey - sy) < 6) {
            // same row: straight
            path.getElements().add(new HLineTo(ex));
        } else {
            // go to right pillar, then along the seam just below/above the source row, then into target
            path.getElements().add(new HLineTo(rightPillar));
            path.getElements().add(new VLineTo(seamY));
            path.getElements().add(new HLineTo(ex));
            path.getElements().add(new VLineTo(ey));
        }
        path.setStroke(color);
        path.setStrokeWidth(3.0);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setFill(Color.TRANSPARENT);
        Polygon head = arrowHead(ex, ey, ex-1, ey, color);
        arrowLayer.getChildren().addAll(path, head);
    }
    private Polygon arrowHead(double ex, double ey, double sx, double sy, Color color){
        double dx = ex - sx, dy = ey - sy; double len=Math.hypot(dx,dy); if (len==0) len=1; dx/=len; dy/=len; double size=8.0; double ox=ex-dx*size; double oy=ey-dy*size; double px=-dy, py=dx; Polygon p = new Polygon(ex,ey, ox+px*(size/2.0), oy+py*(size/2.0), ox-px*(size/2.0), oy-py*(size/2.0)); p.setFill(color); return p;
    }

    private void enableDrag(StackPane n){
        n.setOnMousePressed(e -> { n.setManaged(false); NodeView nv = nodeFor(n); if (nv!=null){ nv.pressX = e.getSceneX() - n.getTranslateX(); nv.pressY = e.getSceneY() - n.getTranslateY(); } e.consume(); });
        n.setOnMouseDragged(e -> { NodeView nv = nodeFor(n); if (nv!=null){ n.setTranslateX(e.getSceneX()-nv.pressX); n.setTranslateY(e.getSceneY()-nv.pressY); scheduleLayout(); } e.consume(); });
        n.setOnMouseReleased(e -> { scheduleLayout(); e.consume(); });
    }

    private NodeView nodeFor(Node n){ for (NodeView v: views.values()){ if (v.root==n) return v; } return null; }

    // Link highlighting controls
    public void highlightCompareLinkFrom(Long fromId){ if (fromId==null) return; linkHighlight.clear(); Long next = nextOf(fromId); if (next!=null) linkHighlight.put(fromId, Color.web("#f39c12")); scheduleLayout(); }
    public void highlightFoundLinkFrom(Long fromId){ if (fromId==null) return; linkHighlight.clear(); Long next = nextOf(fromId); if (next!=null) linkHighlight.put(fromId, Color.web("#27ae60")); scheduleLayout(); }
    public void highlightCompareLinkTo(Long toId){ if (toId==null) return; linkHighlight.clear(); Long from = fromForNext(toId); if (from!=null) linkHighlight.put(from, Color.web("#f39c12")); scheduleLayout(); }
    public void highlightFoundLinkTo(Long toId){ if (toId==null) return; linkHighlight.clear(); Long from = fromForNext(toId); if (from!=null) linkHighlight.put(from, Color.web("#27ae60")); scheduleLayout(); }
    public void clearLinkHighlights(){ linkHighlight.clear(); scheduleLayout(); }

    private Long nextOf(Long id){ LinkedListSolverV2.NodeData nd = latest.get(id); return nd==null? null : nd.nextId; }
    private Long fromForNext(Long toId){ for (Map.Entry<Long, LinkedListSolverV2.NodeData> e : latest.entrySet()){ if (Objects.equals(e.getValue().nextId, toId)) return e.getKey(); } return null; }
    private Map<Long, Point2D> capturePositions(){ Map<Long, Point2D> map = new HashMap<>(); for (Map.Entry<Long, NodeView> e : views.entrySet()){ NodeView v = e.getValue(); Bounds s = v.root.localToScene(v.root.getBoundsInLocal()); Bounds b = canvas.sceneToLocal(s); double cx = (b.getMinX()+b.getMaxX())/2.0; double cy = (b.getMinY()+b.getMaxY())/2.0; map.put(e.getKey(), new Point2D(cx, cy)); } return map; }
    private void animateToNewPositions(Map<Long, Point2D> prev){ if (prev==null || prev.isEmpty()) return; nodeLayer.applyCss(); nodeLayer.layout(); boolean any=false; for (Map.Entry<Long, NodeView> e : views.entrySet()){ Long id = e.getKey(); NodeView v = e.getValue(); if (!v.root.isManaged()) continue; Point2D old = prev.get(id); if (old == null) continue; Bounds s = v.root.localToScene(v.root.getBoundsInLocal()); Bounds b = canvas.sceneToLocal(s); double nx = (b.getMinX()+b.getMaxX())/2.0; double ny = (b.getMinY()+b.getMaxY())/2.0; double dx = old.getX() - nx; double dy = old.getY() - ny; if (Math.abs(dx) < 0.5 && Math.abs(dy) < 0.5) continue; v.root.setTranslateX(v.root.getTranslateX() + dx); v.root.setTranslateY(v.root.getTranslateY() + dy); TranslateTransition tt = new TranslateTransition(Duration.millis(220), v.root); tt.setToX(0); tt.setToY(0); tt.setInterpolator(Interpolator.EASE_BOTH); tt.setOnFinished(ev -> scheduleLayout()); tt.play(); any=true; } if (any) scheduleLayout(); }
}
