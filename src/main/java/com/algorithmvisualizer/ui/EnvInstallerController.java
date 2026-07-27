package com.algorithmvisualizer.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.algorithmvisualizer.execution.ExecutionEnvironment;
import com.algorithmvisualizer.execution.LanguageType;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class EnvInstallerController {

    @FXML private CheckBox javaCheckbox;
    @FXML private CheckBox cppCheckbox;
    @FXML private CheckBox pythonCheckbox;

    @FXML private Label javaStatusLabel;
    @FXML private Label cppStatusLabel;
    @FXML private Label pythonStatusLabel;

    @FXML private VBox progressSection;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressTaskLabel;
    @FXML private Label progressPercentLabel;
    @FXML private Label progressDetailLabel;

    @FXML private Button installButton;
    @FXML private Button skipButton;

    private ExecutionEnvironment environment;
    private Path rootDir;

    // Download URLs
    private static final String PYTHON_URL = "https://www.python.org/ftp/python/3.11.8/python-3.11.8-embed-amd64.zip";
    private static final String JAVA_URL = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse";
    private static final String CPP_URL = "https://github.com/skeeto/w64devkit/releases/download/v1.23.0/w64devkit-1.23.0.zip";

    @FXML
    public void initialize() {
        environment = new ExecutionEnvironment();
        rootDir = environment.getApplicationRoot();
        checkRuntimes();
    }

    private void checkRuntimes() {
        boolean javaOk = environment.isLanguageAvailable(LanguageType.JAVA);
        boolean cppOk = environment.isLanguageAvailable(LanguageType.C) && environment.isLanguageAvailable(LanguageType.CPP);
        boolean pythonOk = environment.isLanguageAvailable(LanguageType.PYTHON);

        updateStatus(javaStatusLabel, javaCheckbox, javaOk);
        updateStatus(cppStatusLabel, cppCheckbox, cppOk);
        updateStatus(pythonStatusLabel, pythonCheckbox, pythonOk);

        // If all are already installed, show standard message, but allow reinstalling if they want
        if (javaOk && cppOk && pythonOk) {
            installButton.setText("Reinstall Selected");
        }
    }

    private void updateStatus(Label label, CheckBox checkBox, boolean installed) {
        if (installed) {
            label.setText("Installed");
            label.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
            checkBox.setSelected(false);
        } else {
            label.setText("Missing");
            label.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 10;");
            checkBox.setSelected(true);
        }
    }

    @FXML
    private void onInstall(ActionEvent event) {
        List<InstallItem> itemsToInstall = new ArrayList<>();
        if (javaCheckbox.isSelected()) {
            itemsToInstall.add(new InstallItem("Java JDK 17", JAVA_URL, "java_jdk.zip", null));
        }
        if (cppCheckbox.isSelected()) {
            itemsToInstall.add(new InstallItem("C/C++ Compiler (MinGW)", CPP_URL, "mingw_cpp.zip", null));
        }
        if (pythonCheckbox.isSelected()) {
            itemsToInstall.add(new InstallItem("Python 3.11", PYTHON_URL, "python_dist.zip", "python-3.11.8"));
        }

        if (itemsToInstall.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one environment to install.");
            alert.showAndWait();
            return;
        }

        // Disable UI
        javaCheckbox.setDisable(true);
        cppCheckbox.setDisable(true);
        pythonCheckbox.setDisable(true);
        installButton.setDisable(true);
        skipButton.setDisable(true);

        progressSection.setVisible(true);
        progressSection.setManaged(true);

        // Start installation task
        Task<Void> installTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build();

                double totalItems = itemsToInstall.size();
                for (int i = 0; i < itemsToInstall.size(); i++) {
                    InstallItem item = itemsToInstall.get(i);
                    double itemStartProgress = i / totalItems;
                    double itemWeight = 1.0 / totalItems;

                    updateMessage("Installing " + item.name + "...");
                    updateProgress(itemStartProgress, 1.0);

                    Path zipPath = rootDir.resolve(item.tempZipName);

                    // 1. Download
                    updateMessage("Downloading " + item.name + "...");
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(item.url))
                            .build();

                    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                    if (response.statusCode() >= 300) {
                        throw new IOException("Failed to download " + item.name + ". Server returned status: " + response.statusCode());
                    }

                    long totalBytes = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
                    try (InputStream in = response.body();
                         OutputStream out = Files.newOutputStream(zipPath)) {

                        byte[] buffer = new byte[8192];
                        long bytesRead = 0;
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                            bytesRead += read;
                            
                            double itemProgress = totalBytes > 0 ? (double) bytesRead / totalBytes : 0;
                            double overallProgress = itemStartProgress + (itemProgress * 0.7 * itemWeight); // Download takes 70% of item time
                            
                            updateProgress(overallProgress, 1.0);
                            
                            long finalBytesRead = bytesRead;
                            Platform.runLater(() -> {
                                if (totalBytes > 0) {
                                    progressPercentLabel.setText((int)(overallProgress * 100) + "%");
                                    progressDetailLabel.setText(String.format("Downloading: %.2f MB / %.2f MB", 
                                            (double) finalBytesRead / (1024 * 1024), 
                                            (double) totalBytes / (1024 * 1024)));
                                } else {
                                    progressPercentLabel.setText((int)(overallProgress * 100) + "%");
                                    progressDetailLabel.setText(String.format("Downloading: %.2f MB", 
                                            (double) finalBytesRead / (1024 * 1024)));
                                }
                            });
                        }
                    }

                    // 2. Extract
                    updateMessage("Extracting " + item.name + "...");
                    unzip(zipPath, rootDir, item.subDirName, itemStartProgress + (0.7 * itemWeight), itemWeight * 0.3); // Extract takes 30%

                    // 3. Clean up Zip
                    Files.deleteIfExists(zipPath);
                }

                updateProgress(1.0, 1.0);
                updateMessage("All environments successfully set up!");
                Platform.runLater(() -> progressPercentLabel.setText("100%"));
                return null;
            }

            private void unzip(Path zipFile, Path destDir, String subDirName, double baseProgress, double progressWeight) throws IOException {
                Path targetDir = destDir;
                if (subDirName != null) {
                    targetDir = destDir.resolve(subDirName);
                    Files.createDirectories(targetDir);
                }

                try (ZipFile zf = new ZipFile(zipFile.toFile())) {
                    int totalEntries = zf.size();
                    if (totalEntries == 0) totalEntries = 1;

                    Enumeration<? extends ZipEntry> entries = zf.entries();
                    int extractedEntries = 0;
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        Path filePath = targetDir.resolve(entry.getName());
                        if (!filePath.normalize().startsWith(targetDir.normalize())) {
                            throw new IOException("Bad zip entry: " + entry.getName());
                        }

                        if (entry.isDirectory()) {
                            Files.createDirectories(filePath);
                        } else {
                            Files.createDirectories(filePath.getParent());
                            String entryName = entry.getName();
                            Platform.runLater(() -> progressDetailLabel.setText("Extracting: " + entryName));

                            try (InputStream is = zf.getInputStream(entry);
                                 BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath))) {
                                byte[] bytesIn = new byte[8192];
                                int read;
                                while ((read = is.read(bytesIn)) != -1) {
                                    bos.write(bytesIn, 0, read);
                                }
                            }
                        }

                        extractedEntries++;
                        double extractionRatio = (double) extractedEntries / totalEntries;
                        double overallProgress = baseProgress + (extractionRatio * progressWeight);
                        updateProgress(overallProgress, 1.0);
                        Platform.runLater(() -> progressPercentLabel.setText((int)(overallProgress * 100) + "%"));
                    }
                }
            }
        };

        progressBar.progressProperty().bind(installTask.progressProperty());
        progressTaskLabel.textProperty().bind(installTask.messageProperty());

        installTask.setOnSucceeded(t -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Setup Complete");
            alert.setHeaderText(null);
            alert.setContentText("All environments are successfully set up! Click OK to launch AlgoBuddy.");
            alert.showAndWait();
            openMainApplication();
        });

        installTask.setOnFailed(t -> {
            Throwable ex = installTask.getException();
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Setup Failed");
            alert.setHeaderText("An error occurred during installation");
            alert.setContentText(ex.getMessage() != null ? ex.getMessage() : "Unknown error.");
            alert.showAndWait();

            // Re-enable UI
            javaCheckbox.setDisable(false);
            cppCheckbox.setDisable(false);
            pythonCheckbox.setDisable(false);
            installButton.setDisable(false);
            skipButton.setDisable(false);
            progressSection.setVisible(false);
            progressSection.setManaged(false);
            checkRuntimes();
        });

        new Thread(installTask).start();
    }

    @FXML
    private void onSkip(ActionEvent event) {
        openMainApplication();
    }

    private void openMainApplication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) skipButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Launch Error");
            alert.setHeaderText("Failed to launch main dashboard");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private static class InstallItem {
        final String name;
        final String url;
        final String tempZipName;
        final String subDirName;

        InstallItem(String name, String url, String tempZipName, String subDirName) {
            this.name = name;
            this.url = url;
            this.tempZipName = tempZipName;
            this.subDirName = subDirName;
        }
    }
}
