package teamup.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import teamup.ui.PageNavigator;

import java.util.HashMap;
import java.util.Map;

/**
 * AppNavBar – Single global top navigation bar across the entire TeamUp application.
 * Styled in Deep Forest (#172D2E) with Peach Buzz (#FEDAB8) and Muted Peach (#D6B89C).
 *
 * Left: TeamUp logo/crest, brand name, and SRM University-AP badge.
 * Right: Home, Explore Activities, Create Activity, My Activities, student profile, and + Create Post button.
 */
public class AppNavBar extends HBox {

    private final PageNavigator navigator;
    private final Label profileNameLabel;
    private final Label profilePillLabel;
    private final Map<String, Button> navButtons = new HashMap<>();

    public AppNavBar(PageNavigator navigator, String initialActiveKey) {
        this.navigator = navigator;

        getStyleClass().add("home-sticky-bar");
        setSpacing(14);
        setAlignment(Pos.CENTER_LEFT);

        // ── LEFT: Circular Mini-Crest & Brand ─────────────────────────────────
        StackPane miniEmblem = new StackPane();
        miniEmblem.getStyleClass().add("home-nav-emblem");
        FontIcon capIcon = AppIcons.graduationCap(16, Color.web("#FEDAB8"));
        miniEmblem.getChildren().add(capIcon);

        Label brandTitle = new Label("TeamUp");
        brandTitle.getStyleClass().add("home-nav-brand-title");

        Label uniBadge = new Label("SRM UNIVERSITY-AP");
        uniBadge.getStyleClass().add("home-nav-uni-badge");

        HBox brandGroup = new HBox(10, miniEmblem, brandTitle, uniBadge);
        brandGroup.setAlignment(Pos.CENTER_LEFT);
        brandGroup.setCursor(javafx.scene.Cursor.HAND);
        brandGroup.setOnMouseClicked(e -> navigator.showHome());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── NAVIGATION LINKS ──────────────────────────────────────────────────
        Button linkHome = createNavLink("Home", "HOME", e -> navigator.showHome());
        Button linkExplore = createNavLink("Explore Activities", "EXPLORE", e -> navigator.showExplore(null));
        Button linkCreate = createNavLink("Create Activity", "CREATE", e -> navigator.showCreateActivity(null));
        Button linkMy = createNavLink("My Activities", "MY_ACTIVITIES", e -> navigator.showMyActivities());

        // ── ACTIVE STUDENT PROFILE AREA ───────────────────────────────────────
        HBox profilePill = new HBox(8);
        profilePill.setAlignment(Pos.CENTER);
        profilePill.setPadding(new Insets(4, 12, 4, 12));
        profilePill.setStyle("-fx-background-color: rgba(52, 79, 80, 0.65); "
                + "-fx-background-radius: 14px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.35); "
                + "-fx-border-radius: 14px; "
                + "-fx-cursor: hand;");

        FontIcon userIcon = AppIcons.user(13, Color.web("#FEDAB8"));

        VBox profileTextBox = new VBox(1);
        profileTextBox.setAlignment(Pos.CENTER_LEFT);

        profileNameLabel = new Label(navigator.getCurrentStudentName());
        profileNameLabel.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #FEDAB8;");

        profilePillLabel = new Label("ID: " + navigator.getCurrentStudentReg());
        profilePillLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #D6B89C;");

        profileTextBox.getChildren().addAll(profileNameLabel, profilePillLabel);
        profilePill.getChildren().addAll(userIcon, profileTextBox);

        Tooltip.install(profilePill, new Tooltip("Active student identity. Click to switch student."));
        profilePill.setOnMouseClicked(e -> navigator.showWelcome());

        profilePill.setOnMouseEntered(e -> profilePill.setStyle("-fx-background-color: #344F50; "
                + "-fx-background-radius: 14px; "
                + "-fx-border-color: #FEDAB8; "
                + "-fx-border-radius: 14px; "
                + "-fx-cursor: hand;"));

        profilePill.setOnMouseExited(e -> profilePill.setStyle("-fx-background-color: rgba(52, 79, 80, 0.65); "
                + "-fx-background-radius: 14px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.35); "
                + "-fx-border-radius: 14px; "
                + "-fx-cursor: hand;"));

        // Dedicated Change Student Demonstration Button
        Button changeStudentBtn = new Button("Change Student");
        changeStudentBtn.setStyle("-fx-background-color: rgba(52, 79, 80, 0.5); "
                + "-fx-text-fill: #FEDAB8; "
                + "-fx-font-size: 11px; "
                + "-fx-font-weight: bold; "
                + "-fx-padding: 6 11; "
                + "-fx-background-radius: 14px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.4); "
                + "-fx-border-radius: 14px; "
                + "-fx-cursor: hand;");

        changeStudentBtn.setOnMouseEntered(e -> changeStudentBtn.setStyle("-fx-background-color: #344F50; -fx-text-fill: #FFF0E0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 11; -fx-background-radius: 14px; -fx-border-color: #FEDAB8; -fx-border-radius: 14px; -fx-cursor: hand;"));
        changeStudentBtn.setOnMouseExited(e -> changeStudentBtn.setStyle("-fx-background-color: rgba(52, 79, 80, 0.5); -fx-text-fill: #FEDAB8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 11; -fx-background-radius: 14px; -fx-border-color: rgba(214, 184, 156, 0.4); -fx-border-radius: 14px; -fx-cursor: hand;"));
        changeStudentBtn.setOnAction(e -> navigator.showWelcome());
        Tooltip.install(changeStudentBtn, new Tooltip("Return to Welcome screen to switch student for demonstration"));

        // Right CTA Button: "+ Create Post"
        Button ctaPost = new Button("+ Create Post");
        ctaPost.getStyleClass().add("home-nav-cta");
        ctaPost.setOnAction(e -> navigator.showCreateActivity(null));

        HBox linksGroup = new HBox(8, linkHome, linkExplore, linkCreate, linkMy, profilePill, changeStudentBtn, ctaPost);
        linksGroup.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(brandGroup, spacer, linksGroup);

        setActiveNav(initialActiveKey);
    }

    private Button createNavLink(String text, String key, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.getStyleClass().add("home-nav-link");
        btn.setOnAction(handler);
        navButtons.put(key.toUpperCase(), btn);
        return btn;
    }

    public void setActiveNav(String key) {
        String upper = key != null ? key.toUpperCase() : "HOME";
        navButtons.forEach((k, btn) -> {
            btn.getStyleClass().remove("home-nav-link-active");
            if (k.equalsIgnoreCase(upper)) {
                btn.getStyleClass().add("home-nav-link-active");
            }
        });
    }

    public void updateStudentInfo(String name, String regNumber) {
        if (profileNameLabel != null && name != null && !name.isBlank()) {
            profileNameLabel.setText(name.trim());
        }
        if (profilePillLabel != null && regNumber != null && !regNumber.isBlank()) {
            profilePillLabel.setText("ID: " + regNumber.trim().toUpperCase());
        }
    }

    public void updateStudentId(String studentId) {
        if (studentId != null) {
            updateStudentInfo(navigator.getCurrentStudentName(), studentId);
        }
    }
}
