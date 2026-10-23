package com.example.postsurgeryrecoverylog;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
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
    private static final String LIGHT_BLUE = "#2F8AD8";
    private static final String PALE_BLUE = "#E8EFF8";
    private static final String BACKGROUND = "#F5F6FA";

    private final List<RecoveryEntry> entries = new ArrayList<>();
    private final List<AdvisedActivity> activities = new ArrayList<>();

    private LocalDate selectedDate = LocalDate.now();
    private boolean providerMode = false;
    private boolean listMode = false;
    private boolean unreadReminder = false;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy");

    private static class RecoveryEntry {
        String type;
        String note;
        String photoPath;
        LocalDate date;

        RecoveryEntry(String type, String note, String photoPath, LocalDate date) {
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

        AdvisedActivity(String title, String instructions, LocalDate date) {
            this.title = title;
            this.instructions = instructions;
            this.date = date;
            this.completed = false;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        // Phuc: I created the main application window and connected
        // the patient and healthcare provider parts of the program.
        stage.setTitle("Post-Surgery Recovery Log");
        stage.setMinWidth(580);
        stage.setMinHeight(720);

        showHome();
        stage.show();
    }

    private Button makeButton(String text, String background, String textColor) {
        Button button = new Button(text);

        button.setStyle(
                "-fx-background-color: " + background + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-background-radius: 9;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;"
        );

        button.setPadding(new Insets(10, 15, 10, 15));

        button.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.03);
            scale.setToY(1.03);
            scale.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1);
            scale.setToY(1);
            scale.play();
        });

        return button;
    }

    private VBox makeCard() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setMaxWidth(Double.MAX_VALUE);

        card.setStyle(
                "-fx-background-color: " + BACKGROUND + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #E1E6EE;" +
                        "-fx-border-radius: 12;"
        );

        return card;
    }

    private Label heading(String text) {
        Label label = new Label(text);

        label.setStyle(
                "-fx-font-size: 21px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + BLUE + ";"
        );

        return label;
    }

    private Label section(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(10));

        label.setStyle(
                "-fx-background-color: #F0F0F5;" +
                        "-fx-text-fill: #666666;" +
                        "-fx-font-weight: bold;"
        );

        return label;
    }

    private void warning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Phuc: I used one main window and changed the page when the user
    // moves between the recovery log, entry form, and provider page.
    private void changePage(BorderPane page) {
        stage.setScene(new Scene(page, 580, 720));

        FadeTransition fade = new FadeTransition(Duration.millis(180), page);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private BorderPane makeHeader(String title, boolean homePage) {
        BorderPane header = new BorderPane();
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setStyle("-fx-background-color: " + BLUE + ";");

        if (!homePage) {
            Button back = makeButton("‹ Back", BLUE, "white");
            back.setOnAction(e -> showHome());

            Label pageTitle = new Label(title);
            pageTitle.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;"
            );

            header.setLeft(back);
            header.setCenter(pageTitle);
            return header;
        }

        Label appTitle = new Label("Recovery Log");
        appTitle.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 19px;" +
                        "-fx-font-weight: bold;"
        );

        header.setLeft(appTitle);

        Label bell = new Label("🔔");
        bell.setStyle("-fx-font-size: 23px; -fx-cursor: hand;");

        Label badge = new Label("1");
        badge.setAlignment(Pos.CENTER);
        badge.setPrefSize(18, 18);
        badge.setMaxSize(18, 18);
        badge.setVisible(unreadReminder);

        badge.setStyle(
                "-fx-background-color: red;" +
                        "-fx-background-radius: 20;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 10px;"
        );

        StackPane notification = new StackPane(bell, badge);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);

        notification.setOnMouseClicked(e -> {
            showNotifications();
            unreadReminder = false;
            badge.setVisible(false);
        });

        Button add = makeButton("+", BLUE, "white");
        add.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 28px;"
        );

        add.setOnAction(e -> {
            if (providerMode) {
                showAddActivity();
            } else {
                showAddEntry();
            }
        });

        HBox right = new HBox(8, notification, add);
        right.setAlignment(Pos.CENTER_RIGHT);
        header.setRight(right);

        return header;
    }

    private void showNotifications() {
        if (activities.isEmpty()) {
            info("Notifications", "No advised activities yet.");
            return;
        }

        StringBuilder message = new StringBuilder();

        for (AdvisedActivity activity : activities) {
            if (!activity.completed) {
                message.append("• ")
                        .append(activity.title)
                        .append(" - ")
                        .append(activity.date.format(DATE_FORMAT))
                        .append("\n");
            }
        }

        if (message.isEmpty()) {
            info("Advised Activities", "All activities are completed.");
        } else {
            info("Advised Activities", message.toString());
        }
    }

    // Phuc: I connected the role selector so we can demonstrate
    // both the patient side and healthcare provider side in one program.
    private void showHome() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color: white;");
        page.setTop(makeHeader("Recovery Log", true));

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label roleLabel = new Label("Demo Role:");

        ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("Patient", "Healthcare Provider");
        role.setValue(providerMode ? "Healthcare Provider" : "Patient");

        role.setOnAction(e -> {
            providerMode = role.getValue().equals("Healthcare Provider");
            showHome();
        });

        HBox roleBox = new HBox(10, roleLabel, role);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().add(roleBox);

        if (providerMode) {
            buildProviderPage(content);
        } else {
            buildPatientPage(content);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        page.setCenter(scroll);

        changePage(page);
    }

    // Johnathan: I worked on the patient interface where the patient
    // can view recovery entries, use the calendar, and see activities.
    private void buildPatientPage(VBox content) {
        content.getChildren().add(heading("My Recovery"));

        Button calendarButton = makeButton(
                "Calendar",
                listMode ? PALE_BLUE : BLUE,
                listMode ? BLUE : "white"
        );

        Button listButton = makeButton(
                "List",
                listMode ? BLUE : PALE_BLUE,
                listMode ? "white" : BLUE
        );

        calendarButton.setOnAction(e -> {
            listMode = false;
            showHome();
        });

        listButton.setOnAction(e -> {
            listMode = true;
            showHome();
        });

        content.getChildren().add(new HBox(10, calendarButton, listButton));

        if (!listMode) {
            content.getChildren().add(makeCalendar());
        }

        content.getChildren().add(section("RECOVERY LOG"));

        List<RecoveryEntry> visibleEntries = new ArrayList<>();

        for (RecoveryEntry entry : entries) {
            if (listMode || entry.date.equals(selectedDate)) {
                visibleEntries.add(entry);
            }
        }

        visibleEntries.sort(
                Comparator.comparing((RecoveryEntry entry) -> entry.date).reversed()
        );

        if (visibleEntries.isEmpty()) {
            content.getChildren().add(
                    new Label("No recovery entries yet.\nClick + to add a note or photo.")
            );
        } else {
            for (RecoveryEntry entry : visibleEntries) {
                content.getChildren().add(makeEntryCard(entry));
            }
        }

        content.getChildren().add(section("ADVISED ACTIVITIES"));

        if (activities.isEmpty()) {
            content.getChildren().add(
                    new Label("No activities assigned by the provider.")
            );
        } else {
            for (AdvisedActivity activity : activities) {
                content.getChildren().add(makeActivityCard(activity, true));
            }
        }
    }

    // Emmanuel: I worked on the healthcare provider page so the provider
    // can review patient recovery information and add advised activities.
    private void buildProviderPage(VBox content) {
        content.getChildren().add(
                heading("Healthcare Provider Dashboard")
        );

        Label description = new Label(
                "Review patient recovery notes, photos, and advised activities."
        );
        description.setWrapText(true);

        Button createActivity = makeButton(
                "+ Create Advised Activity",
                BLUE,
                "white"
        );

        createActivity.setOnAction(e -> showAddActivity());

        content.getChildren().addAll(description, createActivity);
        content.getChildren().add(section("PATIENT RECOVERY ENTRIES"));

        if (entries.isEmpty()) {
            content.getChildren().add(
                    new Label("No patient entries have been added.")
            );
        } else {
            List<RecoveryEntry> sortedEntries = new ArrayList<>(entries);

            sortedEntries.sort(
                    Comparator.comparing((RecoveryEntry entry) -> entry.date).reversed()
            );

            for (RecoveryEntry entry : sortedEntries) {
                content.getChildren().add(makeEntryCard(entry));
            }
        }

        content.getChildren().add(section("PROVIDER ADVISED ACTIVITIES"));

        if (activities.isEmpty()) {
            content.getChildren().add(
                    new Label("No advised activities created yet.")
            );
        } else {
            for (AdvisedActivity activity : activities) {
                content.getChildren().add(makeActivityCard(activity, false));
            }
        }
    }

    // Johnathan: I added the calendar so the patient can choose a date
    // and look at recovery information for that day.
    private VBox makeCalendar() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));

        box.setStyle(
                "-fx-background-color: " + BACKGROUND + ";" +
                        "-fx-background-radius: 12;"
        );

        Button previous = makeButton("‹", "white", BLUE);
        Button next = makeButton("›", "white", BLUE);

        Label month = new Label(
                selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        );

        previous.setOnAction(e -> {
            selectedDate = selectedDate.minusWeeks(1);
            showHome();
        });

        next.setOnAction(e -> {
            selectedDate = selectedDate.plusWeeks(1);
            showHome();
        });

        HBox navigation = new HBox(20, previous, month, next);
        navigation.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setAlignment(Pos.CENTER);

        String[] dayNames = {
                "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
        };

        LocalDate sunday = selectedDate.minusDays(
                selectedDate.getDayOfWeek().getValue() % 7
        );

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            Label dayName = new Label(dayNames[i]);

            Button dateButton = new Button(
                    String.valueOf(date.getDayOfMonth())
            );

            boolean selected = date.equals(selectedDate);

            dateButton.setStyle(
                    "-fx-background-color: " +
                            (selected ? LIGHT_BLUE : "white") + ";" +
                            "-fx-text-fill: " +
                            (selected ? "white" : "black") + ";" +
                            "-fx-background-radius: 30;"
            );

            dateButton.setOnAction(e -> {
                selectedDate = date;
                showHome();
            });

            VBox day = new VBox(5, dayName, dateButton);
            day.setAlignment(Pos.CENTER);

            grid.add(day, i, 0);
        }

        box.getChildren().addAll(navigation, grid);
        return box;
    }

    private VBox makeEntryCard(RecoveryEntry entry) {
        VBox entryCard = makeCard();

        Label title = new Label(entry.type);
        title.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + BLUE + ";"
        );

        Label date = new Label(
                entry.date.format(DATE_FORMAT)
        );

        entryCard.getChildren().addAll(title, date);

        if (!entry.note.isBlank()) {
            Label note = new Label(entry.note);
            note.setWrapText(true);
            entryCard.getChildren().add(note);
        }

        if (entry.photoPath != null) {
            Button viewPhoto = makeButton(
                    "📷 View Photo",
                    PALE_BLUE,
                    BLUE
            );

            viewPhoto.setOnAction(e ->
                    openPhoto(entry.photoPath)
            );

            entryCard.getChildren().add(viewPhoto);
        }

        return entryCard;
    }

    // Johnathan: I added the View Photo option so a patient or provider
    // can open the recovery picture that was attached to an entry.
    private void openPhoto(String photoPath) {
        File file = new File(photoPath);

        if (!file.exists()) {
            warning("Photo Error", "The photo file was not found.");
            return;
        }

        Image image = new Image(file.toURI().toString());

        if (image.isError()) {
            warning("Photo Error", "The photo could not be opened.");
            return;
        }

        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitWidth(700);
        view.setFitHeight(500);

        StackPane root = new StackPane(view);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #202020;");

        Stage photoWindow = new Stage();
        photoWindow.setTitle("Recovery Photo");
        photoWindow.setScene(new Scene(root, 750, 550));
        photoWindow.show();
    }

    private VBox makeActivityCard(
            AdvisedActivity activity,
            boolean patientView) {

        VBox activityCard = makeCard();

        Label title = new Label(activity.title);
        title.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + BLUE + ";"
        );

        Label date = new Label(
                "Advised for: " +
                        activity.date.format(DATE_FORMAT)
        );

        Label instructions = new Label(activity.instructions);
        instructions.setWrapText(true);

        Label status = new Label(
                activity.completed
                        ? "Status: Completed"
                        : "Status: Pending"
        );

        activityCard.getChildren().addAll(
                title,
                date,
                instructions,
                status
        );

        if (patientView) {
            Button complete = makeButton(
                    activity.completed
                            ? "Mark Pending"
                            : "Mark Completed",
                    BLUE,
                    "white"
            );

            complete.setOnAction(e -> {
                activity.completed = !activity.completed;
                showHome();
            });

            activityCard.getChildren().add(complete);
        }

        return activityCard;
    }

    // Johnathan: I added the recovery entry form so the patient
    // can select a date, write a recovery note, and attach a photo.
    private void showAddEntry() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color: white;");
        page.setTop(makeHeader("New Recovery Entry", false));

        VBox form = new VBox(15);
        form.setPadding(new Insets(30));

        DatePicker datePicker = new DatePicker(selectedDate);

        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll(
                "Recovery Note",
                "Recovery Photo",
                "Activity Update"
        );
        type.setPromptText("Select entry type");

        TextArea note = new TextArea();
        note.setPromptText("Describe your recovery progress...");
        note.setWrapText(true);
        note.setPrefHeight(150);

        final String[] photoPath = {null};

        Label photoName = new Label("No photo selected");

        Button addPhoto = makeButton(
                "📷 Add Photo",
                PALE_BLUE,
                BLUE
        );

        addPhoto.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Recovery Photo");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Image Files",
                            "*.png",
                            "*.jpg",
                            "*.jpeg",
                            "*.gif"
                    )
            );

            File file = chooser.showOpenDialog(stage);

            if (file != null) {
                Image testImage = new Image(file.toURI().toString());

                if (testImage.isError()) {
                    warning("Photo Error", "This image could not be opened.");
                    return;
                }

                photoPath[0] = file.getAbsolutePath();
                photoName.setText(file.getName());
            }
        });

        Button save = makeButton(
                "Save Recovery Entry",
                BLUE,
                "white"
        );

        save.setOnAction(e -> {
            if (type.getValue() == null ||
                    datePicker.getValue() == null) {

                warning(
                        "Missing Information",
                        "Select an entry type and date."
                );
                return;
            }

            String noteText = note.getText().trim();

            if (noteText.isEmpty() && photoPath[0] == null) {
                warning(
                        "Missing Information",
                        "Enter a note or add a photo."
                );
                return;
            }

            entries.add(
                    new RecoveryEntry(
                            type.getValue(),
                            noteText,
                            photoPath[0],
                            datePicker.getValue()
                    )
            );

            selectedDate = datePicker.getValue();
            listMode = false;
            showHome();
        });

        form.getChildren().addAll(
                new Label("Entry Date"),
                datePicker,
                new Label("Entry Type"),
                type,
                new Label("Recovery Note"),
                note,
                addPhoto,
                photoName,
                save
        );

        page.setCenter(form);
        changePage(page);
    }

    // Emmanuel: I added the advised activity form so the healthcare
    // provider can give the patient an activity and instructions.
    private void showAddActivity() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color: white;");

        page.setTop(
                makeHeader(
                        "Create Advised Activity",
                        false
                )
        );

        VBox form = new VBox(15);
        form.setPadding(new Insets(30));

        TextField title = new TextField();
        title.setPromptText("Example: Daily Walking");

        TextArea instructions = new TextArea();
        instructions.setPromptText("Enter provider instructions...");
        instructions.setWrapText(true);
        instructions.setPrefHeight(150);

        DatePicker datePicker =
                new DatePicker(LocalDate.now());

        Button save = makeButton(
                "Save Advised Activity",
                BLUE,
                "white"
        );

        save.setOnAction(e -> {
            String activityTitle = title.getText().trim();
            String activityInstructions = instructions.getText().trim();

            if (activityTitle.isEmpty() ||
                    activityInstructions.isEmpty() ||
                    datePicker.getValue() == null) {

                warning(
                        "Missing Information",
                        "Complete all activity fields."
                );
                return;
            }

            activities.add(
                    new AdvisedActivity(
                            activityTitle,
                            activityInstructions,
                            datePicker.getValue()
                    )
            );

            unreadReminder = true;
            showHome();
        });

        form.getChildren().addAll(
                new Label("Activity Title"),
                title,
                new Label("Provider Instructions"),
                instructions,
                new Label("Activity Date"),
                datePicker,
                save
        );

        page.setCenter(form);
        changePage(page);
    }

    // Pedro: I tested the recovery entry, photo upload/view, patient page, provider page, and activity features before
    // the Sprint demonstration to make sure the program worked.

    public static void main(String[] args) {
        launch(args);
    }
}