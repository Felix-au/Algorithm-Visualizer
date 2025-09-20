package com.algorithmvisualizer.visualization;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Renders a 2-row matrix for visited state:
 * Row 1: node indices 0..n-1
 * Row 2: T/F for visited
 */
public class VisitedMatrixRenderer {

    private final VBox container;
    private final FlowPane headerRow;
    private final FlowPane valueRow;
    private VBox[] headerCells;
    private VBox[] valueCells;
    private int n = 0;

    public VisitedMatrixRenderer() {
        container = new VBox(6.0);
        headerRow = new FlowPane(6.0, 6.0);
        headerRow.setAlignment(Pos.CENTER);
        headerRow.setPrefWrapLength(400.0);
        valueRow = new FlowPane(6.0, 6.0);
        valueRow.setAlignment(Pos.CENTER);
        valueRow.setPrefWrapLength(400.0);
        container.getChildren().addAll(headerRow, valueRow);
    }

    public VBox getNode() { return container; }

    public void setSize(int n) {
        this.n = Math.max(0, n);
        rebuild();
    }

    public void updateVisited(boolean[] visited) {
        if (valueCells == null) return;
        for (int i = 0; i < n; i++) {
            boolean v = visited != null && i < visited.length && visited[i];
            Label lab = (Label) valueCells[i].getChildren().get(0);
            lab.setText(v ? "T" : "F");
            if (v) {
                valueCells[i].setStyle("-fx-background-color: FORESTGREEN; -fx-border-color: #333; -fx-padding: 6;");
                lab.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: white;");
            } else {
                valueCells[i].setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #333; -fx-padding: 6;");
                lab.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: black;");
            }
        }
    }

    private void rebuild() {
        headerRow.getChildren().clear();
        valueRow.getChildren().clear();
        headerCells = new VBox[n];
        valueCells = new VBox[n];
        for (int i = 0; i < n; i++) {
            VBox hc = new VBox(4.0);
            hc.setAlignment(Pos.CENTER);
            hc.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #333; -fx-padding: 6;");
            Label h = new Label(String.valueOf(i));
            h.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px;");
            hc.getChildren().add(h);
            headerCells[i] = hc;
            headerRow.getChildren().add(hc);

            VBox vc = new VBox(4.0);
            vc.setAlignment(Pos.CENTER);
            vc.setStyle("-fx-background-color: #f7f7f7; -fx-border-color: #333; -fx-padding: 6;");
            Label v = new Label("F");
            v.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px;");
            vc.getChildren().add(v);
            valueCells[i] = vc;
            valueRow.getChildren().add(vc);
        }
    }
}
