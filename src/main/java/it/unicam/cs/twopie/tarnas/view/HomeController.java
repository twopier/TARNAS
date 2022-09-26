package it.unicam.cs.twopie.tarnas.view;

import it.unicam.cs.twopie.App;
import it.unicam.cs.twopie.tarnas.controller.CleanerController;
import it.unicam.cs.twopie.tarnas.controller.IOController;
import it.unicam.cs.twopie.tarnas.controller.TranslatorController;
import it.unicam.cs.twopie.tarnas.model.rnafile.RNAFile;
import it.unicam.cs.twopie.tarnas.model.rnafile.RNAFormat;
import it.unicam.cs.twopie.tarnas.view.utils.DeleteCell;
import it.unicam.cs.twopie.tarnas.view.utils.LenCell;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static it.unicam.cs.twopie.tarnas.model.rnafile.RNAFormat.*;

public class HomeController {
    private RNAFormat selectedFormat;
    private RNAFormat loadedFilesFormat;

    @FXML
    private TableView<RNAFile> filesTable;

    @FXML
    private TableColumn<RNAFile, String> nameColumn;

    @FXML
    private TableColumn<RNAFile, String> formatColumn;

    @FXML
    private TableColumn<RNAFile, RNAFile> previewColumn;

    @FXML
    private TableColumn<RNAFile, RNAFile> deleteColumn;

    @FXML
    public MenuButton btnSelectFormatTranslation;

    @FXML
    public MenuItem itmAAS, itmAASNS, itmBPSEQ, itmCT, itmDB, itmDBNS, itmFASTA; // example: "AAS_NO_SEQUENCE" instead "AAS NO SEQUENCE" for enum recognition

    @FXML
    public Button btnTranslateAllLoadedFiles;

    @FXML
    public CheckBox chbxRmLinesContainingWord;

    @FXML
    public CheckBox chbxRmLinesContainingPrefix;

    @FXML
    public CheckBox chbxRmBlankLines;

    @FXML
    public CheckBox chbxMergeLines;

    @FXML
    public TextField txtfRmLinesContainingWord;

    @FXML
    public TextField txtRmLinesContainingPrefix;

    @FXML
    public Label lblRecognizedFormat;

    @FXML
    public void initialize() {
        txtRmLinesContainingPrefix.setTextFormatter(new TextFormatter<String>((TextFormatter.Change change) -> {
            String newText = change.getControlNewText();
            if (newText.length() > 1) {
                return null;
            } else {
                return change;
            }
        }));
        // load trash image
        var trashImage = new Image(Objects.requireNonNull(App.class.getResource("/img/trash.png")).toExternalForm(), 18, 18, false, false);
        var lenImage = new Image(Objects.requireNonNull(App.class.getResource("/img/lens-icon.jpeg")).toExternalForm(), 18, 18, false, false);
        //change table label
        this.filesTable.setPlaceholder(new Label("No loaded files"));
        // set column values
        this.nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        this.formatColumn.setCellValueFactory(new PropertyValueFactory<>("format"));
        this.previewColumn.setCellValueFactory(rnaFile -> new ReadOnlyObjectWrapper<>(rnaFile.getValue()));
        this.deleteColumn.setCellValueFactory(rnaFile -> new ReadOnlyObjectWrapper<>(rnaFile.getValue()));
        // set custom cell
        this.previewColumn.setCellFactory(column -> new LenCell(lenImage));
        this.deleteColumn.setCellFactory(column -> new DeleteCell(trashImage));
        // add event to select ButtonItem for destination format translation
        this.initSelectEventOnButtonItems();
        this.btnTranslateAllLoadedFiles.setDisable(true);
    }

    @FXML
    public void handleAddFile() {
        try {
            var fileChooser = new FileChooser();
            var selectedFile = fileChooser.showOpenDialog(this.getPrimaryStage());
            if (selectedFile != null) {
                var selectedRNAFile = Path.of(selectedFile.getPath());
                IOController.getInstance().loadFile(selectedRNAFile);
                var rnaFile = IOController.getInstance().getRNAFileOf(selectedRNAFile);
                this.addFile(rnaFile);
            }
        } catch (Exception e) {
            this.showAlert(Alert.AlertType.ERROR, "Error", "", e.getMessage());
        }
    }

    @FXML
    public void handleAddFolder() {
        try {
            var directoryChooser = new DirectoryChooser();
            var selectedDirectory = directoryChooser.showDialog(this.getPrimaryStage());
            if (selectedDirectory != null) {
                var files = Files.walk(selectedDirectory.toPath())
                        .filter(Files::isRegularFile)
                        .toList();
                for (var f : files)
                    this.addFile(IOController.getInstance().getRNAFileOf(Path.of(String.valueOf(f))));
                IOController.getInstance().loadDirectory(selectedDirectory.toPath());
            }
        } catch (Exception e) {
            this.showAlert(Alert.AlertType.ERROR, "Error", "", e.getMessage());
        }
    }

