package teamup.ui.pages;

import java.time.LocalDate;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import teamup.db.DatabaseManager;
import teamup.model.Activity;
import teamup.model.ContactValidator;
import teamup.ui.PageNavigator;
import teamup.ui.components.AppIcons;

/**
 * CreateActivityPage – Classical, structured activity creation form.
 * Styled in Slate Grav (#203C3D) and Soft Slate (#344F50) with Peach Buzz (#FEDAB8) typography.
 */
public class CreateActivityPage extends BorderPane {

    private final PageNavigator navigator;

    // Edit mode state
    private Integer editingActivityId = null;
    private String returnPageKey = "EXPLORE";

    // Header labels & action button references
    private final Label kickerLabel;
    private final Label titleLabel;
    private final Label subLabel;
    private final Button submitBtn;
    private final Button cancelBtn;

    // Form inputs
    private final ComboBox<String> categoryCombo;
    private final TextField titleField;
    private final TextArea descArea;
    private final DatePicker datePicker;
    private final TextField locationField;
    private final Spinner<Integer> maxMembersSpinner;
    private final TextField createdByField;
    private final ComboBox<String> statusCombo;

    // Contact Information inputs
    private final TextField contactEmailField;
    private final TextField contactPhoneField;

    // Dynamic category section container
    private final VBox dynamicFieldsBox;
    private final TextField dynamicField1;
    private final TextField dynamicField2;
    private final Label dynamicLabel1;
    private final Label dynamicLabel2;

