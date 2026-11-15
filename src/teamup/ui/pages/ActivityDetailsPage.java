package teamup.ui.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import teamup.db.DatabaseManager;
import teamup.model.Activity;
import teamup.model.JoinRequest;
import teamup.ui.PageNavigator;
import teamup.ui.components.AppIcons;
import teamup.ui.components.JoinRequestDialog;
import teamup.ui.components.ManageRequestsDialog;

import java.util.List;
import java.util.Optional;

/**
 * ActivityDetailsPage – Classical full-page view for displaying complete information about a selected activity,
 * confirmed teammate roster, organizer contact channels, and live Join Activity / Organizer Review workflows.
 * Styled in Slate Grav (#203C3D) and Soft Slate (#344F50) with Peach Buzz (#FEDAB8) typography.
 */
public class ActivityDetailsPage extends BorderPane {

    private final PageNavigator navigator;
    private Activity activity;
    private final String returnPageKey;
    private final ScrollPane scrollPane;

    public ActivityDetailsPage(Activity activity, String returnPageKey, PageNavigator navigator) {
        this.activity = activity;
        this.returnPageKey = returnPageKey;
        this.navigator = navigator;

        getStyleClass().add("home-root-pane");

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("home-scroll-pane");
        setCenter(scrollPane);

        renderContent();
    }

    private void renderContent() {
        // Fetch freshest activity state from database
        Activity fresh = DatabaseManager.getActivityById(activity.getActivityId());
        if (fresh != null) {
            this.activity = fresh;
        }

        int actId = activity.getActivityId();
        String currentReg = navigator.getCurrentStudentReg();
        boolean isCreator = activity.getCreatedBy() != null && activity.getCreatedBy().equalsIgnoreCase(currentReg);

        // Capacity and membership calculation (Modification #1: Organizer counts as 1)
        int totalOccupied = DatabaseManager.getTotalCurrentMembers(actId);
        int maxMembers = activity.getMaxMembers();
        int availableSeats = DatabaseManager.getAvailableSeats(actId);
        boolean isFull = DatabaseManager.isActivityFull(actId);
        boolean isOpen = "Open".equalsIgnoreCase(activity.getStatus()) && !isFull;
        List<String> acceptedMembers = DatabaseManager.getAcceptedMembers(actId);
        JoinRequest userRequest = DatabaseManager.getJoinRequest(actId, currentReg);
        int pendingRequestsCount = DatabaseManager.getPendingRequestsCountForActivity(actId);

        // ── SCROLLABLE BODY ───────────────────────────────────────────────────
        VBox pageBody = new VBox(24);
        pageBody.setAlignment(Pos.TOP_CENTER);
        pageBody.setPadding(new Insets(32, 40, 48, 40));
        pageBody.getStyleClass().add("home-root-pane");

        VBox contentBox = new VBox(22);
        contentBox.setMaxWidth(960);
        contentBox.setAlignment(Pos.TOP_LEFT);

        // ── TOP NAVIGATION ROW: Back Button & Badges ──────────────────────────
        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER_LEFT);

        String backLabel;
        if ("MY_ACTIVITIES".equalsIgnoreCase(returnPageKey)) {
            backLabel = "← Back to My Activities";
        } else if ("HOME".equalsIgnoreCase(returnPageKey)) {
            backLabel = "← Back to Home";
        } else {
            backLabel = "← Back to Explore";
        }

        Button backBtn = new Button(backLabel);
        backBtn.getStyleClass().add("home-btn-secondary");
        backBtn.setOnAction(e -> handleBackNavigation());

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Label catBadge = new Label(activity.getCategory().toUpperCase());
        catBadge.setGraphic(AppIcons.categoryIcon(activity.getCategory(), 12, Color.web("#FEDAB8")));
        catBadge.getStyleClass().add("home-badge-cat");

        Label statusBadge = new Label(isOpen
                ? "●  Open (" + availableSeats + " seat" + (availableSeats == 1 ? "" : "s") + " left)"
                : "○  Full / Closed (" + totalOccupied + "/" + maxMembers + ")");
        statusBadge.getStyleClass().add(isOpen ? "home-badge-open" : "badge-closed");

