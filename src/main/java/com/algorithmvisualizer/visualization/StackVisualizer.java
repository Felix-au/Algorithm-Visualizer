package com.algorithmvisualizer.visualization;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
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

/**
 * Visualizes a stack (and an auxiliary stack) vertically with capacity slots.
 */
public class StackVisualizer {
    private final HBox root;
    private final VBox mainContainer;
    private final VBox auxContainer;
    private final VBox queueContainer;
    private final Label mainLabel;
    private final Label auxLabel;
    private final Label queueLabel;

    private int capacity = 10;
    private int top = -1;
    private int[] data = new int[0];

    private int auxTop = -1;
    private int[] auxData = new int[0];
    private int[] queueData = new int[0];
    private int queueSize = 0;

    private final List<StackSlot> mainSlots = new ArrayList<>();
    private final List<StackSlot> auxSlots = new ArrayList<>();
    private final List<StackSlot> queueSlots = new ArrayList<>();
    private HBox queueRow;
    private HBox queueMarkerRow;
    private final List<Label> queueMarkers = new ArrayList<>();
    private boolean queueAnimating = false;
    private final List<Animation> queueActiveAnims = new ArrayList<>();

    private static class StackSlot {
        final StackPane pane;
        final Rectangle rect;
        final Label label;
        StackSlot(StackPane pane, Rectangle rect, Label label) {
            this.pane = pane; this.rect = rect; this.label = label;
        }
    }

    public StackVisualizer() {
        root = new HBox(30);
        root.setAlignment(Pos.CENTER);
        mainContainer = new VBox(6);
        mainContainer.setAlignment(Pos.CENTER);
        auxContainer = new VBox(6);
        auxContainer.setAlignment(Pos.CENTER);
        queueContainer = new VBox(6);
        queueContainer.setAlignment(Pos.CENTER);

        mainLabel = makeTitle("Main Stack");
        auxLabel = makeTitle("Aux Stack");
        queueLabel = makeTitle("Aux Queue");

        mainContainer.getChildren().add(mainLabel);
        auxContainer.getChildren().add(auxLabel);
        queueContainer.getChildren().add(queueLabel);

        Region spacerLeft = new Region();
        Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        root.getChildren().addAll(spacerLeft, mainContainer, auxContainer, queueContainer, spacerRight);

        // Queue initially hidden
        queueContainer.setVisible(false);
        queueContainer.setManaged(false);

        rebuildSlots();
    }

