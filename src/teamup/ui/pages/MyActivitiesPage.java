package teamup.ui.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import teamup.db.DatabaseManager;
import teamup.model.Activity;
import teamup.ui.PageNavigator;
import teamup.ui.components.ActivityCard;
import teamup.ui.components.AppIcons;

import java.util.List;

/**
 * MyActivitiesPage – Classical organizer dashboard for student's posted activities, incoming requests, and metrics.
 * Styled in Slate Grav (#203C3D) and Soft Slate (#344F50) with Peach Buzz (#FEDAB8) typography.
 */
public class MyActivitiesPage extends BorderPane {

    private final PageNavigator navigator;
    private final ComboBox<String> profileSelector;
    private final VBox cardsList;
    private final VBox pendingBannerBox = new VBox();

    // Metric display labels
    private final Label totalCountLabel = new Label("0");
    private final Label openCountLabel = new Label("0");
    private final Label pendingCountLabel = new Label("0");
    private final Label capacityCountLabel = new Label("0");

    public MyActivitiesPage(PageNavigator navigator) {
        this.navigator = navigator;

        getStyleClass().add("home-root-pane");

        // ── SCROLLABLE BODY ───────────────────────────────────────────────────
        VBox pageBody = new VBox(24);
        pageBody.setAlignment(Pos.TOP_CENTER);
        pageBody.setPadding(new Insets(32, 40, 48, 40));
        pageBody.getStyleClass().add("home-root-pane");

        VBox contentBox = new VBox(24);
        contentBox.setMaxWidth(1080);
        contentBox.setAlignment(Pos.TOP_LEFT);

        // ── HEADER SECTION ────────────────────────────────────────────────────
        HBox headerRow = new HBox(16);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label kicker = new Label("ORGANIZER DASHBOARD · SRM UNIVERSITY-AP");
        kicker.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");

        Label pageTitle = new Label("My Activities & Hosted Events");
        pageTitle.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 32px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");

        Region rule = new Region();
        rule.setStyle("-fx-background-color: #FEDAB8; -fx-pref-width: 48px; -fx-max-width: 48px; -fx-pref-height: 2px; -fx-min-height: 2px;");

        Label pageSub = new Label("Track, manage, and monitor activities created under your student registration number.");
        pageSub.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #E6D5C5;");

        titleBox.getChildren().addAll(kicker, pageTitle, rule, pageSub);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button createBtn = new Button("+ Create Activity");
        createBtn.getStyleClass().add("home-btn-primary");
        createBtn.setOnAction(e -> navigator.showCreateActivity(null));

        headerRow.getChildren().addAll(titleBox, headerSpacer, createBtn);

        // ── PROFILE SWITCHER BAR ──────────────────────────────────────────────
        HBox profileBar = new HBox(12);
        profileBar.setAlignment(Pos.CENTER_LEFT);
        profileBar.setPadding(new Insets(10, 20, 10, 20));
        profileBar.setStyle("-fx-background-color: #172D2E; "
                + "-fx-background-radius: 8px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.25); "
                + "-fx-border-radius: 8px;");

        Label viewLbl = new Label("Active Student Profile:");
        viewLbl.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");

        profileSelector = new ComboBox<>();
        profileSelector.getItems().addAll(
                "AP21110010001",
                "AP21110010002",
                "AP21110010003",
                "AP21110010004",
                "AP21110010005",
                "AP21110010006",
                "AP21110010007",
                "AP21110010008"
        );
        profileSelector.setEditable(true);
        profileSelector.setValue(navigator.getCurrentStudentReg());
        profileSelector.setPrefWidth(180);
        profileSelector.setStyle("-fx-background-color: #203C3D; -fx-text-fill: #FEDAB8; -fx-border-color: rgba(214, 184, 156, 0.3); -fx-border-radius: 6px;");

        Button switchBtn = new Button("Switch Profile");
        switchBtn.getStyleClass().add("home-btn-secondary");
        switchBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6 16;");
        switchBtn.setOnAction(e -> {
            String val = profileSelector.getValue();
            if (val != null && !val.isBlank()) {
                navigator.setCurrentStudentReg(val.trim());
                refreshActivities();
            }
        });

        Button welcomeBtn = new Button("Change Student (Welcome Screen)");
        welcomeBtn.getStyleClass().add("home-btn-secondary");
        welcomeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6 16; -fx-border-color: #FEDAB8;");
        welcomeBtn.setOnAction(e -> navigator.showWelcome());

        profileBar.getChildren().addAll(viewLbl, profileSelector, switchBtn, welcomeBtn);

        // ── 4 SUMMARY METRIC CARDS ────────────────────────────────────────────
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(16);
        metricsGrid.setVgap(10);
        metricsGrid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(25);
        col.setHgrow(Priority.ALWAYS);
        metricsGrid.getColumnConstraints().addAll(col, col, col, col);

        VBox m1 = createMetricCard("TOTAL POSTED", totalCountLabel, "#FEDAB8");
        VBox m2 = createMetricCard("OPEN SQUADS", openCountLabel, "#81C784");
        VBox m3 = createMetricCard("PENDING REQUESTS", pendingCountLabel, "#FEDAB8");
        VBox m4 = createMetricCard("TOTAL CAPACITY", capacityCountLabel, "#E6D5C5");

        metricsGrid.add(m1, 0, 0);
        metricsGrid.add(m2, 1, 0);
        metricsGrid.add(m3, 2, 0);
        metricsGrid.add(m4, 3, 0);

        // Pending banner slot
        pendingBannerBox.setAlignment(Pos.CENTER_LEFT);
        pendingBannerBox.setMaxWidth(Double.MAX_VALUE);

        // ── HOSTED ACTIVITIES FEED ────────────────────────────────────────────
        cardsList = new VBox(14);
        cardsList.setAlignment(Pos.TOP_CENTER);
        cardsList.setMaxWidth(Double.MAX_VALUE);

        contentBox.getChildren().addAll(headerRow, profileBar, metricsGrid, pendingBannerBox, cardsList);
        pageBody.getChildren().add(contentBox);

        ScrollPane scrollPane = new ScrollPane(pageBody);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("home-scroll-pane");

        setCenter(scrollPane);

        // Initial load
        refreshActivities();
    }

