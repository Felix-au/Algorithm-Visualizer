package com.algorithmvisualizer.visualization;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class QueueVisualizer {
    private final HBox root;
    private final VBox leftColumn; // main + aux queue
    private final VBox mainContainer;
    private final VBox auxQueueContainer;
    private final VBox auxStackContainer;

    private final Label mainLabel;
    private final Label auxQueueLabel;
    private final Label auxStackLabel;

    private int capacity = 10;
    private int size = 0;
    private int[] data = new int[0];

    private int auxQSize = 0;
    private int[] auxQData = new int[0];

    private int auxTop = -1;
    private int[] auxSData = new int[0];

    private final List<Slot> mainSlots = new ArrayList<>();
    private final List<Slot> auxQSlots = new ArrayList<>();
    private final List<Slot> auxSSlots = new ArrayList<>();

    private HBox mainRow;
    private HBox mainMarkerRow;
    private final List<Label> mainMarkers = new ArrayList<>();

    private HBox auxQRow;

    private boolean mainAnimating = false;
    private final java.util.List<Animation> mainActiveAnims = new java.util.ArrayList<>();
    private boolean auxQAnimating = false;
    private final java.util.List<Animation> auxQActiveAnims = new java.util.ArrayList<>();
    private int mainHighlightedVIdx = -1;
    private int mainFoundVIdx = -1;

    private static class Slot {
        final StackPane pane; final Rectangle rect; final Label label;
        Slot(StackPane p, Rectangle r, Label l) { pane=p; rect=r; label=l; }
    }

    public QueueVisualizer() {
        root = new HBox(30);
        root.setAlignment(Pos.CENTER);

        leftColumn = new VBox(12);
        leftColumn.setAlignment(Pos.CENTER);

        mainContainer = new VBox(6);
        mainContainer.setAlignment(Pos.CENTER);
        auxQueueContainer = new VBox(6);
        auxQueueContainer.setAlignment(Pos.CENTER);

        auxStackContainer = new VBox(6);
        auxStackContainer.setAlignment(Pos.CENTER);

        mainLabel = title("Queue");
        auxQueueLabel = title("Aux Queue");
        auxStackLabel = title("Aux Stack");

        mainContainer.getChildren().add(mainLabel);
        auxQueueContainer.getChildren().add(auxQueueLabel);
        auxStackContainer.getChildren().add(auxStackLabel);

        leftColumn.getChildren().addAll(mainContainer, auxQueueContainer);

        Region spacerLeft = new Region();
        Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        root.getChildren().addAll(spacerLeft, leftColumn, auxStackContainer, spacerRight);

        setCapacity(capacity);

        setAuxQueueVisible(false);
        setAuxStackVisible(false);
    }

    private Label title(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("System", 14));
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private Slot createSlot() {
        Rectangle rect = new Rectangle(60, 28, Color.TRANSPARENT);
        rect.setStroke(Color.DARKGRAY);
        rect.getStrokeDashArray().setAll(6.0, 6.0);
        Label lbl = new Label("");
        StackPane pane = new StackPane(rect, lbl);
        pane.setPrefSize(70, 32);
        return new Slot(pane, rect, lbl);
    }

    private void fillSlot(Slot s, String text, Color color) {
        s.rect.setFill(color); s.rect.setStroke(Color.BLACK); s.rect.getStrokeDashArray().clear();
        s.label.setText(text); s.label.setTextFill(Color.WHITE);
    }

    private void emptySlot(Slot s) {
        s.rect.setFill(Color.TRANSPARENT); s.rect.setStroke(Color.DARKGRAY); s.rect.getStrokeDashArray().setAll(6.0,6.0);
        s.label.setText("");
    }

    public Node getNode() { return root; }

    public void setCapacity(int cap) {
        this.capacity = Math.max(1, cap);
        this.data = new int[this.capacity]; this.size = 0;
        this.auxQData = new int[this.capacity]; this.auxQSize = 0;
        this.auxSData = new int[this.capacity]; this.auxTop = -1;
        rebuild();
    }

    private void rebuild() {
        mainContainer.getChildren().remove(1, mainContainer.getChildren().size());
        auxQueueContainer.getChildren().remove(1, auxQueueContainer.getChildren().size());
        auxStackContainer.getChildren().remove(1, auxStackContainer.getChildren().size());
        mainSlots.clear(); auxQSlots.clear(); auxSSlots.clear(); mainMarkers.clear();

        mainRow = new HBox(6);
        mainMarkerRow = new HBox(6);
        for (int i=0;i<capacity;i++) {
            Slot s = createSlot(); mainSlots.add(s); mainRow.getChildren().add(s.pane);
            Label m = new Label(""); m.setMinWidth(70); m.setPrefWidth(70); m.setAlignment(Pos.CENTER);
            m.setStyle("-fx-text-fill: #2b4c7e; -fx-font-size: 10px; -fx-font-weight: bold;");
            mainMarkers.add(m); mainMarkerRow.getChildren().add(m);
        }
        mainContainer.getChildren().addAll(mainRow, mainMarkerRow);

        auxQRow = new HBox(6);
        for (int i=0;i<capacity;i++) { Slot s = createSlot(); auxQSlots.add(s); auxQRow.getChildren().add(s.pane);}        
        auxQueueContainer.getChildren().add(auxQRow);

        VBox auxSCol = new VBox(4);
        for (int i=capacity-1;i>=0;i--) { Slot s = createSlot(); auxSSlots.add(s); auxSCol.getChildren().add(s.pane);}        
        auxStackContainer.getChildren().add(auxSCol);

        refreshAll();
    }

    private void refreshAll() {
        refreshMainFromData();
        updateMainMarkers();
        refreshAuxQueueFromData();
        for (int i=0;i<capacity;i++) {
            int visualIdx=capacity-1-i; Slot s = auxSSlots.get(visualIdx);
            if (i <= auxTop) fillSlot(s, String.valueOf(auxSData[i]), Color.web("#8e44ad")); else emptySlot(s);
        }
    }

    private void updateMainMarkers() {
        for (Label m: mainMarkers) m.setText("");
        if (size>0) {
            mainMarkers.get(0).setText("Tail");
            mainMarkers.get(Math.min(size-1, mainMarkers.size()-1)).setText("Front");
        }
    }

    public void resetData(int[] values, int size) {
        if (values == null) values = new int[0];
        this.size = Math.max(0, Math.min(size, capacity));
        for (int i=0;i<this.size;i++) this.data[i]=values[i];
        // clear any animations and render from data
        clearMainAnimations();
        refreshAll();
    }

    public void resetAuxQueueData(int[] values, int size) {
        if (values == null) values = new int[0];
        this.auxQSize = Math.max(0, Math.min(size, capacity));
        for (int i=0;i<this.auxQSize;i++) this.auxQData[i]=values[i];
        refreshAll();
    }

    public void resetAuxStackData(int[] values, int top) {
        if (values == null) values = new int[0];
        this.auxTop = top;
        for (int i=0;i<=top && i<capacity;i++) this.auxSData[i]=values[i];
        refreshAll();
    }

    public void setAuxQueueVisible(boolean v) { auxQueueContainer.setVisible(v); auxQueueContainer.setManaged(v); }
    public void setAuxStackVisible(boolean v) { auxStackContainer.setVisible(v); auxStackContainer.setManaged(v); }

    // Main queue animations (newest at left visually). Underlying data: data[0..size-1] front..tail
    public void mainEnq(int idx, int value) {
        // Update internal buffer to match solver (append at tail)
        if (size < capacity) {
            data[size] = value;
            size++;
        }
        if (mainAnimating) clearMainAnimations();
        int oldSize = size - 1;
        if (oldSize > 0) {
            double dx = mainDx();
            ParallelTransition pt = new ParallelTransition();
            for (int i = 0; i < oldSize; i++) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(180), mainSlots.get(i).pane);
                tt.setByX(dx);
                tt.setInterpolator(Interpolator.EASE_BOTH);
                pt.getChildren().add(tt);
                mainActiveAnims.add(tt);
            }
            mainAnimating = true;
            pt.setOnFinished(e -> {
                for (int i = 0; i < oldSize; i++) mainSlots.get(i).pane.setTranslateX(0);
                refreshMainFromData();
                updateMainMarkers();
                mainActiveAnims.clear();
                mainAnimating = false;
            });
            pt.play();
        } else {
            refreshMainFromData();
            if (!mainSlots.isEmpty()) {
                FadeTransition ft = new FadeTransition(Duration.millis(150), mainSlots.get(0).pane);
                ft.setFromValue(0.0); ft.setToValue(1.0); ft.play();
            }
            updateMainMarkers();
        }
    }

    public void mainDeq() {
        if (size<=0) return;
        if (mainAnimating) clearMainAnimations();
        int oldSize = size;
        // remove front from internal buffer
        for (int i = 1; i < oldSize; i++) data[i-1] = data[i];
        data[oldSize-1] = 0; size--;
        double dx = mainDx();
        ParallelTransition pt = new ParallelTransition();
        // fade + slide right the rightmost visual cell
        if (oldSize - 1 >= 0) {
            TranslateTransition ttLast = new TranslateTransition(Duration.millis(160), mainSlots.get(oldSize - 1).pane);
            ttLast.setByX(dx); ttLast.setInterpolator(Interpolator.EASE_BOTH);
            FadeTransition ftLast = new FadeTransition(Duration.millis(160), mainSlots.get(oldSize - 1).pane);
            ftLast.setFromValue(1.0); ftLast.setToValue(0.0);
            pt.getChildren().addAll(ttLast, ftLast);
            mainActiveAnims.add(ttLast); mainActiveAnims.add(ftLast);
        }
        // shift-right remaining visuals
        for (int i = 0; i < size; i++) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(180), mainSlots.get(i).pane);
            tt.setByX(dx); tt.setInterpolator(Interpolator.EASE_BOTH);
            pt.getChildren().add(tt);
            mainActiveAnims.add(tt);
        }
        mainAnimating = true;
        pt.setOnFinished(e -> {
            for (int i = 0; i <= oldSize - 1 && i < mainSlots.size(); i++) {
                mainSlots.get(i).pane.setTranslateX(0);
                mainSlots.get(i).pane.setOpacity(1.0);
            }
            refreshMainFromData();
            updateMainMarkers();
            mainActiveAnims.clear(); mainAnimating = false;
        });
        pt.play();
    }

    private double mainDx() {
        return (mainSlots.isEmpty()?76:mainSlots.get(0).pane.getWidth()) + (mainRow!=null?mainRow.getSpacing():6);
    }

    private void clearMainAnimations() {
        for (Animation a: mainActiveAnims) { try { a.stop(); } catch (Exception ignore) {} }
        for (Slot s: mainSlots) { s.pane.setTranslateX(0); s.pane.setOpacity(1.0); }
        mainActiveAnims.clear(); mainAnimating = false;
        refreshMainFromData(); updateMainMarkers();
    }

    private void refreshMainFromData() {
        // newest at left
        for (int i=0;i<capacity;i++) {
            Slot s = mainSlots.get(i);
            if (i < size) {
                int vi = data[size - 1 - i];
                fillSlot(s, String.valueOf(vi), Color.web("#4a90e2"));
            } else { emptySlot(s); }
        }
    }

    private int visualIndexForMain(int logicalIndex) {
        if (logicalIndex < 0 || logicalIndex >= size) return -1;
        return size - 1 - logicalIndex;
    }

    public void mainSearchCompare(int index) {
        int vIdx = visualIndexForMain(index);
        if (vIdx < 0 || vIdx >= mainSlots.size()) return;
        // clear previous highlight if any and not the found cell
        if (mainHighlightedVIdx >= 0 && mainHighlightedVIdx < mainSlots.size() && mainHighlightedVIdx != mainFoundVIdx) {
            Slot prev = mainSlots.get(mainHighlightedVIdx);
            // restore default fill for occupied slot
            if (mainHighlightedVIdx < size) {
                fillSlot(prev, prev.label.getText(), Color.web("#4a90e2"));
            } else {
                emptySlot(prev);
            }
        }
        Slot s = mainSlots.get(vIdx);
        s.rect.setFill(Color.web("#f39c12"));
        s.rect.setStroke(Color.BLACK);
        mainHighlightedVIdx = vIdx;
    }

    public void mainSearchFound(int index) {
        int vIdx = visualIndexForMain(index);
        if (vIdx < 0 || vIdx >= mainSlots.size()) return;
        Slot s = mainSlots.get(vIdx);
        // transition yellow -> green
        Color from = Color.web("#f39c12");
        Color to = Color.web("#27ae60");
        FillTransition ft = new FillTransition(Duration.millis(320), s.rect, from, to);
        ft.play();
        mainFoundVIdx = vIdx;
        if (mainHighlightedVIdx == vIdx) mainHighlightedVIdx = -1;
    }

    public void flashMainOutlineRed() {
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.ZERO, e -> setMainStrokeRed(true)),
            new javafx.animation.KeyFrame(Duration.millis(150), e -> setMainStrokeRed(false)),
            new javafx.animation.KeyFrame(Duration.millis(300), e -> setMainStrokeRed(true)),
            new javafx.animation.KeyFrame(Duration.millis(450), e -> setMainStrokeRed(false)),
            new javafx.animation.KeyFrame(Duration.millis(600), e -> setMainStrokeRed(true)),
            new javafx.animation.KeyFrame(Duration.millis(750), e -> setMainStrokeRed(false))
        );
        tl.setOnFinished(e -> restoreMainStroke());
        tl.play();
    }

    private void setMainStrokeRed(boolean on) {
        for (int j=0;j<mainSlots.size();j++) {
            Slot s = mainSlots.get(j);
            if (on) s.rect.setStroke(Color.web("#e74c3c"));
            else s.rect.setStroke(j < size ? Color.BLACK : Color.DARKGRAY);
        }
    }

    private void restoreMainStroke() {
        for (int i=0;i<mainSlots.size();i++) {
            Slot s = mainSlots.get(i);
            s.rect.setStroke(i < size ? Color.BLACK : Color.DARKGRAY);
        }
    }

    public void clearMainSearchHighlight() {
        if (mainHighlightedVIdx >= 0 && mainHighlightedVIdx < mainSlots.size() && mainHighlightedVIdx != mainFoundVIdx) {
            Slot prev = mainSlots.get(mainHighlightedVIdx);
            if (mainHighlightedVIdx < size) fillSlot(prev, prev.label.getText(), Color.web("#4a90e2")); else emptySlot(prev);
        }
        mainHighlightedVIdx = -1;
    }

    // Aux queue animations
    public void auxQEnq(int idx, int value) {
        // append to aux queue data and shift-right visuals (newest at left)
        if (auxQAnimating) clearAuxQAnimations();
        int oldSize = auxQSize;
        if (auxQSize < capacity) { auxQData[auxQSize] = value; auxQSize++; }
        if (oldSize > 0) {
            double dx = auxQDx();
            ParallelTransition pt = new ParallelTransition();
            for (int i=0;i<oldSize;i++) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(180), auxQSlots.get(i).pane);
                tt.setByX(dx); tt.setInterpolator(Interpolator.EASE_BOTH);
                pt.getChildren().add(tt); auxQActiveAnims.add(tt);
            }
            auxQAnimating = true;
            pt.setOnFinished(e -> {
                for (int i=0;i<oldSize;i++) auxQSlots.get(i).pane.setTranslateX(0);
                refreshAuxQueueFromData();
                auxQActiveAnims.clear(); auxQAnimating = false;
            });
            pt.play();
        } else {
            refreshAuxQueueFromData();
            if (!auxQSlots.isEmpty()) {
                FadeTransition ft = new FadeTransition(Duration.millis(150), auxQSlots.get(0).pane);
                ft.setFromValue(0.0); ft.setToValue(1.0); ft.play();
            }
        }
    }

    public void auxQDeq(int idx) {
        if (auxQSize<=0) return;
        if (auxQAnimating) clearAuxQAnimations();
        int oldSize = auxQSize;
        // remove front from data
        for (int i=1;i<oldSize;i++) auxQData[i-1] = auxQData[i];
        auxQData[oldSize-1] = 0; auxQSize--;
        double dx = auxQDx();
        ParallelTransition pt = new ParallelTransition();
        // fade + slide right the rightmost visual cell
        if (oldSize - 1 >= 0) {
            TranslateTransition ttLast = new TranslateTransition(Duration.millis(160), auxQSlots.get(oldSize - 1).pane);
            ttLast.setByX(dx); ttLast.setInterpolator(Interpolator.EASE_BOTH);
            FadeTransition ftLast = new FadeTransition(Duration.millis(160), auxQSlots.get(oldSize - 1).pane);
            ftLast.setFromValue(1.0); ftLast.setToValue(0.0);
            pt.getChildren().addAll(ttLast, ftLast);
            auxQActiveAnims.add(ttLast); auxQActiveAnims.add(ftLast);
        }
        // shift-right remaining visuals
        for (int i=0;i<auxQSize;i++) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(180), auxQSlots.get(i).pane);
            tt.setByX(dx); tt.setInterpolator(Interpolator.EASE_BOTH);
            pt.getChildren().add(tt); auxQActiveAnims.add(tt);
        }
        auxQAnimating = true;
        pt.setOnFinished(e -> {
            for (int i=0;i<=oldSize-1 && i<auxQSlots.size(); i++) { auxQSlots.get(i).pane.setTranslateX(0); auxQSlots.get(i).pane.setOpacity(1.0);}            
            refreshAuxQueueFromData();
            auxQActiveAnims.clear(); auxQAnimating = false;
        });
        pt.play();
    }

    private double auxQDx() {
        return (auxQSlots.isEmpty()?76:auxQSlots.get(0).pane.getWidth()) + (auxQRow!=null?auxQRow.getSpacing():6);
    }

    private void clearAuxQAnimations() {
        for (Animation a: auxQActiveAnims) { try { a.stop(); } catch (Exception ignore) {} }
        for (Slot s: auxQSlots) { s.pane.setTranslateX(0); s.pane.setOpacity(1.0); }
        auxQActiveAnims.clear(); auxQAnimating = false; refreshAuxQueueFromData();
    }

    private void refreshAuxQueueFromData() {
        for (int i=0;i<capacity;i++) {
            Slot s = auxQSlots.get(i);
            if (i < auxQSize) {
                int vi = auxQData[auxQSize - 1 - i];
                fillSlot(s, String.valueOf(vi), Color.web("#95a5a6"));
            } else { emptySlot(s); }
        }
    }

    // Aux stack animations
    public void auxSPush(int idx, int value) {
        int visualIdx = capacity-1-idx;
        if (visualIdx<0 || visualIdx>=auxSSlots.size()) return;
        Slot s = auxSSlots.get(visualIdx);
        fillSlot(s, String.valueOf(value), Color.web("#8e44ad"));
        auxTop = Math.max(auxTop, idx);
    }

    public void auxSPop(int idx) {
        int visualIdx = capacity-1-idx;
        if (visualIdx<0 || visualIdx>=auxSSlots.size()) return;
        Slot s = auxSSlots.get(visualIdx);
        FadeTransition ft = new FadeTransition(Duration.millis(120), s.pane);
        ft.setFromValue(1.0); ft.setToValue(0.3); ft.setOnFinished(e -> emptySlot(s)); ft.play();
        auxTop = idx-1;
    }
}