        topRow.getChildren().addAll(backBtn, topSpacer, catBadge, statusBadge);

        // ── MAIN DETAILS CARD ─────────────────────────────────────────────────
        VBox card = new VBox(26);
        card.setStyle("-fx-background-color: #264445; "
                + "-fx-background-radius: 14px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.22); "
                + "-fx-border-radius: 14px; "
                + "-fx-padding: 36 42; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 14, 0, 0, 3);");

        // Activity Title & Subtitle
        VBox titleBox = new VBox(6);
        Label kicker = new Label("ACTIVITY DETAILS · SRM UNIVERSITY-AP");
        kicker.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");

        Label titleLabel = new Label(activity.getTitle());
        titleLabel.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 30px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");
        titleLabel.setWrapText(true);

        Label createdLabel = new Label("Published on " + (activity.getCreatedAt() != null ? activity.getCreatedAt() : "Recently")
                + " by student " + (activity.getCreatedBy() != null ? activity.getCreatedBy() : "Anonymous")
                + (isCreator ? " (You)" : ""));
        createdLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #D6B89C;");

        titleBox.getChildren().addAll(kicker, titleLabel, createdLabel);

        // 4 Key Information Pills Grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(14);
        infoGrid.setVgap(10);
        infoGrid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(25); c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(25); c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(25); c3.setHgrow(Priority.ALWAYS);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(25); c4.setHgrow(Priority.ALWAYS);
        infoGrid.getColumnConstraints().addAll(c1, c2, c3, c4);

        VBox pillDate = createInfoPill("EVENT DATE", activity.getActivityDate() != null ? activity.getActivityDate() : "TBD", AppIcons.calendar(16, Color.web("#FEDAB8")));
        VBox pillLoc = createInfoPill("LOCATION", activity.getLocation() != null ? activity.getLocation() : "Campus", AppIcons.location(16, Color.web("#FEDAB8")));
        VBox pillCap = createInfoPill("CAPACITY (INCL. HOST)", totalOccupied + " / " + maxMembers + " Occupied", AppIcons.members(16, Color.web("#FEDAB8")));
        VBox pillHost = createInfoPill("ORGANIZER ID", activity.getCreatedBy() != null ? activity.getCreatedBy() : "N/A", AppIcons.user(16, Color.web("#FEDAB8")));

        infoGrid.add(pillDate, 0, 0);
        infoGrid.add(pillLoc, 1, 0);
        infoGrid.add(pillCap, 2, 0);
        infoGrid.add(pillHost, 3, 0);

        // Description Section
        VBox descBox = new VBox(10);
        Label descHeader = new Label("About this Activity");
        descHeader.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 18px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");

        Region rule = new Region();
        rule.setStyle("-fx-background-color: #FEDAB8; -fx-pref-width: 44px; -fx-max-width: 44px; -fx-pref-height: 2px; -fx-min-height: 2px;");

        Label descContent = new Label(activity.getDescription() != null && !activity.getDescription().isBlank()
                ? activity.getDescription()
                : "No description has been added for this activity.");
        descContent.setStyle("-fx-font-size: 14px; -fx-text-fill: #E6D5C5; -fx-line-spacing: 4px;");
        descContent.setWrapText(true);

        descBox.getChildren().addAll(descHeader, rule, descContent);

        // ── CONTACT ORGANIZER SECTION (Displayed when email or phone is available) ──
        VBox contactSection = buildContactOrganizerSection(activity);

        // ── CONFIRMED ROSTER SECTION ──────────────────────────────────────────
        VBox rosterBox = new VBox(12);
        Label rosterHeader = new Label("Confirmed Squad Roster (" + totalOccupied + " of " + maxMembers + " filled)");
        rosterHeader.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 17px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");

        FlowPane rosterFlow = new FlowPane();
        rosterFlow.setHgap(10);
        rosterFlow.setVgap(10);