    public CreateActivityPage(PageNavigator navigator) {
        this.navigator = navigator;

        getStyleClass().add("home-root-pane");

        // ── SCROLLABLE FORM BODY ──────────────────────────────────────────────
        VBox pageBody = new VBox(24);
        pageBody.setAlignment(Pos.TOP_CENTER);
        pageBody.setPadding(new Insets(32, 40, 48, 40));
        pageBody.getStyleClass().add("home-root-pane");

        VBox contentBox = new VBox(24);
        contentBox.setMaxWidth(920);
        contentBox.setAlignment(Pos.TOP_LEFT);

        // ── PAGE HEADER ───────────────────────────────────────────────────────
        VBox headerBox = new VBox(4);
        kickerLabel = new Label("COMMUNITY INITIATIVE · SRM UNIVERSITY-AP");
        kickerLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");

        titleLabel = new Label("Create a Campus Activity");
        titleLabel.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 32px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");

        Region rule = new Region();
        rule.setStyle("-fx-background-color: #FEDAB8; -fx-pref-width: 48px; -fx-max-width: 48px; -fx-pref-height: 2px; -fx-min-height: 2px;");

        subLabel = new Label("Fill in the details below to publish your activity and recruit teammates across campus.");
        subLabel.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #E6D5C5;");

        headerBox.getChildren().addAll(kickerLabel, titleLabel, rule, subLabel);

        // ── FORM CARD ─────────────────────────────────────────────────────────
        VBox formCard = new VBox(24);
        formCard.setStyle("-fx-background-color: #264445; "
                + "-fx-background-radius: 14px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.22); "
                + "-fx-border-radius: 14px; "
                + "-fx-padding: 32 38; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 14, 0, 0, 3);");

        // ── SECTION 1: Basic Information ──────────────────────────────────────
        VBox sec1 = new VBox(10);
        Label sec1Title = createSectionTitle("1. Basic Information");

        Label catLbl = createFieldLabel("Category *");
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Travel", "Events", "Sports", "Hackathons", "Study Groups", "Entertainment");
        categoryCombo.setValue("Travel");
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        categoryCombo.setPrefHeight(40);
        styleDarkControl(categoryCombo);
        categoryCombo.setOnAction(e -> updateDynamicFields(categoryCombo.getValue()));

        Label titleLbl = createFieldLabel("Activity Title *");
        titleField = new TextField();
        titleField.setPromptText("e.g. Smart India Hackathon 2025 - SRM-AP Selection Team");
        titleField.setPrefHeight(40);
        styleDarkControl(titleField);

        Label descLbl = createFieldLabel("Description");
        descArea = new TextArea();
        descArea.setPromptText("Describe the activity, agenda, prerequisites, and who you're looking for...");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        styleDarkControl(descArea);

        sec1.getChildren().addAll(sec1Title, catLbl, categoryCombo, titleLbl, titleField, descLbl, descArea);

        // ── SECTION 2: Date & Location ────────────────────────────────────────
        VBox sec2 = new VBox(10);
        Label sec2Title = createSectionTitle("2. Date & Location");

        GridPane dateLocGrid = new GridPane();
        dateLocGrid.setHgap(16);
        dateLocGrid.setVgap(8);

        ColumnConstraints colHalf1 = new ColumnConstraints(); colHalf1.setPercentWidth(50);
        ColumnConstraints colHalf2 = new ColumnConstraints(); colHalf2.setPercentWidth(50);
        dateLocGrid.getColumnConstraints().addAll(colHalf1, colHalf2);

        Label dateLbl = createFieldLabel("Activity Date *");
        datePicker = new DatePicker(LocalDate.now().plusDays(3));
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setPrefHeight(40);
        styleDarkControl(datePicker);
        // Disable past dates in DatePicker calendar view
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: rgba(40, 60, 60, 0.4); -fx-text-fill: #5C7071;");
                }
            }
        });

        Label locLbl = createFieldLabel("Location / Venue *");
        locationField = new TextField();
        locationField.setPromptText("e.g. Innovation Hub Block A / Sports Complex");
        locationField.setPrefHeight(40);
        styleDarkControl(locationField);

        dateLocGrid.add(dateLbl, 0, 0);
        dateLocGrid.add(datePicker, 0, 1);
        dateLocGrid.add(locLbl, 1, 0);
        dateLocGrid.add(locationField, 1, 1);

        sec2.getChildren().addAll(sec2Title, dateLocGrid);

        // ── SECTION 3: Participants & Host ────────────────────────────────────
        VBox sec3 = new VBox(10);
        Label sec3Title = createSectionTitle("3. Participants & Organizer");

        GridPane partGrid = new GridPane();
        partGrid.setHgap(16);
        partGrid.setVgap(8);

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(33.33);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(33.33);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(33.34);
        partGrid.getColumnConstraints().addAll(c1, c2, c3);

        Label maxLbl = createFieldLabel("Max Members *");
        maxMembersSpinner = new Spinner<>(2, 100, 5);
        maxMembersSpinner.setMaxWidth(Double.MAX_VALUE);
        maxMembersSpinner.setPrefHeight(40);
        maxMembersSpinner.setEditable(true);
        styleDarkControl(maxMembersSpinner);

        Label hostLbl = createFieldLabel("Host Student Reg ID *");
        createdByField = new TextField(navigator.getCurrentStudentReg());
        createdByField.setPromptText("e.g. AP21110010001");
        createdByField.setPrefHeight(40);
        styleDarkControl(createdByField);

        Label statusLbl = createFieldLabel("Initial Status");
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Open", "Closed");
        statusCombo.setValue("Open");
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setPrefHeight(40);
        styleDarkControl(statusCombo);

        partGrid.add(maxLbl, 0, 0);
        partGrid.add(maxMembersSpinner, 0, 1);
        partGrid.add(hostLbl, 1, 0);
        partGrid.add(createdByField, 1, 1);
        partGrid.add(statusLbl, 2, 0);
        partGrid.add(statusCombo, 2, 1);

        sec3.getChildren().addAll(sec3Title, partGrid);

        // ── SECTION 4: Organizer Contact Information ─────────────────────────
        VBox secContact = new VBox(10);
        Label secContactTitle = createSectionTitle("4. Organizer Contact Information (At least one required *)");

        GridPane contactGrid = new GridPane();
        contactGrid.setHgap(16);
        contactGrid.setVgap(8);
        contactGrid.getColumnConstraints().addAll(colHalf1, colHalf2);

        Label emailLbl = createFieldLabel("Contact Email Address");
        contactEmailField = new TextField();
        contactEmailField.setPromptText("e.g. yourname@srmap.edu.in or yourname@gmail.com");
        contactEmailField.setPrefHeight(40);
        styleDarkControl(contactEmailField);

        Label phoneLbl = createFieldLabel("Contact Phone Number");
        contactPhoneField = new TextField();
        contactPhoneField.setPromptText("e.g. 9876543210 or +91 98765 43210");
        contactPhoneField.setPrefHeight(40);
        styleDarkControl(contactPhoneField);

        contactGrid.add(emailLbl, 0, 0);
        contactGrid.add(contactEmailField, 0, 1);
        contactGrid.add(phoneLbl, 1, 0);
        contactGrid.add(contactPhoneField, 1, 1);

        Label contactHint = new Label("Interested students can reach out directly via mailto or tel. Provide at least one valid method.");
        contactHint.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #D6B89C; -fx-font-style: italic;");

        secContact.getChildren().addAll(secContactTitle, contactGrid, contactHint);

        // ── SECTION 5: Dynamic Category-Specific Details ──────────────────────
        dynamicFieldsBox = new VBox(10);
        Label sec5Title = createSectionTitle("5. Category Details");

        GridPane dynGrid = new GridPane();
        dynGrid.setHgap(16);
        dynGrid.setVgap(8);
        dynGrid.getColumnConstraints().addAll(colHalf1, colHalf2);

        dynamicLabel1 = createFieldLabel("Destination / Route");
        dynamicField1 = new TextField();
        dynamicField1.setPrefHeight(40);
        styleDarkControl(dynamicField1);

        dynamicLabel2 = createFieldLabel("Estimated Budget / Cost per Head");
        dynamicField2 = new TextField();
        dynamicField2.setPrefHeight(40);
        styleDarkControl(dynamicField2);

        dynGrid.add(dynamicLabel1, 0, 0);
        dynGrid.add(dynamicField1, 0, 1);
        dynGrid.add(dynamicLabel2, 1, 0);
        dynGrid.add(dynamicField2, 1, 1);

        dynamicFieldsBox.getChildren().addAll(sec5Title, dynGrid);

        // ── ACTION BUTTONS ────────────────────────────────────────────────────
        HBox actionsRow = new HBox(14);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        actionsRow.setPadding(new Insets(12, 0, 0, 0));

        cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("home-btn-secondary");
        cancelBtn.setOnAction(e -> handleCancel());

        submitBtn = new Button("Publish Activity");
        submitBtn.setGraphic(AppIcons.create(15, Color.web("#172D2E")));
        submitBtn.getStyleClass().add("home-btn-primary");
        submitBtn.setOnAction(e -> handleSubmit());

        actionsRow.getChildren().addAll(cancelBtn, submitBtn);

        formCard.getChildren().addAll(sec1, sec2, sec3, secContact, dynamicFieldsBox, actionsRow);
        contentBox.getChildren().addAll(headerBox, formCard);
        pageBody.getChildren().add(contentBox);

        ScrollPane scrollPane = new ScrollPane(pageBody);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("home-scroll-pane");

        setCenter(scrollPane);

        // Initialize dynamic fields
        updateDynamicFields("Travel");
    }

    private void handleCancel() {
        if (editingActivityId != null) {
            Activity act = DatabaseManager.getActivityById(editingActivityId);
            clearForm();
            if (act != null) {
                navigator.showActivityDetails(act, returnPageKey);
            } else if ("HOME".equalsIgnoreCase(returnPageKey)) {
                navigator.showHome();
            } else if ("MY_ACTIVITIES".equalsIgnoreCase(returnPageKey)) {
                navigator.showMyActivities();
            } else {
                navigator.showExplore(null);
            }
        } else {
            clearForm();
            navigator.showExplore(null);
        }
    }

    public void clearForm() {
        editingActivityId = null;
        returnPageKey = "EXPLORE";
        if (kickerLabel != null) kickerLabel.setText("COMMUNITY INITIATIVE · SRM UNIVERSITY-AP");
        if (titleLabel != null) titleLabel.setText("Create a Campus Activity");
        if (subLabel != null) subLabel.setText("Fill in the details below to publish your activity and recruit teammates across campus.");
        if (submitBtn != null) {
            submitBtn.setText("Publish Activity");
            submitBtn.setGraphic(AppIcons.create(15, Color.web("#172D2E")));
        }
        titleField.clear();
        descArea.clear();
        locationField.clear();
        contactEmailField.clear();
        contactPhoneField.clear();
        dynamicField1.clear();
        dynamicField2.clear();
        datePicker.setValue(LocalDate.now().plusDays(3));
        maxMembersSpinner.getValueFactory().setValue(5);
        statusCombo.setValue("Open");
        createdByField.setDisable(false);
        createdByField.setText(navigator.getCurrentStudentReg());
    }

    public void setCreateMode(String preselectedCategory) {
        clearForm();
        if (preselectedCategory != null && !preselectedCategory.isBlank()) {
            setPreselectedCategory(preselectedCategory);
        }
    }

    public void loadActivityForEdit(Activity activity, String returnPage) {
        if (activity == null) return;
        this.editingActivityId = activity.getActivityId();
        this.returnPageKey = returnPage != null ? returnPage : "MY_ACTIVITIES";

        if (kickerLabel != null) kickerLabel.setText("ORGANIZER CONTROLS · EDIT ACTIVITY #" + editingActivityId);
        if (titleLabel != null) titleLabel.setText("Edit Campus Activity");
        if (subLabel != null) subLabel.setText("Modify activity details, schedule, capacity limits, or organizer contact information.");
        if (submitBtn != null) {
            submitBtn.setText("Save Changes");
            submitBtn.setGraphic(AppIcons.check(15, Color.web("#172D2E")));
        }

        titleField.setText(activity.getTitle() != null ? activity.getTitle() : "");
        categoryCombo.setValue(activity.getCategory() != null ? activity.getCategory() : "Travel");
        updateDynamicFields(activity.getCategory());

        String desc = activity.getDescription() != null ? activity.getDescription() : "";
        dynamicField1.clear();
        dynamicField2.clear();

        if (desc.contains("── Key Details ──")) {
            String[] parts = desc.split("── Key Details ──", 2);
            descArea.setText(parts[0].trim());

            String detailsSection = parts[1].trim();
            String[] lines = detailsSection.split("\\r?\\n");
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("•") || trimmedLine.startsWith("-") || trimmedLine.startsWith("*")) {
                    trimmedLine = trimmedLine.substring(1).trim();
                }
                int colonIdx = trimmedLine.indexOf(':');
                if (colonIdx > 0) {
                    String labelPart = trimmedLine.substring(0, colonIdx).trim();
                    String valuePart = trimmedLine.substring(colonIdx + 1).trim();

                    if (dynamicLabel1 != null && labelPart.equalsIgnoreCase(dynamicLabel1.getText().trim())) {
                        dynamicField1.setText(valuePart);
                    } else if (dynamicLabel2 != null && labelPart.equalsIgnoreCase(dynamicLabel2.getText().trim())) {
                        dynamicField2.setText(valuePart);
                    } else if (dynamicField1.getText().isEmpty()) {
                        dynamicField1.setText(valuePart);
                    } else if (dynamicField2.getText().isEmpty()) {
                        dynamicField2.setText(valuePart);
                    }
                }
            }
        } else {
            descArea.setText(desc);
        }

        if (activity.getActivityDate() != null && !activity.getActivityDate().isBlank()) {
            try {
                datePicker.setValue(LocalDate.parse(activity.getActivityDate().trim()));
            } catch (Exception e) {
                datePicker.setValue(LocalDate.now().plusDays(3));
            }
        } else {
            datePicker.setValue(LocalDate.now().plusDays(3));
        }

        locationField.setText(activity.getLocation() != null ? activity.getLocation() : "");
        createdByField.setText(activity.getCreatedBy() != null ? activity.getCreatedBy() : navigator.getCurrentStudentReg());
        createdByField.setDisable(true); // Host student registration ID cannot be changed

        maxMembersSpinner.getValueFactory().setValue(activity.getMaxMembers());
        statusCombo.setValue(activity.getStatus() != null ? activity.getStatus() : "Open");

        contactEmailField.setText(activity.getContactEmail() != null ? activity.getContactEmail() : "");
        contactPhoneField.setText(activity.getContactPhone() != null ? activity.getContactPhone() : "");
    }

    public boolean isEditMode() {
        return editingActivityId != null;
    }

    public Integer getEditingActivityId() {
        return editingActivityId;
    }

    public void refreshHostStudent() {
        if (createdByField != null && editingActivityId == null) {
            createdByField.setText(navigator.getCurrentStudentReg());
        }
    }

    public void setPreselectedCategory(String category) {
        if (category != null && !category.isBlank()) {
            for (String item : categoryCombo.getItems()) {
                if (item.equalsIgnoreCase(category) || category.toLowerCase().startsWith(item.toLowerCase())) {
                    categoryCombo.setValue(item);
                    updateDynamicFields(item);
                    break;
                }
            }
        }
        if (editingActivityId == null) {
            createdByField.setText(navigator.getCurrentStudentReg());
        }
    }

    private Label createSectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 17px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8; "
                + "-fx-padding: 0 0 4 0;");
        return lbl;
    }

    private Label createFieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");
        return lbl;
    }

    private void styleDarkControl(Control c) {
        c.setStyle("-fx-background-color: #172D2E; "
                + "-fx-text-fill: #FEDAB8; "
                + "-fx-prompt-text-fill: #8F7B6B; "
                + "-fx-font-size: 13.5px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.3); "
                + "-fx-border-radius: 6px; "
                + "-fx-background-radius: 6px;");
    }

    private void updateDynamicFields(String category) {
        if (category == null) return;
        String cat = category.toLowerCase();

        if (cat.contains("travel")) {
            dynamicLabel1.setText("Destination / Route");
            dynamicField1.setPromptText("e.g. Coorg, Karnataka");
            dynamicLabel2.setText("Estimated Budget / Mode of Travel");
            dynamicField2.setPromptText("e.g. ₹2,500 per head · Shared Tempo Traveler");
        } else if (cat.contains("sport")) {
            dynamicLabel1.setText("Sport / Game Type");
            dynamicField1.setPromptText("e.g. 5-a-Side Football / Badminton Doubles");
            dynamicLabel2.setText("Skill Level / Gear Needed");
            dynamicField2.setPromptText("e.g. Intermediate · Bring your own gear");
        } else if (cat.contains("hackathon")) {
            dynamicLabel1.setText("Hackathon Name / Problem Track");
            dynamicField1.setPromptText("e.g. Smart India Hackathon / AI Track");
            dynamicLabel2.setText("Required Tech Stack / Skills");
            dynamicField2.setPromptText("e.g. React, Spring Boot, PyTorch");
        } else if (cat.contains("study")) {
            dynamicLabel1.setText("Course / Subject & Topic");
            dynamicField1.setPromptText("e.g. CSE201 Data Structures & Algorithms");
            dynamicLabel2.setText("Target Goal / Materials");
            dynamicField2.setPromptText("e.g. Mid-term exam revision · LeetCode Mediums");
        } else if (cat.contains("event")) {
            dynamicLabel1.setText("Event Type / Fest Name");
            dynamicField1.setPromptText("e.g. Milan Cultural Fest / Tech Summit");
            dynamicLabel2.setText("Entry / Passes Info");
            dynamicField2.setPromptText("e.g. Free campus entry / Delegate pass");
        } else {
            dynamicLabel1.setText("Activity Sub-type");
            dynamicField1.setPromptText("e.g. Board Games / Movie Night / Jam Session");
            dynamicLabel2.setText("Items to Bring");
            dynamicField2.setPromptText("e.g. Acoustic guitar / Snacks / Games");
        }
    }

    private void handleSubmit() {
        String title = titleField.getText() != null ? titleField.getText().trim() : "";
        String category = categoryCombo.getValue();
        LocalDate date = datePicker.getValue();
        String location = locationField.getText() != null ? locationField.getText().trim() : "";
        String createdBy = createdByField.getText() != null ? createdByField.getText().trim() : "";
        int maxMembers = maxMembersSpinner.getValue() != null ? maxMembersSpinner.getValue() : 5;
        String status = statusCombo.getValue() != null ? statusCombo.getValue() : "Open";

        if (title.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Title", "Please provide a clear title for the activity.");
            titleField.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Location", "Please specify where this activity takes place.");
            locationField.requestFocus();
            return;
        }

        if (date == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Date", "Please choose an activity date.");
            datePicker.requestFocus();
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Activity Date",
                    "Activity date cannot be in the past. Please select today or an upcoming date.");
            datePicker.requestFocus();
            return;
        }

        if (createdBy.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Organizer", "Please provide your student registration number.");
            createdByField.requestFocus();
            return;
        }

        String email = contactEmailField.getText() != null ? contactEmailField.getText().trim() : "";
        String phone = contactPhoneField.getText() != null ? contactPhoneField.getText().trim() : "";

        // Organizer Contact validation: at least one method required
        if (email.isEmpty() && phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Organizer Contact",
                    "Please provide at least one contact method (email address or phone number) so interested teammates can reach you.");
            contactEmailField.requestFocus();
            return;
        }

        if (!email.isEmpty() && !isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email Format",
                    "Please enter a valid email address (e.g. name@srmap.edu.in or name@example.com).");
            contactEmailField.requestFocus();
            return;
        }

        if (!phone.isEmpty() && !isValidPhone(phone)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Phone Number",
                    "Please enter a valid phone number (e.g. 9876543210 or +91 98765 43210).");
            contactPhoneField.requestFocus();
            return;
        }

        StringBuilder descBuilder = new StringBuilder();
        String baseDesc = descArea.getText() != null ? descArea.getText().trim() : "";
        if (!baseDesc.isEmpty()) {
            descBuilder.append(baseDesc);
        }

        String d1 = dynamicField1.getText() != null ? dynamicField1.getText().trim() : "";
        String d2 = dynamicField2.getText() != null ? dynamicField2.getText().trim() : "";
        if (!d1.isEmpty() || !d2.isEmpty()) {
            if (descBuilder.length() > 0) descBuilder.append("\n\n");
            descBuilder.append("── Key Details ──\n");
            if (!d1.isEmpty()) descBuilder.append("• ").append(dynamicLabel1.getText()).append(": ").append(d1).append("\n");
            if (!d2.isEmpty()) descBuilder.append("• ").append(dynamicLabel2.getText()).append(": ").append(d2).append("\n");
        }

        String finalDesc = descBuilder.toString().trim();
        String dateString = date.toString();

        if (editingActivityId == null) {
            // ── CREATE NEW ACTIVITY FLOW ───────────────────────────────────────
            Activity newActivity = new Activity(
                    category,
                    title,
                    finalDesc,
                    dateString,
                    location,
                    createdBy,
                    maxMembers,
                    status,
                    email.isEmpty() ? null : email,
                    phone.isEmpty() ? null : phone
            );

            boolean success = DatabaseManager.insertActivity(newActivity);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Activity Published!",
                        "Your activity \"" + title + "\" has been published successfully.\n"
                        + "It is now visible to all students on the Explore feed.");
                clearForm();
                navigator.refreshAllPages();
                navigator.showExplore(category);
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error",
                        "Could not save the activity to the SQLite database. Please check console logs.");
            }
        } else {
            // ── EDIT EXISTING ACTIVITY FLOW ───────────────────────────────────
            int confirmedMembers = DatabaseManager.getTotalCurrentMembers(editingActivityId);
            if (maxMembers < confirmedMembers) {
                showAlert(Alert.AlertType.WARNING, "Capacity Cannot Be Reduced",
                        "Maximum members cannot be reduced below " + confirmedMembers + ".\n\n"
                        + "There are currently " + confirmedMembers + " confirmed members in this squad (1 organizer + "
                        + (confirmedMembers - 1) + " accepted teammates).\n\n"
                        + "To reduce capacity further, you must first remove or reject members in 'Manage Requests'.");
                maxMembersSpinner.requestFocus();
                return;
            }

            // Verify organizer ownership
            String currentReg = navigator.getCurrentStudentReg();
            if (createdBy == null || !createdBy.equalsIgnoreCase(currentReg)) {
                showAlert(Alert.AlertType.ERROR, "Permission Denied",
                        "You can only edit activities that you created.");
                return;
            }

            Activity existing = DatabaseManager.getActivityById(editingActivityId);
            String existingCreatedAt = existing != null ? existing.getCreatedAt() : null;

            Activity updatedActivity = new Activity(
                    editingActivityId,
                    category,
                    title,
                    finalDesc,
                    dateString,
                    location,
                    createdBy,
                    maxMembers,
                    status,
                    existingCreatedAt,
                    email.isEmpty() ? null : email,
                    phone.isEmpty() ? null : phone
            );

            boolean success = DatabaseManager.updateActivity(updatedActivity, currentReg);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Activity Updated!",
                        "Your activity \"" + title + "\" has been updated successfully.");
                int editedId = editingActivityId;
                String targetReturn = returnPageKey;
                clearForm();
                navigator.refreshAllPages();
                navigator.showActivityDetails(updatedActivity, targetReturn);
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Failed",
                        "Could not update the activity in the SQLite database. Please check console logs.");
            }
        }
    }

    public static boolean isValidEmail(String email) {
        return ContactValidator.isValidEmail(email);
    }

    public static boolean isValidPhone(String phone) {
        return ContactValidator.isValidPhone(phone);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("TeamUp - Create Activity");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