    private VBox createMetricCard(String title, Label valueLabel, String colorHex) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: #264445; "
                + "-fx-background-radius: 12px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.22); "
                + "-fx-border-radius: 12px; "
                + "-fx-padding: 18 22; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        card.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");

        valueLabel.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 30px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: " + colorHex + ";");

        card.getChildren().addAll(titleLbl, valueLabel);
        return card;
    }

    public void refreshActivities() {
        String studentReg = navigator.getCurrentStudentReg();
        profileSelector.setValue(studentReg);

        List<Activity> myActs = DatabaseManager.getActivitiesByCreator(studentReg);
        int pendingTotal = DatabaseManager.getPendingRequestsCountForCreator(studentReg);

        // Calculate metrics
        int total = myActs != null ? myActs.size() : 0;
        int open = 0;
        int capacity = 0;

        if (myActs != null) {
            for (Activity act : myActs) {
                boolean isFull = DatabaseManager.isActivityFull(act.getActivityId());
                if ("Open".equalsIgnoreCase(act.getStatus()) && !isFull) {
                    open++;
                }
                capacity += act.getMaxMembers();
            }
        }

        totalCountLabel.setText(String.valueOf(total));
        openCountLabel.setText(String.valueOf(open));
        pendingCountLabel.setText(String.valueOf(pendingTotal));
        capacityCountLabel.setText(String.valueOf(capacity));

        // Pending banner update
        pendingBannerBox.getChildren().clear();
        if (pendingTotal > 0) {
            HBox banner = new HBox(12);
            banner.setAlignment(Pos.CENTER_LEFT);
            banner.setPadding(new Insets(12, 18, 12, 18));
            banner.setStyle("-fx-background-color: #172D2E; "
                    + "-fx-background-radius: 8px; "
                    + "-fx-border-color: #FEDAB8; "
                    + "-fx-border-radius: 8px; "
                    + "-fx-border-width: 1px;");

            Label bell = new Label("🔔");
            bell.setStyle("-fx-font-size: 16px;");

            Label msg = new Label("You have " + pendingTotal + " pending teammate application"
                    + (pendingTotal == 1 ? "" : "s") + " waiting for your review. Click 'Manage' on any activity below to accept or reject candidates.");
            msg.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");
            msg.setWrapText(true);

            banner.getChildren().addAll(bell, msg);
            pendingBannerBox.getChildren().add(banner);
        }

        // Render cards
        cardsList.getChildren().clear();

        if (total == 0) {
            renderEmptyState(studentReg);
            return;
        }

        for (Activity act : myActs) {
            ActivityCard card = new ActivityCard(act, navigator, "MY_ACTIVITIES", this::refreshActivities);
            cardsList.getChildren().add(card);
        }
    }

    private void renderEmptyState(String studentReg) {
        VBox emptyBox = new VBox(14);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60, 20, 60, 20));
        emptyBox.setStyle("-fx-background-color: #1A3233; "
                + "-fx-background-radius: 12px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.18); "
                + "-fx-border-radius: 12px;");

        Label title = new Label("No activities posted yet");
        title.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 22px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");

        Label sub = new Label("You haven't posted any campus activities under student ID " + studentReg + " yet.\n"
                + "Create an activity to organize a trip, assemble a sports team, or build a hackathon squad.");
        sub.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #E6D5C5; -fx-line-spacing: 2px;");
        sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button postBtn = new Button("Create Your First Activity");
        postBtn.setGraphic(AppIcons.create(14, Color.web("#172D2E")));
        postBtn.getStyleClass().add("home-btn-primary");
        postBtn.setOnAction(e -> navigator.showCreateActivity(null));

        emptyBox.getChildren().addAll(AppIcons.user(36, Color.web("#FEDAB8")), title, sub, postBtn);
        cardsList.getChildren().add(emptyBox);
    }
}
