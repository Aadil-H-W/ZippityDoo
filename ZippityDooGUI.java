import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
        VBox root = new VBox(10);

        statusLabel = new Label("Select a zip file and extraction destination");
        progressBar = new ProgressBar();
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
                    try {
                        extractZip(zipFile, destDir);
                        statusLabel.setText("Extraction complete");
                    } catch (IOException e) {
                        statusLabel.setText("Extraction failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

        root.getChildren().addAll(statusLabel, progressBar, extractButton);

        Scene scene = new Scene(root, 300, 200);
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
                            double progress = (double) extractedBytes / totalBytes;
                            progressBar.setProgress(progress);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
