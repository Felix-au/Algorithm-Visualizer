package com.algorithmvisualizer.visualization;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.*;

public class TowerOfHanoiVisualizer {
    private final Pane root = new Pane();
    private final Group pegLayer = new Group();
    private final Group diskLayer = new Group();
    private Rectangle diskClip;

    private final List<DiskView> disks = new ArrayList<>();
    private final Map<Integer, DiskView> diskById = new HashMap<>();
    private final int[] pegOfDisk = new int[64];

    private int n = 0;
    private double speed = 1.0; // 1.0 = normal, higher is faster

    // layout params
    private double baseY;
    private double liftY;
    private double[] pegX = new double[3];
    private double pegHeight = 220;
    private double basePadding = 84;
    private double baseShelfHeight = 8.0;
    private double diskThickness = 16;

    private Line baseLine;
    private Rectangle baseShelf;
    private Rectangle[] pegs = new Rectangle[3];
    private final java.util.List<Animation> active = new java.util.ArrayList<>();
    private int movingDiskId = -1;

    public TowerOfHanoiVisualizer(){
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f8fafc, #eef2f7);");
        root.getChildren().addAll(pegLayer, diskLayer);
        buildPegs();
        // Clip any part of disks that would render below the base shelf
        diskClip = new Rectangle(0,0,0,0);
        diskLayer.setClip(diskClip);
        root.widthProperty().addListener((o,ov,nv)-> layoutAll());
        root.heightProperty().addListener((o,ov,nv)-> layoutAll());
        Platform.runLater(this::layoutAll);
    }

    public Node getNode(){ return root; }

    public void setSpeed(double s){ this.speed = Math.max(0.25, Math.min(4.0, s)); }

    public void reset(){
        cancelAnimations();
        disks.clear(); diskById.clear();
        diskLayer.getChildren().clear();
        Arrays.fill(pegOfDisk, -1);
        n = 0;
        layoutAll();
    }

    public void prepare(int n){
        cancelAnimations();
        reset();
        this.n = n;
        // create disks: id 1..n (1 small, n largest)
        for (int i = n; i >= 1; i--) {
            DiskView d = new DiskView(i, n);
            diskById.put(i, d); disks.add(d);
            diskLayer.getChildren().add(d.root);
            pegOfDisk[i] = 0; // all on A
        }
        layoutAll();
    }

    public long onMovePrep(int disk){
        cancelAnimations();
        movingDiskId = disk;
        DiskView d = diskById.get(disk);
        if (d != null) d.highlight(true);
        return 120;
    }

    public long onLift(int disk){
        DiskView d = diskById.get(disk); if (d==null) return 0;
        // animate to liftY (top of pane, well above pegs)
        double targetY = liftY;
        return animateToY(d, targetY, 220);
    }

    public long onSlide(int disk, int toPeg){
        DiskView d = diskById.get(disk); if (d==null) return 0;
        double centerX = pegX[toPeg];
        // place center align using rect width
        double targetX = centerX - d.rect.getWidth()/2.0;
        return animateToX(d, targetX, horizontalDuration(d.root.getLayoutX(), targetX));
    }

    public long onDrop(int disk, int toPeg){
        DiskView d = diskById.get(disk); if (d==null) return 0;
        int targetHeight = stackHeight(toPeg); // before commit
        // Drop to exact stack position aligned to the top of the base shelf
        double y = floorTopY() - (targetHeight + 1) * diskThickness;
        long ms = animateToY(d, y, 220, () -> {
            // Re-snap based on CURRENT stack height (commit may have happened)
            int hNow = stackHeight(toPeg);
            double yNow = floorTopY() - hNow * diskThickness;
            // Clamp above shelf top
            double maxY = floorTopY() - diskThickness;
            d.root.setLayoutY(Math.min(yNow, maxY));
            d.root.setTranslateY(0);
        });
        return ms;
    }

    public void onCommit(int disk, int fromPeg, int toPeg){
        pegOfDisk[disk] = toPeg;
        DiskView d = diskById.get(disk); if (d!=null) d.highlight(false);
        // Snap to exact stack position after commit
        int h = stackHeight(toPeg);
        double centerX = pegX[toPeg];
        if (d!=null){
            // Exact position aligned to the top of the base shelf
            d.root.setLayoutX(centerX - d.rect.getWidth()/2.0);
            d.root.setLayoutY(floorTopY() - h*diskThickness);
            d.root.setTranslateX(0);
            d.root.setTranslateY(0);
        }
        if (movingDiskId == disk) movingDiskId = -1;
    }

    private int stackHeight(int peg){
        int h = 0;
        for (int i=1;i<=n;i++) if (pegOfDisk[i]==peg) h++;
        return h;
    }