        // Organizer Chip (Modification #1: counts as 1 member)
        HBox hostChip = createRosterChip("👑 Host: " + activity.getCreatedBy(), "#FEDAB8", "#172D2E", true);
        rosterFlow.getChildren().add(hostChip);

        // Accepted Teammates Chips
        for (String memberReg : acceptedMembers) {
            boolean isMe = memberReg.equalsIgnoreCase(currentReg);
            HBox memberChip = createRosterChip("👤 Teammate: " + memberReg + (isMe ? " (You)" : ""), "#81C784", "#172D2E", false);
            rosterFlow.getChildren().add(memberChip);
        }

        // Available slots indicator
        for (int i = 0; i < availableSeats; i++) {
            HBox openSlotChip = createRosterChip("○ Open Spot", "#D6B89C", "#203C3D", false);
            rosterFlow.getChildren().add(openSlotChip);
        }

        rosterBox.getChildren().addAll(rosterHeader, rosterFlow);

        // ── ACTION BUTTONS ROW ────────────────────────────────────────────────
        HBox actionsRow = new HBox(12);
        actionsRow.setAlignment(Pos.CENTER_LEFT);
        actionsRow.setPadding(new Insets(10, 0, 0, 0));

        Button actionBtn;

        Button backActionBtn = new Button(backLabel);
        backActionBtn.getStyleClass().add("home-btn-secondary");
        backActionBtn.setOnAction(e -> handleBackNavigation());

        if (isCreator) {
            // Organizer view: Manage requests
            actionBtn = new Button("Manage Requests (" + pendingRequestsCount + ")");
            actionBtn.setGraphic(AppIcons.members(15, Color.web("#172D2E")));
            actionBtn.getStyleClass().add("home-btn-primary");
            actionBtn.setOnAction(e -> {
                ManageRequestsDialog dialog = new ManageRequestsDialog(activity, this::renderContent);
                dialog.showAndWait();
            });

            // Edit Activity button (Organizer only)
            Button editBtn = new Button("Edit Activity");
            editBtn.setGraphic(AppIcons.edit(14, Color.web("#FEDAB8")));
            editBtn.getStyleClass().add("home-btn-secondary");
            editBtn.setOnAction(e -> navigator.showEditActivity(activity, returnPageKey));

            // Delete Activity button (Organizer only)
            Button deleteBtn = new Button("Delete Activity");
            deleteBtn.setGraphic(AppIcons.delete(14, Color.web("#FF8A80")));
            deleteBtn.setStyle("-fx-background-color: rgba(163, 59, 59, 0.25); "
                    + "-fx-text-fill: #FF8A80; "
                    + "-fx-font-weight: bold; "
                    + "-fx-border-color: rgba(163, 59, 59, 0.55); "
                    + "-fx-border-radius: 7px; "
                    + "-fx-background-radius: 7px; "
                    + "-fx-padding: 9 17; "
                    + "-fx-cursor: hand;");
            deleteBtn.setOnAction(e -> confirmAndDeleteActivity());

            actionsRow.getChildren().addAll(actionBtn, editBtn, deleteBtn, backActionBtn);
        } else if (userRequest != null && userRequest.isAccepted()) {
            // Student is already confirmed
            actionBtn = new Button("Confirmed Teammate ✓");
            actionBtn.setGraphic(AppIcons.members(15, Color.web("#81C784")));
            actionBtn.setStyle("-fx-background-color: rgba(129, 199, 132, 0.2); -fx-text-fill: #81C784; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-color: #81C784; -fx-border-radius: 7px; -fx-background-radius: 7px;");
            actionBtn.setDisable(true);
            actionsRow.getChildren().addAll(actionBtn, backActionBtn);
        } else if (userRequest != null && userRequest.isPending()) {
            // Student has pending request
            actionBtn = new Button("Join Request Pending ⏳");
            actionBtn.setStyle("-fx-background-color: rgba(254, 218, 184, 0.2); -fx-text-fill: #FEDAB8; -fx-font-weight: bold; -fx-padding: 10 20; -fx-border-color: #FEDAB8; -fx-border-radius: 7px; -fx-background-radius: 7px;");
            actionBtn.setDisable(true);
            actionsRow.getChildren().addAll(actionBtn, backActionBtn);
        } else if (userRequest != null && userRequest.isRejected()) {
            // Modification #3: Reapplication allowed after rejection
            actionBtn = new Button("Reapply to Join Activity ↻");
            actionBtn.setGraphic(AppIcons.members(15, Color.web("#172D2E")));
            actionBtn.getStyleClass().add("home-btn-primary");
            actionBtn.setOnAction(e -> openJoinDialog());
            actionsRow.getChildren().addAll(actionBtn, backActionBtn);
        } else if (isFull || !isOpen) {
            // Activity full or closed
            actionBtn = new Button("Activity Full (Closed)");
            actionBtn.setStyle("-fx-background-color: rgba(214, 184, 156, 0.15); -fx-text-fill: #D6B89C; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 7px;");
            actionBtn.setDisable(true);
            actionsRow.getChildren().addAll(actionBtn, backActionBtn);
        } else {
            // Eligible to join
            actionBtn = new Button("+ Join Activity / Request Teammate");
            actionBtn.setGraphic(AppIcons.members(15, Color.web("#172D2E")));
            actionBtn.getStyleClass().add("home-btn-primary");
            actionBtn.setOnAction(e -> openJoinDialog());
            actionsRow.getChildren().addAll(actionBtn, backActionBtn);
        }

