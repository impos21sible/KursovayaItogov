package org.main.autoschoolapp.controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.main.autoschoolapp.AutoschoolApp;
import org.main.autoschoolapp.model.ClassRoom;
import org.main.autoschoolapp.model.Lesson;
import org.main.autoschoolapp.model.Status;
import org.main.autoschoolapp.service.ClassRoomService;
import org.main.autoschoolapp.service.LessonService;
import org.main.autoschoolapp.service.StatusService;
import org.main.autoschoolapp.util.Manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

public class ScheduleViewController {
    private static final String FONT = "src/main/resources/fonts/arial.ttf";
    private final LessonService lessonService = new LessonService();
    private final ClassRoomService classRoomService = new ClassRoomService();
    private final StatusService statusService = new StatusService();

    @FXML
    private TableView<Lesson> lessonTable;
    @FXML
    private TableColumn<Lesson, Long> lessonIdColumn;
    @FXML
    private TableColumn<Lesson, String> startDateColumn;
    @FXML
    private TableColumn<Lesson, String> endDateColumn;
    @FXML
    private TableColumn<Lesson, String> classRoomColumn;
    @FXML
    private TableColumn<Lesson, String> statusColumn;

    @FXML
    public void initialize() {
        lessonIdColumn.setCellValueFactory(new PropertyValueFactory<>("lessonId"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        classRoomColumn.setCellValueFactory(new PropertyValueFactory<>("classRoom"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        lessonTable.setItems(FXCollections.observableArrayList(lessonService.findAll()));
    }

    @FXML
    public void handleAdd() {
        Stage newWindow = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(AutoschoolApp.class.getResource("editLesson.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add("base-styles.css");
            newWindow.setTitle("Изменить данные");
            newWindow.initOwner(Manager.secondStage);
            newWindow.initModality(Modality.WINDOW_MODAL);
            newWindow.setScene(scene);
            Manager.currentStage = newWindow;
            newWindow.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Manager.currentStage = null;
        }
    }

    @FXML
    public void handleDelete() {
        Lesson selectedLesson = lessonTable.getSelectionModel().getSelectedItem();
        if (selectedLesson != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Вы уверены, что хотите удалить урок?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                lessonService.delete(selectedLesson);
                lessonTable.getItems().remove(selectedLesson);
            }
        } else {
            showAlert("Ошибка", "Выберите урок для удаления");
        }
    }

    @FXML
    public void handlePrint() {
        try {
            printScheduleToPDF();
        } catch (DocumentException | IOException e) {
            showAlert("Ошибка", "Не удалось создать PDF файл: " + e.getMessage());
        }
    }

    private void printScheduleToPDF() throws DocumentException, IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.PDF)", "*.pdf"));
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(file));
            Font titleFont = FontFactory.getFont(FONT, "cp1251", BaseFont.EMBEDDED, 18);
            Font contentFont = FontFactory.getFont(FONT, "cp1251", BaseFont.EMBEDDED, 12);

            document.open();

            // Заголовок
            Paragraph title = new Paragraph("РАСПИСАНИЕ ЗАНЯТИЙ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Цикл для уроков
            document.add(new Paragraph("Уроки:", contentFont));
            for (Lesson lesson : lessonService.findAll()) {
                String lessonDetails = String.format("Дата начала: %s, Дата окончания: %s, Кабинет: %s",
                        lesson.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        lesson.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        lesson.getClassRoom() != null ? lesson.getClassRoom().getTitle() : "N/A");

                document.add(new Paragraph(lessonDetails, contentFont));
            }






            document.close();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
