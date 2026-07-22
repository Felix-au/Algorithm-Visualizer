package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.KruskalSolver;
import com.algorithmvisualizer.algorithm.MSTSolver;
import com.algorithmvisualizer.algorithm.PrimSolver;
import com.algorithmvisualizer.model.MSTTechnique;
import com.algorithmvisualizer.visualization.GraphRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;

/**
 * Controller for Minimum Spanning Tree visualization.
 * Supports both Kruskal's and Prim's algorithms.
 */
public class MSTController implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;

    // Visuals
    private GraphRenderer graphView;
    
    // Solutions pane views
    private TableView<EdgeRow> kruskalTable;
    private ObservableList<EdgeRow> kruskalEdges = FXCollections.observableArrayList();
    
    private TableView<VertexRow> primVertexTable;
    private ObservableList<VertexRow> primVertices = FXCollections.observableArrayList();
    
    private TableView<EdgeRow> primFrontierTable;
    private ObservableList<EdgeRow> primFrontier = FXCollections.observableArrayList();

    // Solver
    private MSTSolver solver;
    private MSTTechnique technique = MSTTechnique.KRUSKAL;
    private int startVertex = 0; // For Prim only

    // Playback
    private Timeline timeline;
    private boolean isPlaying = false;
    private long stepDelayUntilMs = 0L;

    // History for step-back
    private final Deque<MSTSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // Graph data
    private int nodeCount = 6;
    private int[][] edges; // [u, v, weight]
    private Map<String, Integer> edgeWeightMap = new HashMap<>();
    private double density = 0.5;
    private int minWeight = 1;
    private int maxWeight = 15;

    // Edge colors for visualization
    private static final Color COLOR_DEFAULT = Color.GRAY;
    private static final Color COLOR_CONSIDERING = Color.ORANGE;
    private static final Color COLOR_MST = Color.web("#2ecc71"); // Green
    private static final Color COLOR_REJECTED = Color.web("#e74c3c"); // Red
    private static final Color COLOR_FRONTIER = Color.web("#f39c12"); // Yellow

    @FXML
    private void initialize() {
        graphView = new GraphRenderer();
        graphView.showEdgeWeights(true);
        
        kruskalTable = buildKruskalTable();
        primVertexTable = buildPrimVertexTable();
        primFrontierTable = buildPrimFrontierTable();
        
        initDefaultGraph();
        renderGraph();
        chooseSolver();
        solver.reset();
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        // Place graph view
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(graphView.getNode());
        }

        // Header: title + layout toggle
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Minimum Spanning Tree (MST)");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            ToggleButton layoutToggle = new ToggleButton("Tree View");
            layoutToggle.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 12; -fx-font-size: 11px; -fx-cursor: hand;");
            layoutToggle.setOnAction(e -> {
                if (layoutToggle.isSelected()) {
                    graphView.setLayoutMode(GraphRenderer.LayoutMode.TREE);
                    layoutToggle.setText("Circular View");
                    layoutToggle.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 12; -fx-font-size: 11px; -fx-cursor: hand;");
                } else {
                    graphView.setLayoutMode(GraphRenderer.LayoutMode.CIRCULAR);
                    layoutToggle.setText("Tree View");
                    layoutToggle.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 12; -fx-font-size: 11px; -fx-cursor: hand;");
                }
            });
            
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            parent.chessboardHeaderBox.getChildren().addAll(hdr, spacer, layoutToggle, new Separator());
        }

        // Legend
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            HBox row = new HBox(15.0);
            
            javafx.scene.shape.Rectangle def = new javafx.scene.shape.Rectangle(20, 3, COLOR_DEFAULT);
            HBox lDef = new HBox(5.0, def, new Label("Not processed"));
            
            javafx.scene.shape.Rectangle cons = new javafx.scene.shape.Rectangle(20, 3, COLOR_CONSIDERING);
            HBox lCons = new HBox(5.0, cons, new Label("Considering"));
            
            javafx.scene.shape.Rectangle mst = new javafx.scene.shape.Rectangle(20, 4, COLOR_MST);
            HBox lMst = new HBox(5.0, mst, new Label("In MST"));
            
            javafx.scene.shape.Rectangle rej = new javafx.scene.shape.Rectangle(20, 2, COLOR_REJECTED);
            rej.getStrokeDashArray().addAll(5.0, 5.0);
            rej.setStroke(COLOR_REJECTED);
            HBox lRej = new HBox(5.0, rej, new Label("Rejected"));
            
            row.getChildren().addAll(lDef, lCons, lMst, lRej);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        setupParameterControls();
        buildSolutionsPane();

        // Controls wiring
        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }

        // Initialize text areas
        parent.setCurrentAlgorithmName(technique == MSTTechnique.KRUSKAL ? "MST Kruskal" : "MST Prim");
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready.");
    }

    private void setupParameterControls() {
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Nodes:");
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 30, nodeCount));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onNodeCountChanged(n));
        }
        
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
            parent.paramElementsLabel.setText("Edges (u-v:w, comma-separated):");
        }
        
        if (parent.paramElementsBox != null) { parent.paramElementsBox.setVisible(false); parent.paramElementsBox.setManaged(false); }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            parent.paramElementsField.setPromptText("e.g. 0-1:4, 1-2:3, 2-3:5");
            parent.paramElementsField.setText(edgesToString(edges));
        }

        // Add controls: Randomize + Density + Weight Range + Technique + Start Vertex
        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setOnAction(e -> onRandomizeGraph());
            javafx.scene.Parent buttonParent = parent.paramRandomizeButton.getParent();
            if (buttonParent instanceof HBox) {
                HBox buttonBox = (HBox) buttonParent;
                int insertIdx = buttonBox.getChildren().indexOf(parent.paramRandomizeButton);
                Button randomizeBtn = parent.paramRandomizeButton;
                buttonBox.getChildren().remove(randomizeBtn);
                Button applyBtn = parent.paramApplyButton;

                // Density slider
                Label dLabel = new Label("Density:");
                Slider densitySlider = new Slider(0.3, 0.9, density);
                densitySlider.setPrefWidth(140);
                densitySlider.setStyle("-fx-accent: #3b82f6;");
                Label dVal = new Label(String.format("%d%%", (int)(density*100)));
                densitySlider.valueProperty().addListener((obs, o, n) -> {
                    density = Math.max(0.3, Math.min(0.9, n.doubleValue()));
                    dVal.setText(String.format("%d%%", (int)(density*100)));
                });

                // Weight range sliders
                Label wLabel = new Label("Weight:");
                Slider minWeightSlider = new Slider(1, 10, minWeight);
                minWeightSlider.setPrefWidth(70);
                minWeightSlider.setStyle("-fx-accent: #8b5cf6;");
                Label minVal = new Label(String.valueOf(minWeight));
                minWeightSlider.valueProperty().addListener((obs, o, n) -> {
                    minWeight = (int)Math.round(n.doubleValue());
                    if (minWeight > maxWeight) minWeight = maxWeight;
                    minVal.setText(String.valueOf(minWeight));
                });
                
                Slider maxWeightSlider = new Slider(5, 20, maxWeight);
                maxWeightSlider.setPrefWidth(70);
                maxWeightSlider.setStyle("-fx-accent: #8b5cf6;");
                Label maxVal = new Label(String.valueOf(maxWeight));
                maxWeightSlider.valueProperty().addListener((obs, o, n) -> {
                    maxWeight = (int)Math.round(n.doubleValue());
                    if (maxWeight < minWeight) maxWeight = minWeight;
                    maxVal.setText(String.valueOf(maxWeight));
                });

                // Technique toggle
                ToggleButton techniqueToggle = new ToggleButton();
                Runnable updateTechniqueToggle = () -> {
                    boolean prim = (technique == MSTTechnique.PRIM);
                    techniqueToggle.setSelected(prim);
                    techniqueToggle.setText(prim ? "Technique: Prim" : "Technique: Kruskal");
                    techniqueToggle.setStyle(prim
                            ? "-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;"
                            : "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                };
                updateTechniqueToggle.run();
                techniqueToggle.setOnAction(e -> {
                    technique = techniqueToggle.isSelected() ? MSTTechnique.PRIM : MSTTechnique.KRUSKAL;
                    updateTechniqueToggle.run();
                    refreshAll();
                });

                // Style buttons
                randomizeBtn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
                if (applyBtn != null) applyBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");

                double leftColWidth = 160.0;
                randomizeBtn.setPrefWidth(leftColWidth);
                techniqueToggle.setPrefWidth(leftColWidth);

                dLabel.setPrefWidth(60);
                wLabel.setPrefWidth(60);

                // Rows
                HBox right1 = new HBox(6.0, dLabel, densitySlider, dVal);
                HBox right2 = new HBox(6.0, wLabel, minWeightSlider, minVal, new Label("-"), maxWeightSlider, maxVal);
                HBox row1 = new HBox(8.0, randomizeBtn, new Separator(), right1);
                HBox row2 = new HBox(8.0, techniqueToggle, new Separator(), right2);
                HBox row3 = new HBox(8.0, (applyBtn != null ? applyBtn : new Label("")));
                row1.setAlignment(Pos.CENTER_LEFT);
                row2.setAlignment(Pos.CENTER_LEFT);
                row3.setAlignment(Pos.CENTER_LEFT);
                VBox rows = new VBox(8.0, row1, row2, row3);
                buttonBox.getChildren().add(insertIdx >= 0 ? insertIdx : 0, rows);
            }
        }
        if (parent.paramApplyButton != null) parent.paramApplyButton.setOnAction(e -> onApplyGraph());
    }

    private void buildSolutionsPane() {
        if (parent == null || parent.solutionsContent == null) return;
        parent.solutionsHeaderBox.setVisible(true);
        parent.solutionsHeaderBox.setManaged(true);
        parent.solutionsHeaderBox.getChildren().clear();
        
        Label solHeader = new Label("MST - " + (technique == MSTTechnique.KRUSKAL ? "Edge Table" : "Vertex & Frontier"));
        solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        parent.solutionsHeaderBox.getChildren().addAll(solHeader, spacer);

        parent.solutionsSubHeaderBox.setVisible(true);
        parent.solutionsSubHeaderBox.setManaged(true);
        parent.solutionsSubHeaderBox.getChildren().clear();
        Label sub = new Label(technique == MSTTechnique.KRUSKAL ? "Sorted Edges" : "Algorithm State");
        sub.setStyle("-fx-font-weight: bold;");
        parent.solutionsSubHeaderBox.getChildren().addAll(sub);

        parent.solutionsContent.getChildren().clear();
        if (technique == MSTTechnique.KRUSKAL) {
            kruskalEdges.clear();
            kruskalTable.setItems(kruskalEdges);
            VBox.setVgrow(kruskalTable, Priority.ALWAYS);
            parent.solutionsContent.getChildren().add(kruskalTable);
            updateKruskalTable();
        } else {
            primVertices.clear();
            primFrontier.clear();
            primVertexTable.setItems(primVertices);
            primFrontierTable.setItems(primFrontier);
            
            Label vertexLabel = new Label("Vertices:");
            vertexLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            VBox.setVgrow(primVertexTable, Priority.ALWAYS);
            
            Region spacer2 = new Region();
            spacer2.setPrefHeight(15);
            
            Label frontierLabel = new Label("Frontier (Priority Queue):");
            frontierLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            VBox.setVgrow(primFrontierTable, Priority.ALWAYS);
            
            parent.solutionsContent.getChildren().addAll(
                vertexLabel, primVertexTable,
                spacer2,
                frontierLabel, primFrontierTable
            );
            updatePrimTables();
        }
    }

    private TableView<EdgeRow> buildKruskalTable() {
        TableView<EdgeRow> table = new TableView<>();
        
        TableColumn<EdgeRow, String> edgeCol = new TableColumn<>("Edge");
        edgeCol.setCellValueFactory(new PropertyValueFactory<>("edge"));
        edgeCol.setPrefWidth(60);
        
        TableColumn<EdgeRow, Integer> weightCol = new TableColumn<>("Weight");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));
        weightCol.setPrefWidth(60);
        
        TableColumn<EdgeRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<EdgeRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                
                javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(6);
                switch (item) {
                    case "Not Processed":
                        dot.setFill(Color.LIGHTGRAY);
                        break;
                    case "Considering":
                        dot.setFill(COLOR_CONSIDERING);
                        break;
                    case "In MST":
                        dot.setFill(COLOR_MST);
                        break;
                    case "Rejected":
                        dot.setFill(COLOR_REJECTED);
                        break;
                }
                dot.setStroke(Color.BLACK);
                dot.setStrokeWidth(0.5);
                
                HBox box = new HBox(5, dot, new Label(item));
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
            }
        });
        
        TableColumn<EdgeRow, String> setInfoCol = new TableColumn<>("Sets");
        setInfoCol.setCellValueFactory(new PropertyValueFactory<>("setInfo"));
        setInfoCol.setPrefWidth(80);
        
        table.getColumns().addAll(edgeCol, weightCol, statusCol, setInfoCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TableView<VertexRow> buildPrimVertexTable() {
        TableView<VertexRow> table = new TableView<>();
        
        TableColumn<VertexRow, Integer> vertexCol = new TableColumn<>("Vertex");
        vertexCol.setCellValueFactory(new PropertyValueFactory<>("vertex"));
        vertexCol.setPrefWidth(60);
        
        TableColumn<VertexRow, String> inMSTCol = new TableColumn<>("In MST");
        inMSTCol.setCellValueFactory(new PropertyValueFactory<>("inMST"));
        inMSTCol.setPrefWidth(60);
        inMSTCol.setCellFactory(col -> new TableCell<VertexRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if (item.equals("Yes")) {
                    setStyle("-fx-background-color: rgba(46, 204, 113, 0.2);");
                } else {
                    setStyle("");
                }
            }
        });
        
        TableColumn<VertexRow, String> minWeightCol = new TableColumn<>("Min Weight");
        minWeightCol.setCellValueFactory(new PropertyValueFactory<>("minWeight"));
        minWeightCol.setPrefWidth(80);
        
        table.getColumns().addAll(vertexCol, inMSTCol, minWeightCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TableView<EdgeRow> buildPrimFrontierTable() {
        TableView<EdgeRow> table = new TableView<>();
        
        TableColumn<EdgeRow, String> edgeCol = new TableColumn<>("Edge");
        edgeCol.setCellValueFactory(new PropertyValueFactory<>("edge"));
        edgeCol.setPrefWidth(60);
        
        TableColumn<EdgeRow, Integer> weightCol = new TableColumn<>("Weight");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));
        weightCol.setPrefWidth(60);
        
        table.getColumns().addAll(edgeCol, weightCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    // --- Playback controls ---
    public void onPause() { stopTimeline(); }

    @Override
    public void onPlay() {
        if (isPlaying) { onPause(); return; }
        isPlaying = true;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null) timeline.play();
        if (parent != null) parent.playButton.setText("⏸ Pause");
    }

    public void onStepForward() {
        if (solver == null || solver.isDone()) return;
        history.push(solver.snapshot());
        solver.step();
    }

    public void onStepBack() {
        if (isPlaying) onPause();
        if (history.isEmpty()) return;
        MSTSolver.State s = history.pop();
        solver.restore(s);
        
        // Re-render graph from restored state
        renderGraph();
        visualizeState(s);
        
        // Remove logs for last step
        if (parent != null && parent.progressArea != null) {
            if (countLogsForStep && currentStepLogLines > 0) {
                removeLastLogLines(currentStepLogLines);
                currentStepLogLines = 0;
                countLogsForStep = false;
            } else if (!progressHistory.isEmpty()) {
                int toRemove = progressHistory.pop();
                removeLastLogLines(toRemove);
            }
        }
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Step back");
    }

    public void onReset() {
        stopTimeline();
        history.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        chooseSolver();
        solver.reset();
        renderGraph();
        initProgressLog();
        updateVariablesPanel();
        buildSolutionsPane();
        if (parent != null) parent.stepDescription.setText("Ready.");
    }

    private void stopTimeline() {
        if (timeline != null) { timeline.stop(); timeline = null; }
        isPlaying = false;
        if (parent != null) parent.playButton.setText("▶ Play");
    }

    private void rebuildTimelineWithCurrentSpeed() {
        if (timeline != null) { timeline.stop(); timeline = null; }
        double speed = parent != null ? parent.speedSlider.getValue() : 1.0;
        double fps = Math.max(1.0, speed);
        Duration frame = Duration.millis(1000.0 / fps);
        timeline = new Timeline(new KeyFrame(frame, e -> {
            if (solver.isDone()) { stopTimeline(); return; }
            long now = System.currentTimeMillis();
            if (now < stepDelayUntilMs) return;
            history.push(solver.snapshot());
            solver.step();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void updatePlaybackSpeed() {
        if (!isPlaying) return;
        rebuildTimelineWithCurrentSpeed();
        if (timeline != null) timeline.play();
    }

    // --- Step event handling ---
    private void onStepEvent(MSTSolver.StepType type, int u, int v, int weight, Map<String, Object> meta) {
        countLogsForStep = true;
        currentStepLogLines = 0;
        
        switch (type) {
            case INIT:
                if (technique == MSTTechnique.KRUSKAL) {
                    appendProgress("🚀 Starting Kruskal's algorithm...");
                    graphView.flashAllNodes();
                } else {
                    appendProgress("🚀 Starting Prim's algorithm from vertex " + u);
                    graphView.highlightCurrent(u);
                    graphView.setNodeColor(u, COLOR_MST);
                }
                delay(800);
                break;
                
            case SORT_EDGES:
                appendProgress("📊 Sorted " + edges.length + " edges by weight");
                updateKruskalTable();
                delay(1200);
                break;
                
            case SELECT_EDGE:
                appendProgress("🔍 Considering edge " + u + "-" + v + " (weight: " + weight + ")");
                graphView.highlightEdgeWithColor(u, v, COLOR_CONSIDERING, 3.0);
                graphView.highlightCurrent(u);
                graphView.highlightCurrent(v);
                updateKruskalTable();
                delay(400);
                break;
                
            case UNION_SETS:
                int unionRootU = meta.containsKey("rootU") ? (int) meta.get("rootU") : u;
                int unionRootV = meta.containsKey("rootV") ? (int) meta.get("rootV") : v;
                appendProgress("🔗 Union: Merging sets with roots " + unionRootU + " and " + unionRootV);
                delay(500);
                break;
                
            case MST_EDGE_ADDED:
                appendProgress("✅ Added edge " + u + "-" + v + " (weight: " + weight + ") to MST");
                graphView.transitionEdgeColor(u, v, COLOR_CONSIDERING, COLOR_MST, 5.0, Duration.millis(500));
                graphView.setNodeColor(u, COLOR_MST);
                graphView.setNodeColor(v, COLOR_MST);
                
                int mstSize = meta.containsKey("mstSize") ? (int) meta.get("mstSize") : 0;
                int totalWeight = meta.containsKey("totalWeight") ? (int) meta.get("totalWeight") : 0;
                appendProgress("📈 MST Progress: " + mstSize + "/" + (nodeCount - 1) + " edges, Total weight: " + totalWeight);
                
                updateKruskalTable();
                updatePrimTables();
                delay(500);
                break;
                
            case MST_EDGE_REJECTED:
                appendProgress("❌ Rejected edge " + u + "-" + v + " (creates cycle)");
                graphView.highlightEdgeWithColor(u, v, COLOR_REJECTED, 1.5);
                graphView.setEdgeDashed(u, v, true);
                graphView.shakeEdge(u, v);
                graphView.shakeNode(u);
                graphView.shakeNode(v);
                updateKruskalTable();
                delay(600);
                break;
                
            // Kruskal granular steps
            case FIND_SET_U:
                int rootU = meta.containsKey("rootU") ? (int) meta.get("rootU") : u;
                appendProgress("🔎 Finding set of vertex " + u + " → root: " + rootU);
                graphView.glowVertex(u, COLOR_CONSIDERING);
                graphView.glowVertex(rootU, Color.PURPLE);
                delay(350);
                break;
                
            case FIND_SET_V:
                int rootV = meta.containsKey("rootV") ? (int) meta.get("rootV") : v;
                appendProgress("🔎 Finding set of vertex " + v + " → root: " + rootV);
                graphView.glowVertex(v, COLOR_CONSIDERING);
                graphView.glowVertex(rootV, Color.PURPLE);
                delay(350);
                break;
                
            case COMPARE_ROOTS:
                int rU = meta.containsKey("rootU") ? (int) meta.get("rootU") : u;
                int rV = meta.containsKey("rootV") ? (int) meta.get("rootV") : v;
                boolean sameset = meta.containsKey("sameset") && (boolean) meta.get("sameset");
                
                if (sameset) {
                    appendProgress("⚠️ Same root (" + rU + ") - edge would create cycle!");
                    graphView.shakeNode(rU);
                } else {
                    appendProgress("✓ Different roots (" + rU + " ≠ " + rV + ") - safe to add!");
                    graphView.pulseEdge(u, v, Color.LIGHTGREEN);
                }
                
                // Remove glows
                graphView.removeGlow(u);
                graphView.removeGlow(v);
                graphView.removeGlow(rU);
                graphView.removeGlow(rV);
                delay(500);
                break;
                
            // Prim granular steps
            case HIGHLIGHT_CURRENT_VERTEX:
                appendProgress("⭐ Current vertex: " + u);
                graphView.glowVertex(u, Color.GOLD);
                graphView.shakeNode(u);
                delay(400);
                break;
                
            case EXPLORE_EDGE_START:
                appendProgress("👀 Exploring edge " + u + "-" + v + " (weight: " + weight + ")");
                graphView.highlightEdgeWithColor(u, v, Color.YELLOW, 2.5);
                graphView.blinkEdge(u, v, Color.YELLOW, 1);
                graphView.emphasizeEdgeWeight(u, v);
                delay(300);
                break;
                
            case COMPARE_EDGE_WEIGHT:
                int oldW = meta.containsKey("oldWeight") ? (int) meta.get("oldWeight") : -1;
                boolean isBetter = meta.containsKey("isBetter") && (boolean) meta.get("isBetter");
                
                if (oldW == -1) {
                    appendProgress("📝 First path to vertex " + v + " with weight " + weight);
                } else if (isBetter) {
                    appendProgress("📉 Better path! Old: " + oldW + " → New: " + weight);
                    graphView.pulseEdge(u, v, Color.LIGHTGREEN);
                } else {
                    appendProgress("📈 Worse path. Current: " + oldW + " < New: " + weight);
                    graphView.shakeEdge(u, v);
                }
                delay(350);
                break;
                
            case EXPLORE_EDGE_END:
                graphView.highlightEdgeWithColor(u, v, Color.LIGHTGRAY, 1.0);
                delay(200);
                break;
                
            case SCAN_FRONTIER_START:
                appendProgress("🎯 Scanning frontier for minimum edge...");
                delay(400);
                break;
                
            case SCAN_FRONTIER_COMPARE:
                int frontierSize = meta.containsKey("frontierSize") ? (int) meta.get("frontierSize") : 0;
                appendProgress("🔍 Comparing " + frontierSize + " frontier edges");
                graphView.blinkEdge(u, v, Color.CYAN, 1);
                delay(300);
                break;
                
            case SCAN_FRONTIER_FOUND:
                appendProgress("🎉 Found minimum: edge " + u + "-" + v + " (weight: " + weight + ")");
                graphView.blinkEdge(u, v, Color.GOLD, 3);
                graphView.pulseEdge(u, v, Color.GOLD);
                delay(500);
                break;
                
            case SELECT_MIN_EDGE:
                appendProgress("✨ Extracting minimum edge from frontier");
                graphView.removeGlow(u);
                delay(300);
                break;
                
            case ADD_VERTEX_TO_MST:
                appendProgress("✅ Added vertex " + u + " to MST");
                graphView.setNodeColor(u, COLOR_MST);
                int mstVertices = meta.containsKey("mstSize") ? (int) meta.get("mstSize") : 0;
                appendProgress("📈 MST has " + mstVertices + " vertices");
                updatePrimTables();
                delay(500);
                break;
                
            case EXPLORE_EDGES:
                appendProgress("🔎 Exploring edges from vertex " + u);
                delay(300);
                break;
                
            case ADD_TO_FRONTIER:
                appendProgress("➕ Added edge " + u + "-" + v + " (weight: " + weight + ") to frontier");
                graphView.highlightEdgeWithColor(u, v, COLOR_FRONTIER, 3.0);
                graphView.highlightCurrent(v);
                updatePrimTables();
                delay(350);
                break;
                
            case UPDATE_PRIORITY:
                int oldWeight = meta.containsKey("oldWeight") ? (int) meta.get("oldWeight") : -1;
                appendProgress("🔄 Updated: Found shorter edge to vertex " + v + " (old: " + oldWeight + ", new: " + weight + ")");
                graphView.blinkEdge(u, v, COLOR_FRONTIER, 2);
                updatePrimTables();
                delay(450);
                break;
                
            case COMPLETE:
                Set<String> mstEdges = solver.getMSTEdges();
                appendProgress("═══════════════════════════════════════");
                appendProgress("🎉 MST COMPLETE!");
                appendProgress("═══════════════════════════════════════");
                
                if (technique == MSTTechnique.KRUSKAL) {
                    appendProgress("Algorithm: Kruskal's (Union-Find)");
                    appendProgress("Total Edges Examined: " + edges.length);
                    appendProgress("Edges in MST: " + mstEdges.size());
                    appendProgress("Edges Rejected: " + (edges.length - mstEdges.size()));
                } else {
                    appendProgress("Algorithm: Prim's (Priority Queue)");
                    int verticesInMST = 0;
                    for (boolean b : solver.getInMST()) if (b) verticesInMST++;
                    appendProgress("Vertices in MST: " + verticesInMST + "/" + nodeCount);
                    appendProgress("Edges in MST: " + mstEdges.size());
                }
                
                appendProgress("Minimum Total Weight: " + weight);
                appendProgress("");
                appendProgress("MST Edges:");
                int edgeNum = 1;
                for (String edgeKey : mstEdges) {
                    String[] parts = edgeKey.split("-");
                    int eu = Integer.parseInt(parts[0]);
                    int ev = Integer.parseInt(parts[1]);
                    // Find weight
                    int edgeWeight = 0;
                    for (int[] e : edges) {
                        if ((e[0] == eu && e[1] == ev) || (e[0] == ev && e[1] == eu)) {
                            edgeWeight = e[2];
                            break;
                        }
                    }
                    appendProgress("  " + edgeNum + ". Edge " + eu + "-" + ev + " (weight: " + edgeWeight + ")");
                    edgeNum++;
                }
                appendProgress("═══════════════════════════════════════");
                
                // Pulse all MST edges
                for (String edgeKey : mstEdges) {
                    String[] parts = edgeKey.split("-");
                    int eu = Integer.parseInt(parts[0]);
                    int ev = Integer.parseInt(parts[1]);
                    graphView.pulseEdge(eu, ev, COLOR_MST);
                }
                stopTimeline();
                delay(1000);
                break;
        }
        
        updateVariablesPanel();
        if (countLogsForStep) {
            progressHistory.push(currentStepLogLines);
            countLogsForStep = false;
            currentStepLogLines = 0;
        }
    }

    private void delay(long ms) {
        stepDelayUntilMs = System.currentTimeMillis() + ms;
    }

    // --- Helper methods ---
    private void chooseSolver() {
        if (technique == MSTTechnique.KRUSKAL) {
            solver = new KruskalSolver();
        } else {
            solver = new PrimSolver();
            ((PrimSolver) solver).setStartVertex(startVertex);
        }
        solver.setGraph(nodeCount, edges);
        solver.setStepListener(this::onStepEvent);
    }

    private void onNodeCountChanged(int n) {
        nodeCount = Math.max(3, n);
        refreshAll();
    }

    private void onRandomizeGraph() {
        edges = randomWeightedGraph(nodeCount, density, minWeight, maxWeight);
        if (parent != null && parent.paramElementsField != null) {
            parent.paramElementsField.setText(edgesToString(edges));
        }
        refreshAll();
    }

    private void onApplyGraph() {
        if (parent == null || parent.paramElementsField == null) {
            refreshAll();
            return;
        }
        int[][] parsed = parseEdges(parent.paramElementsField.getText(), nodeCount);
        if (parsed != null) {
            edges = parsed;
        }
        refreshAll();
    }

    private void refreshAll() {
        stopTimeline();
        chooseSolver();
        solver.reset();
        renderGraph();
        renderCode();
        initProgressLog();
        updateVariablesPanel();
        buildSolutionsPane();
    }

    private void renderGraph() {
        // Build adjacency list for GraphRenderer
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            adj.add(new ArrayList<>());
        }
        
        edgeWeightMap.clear();
        for (int[] e : edges) {
            int u = e[0], v = e[1], w = e[2];
            // Skip edges that reference nodes beyond current nodeCount
            if (u >= nodeCount || v >= nodeCount || u < 0 || v < 0) continue;
            if (!adj.get(u).contains(v)) {
                adj.get(u).add(v);
                adj.get(v).add(u);
            }
            String key = Math.min(u, v) + "-" + Math.max(u, v);
            edgeWeightMap.put(key, w);
        }
        
        graphView.setGraph(nodeCount, adj);
        graphView.setEdgeWeights(edgeWeightMap);
        graphView.showEdgeWeights(true);
    }

    private void visualizeState(MSTSolver.State s) {
        // Visualize MST edges
        for (String edgeKey : s.mstEdges) {
            String[] parts = edgeKey.split("-");
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            graphView.highlightEdgeWithColor(u, v, COLOR_MST, 5.0);
        }
        
        // Visualize vertices in MST
        for (int i = 0; i < s.inMST.length; i++) {
            if (s.inMST[i]) {
                graphView.setNodeColor(i, COLOR_MST);
            }
        }
    }

    private void updateKruskalTable() {
        if (technique != MSTTechnique.KRUSKAL) return;
        
        kruskalEdges.clear();
        Set<String> mstEdges = solver.getMSTEdges();
        int[] parent = solver.getParent();
        
        for (int i = 0; i < edges.length; i++) {
            int u = edges[i][0], v = edges[i][1], w = edges[i][2];
            String edgeKey = Math.min(u, v) + "-" + Math.max(u, v);
            
            String status = "Not Processed";
            if (mstEdges.contains(edgeKey)) {
                status = "In MST";
            }
            
            String setInfo = "";
            if (parent != null && parent.length > 0) {
                int rootU = find(parent, u);
                int rootV = find(parent, v);
                setInfo = "{" + rootU + "," + rootV + "}";
            }
            
            kruskalEdges.add(new EdgeRow(edgeKey, w, status, setInfo));
        }
    }

    private void updatePrimTables() {
        if (technique != MSTTechnique.PRIM) return;
        
        primVertices.clear();
        boolean[] inMST = solver.getInMST();
        Map<Integer, Integer> minWeight = solver.getMinWeight();
        
        for (int i = 0; i < nodeCount; i++) {
            String inMSTStr = inMST[i] ? "Yes" : "No";
            int minW = minWeight.getOrDefault(i, Integer.MAX_VALUE);
            String minWStr = minW == Integer.MAX_VALUE ? "∞" : String.valueOf(minW);
            primVertices.add(new VertexRow(i, inMSTStr, minWStr));
        }
        
        // Update frontier table (simplified - would need access to actual frontier)
        primFrontier.clear();
    }

    private int find(int[] parent, int x) {
        if (parent[x] != x) {
            parent[x] = find(parent, parent[x]);
        }
        return parent[x];
    }

    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        appendProgress("MST: n = " + nodeCount + ", edges = " + edges.length);
        appendProgress("Technique: " + (technique == MSTTechnique.KRUSKAL ? "Kruskal" : "Prim"));
    }

    private void appendProgress(String line) {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.appendText(line + "\n");
        if (countLogsForStep) currentStepLogLines++;
    }

    private void removeLastLogLines(int count) {
        if (parent == null || parent.progressArea == null || count <= 0) return;
        String text = parent.progressArea.getText();
        if (text == null || text.isEmpty()) return;
        String[] lines = text.split("\n", -1);
        int total = lines.length;
        int newLen = Math.max(0, total - count);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < newLen; i++) sb.append(lines[i]).append("\n");
        parent.progressArea.setText(sb.toString());
    }

    private void updateVariablesPanel() {
        if (parent == null || parent.variableList == null) return;
        parent.variableList.getItems().clear();
        
        Set<String> mstEdges = solver != null ? solver.getMSTEdges() : new HashSet<>();
        int totalWeight = solver != null ? solver.getTotalWeight() : 0;
        
        parent.variableList.getItems().addAll(
            "nodes: " + nodeCount,
            "edges: " + edges.length,
            "technique: " + (technique == MSTTechnique.KRUSKAL ? "Kruskal" : "Prim"),
            "mstEdges: " + mstEdges.size(),
            "targetEdges: " + (nodeCount - 1),
            "totalWeight: " + totalWeight,
            "done: " + (solver != null && solver.isDone())
        );
        
        if (technique == MSTTechnique.PRIM) {
            parent.variableList.getItems().add("startVertex: " + startVertex);
        }
    }

    private void renderCode() {
        if (parent == null) return;
        parent.setCurrentAlgorithmName(technique == MSTTechnique.KRUSKAL ? "MST Kruskal" : "MST Prim");
        
        // Update code templates with current parameters
        if (technique == MSTTechnique.KRUSKAL) {
            com.algorithmvisualizer.code.AlgorithmCode code = 
                com.algorithmvisualizer.code.CodeRepository.getCode("MST Kruskal");
            if (code instanceof com.algorithmvisualizer.code.implementations.KruskalCode) {
                ((com.algorithmvisualizer.code.implementations.KruskalCode) code).updateParameters(nodeCount, edges);
            }
        } else {
            com.algorithmvisualizer.code.AlgorithmCode code = 
                com.algorithmvisualizer.code.CodeRepository.getCode("MST Prim");
            if (code instanceof com.algorithmvisualizer.code.implementations.PrimCode) {
                ((com.algorithmvisualizer.code.implementations.PrimCode) code).updateParameters(nodeCount, edges, startVertex);
            }
        }
        
        // Notify parent to reload code for current language
        parent.loadCodeForCurrentLanguage();
    }

    // --- Graph generation and parsing ---
    private void initDefaultGraph() {
        edges = new int[][] {
            {0, 1, 4}, {0, 2, 3},
            {1, 2, 1}, {1, 3, 2},
            {2, 3, 4}, {2, 4, 5},
            {3, 4, 7}, {3, 5, 8},
            {4, 5, 6}
        };
    }

    private static int[][] randomWeightedGraph(int n, double density, int minW, int maxW) {
        Random rnd = new Random();
        List<int[]> edgeList = new ArrayList<>();
        
        // Create spanning tree first
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) order.add(i);
        Collections.shuffle(order, rnd);
        
        for (int i = 1; i < n; i++) {
            int u = order.get(i);
            int v = order.get(rnd.nextInt(i));
            int w = minW + rnd.nextInt(maxW - minW + 1);
            edgeList.add(new int[]{u, v, w});
        }
        
        // Add extra edges based on density
        int maxEdges = n * (n - 1) / 2;
        int targetEdges = Math.max(n - 1, (int) (maxEdges * density));
        
        Set<String> existing = new HashSet<>();
        for (int[] e : edgeList) {
            existing.add(Math.min(e[0], e[1]) + "-" + Math.max(e[0], e[1]));
        }
        
        while (edgeList.size() < targetEdges) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            if (u == v) continue;
            String key = Math.min(u, v) + "-" + Math.max(u, v);
            if (!existing.contains(key)) {
                int w = minW + rnd.nextInt(maxW - minW + 1);
                edgeList.add(new int[]{u, v, w});
                existing.add(key);
            }
        }
        
        return edgeList.toArray(new int[0][]);
    }

    private static int[][] parseEdges(String text, int n) {
        if (text == null || text.trim().isEmpty()) return new int[0][];
        
        List<int[]> edgeList = new ArrayList<>();
        String[] parts = text.split(",");
        
        try {
            for (String p : parts) {
                String s = p.trim();
                if (s.isEmpty()) continue;
                
                // Format: u-v:w
                String[] edgeWeight = s.split(":");
                if (edgeWeight.length != 2) continue;
                
                String[] uv = edgeWeight[0].split("-");
                if (uv.length != 2) continue;
                
                int u = Integer.parseInt(uv[0].trim());
                int v = Integer.parseInt(uv[1].trim());
                int w = Integer.parseInt(edgeWeight[1].trim());
                
                if (u < 0 || v < 0 || u >= n || v >= n || u == v) continue;
                if (w < 1) continue;
                
                edgeList.add(new int[]{u, v, w});
            }
            return edgeList.toArray(new int[0][]);
        } catch (Exception ex) {
            return new int[0][];
        }
    }

    private static String edgesToString(int[][] edges) {
        if (edges == null || edges.length == 0) return "";
        
        List<String> parts = new ArrayList<>();
        for (int[] e : edges) {
            parts.add(e[0] + "-" + e[1] + ":" + e[2]);
        }
        return String.join(", ", parts);
    }

    // --- Table row classes ---
    public static class EdgeRow {
        private final SimpleStringProperty edge;
        private final SimpleIntegerProperty weight;
        private final SimpleStringProperty status;
        private final SimpleStringProperty setInfo;

        public EdgeRow(String edge, int weight, String status, String setInfo) {
            this.edge = new SimpleStringProperty(edge);
            this.weight = new SimpleIntegerProperty(weight);
            this.status = new SimpleStringProperty(status);
            this.setInfo = new SimpleStringProperty(setInfo);
        }

        public String getEdge() { return edge.get(); }
        public int getWeight() { return weight.get(); }
        public String getStatus() { return status.get(); }
        public String getSetInfo() { return setInfo.get(); }
    }

    public static class VertexRow {
        private final SimpleIntegerProperty vertex;
        private final SimpleStringProperty inMST;
        private final SimpleStringProperty minWeight;

        public VertexRow(int vertex, String inMST, String minWeight) {
            this.vertex = new SimpleIntegerProperty(vertex);
            this.inMST = new SimpleStringProperty(inMST);
            this.minWeight = new SimpleStringProperty(minWeight);
        }

        public int getVertex() { return vertex.get(); }
        public String getInMST() { return inMST.get(); }
        public String getMinWeight() { return minWeight.get(); }
    }
}
