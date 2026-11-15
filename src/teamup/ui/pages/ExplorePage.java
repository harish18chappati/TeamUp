package teamup.ui.pages;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import teamup.db.DatabaseManager;
import teamup.model.Activity;
import teamup.ui.PageNavigator;
import teamup.ui.components.ActivityCard;
import teamup.ui.components.AppIcons;

import java.util.ArrayList;
import java.util.List;

/**
 * ExplorePage – Classical directory feed of campus activities.
 * Styled in Slate Grav (#203C3D) and Soft Slate (#344F50) with Peach Buzz (#FEDAB8) typography.
 */
public class ExplorePage extends BorderPane {

    private final PageNavigator navigator;
    private final TextField searchField;
    private final Label countLabel;
    private final VBox cardsList;
    private final List<Button> pillButtons = new ArrayList<>();
    private List<Activity> cachedCategoryActivities = new ArrayList<>();

    private String currentCategory = "All";

    private static final String[] CATEGORIES = {
            "All", "Travel", "Events", "Sports", "Hackathons", "Study Groups", "Entertainment"
    };

    public ExplorePage(PageNavigator navigator) {
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
        Label kicker = new Label("CAMPUS DIRECTORY · SRM UNIVERSITY-AP");
        kicker.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D6B89C;");

        Label pageTitle = new Label("Explore Campus Activities");
        pageTitle.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 32px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");

        Region rule = new Region();
        rule.setStyle("-fx-background-color: #FEDAB8; -fx-pref-width: 48px; -fx-max-width: 48px; -fx-pref-height: 2px; -fx-min-height: 2px;");

        Label pageSub = new Label("Discover upcoming initiatives, hackathons, sports matches, and travel groups.");
        pageSub.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #E6D5C5;");

        titleBox.getChildren().addAll(kicker, pageTitle, rule, pageSub);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button createBtn = new Button("+ Create Activity");
        createBtn.getStyleClass().add("home-btn-primary");
        createBtn.setOnAction(e -> navigator.showCreateActivity(currentCategory.equals("All") ? null : currentCategory));

        headerRow.getChildren().addAll(titleBox, headerSpacer, createBtn);

        // ── SEARCH BAR ROW ────────────────────────────────────────────────────
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.setPadding(new Insets(4, 0, 4, 0));

        searchField = new TextField();
        searchField.setPromptText("Search activities by title, keywords or topic...");
        searchField.setPrefHeight(42);
        searchField.setStyle("-fx-background-color: #172D2E; "
                + "-fx-text-fill: #FEDAB8; "
                + "-fx-prompt-text-fill: #A38C77; "
                + "-fx-font-size: 13.5px; "
                + "-fx-border-color: rgba(214, 184, 156, 0.3); "
                + "-fx-border-radius: 6px; "
                + "-fx-background-radius: 6px; "
                + "-fx-padding: 8 14;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.setOnAction(e -> applyFilterAndSearch());
        // Dynamic instant filtering on keystroke without database hits
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndRender());

        Button searchBtn = new Button("Search");
        searchBtn.setGraphic(AppIcons.search(14, Color.web("#172D2E")));
        searchBtn.getStyleClass().add("home-btn-primary");
        searchBtn.setPrefHeight(42);
        searchBtn.setOnAction(e -> applyFilterAndSearch());

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("home-btn-secondary");
        clearBtn.setPrefHeight(42);
        clearBtn.setOnAction(e -> {
            searchField.clear();
        });

        searchRow.getChildren().addAll(searchField, searchBtn, clearBtn);

        // ── FILTER PILLS ROW ──────────────────────────────────────────────────
        HBox pillsRow = new HBox(8);
        pillsRow.setAlignment(Pos.CENTER_LEFT);

        for (String cat : CATEGORIES) {
            Button pill = new Button(cat.equals("All") ? "All Activities" : cat);
            pill.setStyle("-fx-background-color: #264445; "
                    + "-fx-text-fill: #D6B89C; "
                    + "-fx-font-size: 12px; "
                    + "-fx-font-weight: bold; "
                    + "-fx-border-color: rgba(214, 184, 156, 0.25); "
                    + "-fx-border-radius: 18px; "
                    + "-fx-background-radius: 18px; "
                    + "-fx-padding: 6 16; "
                    + "-fx-cursor: hand;");
            pill.setOnAction(e -> selectCategory(cat));
            pillButtons.add(pill);
            pillsRow.getChildren().add(pill);
        }

        // ── COUNT ROW ─────────────────────────────────────────────────────────
        countLabel = new Label("Showing activities...");
        countLabel.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #D6B89C; -fx-font-weight: bold;");

        // ── CARDS FEED ────────────────────────────────────────────────────────
        cardsList = new VBox(14);
        cardsList.setAlignment(Pos.TOP_CENTER);
        cardsList.setMaxWidth(Double.MAX_VALUE);

        contentBox.getChildren().addAll(headerRow, searchRow, pillsRow, countLabel, cardsList);
        pageBody.getChildren().add(contentBox);

        ScrollPane scrollPane = new ScrollPane(pageBody);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("home-scroll-pane");

        setCenter(scrollPane);

        // Initial load
        selectCategory("All");
    }

