
package com.example.postsurgeryrecoverylog;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HelloApplication extends Application {

    private Stage stage;

    private static final String BLUE = "#38588D";
    private static final String SKY = "#2F8AD8";
    private static final String PALE = "#E8EFF8";
    private static final String BG = "#F5F6FA";

    private final List<RecoveryEntry> entries = new ArrayList<>();
    private final List<AdvisedActivity> activities = new ArrayList<>();

    private LocalDate selectedDate = LocalDate.now();
    private boolean providerMode = false;
    private boolean listMode = false;
    private boolean unreadReminder = false;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy");

    // DATA

    private static class RecoveryEntry {
        String type;
        String note;
        String photoPath;
        LocalDate date;

        RecoveryEntry(String type, String note,
                      String photoPath, LocalDate date) {
            this.type = type;
            this.note = note;
            this.photoPath = photoPath;
            this.date = date;
        }
    }

    private static class AdvisedActivity {
        String title;
        String instructions;
        LocalDate date;
        boolean completed;

        AdvisedActivity(String title, String instructions,
                        LocalDate date) {
            this.title = title;
            this.instructions = instructions;
            this.date = date;
            this.completed = false;
        }
    }

    // START

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Post-Surgery Recovery Log");
        stage.setMinWidth(580);
        stage.setMinHeight(720);

        showHome();
        stage.show();
    }

    // UI HELPERS

    private Button button(String text, String background,
                          String foreground) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: " + background + ";" +
                        "-fx-text-fill: " + foreground + ";" +
                        "-fx-background-radius: 9;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;"
        );
        b.setPadding(new Insets(11, 16, 11, 16));

        b.setOnMouseEntered(e -> {
            ScaleTransition t =
                    new ScaleTransition(Duration.millis(120), b);
            t.setToX(1.03);
            t.setToY(1.03);
            t.play();
        });

        b.setOnMouseExited(e -> {
            ScaleTransition t =
                    new ScaleTransition(Duration.millis(120), b);
            t.setToX(1);
            t.setToY(1);
            t.play();
        });

        return b;
    }

    private VBox card() {
        VBox v = new VBox(10);
        v.setPadding(new Insets(17));
        v.setMaxWidth(Double.MAX_VALUE);
        v.setStyle(
                "-fx-background-color: " + BG + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #E1E6EE;" +
                        "-fx-border-radius: 12;"
        );
        return v;
    }

    private Label heading(String text) {
        Label l = new Label(text);
        l.setStyle(
                "-fx-font-size: 21px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + BLUE + ";"
        );
        return l;
    }

    private Label section(String text) {
        Label l = new Label(text);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setPadding(new Insets(12));
        l.setStyle(
                "-fx-background-color: #F0F0F5;" +
                        "-fx-text-fill: #666666;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );
        return l;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );
        return l;
    }

    private void alert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private void info(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private void display(BorderPane page) {
        Scene scene = new Scene(page, 580, 720);
        stage.setScene(scene);

        FadeTransition fade =
                new FadeTransition(Duration.millis(220), page);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private BorderPane header(String title, boolean home) {
        BorderPane bar = new BorderPane();
        bar.setPadding(new Insets(14, 18, 14, 18));
        bar.setStyle("-fx-background-color: " + BLUE + ";");

        if (!home) {
            Button back = button("‹ Back", BLUE, "white");
            back.setOnAction(e -> showHome());

            Label label = new Label(title);
            label.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;"
            );

            bar.setLeft(back);
            bar.setCenter(label);
            return bar;
        }

        Label appName = new Label("Recovery Log");
        appName.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 19px;" +
                        "-fx-font-weight: bold;"
        );
        bar.setLeft(appName);

        Label bell = new Label("🔔");
        bell.setStyle("-fx-font-size: 25px; -fx-cursor: hand;");

        Label badge = new Label("1");
        badge.setAlignment(Pos.CENTER);
        badge.setPrefSize(19, 19);
        badge.setMaxSize(19, 19);
        badge.setStyle(
                "-fx-background-color: red;" +
                        "-fx-background-radius: 20;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 11px;"
        );
        badge.setVisible(unreadReminder);

        StackPane notification = new StackPane(bell, badge);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        notification.setPrefSize(44, 44);
        notification.setStyle("-fx-cursor: hand;");

        notification.setOnMouseClicked(e -> {
            TranslateTransition shake =
                    new TranslateTransition(Duration.millis(65), bell);
            shake.setFromX(-5);
            shake.setToX(5);
            shake.setCycleCount(8);
            shake.setAutoReverse(true);
            shake.play();

            if (activities.isEmpty()) {
                info("Notifications", "No advised activities yet.");
            } else {
                StringBuilder message = new StringBuilder();
                for (AdvisedActivity a : activities) {
                    if (!a.completed) {
                        message.append("• ")
                                .append(a.title)
                                .append(" — ")
                                .append(a.date.format(DATE_FORMAT))
                                .append("\n");
                    }
                }
                info("Advised Activities",
                        message.length() == 0
                                ? "All activities are completed."
                                : message.toString());
            }

            unreadReminder = false;
            badge.setVisible(false);
        });

        Button add = button("+", BLUE, "white");
        add.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 29px;" +
                        "-fx-cursor: hand;"
        );
        add.setOnAction(e -> {
            if (providerMode) {
                showAddActivity();
            } else {
                showAddEntry();
            }
        });

        HBox actions = new HBox(10, notification, add);
        actions.setAlignment(Pos.CENTER_RIGHT);
        bar.setRight(actions);
        return bar;
    }

    // HOME

    private void showHome() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color: white;");
        page.setTop(header("Recovery Log", true));

        VBox content = new VBox(16);
        content.setPadding(new Insets(20));

        HBox roleRow = new HBox(12);
        roleRow.setAlignment(Pos.CENTER_LEFT);

        Label roleText = fieldLabel("Demo Role:");

        ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("Patient", "Healthcare Provider");
        role.setValue(providerMode
                ? "Healthcare Provider" : "Patient");

        role.setOnAction(e -> {
            providerMode = "Healthcare Provider"
                    .equals(role.getValue());
            showHome();
        });

        roleRow.getChildren().addAll(roleText, role);
        content.getChildren().add(roleRow);

        if (providerMode) {
            buildProviderDashboard(content);
        } else {
            buildPatientDashboard(content);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle(
                "-fx-background: white;" +
                        "-fx-background-color: white;"
        );
        page.setCenter(scroll);

        display(page);
    }

    // PATIENT

    private void buildPatientDashboard(VBox content) {
        content.getChildren().add(heading("My Recovery"));

        HBox toggles = new HBox(10);

        Button calendar = button(
                "Calendar",
                listMode ? PALE : BLUE,
                listMode ? BLUE : "white"
        );

        Button list = button(
                "List",
                listMode ? BLUE : PALE,
                listMode ? "white" : BLUE
        );

        calendar.setOnAction(e -> {
            listMode = false;
            showHome();
        });

        list.setOnAction(e -> {
            listMode = true;
            showHome();
        });

        toggles.getChildren().addAll(calendar, list);
        content.getChildren().add(toggles);

        if (!listMode) {
            content.getChildren().add(createCalendar());
        }

        content.getChildren().add(section("RECOVERY LOG"));

        List<RecoveryEntry> visible = new ArrayList<>();
        for (RecoveryEntry entry : entries) {
            if (listMode || entry.date.equals(selectedDate)) {
                visible.add(entry);
            }
        }

        visible.sort(Comparator.comparing(
                (RecoveryEntry e) -> e.date
        ).reversed());

        if (visible.isEmpty()) {
            Label empty = new Label(
                    "No recovery entries yet.\nClick + to add a note or photo."
            );
            empty.setStyle(
                    "-fx-text-fill: #777777;" +
                            "-fx-font-size: 15px;"
            );
            content.getChildren().add(empty);
        } else {
            for (RecoveryEntry entry : visible) {
                content.getChildren().add(entryCard(entry));
            }
        }

        content.getChildren().add(section("ADVISED ACTIVITIES"));

        if (activities.isEmpty()) {
            content.getChildren().add(
                    new Label("No activities assigned by the provider.")
            );
        } else {
            for (AdvisedActivity a : activities) {
                content.getChildren().add(activityCard(a, true));
            }
        }
    }

    // PROVIDER
    private void buildProviderDashboard(VBox content) {
        content.getChildren().add(
                heading("Healthcare Provider Dashboard")
        );

        Label description = new Label(
                "Review patient recovery notes, view photos, " +
                        "and create advised activities."
        );
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #777777;");

        Button create = button(
                "+ Create Advised Activity", BLUE, "white"
        );
        create.setOnAction(e -> showAddActivity());

        content.getChildren().addAll(description, create);
        content.getChildren().add(
                section("PATIENT RECOVERY ENTRIES")
        );

        if (entries.isEmpty()) {
            content.getChildren().add(
                    new Label("No patient entries have been added.")
            );
        } else {
            List<RecoveryEntry> sorted = new ArrayList<>(entries);
            sorted.sort(Comparator.comparing(
                    (RecoveryEntry e) -> e.date
            ).reversed());

            for (RecoveryEntry entry : sorted) {
                content.getChildren().add(entryCard(entry));
            }
        }

        content.getChildren().add(
                section("PROVIDER ADVISED ACTIVITIES")
        );

        if (activities.isEmpty()) {
            content.getChildren().add(
                    new Label("No advised activities created yet.")
            );
        } else {
            for (AdvisedActivity a : activities) {
                content.getChildren().add(activityCard(a, false));
            }
        }
    }

    // CALENDAR

    private VBox createCalendar() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle(
                "-fx-background-color: " + BG + ";" +
                        "-fx-background-radius: 12;"
        );

        HBox nav = new HBox(22);
        nav.setAlignment(Pos.CENTER);

        Button previous = button("‹", "white", BLUE);
        Button next = button("›", "white", BLUE);

        Label month = new Label(
                selectedDate.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy")
                )
        );
        month.setStyle(
                "-fx-font-size: 21px;" +
                        "-fx-text-fill: " + BLUE + ";"
        );

        previous.setOnAction(e -> {
            selectedDate = selectedDate.minusWeeks(1);
            showHome();
        });

        next.setOnAction(e -> {
            selectedDate = selectedDate.plusWeeks(1);
            showHome();
        });

        nav.getChildren().addAll(previous, month, next);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);

        String[] names = {
                "Sun", "Mon", "Tue", "Wed",
                "Thu", "Fri", "Sat"
        };

        LocalDate sunday = selectedDate.minusDays(
                selectedDate.getDayOfWeek().getValue() % 7
        );

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            VBox day = new VBox(6);
            day.setAlignment(Pos.CENTER);
            day.setPrefWidth(55);

            Label name = new Label(names[i]);
            name.setStyle("-fx-text-fill: " + BLUE + ";");

            Button dateButton = new Button(
                    String.valueOf(date.getDayOfMonth())
            );
            dateButton.setPrefSize(42, 42);

            boolean selected = date.equals(selectedDate);

            dateButton.setStyle(
                    "-fx-background-color: " +
                            (selected ? SKY : "white") + ";" +
                            "-fx-text-fill: " +
                            (selected ? "white" : "#333333") + ";" +
                            "-fx-background-radius: 50;" +
                            "-fx-cursor: hand;"
            );

            dateButton.setOnAction(e -> {
                selectedDate = date;
                showHome();
            });

            day.getChildren().addAll(name, dateButton);
            grid.add(day, i, 0);
        }

        box.getChildren().addAll(nav, grid);
        return box;
    }

    // RECOVERY CARD

    private VBox entryCard(RecoveryEntry entry) {
        VBox box = card();

        Label title = heading(entry.type);
        title.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + BLUE + ";"
        );

        Label date = new Label(entry.date.format(DATE_FORMAT));
        date.setStyle("-fx-text-fill: #777777;");

        box.getChildren().addAll(title, date);

        if (!entry.note.isBlank()) {
            Label note = new Label(entry.note);
            note.setWrapText(true);
            box.getChildren().add(note);
        }

        if (entry.photoPath != null) {
            Button view = button("📷 View Photo", PALE, BLUE);
            view.setOnAction(e -> viewPhoto(entry.photoPath));
            box.getChildren().add(view);
        }

        return box;
    }

    // VIEW PHOTO

    private void viewPhoto(String photoPath) {
        File file = new File(photoPath);

        if (!file.isFile()) {
            alert("Photo Error", "The photo file was not found.");
            return;
        }

        Image image = new Image(file.toURI().toString(), false);

        if (image.isError()) {
            alert("Photo Error", "Unable to open this photo.");
            return;
        }

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(700);
        imageView.setFitHeight(520);

        StackPane viewer = new StackPane(imageView);
        viewer.setPadding(new Insets(20));
        viewer.setStyle("-fx-background-color: #202020;");

        Stage photoWindow = new Stage();
        photoWindow.initOwner(stage);
        photoWindow.initModality(Modality.NONE);
        photoWindow.setTitle("View Recovery Photo");
        photoWindow.setScene(new Scene(viewer, 750, 570));
        photoWindow.show();
    }

    // ACTIVITY CARD

    private VBox activityCard(AdvisedActivity a,
                              boolean patientView) {
        VBox box = card();

        Label title = heading(a.title);
        title.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + BLUE + ";"
        );

        Label date = new Label(
                "Advised for: " + a.date.format(DATE_FORMAT)
        );

        Label instructions = new Label(a.instructions);
        instructions.setWrapText(true);

        Label status = new Label(
                a.completed ? "Status: Completed" : "Status: Pending"
        );
        status.setStyle(
                "-fx-text-fill: " +
                        (a.completed ? "#24864A" : "#B7791F") + ";"
        );

        box.getChildren().addAll(
                title, date, instructions, status
        );

        if (patientView) {
            Button update = button(
                    a.completed ? "Mark Pending" : "Mark Completed",
                    BLUE, "white"
            );

            update.setOnAction(e -> {
                a.completed = !a.completed;
                showHome();
            });

            box.getChildren().add(update);
        }

        return box;
    }

    // ADD PATIENT ENTRY

    private void showAddEntry() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color: white;");
        page.setTop(header("New Recovery Entry", false));

        VBox form = new VBox(17);
        form.setPadding(new Insets(30));

        DatePicker date = new DatePicker(selectedDate);
        date.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll(
                "Recovery Note",
                "Recovery Photo",
                "Activity Update"
        );
        type.setPromptText("Select entry type");
        type.setMaxWidth(Double.MAX_VALUE);

        TextArea note = new TextArea();
        note.setPromptText(
                "Describe your recovery progress..."
        );
        note.setWrapText(true);
        note.setPrefHeight(170);

        Label photoName = new Label("No photo selected");
        photoName.setStyle("-fx-text-fill: #777777;");

        final String[] photoPath = {null};

        Button addPhoto = button("📷 Add Photo", PALE, BLUE);
        addPhoto.setMaxWidth(Double.MAX_VALUE);

        addPhoto.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Recovery Photo");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Images",
                            "*.png", "*.jpg", "*.jpeg", "*.gif"
                    )
            );

            File file = chooser.showOpenDialog(stage);

            if (file != null) {
                Image test = new Image(
                        file.toURI().toString(), false
                );

                if (test.isError()) {
                    alert("Photo Error",
                            "This image could not be opened.");
                    return;
                }

                photoPath[0] = file.getAbsolutePath();
                photoName.setText(file.getName());
            }
        });

        Button save = button("Save Recovery Entry", BLUE, "white");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setPrefHeight(48);

        save.setOnAction(e -> {
            if (type.getValue() == null || date.getValue() == null) {
                alert("Missing Information",
                        "Select an entry type and date.");
                return;
            }

            String text = note.getText().trim();

            if (text.isEmpty() && photoPath[0] == null) {
                alert("Missing Information",
                        "Enter a note or add a photo.");
                return;
            }

            entries.add(new RecoveryEntry(
                    type.getValue(),
                    text,
                    photoPath[0],
                    date.getValue()
            ));

            selectedDate = date.getValue();
            listMode = false;
            showHome();
        });

        form.getChildren().addAll(
                fieldLabel("Entry Date"),
                date,
                fieldLabel("Entry Type"),
                type,
                fieldLabel("Recovery Note"),
                note,
                addPhoto,
                photoName,
                save
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        page.setCenter(scroll);

        display(page);
    }

    // ADD PROVIDER ACTIVITY

    private void showAddActivity() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color: white;");
        page.setTop(header("Create Advised Activity", false));

        VBox form = new VBox(18);
        form.setPadding(new Insets(30));

        TextField title = new TextField();
        title.setPromptText("Example: Daily Walking");

        TextArea instructions = new TextArea();
        instructions.setPromptText(
                "Enter the provider's advised activity..."
        );
        instructions.setWrapText(true);
        instructions.setPrefHeight(160);

        DatePicker date = new DatePicker(LocalDate.now());
        date.setMaxWidth(Double.MAX_VALUE);

        Button save = button(
                "Save Advised Activity", BLUE, "white"
        );
        save.setMaxWidth(Double.MAX_VALUE);

        save.setOnAction(e -> {
            String name = title.getText().trim();
            String details = instructions.getText().trim();

            if (name.isEmpty() ||
                    details.isEmpty() ||
                    date.getValue() == null) {
                alert("Missing Information",
                        "Complete all activity fields.");
                return;
            }

            activities.add(new AdvisedActivity(
                    name, details, date.getValue()
            ));

            unreadReminder = true;
            showHome();
        });

        form.getChildren().addAll(
                fieldLabel("Activity Title"),
                title,
                fieldLabel("Provider Instructions"),
                instructions,
                fieldLabel("Activity Date"),
                date,
                save
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        page.setCenter(scroll);

        display(page);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
