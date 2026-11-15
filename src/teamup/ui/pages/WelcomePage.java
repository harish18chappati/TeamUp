package teamup.ui.pages;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import teamup.db.DatabaseManager;
import teamup.model.Student;
import teamup.model.StudentSession;
import teamup.model.StudentValidator;
import teamup.ui.PageNavigator;
import teamup.ui.components.AppIcons;

import java.io.InputStream;

/**
 * WelcomePage – Student Welcome and Identification Screen for TeamUp.
 *
 * Appears upon application startup and during prototype demonstration profile switches.
 * Collects and validates:
 *   - Student Full Name
 *   - SRM Registration Number (e.g. AP21110010001)
 *
 * Visual Design:
 *   - Slate Grav (#203C3D) + Deep Forest (#172D2E) + Soft Slate (#344F50)
 *   - Headings & Accents: Peach Buzz (#FEDAB8)
 *   - Borders: Muted Peach (#D6B89C)
 *   - High-resolution campus photographic background with dark translucent overlay
 *   - Classical typography and smooth entrance animation
 */
public class WelcomePage extends StackPane {

    private final PageNavigator navigator;

    private final TextField nameField;
    private final TextField regField;
    private final HBox errorBox;
    private final Label errorLabel;
    private final Button continueBtn;
    private final Button returnBtn;
    private final VBox card;

    public WelcomePage(PageNavigator navigator) {
        this.navigator = navigator;

        getStyleClass().add("root");
        setStyle("-fx-background-color: #203C3D;");

        // ── LAYER 1: Background Photograph (Campus Hero) ──────────────────────
        ImageView bgImageView = new ImageView();
        bgImageView.setPreserveRatio(true);
        bgImageView.setSmooth(true);
        bgImageView.setManaged(false);

        try {
            InputStream is = getClass().getResourceAsStream("/teamup/ui/images/campus_hero.png");
            if (is == null) is = getClass().getResourceAsStream("images/campus_hero.png");
            if (is == null) is = getClass().getResourceAsStream("/teamup/ui/images/campus_hero.jpg");
            if (is == null) is = getClass().getResourceAsStream("images/campus_hero.jpg");
            if (is != null) {
                bgImageView.setImage(new Image(is));
            }
        } catch (Exception ex) {
            System.err.println("[WelcomePage] Note: campus image load: " + ex.getMessage());
        }

        widthProperty().addListener((obs, oldW, newW) -> {
            double w = newW.doubleValue();
            if (w > 0) {
                bgImageView.setFitWidth(Math.max(w, 1180));
            }
        });

        // ── LAYER 2: Classical Dark Translucent Overlay ───────────────────────
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(23, 45, 46, 0.88), rgba(32, 60, 61, 0.94));");