    @FXML
    public void handleClean() {
        try {
            var cleanedFiles = this.filesTable.getItems().stream().toList();
            if (this.chbxRmLinesContainingWord.isSelected())
                cleanedFiles = cleanedFiles
                        .parallelStream()
                        .map(f -> CleanerController.getInstance().removeLinesContaining(f, this.txtfRmLinesContainingWord.getText()))
                        .toList();
            if (this.chbxRmLinesContainingPrefix.isSelected())
                cleanedFiles = cleanedFiles
                        .parallelStream()
                        .map(f -> CleanerController.getInstance().removeLinesStartingWith(f, this.txtRmLinesContainingPrefix.getText()))
                        .toList();
            if (this.chbxRmBlankLines.isSelected())
                cleanedFiles = cleanedFiles
                        .parallelStream()
                        .map(f -> CleanerController.getInstance().removeWhiteSpaces(f))
                        .toList();
            if (this.chbxRmBlankLines.isSelected())
                cleanedFiles = cleanedFiles
                        .parallelStream()
                        .map(f -> CleanerController.getInstance().mergeDBLines(f))
                        .toList();
            this.showAlert(Alert.AlertType.INFORMATION, "", "", "Choose the directory where to save the files");
            var directoryChooser = new DirectoryChooser();
            var selectedDirectory = directoryChooser.showDialog(this.getPrimaryStage());
            if (selectedDirectory != null) {
                IOController.getInstance().saveFilesTo(cleanedFiles, selectedDirectory.toPath());
                this.showAlert(Alert.AlertType.INFORMATION,
                        "",
                        "Files saved successfully",
                        cleanedFiles.size() + " files saved in: " + selectedDirectory.getPath());
            }
        } catch (Exception e) {
            this.showAlert(Alert.AlertType.ERROR, "Error", "", e.getMessage());
        }
    }

    @FXML
    public void translateAllLoadedFiles(ActionEvent event) {
        List<RNAFile> translatedRNAFiles = new ArrayList<>();
        var result = this.showAlert(Alert.AlertType.CONFIRMATION,
                "TRANSLATION FILES CONFIRM",
                "Translate all loaded files to " + this.selectedFormat + "?",
                "Are you sure you want to translate all loaded files?");

        if (result.isPresent() && result.get() == ButtonType.OK)
            translatedRNAFiles = TranslatorController.getInstance().translateAllLoadedFiles(IOController.getInstance().getLoadedRNAFiles(), this.selectedFormat);
        if (translatedRNAFiles != null) {
            try {
                IOController.getInstance().saveFilesTo(translatedRNAFiles, Path.of("C:\\Users\\Piermuz\\Documents\\GitHub\\TARNAS\\src\\main\\java\\it\\unicam\\cs\\twopie\\tarnas\\controller\\"));
            } catch (IOException e) {
                this.showAlert(Alert.AlertType.ERROR, "Error", "", e.getMessage());
            }
        }
    }


    @FXML
    public void resetAll(ActionEvent event) {
        // Reset all data structures
        this.filesTable.getItems().clear();
        // Reset all buttons
        this.btnSelectFormatTranslation.setText("TRANSLATE TO...");
        this.btnTranslateAllLoadedFiles.setDisable(true);
        // TODO: insert clean options
    }

    private Stage getPrimaryStage() {
        return (Stage) this.filesTable.getScene().getWindow();
    }

    private Optional<ButtonType> showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.initOwner(this.getPrimaryStage());
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    private void initSelectEventOnButtonItems() {
        this.itmAAS.setId(AAS.toString());
        this.itmAASNS.setId(AAS_NO_SEQUENCE.toString());
        this.itmBPSEQ.setId(BPSEQ.toString());
        this.itmCT.setId(CT.toString());
        this.itmDB.setId(DB.toString());
        this.itmDBNS.setId(DB_NO_SEQUENCE.toString());
        this.itmFASTA.setId(FASTA.toString());
        //EventHandler<ActionEvent> event1 = e -> System.out.println((((MenuItem) e.getSource()).getText() + " selected"));
        EventHandler<ActionEvent> event1 = e -> {
            this.selectedFormat = RNAFormat.valueOf((((MenuItem) e.getSource()).getId()));  // set RNAFormat enum
            //System.out.println("sel: " + selectedFormat);
            this.btnSelectFormatTranslation.setText(String.valueOf((((MenuItem) e.getSource()).getText()))); // set String to display in MenuItem
            this.btnTranslateAllLoadedFiles.setDisable(false);  // when format translation is selected, translate btn is enabled
        };
        this.btnSelectFormatTranslation.getItems().forEach(f -> f.setOnAction(event1));

    }

    private void addFile(RNAFile rnaFile) {
        if (this.loadedFilesFormat == null) {
            var labelText = this.lblRecognizedFormat.getText();
            this.loadedFilesFormat = rnaFile.getFormat();
            this.lblRecognizedFormat.setText(labelText + " " + this.loadedFilesFormat.toString());
            this.lblRecognizedFormat.setVisible(true);
        }
        if (this.loadedFilesFormat != rnaFile.getFormat())
            throw new IllegalArgumentException("All loaded files must be of the same format!");
        this.filesTable.getItems().add(rnaFile);
    }
}
