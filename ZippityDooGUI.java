import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZippityDooGUI extends Application {

    private Label statusLabel;
    private ProgressBar progressBar;
    private Button extractButton;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(20));
        root.setCenter(centerBox);

        statusLabel = new Label("Select a zip file and extraction destination");
        progressBar = new ProgressBar();
        progressBar.prefWidthProperty().bind(centerBox.widthProperty());
        progressBar.setPrefHeight(20);
        progressBar.setVisible(false);

        extractButton = new Button("Extract");
        extractButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files", "*.zip"));
            File zipFile = fileChooser.showOpenDialog(primaryStage);
            if (zipFile != null) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Select Extraction Destination");
                File destDir = directoryChooser.showDialog(primaryStage);
                if (destDir != null) {
                    progressBar.setVisible(true);
                    Task<Void> task = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            extractZip(zipFile, destDir);
                            return null;
                        }
                    };
                    task.setOnSucceeded(e -> {
                        // Animate progress bar to 100% in 3 seconds
                        animateProgressBar();
                    });
                    task.setOnFailed(e -> {
                        statusLabel.setText("Extraction failed: " + task.getException().getMessage());
                        progressBar.setVisible(false);
                    });
                    new Thread(task).start();
                }
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(extractButton);
        centerBox.getChildren().addAll(statusLabel, progressBar, buttonBox);

        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ZippityDoo");
        primaryStage.show();
    }

    private void extractZip(File zipFile, File destDir) throws IOException {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            long totalBytes = zipFile.length();
            long extractedBytes = 0;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                            extractedBytes += len;
                        }
                    }
                }
            }
        }
    }

    private void animateProgressBar() {
        // Animate progress bar to 100% in 3 seconds
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(3), new javafx.animation.KeyValue(progressBar.progressProperty(), 1))
        );
        timeline.setOnFinished(event -> {
            statusLabel.setText("Extraction complete");
            progressBar.setVisible(false);
        });
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
