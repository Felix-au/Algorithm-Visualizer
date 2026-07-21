package com.algorithmvisualizer.ui;

import com.algorithmvisualizer.algorithm.HuffmanCodingSolverImpl;
import com.algorithmvisualizer.code.implementations.HuffmanCodingCode;
import com.algorithmvisualizer.visualization.HuffmanTreeRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HuffmanCodingController
        implements AlgorithmViewController.AlgorithmSpecificController {

    private AlgorithmViewController parent;
    private final HuffmanCodingSolverImpl solver = new HuffmanCodingSolverImpl();
    private HuffmanTreeRenderer treeRenderer;

    private Timeline timeline;
    private boolean isPlaying = false;
    private long stepDelayUntilMs = 0;
    private final Deque<Integer> cursorHistory = new ArrayDeque<Integer>();
    private final Deque<Integer> progressHistory = new ArrayDeque<Integer>();
    private int currentStepLogLines = 0;
    private boolean countLogsForStep = false;
    private boolean suppressLogsDuringReplay = false;

    private TextField inputField;
    private FlowPane freqChips;
    private FlowPane codeChips;
    private FlowPane queueChips;
    private Label encodedLabel;
    private Label statsLabel;
    private Label phaseLabel;

    @FXML
    private void initialize() {
        treeRenderer = new HuffmanTreeRenderer();
        solver.setStepListener(this::onStep);
    }

    @Override
    public void setParentController(
            AlgorithmViewController parent) {
        this.parent = parent;
        parent.setCurrentAlgorithmName(
                "Huffman Coding");

        if (parent.chessboardContainer != null) {
            parent.chessboardContainer
                    .getChildren().clear();
            parent.chessboardContainer
                    .getChildren()
                    .add(treeRenderer.getNode());
        }

        if (parent.chessboardHeaderBox != null) {
            parent.chessboardHeaderBox
                    .setVisible(true);
            parent.chessboardHeaderBox
                    .setManaged(true);
            parent.chessboardHeaderBox
                    .getChildren().clear();
            Label title = new Label(
                    "Huffman Coding Tree");
            title.setStyle(
                    "-fx-font-weight: bold;"
                            + " -fx-font-size: 14px;");
            phaseLabel = new Label("Ready");
            phaseLabel.setStyle(
                    "-fx-text-fill: #6366f1;"
                            + " -fx-font-weight: bold;");
            Region spacer = new Region();
            HBox.setHgrow(spacer,
                    Priority.ALWAYS);
            parent.chessboardHeaderBox
                    .getChildren()
                    .addAll(title, spacer,
                            phaseLabel);
        }

        if (parent.chessboardLegendBox != null) {
            parent.chessboardLegendBox
                    .setVisible(true);
            parent.chessboardLegendBox
                    .setManaged(true);
            parent.chessboardLegendBox
                    .getChildren().clear();
            Label legendTitle = new Label("Legend:");
            legendTitle.setStyle(
                    "-fx-font-weight: bold;");
            HBox row = new HBox(15.0);
            row.getChildren().addAll(
                    legendItem("#10b981",
                            "Leaf node"),
                    legendItem("#6366f1",
                            "Internal node"),
                    legendItem("#f59e0b",
                            "Highlighted"),
                    legendItem("#ef4444",
                            "Extracted"),
                    legendItem("#3b82f6",
                            "Path"));
            parent.chessboardLegendBox
                    .getChildren()
                    .addAll(legendTitle, row);
        }

        buildParameterPanel();
        buildSolutionsPane();
        wirePlaybackControls();
        loadCode();
        initProgressLog();
    }

    private void buildParameterPanel() {
        if (parent.paramSizeLabel != null) {
            parent.paramSizeLabel
                    .setVisible(false);
            parent.paramSizeLabel
                    .setManaged(false);
        }
        if (parent.paramBoardSizeSpinner != null) {
            parent.paramBoardSizeSpinner
                    .setVisible(false);
            parent.paramBoardSizeSpinner
                    .setManaged(false);
        }
        if (parent.paramElementsBox != null) {
            parent.paramElementsBox
                    .setVisible(false);
            parent.paramElementsBox
                    .setManaged(false);
        }
        if (parent.paramApplyButton != null) {
            parent.paramApplyButton
                    .setVisible(false);
            parent.paramApplyButton
                    .setManaged(false);
        }
        if (parent.paramTargetLabel != null) {
            parent.paramTargetLabel
                    .setVisible(false);
            parent.paramTargetLabel
                    .setManaged(false);
        }

        if (parent.paramElementsLabel != null) {
            parent.paramElementsLabel
                    .setVisible(true);
            parent.paramElementsLabel
                    .setManaged(true);
            parent.paramElementsLabel
                    .setText("Input Text");
        }
        if (parent.paramElementsField != null) {
            parent.paramElementsField
                    .setVisible(true);
            parent.paramElementsField
                    .setManaged(true);
            parent.paramElementsField
                    .setPromptText(
                            "e.g. hello world");
            parent.paramElementsField
                    .setText("hello world");
            inputField = parent.paramElementsField;
        }

        if (parent.paramRandomizeButton != null) {
            parent.paramRandomizeButton
                    .setVisible(true);
            parent.paramRandomizeButton
                    .setManaged(true);
            parent.paramRandomizeButton
                    .setText("Randomize");
            parent.paramRandomizeButton
                    .setOnAction(
                            ev -> onRandomize());

            Node pn = parent.paramRandomizeButton
                    .getParent();
            if (pn instanceof HBox) {
                HBox box = (HBox) pn;
                Button encodeBtn = new Button("Encode");
                String btnStyle = "-fx-background-color: "
                        + "#10b981; "
                        + "-fx-text-fill: white; "
                        + "-fx-background-radius: 6; "
                        + "-fx-font-weight: bold; "
                        + "-fx-padding: 6 14; "
                        + "-fx-cursor: hand;";
                encodeBtn.setStyle(btnStyle);
                encodeBtn.setOnAction(
                        ev -> onEncode());
                box.getChildren().remove(
                        parent.paramRandomizeButton);
                HBox row1 = new HBox(8.0,
                        parent.paramRandomizeButton,
                        encodeBtn);
                row1.setAlignment(
                        Pos.CENTER_LEFT);
                box.getChildren()
                        .add(0, row1);
            }
        }
    }

    private void buildSolutionsPane() {
        if (parent.solutionsHeaderBox != null) {
            parent.solutionsHeaderBox
                    .setVisible(true);
            parent.solutionsHeaderBox
                    .setManaged(true);
            parent.solutionsHeaderBox
                    .getChildren().clear();
            Label h = new Label("Results");
            h.setStyle(
                    "-fx-font-weight: bold;"
                            + " -fx-font-size: 14px;");
            parent.solutionsHeaderBox
                    .getChildren().add(h);
        }
        if (parent.solutionsSubHeaderBox != null) {
            parent.solutionsSubHeaderBox
                    .setVisible(false);
            parent.solutionsSubHeaderBox
                    .setManaged(false);
        }
        if (parent.solutionsContent != null) {
            parent.solutionsContent
                    .getChildren().clear();
            parent.solutionsContent
                    .setAlignment(Pos.CENTER);

            Label freqLabel = sectionLabel(
                    "Frequency Table");
            freqChips = new FlowPane();
            freqChips.setHgap(6);
            freqChips.setVgap(6);
            freqChips.setAlignment(
                    Pos.CENTER);

            Label codeLabel = sectionLabel(
                    "Huffman Codes");
            codeChips = new FlowPane();
            codeChips.setHgap(6);
            codeChips.setVgap(6);
            codeChips.setAlignment(
                    Pos.CENTER);

            Label encLabel = sectionLabel(
                    "      Encoded Output");
            encodedLabel = new Label("");
            encodedLabel.setWrapText(true);
            encodedLabel.setMaxWidth(
                    Double.MAX_VALUE);
            encodedLabel.setAlignment(
                    Pos.CENTER);
            String encStyle = "-fx-font-family: Monospaced; "
                    + "-fx-font-size: 11px; "
                    + "-fx-text-fill: #374151; "
                    + "-fx-background-color: #f3f4f6; "
                    + "-fx-padding: 6; "
                    + "-fx-background-radius: 4;";
            encodedLabel.setStyle(encStyle);

            statsLabel = new Label("");
            statsLabel.setWrapText(true);
            statsLabel.setMaxWidth(
                    Double.MAX_VALUE);
            statsLabel.setAlignment(
                    Pos.CENTER);
            String statsStyle = "-fx-font-weight: bold; "
                    + "-fx-text-fill: #059669; "
                    + "-fx-font-size: 12px;";
            statsLabel.setStyle(statsStyle);

            parent.solutionsContent
                    .getChildren().addAll(
                            freqLabel, freqChips,
                            codeLabel, codeChips,
                            encLabel, encodedLabel,
                            statsLabel);
        }
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        String sty = "-fx-font-weight: bold; "
                + "-fx-text-fill: #1f2937; "
                + "-fx-font-size: 13px; "
                + "-fx-alignment: center;";
        l.setStyle(sty);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private void wirePlaybackControls() {
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
            parent.pauseButton.setOnAction(ev -> onPause());
        }
        if (parent.playButton != null) {
            parent.playButton.setOnAction(ev -> onPlay());
        }
        if (parent.resetButton != null) {
            parent.resetButton.setOnAction(ev -> onReset());
        }
        if (parent.stepForwardButton != null) {
            parent.stepForwardButton.setOnAction(ev -> onStepForward());
        }
        if (parent.stepBackButton != null) {
            parent.stepBackButton.setOnAction(ev -> onStepBack());
            parent.stepBackButton.setDisable(true);
        }
        if (parent.speedSlider != null) {
            parent.speedSlider.valueProperty().addListener((obs, ov, nv) -> {
                if (isPlaying) {
                    rebuildTimeline();
                }
            });
        }
    }

    private void onRandomize() {
        String[] samples = {
                "hello world",
                "abracadabra",
                "MISSISSIPPI",
                "huffman coding",
                "the quick brown fox",
                "aabbccddee",
                "banana",
                "programming"
        };
        Random r = new Random();
        String choice = samples[r.nextInt(
                samples.length)];
        if (inputField != null) {
            inputField.setText(choice);
        }
        syncCodeWithInput(choice);
    }

    private void onEncode() {
        String text = "";
        if (inputField != null) {
            text = inputField.getText();
        }
        if (text == null
                || text.trim().isEmpty()) {
            appendProgress(
                    "Enter text to encode.");
            return;
        }
        clearAll();
        syncCodeWithInput(text);
        solver.encode(text);
    }

    @Override
    public void onPlay() {
        if (isPlaying) {
            stopTimeline();
            return;
        }
        if (!solver.hasPending()) {
            onEncode();
        }
        if (!solver.hasPending()) {
            return;
        }
        isPlaying = true;
        rebuildTimeline();
        if (timeline != null) {
            timeline.play();
        }
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(true);
            parent.pauseButton.setManaged(true);
            parent.pauseButton.setDisable(false);
        }
        if (parent.playButton != null) {
            parent.playButton.setVisible(false);
            parent.playButton.setManaged(false);
        }
    }

    @Override
    public void onReset() {
        stopTimeline();
        clearAll();
        solver.reset();
        initProgressLog();
    }

    @Override
    public void onStepForward() {
        if (!solver.hasPending()) {
            onEncode();
        }
        if (!solver.hasPending()) {
            return;
        }
        cursorHistory.push(
                solver.getCursor());
        if (!solver.step()) {
            stopTimeline();
        }
        updateStepButtons();
    }

    @Override
    public void onStepBack() {
        if (isPlaying) {
            stopTimeline();
        }
        if (cursorHistory.isEmpty()) {
            updateStepButtons();
            return;
        }
        int target = cursorHistory.pop();
        clearAll();
        suppressLogsDuringReplay = true;
        solver.replayTo(target);
        solver.setCursor(target);
        suppressLogsDuringReplay = false;
        if (parent != null
                && parent.progressArea != null
                && !progressHistory.isEmpty()) {
            int toRemove = progressHistory.pop();
            removeLastLogLines(toRemove);
        }
        updateStepButtons();
    }

    private void stopTimeline() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = null;
        isPlaying = false;
        if (parent.playButton != null) {
            parent.playButton.setVisible(true);
            parent.playButton.setManaged(true);
        }
        if (parent.pauseButton != null) {
            parent.pauseButton.setVisible(false);
            parent.pauseButton.setManaged(false);
        }
    }

    private void rebuildTimeline() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        double speed = 1.0;
        if (parent != null
                && parent.speedSlider != null) {
            speed = parent.speedSlider.getValue();
        }
        double fps = Math.max(1.0, speed);
        Duration frame = Duration.millis(1000.0 / fps);
        timeline = new Timeline(
                new KeyFrame(frame, ev -> {
                    if (!solver.hasPending()) {
                        stopTimeline();
                        return;
                    }
                    long now = System.currentTimeMillis();
                    if (now < stepDelayUntilMs) {
                        return;
                    }
                    cursorHistory.push(
                            solver.getCursor());
                    solver.step();
                    updateStepButtons();
                }));
        timeline.setCycleCount(
                Animation.INDEFINITE);
    }

    private void updateStepButtons() {
        if (parent != null
                && parent.stepBackButton != null) {
            parent.stepBackButton
                    .setDisable(
                            solver.getCursor() <= 0);
        }
    }

    private void delay(long ms) {
        stepDelayUntilMs = System.currentTimeMillis() + ms;
    }

    private void onStep(
            HuffmanCodingSolverImpl.StepType type,
            HuffmanCodingSolverImpl.StepPayload p) {
        countLogsForStep = true;
        currentStepLogLines = 0;

        updateVariables(type, p);
        updatePhaseLabel(type);
        if (parent != null
                && parent.stepDescription != null) {
            String msg = p.message != null
                    ? p.message
                    : type.name();
            parent.stepDescription.setText(msg);
        }

        switch (type) {
            case INIT:
                appendProgress(
                        "Starting Huffman Coding...");
                if (p.message != null) {
                    appendProgress(
                            "Input: " + p.message);
                }
                delay(600);
                break;

            case COUNT_CHAR:
                if (p.character != null) {
                    String dc = displayChar(p.character);
                    appendProgress(
                            "Scanning '" + dc + "'");
                }
                delay(150);
                break;

            case FREQ_UPDATE:
                if (p.character != null
                        && p.frequency != null) {
                    updateFreqChip(
                            p.character, p.frequency);
                    String dc = displayChar(p.character);
                    appendProgress(
                            "'" + dc + "' freq -> "
                                    + p.frequency);
                }
                delay(200);
                break;

            case FREQ_DONE:
                appendProgress(
                        "==========================");
                appendProgress(
                        "Frequency analysis complete!");
                appendProgress(
                        "==========================");
                delay(800);
                break;

            case CREATE_LEAF:
                if (p.nodeId != null
                        && p.character != null
                        && p.frequency != null) {
                    treeRenderer.addNode(
                            p.nodeId,
                            p.character,
                            p.frequency);
                    treeRenderer.renderForest();
                    addQueueChip(
                            p.character,
                            p.frequency);
                    String dc = displayChar(p.character);
                    appendProgress(
                            "Leaf '" + dc
                                    + "' freq="
                                    + p.frequency);
                }
                delay(600);
                break;

            case ADD_TO_QUEUE:
                if (p.queueSize != null) {
                    appendProgress(
                            "Added to queue (size="
                                    + p.queueSize + ")");
                }
                delay(400);
                break;

            case QUEUE_SORTED:
                appendProgress(
                        "Queue sorted by frequency");
                treeRenderer.renderForest();
                delay(1000);
                break;

            case EXTRACT_MIN_1:
                if (p.nodeId != null) {
                    treeRenderer.glowNode(
                            p.nodeId,
                            Color.web("#ef4444"));
                    treeRenderer.shakeNode(
                            p.nodeId);
                    removeQueueChipByFreq(
                            p.frequency);
                    String msg = safeMsg(p);
                    appendProgress(
                            "Extract min #1: "
                                    + msg);
                }
                delay(800);
                break;

            case EXTRACT_MIN_2:
                if (p.nodeId != null) {
                    treeRenderer.glowNode(
                            p.nodeId,
                            Color.web("#ef4444"));
                    treeRenderer.shakeNode(
                            p.nodeId);
                    removeQueueChipByFreq(
                            p.frequency);
                    String msg = safeMsg(p);
                    appendProgress(
                            "Extract min #2: "
                                    + msg);
                }
                delay(800);
                break;

            case MERGE_NODES:
                if (p.nodeId != null
                        && p.leftId != null
                        && p.rightId != null
                        && p.frequency != null) {
                    treeRenderer.addNode(
                            p.nodeId,
                            null,
                            p.frequency);
                    treeRenderer.setChildren(
                            p.nodeId,
                            p.leftId,
                            p.rightId);
                    treeRenderer.renderForest();
                    appendProgress(
                            "Merge -> freq="
                                    + p.frequency);
                }
                delay(800);
                break;

            case INSERT_MERGED:
                if (p.nodeId != null) {
                    treeRenderer.renderForest();
                    treeRenderer
                            .pulseNode(p.nodeId);
                    addQueueChip(
                            null, p.frequency);
                    String qs = (p.queueSize != null)
                            ? p.queueSize.toString()
                            : "?";
                    appendProgress(
                            "Inserted merged (queue="
                                    + qs + ")");
                }
                delay(700);
                break;

            case TREE_GROWING:
                if (p.nodeId != null) {
                    treeRenderer.renderForest();
                    appendProgress(
                            "Tree updated");
                }
                delay(600);
                break;

            case TREE_COMPLETE:
                if (p.nodeId != null) {
                    treeRenderer
                            .setRoot(p.nodeId);
                    treeRenderer
                            .clearHighlights();
                    treeRenderer.render();
                    if (queueChips != null) {
                        queueChips
                                .getChildren()
                                .clear();
                    }
                }
                appendProgress(
                        "==========================");
                appendProgress(
                        "Huffman tree complete!");
                appendProgress(
                        "==========================");
                delay(1000);
                break;

            case TRAVERSE_START:
                treeRenderer.clearHighlights();
                appendProgress(
                        "Generating codes...");
                delay(500);
                break;

            case TRAVERSE_LEFT:
                if (p.nodeId != null
                        && p.leftId != null) {
                    treeRenderer.highlightNode(
                            p.nodeId,
                            Color.web("#3b82f6"));
                    treeRenderer.highlightEdge(
                            p.nodeId, p.leftId);
                    treeRenderer.render();
                    String cd = p.code != null
                            ? p.code
                            : "";
                    appendProgress(
                            "Left -> '0' ("
                                    + cd + ")");
                }
                delay(300);
                break;

            case TRAVERSE_RIGHT:
                if (p.nodeId != null
                        && p.rightId != null) {
                    treeRenderer.highlightNode(
                            p.nodeId,
                            Color.web("#3b82f6"));
                    treeRenderer.highlightEdge(
                            p.nodeId, p.rightId);
                    treeRenderer.render();
                    String cd = p.code != null
                            ? p.code
                            : "";
                    appendProgress(
                            "Right -> '1' ("
                                    + cd + ")");
                }
                delay(300);
                break;

            case LEAF_REACHED:
                if (p.nodeId != null) {
                    treeRenderer.glowNode(
                            p.nodeId,
                            Color.web("#f59e0b"));
                    treeRenderer.pulseNode(
                            p.nodeId);
                    treeRenderer.render();
                    if (p.character != null
                            && p.code != null) {
                        String dc = displayChar(
                                p.character);
                        appendProgress(
                                "Leaf: '" + dc
                                        + "' -> '"
                                        + p.code + "'");
                    }
                }
                delay(400);
                break;

            case CODE_ASSIGNED:
                if (p.character != null
                        && p.code != null) {
                    addCodeChip(
                            p.character, p.code);
                    String dc = displayChar(p.character);
                    appendProgress(
                            "'" + dc + "' = "
                                    + p.code);
                }
                treeRenderer.clearHighlights();
                treeRenderer.render();
                delay(300);
                break;

            case ENCODE_START:
                appendProgress(
                        "==========================");
                appendProgress(
                        "Encoding input text...");
                appendProgress(
                        "==========================");
                delay(600);
                break;

            case ENCODE_CHAR:
                if (p.character != null
                        && p.code != null) {
                    highlightLeafByChar(
                            p.character);
                    String dc = displayChar(p.character);
                    appendProgress(
                            "'" + dc + "' -> "
                                    + p.code);
                }
                delay(250);
                break;

            case APPEND_CODE:
                if (encodedLabel != null) {
                    String cur = encodedLabel.getText();
                    String nb = p.code != null
                            ? p.code
                            : "";
                    String nc = cur != null
                            ? cur
                            : "";
                    encodedLabel.setText(
                            nc + nb);
                }
                treeRenderer.clearHighlights();
                treeRenderer.render();
                delay(150);
                break;

            case ENCODE_COMPLETE:
                treeRenderer.clearHighlights();
                treeRenderer.render();
                appendProgress(
                        "Encoding complete!");
                delay(600);
                break;

            case SHOW_STATS:
                if (statsLabel != null
                        && p.message != null) {
                    statsLabel.setText(
                            p.message);
                }
                appendProgress(
                        "==========================");
                if (p.message != null) {
                    appendProgress(p.message);
                }
                appendProgress(
                        "==========================");
                delay(800);
                break;

            case COMPLETE:
                treeRenderer.clearHighlights();
                treeRenderer.render();
                appendProgress(
                        "Huffman coding complete!");
                stopTimeline();
                delay(500);
                break;

            default:
                break;
        }

        if (countLogsForStep
                && !suppressLogsDuringReplay) {
            int lines = Math.max(
                    1, currentStepLogLines);
            progressHistory.push(lines);
            countLogsForStep = false;
            currentStepLogLines = 0;
        }
        updateStepButtons();
    }

    private String safeMsg(
            HuffmanCodingSolverImpl.StepPayload p) {
        if (p.message != null) {
            return p.message;
        }
        return "";
    }

    private void showAllLeavesAsForest() {
        treeRenderer.renderForest();
    }

    private void updateFreqChip(
            char c, int freq) {
        if (freqChips == null)
            return;
        String charStr = displayChar(c);
        for (Node node : freqChips.getChildren()) {
            if (node instanceof Label
                    && node.getUserData() != null
                    && node.getUserData()
                            .equals(c)) {
                ((Label) node).setText(
                        "'" + charStr
                                + "': " + freq);
                String hlSty = "-fx-background-color: "
                        + "#fef3c7; "
                        + "-fx-padding: 3 8; "
                        + "-fx-background-radius: "
                        + "8; "
                        + "-fx-text-fill: #92400e; "
                        + "-fx-font-weight: bold; "
                        + "-fx-font-size: 11px;";
                node.setStyle(hlSty);
                String normSty = "-fx-background-color: "
                        + "#e2e8f0; "
                        + "-fx-padding: 3 8; "
                        + "-fx-background-radius: "
                        + "8; "
                        + "-fx-text-fill: #1f2937; "
                        + "-fx-font-weight: bold; "
                        + "-fx-font-size: 11px;";
                Node nr = node;
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(
                        Duration.millis(300));
                pt.setOnFinished(ev -> nr.setStyle(normSty));
                pt.play();
                return;
            }
        }
        Label chip = new Label(
                "'" + charStr + "': " + freq);
        chip.setUserData(c);
        String newSty = "-fx-background-color: "
                + "#dbeafe; "
                + "-fx-padding: 3 8; "
                + "-fx-background-radius: 8; "
                + "-fx-text-fill: #1e40af; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 11px;";
        chip.setStyle(newSty);
        freqChips.getChildren().add(chip);
    }

    private void addQueueChip(
            Character c, Integer freq) {
        if (queueChips == null
                || freq == null) {
            return;
        }
        String text;
        if (c != null) {
            text = "'" + displayChar(c)
                    + "':" + freq;
        } else {
            text = "[" + freq + "]";
        }
        Label chip = new Label(text);
        chip.setUserData(freq);
        String sty = "-fx-background-color: "
                + "#ede9fe; "
                + "-fx-padding: 3 8; "
                + "-fx-background-radius: 8; "
                + "-fx-text-fill: #5b21b6; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 11px;";
        chip.setStyle(sty);
        queueChips.getChildren().add(chip);
    }

    private void removeQueueChipByFreq(
            Integer freq) {
        if (queueChips == null
                || freq == null) {
            return;
        }
        Iterator<Node> it = queueChips.getChildren()
                .iterator();
        while (it.hasNext()) {
            Node n = it.next();
            if (n instanceof Label
                    && freq.equals(
                            n.getUserData())) {
                it.remove();
                return;
            }
        }
    }

    private void addCodeChip(
            char c, String code) {
        if (codeChips == null)
            return;
        String dc = displayChar(c);
        Label chip = new Label(
                "'" + dc + "' = " + code);
        String sty = "-fx-background-color: "
                + "#d1fae5; "
                + "-fx-padding: 3 8; "
                + "-fx-background-radius: 8; "
                + "-fx-text-fill: #065f46; "
                + "-fx-font-weight: bold; "
                + "-fx-font-size: 11px;";
        chip.setStyle(sty);
        codeChips.getChildren().add(chip);
    }

    private void highlightLeafByChar(
            char c) {
        for (HuffmanCodingSolverImpl.HNode hn : solver.getAllNodes()) {
            if (hn.ch != null
                    && hn.ch == c) {
                treeRenderer.highlightNode(
                        hn.id,
                        Color.web("#f59e0b"));
                treeRenderer.render();
                return;
            }
        }
    }

    private void updatePhaseLabel(
            HuffmanCodingSolverImpl.StepType type) {
        if (phaseLabel == null)
            return;
        String phase;
        String color;
        switch (type) {
            case INIT:
            case COUNT_CHAR:
            case FREQ_UPDATE:
            case FREQ_DONE:
                phase = "Phase 1: Freq";
                color = "#6366f1";
                break;
            case CREATE_LEAF:
            case ADD_TO_QUEUE:
            case QUEUE_SORTED:
                phase = "Phase 2: Queue";
                color = "#8b5cf6";
                break;
            case EXTRACT_MIN_1:
            case EXTRACT_MIN_2:
            case MERGE_NODES:
            case INSERT_MERGED:
            case TREE_GROWING:
            case TREE_COMPLETE:
                phase = "Phase 3: Tree";
                color = "#2563eb";
                break;
            case TRAVERSE_START:
            case TRAVERSE_LEFT:
            case TRAVERSE_RIGHT:
            case LEAF_REACHED:
            case CODE_ASSIGNED:
                phase = "Phase 4: Codes";
                color = "#059669";
                break;
            case ENCODE_START:
            case ENCODE_CHAR:
            case APPEND_CODE:
            case ENCODE_COMPLETE:
                phase = "Phase 5: Encode";
                color = "#d97706";
                break;
            case SHOW_STATS:
            case COMPLETE:
                phase = "Done";
                color = "#10b981";
                break;
            default:
                phase = type.name();
                color = "#6b7280";
                break;
        }
        phaseLabel.setText(phase);
        String sty = "-fx-text-fill: " + color
                + "; -fx-font-weight: bold;";
        phaseLabel.setStyle(sty);
    }

    private void updateVariables(
            HuffmanCodingSolverImpl.StepType type,
            HuffmanCodingSolverImpl.StepPayload p) {
        if (parent == null
                || parent.variableList == null) {
            return;
        }
        List<String> items = new ArrayList<String>();
        items.add("step=" + type.name());
        if (p.character != null) {
            items.add("char='"
                    + displayChar(p.character)
                    + "'");
        }
        if (p.frequency != null) {
            items.add("freq="
                    + p.frequency);
        }
        if (p.nodeId != null) {
            items.add("nodeId="
                    + p.nodeId);
        }
        if (p.code != null) {
            items.add("code=" + p.code);
        }
        if (p.queueSize != null) {
            items.add("queueSize="
                    + p.queueSize);
        }
        if (p.originalBits != null) {
            items.add("origBits="
                    + p.originalBits);
        }
        if (p.encodedBits != null) {
            items.add("encBits="
                    + p.encodedBits);
        }
        if (p.compressionRatio != null) {
            items.add(String.format(
                    "ratio=%.1f%%",
                    p.compressionRatio));
        }
        parent.variableList.getItems()
                .setAll(items);
    }

    private void appendProgress(
            String line) {
        if (suppressLogsDuringReplay) {
            return;
        }
        if (parent != null
                && parent.progressArea != null) {
            parent.progressArea
                    .appendText(line + "\n");
        }
        if (countLogsForStep) {
            currentStepLogLines++;
        }
    }

    private void initProgressLog() {
        if (parent == null
                || parent.progressArea == null) {
            return;
        }
        parent.progressArea.clear();
        progressHistory.clear();
        currentStepLogLines = 0;
        countLogsForStep = false;
        appendProgress(
                "Huffman Coding - Ready");
        appendProgress(
                "Press Play or Step to begin.");
    }

    private void removeLastLogLines(
            int count) {
        if (parent == null
                || parent.progressArea == null
                || count <= 0) {
            return;
        }
        String text = parent.progressArea.getText();
        if (text == null
                || text.isEmpty()) {
            return;
        }
        String[] logLines = text.split("\n", -1);
        int total = logLines.length;
        int newLen = Math.max(
                0, total - count);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < newLen; i++) {
            sb.append(logLines[i])
                    .append("\n");
        }
        parent.progressArea.setText(
                sb.toString());
    }

    private void clearAll() {
        cursorHistory.clear();
        progressHistory.clear();
        stepDelayUntilMs = 0;
        treeRenderer.clear();
        if (freqChips != null) {
            freqChips.getChildren().clear();
        }
        if (codeChips != null) {
            codeChips.getChildren().clear();
        }
        if (queueChips != null) {
            queueChips.getChildren().clear();
        }
        if (encodedLabel != null) {
            encodedLabel.setText("");
        }
        if (statsLabel != null) {
            statsLabel.setText("");
        }
        if (phaseLabel != null) {
            phaseLabel.setText("Ready");
            String sty = "-fx-text-fill: #6366f1;"
                    + " -fx-font-weight: bold;";
            phaseLabel.setStyle(sty);
        }
        if (parent != null
                && parent.progressArea != null) {
            parent.progressArea.clear();
        }
        if (parent != null
                && parent.variableList != null) {
            parent.variableList
                    .getItems().clear();
        }
        if (parent != null
                && parent.stepDescription != null) {
            parent.stepDescription
                    .setText("Ready");
        }
    }

    private void syncCodeWithInput(
            String text) {
        com.algorithmvisualizer.code.AlgorithmCode codeObj = com.algorithmvisualizer.code.CodeRepository.getCode(
                "Huffman Coding");
        if (codeObj instanceof HuffmanCodingCode) {
            ((HuffmanCodingCode) codeObj)
                    .updateParameters(text);
        }
        loadCode();
    }

    private void loadCode() {
        if (parent == null
                || parent.codeArea == null) {
            return;
        }
        com.algorithmvisualizer.code.AlgorithmCode code = com.algorithmvisualizer.code.CodeRepository.getCode(
                "Huffman Coding");
        if (code != null) {
            String lang = "Java";
            if (parent.languageSelector != null) {
                lang = parent.languageSelector
                        .getValue();
            }
            String ct = code.getCodeForLanguage(
                    lang);
            parent.codeArea.replaceText(
                    0,
                    parent.codeArea.getLength(),
                    ct);
        } else {
            parent.codeArea.replaceText(
                    0,
                    parent.codeArea.getLength(),
                    "// Code not available");
        }

        if (parent.languageSelector != null) {
            parent.languageSelector
                    .valueProperty()
                    .addListener(
                            (obs, ov, nv) -> {
                                com.algorithmvisualizer.code.AlgorithmCode cx = com.algorithmvisualizer.code.CodeRepository
                                        .getCode(
                                                "Huffman Coding");
                                if (cx != null
                                        && parent.codeArea != null) {
                                    String t = cx.getCodeForLanguage(
                                            nv);
                                    parent.codeArea
                                            .replaceText(
                                                    0,
                                                    parent.codeArea
                                                            .getLength(),
                                                    t);
                                }
                            });
        }
    }

    private HBox legendItem(
            String color, String text) {
        javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(
                6, Color.web(color));
        c.setStroke(Color.BLACK);
        return new HBox(5.0,
                c, new Label(text));
    }

    private String displayChar(char c) {
        if (c == ' ')
            return "SP";
        if (c == '\n')
            return "NL";
        if (c == '\t')
            return "TAB";
        return String.valueOf(c);
    }
}