        // ── LAYER 3: Scrollable Centered Card Container ───────────────────────
        card = new VBox(18);
        card.setMaxWidth(530);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(32, 36, 32, 36));
        card.setStyle("-fx-background-color: #172D2E; "
                + "-fx-background-radius: 16px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.45); "
                + "-fx-border-width: 1.2px; "
                + "-fx-border-radius: 16px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.45), 30, 0, 0, 10);");

        // 1. Circular TeamUp Emblem
        StackPane emblem = new StackPane();
        emblem.setPrefSize(56, 56);
        emblem.setMaxSize(56, 56);
        emblem.setStyle("-fx-background-color: #203C3D; "
                + "-fx-background-radius: 50%; "
                + "-fx-border-color: #FEDAB8; "
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 50%; "
                + "-fx-effect: dropshadow(gaussian, rgba(254, 218, 184, 0.25), 14, 0, 0, 0);");
        FontIcon capIcon = AppIcons.graduationCap(26, Color.web("#FEDAB8"));
        emblem.getChildren().add(capIcon);

        // 2. SRM University-AP Badge
        Label uniBadge = new Label("SRM UNIVERSITY-AP · AMARAVATI");
        uniBadge.setStyle("-fx-font-family: 'Segoe UI', sans-serif; "
                + "-fx-font-size: 10.5px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #D6B89C; "
                + "-fx-background-color: #203C3D; "
                + "-fx-padding: 4 10 4 10; "
                + "-fx-background-radius: 4px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.3); "
                + "-fx-border-radius: 4px;");

        // 3. Main Welcome Heading
        Label welcomeTitle = new Label("Welcome to TeamUp");
        welcomeTitle.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', serif; "
                + "-fx-font-size: 27px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8; "
                + "-fx-padding: 2 0 0 0;");

        // 4. Supporting Subtitle
        Label subtitle = new Label("Find your squad, coordinate campus activities, and build lifelong memories.");
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        subtitle.setStyle("-fx-font-family: 'Segoe UI', sans-serif; "
                + "-fx-font-size: 13px; "
                + "-fx-text-fill: #E6D5C5; "
                + "-fx-line-spacing: 2px;");

        // Elegant subtle separator line
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setMaxWidth(460);
        divider.setStyle("-fx-background-color: linear-gradient(to right, transparent, rgba(214, 184, 156, 0.4), transparent);");

        // 5. Section Header
        VBox sectionBox = new VBox(4);
        sectionBox.setAlignment(Pos.CENTER_LEFT);
        sectionBox.setMaxWidth(Double.MAX_VALUE);

        Label secTitle = new Label("STUDENT IDENTIFICATION");
        secTitle.setStyle("-fx-font-family: 'Segoe UI', sans-serif; "
                + "-fx-font-size: 11px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8; "
                + "-fx-letter-spacing: 1px;");

        Label secDesc = new Label("Please identify yourself to personalize your campus experience and manage activities.");
        secDesc.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #A8C2C0;");
        sectionBox.getChildren().addAll(secTitle, secDesc);

        // 6. Form Fields
        VBox formBox = new VBox(14);
        formBox.setMaxWidth(Double.MAX_VALUE);

        // Field 1: Full Name
        VBox nameGroup = new VBox(6);
        Label nameLbl = new Label("Full Name *");
        nameLbl.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");

        nameField = new TextField();
        nameField.setPromptText("e.g. Rahul Varma");
        nameField.setPrefHeight(42);
        styleInput(nameField);

        nameGroup.getChildren().addAll(nameLbl, nameField);

        // Field 2: SRM Registration Number
        VBox regGroup = new VBox(6);
        Label regLbl = new Label("SRM Registration Number *");
        regLbl.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");

        regField = new TextField();
        regField.setPromptText("e.g. AP21110010001");
        regField.setPrefHeight(42);
        styleInput(regField);

        // Automatically convert input to uppercase
        regField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.equals(newV.toUpperCase())) {
                regField.setText(newV.toUpperCase());
            }
        });

        Label regHint = new Label("Format: AP followed by 11 digits (e.g. AP21110010001)");
        regHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #8FA6A4;");

        regGroup.getChildren().addAll(regLbl, regField, regHint);

        // 7. Validation / Error Box (initially hidden)
        errorBox = new HBox(8);
        errorBox.setAlignment(Pos.CENTER_LEFT);
        errorBox.setPadding(new Insets(8, 12, 8, 12));
        errorBox.setStyle("-fx-background-color: rgba(220, 53, 69, 0.2); "
                + "-fx-border-color: rgba(220, 53, 69, 0.6); "
                + "-fx-background-radius: 6px; "
                + "-fx-border-radius: 6px;");
        errorBox.setVisible(false);
        errorBox.setManaged(false);

        FontIcon alertIcon = AppIcons.alert(14, Color.web("#FF8A80"));
        errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF8A80; -fx-font-weight: bold;");
        errorBox.getChildren().addAll(alertIcon, errorLabel);

        // 8. Quick Demo Profiles (Prototype Demonstration Helpers)
        VBox demoBox = new VBox(6);
        Label demoTitle = new Label("Quick Demo Profiles (Click to fill):");
        demoTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");

        HBox demoPills = new HBox(8);
        demoPills.setAlignment(Pos.CENTER_LEFT);

        Button chip1 = createDemoChip("Rahul Varma", "AP21110010001", "Organizer");
        Button chip2 = createDemoChip("Priya Sharma", "AP21110010002", "Teammate");
        Button chip3 = createDemoChip("Ananya Reddy", "AP21110010003", "Teammate");

        demoPills.getChildren().addAll(chip1, chip2, chip3);
        demoBox.getChildren().addAll(demoTitle, demoPills);

        // 9. Prominent Continue Button
        continueBtn = new Button("Continue to Campus  →");
        continueBtn.setPrefHeight(46);
        continueBtn.setMaxWidth(Double.MAX_VALUE);
        continueBtn.setStyle("-fx-background-color: #FEDAB8; "
                + "-fx-text-fill: #172D2E; "
                + "-fx-font-size: 14px; "
                + "-fx-font-weight: bold; "
                + "-fx-background-radius: 8px; "
                + "-fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(254, 218, 184, 0.25), 10, 0, 0, 2);");

        continueBtn.setOnMouseEntered(e ->
                continueBtn.setStyle("-fx-background-color: #FFF0E0; "
                        + "-fx-text-fill: #172D2E; "
                        + "-fx-font-size: 14px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-background-radius: 8px; "
                        + "-fx-cursor: hand; "
                        + "-fx-effect: dropshadow(gaussian, rgba(255, 240, 224, 0.4), 14, 0, 0, 3);"));

        continueBtn.setOnMouseExited(e ->
                continueBtn.setStyle("-fx-background-color: #FEDAB8; "
                        + "-fx-text-fill: #172D2E; "
                        + "-fx-font-size: 14px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-background-radius: 8px; "
                        + "-fx-cursor: hand; "
                        + "-fx-effect: dropshadow(gaussian, rgba(254, 218, 184, 0.25), 10, 0, 0, 2);"));

        continueBtn.setOnAction(e -> handleContinue());

        // Also allow Enter key press to submit
        nameField.setOnAction(e -> handleContinue());
        regField.setOnAction(e -> handleContinue());

        // 10. Return to App Button (when returning from inside the app)
        returnBtn = new Button("← Return to Application");
        returnBtn.setStyle("-fx-background-color: transparent; "
                + "-fx-text-fill: #D6B89C; "
                + "-fx-font-size: 12px; "
                + "-fx-font-weight: bold; "
                + "-fx-cursor: hand;");
        returnBtn.setOnMouseEntered(e -> returnBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FEDAB8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;"));
        returnBtn.setOnMouseExited(e -> returnBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #D6B89C; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;"));
        returnBtn.setOnAction(e -> navigator.showHome());
        returnBtn.setVisible(false);
        returnBtn.setManaged(false);

        // 11. Academic Prototype Footer
        Label footerNote = new Label("Academic Project Prototype · SRM University-AP Amaravati");
        footerNote.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #8FA6A4; -fx-padding: 4 0 0 0;");

        formBox.getChildren().addAll(nameGroup, regGroup, errorBox, demoBox, continueBtn, returnBtn);

        card.getChildren().addAll(
                emblem,
                uniBadge,
                welcomeTitle,
                subtitle,
                divider,
                sectionBox,
                formBox,
                footerNote
        );

        // Centering Container inside a transparent ScrollPane for safety on smaller displays
        VBox centeringWrapper = new VBox(card);
        centeringWrapper.setAlignment(Pos.CENTER);
        centeringWrapper.setPadding(new Insets(24, 20, 24, 20));

        ScrollPane scrollPane = new ScrollPane(centeringWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        getChildren().addAll(bgImageView, overlay, scrollPane);

        // Pre-fill with active session if already present
        prefill(StudentSession.getActiveStudentName(), StudentSession.getActiveStudentReg());
    }

    private void styleInput(TextField tf) {
        tf.setStyle("-fx-background-color: #203C3D; "
                + "-fx-text-fill: #FEDAB8; "
                + "-fx-prompt-text-fill: #8F7B6B; "
                + "-fx-font-size: 13.5px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.35); "
                + "-fx-border-radius: 6px; "
                + "-fx-background-radius: 6px; "
                + "-fx-padding: 8 12;");

        tf.focusedProperty().addListener((obs, oldF, newF) -> {
            if (newF) {
                tf.setStyle("-fx-background-color: #264344; "
                        + "-fx-text-fill: #FEDAB8; "
                        + "-fx-prompt-text-fill: #8F7B6B; "
                        + "-fx-font-size: 13.5px; "
                        + "-fx-border-color: #FEDAB8; "
                        + "-fx-border-radius: 6px; "
                        + "-fx-background-radius: 6px; "
                        + "-fx-padding: 8 12;");
                hideError();
            } else {
                tf.setStyle("-fx-background-color: #203C3D; "
                        + "-fx-text-fill: #FEDAB8; "
                        + "-fx-prompt-text-fill: #8F7B6B; "
                        + "-fx-font-size: 13.5px; "
                        + "-fx-border-color: rgba(214, 184, 156, 0.35); "
                        + "-fx-border-radius: 6px; "
                        + "-fx-background-radius: 6px; "
                        + "-fx-padding: 8 12;");
            }
        });
    }

    private Button createDemoChip(String name, String reg, String role) {
        Button chip = new Button(reg + " (" + role + ")");
        chip.setStyle("-fx-background-color: #203C3D; "
                + "-fx-text-fill: #FEDAB8; "
                + "-fx-font-size: 10.5px; "
                + "-fx-font-weight: bold; "
                + "-fx-padding: 4 8; "
                + "-fx-background-radius: 12px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.3); "
                + "-fx-border-radius: 12px; "
                + "-fx-cursor: hand;");

        chip.setOnAction(e -> {
            nameField.setText(name);
            regField.setText(reg);
            hideError();
        });

        chip.setOnMouseEntered(e -> chip.setStyle("-fx-background-color: #344F50; -fx-text-fill: #FFE0BF; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 12px; -fx-border-color: #FEDAB8; -fx-border-radius: 12px; -fx-cursor: hand;"));
        chip.setOnMouseExited(e -> chip.setStyle("-fx-background-color: #203C3D; -fx-text-fill: #FEDAB8; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 12px; -fx-border-color: rgba(214, 184, 156, 0.3); -fx-border-radius: 12px; -fx-cursor: hand;"));

        return chip;
    }

    private void handleContinue() {
        String rawName = nameField.getText();
        String rawReg = regField.getText();

        String validationError = StudentValidator.validate(rawName, rawReg);
        if (validationError != null) {
            showError(validationError);
            if (rawName == null || rawName.trim().isEmpty() || !StudentValidator.isValidName(rawName)) {
                nameField.requestFocus();
            } else {
                regField.requestFocus();
            }
            return;
        }

        hideError();

        String cleanName = StudentValidator.normalizeName(rawName);
        String cleanReg = StudentValidator.normalizeRegNumber(rawReg);

        // 1. Persist/update in SQLite (idempotent, safe update of display name)
        Student student = DatabaseManager.saveOrUpdateStudent(cleanName, cleanReg);
        if (student == null) {
            student = new Student(cleanName, cleanReg, cleanReg.toLowerCase() + "@srmap.edu.in");
        }

        // 2. Set active application session
        StudentSession.setStudent(student);

        // 3. Update navigator session state and navbar
        navigator.setCurrentStudent(cleanName, cleanReg);

        // 4. Navigate into Home Page
        navigator.showHome();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorBox.setVisible(true);
        errorBox.setManaged(true);
    }

    private void hideError() {
        errorBox.setVisible(false);
        errorBox.setManaged(false);
    }

    /**
     * Pre-fills the fields with student identity, e.g. when reopening via "Change Student".
     */
    public void prefill(String name, String reg) {
        if (name != null && !name.isBlank()) {
            nameField.setText(name.trim());
        }
        if (reg != null && !reg.isBlank()) {
            regField.setText(reg.trim().toUpperCase());
        }
        hideError();

        boolean hasSession = StudentSession.isIdentified();
        returnBtn.setVisible(hasSession);
        returnBtn.setManaged(hasSession);
    }

    /**
     * Triggers smooth card entrance animation.
     */
    public void playEntranceAnimation() {
        card.setOpacity(0);
        card.setTranslateY(16);

        FadeTransition ft = new FadeTransition(Duration.millis(350), card);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), card);
        tt.setFromY(16);
        tt.setToY(0);

        ft.play();
        tt.play();
    }
}