    private long animateToY(DiskView d, double y, long baseMs){
        final double targetAbsY = y; // capture exact intended Y
        TranslateTransition tt = new TranslateTransition(Duration.millis(scale(baseMs)), d.root);
        tt.setToY(targetAbsY - d.root.getLayoutY());
        tt.setInterpolator(Interpolator.EASE_BOTH);
        d.root.toFront();
        active.add(tt);
        tt.setOnFinished(e -> {
            active.remove(tt);
            // Snap to absolute to avoid drift across resizes
            d.root.setLayoutY(d.root.getLayoutY() + d.root.getTranslateY());
            d.root.setTranslateY(0);
            // Snap to exact requested target, but never below shelf top
            double minY = floorTopY() - diskThickness;
            double finalY = Math.min(targetAbsY, minY);
            d.root.setLayoutY(finalY);
        });
        tt.play();
        return (long) (scale(baseMs));
    }

    private long animateToY(DiskView d, double y, long baseMs, Runnable onFinished){
        final double targetAbsY = y;
        TranslateTransition tt = new TranslateTransition(Duration.millis(scale(baseMs)), d.root);
        tt.setToY(targetAbsY - d.root.getLayoutY());
        tt.setInterpolator(Interpolator.EASE_BOTH);
        d.root.toFront();
        active.add(tt);
        tt.setOnFinished(e -> {
            active.remove(tt);
            d.root.setLayoutY(d.root.getLayoutY() + d.root.getTranslateY());
            d.root.setTranslateY(0);
            double minY = floorTopY() - diskThickness;
            double finalY = Math.min(targetAbsY, minY);
            d.root.setLayoutY(finalY);
            if (onFinished != null) onFinished.run();
        });
        tt.play();
        return (long) (scale(baseMs));
    }

    private long animateToX(DiskView d, double x, long baseMs){
        TranslateTransition tt = new TranslateTransition(Duration.millis(scale(baseMs)), d.root);
        tt.setToX(x - d.root.getLayoutX());
        tt.setInterpolator(Interpolator.EASE_BOTH);
        d.root.toFront();
        active.add(tt);
        tt.setOnFinished(e -> {
            active.remove(tt);
            // Snap to absolute to avoid drift across resizes
            d.root.setLayoutX(d.root.getLayoutX() + d.root.getTranslateX());
            d.root.setTranslateX(0);
        });
        tt.play();
        return (long) (scale(baseMs));
    }

    private double scale(double ms){ return ms / Math.max(0.25, speed); }

    private long horizontalDuration(double fromX, double toX){
        double dx = Math.abs(toX - fromX);
        double perPx = 0.6; // ms per pixel at 1x speed
        long ms = (long)Math.max(220, Math.min(900, dx * perPx));
        return ms;
    }

    private void buildPegs(){
        baseShelf = new Rectangle();
        baseShelf.setFill(Color.web("#e2e8f0"));
        baseShelf.setArcWidth(4); baseShelf.setArcHeight(4);
        baseLine = new Line();
        baseLine.setStroke(Color.web("#cbd5e1"));
        baseLine.setStrokeWidth(3);
        pegLayer.getChildren().addAll(baseShelf, baseLine);
        for (int i=0;i<3;i++){
            Rectangle r = new Rectangle(12, pegHeight);
            r.setArcWidth(8); r.setArcHeight(8);
            r.setFill(Color.web("#94a3b8"));
            r.setStroke(Color.web("#64748b"));
            pegs[i] = r;
            pegLayer.getChildren().add(r);
        }
    }

