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
import teamup.model.JoinResult;
import teamup.ui.PageNavigator;

/**
 * JoinRequestDialog – Modal dialog for students to submit or reapply for a teammate request.
 * Styled in TeamUp's classical palette: Slate Grav (#203C3D), Deep Forest (#172D2E), Peach Buzz (#FEDAB8).
 */
public class JoinRequestDialog extends Dialog<Boolean> {

    public JoinRequestDialog(Activity activity, PageNavigator navigator) {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("TeamUp — Request to Join Activity");

        DialogPane pane = getDialogPane();
        pane.setStyle("-fx-background-color: #203C3D; "
                + "-fx-border-color: rgba(214, 184, 156, 0.35); "
                + "-fx-border-width: 1.5px; "
                + "-fx-border-radius: 12px; "
                + "-fx-background-radius: 12px; "
                + "-fx-padding: 24;");

        // Load application CSS
        try {
            pane.getStylesheets().add(getClass().getResource("/teamup/ui/styles.css").toExternalForm());
        } catch (Exception ignored) {}

        String currentReg = navigator.getCurrentStudentReg();
        int totalOccupied = DatabaseManager.getTotalCurrentMembers(activity.getActivityId());
        int availableSeats = DatabaseManager.getAvailableSeats(activity.getActivityId());

        VBox content = new VBox(18);
        content.setPrefWidth(480);
        content.setAlignment(Pos.TOP_LEFT);

        // Header
        VBox headerBox = new VBox(4);
        Label kicker = new Label("TEAMUP · JOIN REQUEST");
        kicker.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");

        Label titleLbl = new Label(activity.getTitle());
        titleLbl.setWrapText(true);
        titleLbl.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', serif; "
                + "-fx-font-size: 22px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");

        Label hostLbl = new Label("Hosted by: " + activity.getCreatedBy() + "  ·  Location: " + activity.getLocation());
        hostLbl.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #E6D5C5;");

        headerBox.getChildren().addAll(kicker, titleLbl, hostLbl);

        // Status Card
        HBox metaCard = new HBox(16);
        metaCard.setAlignment(Pos.CENTER_LEFT);
        metaCard.setPadding(new Insets(12, 16, 12, 16));
        metaCard.setStyle("-fx-background-color: #172D2E; "
                + "-fx-background-radius: 8px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.2); "
                + "-fx-border-radius: 8px;");

        VBox col1 = new VBox(3);
        Label l1 = new Label("APPLICANT ID");
        l1.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");
        Label v1 = new Label(currentReg);
        v1.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");
        col1.getChildren().addAll(l1, v1);

        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);

        VBox col2 = new VBox(3);
        Label l2 = new Label("SEATS OCCUPIED");
        l2.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");
        Label v2 = new Label(totalOccupied + " of " + activity.getMaxMembers() + " (Incl. Host)");
        v2.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");
        col2.getChildren().addAll(l2, v2);

        Region s2 = new Region();
        HBox.setHgrow(s2, Priority.ALWAYS);

        VBox col3 = new VBox(3);
        Label l3 = new Label("AVAILABLE SEATS");
        l3.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");
        Label v3 = new Label(availableSeats + " remaining");
        v3.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + (availableSeats > 0 ? "#81C784" : "#E57373") + ";");
        col3.getChildren().addAll(l3, v3);

        metaCard.getChildren().addAll(col1, s1, col2, s2, col3);

        // Note / Message input
        VBox noteBox = new VBox(6);
        Label notePrompt = new Label("Introduction Note / Message for Organizer:");
        notePrompt.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");

        TextArea noteArea = new TextArea();
        noteArea.setPromptText("Briefly share your skills, interests, or reason for wanting to team up...");
        noteArea.setPrefRowCount(3);
        noteArea.setWrapText(true);
        noteArea.setStyle("-fx-control-inner-background: #172D2E; "
                + "-fx-text-fill: #FEDAB8; "
                + "-fx-prompt-text-fill: #A38C77; "
                + "-fx-font-size: 13px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.3); "
                + "-fx-border-radius: 6px; "
                + "-fx-background-radius: 6px;");

        noteBox.getChildren().addAll(notePrompt, noteArea);

        // Feedback label for inline errors
        Label feedbackLbl = new Label();
        feedbackLbl.setWrapText(true);
        feedbackLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #E57373;");
        feedbackLbl.setVisible(false);

        content.getChildren().addAll(headerBox, metaCard, noteBox, feedbackLbl);
        pane.setContent(content);

        // Buttons
        ButtonType sendBtnType = new ButtonType("Send Request", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtnType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().addAll(sendBtnType, cancelBtnType);

        Button sendBtn = (Button) pane.lookupButton(sendBtnType);
        sendBtn.getStyleClass().add("home-btn-primary");
        sendBtn.setStyle("-fx-background-color: #FEDAB8; -fx-text-fill: #172D2E; -fx-font-weight: bold; -fx-font-size: 12.5px; -fx-padding: 8 18;");

        Button cancelBtn = (Button) pane.lookupButton(cancelBtnType);
        cancelBtn.getStyleClass().add("home-btn-secondary");
        cancelBtn.setStyle("-fx-background-color: #264445; -fx-text-fill: #D6B89C; -fx-font-size: 12.5px; -fx-padding: 8 16;");

        // Action handler
        sendBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String note = noteArea.getText() != null ? noteArea.getText().trim() : "";
            JoinResult result = DatabaseManager.sendJoinRequest(activity.getActivityId(), currentReg, note);
            if (!result.isSuccess()) {
                feedbackLbl.setText("⚠ " + result.getMessage());
                feedbackLbl.setVisible(true);
                event.consume(); // keep dialog open
            }
        });

        setResultConverter(btnType -> {
            if (btnType == sendBtnType) {
                return true;
            }
            return false;
        });
    }
}