    private Label makeTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", 14));
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    public Node getNode() { return root; }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(1, capacity);
        data = new int[this.capacity];
        auxData = new int[this.capacity];
        queueData = new int[this.capacity];
        top = -1;
        auxTop = -1;
        queueSize = 0;
        rebuildSlots();
    }

    private void rebuildSlots() {
        // Clear existing item panes after the title
        mainContainer.getChildren().remove(1, mainContainer.getChildren().size());
        auxContainer.getChildren().remove(1, auxContainer.getChildren().size());
        queueContainer.getChildren().remove(1, queueContainer.getChildren().size());
        mainSlots.clear(); auxSlots.clear(); queueSlots.clear();
        // Build from top (index capacity-1) to 0 visually so top is at bottom
        for (int i = capacity - 1; i >= 0; i--) {
            mainSlots.add(createSlot());
            auxSlots.add(createSlot());
        }
        for (StackSlot s : mainSlots) mainContainer.getChildren().add(s.pane);
        for (StackSlot s : auxSlots) auxContainer.getChildren().add(s.pane);
        // Queue as horizontal row with markers under it
        queueRow = new HBox(6);
        queueMarkerRow = new HBox(6);
        queueMarkers.clear();
        for (int i = 0; i < capacity; i++) {
            StackSlot s = createSlot();
            queueSlots.add(s);
            queueRow.getChildren().add(s.pane);
            Label m = new Label("");
            m.setMinWidth(70); m.setPrefWidth(70); m.setAlignment(Pos.CENTER);
            m.setStyle("-fx-text-fill: #2b4c7e; -fx-font-size: 10px; -fx-font-weight: bold;");
            queueMarkers.add(m);
            queueMarkerRow.getChildren().add(m);
        }
        queueContainer.getChildren().addAll(queueRow, queueMarkerRow);
        refreshAll();
    }

    private StackSlot createSlot() {
        Rectangle rect = new Rectangle(60, 28, Color.TRANSPARENT);
        rect.setStroke(Color.DARKGRAY);
        rect.getStrokeDashArray().setAll(6.0, 6.0);
        Label lbl = new Label("");
        StackPane pane = new StackPane(rect, lbl);
        pane.setPrefSize(70, 32);
        return new StackSlot(pane, rect, lbl);
    }

    public void resetData(int[] values, int top) {
        this.top = top;
        if (values == null) values = new int[0];
        for (int i = 0; i < capacity; i++) {
            if (i <= top) this.data[i] = values[i];
        }
        refreshAll();
    }

    public void resetAux() {
        this.auxTop = -1;
        refreshAll();
    }

    public void setQueueVisible(boolean visible) {
        queueContainer.setVisible(visible);
        queueContainer.setManaged(visible);
    }

    public void resetQueue() {
        this.queueSize = 0;
        for (int i = 0; i < capacity; i++) {
            int vi = i;
            if (i < queueSlots.size()) emptySlot(queueSlots.get(i));
        }
        // clear markers
        for (Label m : queueMarkers) m.setText("");
        // clear data buffer
        for (int i = 0; i < queueData.length; i++) queueData[i] = 0;
        clearQueueAnimations();
    }

    public void resetQueueData(int[] values, int size) {
        if (values == null) values = new int[0];
        // Defensive: ensure internal buffers/slots are sized to current capacity.
        if (queueData == null || queueData.length != capacity || queueSlots.size() != capacity) {
            queueData = new int[capacity];
            // If slots are not yet built (e.g. visualizer not initialized), rebuild slots so access is safe
            if (queueSlots.size() != capacity) rebuildSlots();
        }
        this.queueSize = Math.max(0, Math.min(size, capacity));
        // copy into internal buffer (treat values[0]..values[size-1] as chronological front..tail)
        for (int i = 0; i < capacity; i++) queueData[i] = 0;
        for (int i = 0; i < this.queueSize && i < values.length; i++) queueData[i] = values[i];
        for (int i = 0; i < capacity; i++) {
            StackSlot slot = queueSlots.get(i);
            if (i < queueSize) {
                // newest at left, so map left index i to values[queueSize-1-i]
                int vi = queueData[queueSize - 1 - i];
                fillSlot(slot, String.valueOf(vi), Color.web("#16a085"));
            } else {
                emptySlot(slot);
            }
        }
        updateQueueMarkers();
    }

    public void resetAuxData(int[] values, int auxTop) {
        if (values == null) values = new int[0];
        this.auxTop = auxTop;
        for (int i = 0; i < capacity; i++) {
            if (i <= auxTop) this.auxData[i] = values[i];
        }
        refreshAll();
    }

    private void refreshAll() {
        // Main
        for (int i = 0; i < capacity; i++) {
            int visualIdx = capacity - 1 - i; // bottom visually is i=0
            StackSlot slot = mainSlots.get(visualIdx);
            if (i <= top) {
                fillSlot(slot, String.valueOf(data[i]), Color.web("#4a90e2"));
            } else {
                emptySlot(slot);
            }
        }
        // Aux
        for (int i = 0; i < capacity; i++) {
            int visualIdx = capacity - 1 - i;
            StackSlot slot = auxSlots.get(visualIdx);
            if (i <= auxTop) {
                fillSlot(slot, String.valueOf(auxData[i]), Color.web("#95a5a6"));
            } else {
                emptySlot(slot);
            }
        }
        // Queue (newest at left)
        for (int i = 0; i < capacity; i++) {
            StackSlot slot = queueSlots.get(i);
            if (i < queueSize) {
                int vi = queueData[queueSize - 1 - i];
                fillSlot(slot, String.valueOf(vi), Color.web("#16a085"));
            } else {
                emptySlot(slot);
            }
        }
        updateQueueMarkers();
        // Highlight top
        if (top >= 0) highlightTop(top);
    }

    private void fillSlot(StackSlot slot, String text, Color color) {
        slot.rect.setFill(color);
        slot.rect.setStroke(Color.BLACK);
        slot.rect.getStrokeDashArray().clear();
        slot.label.setText(text);
        slot.label.setTextFill(Color.WHITE);
        slot.label.setStyle("-fx-font-weight: bold;");
    }

    private void emptySlot(StackSlot slot) {
        slot.rect.setFill(Color.TRANSPARENT);
        slot.rect.setStroke(Color.DARKGRAY);
        slot.rect.getStrokeDashArray().setAll(6.0, 6.0);
        slot.label.setText("");
    }

    private void highlightTop(int idx) {
        int visualIdx = capacity - 1 - idx;
        if (visualIdx < 0 || visualIdx >= mainSlots.size()) return;
        StackSlot slot = mainSlots.get(visualIdx);
        slot.rect.setFill(Color.web("#2ecc71"));
    }

    // Animations
    public void animatePush(int idx, int value) {
        int visualIdx = capacity - 1 - idx;
        StackSlot slot = mainSlots.get(visualIdx);
        slot.label.setText(String.valueOf(value));
        slot.label.setTextFill(Color.WHITE);
        slot.rect.setFill(Color.web("#4a90e2"));
        slot.rect.setStroke(Color.BLACK);
        slot.rect.getStrokeDashArray().clear();

        TranslateTransition tt = new TranslateTransition(Duration.millis(200), slot.pane);
        tt.setFromY(-20); tt.setToY(0);
        FadeTransition ft = new FadeTransition(Duration.millis(200), slot.pane);
        ft.setFromValue(0.0); ft.setToValue(1.0);
        tt.play(); ft.play();
        this.top = Math.max(this.top, idx);
    }

    public void animatePop(int idx) {
        int visualIdx = capacity - 1 - idx;
        StackSlot slot = mainSlots.get(visualIdx);
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), slot.pane);
        tt.setFromY(0); tt.setToY(-20);
        FadeTransition ft = new FadeTransition(Duration.millis(200), slot.pane);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        tt.setOnFinished(e -> emptySlot(slot));
        tt.play(); ft.play();
        this.top = idx - 1;
    }

    public void animatePeek(int idx) {
        int visualIdx = capacity - 1 - idx;
        StackSlot slot = mainSlots.get(visualIdx);
        slot.rect.setFill(Color.web("#f1c40f"));
    }

    public void animateSearchCompare(int idx) {
        int visualIdx = capacity - 1 - idx;
        StackSlot slot = mainSlots.get(visualIdx);
        slot.rect.setFill(Color.web("#f39c12"));
    }

    public void animateFound(int idx) {
        int visualIdx = capacity - 1 - idx;
        StackSlot slot = mainSlots.get(visualIdx);
        slot.rect.setFill(Color.web("#27ae60"));
    }

    public void flashOverflowUnderflow() {
        // Brief border flash on all slots
        for (StackSlot s : mainSlots) {
            s.rect.setStroke(Color.web("#e74c3c"));
        }
    }

    // Aux stack animations
    public void auxPush(int idx, int value) {
        int visualIdx = capacity - 1 - idx;
        StackSlot slot = auxSlots.get(visualIdx);
        slot.label.setText(String.valueOf(value));
        slot.label.setTextFill(Color.WHITE);
        slot.rect.setFill(Color.web("#95a5a6"));
        slot.rect.setStroke(Color.BLACK);
        slot.rect.getStrokeDashArray().clear();
        auxTop = Math.max(auxTop, idx);
    }

    public void auxClear() {
        auxTop = -1;
        for (int i = 0; i < capacity; i++) {
            int visualIdx = capacity - 1 - i;
            emptySlot(auxSlots.get(visualIdx));
        }
    }

    public void auxPop(int idx) {
        int visualIdx = capacity - 1 - idx;
        if (visualIdx < 0 || visualIdx >= auxSlots.size()) return;
        StackSlot slot = auxSlots.get(visualIdx);
        // simple fade-out to indicate pop from aux
        FadeTransition ft = new FadeTransition(Duration.millis(150), slot.pane);
        ft.setFromValue(1.0); ft.setToValue(0.3);
        ft.setOnFinished(e -> {
            emptySlot(slot);
            slot.pane.setOpacity(1.0);
        });
        ft.play();
        if (auxTop >= idx) auxTop = idx - 1;
    }

    // Queue animations
    public void queueEnq(int idx, int value) {
        if (queueAnimating) clearQueueAnimations();
        int oldSize = queueSize;
        if (queueSize < capacity) {
            // append to internal buffer tail (chronological)
            queueData[queueSize] = value;
            queueSize++;
        }
        // Animate right-shift of existing visible items (oldSize slots)
        if (oldSize > 0) {
            double dx = queueDx();
            ParallelTransition pt = new ParallelTransition();
            for (int i = 0; i < oldSize; i++) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(180), queueSlots.get(i).pane);
                tt.setByX(dx);
                tt.setInterpolator(Interpolator.EASE_BOTH);
                pt.getChildren().add(tt);
                queueActiveAnims.add(tt);
            }
            queueAnimating = true;
            pt.setOnFinished(e -> {
                // Reset translations and render from data
                for (int i = 0; i < oldSize; i++) queueSlots.get(i).pane.setTranslateX(0);
                refreshQueueFromData();
                updateQueueMarkers();
                queueActiveAnims.clear();
                queueAnimating = false;
            });
            pt.play();
        } else {
            // No existing items: just render new at left with a small fade
            refreshQueueFromData();
            if (!queueSlots.isEmpty()) {
                FadeTransition ft = new FadeTransition(Duration.millis(150), queueSlots.get(0).pane);
                ft.setFromValue(0.0); ft.setToValue(1.0); ft.play();
            }
            updateQueueMarkers();
        }
    }

    public void queueDeq(int idx) {
        if (queueSize <= 0) return;
        if (queueAnimating) clearQueueAnimations();
        int oldSize = queueSize;
        // remove front from internal buffer (index 0)
        for (int i = 1; i < oldSize; i++) queueData[i - 1] = queueData[i];
        queueData[oldSize - 1] = 0; // clear tail cell
        queueSize--;
        double dx = queueDx();
        ParallelTransition pt = new ParallelTransition();
        // fade + slide out the previous rightmost visual cell
        if (oldSize - 1 >= 0) {
            TranslateTransition ttLast = new TranslateTransition(Duration.millis(160), queueSlots.get(oldSize - 1).pane);
            ttLast.setByX(dx);
            ttLast.setInterpolator(Interpolator.EASE_BOTH);
            FadeTransition ftLast = new FadeTransition(Duration.millis(160), queueSlots.get(oldSize - 1).pane);
            ftLast.setFromValue(1.0); ftLast.setToValue(0.0);
            pt.getChildren().addAll(ttLast, ftLast);
            queueActiveAnims.add(ttLast); queueActiveAnims.add(ftLast);
        }
        // shift-right remaining visuals
        for (int i = 0; i < queueSize; i++) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(180), queueSlots.get(i).pane);
            tt.setByX(dx);
            tt.setInterpolator(Interpolator.EASE_BOTH);
            pt.getChildren().add(tt);
            queueActiveAnims.add(tt);
        }
        queueAnimating = true;
        pt.setOnFinished(ev -> {
            // reset translations and render from data
            for (int i = 0; i <= oldSize - 1 && i < queueSlots.size(); i++) {
                queueSlots.get(i).pane.setTranslateX(0);
                queueSlots.get(i).pane.setOpacity(1.0);
            }
            refreshQueueFromData();
            updateQueueMarkers();
            queueActiveAnims.clear();
            queueAnimating = false;
        });
        pt.play();
    }

    private void updateQueueMarkers() {
        if (queueMarkers.isEmpty()) return;
        for (int i = 0; i < queueMarkers.size(); i++) queueMarkers.get(i).setText("");
        if (queueSize > 0) {
            queueMarkers.get(0).setText("Tail");
            queueMarkers.get(Math.min(queueSize - 1, queueMarkers.size() - 1)).setText("Front");
        }
    }

    private double queueDx() {
        return (queueSlots.isEmpty() ? 76 : queueSlots.get(0).pane.getWidth()) + (queueRow != null ? queueRow.getSpacing() : 6);
    }

    private void clearQueueAnimations() {
        for (Animation a : queueActiveAnims) {
            try { a.stop(); } catch (Exception ignore) {}
        }
        for (StackSlot s : queueSlots) {
            s.pane.setTranslateX(0);
            s.pane.setOpacity(1.0);
        }
        queueActiveAnims.clear();
        queueAnimating = false;
        refreshQueueFromData();
        updateQueueMarkers();
    }

    private void refreshQueueFromData() {
        for (int i = 0; i < capacity; i++) {
            StackSlot slot = queueSlots.get(i);
            if (i < queueSize) {
                int vi = queueData[queueSize - 1 - i];
                fillSlot(slot, String.valueOf(vi), Color.web("#16a085"));
            } else {
                emptySlot(slot);
            }
        }
    }
}
