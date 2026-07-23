package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.TowerOfHanoiSolver;
import com.algorithmvisualizer.algorithm.TowerOfHanoiSolver.StepPayload;
import com.algorithmvisualizer.algorithm.TowerOfHanoiSolver.StepType;
import com.algorithmvisualizer.visualization.TowerOfHanoiVisualizer;
import com.algorithmvisualizer.visualization.GraphRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.ArrayDeque;
import java.util.Deque;

public class TowerOfHanoiController implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;
    private final TowerOfHanoiSolver solver = new TowerOfHanoiSolver();
    private final TowerOfHanoiVisualizer visualizer = new TowerOfHanoiVisualizer();

    private Timeline timeline;
    private boolean isPlaying = false;
    private long stepDelayUntilMs = 0L;

    private int N = 4; // default
    private int moveCount = 0;

    // Recursion Graph using GraphRenderer (TREE layout)
    private GraphRenderer recursionGraph;
    private final Deque<Integer> callIdStack = new ArrayDeque<>();
    private final java.util.List<java.util.List<Integer>> callAdj = new java.util.ArrayList<>();
    private final java.util.List<String> callLabels = new java.util.ArrayList<>();
    private int nextCallId = 0;
    private int currentActiveCallId = -1;
    private final java.util.List<GraphRenderer> recursionGraphMirrors = new java.util.ArrayList<>();

    @FXML
    private void initialize(){
        solver.setStepListener(this::onStep);
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;

        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(visualizer.getNode());
        }

        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label hdr = new Label("Tower of Hanoi (Recursion)");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().addAll(hdr, new Separator());
        }
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            HBox row = new HBox(12.0);
            row.getChildren().addAll(new Label("Active disk highlighted"), new Label("Peg A | Peg B | Peg C"));
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Configure params: N spinner visible (3..8) and Apply button
        if (parent.paramSizeLabel != null) { parent.paramSizeLabel.setVisible(true); parent.paramSizeLabel.setManaged(true); parent.paramSizeLabel.setText("Disks (N)"); }
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true); parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 8, Math.min(8, Math.max(3, N))));
            parent.paramBoardSizeSpinner.valueProperty().addListener((o,ov,nv)-> { 
                N = Math.max(3, Math.min(8, nv)); 
                renderCode(); // Update code when N changes
            });
        }
        if (parent.paramElementsLabel != null) { parent.paramElementsLabel.setVisible(false); parent.paramElementsLabel.setManaged(false); }
        if (parent.paramElementsBox != null) { parent.paramElementsBox.setVisible(false); parent.paramElementsBox.setManaged(false); }
        if (parent.paramElementsField != null) { parent.paramElementsField.setVisible(false); parent.paramElementsField.setManaged(false); }
        if (parent.paramApplyButton != null) { parent.paramApplyButton.setVisible(true); parent.paramApplyButton.setManaged(true); parent.paramApplyButton.setText("Apply N"); parent.paramApplyButton.setOnAction(e -> applyN()); }
        if (parent.paramRandomizeButton != null) { 
            parent.paramRandomizeButton.setVisible(true); 
            parent.paramRandomizeButton.setManaged(true); 
            parent.paramRandomizeButton.setText("Solve"); 
            parent.paramRandomizeButton.setOnAction(e -> startSolve()); 
            // Duplicate Pop-out button next to Solve
            javafx.scene.Parent p = parent.paramRandomizeButton.getParent();
            if (p instanceof HBox) {
                Button pop2 = new Button("Pop-out Graph");
                pop2.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 10; -fx-cursor: hand;");
                pop2.setOnAction(e -> openGraphPopout());
                ((HBox)p).getChildren().add(pop2);
            }
        }

        // Solutions pane
        if (parent.solutionsHeaderBox != null) { parent.solutionsHeaderBox.setVisible(true); parent.solutionsHeaderBox.setManaged(true); parent.solutionsHeaderBox.getChildren().clear(); }
        if (parent.solutionsHeaderLabel != null) parent.solutionsHeaderLabel.setText("Recursion Graph");
        if (parent.solutionsSubHeaderBox != null) { parent.solutionsSubHeaderBox.setVisible(false); parent.solutionsSubHeaderBox.setManaged(false); }
        if (parent.solutionsContainer != null) { parent.solutionsContainer.setVisible(true); parent.solutionsContainer.setManaged(true); }
        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();
            recursionGraph = new GraphRenderer();
            recursionGraph.setLayoutMode(GraphRenderer.LayoutMode.TREE);
            // ToH-only visual tuning
            recursionGraph.setNodeRadius(24.0);
            recursionGraph.setTreeLeafSpacingEnabled(true);
            recursionGraph.setTreeMinLeafSpacing(90.0);
            // Header with pop-out button
            HBox hdr = new HBox(8.0);
            Label lbl = new Label("Recursion Graph (live)");
            lbl.setStyle("-fx-font-weight: bold;");
            Button pop = new Button("Pop-out");
            pop.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 10; -fx-cursor: hand;");
            pop.setOnAction(e -> openGraphPopout());
            hdr.getChildren().addAll(lbl, pop);
            VBox.setVgrow(recursionGraph.getNode(), Priority.ALWAYS);
            parent.solutionsContent.getChildren().addAll(hdr, recursionGraph.getNode());
        }

        // Hide entire speed control section for ToH
        if (parent.speedSlider != null) { parent.speedSlider.setVisible(false); parent.speedSlider.setManaged(false); }
        if (parent.speedLabel != null) { parent.speedLabel.setVisible(false); parent.speedLabel.setManaged(false); }
        // Hide the parent VBox containing "Playback Speed" label and slider
        if (parent.speedSlider != null && parent.speedSlider.getParent() != null) {
            javafx.scene.Node speedParent = parent.speedSlider.getParent();
            if (speedParent != null && speedParent.getParent() instanceof VBox) {
                VBox speedSection = (VBox) speedParent.getParent();
                speedSection.setVisible(false);
                speedSection.setManaged(false);
            }
        }
        if (parent.stepBackButton != null) parent.stepBackButton.setOnAction(e -> onStepBack());
        if (parent.resetButton != null) parent.resetButton.setOnAction(e -> onReset());
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready.");

        // Language selector listener - update code when language changes
        if (parent.languageSelector != null) {
            parent.languageSelector.valueProperty().addListener((obs, oldLang, newLang) -> {
                if (newLang != null && !newLang.equals(oldLang)) {
                    renderCode(); // Reload code in new language
                }
            });
        }

        parent.setCurrentAlgorithmName("Tower of Hanoi");
        renderCode();
        initVariables();

        // Prepare initial scene
        visualizer.prepare(N);
        clearRecursionViz();
    }

    private void startSolve(){
        stopTimeline();
        moveCount = 0;
        clearRecursionViz();
        visualizer.prepare(N);
        solver.queueSolve(N);
        autoPlayIfQueued();
    }

    private void applyN(){
        moveCount = 0;
        clearRecursionViz();
        visualizer.prepare(N);
        initVariables();
        if (parent!=null && parent.progressArea!=null){ parent.progressArea.clear(); parent.progressArea.appendText("Applied N="+N+"\n"); }
    }

    private void clearRecursionViz(){
        callIdStack.clear();
        callAdj.clear();
        callLabels.clear();
        nextCallId = 0;
        currentActiveCallId = -1;
        updateRecursionGraphViews();
    }

    private Node bullet(String text){
        Label lbl = new Label("• "+text);
        lbl.setStyle("-fx-text-fill: #2b4c7e; -fx-font-size: 12px;");
        return lbl;
    }

    private void onStep(StepType type, StepPayload p){
        long delayMs = 0L;
        switch (type){
            case INIT: break;
            case PREP: {
                int n = p.n != null ? p.n : N;
                visualizer.prepare(n);
                int total = (1<<n) - 1;
                log("🧰 Prepare N="+n+" (total moves="+total+")");
                break;
            }
            case CALL_START:
                onCallStart(p);
                log("↘ call T("+p.n+","+name(p.fromPeg)+"→"+name(p.toPeg)+") depth="+p.depth);
                delayMs = 120;
                break;
            case BASE_CASE:
                log("• base case n=1 at depth="+p.depth);
                delayMs = 120;
                break;
            case MOVE_PREP:
                delayMs = visualizer.onMovePrep(p.disk != null ? p.disk : p.n);
                int max = (1<<N) - 1;
                log("• move #"+p.moveIndex+"/"+max+": disk "+(p.disk!=null?p.disk:p.n)+" "+name(p.fromPeg)+"→"+name(p.toPeg));
                break;
            case LIFT_DISK:
                delayMs = visualizer.onLift(p.disk != null ? p.disk : p.n);
                log("  ↥ lift disk "+(p.disk!=null?p.disk:p.n));
                break;
            case MOVE_HORIZONTAL:
                delayMs = visualizer.onSlide(p.disk != null ? p.disk : p.n, p.toPeg);
                log("  → slide to peg "+name(p.toPeg));
                break;
            case DROP_DISK:
                delayMs = visualizer.onDrop(p.disk != null ? p.disk : p.n, p.toPeg);
                log("  ↧ drop on peg "+name(p.toPeg));
                break;
            case MOVE_COMMIT:
                visualizer.onCommit(p.disk != null ? p.disk : p.n, p.fromPeg, p.toPeg);
                moveCount++;
                updateVariables();
                log("  ✓ committed move #"+moveCount);
                delayMs = 150;
                break;
            case CALL_END:
                onCallEnd();
                log("↙ return depth="+p.depth);
                delayMs = 80;
                break;
            case DONE:
                log("✅ done");
                break;
            case ALERT:
                log("⚠ "+(p.message!=null?p.message:""));
                break;
        }
        stepDelayUntilMs = System.currentTimeMillis() + Math.max(0, delayMs);
    }

    private void onCallStart(StepPayload p){
        // create node
        int id = nextCallId++;
        ensureAdjCapacity(id+1);
        String label = "T("+p.n+","+name(p.fromPeg)+"→"+name(p.toPeg)+")";
        while (callLabels.size() < id+1) callLabels.add(null);
        callLabels.set(id, label);
        if (!callIdStack.isEmpty()){
            int parentId = callIdStack.peek();
            if (!callAdj.get(parentId).contains(id)) callAdj.get(parentId).add(id);
        }
        callIdStack.push(id);
        currentActiveCallId = id;
        // update all graph views
        updateRecursionGraphViews();
    }

    private void onCallEnd(){ 
        if (!callIdStack.isEmpty()) {
            callIdStack.pop();
            currentActiveCallId = callIdStack.isEmpty() ? -1 : callIdStack.peek();
            updateRecursionGraphViews();
        }
    }

    private static String name(Integer peg){ if (peg==null) return "-"; return peg==0?"A": peg==1?"B":"C"; }

    @Override public void onStepBack(){ /* optional for v1 */ }
    @Override public void onPlay(){ if (isPlaying){ onPause(); return; } if (!solver.hasPending()){ startSolve(); } isPlaying=true; rebuildTimelineWithCurrentSpeed(); if (timeline!=null) timeline.play(); if (parent!=null && parent.playButton!=null) parent.playButton.setText("⏸ Pause"); }
    @Override public void onPause(){ stopTimeline(); }
    @Override public void onStepForward(){ if (!solver.hasPending()) return; solver.step(); }
    @Override public void onReset(){ stopTimeline(); moveCount=0; clearRecursionViz(); visualizer.prepare(N); if (parent!=null) parent.stepDescription.setText("Ready."); updateVariables(); }

    private void stopTimeline(){ if (timeline!=null){ timeline.stop(); timeline=null; } isPlaying=false; if (parent!=null && parent.playButton!=null) parent.playButton.setText("▶ Play"); }
    private void rebuildTimelineWithCurrentSpeed(){ if (timeline!=null){ timeline.stop(); timeline=null; } double fps = 30.0; Duration frame = Duration.millis(1000.0 / fps); timeline = new Timeline(new KeyFrame(frame, e -> { if (!solver.hasPending()){ stopTimeline(); return; } long now = System.currentTimeMillis(); if (now < stepDelayUntilMs) return; solver.step(); })); timeline.setCycleCount(Animation.INDEFINITE); }

    private void autoPlayIfQueued(){ if (!isPlaying && solver.hasPending()) onPlay(); }

    private void initVariables(){ if (parent==null || parent.variableList==null) return; parent.variableList.getItems().clear(); parent.variableList.getItems().addAll("N: "+N, "moves: "+moveCount, "max moves: "+((1<<N)-1)); }
    private void updateVariables(){ if (parent==null || parent.variableList==null) return; parent.variableList.getItems().clear(); parent.variableList.getItems().addAll("N: "+N, "moves: "+moveCount, "max moves: "+((1<<N)-1)); }

    private void renderCode(){
        if (parent == null || parent.codeArea == null || parent.languageSelector == null) return;
        
        // Get the code implementation
        com.algorithmvisualizer.code.AlgorithmCode code = com.algorithmvisualizer.code.CodeRepository.getCode("Tower of Hanoi");
        if (code instanceof com.algorithmvisualizer.code.implementations.TowerOfHanoiCode) {
            com.algorithmvisualizer.code.implementations.TowerOfHanoiCode tohCode = 
                (com.algorithmvisualizer.code.implementations.TowerOfHanoiCode) code;
            tohCode.updateParameters(N);
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
    private void log(String line){ if (parent!=null && parent.progressArea!=null) parent.progressArea.appendText(line+"\n"); }

    private void ensureAdjCapacity(int n){
        while (callAdj.size() < n) callAdj.add(new java.util.ArrayList<>());
    }

    private void openGraphPopout(){
        Stage st = new Stage(); 
        st.setTitle("Recursion Graph (Pop-out)");
        GraphRenderer gr = new GraphRenderer();
        // ToH-only tuning mirrored
        gr.setLayoutMode(GraphRenderer.LayoutMode.TREE);
        gr.setNodeRadius(24.0);
        gr.setTreeLeafSpacingEnabled(true);
        gr.setTreeMinLeafSpacing(90.0);
        applyGraphState(gr);
        // Track for live sync
        recursionGraphMirrors.add(gr);
        st.setOnCloseRequest(e -> recursionGraphMirrors.remove(gr));

        ScrollPane sp = new ScrollPane(gr.getNode()); 
        sp.setPrefViewportWidth(800); 
        sp.setPrefViewportHeight(560); 
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); 
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        Scene scene = new Scene(sp);
        st.setScene(scene); 
        st.show();
    }

    private void updateRecursionGraphViews(){
        if (recursionGraph != null) applyGraphState(recursionGraph);
        for (GraphRenderer m : recursionGraphMirrors) applyGraphState(m);
    }

    private void applyGraphState(GraphRenderer gr){
        gr.setNodeLabels(callLabels);
        gr.setGraph(nextCallId, callAdj);
        gr.setLayoutMode(GraphRenderer.LayoutMode.TREE);
        gr.clearHighlights();
        if (currentActiveCallId >= 0) gr.highlightCurrent(currentActiveCallId);
    }
}
