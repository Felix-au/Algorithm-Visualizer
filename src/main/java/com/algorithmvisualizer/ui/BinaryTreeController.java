package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.BinaryTreeSolver;
import com.algorithmvisualizer.visualization.GraphRenderer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.*;

/**
 * Controller for Binary Tree (Concept).
 * Initial implementation: build from list, inorder traversal, height.
 */
public class BinaryTreeController implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;

    // Visuals
    private GraphRenderer graph;
    private final List<GraphRenderer> mirrors = new ArrayList<>();

    // Data for GraphRenderer
    private final List<List<Integer>> adj = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private int nodeCount = 0;

    // Solver
    private final BinaryTreeSolver solver = new BinaryTreeSolver();
    private Timeline timeline;
    private boolean isPlaying = false;
    // Step-back support similar to BFS
    private final java.util.Deque<Integer> progressHistory = new java.util.ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean suppressLogsDuringReplay = false;
    private final java.util.Deque<Integer> cursorHistory = new java.util.ArrayDeque<>();
    private Integer lastDepth = null;
    private Integer lastHeight = null;
    private Integer lastTreeHeight = null;

    // Results
    private FlowPane resultChips;
    private TextField keyField;

    @FXML
    private void initialize() {
        graph = new GraphRenderer();
        graph.setLayoutMode(GraphRenderer.LayoutMode.TREE);
        solver.setStepListener(this::onStep);
    }

    @Override
    public void setParentController(AlgorithmViewController parent) {
        this.parent = parent;
        parent.setCurrentAlgorithmName("Binary Tree");

        // Place graph in main visual area
        if (parent.chessboardContainer != null) {
            parent.chessboardContainer.getChildren().clear();
            parent.chessboardContainer.getChildren().add(graph.getNode());
        }

        // Header with title and pop-out
        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox.setVisible(true);
            parent.chessboardHeaderBox.setManaged(true);
            parent.chessboardHeaderBox.getChildren().clear();
            Label title = new Label("Binary Tree (Concept)");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Button pop = new Button("Pop-out Graph");
            pop.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 10; -fx-cursor: hand;");
            pop.setOnAction(e -> openPopout());
            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
            parent.chessboardHeaderBox.getChildren().addAll(title, spacer, pop);
        }

        // Legend
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:"); legendTitle.setStyle("-fx-font-weight: bold;");
            HBox row = new HBox(15.0);
            javafx.scene.shape.Circle cur = new javafx.scene.shape.Circle(6, javafx.scene.paint.Color.GOLD);
            cur.setStroke(javafx.scene.paint.Color.BLACK);
            HBox lCur = new HBox(5.0, cur, new Label("Current (yellow)"));
            javafx.scene.shape.Circle back = new javafx.scene.shape.Circle(6);
            back.setFill(javafx.scene.paint.Color.TRANSPARENT);
            back.setStroke(javafx.scene.paint.Color.CRIMSON);
            HBox lBack = new HBox(5.0, back, new Label("Backtrack pulse"));
            javafx.scene.shape.Circle err = new javafx.scene.shape.Circle(6, javafx.scene.paint.Color.CRIMSON);
            err.setStroke(javafx.scene.paint.Color.BLACK);
            HBox lErr = new HBox(5.0, err, new Label("Full/Blocked (red)"));
            row.getChildren().addAll(lCur, lBack, lErr);
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row);
        }

        // Parameters UI
        if (parent.paramSizeLabel != null) { parent.paramSizeLabel.setVisible(true); parent.paramSizeLabel.setManaged(true); parent.paramSizeLabel.setText("Node count"); }
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner.setVisible(true); parent.paramBoardSizeSpinner.setManaged(true);
            parent.paramBoardSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 7));
        }
        if (parent.paramElementsLabel != null) { parent.paramElementsLabel.setVisible(true); parent.paramElementsLabel.setManaged(true); parent.paramElementsLabel.setText("Numbers (comma/space)"); }
        if (parent.paramElementsBox != null) { parent.paramElementsBox.setVisible(false); parent.paramElementsBox.setManaged(false); }
        if (parent.paramElementsField != null) { parent.paramElementsField.setVisible(true); parent.paramElementsField.setManaged(true); parent.paramElementsField.setPromptText("e.g. 8, 3 10 1 6 14 4 7 13"); }

        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton.setVisible(true); parent.paramRandomizeButton.setManaged(true);
            parent.paramRandomizeButton.setText("Randomize");
            parent.paramRandomizeButton.setOnAction(e -> onRandomize());
            // Add Build button next to Randomize
            Node pn = parent.paramRandomizeButton.getParent();
            if (pn instanceof HBox) {
                HBox box = (HBox) pn;
                // Build rows inside a VBox placed next to Randomize button
                Button build = new Button("Build Tree");
                build.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 10; -fx-cursor: hand;");
                build.setOnAction(e2 -> onBuild());

                Button height = new Button("Height"); height.setOnAction(e2 -> onHeight());
                height.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 10; -fx-cursor: hand;");

                Button allTrav = new Button("All Traversals"); allTrav.setOnAction(e2 -> onAllTraversals());
                allTrav.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 10; -fx-cursor: hand;");

                Button inorder = new Button("Inorder"); inorder.setOnAction(e2 -> onInorder());
                Button preorder = new Button("Preorder"); preorder.setOnAction(e2 -> onPreorder());
                Button postorder = new Button("Postorder"); postorder.setOnAction(e2 -> onPostorder());
                Button level = new Button("Level Order"); level.setOnAction(e2 -> onLevel());
                for (Button b : new Button[]{inorder,preorder,postorder,level}){
                    b.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 10; -fx-cursor: hand;");
                }

                keyField = new TextField(); keyField.setPromptText("key"); keyField.setPrefWidth(80);
                Button insertBtn = new Button("Insert"); insertBtn.setOnAction(e2 -> onInsert()); insertBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 10; -fx-cursor: hand;");
                Button deleteBtn = new Button("Delete"); deleteBtn.setOnAction(e2 -> onDelete()); deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 10; -fx-cursor: hand;");

                // Move Randomize into row1
                box.getChildren().remove(parent.paramRandomizeButton);
                HBox row1 = new HBox(8.0, parent.paramRandomizeButton, build, height, allTrav);
                HBox row2 = new HBox(8.0, inorder, preorder, postorder, level);
                HBox row3 = new HBox(8.0, keyField, insertBtn, deleteBtn);
                VBox opsBox = new VBox(8.0, row1, row2, row3);

                // Place opsBox where Randomize used to be
                int idx = 0;
                box.getChildren().add(idx, opsBox);
            }
        }
        if (parent.paramApplyButton != null) { parent.paramApplyButton.setVisible(false); parent.paramApplyButton.setManaged(false); }

        // Solutions pane setup
        if (parent.solutionsHeaderBox != null) { parent.solutionsHeaderBox.setVisible(true); parent.solutionsHeaderBox.setManaged(true); parent.solutionsHeaderBox.getChildren().clear(); parent.solutionsHeaderLabel.setText("Results"); }
        if (parent.solutionsSubHeaderBox != null) { parent.solutionsSubHeaderBox.setVisible(false); parent.solutionsSubHeaderBox.setManaged(false); }
        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();
            Label resLbl = new Label("Output:"); resLbl.setStyle("-fx-font-weight: bold;");
            resultChips = new FlowPane(); resultChips.setHgap(6); resultChips.setVgap(6);
            parent.solutionsContent.getChildren().addAll(resLbl, resultChips);
        }

        // Playback
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }
        if (parent.playButton != null) parent.playButton.setOnAction(e -> onPlay());
        if (parent.resetButton != null) parent.resetButton.setOnAction(e -> onReset());
        if (parent.stepForwardButton != null) parent.stepForwardButton.setOnAction(e -> onStepForward());
        if (parent.stepBackButton != null) parent.stepBackButton.setOnAction(e -> onStepBack());
        if (parent.speedSlider != null) {
            parent.speedSlider.valueProperty().addListener((obs, ov, nv) -> {
                if (isPlaying) autoPlay();
            });
        }
        if (parent.stepBackButton != null) parent.stepBackButton.setDisable(true);

        clearGraph();
        // Populate Algorithm Code section with a standalone snippet
        if (parent.codeArea != null) {
            com.algorithmvisualizer.code.AlgorithmCode code = 
                com.algorithmvisualizer.code.CodeRepository.getCode("Binary Tree");
            if (code != null) {
                String selectedLang = parent.languageSelector != null ? 
                    parent.languageSelector.getValue() : "Java";
                String codeText = code.getCodeForLanguage(selectedLang);
                parent.codeArea.replaceText(0, parent.codeArea.getLength(), codeText);
            } else {
                parent.codeArea.replaceText(0, parent.codeArea.getLength(), "// Code not available");
            }
        }
        
        // Add language selector listener
        if (parent.languageSelector != null) {
            parent.languageSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
                com.algorithmvisualizer.code.AlgorithmCode code = 
                    com.algorithmvisualizer.code.CodeRepository.getCode("Binary Tree");
                if (code != null && parent.codeArea != null) {
                    // If tree has been built, use current values
                    List<Integer> currentKeys = parseKeys();
                    if (!currentKeys.isEmpty() && code instanceof com.algorithmvisualizer.code.implementations.BinaryTreeCode) {
                        int[] treeNodes = currentKeys.stream().mapToInt(Integer::intValue).toArray();
                        ((com.algorithmvisualizer.code.implementations.BinaryTreeCode) code).updateParameters(treeNodes);
                    }
                    String codeText = code.getCodeForLanguage(newVal);
                    parent.codeArea.replaceText(0, parent.codeArea.getLength(), codeText);
                }
            });
        }
    }

    private void onRandomize(){
        if (parent == null || parent.paramBoardSizeSpinner == null || parent.paramElementsField == null) return;
        int n = Math.max(1, Math.min(31, parent.paramBoardSizeSpinner.getValue()));
        Random r = new Random();
        Set<Integer> set = new LinkedHashSet<>();
        while (set.size() < n){ set.add(r.nextInt(100)); }
        parent.paramElementsField.setText(join(set));
        
        // Update code with randomized values
        List<Integer> randomKeys = new ArrayList<>(set);
        updateCodeWithCurrentValues(randomKeys);
    }

    private void onBuild(){
        List<Integer> keys = parseKeys();
        if (keys.isEmpty()) { appendLog("Provide numbers to build the tree"); return; }
        clearGraph(); clearResults(); lastDepth=null; lastHeight=null; lastTreeHeight=null;
        progressHistory.clear(); cursorHistory.clear();
        
        // Update code with current tree values
        updateCodeWithCurrentValues(keys);
        
        solver.queueBuild(keys);
        autoPlay();
    }

    private void onAllTraversals(){ if (!ensureBuilt()) return; clearResults(); lastDepth=null; lastHeight=null; lastTreeHeight=null; progressHistory.clear(); cursorHistory.clear(); solver.queueAllTraversals(); autoPlay(); }
    private void onInorder(){ if (!ensureBuilt()) return; clearResults(); lastDepth=null; lastHeight=null; lastTreeHeight=null; progressHistory.clear(); cursorHistory.clear(); solver.queueInorder(); autoPlay(); }
    private void onPreorder(){ if (!ensureBuilt()) return; clearResults(); lastDepth=null; lastHeight=null; lastTreeHeight=null; progressHistory.clear(); cursorHistory.clear(); solver.queuePreorder(); autoPlay(); }
    private void onPostorder(){ if (!ensureBuilt()) return; clearResults(); lastDepth=null; lastHeight=null; lastTreeHeight=null; progressHistory.clear(); cursorHistory.clear(); solver.queuePostorder(); autoPlay(); }
    private void onLevel(){ if (!ensureBuilt()) return; clearResults(); lastDepth=null; lastHeight=null; lastTreeHeight=null; progressHistory.clear(); cursorHistory.clear(); solver.queueLevelOrder(); autoPlay(); }
    private void onHeight(){ if (!ensureBuilt()) return; clearResults(); lastDepth=null; lastHeight=null; lastTreeHeight=null; progressHistory.clear(); cursorHistory.clear(); solver.queueHeight(); autoPlay(); }
    private void onInsert(){ Integer k = parseKeyField(); if (k==null) { appendLog("Enter a valid key for insert"); return; } clearResults(); lastDepth=null; lastHeight=null; lastTreeHeight=null; progressHistory.clear(); cursorHistory.clear(); solver.queueInsert(k); autoPlay(); }
    private void onDelete(){ Integer k = parseKeyField(); if (k==null) { appendLog("Enter a valid key for delete"); return; } clearResults(); lastDepth=null; lastHeight=null; lastTreeHeight=null; progressHistory.clear(); cursorHistory.clear(); solver.queueDelete(k); autoPlay(); }

    private boolean ensureBuilt(){ return true; }

    private void autoPlay(){
        stopTimeline(); isPlaying = true;
        double speed = (parent!=null && parent.speedSlider!=null) ? parent.speedSlider.getValue() : 5.0;
        long periodMs = (long)Math.max(100, 1200.0 / Math.max(1.0, speed));
        timeline = new Timeline(new KeyFrame(Duration.millis(periodMs), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        if (parent!=null && parent.playButton!=null) parent.playButton.setText("⏸ Pause");
    }
    private void tick(){
        int cur = solver.getCursor();
        cursorHistory.push(cur);
        if (!solver.step()){ stopTimeline(); if (parent!=null && parent.playButton!=null) parent.playButton.setText("▶ Play"); }
    }
    @Override public void onPlay(){ if (isPlaying){ stopTimeline(); } else { autoPlay(); } }
    @Override public void onReset(){ stopTimeline(); clearGraph(); clearResults(); progressHistory.clear(); cursorHistory.clear(); }
    @Override public void onStepForward(){ int cur = solver.getCursor(); cursorHistory.push(cur); if (!solver.step()) stopTimeline(); updateStepButtons(); }
    @Override public void onStepBack(){
        if (cursorHistory.isEmpty()) { updateStepButtons(); return; }
        int target = cursorHistory.pop();
        // Rebuild state up to target without duplicating logs
        stopTimeline();
        clearGraph();
        clearResults();
        suppressLogsDuringReplay = true;
        solver.replayTo(target);
        solver.setCursor(target);
        suppressLogsDuringReplay = false;
        // Remove last step's logs
        if (parent != null && parent.progressArea != null && !progressHistory.isEmpty()) {
            int toRemove = progressHistory.pop();
            removeLastLogLines(toRemove);
        }
        updateStepButtons();
    }
    private void removeLastLogLines(int count){
        if (parent == null || parent.progressArea == null || count <= 0) return;
        String text = parent.progressArea.getText(); if (text == null || text.isEmpty()) return;
        String[] lines = text.split("\n", -1);
        int total = lines.length;
        int newLen = Math.max(0, total - count);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < newLen; i++) sb.append(lines[i]).append("\n");
        parent.progressArea.setText(sb.toString());
    }
    private void updateStepButtons(){ if (parent!=null && parent.stepBackButton!=null) parent.stepBackButton.setDisable(solver.getCursor()<=0); }
    private void stopTimeline(){ if (timeline!=null) timeline.stop(); timeline=null; isPlaying=false; }

    private void onStep(BinaryTreeSolver.StepType t, BinaryTreeSolver.StepPayload p){
        // count one log line per event (for step-back removal), unless replaying
        currentStepLogLines = 0;
        updateVariables(t, p);
        appendStepLog(t, p);
        updateCurrentStep(t, p);
        switch (t){
            case INIT: case START_BUILD: case RELAYOUT: updateGraph(); break;
            case START_TRAVERSAL: if (!resultChips.getChildren().isEmpty()) addTraversalBreak(); if (p.message!=null) addSectionHeader(p.message); updateGraph(); break;
            case COMPARE_AT: if (p.nodeId!=null){ graph.clearHighlights(); graph.highlightCurrent(p.nodeId); } break;
            case MOVE_LEFT: case MOVE_RIGHT: /* optional edge highlight */ break;
            case ATTACH_NODE: onAttach(p); break;
            case LINK_EDGE: onLink(p); break;
            case VISIT: if (p.nodeId!=null){ graph.clearHighlights(); graph.highlightCurrent(p.nodeId); } break;
            case BACKTRACK: if (p.nodeId!=null){
                if (p.message != null && p.message.toLowerCase().contains("full")) {
                    graph.highlightError(p.nodeId);
                } else {
                    graph.flashBacktrackNode(p.nodeId);
                }
            } break;
            case OUTPUT_APPEND: if (p.key!=null) addChip(String.valueOf(p.key)); else if (p.message!=null) { if (!p.message.startsWith("height@") && !p.message.startsWith("treeHeight@")) addChip(p.message); } break;
            case UPDATE_NODE_KEY: if (p.nodeId!=null && p.key!=null){ ensureCapacity(p.nodeId+1); labels.set(p.nodeId, String.valueOf(p.key)); updateGraph(); } break;
            case UNLINK_EDGE: if (p.parentId!=null && p.nodeId!=null){ ensureCapacity(Math.max(p.parentId, p.nodeId)+1); adj.get(p.parentId).remove((Integer)p.nodeId); updateGraph(); } break;
            case REMOVE_NODE: if (p.nodeId!=null){ ensureCapacity(p.nodeId+1); labels.set(p.nodeId, ""); // leave node detached, hide text
                // remove any edges from this node
                if (p.nodeId < adj.size()) adj.get(p.nodeId).clear();
                // remove incoming edges
                for (List<Integer> row : adj) row.remove((Integer)p.nodeId);
                updateGraph();
            } break;
            case HEIGHT_COMPUTE: /* optional */ break;
            case ALERT: appendLog(p.message); break;
            case DONE:
                // Operation finished: clear error and standard highlights (skip during replay)
                if (!suppressLogsDuringReplay){
                    graph.clearErrors();
                    graph.clearHighlights();
                    updateGraph();
                }
                break;
        }
        // finalize log group unless we are replaying
        if (!suppressLogsDuringReplay) {
            progressHistory.push(Math.max(1, currentStepLogLines));
        }
        updateStepButtons();
    }

    private void onAttach(BinaryTreeSolver.StepPayload p){
        int id = p.nodeId != null ? p.nodeId : 0;
        ensureCapacity(id+1);
        labels.set(id, String.valueOf(p.key));
        nodeCount = Math.max(nodeCount, id+1);
        updateGraph();
    }

    private void onLink(BinaryTreeSolver.StepPayload p){
        if (p.parentId == null || p.nodeId == null) return;
        ensureCapacity(Math.max(p.parentId, p.nodeId)+1);
        if (!adj.get(p.parentId).contains(p.nodeId)) adj.get(p.parentId).add(p.nodeId);
        updateGraph();
    }

    private void ensureCapacity(int n){
        while (labels.size() < n) labels.add(null);
        while (adj.size() < n) adj.add(new ArrayList<>());
    }

    private void updateGraph(){
        // trim nodeCount to last meaningful node (has label text or any incident edge)
        int n = Math.max(nodeCount, Math.max(labels.size(), adj.size()));
        // ensure capacity
        ensureCapacity(n);
        boolean[] hasIncoming = new boolean[n];
        for (int i = 0; i < n; i++){
            for (Integer v : adj.get(i)){
                if (v != null && v >= 0 && v < n) hasIncoming[v] = true;
            }
        }
        int last = -1;
        for (int i = 0; i < n; i++){
            String lab = (i < labels.size()) ? labels.get(i) : null;
            boolean hasOut = (i < adj.size()) && !adj.get(i).isEmpty();
            boolean keep = (lab != null && !lab.isEmpty()) || hasOut || hasIncoming[i];
            if (keep) last = i;
        }
        int effectiveCount = Math.max(0, last + 1);
        graph.setNodeLabels(labels);
        graph.setGraph(effectiveCount, adj);
        graph.setLayoutMode(GraphRenderer.LayoutMode.TREE);
        // mirrors
        for (GraphRenderer m : mirrors){ m.setNodeLabels(labels); m.setGraph(effectiveCount, adj); m.setLayoutMode(GraphRenderer.LayoutMode.TREE); }
    }

    private void clearGraph(){ adj.clear(); labels.clear(); nodeCount=0; updateGraph(); }
    private boolean truncated=false; private int hiddenCount=0; private Label truncateLabel=null; private static final int MAX_CHIPS=200;
    private void clearResults(){ if (resultChips!=null) { resultChips.getChildren().clear(); } truncated=false; hiddenCount=0; truncateLabel=null; }

    private void addChip(String s){
        if (resultChips==null) return;
        if (truncated){
            hiddenCount++;
            updateTruncateLabel();
            return;
        }
        if (resultChips.getChildren().size() >= MAX_CHIPS){
            truncated = true; hiddenCount = 1;
            truncateLabel = new Label("… +"+hiddenCount+" more");
            truncateLabel.setStyle("-fx-background-color: #fee2e2; -fx-padding: 4 8; -fx-background-radius: 10; -fx-text-fill: #991b1b; -fx-font-weight: bold;");
            resultChips.getChildren().add(truncateLabel);
            return;
        }
        Label chip = new Label(s);
        chip.setStyle("-fx-background-color: #e2e8f0; -fx-padding: 4 8; -fx-background-radius: 10; -fx-text-fill: #1f2937; -fx-font-weight: bold;");
        resultChips.getChildren().add(chip);
    }

    private void updateTruncateLabel(){ if (truncateLabel!=null) truncateLabel.setText("… +"+hiddenCount+" more"); }

    private void addSectionHeader(String s){
        Label head = new Label(s);
        head.setStyle("-fx-text-fill: #111827; -fx-font-weight: bold; -fx-background-color: #dbeafe; -fx-padding: 4 8; -fx-background-radius: 6;");
        resultChips.getChildren().add(head);
    }

    private void addTraversalBreak(){
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator(javafx.geometry.Orientation.HORIZONTAL);
        sep.setPrefWidth(4000);
        resultChips.getChildren().add(sep);
    }

    private void appendLog(String line){ if (parent!=null && parent.progressArea!=null) parent.progressArea.appendText(line+"\n"); }

    private void appendStepLog(BinaryTreeSolver.StepType t, BinaryTreeSolver.StepPayload p){
        if (suppressLogsDuringReplay) return;
        String text = toHumanText(t, p);
        appendLog(text);
        currentStepLogLines++;
    }

    private void updateVariables(BinaryTreeSolver.StepType t, BinaryTreeSolver.StepPayload p){
        if (parent == null || parent.variableList == null) return;
        java.util.List<String> items = new java.util.ArrayList<>();
        items.add("event="+t.name());
        // constants
        Integer rootId = solver.getRootId(); Integer rootKey = solver.getRootKey();
        if (rootKey != null) items.add("root="+rootKey + (rootId!=null?" (#"+rootId+")":""));
        if (p != null){
            if (p.message != null) {
                if (p.message.startsWith("height@")) {
                    try { lastHeight = Integer.parseInt(p.message.substring("height@".length())); } catch(Exception ignore) {}
                } else if (p.message.startsWith("treeHeight@")) {
                    try { lastTreeHeight = Integer.parseInt(p.message.substring("treeHeight@".length())); } catch(Exception ignore) {}
                } else if (p.message.startsWith("height=")) {
                    try { lastTreeHeight = Integer.parseInt(p.message.substring("height=".length())); } catch(Exception ignore) {}
                } else {
                    items.add("msg="+p.message);
                }
            }
            if (p.key != null) items.add("key="+p.key);
            if (p.nodeId != null) items.add("node="+p.nodeId);
            if (p.parentId != null) items.add("parent="+p.parentId);
            if (p.leftId != null) items.add("left="+p.leftId);
            if (p.rightId != null) items.add("right="+p.rightId);
            if (p.depth != null) lastDepth = p.depth;
        }
        if (lastDepth != null) items.add("depth="+lastDepth);
        if (lastHeight != null) items.add("height="+lastHeight);
        if (lastTreeHeight != null) items.add("treeHeight="+lastTreeHeight);
        if (p != null && p.direction != null) items.add("dir="+(p.direction==-1?"left":(p.direction==1?"right":p.direction.toString())));
        parent.variableList.getItems().setAll(items);
    }

    private void updateCurrentStep(BinaryTreeSolver.StepType t, BinaryTreeSolver.StepPayload p){
        if (parent == null || parent.stepDescription == null) return;
        parent.stepDescription.setText(toHumanText(t, p));
    }

    private String toHumanText(BinaryTreeSolver.StepType t, BinaryTreeSolver.StepPayload p){
        String msg = (p!=null && p.message!=null)? p.message : "";
        String dir = (p!=null && p.direction!=null)? (p.direction==-1?"left":"right") : null;
        Integer key = (p!=null)? p.key : null;
        Integer node = (p!=null)? p.nodeId : null;
        Integer parentId = (p!=null)? p.parentId : null;
        switch (t){
            case START_BUILD: return "Starting level-wise build";
            case ATTACH_NODE: return "Attach node "+key+ (parentId!=null?" under parent #"+parentId:"")+ (msg.isEmpty()?"":" ("+msg+")");
            case LINK_EDGE: return "Link "+(dir!=null?dir+" ":"")+"edge from parent #"+parentId+" to node #"+node;
            case COMPARE_AT: return "At node #"+node+ (key!=null?" (key="+key+")":"")+ (msg.isEmpty()?"":" — "+msg);
            case MOVE_LEFT: return "Move left from node #"+node+ (msg.isEmpty()?"":" — "+msg);
            case MOVE_RIGHT: return "Move right from node #"+node+ (msg.isEmpty()?"":" — "+msg);
            case VISIT: return "Visit node #"+node+ (key!=null?" (key="+key+")":"");
            case BACKTRACK:
                if (msg.toLowerCase().contains("full")) return "Parent node #"+node+" is full; marking as blocked";
                return "Backtrack from node #"+node+ (msg.isEmpty()?"":" — "+msg);
            case UPDATE_NODE_KEY: return "Replace target node #"+node+" key with "+key;
            case UNLINK_EDGE: return "Unlink edge from parent #"+parentId+" to node #"+node;
            case REMOVE_NODE: return "Remove node #"+node;
            case RELAYOUT: return "Recalculate layout";
            case START_TRAVERSAL: return (msg.isEmpty()?"Start traversal":msg);
            case OUTPUT_APPEND: return (msg!=null?msg:"Output node key " + key);
            case HEIGHT_COMPUTE: return "Compute height with backtracking";
            case ALERT: return (msg.isEmpty()?"Alert":msg);
            case DONE: return "Done";
            case INIT: default: return msg.isEmpty()? t.name() : msg;
        }
    }

    private Integer parseKeyField(){ if (keyField==null) return null; try { return Integer.parseInt(keyField.getText().trim()); } catch(Exception e){ return null; } }

    private List<Integer> parseKeys(){
        if (parent==null || parent.paramElementsField==null) return Collections.emptyList();
        String txt = parent.paramElementsField.getText(); if (txt==null) return Collections.emptyList();
        List<Integer> out = new ArrayList<>();
        for (String part : txt.replace(',', ' ').split("\\s+")){
            if (part.isBlank()) continue;
            try { out.add(Integer.parseInt(part.trim())); } catch(Exception ignore) {}
        }
        return out;
    }

    private String join(Collection<Integer> vals){ StringBuilder sb = new StringBuilder(); boolean first=true; for (Integer v: vals){ if (!first) sb.append(", "); first=false; sb.append(v); } return sb.toString(); }

    private void updateCodeWithCurrentValues(List<Integer> keys) {
        if (parent == null || parent.codeArea == null) return;
        
        // Get the code from repository
        com.algorithmvisualizer.code.AlgorithmCode code = 
            com.algorithmvisualizer.code.CodeRepository.getCode("Binary Tree");
        
        if (code instanceof com.algorithmvisualizer.code.implementations.BinaryTreeCode) {
            // Convert List<Integer> to int[]
            int[] treeNodes = keys.stream().mapToInt(Integer::intValue).toArray();
            
            // Update parameters
            ((com.algorithmvisualizer.code.implementations.BinaryTreeCode) code).updateParameters(treeNodes);
            
            // Refresh the displayed code
            String selectedLang = parent.languageSelector != null ? 
                parent.languageSelector.getValue() : "Java";
            String codeText = code.getCodeForLanguage(selectedLang);
            parent.codeArea.replaceText(0, parent.codeArea.getLength(), codeText);
        }
    }

    private void openPopout(){
        GraphRenderer gr = new GraphRenderer();
        gr.setLayoutMode(GraphRenderer.LayoutMode.TREE);
        mirrors.add(gr);
        updateGraph();
        ScrollPane sp = new ScrollPane(gr.getNode());
        sp.setPrefViewportWidth(800); sp.setPrefViewportHeight(560);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        javafx.stage.Stage st = new javafx.stage.Stage(); st.setTitle("Binary Tree (Pop-out)"); st.setScene(new javafx.scene.Scene(sp));
        st.setOnCloseRequest(e -> mirrors.remove(gr));
        st.show();
    }
}
