package it.unicam.cs.twopie.tarnas.view.utils;

import it.unicam.cs.twopie.tarnas.controller.IOController;
import it.unicam.cs.twopie.tarnas.model.rnafile.RNAFile;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.stage.Modality;

import java.util.Optional;

public class DeleteCell extends TableCell<RNAFile, RNAFile> {

    private final ImageButton imageButton;

    private final Alert trashAlert;

    public DeleteCell(Image image) {
        this.imageButton = new ImageButton(image);
        this.trashAlert = new Alert(Alert.AlertType.CONFIRMATION);
    }

    @Override
    protected void updateItem(RNAFile rnaFile, boolean empty) {
        super.updateItem(rnaFile, empty);
        if (rnaFile == null) {
            setGraphic(null);
            return;
        }
        setGraphic(imageButton);
        this.imageButton.setOnMouseClicked(event -> {
            if (this.confirmAndRemoveFile(rnaFile)) {
                getTableView().getItems().remove(rnaFile);
                IOController.getInstance().deleteFile(rnaFile);
            }
        });
    }

    private boolean confirmAndRemoveFile(RNAFile rnaFile) {
        //alert.initOwner(stage);
        this.trashAlert.initModality(Modality.WINDOW_MODAL);
        this.trashAlert.setTitle("Deleting file confirmation");
        this.trashAlert.setHeaderText("Delete \"" + rnaFile.getFileName() + "\"?");
        this.trashAlert.setContentText("Are you sure you want to delete this file?");
        Optional<ButtonType> result = this.trashAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}