    public void selectCategory(String category) {
        this.currentCategory = (category == null || category.isBlank()) ? "All" : category;

        // Update pill styling
        for (Button btn : pillButtons) {
            String text = btn.getText();
            boolean isMatch = (currentCategory.equalsIgnoreCase("All") && text.startsWith("All"))
                    || text.equalsIgnoreCase(currentCategory);
            if (isMatch) {
                btn.setStyle("-fx-background-color: #FEDAB8; "
                        + "-fx-text-fill: #172D2E; "
                        + "-fx-font-size: 12px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-border-color: #FEDAB8; "
                        + "-fx-border-radius: 18px; "
                        + "-fx-background-radius: 18px; "
                        + "-fx-padding: 6 16; "
                        + "-fx-cursor: hand; "
                        + "-fx-effect: dropshadow(gaussian, rgba(254, 218, 184, 0.3), 8, 0, 0, 1);");
            } else {
                btn.setStyle("-fx-background-color: #264445; "
                        + "-fx-text-fill: #D6B89C; "
                        + "-fx-font-size: 12px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-border-color: rgba(214, 184, 156, 0.25); "
                        + "-fx-border-radius: 18px; "
                        + "-fx-background-radius: 18px; "
                        + "-fx-padding: 6 16; "
                        + "-fx-cursor: hand;");
            }
        }

        loadCategoryCache();
        filterAndRender();
    }

    private void loadCategoryCache() {
        if ("All".equalsIgnoreCase(currentCategory)) {
            cachedCategoryActivities = DatabaseManager.getAllActivities();
        } else {
            cachedCategoryActivities = DatabaseManager.getActivitiesByCategory(currentCategory);
        }
        if (cachedCategoryActivities == null) {
            cachedCategoryActivities = new ArrayList<>();
        }
    }

    private void filterAndRender() {
        String keyword = searchField.getText() != null ? searchField.getText().trim() : "";
        List<Activity> results;
        if (keyword.isEmpty()) {
            results = cachedCategoryActivities;
        } else {
            String lowerKw = keyword.toLowerCase();
            results = cachedCategoryActivities.stream()
                    .filter(a -> a.getTitle() != null && a.getTitle().toLowerCase().contains(lowerKw))
                    .toList();
        }
        renderCards(results);
    }

    public void applyFilterAndSearch() {
        loadCategoryCache();
        filterAndRender();
    }

    private void renderCards(List<Activity> activities) {
        cardsList.getChildren().clear();

        int count = activities != null ? activities.size() : 0;
        String filterName = currentCategory.equals("All") ? "All Categories" : currentCategory;
        countLabel.setText("Showing " + count + " " + (count == 1 ? "activity" : "activities")
                + " in " + filterName
                + (!searchField.getText().isBlank() ? " matching \"" + searchField.getText().trim() + "\"" : ""));

        if (count == 0) {
            renderEmptyState();
            return;
        }

        for (Activity act : activities) {
            ActivityCard card = new ActivityCard(act, navigator, "EXPLORE", this::applyFilterAndSearch);
            cardsList.getChildren().add(card);
        }
    }

    private void renderEmptyState() {
        VBox emptyBox = new VBox(14);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(60, 20, 60, 20));
        emptyBox.setStyle("-fx-background-color: #1A3233; -fx-background-radius: 12px; -fx-border-color: rgba(214, 184, 156, 0.18); -fx-border-radius: 12px;");

        Label title = new Label("No activities found");
        title.setStyle("-fx-font-family: 'Palatino Linotype', 'Georgia', 'Times New Roman', serif; "
                + "-fx-font-size: 22px; "
                + "-fx-font-weight: bold; "
                + "-fx-text-fill: #FEDAB8;");

        Label sub = new Label("No activities matched your search criteria. Try a different keyword or category.");
        sub.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #E6D5C5;");

        Button resetBtn = new Button("Reset Filters");
        resetBtn.getStyleClass().add("home-btn-secondary");
        resetBtn.setOnAction(e -> {
            searchField.clear();
            selectCategory("All");
        });

        emptyBox.getChildren().addAll(AppIcons.search(36, Color.web("#FEDAB8")), title, sub, resetBtn);
        cardsList.getChildren().add(emptyBox);
    }
}