    private void layoutAll(){
        double w = Math.max(600, root.getWidth());
        double h = Math.max(420, root.getHeight());
        baseY = h - basePadding; // leave visible margin at bottom
        liftY = basePadding + 40; // top lift ceiling
        // adapt sizes to fit all disks cleanly within pane height
        double availVertical = Math.max(160, baseY - (liftY + 20));
        if (n > 0) {
            diskThickness = Math.max(12, Math.min(18, availVertical / (n + 2.0)));
            pegHeight = Math.max(180, Math.min(280, availVertical * 0.92));
        }
        // Place pegs within a padded region to leave extra room at right
        double leftPad = 56.0, rightPad = 96.0;
        double regionX = leftPad, regionW = Math.max(300, w - leftPad - rightPad);
        // Centers at 1/6, 1/2, 5/6 of region for symmetry
        pegX[0] = regionX + regionW/6.0;
        pegX[1] = regionX + regionW/2.0;
        pegX[2] = regionX + 5.0*regionW/6.0;

        baseShelf.setX(leftPad);
        baseShelf.setY(baseY - baseShelfHeight);
        baseShelf.setWidth(w - leftPad - rightPad);
        baseShelf.setHeight(baseShelfHeight);
        baseLine.setStartX(leftPad); baseLine.setEndX(w-rightPad); baseLine.setStartY(baseY); baseLine.setEndY(baseY);
        // Update clip to prevent drawing below shelf top
        diskClip.setX(0);
        diskClip.setY(0);
        diskClip.setWidth(w);
        diskClip.setHeight(baseY - baseShelfHeight);
        for (int i=0;i<3;i++){
            Rectangle r = pegs[i];
            r.setHeight(pegHeight);
            r.setWidth(12);
            r.setLayoutX(pegX[i]-6);
            r.setLayoutY(baseY-pegHeight);
        }
        // Compute disk widths relative to peg spacing and normalize heights
        double pegGap = pegX[1] - pegX[0];
        double maxDiskWidth = Math.max(120, Math.min(260, pegGap * 0.78));
        double minDiskWidth = Math.max(60, Math.min(maxDiskWidth * 0.45, maxDiskWidth - 80));
        // Normalize disk heights to current diskThickness
        for (DiskView dv : disks){
            double t = (dv.id - 1.0) / Math.max(1, n - 1.0);
            double targetW = minDiskWidth + (maxDiskWidth - minDiskWidth) * t;
            dv.rect.setWidth(targetW);
            dv.root.setMinWidth(targetW);
            dv.root.setPrefWidth(targetW);
            dv.root.setMaxWidth(targetW);
            dv.rect.setHeight(diskThickness);
            dv.root.setMinHeight(diskThickness);
            dv.root.setPrefHeight(diskThickness);
            dv.root.setMaxHeight(diskThickness);
        }
        // If a disk is moving during a resize, ensure it stays above the base line
        clampMovingDiskAboveBase();
        layoutDisks();
    }

    public void cancelAnimations(){
        for (Animation a : new ArrayList<>(active)){
            try { a.stop(); } catch(Exception ignore) {}
        }
        active.clear();
    }

    private void layoutDisks(){
        if (n<=0) return;
        for (int size = n; size >= 1; size--) {
            DiskView d = diskById.get(size);
            if (d == null) continue;
            if (size == movingDiskId) continue; // do not reset position of moving disk during animations/resizes
            int peg = pegOfDisk[size] < 0 ? 0 : pegOfDisk[size];
            int idx = indexOnPeg(size, peg);
            double centerX = pegX[peg];
            // Exact stack position aligned to the top of the base shelf
            double y = floorTopY() - (idx+1)*diskThickness;
            double x = centerX - d.rect.getWidth()/2.0;
            d.root.setLayoutX(x); d.root.setLayoutY(y);
            d.root.setTranslateX(0); d.root.setTranslateY(0);
        }
    }

    private double floorTopY(){
        return baseY - baseShelfHeight;
    }

    private void clampMovingDiskAboveBase(){
        if (movingDiskId <= 0) return;
        DiskView d = diskById.get(movingDiskId);
        if (d == null) return;
        double effectiveY = d.root.getLayoutY() + d.root.getTranslateY();
        double maxY = floorTopY() - diskThickness; // top-left should never go below base shelf top
        if (effectiveY > maxY){
            double dy = maxY - effectiveY;
            d.root.setTranslateY(d.root.getTranslateY() + dy);
        }
    }

    public boolean isAnimating(){ return !active.isEmpty(); }

    private int indexOnPeg(int size, int peg){
        int below = 0;
        for (int s = n; s >= 1; s--) {
            if (s == size) break;
            if (pegOfDisk[s] == peg) below++;
        }
        return below;
    }

    private static Color colorFor(int id, int n){
        // smooth blue-purple palette
        double t = (id-1.0)/Math.max(1,n-1.0);
        return Color.hsb(230 - 150*t, 0.65, 0.95);
    }

    private class DiskView {
        final int id;
        final StackPane root;
        final Rectangle rect;
        final Label label;
        DiskView(int id, int n){
            this.id = id;
            double maxWidth = 200;
            double minWidth = 60;
            double w = minWidth + (maxWidth-minWidth) * (id-1.0)/Math.max(1, n-1.0);
            rect = new Rectangle(w, diskThickness);
            rect.setFill(colorFor(id, n));
            rect.setStroke(Color.web("#1f2937"));
            rect.setArcWidth(12); rect.setArcHeight(12);
            label = new Label(String.valueOf(id));
            label.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-font-size: 12px;");
            root = new StackPane(rect, label);
            root.setMinSize(w, diskThickness);
            root.setPrefSize(w, diskThickness);
            root.setMaxSize(w, diskThickness);
        }
        void highlight(boolean on){ rect.setStroke(on? Color.web("#f59e0b"): Color.web("#1f2937")); rect.setStrokeWidth(on? 3:1); }
    }
}
