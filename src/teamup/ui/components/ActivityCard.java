package teamup.ui.components;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import teamup.db.DatabaseManager;
import teamup.model.Activity;
import teamup.model.JoinRequest;
import teamup.ui.PageNavigator;

import java.util.Optional;

/**
 * ActivityCard – Classical, high-contrast wide horizontal card for activity feeds.
 * Styled in Soft Slate (#344F50) with Peach Buzz (#FEDAB8) and Muted Peach (#D6B89C).
 * Used in Explore Activities and My Activities pages.
 */
public class ActivityCard extends HBox {

    private final Activity activity;
    private final PageNavigator navigator;
    private final String returnPageKey;
    private final Runnable onRefresh;

    public ActivityCard(Activity activity, PageNavigator navigator, String returnPageKey) {
        this(activity, navigator, returnPageKey, null);
    }

    public ActivityCard(Activity activity, PageNavigator navigator, String returnPageKey, Runnable onRefresh) {
        this.activity = activity;
        this.navigator = navigator;
        this.returnPageKey = returnPageKey;
        this.onRefresh = onRefresh;

        getStyleClass().add("home-featured-card");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(20);
        setMaxWidth(Double.MAX_VALUE);
        setPadding(new Insets(18, 24, 18, 24));
        setStyle("-fx-background-color: #2F4C4D; "
                + "-fx-background-radius: 12px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.22); "
                + "-fx-border-radius: 12px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.25), 10, 0, 0, 2);");

        int actId = activity.getActivityId();
        String currentReg = navigator.getCurrentStudentReg();
        boolean isCreator = activity.getCreatedBy() != null && activity.getCreatedBy().equalsIgnoreCase(currentReg);

        // Capacity and membership calculation (Modification #1: Organizer counts as 1)
        int totalOccupied = DatabaseManager.getTotalCurrentMembers(actId);
        int maxMembers = activity.getMaxMembers();
        int availableSeats = DatabaseManager.getAvailableSeats(actId);
        boolean isFull = DatabaseManager.isActivityFull(actId);
        boolean isOpen = "Open".equalsIgnoreCase(activity.getStatus()) && !isFull;
        JoinRequest userRequest = DatabaseManager.getJoinRequest(actId, currentReg);
        int pendingRequestsCount = DatabaseManager.getPendingRequestsCountForActivity(actId);

        // ── LEFT CONTENT: Badges, Title, Description, Metadata ─────────────────
        VBox leftContent = new VBox(8);
        leftContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftContent, Priority.ALWAYS);

        // Badges Row
        HBox badgesRow = new HBox(8);
        badgesRow.setAlignment(Pos.CENTER_LEFT);

        Label catBadge = new Label(activity.getCategory().toUpperCase());
        catBadge.setGraphic(AppIcons.categoryIcon(activity.getCategory(), 12, Color.web("#FEDAB8")));
        catBadge.getStyleClass().add("home-badge-cat");

        Label statusBadge = new Label(isOpen
                ? "●  Open (" + availableSeats + " left)"
                : "○  Full / Closed (" + totalOccupied + "/" + maxMembers + ")");
        statusBadge.getStyleClass().add(isOpen ? "home-badge-open" : "badge-closed");

        badgesRow.getChildren().addAll(catBadge, statusBadge);

        if (isCreator && pendingRequestsCount > 0) {
            Label reqBadge = new Label("🔔 " + pendingRequestsCount + " Pending");
            reqBadge.setStyle("-fx-background-color: #FEDAB8; -fx-text-fill: #172D2E; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10px;");
            badgesRow.getChildren().add(reqBadge);
        }

        // Title in Serif Peach Buzz
        Label titleLabel = new Label(activity.getTitle());
        titleLabel.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 20px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");
        titleLabel.setWrapText(true);

        // Description preview
        String descText = activity.getDescription();
        if (descText != null && descText.length() > 140) {
            descText = descText.substring(0, 137).trim() + "...";
        }
        Label descLabel = new Label(descText != null ? descText : "No description provided.");
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #E6D5C5; -fx-line-spacing: 2px;");
        descLabel.setWrapText(true);

        // Metadata Row
        HBox metaRow = new HBox(18);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.setPadding(new Insets(4, 0, 0, 0));

