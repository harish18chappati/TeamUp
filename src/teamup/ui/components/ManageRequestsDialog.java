package teamup.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import teamup.db.DatabaseManager;
import teamup.model.Activity;
import teamup.model.JoinRequest;
import teamup.model.JoinResult;

import java.util.List;

/**
 * ManageRequestsDialog – Modal interface for activity organizers to review, accept, and reject
 * incoming teammate join requests with SQLite transactional safety.
 */
public class ManageRequestsDialog extends Dialog<Void> {

    private final Activity activity;
    private final Runnable onUpdated;
    private final VBox requestsContainer;
    private final Label capacitySummaryLbl = new Label();
    private final Label pendingSummaryLbl = new Label();

    public ManageRequestsDialog(Activity activity, Runnable onUpdated) {
        this.activity = activity;
        this.onUpdated = onUpdated;

        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("TeamUp — Manage Teammate Requests");

        DialogPane pane = getDialogPane();
        pane.setStyle("-fx-background-color: #203C3D; "
                + "-fx-border-color: rgba(214, 184, 156, 0.35); "
                + "-fx-border-width: 1.5px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px; "
                + "-fx-padding: 24;");

        try {
            pane.getStylesheets().add(getClass().getResource("/teamup/ui/styles.css").toExternalForm());
        } catch (Exception ignored) {}

        VBox root = new VBox(18);
        root.setPrefWidth(580);
        root.setMaxWidth(620);
        root.setAlignment(Pos.TOP_LEFT);

        // Header
        VBox headerBox = new VBox(4);
        Label kicker = new Label("ORGANIZER CONTROL PANEL · SRM UNIVERSITY-AP");
        kicker.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");

        Label titleLbl = new Label(activity.getTitle());
        titleLbl.setWrapText(true);
        titleLbl.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', serif; "
                + "-fx-font-size: 22px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");

        Label subLbl = new Label("Review applications, accept teammates, and finalize your activity squad.");
        subLbl.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #E6D5C5;");

        headerBox.getChildren().addAll(kicker, titleLbl, subLbl);

        // Summary Bar
        HBox summaryBar = new HBox(16);
        summaryBar.setAlignment(Pos.CENTER_LEFT);
        summaryBar.setPadding(new Insets(12, 16, 12, 16));
        summaryBar.setStyle("-fx-background-color: #172D2E; "
                + "-fx-background-radius: 8px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.2); "
                + "-fx-border-radius: 8px;");

        VBox capBox = new VBox(3);
        Label capT = new Label("CAPACITY (INCL. HOST)");
        capT.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");
        capacitySummaryLbl.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");
        capBox.getChildren().addAll(capT, capacitySummaryLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox pendBox = new VBox(3);
        Label pendT = new Label("PENDING APPLICATIONS");
        pendT.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");
        pendingSummaryLbl.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");
        pendBox.getChildren().addAll(pendT, pendingSummaryLbl);

        summaryBar.getChildren().addAll(capBox, spacer, pendBox);

        // Requests List inside ScrollPane
        requestsContainer = new VBox(12);
        requestsContainer.setAlignment(Pos.TOP_CENTER);
        requestsContainer.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scroll = new ScrollPane(requestsContainer);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);
        scroll.setMaxHeight(380);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        root.getChildren().addAll(headerBox, summaryBar, scroll);
        pane.setContent(root);

        // Close button
        ButtonType closeBtnType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().add(closeBtnType);
        Button closeBtn = (Button) pane.lookupButton(closeBtnType);
        closeBtn.getStyleClass().add("home-btn-secondary");
        closeBtn.setStyle("-fx-background-color: #264445; -fx-text-fill: #D6B89C; -fx-font-size: 12.5px; -fx-padding: 8 18;");

        // Initial populate
        refreshRequests();

