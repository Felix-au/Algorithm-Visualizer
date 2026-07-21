package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.BruteForceColoringSolver;
import com.algorithmvisualizer.algorithm.CSPColoringSolver;
import com.algorithmvisualizer.algorithm.GraphColoringSolver;
import com.algorithmvisualizer.model.ColoringMode;
import com.algorithmvisualizer.model.ColoringTechnique;
import com.algorithmvisualizer.visualization.GraphRenderer;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.input.ScrollEvent;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.*;

public class GraphColoringController implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;

    // Visuals
    private GraphRenderer graphView;

    // Solutions pane views
    // Brute Force recursion tree using GraphRenderer
    private GraphRenderer recursionGraph;
    private final java.util.List<java.util.List<Integer>> assignAdj = new java.util.ArrayList<>();
    private final Deque<Integer> assignIdStack = new ArrayDeque<>();
    private final java.util.List<String> assignLabels = new java.util.ArrayList<>();
    private final java.util.List<Integer> assignColors = new java.util.ArrayList<>(); // color index per recursion node id
    private final java.util.List<GraphRenderer> recursionGraphMirrors = new java.util.ArrayList<>();
    private int nextAssignId = 0;
    private int currentAssignId = -1;
    private TableView<CSPRow> cspTable;     // for CSP
    private ObservableList<CSPRow> cspRows = FXCollections.observableArrayList();

    // Solver
    private GraphColoringSolver solver;
    private ColoringTechnique technique = ColoringTechnique.BRUTE_FORCE;
    private ColoringMode mode = ColoringMode.FIND_MINIMUM;
    private int userK = 3; // for USE_K_COLORS
    private boolean anchorV0Enabled = true; // symmetry breaking for brute force

    // Playback
    private Timeline timeline;
    private boolean isPlaying = false;
    private double density = 0.5; // edge density for random graph
    private long stepDelayUntilMs = 0L;

    // History for step-back
    private final Deque<GraphColoringSolver.State> history = new ArrayDeque<>();
    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;

    // Graph data
    private int nodeCount = 8;
    private List<List<Integer>> adj = new ArrayList<>();
    private boolean circularLayout = true;
    private int activeCspNode = -1;
    private Integer pendingTryTreeId = null; // last TRY tree node to potentially promote on ASSIGN
    private Integer currentAttemptK = null; // tracks current k during Find Minimum

    // Color palette (exclude blue which is default graph color)
    private static final Color[] PALETTE = new Color[] {
            Color.web("#e41a1c"), // Red
            Color.web("#4daf4a"), // Green
            Color.web("#ffff33"), // Yellow
            Color.web("#984ea3"), // Purple
            Color.web("#ff7f00"), // Orange
            Color.web("#a65628"), // Brown
            Color.web("#f781bf"), // Pink
            Color.web("#00CED1"), // Teal
            Color.web("#00FFFF"), // Cyan
            Color.web("#FF00FF")  // Magenta
    };

    @FXML
    private void initialize() {
        graphView = new GraphRenderer();
        cspTable = buildCspTable();
        initDefaultGraph();
        renderGraph();
        chooseSolver();
        // Ensure solver is initialized before any playback
        solver.setGraph(nodeCount, adj);
        if (mode == ColoringMode.USE_K_COLORS) solver.setMaxColors(userK);
        solver.reset();
        currentAttemptK = null;
        clearRecursionViz();
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
            Label hdr = new Label("Graph Coloring");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            ToggleButton layoutToggle = new ToggleButton("Tree View");
            layoutToggle.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 12; -fx-font-size: 11px; -fx-cursor: hand;");
            layoutToggle.setOnAction(e -> {
                circularLayout = !layoutToggle.isSelected();
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
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(15.0);
            javafx.scene.shape.Rectangle un = new javafx.scene.shape.Rectangle(12,12, Color.STEELBLUE);
            un.setStroke(Color.BLACK);
            javafx.scene.shape.Rectangle asg = new javafx.scene.shape.Rectangle(12,12, PALETTE[0]);
            asg.setStroke(Color.BLACK);
            javafx.scene.layout.HBox lUn = new javafx.scene.layout.HBox(5.0, un, new Label("Uncolored (blue)"));
            javafx.scene.layout.HBox lAsg = new javafx.scene.layout.HBox(5.0, asg, new Label("Assigned color"));
            row.getChildren().addAll(lUn, lAsg);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Parameter controls reuse and add-ons
        if (parent.paramSizeLabel != null) parent.paramSizeLabel.setText("Nodes:");
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true);
            parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, nodeCount));
            parent.paramBoardSizeSpinner.valueProperty().addListener((obs, o, n) -> onNodeCountChanged(n));
        }
        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel.setVisible(true);
            parent.paramElementsLabel.setManaged(true);
            parent.paramElementsLabel.setText("Edges (u-v, comma-separated):");
        }
        if (parent.paramElementsBox != null) { parent.paramElementsBox.setVisible(false); parent.paramElementsBox.setManaged(false); }
        if (parent.paramElementsField != null) {
            parent.paramElementsField.setVisible(true);
            parent.paramElementsField.setManaged(true);
            parent.paramElementsField.setPromptText("e.g. 0-1, 1-2, 2-3, 0-4");
            parent.paramElementsField.setText(edgesToString(adj));
        }

        // Randomize + Density + Technique + Anchor + Mode + k (rows like BinaryTreeController)
        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setOnAction(e -> onRandomizeGraph());
            javafx.scene.Parent buttonParent = parent.paramRandomizeButton.getParent();
            if (buttonParent instanceof HBox) {
                HBox buttonBox = (HBox) buttonParent;
                // Remove Randomize (we will reinsert inside a row), keep Apply for row 3
                int insertIdx = buttonBox.getChildren().indexOf(parent.paramRandomizeButton);
                Button randomizeBtn = parent.paramRandomizeButton; buttonBox.getChildren().remove(randomizeBtn);
                Button applyBtn = parent.paramApplyButton;

                // Density slider
                Label dLabel = new Label("Density:");
                Slider densitySlider = new Slider(0.0, 1.0, density);
                densitySlider.setPrefWidth(140);
                densitySlider.setStyle("-fx-accent: #3b82f6;") ;
                Label dVal = new Label(String.format("%d%%", (int)(density*100)));
                densitySlider.valueProperty().addListener((obs, o, n) -> {
                    density = Math.max(0.0, Math.min(1.0, n.doubleValue()));
                    dVal.setText(String.format("%d%%", (int)(density*100)));
                });

                // Mode toggle + k slider
                ToggleButton modeToggle = new ToggleButton("Find Minimum");
                modeToggle.setSelected(false);
                modeToggle.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                modeToggle.setOnAction(e -> {
                    if (modeToggle.isSelected()) {
                        modeToggle.setText("Use k Colors");
                        modeToggle.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                        mode = ColoringMode.USE_K_COLORS;
                    } else {
                        modeToggle.setText("Find Minimum");
                        modeToggle.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                        mode = ColoringMode.FIND_MINIMUM;
                    }
                    refreshAll();
                });
                Label kLabel = new Label("Colors k:");
                Slider kSlider = new Slider(2, 20, userK);
                kSlider.setBlockIncrement(1);
                kSlider.setMajorTickUnit(1);
                kSlider.setMinorTickCount(0);
                kSlider.setSnapToTicks(true);
                kSlider.setPrefWidth(160);
                kSlider.setStyle("-fx-accent: #8b5cf6;");
                Label kVal = new Label(String.valueOf(userK));
                kSlider.valueProperty().addListener((obs, o, n) -> {
                    int v = (int)Math.round(n.doubleValue());
                    if (v < 2) v = 2; if (v > 20) v = 20;
                    userK = v; kVal.setText(String.valueOf(v));
                    if (mode == ColoringMode.USE_K_COLORS) refreshAll();
                });

                // Technique toggle (replaces dropdown)
                ToggleButton techniqueToggle = new ToggleButton();
                Runnable updateTechniqueToggle = () -> {
                    boolean csp = (technique == ColoringTechnique.CSP);
                    techniqueToggle.setSelected(csp);
                    techniqueToggle.setText(csp ? "Technique: CSP" : "Technique: Brute Force");
                    techniqueToggle.setStyle(csp
                            ? "-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;"
                            : "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                };
                updateTechniqueToggle.run();
                techniqueToggle.setOnAction(e -> {
                    technique = techniqueToggle.isSelected() ? ColoringTechnique.CSP : ColoringTechnique.BRUTE_FORCE;
                    updateTechniqueToggle.run();
                    refreshAll();
                });

                // Anchor toggle for brute force (styled ToggleButton like DFS cycles toggle)
                ToggleButton anchorToggle = new ToggleButton(anchorV0Enabled ? "Anchor: ON" : "Anchor: OFF");
                anchorToggle.setSelected(anchorV0Enabled);
                anchorToggle.setStyle(anchorV0Enabled
                        ? "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;"
                        : "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                anchorToggle.setOnAction(e2 -> {
                    anchorV0Enabled = anchorToggle.isSelected();
                    anchorToggle.setText(anchorV0Enabled ? "Anchor: ON" : "Anchor: OFF");
                    anchorToggle.setStyle(anchorV0Enabled
                            ? "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;"
                            : "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                    if (technique == ColoringTechnique.BRUTE_FORCE && solver instanceof com.algorithmvisualizer.algorithm.BruteForceColoringSolver) {
                        ((com.algorithmvisualizer.algorithm.BruteForceColoringSolver)solver).setAnchorV0(anchorV0Enabled);
                    }
                    refreshAll();
                });

                // Style buttons
                randomizeBtn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");
                if (applyBtn != null) applyBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 12; -fx-cursor: hand;");

                // Align left column widths so Density and k sliders start at same x
                double leftColWidth = 160.0;
                randomizeBtn.setPrefWidth(leftColWidth);
                modeToggle.setPrefWidth(leftColWidth);

                // Ensure labels align so sliders start in the same column
                dLabel.setPrefWidth(60);
                kLabel.setPrefWidth(60);

                // Rows
                HBox right1 = new HBox(6.0, dLabel, densitySlider, dVal);
                HBox right2 = new HBox(6.0, kLabel, kSlider, kVal);
                HBox row1 = new HBox(8.0, randomizeBtn, new Separator(), right1);
                HBox row2 = new HBox(8.0, modeToggle, new Separator(), right2);
                HBox row3 = new HBox(8.0, techniqueToggle, anchorToggle, (applyBtn != null ? applyBtn : new Label("")));
                row1.setAlignment(Pos.CENTER_LEFT);
                row2.setAlignment(Pos.CENTER_LEFT);
                row3.setAlignment(Pos.CENTER_LEFT);
                VBox rows = new VBox(8.0, row1, row2, row3);
                buttonBox.getChildren().add(insertIdx >= 0 ? insertIdx : 0, rows);
            }
        }
        if (parent.paramApplyButton != null) parent.paramApplyButton.setOnAction(e -> onApplyGraph());

        // Solutions pane content placeholder
        buildSolutionsPane();

        // Controls wiring
        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs, o, n) -> updatePlaybackSpeed());
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }
        
        // Language selector listener - update code when language changes
        if (parent.languageSelector != null) {
            parent.languageSelector.valueProperty().addListener((obs, oldLang, newLang) -> {
                if (newLang != null && !newLang.equals(oldLang)) {
                    renderCode(); // Reload code in new language
                }
            });
        }

        // Initialize text areas
        // Set algorithm name based on technique for full-screen editor
        if (technique == ColoringTechnique.BRUTE_FORCE) {
            parent.setCurrentAlgorithmName("Graph Coloring Brute Force");
        } else {
            parent.setCurrentAlgorithmName("Graph Coloring CSP");
        }
        
        if (parent.languageSelector != null) {
            parent.languageSelector.setValue("Python"); // Set default language
        }
        renderCode(); // This will load the appropriate code
        initProgressLog();
        updateVariablesPanel();
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready.");
    }

    private void buildSolutionsPane() {
        if (parent == null || parent.solutionsContent == null) return;
        parent.solutionsHeaderBox.setVisible(true);
        parent.solutionsHeaderBox.setManaged(true);
        parent.solutionsHeaderBox.getChildren().clear();
        Label solHeader = new Label("Graph Coloring - Solutions Pane");
        solHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button popBtn = new Button(technique == ColoringTechnique.BRUTE_FORCE ? "Pop-out Tree" : "Pop-out Table");
        popBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 10; -fx-cursor: hand;");
        popBtn.setOnAction(e -> { if (technique == ColoringTechnique.BRUTE_FORCE) openRecursionGraphPopout(); else openCspTablePopout(); });
        parent.solutionsHeaderBox.getChildren().addAll(solHeader, spacer, popBtn);

        parent.solutionsSubHeaderBox.setVisible(true);
        parent.solutionsSubHeaderBox.setManaged(true);
        parent.solutionsSubHeaderBox.getChildren().clear();
        Label sub = new Label(technique == ColoringTechnique.BRUTE_FORCE ? "Backtracking Tree" : "CSP Domains Table");
        sub.setStyle("-fx-font-weight: bold;");
        parent.solutionsSubHeaderBox.getChildren().addAll(sub);

        parent.solutionsContent.getChildren().clear();
        if (technique == ColoringTechnique.BRUTE_FORCE) {
            recursionGraph = new GraphRenderer();
            recursionGraph.setLayoutMode(GraphRenderer.LayoutMode.TREE);
            recursionGraph.setNodeRadius(16.0);
            recursionGraph.setTreeLeafSpacingEnabled(true);
            recursionGraph.setTreeMinLeafSpacing(60.0);
            VBox.setVgrow(recursionGraph.getNode(), Priority.ALWAYS);
            parent.solutionsContent.getChildren().addAll(recursionGraph.getNode());
            applyAssignGraphState(recursionGraph);
        } else {
            cspRows.clear();
            cspTable.setItems(cspRows);
            cspTable.setPrefHeight(300);
            parent.solutionsContent.getChildren().add(cspTable);
            // Populate initial domains snapshot
            updateCspTableFromSnapshot(solver.snapshot());
        }
    }

    private TableView<CSPRow> buildCspTable() {
        TableView<CSPRow> table = new TableView<>();
        TableColumn<CSPRow, Integer> nodeCol = new TableColumn<>("Node");
        nodeCol.setCellValueFactory(c -> c.getValue().nodeProperty().asObject());
        nodeCol.setPrefWidth(60);
        TableColumn<CSPRow, Integer> degCol = new TableColumn<>("Degree");
        degCol.setCellValueFactory(c -> c.getValue().degreeProperty().asObject());
        degCol.setPrefWidth(68);
        TableColumn<CSPRow, String> domCol = new TableColumn<>("Domain");
        domCol.setCellValueFactory(c -> c.getValue().domainProperty());
        domCol.setPrefWidth(260);
        // Render domain as colored dots
        domCol.setCellFactory(col -> new TableCell<CSPRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setGraphic(null); return;
                }
                CSPRow r = (CSPRow) getTableRow().getItem();
                java.util.Set<Integer> domain = r.getDomainSet();
                HBox box = new HBox(4);
                if (domain != null) {
                    for (Integer d : domain) {
                        if (d != null && d >= 0 && d < PALETTE.length) {
                            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(6, PALETTE[d]);
                            dot.setStroke(Color.BLACK);
                            dot.setStrokeWidth(0.4);
                            box.getChildren().add(dot);
                        }
                    }
                }
                setText(null);
                setGraphic(box);
            }
        });
        table.setRowFactory(tv -> new TableRow<CSPRow>() {
            @Override
            protected void updateItem(CSPRow item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && item.getNode() == activeCspNode) {
                    setStyle("-fx-background-color: rgba(255,235,59,0.25);");
                } else {
                    setStyle("");
                }
            }
        });
        table.getColumns().addAll(nodeCol, degCol, domCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }


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
        GraphColoringSolver.State s = history.pop();
        solver.restore(s);
        // Re-render colors from restored state
        graphView.setGraph(s.n, adj);
        for (int i = 0; i < s.coloring.length; i++) {
            int col = s.coloring[i];
            if (col >= 0 && col < PALETTE.length) graphView.setNodeColor(i, PALETTE[col]);
        }
        // Remove logs for last step
        if (parent != null && parent.progressArea != null) {
            if (countLogsForStep && currentStepLogLines > 0) {
                removeLastLogLines(currentStepLogLines);
                currentStepLogLines = 0; countLogsForStep = false;
            } else if (!progressHistory.isEmpty()) {
                int toRemove = progressHistory.pop();
                removeLastLogLines(toRemove);
            }
        }
        updateVariablesPanel();
        if (parent != null) parent.stepDescription.setText("Step back");
        updateCspTableFromSnapshot(s);
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
        clearRecursionViz();
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

    // --- Step events ---
    private void onStepEvent(GraphColoringSolver.StepType type, int node, int color, Map<String, Object> meta) {
        countLogsForStep = true; currentStepLogLines = 0;
        switch (type) {
            case TRY_K_COLORS:
                appendProgress("🎯 Trying k = " + color + " colors...");
                currentAttemptK = color;
                // Visual emphasis: flash & shake all nodes, reset trees/tables
                if (graphView != null) {
                    graphView.flashAllNodes();
                    for (int i = 0; i < nodeCount; i++) graphView.shakeNode(i);
                    for (int i = 0; i < nodeCount; i++) graphView.setNodeColor(i, Color.STEELBLUE);
                }
                if (technique == ColoringTechnique.BRUTE_FORCE) {
                    clearRecursionViz();
                    updateRecursionGraphViews();
                } else if (technique == ColoringTechnique.CSP) {
                    // Snapshot here may not yet have new domains; schedule a follow-up refresh
                    Platform.runLater(() -> {
                        updateCspTableFromSnapshot(solver.snapshot());
                        activeCspNode = -1;
                        if (cspTable != null) cspTable.refresh();
                    });
                }
                delay(700);
                break;
            case SELECT_NODE:
                graphView.highlightCurrent(node);
                appendProgress("🔎 Select node " + node);
                activeCspNode = node; if (cspTable != null) cspTable.refresh();
                delay(120);
                break;
            case TRY_COLOR:
                appendProgress("🧪 Try color " + color + " at node " + node);
                // Temporarily show attempted color on the node
                if (color >= 0 && color < PALETTE.length) graphView.setNodeColor(node, PALETTE[color]);
                activeCspNode = node; if (cspTable != null) cspTable.refresh();
                delay(350);
                if (technique == ColoringTechnique.BRUTE_FORCE) {
                    // Create a TRY leaf under currentAssignId
                    int id = nextAssignId++;
                    ensureAssignAdjCapacity(id + 1);
                    while (assignLabels.size() < id + 1) assignLabels.add(null);
                    assignLabels.set(id, String.valueOf(node));
                    while (assignColors.size() < id + 1) assignColors.add(null);
                    assignColors.set(id, color);
                    int parentId = assignIdStack.isEmpty() ? 0 : assignIdStack.peek();
                    if (!assignAdj.get(parentId).contains(id)) assignAdj.get(parentId).add(id);
                    pendingTryTreeId = id; // may be promoted on ASSIGN
                    updateRecursionGraphViews();
                }
                break;
            case CHECK_CONSTRAINT:
                appendProgress("❌ Color not valid at node " + node + " (conflict)");
                // Vibrate conflicting neighbors, then uncolor node and pulse
                try {
                    int[] cur = solver != null ? solver.getColoring() : new int[0];
                    for (int nb : adj.get(node)) {
                        int nbColor = (cur != null && nb < cur.length) ? cur[nb] : -1;
                        if (nbColor == color) {
                            graphView.shakeNode(nb);
                            graphView.highlightEdge(node, nb);
                        }
                    }
                } catch (Exception ignore) {}
                // Uncolor the node back to default and pulse
                graphView.setNodeColor(node, Color.STEELBLUE);
                graphView.flashBacktrackNode(node);
                delay(450);
                if (technique == ColoringTechnique.BRUTE_FORCE) {
                    // keep TRY leaf as-is; clear pending since it failed
                    pendingTryTreeId = null;
                }
                break;
            case ASSIGN_COLOR:
                if (color >= 0 && color < PALETTE.length) graphView.setNodeColor(node, PALETTE[color]);
                appendProgress("✅ Assign color " + color + " to node " + node);
                if (technique == ColoringTechnique.CSP) updateCspRow(node, meta);
                if (technique == ColoringTechnique.BRUTE_FORCE) {
                    // Promote existing TRY leaf to an ASSIGN branch by pushing it on stack
                    int id;
                    if (pendingTryTreeId != null) {
                        id = pendingTryTreeId;
                        pendingTryTreeId = null;
                    } else {
                        // Fallback: create if missing (shouldn't happen normally)
                        id = nextAssignId++;
                        ensureAssignAdjCapacity(id + 1);
                        while (assignLabels.size() < id + 1) assignLabels.add(null);
                        assignLabels.set(id, String.valueOf(node));
                        while (assignColors.size() < id + 1) assignColors.add(null);
                        assignColors.set(id, color);
                        int parentId = assignIdStack.isEmpty() ? 0 : assignIdStack.peek();
                        if (!assignAdj.get(parentId).contains(id)) assignAdj.get(parentId).add(id);
                    }
                    assignIdStack.push(id);
                    currentAssignId = id;
                    updateRecursionGraphViews();
                }
                delay(200);
                break;
            case REDUCE_DOMAIN:
                if (technique == ColoringTechnique.CSP) updateCspRow(node, meta);
                appendProgress("↘ Reduce domain of node " + node + " remove color " + color);
                delay(120);
                break;
            case BACKTRACK:
                graphView.setNodeColor(node, Color.STEELBLUE);
                graphView.flashBacktrackNode(node);
                appendProgress("↩ Backtrack from node " + node);
                if (technique == ColoringTechnique.BRUTE_FORCE) {
                    if (!assignIdStack.isEmpty()) assignIdStack.pop();
                    currentAssignId = assignIdStack.isEmpty() ? -1 : assignIdStack.peek();
                    updateRecursionGraphViews();
                }
                delay(250);
                break;
            case SOLUTION_FOUND:
                appendProgress("🎉 Solution found (colors used = " + color + ")");
                break;
            case NO_SOLUTION:
                appendProgress("⚠ No solution with current k");
                break;
            case DONE:
                appendProgress("✅ Done");
                stopTimeline();
                break;
            case INIT:
            default:
                break;
        }
        updateVariablesPanel();
        // finalize log group
        if (countLogsForStep) { progressHistory.push(currentStepLogLines); countLogsForStep = false; currentStepLogLines = 0; }
    }

    private void delay(long ms) {
        stepDelayUntilMs = System.currentTimeMillis() + Math.max(0, ms);
    }

    private void updateCspRow(int node, Map<String, Object> meta) {
        if (meta == null || cspTable == null) return;
        // degree and domain string
        int degree = (int) meta.getOrDefault("degree", -1);
        @SuppressWarnings("unchecked")
        Set<Integer> domain = (Set<Integer>) meta.getOrDefault("domain", Collections.emptySet());
        String domStr = domainToString(domain);
        boolean found = false;
        for (CSPRow r : cspRows) {
            if (r.getNode() == node) {
                r.setDegree(degree);
                r.setDomain(domStr);
                r.setDomainSet(new LinkedHashSet<>(domain));
                found = true; break;
            }
        }
        if (!found) {
            CSPRow r = new CSPRow(node, degree, domStr);
            r.setDomainSet(new LinkedHashSet<>(domain));
            cspRows.add(r);
        }
        // keep table sorted by node
        cspRows.sort(Comparator.comparingInt(CSPRow::getNode));
        cspTable.refresh();
    }

    private void updateCspTableFromSnapshot(GraphColoringSolver.State s) {
        if (s == null || s.domains == null) return;
        cspRows.clear();
        for (Map.Entry<Integer, Set<Integer>> e : s.domains.entrySet()) {
            int node = e.getKey();
            String domStr = domainToString(e.getValue());
            CSPRow r = new CSPRow(node, degreeOf(node), domStr);
            r.setDomainSet(new LinkedHashSet<>(e.getValue()));
            cspRows.add(r);
        }
        cspRows.sort(Comparator.comparingInt(CSPRow::getNode));
        cspTable.refresh();
    }

    private String domainToString(Set<Integer> domain) {
        if (domain == null || domain.isEmpty()) return "{}";
        List<String> parts = new ArrayList<>();
        for (int d : domain) parts.add(colorName(d));
        return "{" + String.join(", ", parts) + "}";
        
    }

    private String colorName(int idx) {
        switch (idx) {
            case 0: return "Red";
            case 1: return "Green";
            case 2: return "Yellow";
            case 3: return "Purple";
            case 4: return "Orange";
            case 5: return "Brown";
            case 6: return "Pink";
            case 7: return "Teal";
            case 8: return "Cyan";
            case 9: return "Magenta";
            default: return "C" + idx;
        }
    }

    // --- UI helpers ---
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
        String curKStr;
        if (mode == ColoringMode.USE_K_COLORS) {
            curKStr = String.valueOf(userK);
        } else {
            Integer k = currentAttemptK;
            if (k == null) {
                GraphColoringSolver.State s = solver != null ? solver.snapshot() : null;
                k = (s != null && s.attemptK > 0) ? s.attemptK : null;
            }
            curKStr = (k != null && k > 0) ? String.valueOf(k) : "—";
        }
        parent.variableList.getItems().addAll(
                "nodes: " + nodeCount,
                "technique: " + technique,
                "mode: " + mode,
                "current k: " + curKStr
        );
    }

    private void renderCode() {
        if (parent == null || parent.codeArea == null || parent.languageSelector == null) return;
        
        // Build edges array from current adjacency (unique undirected pairs i<j)
        List<int[]> edgesList = new ArrayList<>();
        for (int i = 0; i < adj.size(); i++) {
            for (int j : adj.get(i)) {
                if (i < j) edgesList.add(new int[]{i, j});
            }
        }
        int[][] edges = edgesList.toArray(new int[0][]);
        
        // Get the appropriate code implementation based on technique
        com.algorithmvisualizer.code.AlgorithmCode code = null;
        
        if (technique == ColoringTechnique.BRUTE_FORCE) {
            code = com.algorithmvisualizer.code.CodeRepository.getCode("Graph Coloring Brute Force");
            if (code instanceof com.algorithmvisualizer.code.implementations.GraphColoringBruteForceCode) {
                com.algorithmvisualizer.code.implementations.GraphColoringBruteForceCode bfCode = 
                    (com.algorithmvisualizer.code.implementations.GraphColoringBruteForceCode) code;
                bfCode.updateParameters(nodeCount, edges, mode, userK, anchorV0Enabled);
            }
        } else {
            code = com.algorithmvisualizer.code.CodeRepository.getCode("Graph Coloring CSP");
            if (code instanceof com.algorithmvisualizer.code.implementations.GraphColoringCSPCode) {
                com.algorithmvisualizer.code.implementations.GraphColoringCSPCode cspCode = 
                    (com.algorithmvisualizer.code.implementations.GraphColoringCSPCode) code;
                cspCode.updateParameters(nodeCount, edges, mode, userK);
            }
        }
        
        // Load and display the code for the current language
        if (code != null) {
            String selectedLanguage = parent.languageSelector.getValue();
            if (selectedLanguage != null) {
                String codeText = code.getCodeForLanguage(selectedLanguage);
                if (codeText != null && !codeText.isEmpty()) {
                    parent.codeArea.replaceText(codeText);
                    // Apply syntax highlighting
                    javafx.application.Platform.runLater(() -> {
                        com.algorithmvisualizer.ui.CodeHighlighter.applyHighlighting(parent.codeArea, selectedLanguage);
                    });
                }
            }
        }
    }

    private void renderGraph() {
        graphView.setGraph(nodeCount, adj);
    }

    private void chooseSolver() {
        if (technique == ColoringTechnique.BRUTE_FORCE) {
            solver = new BruteForceColoringSolver();
        } else {
            solver = new CSPColoringSolver();
        }
        solver.setStepListener(this::onStepEvent);
        solver.setMode(mode);
        if (technique == ColoringTechnique.BRUTE_FORCE && solver instanceof BruteForceColoringSolver) {
            ((BruteForceColoringSolver) solver).setAnchorV0(anchorV0Enabled);
        }
    }

    private void onNodeCountChanged(int n) {
        nodeCount = Math.max(1, n);
        ensureAdjSize();
        refreshAll();
    }

    private void onRandomizeGraph() {
        adj = randomConnectedGraphByDensity(nodeCount, density);
        if (parent != null && parent.paramElementsField != null) parent.paramElementsField.setText(edgesToString(adj));
        refreshAll();
    }

    private void onApplyGraph() {
        if (parent == null || parent.paramElementsField == null) { refreshAll(); return; }
        List<List<Integer>> parsed = parseEdges(parent.paramElementsField.getText(), nodeCount);
        if (parsed != null) { adj = parsed; }
        refreshAll();
    }

    private void refreshAll() {
        stopTimeline();
        chooseSolver();
        // Ensure graph is set before initializing domains (for CSP)
        solver.setGraph(nodeCount, adj);
        if (mode == ColoringMode.USE_K_COLORS) solver.setMaxColors(userK);
        // Always reset after changes in technique/mode/k to re-init domains and state
        solver.reset();
        renderGraph();
        renderCode(); // This now handles code loading and display
        initProgressLog();
        updateVariablesPanel();
        clearRecursionViz();
        buildSolutionsPane();
        // Hard refresh CSP table snapshot after reset
        if (technique == ColoringTechnique.CSP) {
            updateCspTableFromSnapshot(solver.snapshot());
            activeCspNode = -1; if (cspTable != null) cspTable.refresh();
        }
        if (parent != null && parent.stepDescription != null) parent.stepDescription.setText("Ready.");
    }

    private void initProgressLog() {
        if (parent == null || parent.progressArea == null) return;
        parent.progressArea.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        parent.progressArea.appendText("Graph Coloring initialized.\n");
    }

    // --- Graph helpers (copied from DFSController) ---
    private void initDefaultGraph() {
        adj = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) adj.add(new ArrayList<>());
        // simple connected example
        addEdge(0,1); addEdge(1,2); addEdge(2,3); addEdge(3,0);
        addEdge(1,4); addEdge(4,5);
        if (nodeCount > 6) {
            for (int i = 6; i < nodeCount; i++) addEdge(i, (i-1));
        }
        for (List<Integer> row : adj) Collections.sort(row);
    }
    private void ensureAdjSize() {
        if (adj == null) adj = new ArrayList<>();
        while (adj.size() < nodeCount) adj.add(new ArrayList<>());
        while (adj.size() > nodeCount) adj.remove(adj.size()-1);
        for (int i = 0; i < nodeCount; i++) {
            final int ii = i;
            List<Integer> row = adj.get(ii);
            row.removeIf(v -> v < 0 || v >= nodeCount || v == ii);
            for (int v : new ArrayList<>(row)) {
                if (!adj.get(v).contains(ii)) adj.get(v).add(ii);
            }
            Collections.sort(row);
        }
    }

    private static List<List<Integer>> randomConnectedGraph(int n, boolean allowCycles) {
        Random rnd = new Random();
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) order.add(i);
        Collections.shuffle(order, rnd);
        for (int i = 1; i < n; i++) {
            int u = order.get(i);
            int v = order.get(rnd.nextInt(i));
            if (!g.get(u).contains(v)) { g.get(u).add(v); g.get(v).add(u); }
        }
        if (allowCycles) {
            int extra = Math.max(0, n);
            for (int k = 0; k < extra; k++) {
                int u = rnd.nextInt(n), v = rnd.nextInt(n);
                if (u != v && !g.get(u).contains(v)) { g.get(u).add(v); g.get(v).add(u); }
            }
        }
        for (List<Integer> row : g) Collections.sort(row);
        return g;
    }

    // Density-based connected random graph: density 0.0 => tree (n-1 edges), 1.0 => complete graph
    private static List<List<Integer>> randomConnectedGraphByDensity(int n, double density) {
        density = Math.max(0.0, Math.min(1.0, density));
        Random rnd = new Random();
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        // Spanning tree first
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) order.add(i);
        Collections.shuffle(order, rnd);
        for (int i = 1; i < n; i++) {
            int u = order.get(i);
            int v = order.get(rnd.nextInt(i));
            if (!g.get(u).contains(v)) { g.get(u).add(v); g.get(v).add(u); }
        }
        int maxEdges = n * (n - 1) / 2;
        int treeEdges = Math.max(0, n - 1);
        int targetEdges = treeEdges + (int)Math.round(density * (maxEdges - treeEdges));
        // Add random edges until target reached
        int currentEdges = 0;
        for (int i = 0; i < n; i++) for (int j : g.get(i)) if (i < j) currentEdges++;
        int attempts = 0, maxAttempts = n * n * 4 + 100;
        while (currentEdges < targetEdges && attempts++ < maxAttempts) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u == v) continue;
            int a = Math.min(u, v), b = Math.max(u, v);
            if (!g.get(a).contains(b)) {
                g.get(a).add(b); g.get(b).add(a); currentEdges++;
            }
        }
        for (List<Integer> row : g) Collections.sort(row);
        return g;
    }

    private static List<List<Integer>> parseEdges(String text, int n) {
        if (text == null) return null;
        List<List<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new ArrayList<>());
        String[] parts = text.split(",");
        try {
            for (String p : parts) {
                String s = p.trim();
                if (s.isEmpty()) continue;
                String[] uv = s.split("-");
                if (uv.length != 2) return null;
                int u = Integer.parseInt(uv[0].trim());
                int v = Integer.parseInt(uv[1].trim());
                if (u < 0 || v < 0 || u >= n || v >= n || u == v) return null;
                if (!g.get(u).contains(v)) { g.get(u).add(v); g.get(v).add(u); }
            }
            for (List<Integer> row : g) Collections.sort(row);
            return g;
        } catch (Exception ex) { return null; }
    }

    private static String edgesToString(List<List<Integer>> g) {
        if (g == null) return "";
        Set<String> set = new LinkedHashSet<>();
        for (int i = 0; i < g.size(); i++) {
            for (int j : g.get(i)) if (i < j) set.add(i + "-" + j);
        }
        return String.join(", ", set);
    }

    private int degreeOf(int node) {
        if (node < 0 || node >= adj.size()) return 0;
        return adj.get(node).size();
    }

    // --- Recursion graph helpers (brute force) ---
    private void clearRecursionViz(){
        assignIdStack.clear();
        assignAdj.clear();
        assignLabels.clear();
        assignColors.clear();
        // Initialize with imaginary root "start"
        ensureAssignAdjCapacity(1);
        while (assignLabels.size() < 1) assignLabels.add(null);
        assignLabels.set(0, "start");
        while (assignColors.size() < 1) assignColors.add(null);
        assignIdStack.push(0);
        nextAssignId = 1;
        currentAssignId = 0;
        updateRecursionGraphViews();
    }

    private void ensureAssignAdjCapacity(int n){
        while (assignAdj.size() < n) assignAdj.add(new ArrayList<>());
    }

    private void openRecursionGraphPopout(){
        javafx.stage.Stage st = new javafx.stage.Stage();
        st.setTitle("Backtracking Tree (Pop-out)");
        GraphRenderer gr = new GraphRenderer();
        gr.setLayoutMode(GraphRenderer.LayoutMode.TREE);
        gr.setNodeRadius(16.0);
        gr.setTreeLeafSpacingEnabled(true);
        gr.setTreeMinLeafSpacing(60.0);
        applyAssignGraphState(gr);
        recursionGraphMirrors.add(gr);
        st.setOnCloseRequest(e -> recursionGraphMirrors.remove(gr));

        Group content = new Group(gr.getNode());
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane(content);
        stack.setPadding(new Insets(24));
        javafx.scene.control.ScrollPane sp = new javafx.scene.control.ScrollPane(stack);
        sp.setPrefViewportWidth(900);
        sp.setPrefViewportHeight(600);
        sp.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setPannable(true);

        // Controls bar at bottom-right
        HBox controls = new HBox(8.0);
        controls.setAlignment(Pos.CENTER_RIGHT);
        Button zoomOut = new Button("-");
        Button zoomIn = new Button("+");
        zoomOut.setTooltip(new Tooltip("Zoom Out (Ctrl+Scroll Down)"));
        zoomIn.setTooltip(new Tooltip("Zoom In (Ctrl+Scroll Up)"));
        ToggleButton autoFit = new ToggleButton();
        zoomOut.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10;");
        zoomIn.setStyle("-fx-background-color: #4ade80; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10;");

        final double[] scale = {1.0};
        Runnable applyScale = () -> { content.setScaleX(scale[0]); content.setScaleY(scale[0]); };

        // Auto-Fit visuals and behavior
        Runnable updateAutoFitAppearance = () -> {
            if (autoFit.isSelected()) {
                autoFit.setText("Auto-Fit: ON");
                autoFit.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-weight: bold;");
            } else {
                autoFit.setText("Auto-Fit: OFF");
                autoFit.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-weight: bold;");
            }
        };
        updateAutoFitAppearance.run();

        zoomIn.setOnAction(e -> { autoFit.setSelected(false); updateAutoFitAppearance.run(); scale[0] = Math.min(5.0, scale[0] + 0.1); applyScale.run(); });
        zoomOut.setOnAction(e -> { autoFit.setSelected(false); updateAutoFitAppearance.run(); scale[0] = Math.max(0.2, scale[0] - 0.1); applyScale.run(); });

        Runnable fitToView = () -> {
            javafx.geometry.Bounds b = content.getBoundsInLocal();
            double cw = Math.max(1.0, b.getWidth());
            double ch = Math.max(1.0, b.getHeight());
            double vw = Math.max(1.0, sp.getViewportBounds().getWidth());
            double vh = Math.max(1.0, sp.getViewportBounds().getHeight());
            double padX = stack.getPadding().getLeft() + stack.getPadding().getRight();
            double padY = stack.getPadding().getTop() + stack.getPadding().getBottom();
            double s = Math.min((vw - padX) / cw, (vh - padY) / ch);
            // apply small safety margin to avoid edge cut-offs
            s *= 0.95;
            s = Math.max(0.2, Math.min(5.0, s));
            scale[0] = s;
            applyScale.run();
            // Center the content so the full tree is visible
            Platform.runLater(() -> {
                try {
                    sp.setHvalue(0.5);
                    sp.setVvalue(0.5);
                } catch (Exception ignore) {}
            });
        };
        autoFit.setOnAction(e -> { updateAutoFitAppearance.run(); if (autoFit.isSelected()) fitToView.run(); });
        sp.viewportBoundsProperty().addListener((o, ov, nv) -> { if (autoFit.isSelected()) fitToView.run(); });
        content.boundsInLocalProperty().addListener((o, ov, nv) -> { if (autoFit.isSelected()) fitToView.run(); });

        controls.getChildren().addAll(zoomOut, zoomIn, autoFit);

        // Ctrl + mouse wheel zooming
        sp.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                if (e.getDeltaY() < 0) { // scroll down -> zoom out
                    autoFit.setSelected(false); updateAutoFitAppearance.run();
                    scale[0] = Math.max(0.2, scale[0] - 0.1);
                } else if (e.getDeltaY() > 0) { // scroll up -> zoom in
                    autoFit.setSelected(false); updateAutoFitAppearance.run();
                    scale[0] = Math.min(5.0, scale[0] + 0.1);
                }
                applyScale.run();
                e.consume();
            }
        });
        BorderPane root = new BorderPane(sp);
        BorderPane.setAlignment(controls, Pos.CENTER_RIGHT);
        root.setBottom(controls);
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        st.setScene(scene);
        st.show();
    }

    private void openCspTablePopout(){
        javafx.stage.Stage st = new javafx.stage.Stage();
        st.setTitle("CSP Domains (Pop-out)");
        TableView<CSPRow> table = buildCspTable();
        table.setItems(cspRows);
        table.setPrefWidth(520);
        table.setPrefHeight(600);
        javafx.scene.Scene sc = new javafx.scene.Scene(new javafx.scene.layout.StackPane(table), 560, 640);
        st.setScene(sc);
        st.show();
    }

    private void updateRecursionGraphViews(){
        if (recursionGraph != null) applyAssignGraphState(recursionGraph);
        for (GraphRenderer m : recursionGraphMirrors) applyAssignGraphState(m);
    }

    private void applyAssignGraphState(GraphRenderer gr){
        gr.setNodeLabels(assignLabels);
        gr.setGraph(nextAssignId, assignAdj);
        gr.setLayoutMode(GraphRenderer.LayoutMode.TREE);
        gr.clearHighlights();
        if (currentAssignId >= 0) gr.highlightCurrent(currentAssignId);
        // Apply per-node colors for assigned steps
        for (int i = 0; i < nextAssignId; i++) {
            Integer cidx = (i < assignColors.size()) ? assignColors.get(i) : null;
            if (cidx != null && cidx >= 0 && cidx < PALETTE.length) {
                gr.setNodeColor(i, PALETTE[cidx]);
            }
        }
    }
    // Local addEdge helper used by initDefaultGraph
    private void addEdge(int u, int v) {
        if (u == v) return;
        while (adj.size() <= Math.max(u, v)) adj.add(new ArrayList<>());
        if (!adj.get(u).contains(v)) adj.get(u).add(v);
        if (!adj.get(v).contains(u)) adj.get(v).add(u);
    }

    // --- CSP table row model ---
    public static class CSPRow {
        private final javafx.beans.property.IntegerProperty node = new javafx.beans.property.SimpleIntegerProperty();
        private final javafx.beans.property.IntegerProperty degree = new javafx.beans.property.SimpleIntegerProperty();
        private final javafx.beans.property.StringProperty domain = new javafx.beans.property.SimpleStringProperty();
        private java.util.Set<Integer> domainSet = new java.util.LinkedHashSet<>();
        public CSPRow(int node, int degree, String domain) { this.node.set(node); this.degree.set(degree); this.domain.set(domain); }
        public int getNode() { return node.get(); }
        public void setNode(int v) { node.set(v); }
        public javafx.beans.property.IntegerProperty nodeProperty() { return node; }
        public int getDegree() { return degree.get(); }
        public void setDegree(int v) { degree.set(v); }
        public javafx.beans.property.IntegerProperty degreeProperty() { return degree; }
        public String getDomain() { return domain.get(); }
        public void setDomain(String v) { domain.set(v); }
        public javafx.beans.property.StringProperty domainProperty() { return domain; }
        public java.util.Set<Integer> getDomainSet() { return domainSet; }
        public void setDomainSet(java.util.Set<Integer> s) { domainSet = s; }
    }
}