        HBox dateBox = createMetaItem(AppIcons.calendar(13, Color.web("#D6B89C")),
                activity.getActivityDate() != null ? activity.getActivityDate() : "TBD");

        HBox locBox = createMetaItem(AppIcons.location(13, Color.web("#D6B89C")),
                activity.getLocation() != null ? activity.getLocation() : "Campus");

        HBox membersBox = createMetaItem(AppIcons.members(13, Color.web("#D6B89C")),
                totalOccupied + "/" + maxMembers + " filled (Host incl.)");

        HBox creatorBox = createMetaItem(AppIcons.user(13, Color.web("#D6B89C")),
                (isCreator ? "Hosted by You" : "By " + (activity.getCreatedBy() != null ? activity.getCreatedBy() : "Anonymous")));

        metaRow.getChildren().addAll(dateBox, locBox, membersBox, creatorBox);

        leftContent.getChildren().addAll(badgesRow, titleLabel, descLabel, metaRow);

        // ── RIGHT ACTION AREA: View Details & Join / Manage ─────────────────────
        VBox actionBox = new VBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setMinWidth(140);

        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setGraphic(AppIcons.explore(13, Color.web("#FEDAB8")));
        viewDetailsBtn.getStyleClass().add("home-btn-secondary");
        viewDetailsBtn.setStyle("-fx-font-size: 12px; -fx-padding: 8 16;");
        viewDetailsBtn.setMaxWidth(Double.MAX_VALUE);
        viewDetailsBtn.setOnAction(e -> navigator.showActivityDetails(activity, returnPageKey));

        Button actionBtn;

        if (isCreator) {
            // Organizer view: Manage requests
            actionBtn = new Button(pendingRequestsCount > 0 ? "Manage (" + pendingRequestsCount + ")" : "Manage Squad");
            actionBtn.setGraphic(AppIcons.members(13, Color.web("#172D2E")));
            actionBtn.getStyleClass().add("home-btn-primary");
            actionBtn.setStyle("-fx-font-size: 12px; -fx-padding: 8 16;");
            actionBtn.setMaxWidth(Double.MAX_VALUE);
            actionBtn.setOnAction(e -> {
                ManageRequestsDialog dialog = new ManageRequestsDialog(activity, () -> {
                    if (onRefresh != null) onRefresh.run();
                });
                dialog.showAndWait();
            });

            // Compact Edit & Delete quick-actions row for Organizer
            HBox ownerRow = new HBox(6);
            ownerRow.setAlignment(Pos.CENTER_RIGHT);

            Button editCardBtn = new Button("Edit");
            editCardBtn.setGraphic(AppIcons.edit(11, Color.web("#FEDAB8")));
            editCardBtn.getStyleClass().add("home-btn-secondary");
            editCardBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
            editCardBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(editCardBtn, Priority.ALWAYS);
            editCardBtn.setOnAction(e -> navigator.showEditActivity(activity, returnPageKey));

            Button deleteCardBtn = new Button("Delete");
            deleteCardBtn.setGraphic(AppIcons.delete(11, Color.web("#FF8A80")));
            deleteCardBtn.setStyle("-fx-background-color: rgba(163, 59, 59, 0.22); "
                    + "-fx-text-fill: #FF8A80; "
                    + "-fx-font-size: 11px; "
                    + "-fx-font-weight: bold; "
                    + "-fx-padding: 5 10; "
                    + "-fx-border-color: rgba(163, 59, 59, 0.5); "
                    + "-fx-border-radius: 6px; "
                    + "-fx-background-radius: 6px; "
                    + "-fx-cursor: hand;");
            deleteCardBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(deleteCardBtn, Priority.ALWAYS);
            deleteCardBtn.setOnAction(e -> confirmAndDeleteCard());

            ownerRow.getChildren().addAll(editCardBtn, deleteCardBtn);
            actionBox.getChildren().addAll(viewDetailsBtn, actionBtn, ownerRow);
        } else if (userRequest != null && userRequest.isAccepted()) {
            actionBtn = new Button("Joined ✓");
            actionBtn.setStyle("-fx-background-color: rgba(129, 199, 132, 0.2); -fx-text-fill: #81C784; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-border-color: #81C784; -fx-border-radius: 7px;");
            actionBtn.setMaxWidth(Double.MAX_VALUE);
            actionBtn.setDisable(true);
            actionBox.getChildren().addAll(viewDetailsBtn, actionBtn);
        } else if (userRequest != null && userRequest.isPending()) {
            actionBtn = new Button("Pending ⏳");
            actionBtn.setStyle("-fx-background-color: rgba(254, 218, 184, 0.2); -fx-text-fill: #FEDAB8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-border-color: #FEDAB8; -fx-border-radius: 7px;");
            actionBtn.setMaxWidth(Double.MAX_VALUE);
            actionBtn.setDisable(true);
            actionBox.getChildren().addAll(viewDetailsBtn, actionBtn);
        } else if (userRequest != null && userRequest.isRejected()) {
            actionBtn = new Button("Reapply ↻");
            actionBtn.setGraphic(AppIcons.members(13, Color.web("#172D2E")));
            actionBtn.getStyleClass().add("home-btn-primary");
            actionBtn.setStyle("-fx-font-size: 12px; -fx-padding: 8 16;");
            actionBtn.setMaxWidth(Double.MAX_VALUE);
            actionBtn.setOnAction(e -> showJoinDialog());
            actionBox.getChildren().addAll(viewDetailsBtn, actionBtn);
        } else if (isFull || !isOpen) {
            actionBtn = new Button("Full");
            actionBtn.setStyle("-fx-background-color: rgba(214, 184, 156, 0.15); -fx-text-fill: #D6B89C; -fx-font-size: 12px; -fx-padding: 8 16;");
            actionBtn.setMaxWidth(Double.MAX_VALUE);
            actionBtn.setDisable(true);
            actionBox.getChildren().addAll(viewDetailsBtn, actionBtn);
        } else {
            actionBtn = new Button("Join Activity");
            actionBtn.setGraphic(AppIcons.members(13, Color.web("#172D2E")));
            actionBtn.getStyleClass().add("home-btn-primary");
            actionBtn.setStyle("-fx-font-size: 12px; -fx-padding: 8 16;");
            actionBtn.setMaxWidth(Double.MAX_VALUE);
            actionBtn.setOnAction(e -> showJoinDialog());
            actionBox.getChildren().addAll(viewDetailsBtn, actionBtn);
        }
        getChildren().addAll(leftContent, actionBox);