        // When dialog closes, notify parent page to refresh
        setOnHidden(e -> {
            if (onUpdated != null) {
                onUpdated.run();
            }
        });
    }

    private void refreshRequests() {
        int actId = activity.getActivityId();
        // Update updated activity record
        Activity refreshedAct = DatabaseManager.getActivityById(actId);
        int totalOccupied = DatabaseManager.getTotalCurrentMembers(actId);
        int max = refreshedAct != null ? refreshedAct.getMaxMembers() : activity.getMaxMembers();
        int available = DatabaseManager.getAvailableSeats(actId);
        int pending = DatabaseManager.getPendingRequestsCountForActivity(actId);

        capacitySummaryLbl.setText(totalOccupied + " / " + max + " Filled (" + available + " available)");
        pendingSummaryLbl.setText(pending + " Awaiting Review");

        requestsContainer.getChildren().clear();

        List<JoinRequest> list = DatabaseManager.getJoinRequestsForActivity(actId);
        if (list == null || list.isEmpty()) {
            VBox emptyBox = new VBox(8);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30, 20, 30, 20));
            Label emptyLbl = new Label("No teammate join requests yet.");
            emptyLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");
            Label emptySub = new Label("When students apply to join your activity, their requests will appear here.");
            emptySub.setStyle("-fx-font-size: 12px; -fx-text-fill: #E6D5C5;");
            emptyBox.getChildren().addAll(emptyLbl, emptySub);
            requestsContainer.getChildren().add(emptyBox);
            return;
        }

        for (JoinRequest req : list) {
            requestsContainer.getChildren().add(createRequestCard(req));
        }
    }

    private VBox createRequestCard(JoinRequest req) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle("-fx-background-color: #264445; "
                + "-fx-background-radius: 8px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.2); "
                + "-fx-border-radius: 8px;");

        // Top Row: Student ID + Status Badge
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label studentLbl = new Label("👤  " + req.getStudentReg());
        studentLbl.setStyle("-fx-font-size: 13.5px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(req.getStatus().toUpperCase());
        if (req.isAccepted()) {
            statusBadge.setStyle("-fx-background-color: rgba(129, 199, 132, 0.2); -fx-text-fill: #81C784; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4px;");
        } else if (req.isRejected()) {
            statusBadge.setStyle("-fx-background-color: rgba(229, 115, 115, 0.2); -fx-text-fill: #E57373; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4px;");
        } else {
            statusBadge.setStyle("-fx-background-color: rgba(254, 218, 184, 0.2); -fx-text-fill: #FEDAB8; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4px;");
        }

        topRow.getChildren().addAll(studentLbl, spacer, statusBadge);

        // Message
        String msg = req.getMessage();
        if (msg != null && !msg.isBlank()) {
            Label msgLbl = new Label("\"" + msg + "\"");
            msgLbl.setWrapText(true);
            msgLbl.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #E6D5C5; -fx-font-style: italic;");
            card.getChildren().add(msgLbl);
        }

        // Bottom Row: Date and Action Buttons
        HBox btmRow = new HBox(12);
        btmRow.setAlignment(Pos.CENTER_LEFT);

        Label dateLbl = new Label("Applied: " + (req.getRequestedAt() != null ? req.getRequestedAt() : "Recently"));
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #D6B89C;");

        Region btmSpacer = new Region();
        HBox.setHgrow(btmSpacer, Priority.ALWAYS);
        btmRow.getChildren().addAll(dateLbl, btmSpacer);

        if (req.isPending()) {
            Button acceptBtn = new Button("✓ Accept");
            acceptBtn.setStyle("-fx-background-color: #81C784; -fx-text-fill: #172D2E; -fx-font-weight: bold; -fx-font-size: 11.5px; -fx-padding: 5 12; -fx-background-radius: 4px; -fx-cursor: hand;");
            acceptBtn.setOnAction(e -> {
                JoinResult res = DatabaseManager.respondToJoinRequest(req.getRequestId(), true);
                if (res.isSuccess()) {
                    refreshRequests();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Cannot Accept Request");
                    alert.setHeaderText("Capacity Notice");
                    alert.setContentText(res.getMessage());
                    alert.showAndWait();
                    refreshRequests();
                }
            });

            Button rejectBtn = new Button("✗ Reject");
            rejectBtn.setStyle("-fx-background-color: #3B2A2A; -fx-text-fill: #E57373; -fx-border-color: #E57373; -fx-border-width: 1px; -fx-border-radius: 4px; -fx-font-size: 11.5px; -fx-padding: 4 10; -fx-background-radius: 4px; -fx-cursor: hand;");
            rejectBtn.setOnAction(e -> {
                DatabaseManager.respondToJoinRequest(req.getRequestId(), false);
                refreshRequests();
            });

            btmRow.getChildren().addAll(acceptBtn, rejectBtn);
        } else if (req.isAccepted()) {
            Button rejectBtn = new Button("Revoke / Remove");
            rejectBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #D6B89C; -fx-font-size: 11px; -fx-cursor: hand;");
            rejectBtn.setOnAction(e -> {
                DatabaseManager.respondToJoinRequest(req.getRequestId(), false);
                refreshRequests();
            });
            btmRow.getChildren().add(rejectBtn);
        }

        card.getChildren().addAll(topRow, btmRow);
        return card;
    }
}