        // Assembly
        if (contactSection != null) {
            card.getChildren().addAll(titleBox, infoGrid, descBox, contactSection, rosterBox, actionsRow);
        } else {
            card.getChildren().addAll(titleBox, infoGrid, descBox, rosterBox, actionsRow);
        }

        contentBox.getChildren().addAll(topRow, card);
        pageBody.getChildren().add(contentBox);

        scrollPane.setContent(pageBody);
    }

    /**
     * Builds the Contact Organizer section displaying email and/or phone cards with mailto and tel actions.
     * Returns null if neither email nor phone is provided on the activity record.
     */
    private VBox buildContactOrganizerSection(Activity activity) {
        boolean hasEmail = activity.hasContactEmail();
        boolean hasPhone = activity.hasContactPhone();

        if (!hasEmail && !hasPhone) {
            return null;
        }

        VBox contactBox = new VBox(12);
        contactBox.setStyle("-fx-background-color: #1A3233; "
                + "-fx-background-radius: 10px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.28); "
                + "-fx-border-radius: 10px; "
                + "-fx-padding: 18 22;");

        VBox titleArea = new VBox(4);
        Label contactHeader = new Label("Contact Organizer");
        contactHeader.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 17px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");

        Label contactSub = new Label("Direct communication details provided by the activity host (" + activity.getCreatedBy() + ").");
        contactSub.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #D6B89C;");
        titleArea.getChildren().addAll(contactHeader, contactSub);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);

        if (hasEmail && hasPhone) {
            ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50); c1.setHgrow(Priority.ALWAYS);
            ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(50); c2.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().addAll(c1, c2);

            VBox emailCard = createEmailContactCard(activity.getContactEmail());
            VBox phoneCard = createPhoneContactCard(activity.getContactPhone());
            grid.add(emailCard, 0, 0);
            grid.add(phoneCard, 1, 0);
        } else if (hasEmail) {
            ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(100); c1.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c1);

            VBox emailCard = createEmailContactCard(activity.getContactEmail());
            grid.add(emailCard, 0, 0);
        } else {
            ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(100); c1.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c1);

            VBox phoneCard = createPhoneContactCard(activity.getContactPhone());
            grid.add(phoneCard, 0, 0);
        }

        contactBox.getChildren().addAll(titleArea, grid);
        return contactBox;
    }

    private VBox createEmailContactCard(String email) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #244041; "
                + "-fx-background-radius: 8px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.22); "
                + "-fx-border-radius: 8px; "
                + "-fx-padding: 12 16;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label("ORGANIZER EMAIL");
        titleLbl.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");
        header.getChildren().addAll(AppIcons.mail(14, Color.web("#FEDAB8")), titleLbl);

        Label emailLbl = new Label(email);
        emailLbl.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");
        emailLbl.setWrapText(true);

        HBox actionsRow = new HBox(8);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        Button mailtoBtn = new Button("Send Email");
        mailtoBtn.setGraphic(AppIcons.mail(13, Color.web("#172D2E")));
        mailtoBtn.getStyleClass().add("home-btn-primary");
        mailtoBtn.setStyle("-fx-font-size: 11.5px; -fx-padding: 6 14;");
        mailtoBtn.setOnAction(e -> handleEmailAction(email));

        Button copyBtn = new Button("Copy");
        copyBtn.getStyleClass().add("home-btn-secondary");
        copyBtn.setStyle("-fx-font-size: 11.5px; -fx-padding: 6 12;");
        copyBtn.setOnAction(e -> copyToClipboard(email, "Email address copied to clipboard:\n" + email));

        actionsRow.getChildren().addAll(mailtoBtn, copyBtn);
        card.getChildren().addAll(header, emailLbl, actionsRow);
        return card;
    }

    private VBox createPhoneContactCard(String phone) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #244041; "
                + "-fx-background-radius: 8px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.22); "
                + "-fx-border-radius: 8px; "
                + "-fx-padding: 12 16;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label("ORGANIZER PHONE");
        titleLbl.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");
        header.getChildren().addAll(AppIcons.phone(14, Color.web("#FEDAB8")), titleLbl);

        Label phoneLbl = new Label(phone);
        phoneLbl.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");
        phoneLbl.setWrapText(true);

        HBox actionsRow = new HBox(8);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        Button telBtn = new Button("Call / Message");
        telBtn.setGraphic(AppIcons.phone(13, Color.web("#172D2E")));
        telBtn.getStyleClass().add("home-btn-primary");
        telBtn.setStyle("-fx-font-size: 11.5px; -fx-padding: 6 14;");
        telBtn.setOnAction(e -> handlePhoneAction(phone));

        Button copyBtn = new Button("Copy");
        copyBtn.getStyleClass().add("home-btn-secondary");
        copyBtn.setStyle("-fx-font-size: 11.5px; -fx-padding: 6 12;");
        copyBtn.setOnAction(e -> copyToClipboard(phone, "Phone number copied to clipboard:\n" + phone));

        actionsRow.getChildren().addAll(telBtn, copyBtn);
        card.getChildren().addAll(header, phoneLbl, actionsRow);
        return card;
    }

    private void handleEmailAction(String email) {
        copyToClipboard(email, null);
        String uriStr = "mailto:" + email.trim();
        boolean launched = false;
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.MAIL)) {
                java.awt.Desktop.getDesktop().mail(new java.net.URI(uriStr));
                launched = true;
            } else if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(uriStr));
                launched = true;
            }
        } catch (Throwable t) {
            System.out.println("[Contact] mailto launch handled with clipboard fallback: " + t.getMessage());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TeamUp — Contact Organizer");
        alert.setHeaderText("Email: " + activity.getCreatedBy());
        alert.setContentText((launched ? "Opening default mail client for:\n" : "Email address copied to clipboard:\n")
                + email + "\n\n(mailto link: " + uriStr + ")");
        alert.showAndWait();
    }

    private void handlePhoneAction(String phone) {
        copyToClipboard(phone, null);
        String digits = phone.replaceAll("[^0-9+]", "");
        String uriStr = "tel:" + digits;
        boolean launched = false;
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(uriStr));
                launched = true;
            }
        } catch (Throwable t) {
            System.out.println("[Contact] tel launch handled with clipboard fallback: " + t.getMessage());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TeamUp — Contact Organizer");
        alert.setHeaderText("Phone: " + activity.getCreatedBy());
        alert.setContentText((launched ? "Initiating phone call handler for:\n" : "Phone number copied to clipboard:\n")
                + phone + "\n\n(tel link: " + uriStr + ")");
        alert.showAndWait();
    }

    private void copyToClipboard(String text, String alertMsg) {
        try {
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(text);
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);

            if (alertMsg != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("TeamUp — Copied");
                alert.setHeaderText("Copied to Clipboard");
                alert.setContentText(alertMsg);
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("[Contact] Error copying to clipboard: " + e.getMessage());
        }
    }

    private HBox createRosterChip(String text, String fgHex, String bgHex, boolean isHighlight) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(6, 12, 6, 12));
        chip.setStyle("-fx-background-color: " + bgHex + "; "
                + "-fx-border-color: " + fgHex + "; "
                + "-fx-border-radius: 16px; "
                + "-fx-background-radius: 16px; "
                + "-fx-border-width: " + (isHighlight ? "1.5px;" : "1px;"));

        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + fgHex + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        chip.getChildren().add(lbl);
        return chip;
    }

    private VBox createInfoPill(String title, String value, javafx.scene.Node icon) {
        VBox pill = new VBox(6);
        pill.setStyle("-fx-background-color: #172D2E; "
                + "-fx-background-radius: 8px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.2); "
                + "-fx-border-radius: 8px; "
                + "-fx-padding: 12 16;");

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 10.5px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");
        header.getChildren().addAll(icon, titleLbl);

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");
        valueLbl.setWrapText(true);

        pill.getChildren().addAll(header, valueLbl);
        return pill;
    }

    private void openJoinDialog() {
        JoinRequestDialog dialog = new JoinRequestDialog(activity, navigator);
        dialog.showAndWait().ifPresent(submitted -> {
            if (submitted) {
                renderContent(); // live refresh
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("TeamUp — Request Sent");
                alert.setHeaderText("Join Request Submitted!");
                alert.setContentText("Your teammate request for '" + activity.getTitle() + "' was sent to the organizer (" + activity.getCreatedBy() + ").");
                alert.showAndWait();
            }
        });
    }

    private void handleBackNavigation() {
        if ("MY_ACTIVITIES".equalsIgnoreCase(returnPageKey)) {
            navigator.showMyActivities();
        } else if ("HOME".equalsIgnoreCase(returnPageKey)) {
            navigator.showHome();
        } else {
            navigator.showExplore(activity != null ? activity.getCategory() : null);
        }
    }

    private void confirmAndDeleteActivity() {
        String currentReg = navigator.getCurrentStudentReg();
        if (activity.getCreatedBy() == null || !activity.getCreatedBy().equalsIgnoreCase(currentReg)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Permission Denied");
            alert.setHeaderText("Unauthorized Action");
            alert.setContentText("Only the activity organizer (" + activity.getCreatedBy() + ") can delete this activity.");
            alert.showAndWait();
            return;
        }

        int confirmedCount = DatabaseManager.getAcceptedMembersCount(activity.getActivityId());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion – TeamUp");
        confirm.setHeaderText("Delete Activity: \"" + activity.getTitle() + "\"?");
        confirm.setContentText("Are you sure you want to permanently delete this activity?\n\n"
                + "• All " + confirmedCount + " confirmed teammate registration(s) and any pending join requests will be deleted.\n"
                + "• This action cannot be undone.\n\n"
                + "Click OK to delete, or Cancel to keep this activity.");

        DialogPane dp = confirm.getDialogPane();
        dp.setStyle("-fx-background-color: #172D2E;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = DatabaseManager.deleteActivityByOrganizer(activity.getActivityId(), currentReg);
            if (success) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Activity Deleted");
                info.setHeaderText("Activity Successfully Deleted");
                info.setContentText("The activity \"" + activity.getTitle() + "\" and its associated join requests have been removed.");
                info.showAndWait();

                navigator.refreshAllPages();

                if ("MY_ACTIVITIES".equalsIgnoreCase(returnPageKey)) {
                    navigator.showMyActivities();
                } else if ("HOME".equalsIgnoreCase(returnPageKey)) {
                    navigator.showHome();
                } else {
                    navigator.showExplore(activity.getCategory());
                }
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Error");
                err.setHeaderText("Deletion Failed");
                err.setContentText("Could not delete activity. Please check console logs.");
                err.showAndWait();
            }
        }
    }
}
