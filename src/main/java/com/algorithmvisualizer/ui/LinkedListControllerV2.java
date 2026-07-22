package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.LinkedListSolverV2;
import com.algorithmvisualizer.algorithm.LinkedListSolverV2.ListType;
import com.algorithmvisualizer.algorithm.LinkedListSolverV2.StepPayload;
import com.algorithmvisualizer.algorithm.LinkedListSolverV2.StepType;
import com.algorithmvisualizer.visualization.LinkedListVisualizerV2;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import javafx.geometry.Pos;

import java.util.ArrayDeque;
import java.util.Deque;

public class LinkedListControllerV2 implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;
    private LinkedListSolverV2 solver;
    private LinkedListVisualizerV2 visualizer;

    private Timeline timeline;
    private boolean isPlaying = false;
    private long stepDelayUntilMs = 0L;

    private final Deque<LinkedListSolverV2.State> history = new ArrayDeque<>();

    private final Deque<Integer> progressHistory = new ArrayDeque<>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;
    private TextField indexField;
    private TextField afterValueField;

    @FXML
    private void initialize(){
        solver = new LinkedListSolverV2();
        visualizer = new LinkedListVisualizerV2();
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
            Label hdr = new Label("Linked List (V2) - Singly");
            hdr.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            parent.chessboardHeaderBox.getChildren().addAll(hdr, new Separator());
        }
        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox.setVisible(true);
            parent.chessboardLegendBox.setManaged(true);
            parent.chessboardLegendBox.getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle("-fx-font-weight: bold;");
            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(12.0);
            javafx.scene.shape.Rectangle value = new javafx.scene.shape.Rectangle(14,14, javafx.scene.paint.Color.web("#4a90e2")); value.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.shape.Rectangle compare = new javafx.scene.shape.Rectangle(14,14, javafx.scene.paint.Color.web("#f39c12")); compare.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.shape.Rectangle found = new javafx.scene.shape.Rectangle(14,14, javafx.scene.paint.Color.web("#27ae60")); found.setStroke(javafx.scene.paint.Color.BLACK);
            javafx.scene.layout.HBox row2 = new javafx.scene.layout.HBox(12.0);
            javafx.scene.shape.Line linkCmp = new javafx.scene.shape.Line(0,0,24,0); linkCmp.setStroke(javafx.scene.paint.Color.web("#f39c12")); linkCmp.setStrokeWidth(3.0);
            javafx.scene.shape.Line linkFnd = new javafx.scene.shape.Line(0,0,24,0); linkFnd.setStroke(javafx.scene.paint.Color.web("#27ae60")); linkFnd.setStrokeWidth(3.0);
            row.getChildren().addAll(value, new Label("Node"), compare, new Label("Compare"), found, new Label("Found"));
            row2.getChildren().addAll(linkCmp, new Label("Link compare"), linkFnd, new Label("Link found"));
            parent.chessboardLegendBox.getChildren().addAll(legendTitle, row, row2);
        }

        if (parent.paramSizeLabel != null) { parent.paramSizeLabel.setVisible(false); parent.paramSizeLabel.setManaged(false); }
        if (parent.paramBoardSizeSpinner != null) { parent.paramBoardSizeSpinner.setVisible(false); parent.paramBoardSizeSpinner.setManaged(false); }
        if (parent.paramElementsLabel != null) { parent.paramElementsLabel.setVisible(false); parent.paramElementsLabel.setManaged(false); }
        if (parent.paramElementsBox != null) { parent.paramElementsBox.setVisible(false); parent.paramElementsBox.setManaged(false); }
        if (parent.paramElementsField != null) { parent.paramElementsField.setVisible(true); parent.paramElementsField.setManaged(true); parent.paramElementsField.setPromptText("Value"); parent.paramElementsField.setPrefWidth(96); parent.paramElementsField.setMinWidth(96); parent.paramElementsField.setMaxWidth(96); parent.paramElementsField.setStyle("-fx-background-color: white; -fx-background-insets: 0; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 6 8; -fx-font-size: 12px;"); parent.paramElementsField.setOnAction(e -> { doInsertEnd(); parent.paramElementsField.clear(); }); }
        if (parent.paramRandomizeButton != null) { parent.paramRandomizeButton.setVisible(false); parent.paramRandomizeButton.setManaged(false); }
        if (parent.paramApplyButton != null) { parent.paramApplyButton.setText("Clear"); parent.paramApplyButton.setOnAction(e -> doClear()); }

        addOperationsRow();

        if (parent.speedSlider != null) parent.speedSlider.valueProperty().addListener((obs,o,n)-> updatePlaybackSpeed());
        if (parent.stepBackButton != null) parent.stepBackButton.setOnAction(e -> onStepBack());
        if (parent.resetButton != null) parent.resetButton.setOnAction(e -> onReset());
        if (parent.pauseButton != null) { parent.pauseButton.setVisible(false); parent.pauseButton.setManaged(false); }

        if (parent.solutionsHeaderBox != null) { parent.solutionsHeaderBox.setVisible(true); parent.solutionsHeaderBox.setManaged(true); }
        if (parent.solutionsHeaderLabel != null) parent.solutionsHeaderLabel.setText("Linked List Concepts");
        if (parent.solutionsSubHeaderBox != null) { parent.solutionsSubHeaderBox.setVisible(false); parent.solutionsSubHeaderBox.setManaged(false); }
        if (parent.solutionsContainer != null) { parent.solutionsContainer.setVisible(true); parent.solutionsContainer.setManaged(true); }
        if (parent.solutionsContent != null) {
            parent.solutionsContent.getChildren().clear();
            javafx.scene.layout.VBox bullets = new javafx.scene.layout.VBox(6.0);
            bullets.setStyle("-fx-padding: 4;");
            bullets.getChildren().addAll(
                bullet("Linked List: linear structure of nodes; each node = [data | next]"),
                bullet("Head points to first node; tail.next = null (for SLL)"),
                bullet("Insert: start O(1), end O(1) with tail else O(n), at index O(n), after value O(n)"),
                bullet("Delete: start O(1), end O(n) unless prev tracked, at index/by value O(n)"),
                bullet("Traverse/Search: O(n) via following next pointers"),
                bullet("Stable node IDs bind logic ↔ UI; dragging is freeform and does not change order"),
                bullet("Breaking a next link loses access to the rest of the chain"),
                bullet("Use-cases: adjacency lists, LRU lists, hashing with chaining, schedulers")
            );
            parent.solutionsContent.getChildren().add(bullets);
        }
        if (parent.codeArea != null) {
            parent.codeArea.replaceText(0, parent.codeArea.getLength(), "Paste your linked list algorithm code here to show in this panel.\n\n// Provide the snippet and I will update this panel.");
        }

        initProgressLog();
        updateVariables();
        parent.setCurrentAlgorithmName("Linked List");
        renderCode();
        
        // Add language selector listener
        if (parent.languageSelector != null) {
            parent.languageSelector.valueProperty().addListener((obs, oldVal, newVal) -> renderCode());
        }
        
        if (parent.stepDescription != null) parent.stepDescription.setText("Ready.");

        // initial sync
        syncVisualizer();
    }

    private void addOperationsRow(){
        if (parent == null || parent.paramElementsField == null) return;
        javafx.scene.Parent container = parent.paramElementsField.getParent();
        javafx.scene.layout.VBox vbox;
        if (container instanceof javafx.scene.layout.VBox) vbox = (javafx.scene.layout.VBox) container; else if (container != null && container.getParent() instanceof javafx.scene.layout.VBox) vbox = (javafx.scene.layout.VBox) container.getParent(); else return;

        javafx.scene.layout.VBox opsBox = new javafx.scene.layout.VBox(6.0);
        javafx.scene.layout.HBox inputs = new javafx.scene.layout.HBox(8.0);
        javafx.scene.layout.HBox row1 = new javafx.scene.layout.HBox(8.0);
        javafx.scene.layout.HBox row2 = new javafx.scene.layout.HBox(8.0);
        javafx.scene.layout.HBox row3 = new javafx.scene.layout.HBox(8.0);
        opsBox.setStyle("-fx-padding: 4 0 0 0;");

        Button insStart = makeButton("Start", "#4a90e2");
        Button insEnd = makeButton("End", "#4a90e2");
        Button delStart = makeButton("Start", "#e74c3c");
        Button delEnd = makeButton("End", "#e74c3c");
        Button traverse = makeButton("Traversal", "#7b68ee");
        Button search = makeButton("Search", "#f39c12");

        Button insAtIdx = makeButton("Index", "#4a90e2");
        Button insAfterVal = makeButton("After", "#4a90e2");
        Button delAtIdx = makeButton("Index", "#e74c3c");
        Button delByVal = makeButton("Value", "#e74c3c");

        insStart.setOnAction(e -> doInsertStart());
        insEnd.setOnAction(e -> doInsertEnd());
        delStart.setOnAction(e -> doDeleteStart());
        delEnd.setOnAction(e -> doDeleteEnd());
        traverse.setOnAction(e -> doTraverse());
        search.setOnAction(e -> doSearch());

        insAtIdx.setOnAction(e -> doInsertAtIndex());
        insAfterVal.setOnAction(e -> doInsertAfterValue());
        delAtIdx.setOnAction(e -> doDeleteAtIndex());
        delByVal.setOnAction(e -> doDeleteByValue());

        indexField = new TextField(); indexField.setPromptText("Index"); indexField.setPrefWidth(96); indexField.setMinWidth(96); indexField.setMaxWidth(96); indexField.setStyle("-fx-background-color: white; -fx-background-insets: 0; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 6 8; -fx-font-size: 12px;");
        afterValueField = new TextField(); afterValueField.setPromptText("After value"); afterValueField.setPrefWidth(96); afterValueField.setMinWidth(96); afterValueField.setMaxWidth(96); afterValueField.setStyle("-fx-background-color: white; -fx-background-insets: 0; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-padding: 6 8; -fx-font-size: 12px;");
        javafx.scene.control.Label valLbl = new javafx.scene.control.Label("Value:");
        // move the original value field into this single inputs row
        if (parent.paramElementsField.getParent() instanceof javafx.scene.layout.Pane) {
            ((javafx.scene.layout.Pane) parent.paramElementsField.getParent()).getChildren().remove(parent.paramElementsField);
        }
        inputs.getChildren().addAll(valLbl, parent.paramElementsField, new Label("Index:"), indexField, new Label("After:"), afterValueField);

        javafx.scene.control.Label insertLbl = new javafx.scene.control.Label("Insert:"); insertLbl.setStyle("-fx-font-weight: bold;");
        javafx.scene.control.Label deleteLbl = new javafx.scene.control.Label("Delete:"); deleteLbl.setStyle("-fx-font-weight: bold;");
        javafx.scene.control.Label otherLbl = new javafx.scene.control.Label("Rest:"); otherLbl.setStyle("-fx-font-weight: bold;");

        row1.getChildren().addAll(insertLbl, insStart, insEnd, insAtIdx, insAfterVal);
        if (parent.paramApplyButton != null) {
            javafx.scene.Parent applyParent = parent.paramApplyButton.getParent();
            if (applyParent instanceof javafx.scene.layout.HBox) {
                ((javafx.scene.layout.HBox) applyParent).getChildren().remove(parent.paramApplyButton);
                vbox.getChildren().remove(applyParent);
            }
            parent.paramApplyButton.setText("Clear");
            parent.paramApplyButton.setOnAction(e -> doClear());
            row3.getChildren().addAll(otherLbl, traverse, search, parent.paramApplyButton);
        } else {
            // Fallback Clear button only if paramApplyButton is missing
            Button clear = makeButton("Clear", "#95a5a6");
            clear.setOnAction(e -> doClear());
            row3.getChildren().addAll(otherLbl, traverse, search, clear);
        }

        row2.getChildren().addAll(deleteLbl, delStart, delEnd, delAtIdx, delByVal);

        opsBox.getChildren().addAll(inputs, row1, row2, row3);
        vbox.getChildren().add(opsBox);
    }

    private javafx.scene.Node bullet(String text){
        javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(3, javafx.scene.paint.Color.web("#2b4c7e"));
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(text);
        lbl.setStyle("-fx-text-fill: #2b4c7e; -fx-font-size: 12px;");
        lbl.setWrapText(true);
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(8.0, dot, lbl);
        row.setFillHeight(true);
        return row;
    }

    private Button makeButton(String text, String color){ Button b = new Button(text); b.setStyle("-fx-background-color: "+color+"; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;"); return b; }

    private Integer parseValue(){ if (parent==null || parent.paramElementsField==null) return null; try { return Integer.parseInt(parent.paramElementsField.getText().trim()); } catch (Exception ex){ return null; } }
    private Integer parseIndex(){ try { return indexField==null? null : Integer.parseInt(indexField.getText().trim()); } catch(Exception ex){ return null; } }
    private Integer parseAfterValue(){ try { return afterValueField==null? null : Integer.parseInt(afterValueField.getText().trim()); } catch(Exception ex){ return null; } }

    private void doInsertStart(){ Integer v = parseValue(); if (v==null){ appendProgress("⚠ Enter a number to Insert."); return; } solver.queueInsertStart(v); autoPlayIfQueued(); }
    private void doInsertEnd(){ Integer v = parseValue(); if (v==null){ appendProgress("⚠ Enter a number to Insert."); return; } solver.queueInsertEnd(v); autoPlayIfQueued(); }
    private void doDeleteStart(){ solver.queueDeleteStart(); autoPlayIfQueued(); }
    private void doDeleteEnd(){ solver.queueDeleteEnd(); autoPlayIfQueued(); }
    private void doTraverse(){ solver.queueTraverse(); autoPlayIfQueued(); }
    private void doSearch(){ Integer v = parseValue(); if (v==null){ appendProgress("⚠ Enter a number to Search."); return; } solver.queueSearch(v); autoPlayIfQueued(); }
    private void doClear(){ solver.queueClear(); autoPlayIfQueued(); }
    private void doInsertAtIndex(){ Integer idx = parseIndex(); Integer v = parseValue(); if (idx==null || v==null){ appendProgress("⚠ Provide Index and Value."); return; } solver.queueInsertAtIndex(idx, v); autoPlayIfQueued(); }
    private void doInsertAfterValue(){ Integer after = parseAfterValue(); Integer v = parseValue(); if (after==null || v==null){ appendProgress("⚠ Provide After-value and Value."); return; } solver.queueInsertAfterValue(after, v); autoPlayIfQueued(); }
    private void doDeleteAtIndex(){ Integer idx = parseIndex(); if (idx==null){ appendProgress("⚠ Provide Index."); return; } solver.queueDeleteAtIndex(idx); autoPlayIfQueued(); }
    private void doDeleteByValue(){ Integer v = parseValue(); if (v==null){ appendProgress("⚠ Provide Value."); return; } solver.queueDeleteByValue(v); autoPlayIfQueued(); }

    @Override public void onStepBack(){ if (isPlaying) onPause(); if (history.isEmpty()) return; LinkedListSolverV2.State s = history.pop(); solver.restore(s); syncVisualizer(); if (parent!=null && parent.progressArea!=null && !progressHistory.isEmpty()){ int toRemove = progressHistory.pop(); removeLastLogLines(toRemove); } if (parent!=null && parent.stepDescription!=null) parent.stepDescription.setText("Stepped back"); }
    @Override public void onPlay(){ if (isPlaying){ onPause(); return; } if (!solver.hasPending()){ if (parent!=null) parent.stepDescription.setText("No queued steps."); return; } isPlaying=true; rebuildTimelineWithCurrentSpeed(); if (timeline!=null) timeline.play(); if (parent!=null && parent.playButton!=null) parent.playButton.setText("⏸ Pause"); }
    @Override public void onPause(){ stopTimeline(); }
    @Override public void onStepForward(){ if (!solver.hasPending()) return; history.push(solver.snapshot()); solver.step(); }
    @Override public void onReset(){ history.clear(); solver = new LinkedListSolverV2(); solver.setStepListener(this::onStep); syncVisualizer(); initProgressLog(); updateVariables(); if (parent!=null) parent.stepDescription.setText("Ready."); }

    private void stopTimeline(){ if (timeline!=null){ timeline.stop(); timeline=null; } isPlaying=false; if (parent!=null && parent.playButton!=null) parent.playButton.setText("▶ Play"); }
    private void updatePlaybackSpeed(){ if (!isPlaying) return; rebuildTimelineWithCurrentSpeed(); if (timeline!=null) timeline.play(); }
    private void rebuildTimelineWithCurrentSpeed(){ if (timeline!=null){ timeline.stop(); timeline=null; } double speed = parent!=null && parent.speedSlider!=null ? parent.speedSlider.getValue() : 1.0; double fps = Math.max(1.0, speed); Duration frame = Duration.millis(1000.0 / fps); timeline = new Timeline(new KeyFrame(frame, e -> { if (!solver.hasPending()){ stopTimeline(); return; } long now = System.currentTimeMillis(); if (now < stepDelayUntilMs) return; history.push(solver.snapshot()); solver.step(); })); timeline.setCycleCount(Animation.INDEFINITE); }

    private void initProgressLog(){ if (parent==null || parent.progressArea==null) return; parent.progressArea.clear(); progressHistory.clear(); currentStepLogLines=0; countLogsForStep=false; appendProgress("Linked List V2 ready."); }
    private void appendProgress(String line){ if (parent==null || parent.progressArea==null) return; parent.progressArea.appendText(line+"\n"); if (countLogsForStep) currentStepLogLines++; }
    private void removeLastLogLines(int count){ if (parent==null || parent.progressArea==null || count<=0) return; String text = parent.progressArea.getText(); if (text==null || text.isEmpty()) return; String[] lines = text.split("\n", -1); int total=lines.length; int newLen = Math.max(0,total-count); StringBuilder sb = new StringBuilder(); for (int i=0;i<newLen;i++) sb.append(lines[i]).append("\n"); parent.progressArea.setText(sb.toString()); }

    private void onStep(StepType type, StepPayload p){
        countLogsForStep = true; currentStepLogLines = 0;
        switch (type){
            case INIT: case RESET: syncVisualizer(); visualizer.clearHighlight(); visualizer.clearLinkHighlights(); break;
            case SET_TYPE: appendProgress("🔧 "+p.message); break;
            case FIND_TAIL_START: appendProgress("🔎 find tail"); visualizer.clearHighlight(); visualizer.clearLinkHighlights(); break;
            case FIND_TAIL_CHECK: visualizer.highlightCompare(p.cursorNodeId); visualizer.highlightCompareLinkFrom(p.cursorNodeId); appendProgress("• check node["+p.cursorIndex+"]: next != null → move next"); break;
            case FIND_TAIL_FOUND: if (p.cursorNodeId!=null) visualizer.highlightFound(p.cursorNodeId); appendProgress("✅ tail identified"); visualizer.clearLinkHighlights(); break;
            case INS_START_PREP: visualizer.clearLinkHighlights(); appendProgress("➕ INSERT_START("+p.value+")"); break;
            case INS_START_DONE: visualizer.clearLinkHighlights(); appendProgress("✅ done"); break;
            case INS_END_PREP: visualizer.clearLinkHighlights(); appendProgress("➕ INSERT_END("+p.value+")"); break;
            case INS_END_DONE: visualizer.clearLinkHighlights(); appendProgress("✅ done"); break;
            case MOVE_TO_INDEX_START: visualizer.clearLinkHighlights(); appendProgress("↘ move to index start"); break;
            case MOVE_TO_INDEX_STEP: visualizer.highlightCompare(p.cursorNodeId); visualizer.highlightCompareLinkFrom(p.cursorNodeId); appendProgress("• step at index="+p.cursorIndex); break;
            case MOVE_TO_INDEX_DONE: visualizer.highlightCompare(p.cursorNodeId); appendProgress("✅ reached position"); break;
            case INS_AT_INDEX_PREP: visualizer.clearLinkHighlights(); appendProgress("➕ INSERT_AT_INDEX("+p.value+")"); break;
            case INS_AT_INDEX_DONE: visualizer.clearLinkHighlights(); appendProgress("✅ done"); break;
            case INS_AFTER_PREP: visualizer.clearLinkHighlights(); appendProgress("➕ INSERT_AFTER(value="+p.value+")"); break;
            case INS_AFTER_DONE: visualizer.clearLinkHighlights(); appendProgress("✅ done"); break;
            case DEL_START_PREP: visualizer.highlightCompare(p.nodeId); visualizer.highlightCompareLinkFrom(p.nodeId); appendProgress("➖ DELETE_START (value="+p.value+")"); break;
            case DEL_START_REMOVE: syncVisualizer(); visualizer.clearLinkHighlights(); appendProgress("• removed head"); break;
            case DEL_START_DONE: appendProgress("✅ done"); break;
            case DEL_END_PREP: visualizer.highlightCompare(p.nodeId); visualizer.highlightCompareLinkTo(p.nodeId); appendProgress("➖ DELETE_END (value="+p.value+")"); break;
            case DEL_END_DONE: visualizer.clearLinkHighlights(); appendProgress("✅ done"); break;
            case DEL_AT_INDEX_PREP: if (p.nodeId!=null) visualizer.highlightFound(p.nodeId); visualizer.clearLinkHighlights(); appendProgress("➖ DELETE_AT_INDEX (index="+p.index+", value="+p.value+")"); break;
            case DEL_AT_INDEX_DONE: visualizer.clearLinkHighlights(); appendProgress("✅ done"); break;
            case DEL_BY_VALUE_PREP: visualizer.highlightCompare(p.nodeId); visualizer.highlightCompareLinkTo(p.nodeId); appendProgress("➖ DELETE_BY_VALUE"); break;
            case DEL_BY_VALUE_DONE: visualizer.clearLinkHighlights(); appendProgress("✅ done"); break;
            case TRAVERSE_START: visualizer.clearHighlight(); visualizer.clearLinkHighlights(); appendProgress("▶ Traversal"); break;
            case TRAVERSE_VISIT: visualizer.highlightCompare(p.nodeId); visualizer.highlightCompareLinkFrom(p.nodeId); appendProgress("• visit index="+p.cursorIndex+" value="+p.value); break;
            case TRAVERSE_DONE: visualizer.clearHighlight(); visualizer.clearLinkHighlights(); appendProgress("✔ Traversal done"); break;
            case SEARCH_START: visualizer.clearHighlight(); visualizer.clearLinkHighlights(); appendProgress("🔍 SEARCH target="+p.value); break;
            case SEARCH_COMPARE: visualizer.highlightCompare(p.nodeId); visualizer.highlightCompareLinkFrom(p.nodeId); appendProgress("• check index="+p.cursorIndex+" value="+p.value); break;
            case SEARCH_FOUND: visualizer.highlightFound(p.nodeId); visualizer.highlightFoundLinkFrom(p.nodeId); appendProgress("✅ Found at index "+p.cursorIndex+" value="+p.value); break;
            case SEARCH_NOT_FOUND: visualizer.clearHighlight(); visualizer.clearLinkHighlights(); appendProgress("❌ Not found target="+p.value); break;
            case CLEAR: visualizer.clearHighlight(); visualizer.clearLinkHighlights(); syncVisualizer(); appendProgress("🧹 Clear"); break;
            case ALERT_BOUNDS: case ALERT_EMPTY: visualizer.clearLinkHighlights(); appendProgress("⚠ "+p.message); visualizer.flashAlert(); break;
            // Granular generic steps
            case NODE_CREATE:
                if (p.nodeId!=null && p.value!=null) visualizer.ensureEphemeralNode(p.nodeId, p.value);
                if (p.nodeId!=null) visualizer.highlightFound(p.nodeId);
                appendProgress("• "+(p.message!=null?p.message:"create node")+ (p.value!=null? (" ("+p.value+")"):""));
                break;
            case SET_NEXT:
                syncVisualizer();
                visualizer.highlightFoundLinkFrom(p.nodeId);
                appendProgress("• "+(p.message!=null?p.message:"set next"));
                break;
            case CLEAR_NEXT:
                // Keep the detached suffix chain (starting at the node being removed) visible even though it's now unreachable
                if (p.cursorNodeId != null) visualizer.ensureEphemeralChainFrom(p.cursorNodeId);
                syncVisualizer();
                visualizer.clearLinkHighlights();
                if (p.nodeId!=null) visualizer.highlightCompare(p.nodeId);
                appendProgress("• "+(p.message!=null?p.message:"clear next"));
                break;
            case UPDATE_HEAD:
                syncVisualizer();
                if (p.nodeId!=null) visualizer.highlightFound(p.nodeId); else visualizer.clearHighlight();
                appendProgress("• "+(p.message!=null?p.message:"update head"));
                break;
            case UPDATE_TAIL:
                syncVisualizer();
                if (p.nodeId!=null) visualizer.highlightFound(p.nodeId);
                appendProgress("• "+(p.message!=null?p.message:"update tail"));
                break;
            case NODE_REMOVE:
                // Now remove the ephemeral node view explicitly
                if (p.nodeId!=null) visualizer.removeNodeView(p.nodeId);
                syncVisualizer();
                visualizer.clearLinkHighlights();
                appendProgress("• "+(p.message!=null?p.message:"remove node"));
                break;
        }
        updateVariables();
        // Add small delay after structural link/remove steps for clarity
        if (type == StepType.SET_NEXT || type == StepType.CLEAR_NEXT || type == StepType.NODE_REMOVE) {
            stepDelayUntilMs = System.currentTimeMillis() + 220L;
        }
        if (countLogsForStep) { progressHistory.push(currentStepLogLines); countLogsForStep=false; currentStepLogLines=0; }
    }

    private void syncVisualizer(){ visualizer.setData(solver.getOrderedNodes(), solver.getHeadId(), solver.getTailId(), solver.getType()); }

    private void updateVariables(){ if (parent==null || parent.variableList==null) return; parent.variableList.getItems().clear(); int size = solver.getSize(); parent.variableList.getItems().addAll(
            "type: "+solver.getType(),
            "size: "+size,
            size>0? ("head: id="+solver.getHeadId()): "head: null",
            size>0? ("tail: id="+solver.getTailId()): "tail: null"
    ); }

    private void autoPlayIfQueued(){ if (!isPlaying && solver.hasPending()) onPlay(); }

    private void renderCode(){
        if (parent==null || parent.codeArea==null) return;
        com.algorithmvisualizer.code.AlgorithmCode code = 
            com.algorithmvisualizer.code.CodeRepository.getCode("Linked List");
        if (code == null) {
            parent.codeArea.replaceText(0, parent.codeArea.getLength(), 
                "// Code not available");
            return;
        }
        String selectedLang = parent.languageSelector != null ? 
            parent.languageSelector.getValue() : "Java";
        String codeText = code.getCodeForLanguage(selectedLang);
        parent.codeArea.replaceText(0, parent.codeArea.getLength(), codeText);
    }
}