        // Hover effect
        setOnMouseEntered(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(160), this);
            tt.setToY(-3);
            tt.play();
            setStyle("-fx-background-color: #365657; "
                    + "-fx-background-radius: 12px; "
                    + "-fx-border-color: #FEDAB8; "
                    + "-fx-border-radius: 12px; "
                    + "-fx-effect: dropshadow(gaussian, rgba(254, 218, 184, 0.22), 14, 0, 0, 3);");
        });
        setOnMouseExited(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(160), this);
            tt.setToY(0);
            tt.play();
            setStyle("-fx-background-color: #2F4C4D; "
                    + "-fx-background-radius: 12px; "
                    + "-fx-border-color: rgba(214, 184, 156, 0.22); "
                    + "-fx-border-radius: 12px; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.25), 10, 0, 0, 2);");
        });
    }

    private HBox createMetaItem(javafx.scene.Node icon, String text) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #D6B89C;");
        box.getChildren().addAll(icon, label);
        return box;
    }

    private void showJoinDialog() {
        JoinRequestDialog dialog = new JoinRequestDialog(activity, navigator);
        dialog.showAndWait().ifPresent(submitted -> {
            if (submitted) {
                if (onRefresh != null) {
                    onRefresh.run();
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("TeamUp — Request Sent");
                alert.setHeaderText("Join Request Submitted!");
                alert.setContentText("Your teammate request for '" + activity.getTitle() + "' was sent to the organizer (" + activity.getCreatedBy() + ").");
                alert.showAndWait();
            }
        });
    }

    private void confirmAndDeleteCard() {
        String currentReg = navigator.getCurrentStudentReg();
        if (activity.getCreatedBy() == null || !activity.getCreatedBy().equalsIgnoreCase(currentReg)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Permission Denied");
            alert.setHeaderText("Unauthorized Action");
            alert.setContentText("You can only delete activities that you created.");
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
                if (onRefresh != null) onRefresh.run();
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
