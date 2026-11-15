package teamup.ui.pages;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import teamup.db.DatabaseManager;
import teamup.model.Activity;
import teamup.ui.PageNavigator;
import teamup.ui.components.AppIcons;
import teamup.ui.components.JoinRequestDialog;

/**
 * HomePage – Classical, elegant, premium landing dashboard for TeamUp.
 * Features tasteful colour distribution across sections:
 *   - Navigation: Deep Forest (#172D2E)
 *   - Hero: Slate Grav (#203C3D) with dark translucent overlay
 *   - Statistics: Soft Slate (#344F50)
 *   - Categories: Darker Slate Grav (#1A3233) with #264445 cards
 *   - Featured Activities: Contrasting Slate (#223E3F) with #2E4C4D cards
 *   - How TeamUp Works: Deep Forest (#172D2E) with #254041 step cards
 *   - Final CTA: Rich gradient Soft Slate (#344F50) with Peach Buzz accents
 *   - Footer: Deep Forest (#172D2E)
 */
public class HomePage extends BorderPane {

    private final PageNavigator navigator;

    // Stat Labels and animated properties
    private final Label statActivitiesLabel = new Label("0");
    private final Label statOpenLabel = new Label("0");
    private final Label statHostsLabel = new Label("0");
    private final Label statCategoriesLabel = new Label("6");

    private final IntegerProperty animActivities = new SimpleIntegerProperty(0);
    private final IntegerProperty animOpen = new SimpleIntegerProperty(0);
    private final IntegerProperty animHosts = new SimpleIntegerProperty(0);
    private final IntegerProperty animCategories = new SimpleIntegerProperty(0);

    // Featured container for live database records
    private final HBox featuredCardsContainer = new HBox(20);

    private final ScrollPane scrollPane;
    private boolean entrancePlayed = false;

    public HomePage(PageNavigator navigator) {
        this.navigator = navigator;

        getStyleClass().add("home-root-pane");

        // ── SCROLLABLE PAGE BODY ──────────────────────────────────────────
        VBox pageBody = new VBox(40);
        pageBody.setAlignment(Pos.TOP_CENTER);
        pageBody.getStyleClass().add("home-root-pane");

        // Section 2..8: Large Full-Width Hero Section (Slate Grav #203C3D + Overlay)
        StackPane heroSection = buildHeroSection();

        // Constrained width container holding the distinct banded sections
        VBox constrainedSections = new VBox(36);
        constrainedSections.setAlignment(Pos.TOP_CENTER);
        constrainedSections.setMaxWidth(1100);
        constrainedSections.setPadding(new Insets(0, 40, 40, 40));

        // Section 9: Statistics (Soft Slate #344F50)
        HBox statsSection = buildStatisticsSection();

        // Section 10: Six Category Disciplines (Darker Slate Grav #1A3233)
        VBox categorySection = buildCategorySection();

        // Section 11: Featured Activities (Contrasting Slate #223E3F)
        VBox featuredSection = buildFeaturedSection();

        // Section 12: How TeamUp Works (Alternate Deep Forest #172D2E)
        VBox howItWorksSection = buildHowItWorksSection();

        // Section 13A: Call to Action Card (Gradient Soft Slate #344F50)
        VBox ctaSection = buildCallToActionSection();

        constrainedSections.getChildren().addAll(
                statsSection,
                categorySection,
                featuredSection,
                howItWorksSection,
                ctaSection
        );

        // Section 13B: Elegant Footer (Deep Forest #172D2E)
        VBox footerSection = buildFooterSection();

        pageBody.getChildren().addAll(heroSection, constrainedSections, footerSection);

        scrollPane = new ScrollPane(pageBody);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("home-scroll-pane");

        setCenter(scrollPane);

        // Bind animated properties to labels
        animActivities.addListener((obs, oldVal, newVal) -> statActivitiesLabel.setText(String.valueOf(newVal.intValue())));
        animOpen.addListener((obs, oldVal, newVal) -> statOpenLabel.setText(String.valueOf(newVal.intValue())));
        animHosts.addListener((obs, oldVal, newVal) -> statHostsLabel.setText(String.valueOf(newVal.intValue())));
        animCategories.addListener((obs, oldVal, newVal) -> statCategoriesLabel.setText(String.valueOf(newVal.intValue())));

        // Scene trigger for entrance transitions
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && !entrancePlayed) {
                entrancePlayed = true;
                playEntranceTransitions(heroSection, statsSection, categorySection);
            }
        });

        // Load initial data
        refreshSnapshot();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2..8. FULL-WIDTH HERO SECTION (Slate Grav #203C3D + Image + Dark Overlay)
    // ─────────────────────────────────────────────────────────────────────────
    private StackPane buildHeroSection() {
        StackPane hero = new StackPane();
        hero.getStyleClass().add("home-hero-stack");

        // Layer 1: Background Photograph
        ImageView campusImageView = new ImageView();
        campusImageView.setPreserveRatio(true);
        campusImageView.setSmooth(true);
        campusImageView.setManaged(false); // Unmanaged so it doesn't inflate hero bounds or trigger layout loops

        try {
            InputStream is = getClass().getResourceAsStream("/teamup/ui/images/campus_hero.png");
            if (is == null) {
                is = getClass().getResourceAsStream("images/campus_hero.png");
            }
            if (is == null) {
                is = getClass().getResourceAsStream("/teamup/ui/images/campus_hero.jpg");
            }
            if (is == null) {
                is = getClass().getResourceAsStream("images/campus_hero.jpg");
            }
            if (is != null) {
                Image img = new Image(is);
                campusImageView.setImage(img);
            }
        } catch (Exception ex) {
            System.err.println("[HomePage] Note: campus_hero image load: " + ex.getMessage());
        }

        // Dynamically size hero section to fit the FULL image naturally without cropping
        hero.widthProperty().addListener((obs, oldW, newW) -> {
            double w = newW.doubleValue();
            if (w > 0) {
                campusImageView.setFitWidth(w);
                if (campusImageView.getImage() != null && campusImageView.getImage().getWidth() > 0) {
                    double imgW = campusImageView.getImage().getWidth();
                    double imgH = campusImageView.getImage().getHeight();
                    double naturalH = Math.round(w * (imgH / imgW));
                    hero.setPrefHeight(naturalH);
                    hero.setMinHeight(naturalH);
                }
            }
        });

        // Clip hero stack to match exact full image bounds
        javafx.scene.shape.Rectangle heroClip = new javafx.scene.shape.Rectangle();
        heroClip.widthProperty().bind(hero.widthProperty());
        heroClip.heightProperty().bind(hero.heightProperty());
        hero.setClip(heroClip);

        // Central Foreground Content (Uncovered, clear natural photograph)
        VBox heroContent = new VBox(16);
        heroContent.setAlignment(Pos.CENTER);
        heroContent.setPadding(new Insets(56, 32, 60, 32));
        heroContent.setMaxWidth(860);

        // Circular TeamUp Emblem in the Center
        StackPane emblemOuter = new StackPane();
        emblemOuter.getStyleClass().add("home-emblem-outer");

        StackPane emblemInner = new StackPane();
        emblemInner.getStyleClass().add("home-emblem-inner");

        FontIcon emblemIcon = AppIcons.graduationCap(30, Color.web("#FEDAB8"));
        emblemInner.getChildren().add(emblemIcon);
        emblemOuter.getChildren().add(emblemInner);

        // Campus Kicker
        Label kicker = new Label("SRM UNIVERSITY-AP  ·  CAMPUS ACTIVITY & TEAMMATE FINDER");
        kicker.getStyleClass().add("home-hero-kicker");

        // Main Heading
        Label mainHeading = new Label("Life is better when shared.");
        mainHeading.getStyleClass().add("home-hero-title");
        mainHeading.setWrapText(true);

        // Supporting Heading
        Label subHeading = new Label("Find your people. Create memories.");
        subHeading.getStyleClass().add("home-hero-sub");
        subHeading.setWrapText(true);

        // Ornamental Divider Rule
        Region rule = new Region();
        rule.getStyleClass().add("home-hero-rule");

        // Description
        Label description = new Label("From spontaneous trips to exciting hackathons, discover people who are ready to experience more with you.");
        description.getStyleClass().add("home-hero-desc");
        description.setWrapText(true);
        description.setMaxWidth(680);

        // Two Action Buttons
        Button exploreBtn = new Button("Explore Activities");
        exploreBtn.setGraphic(AppIcons.explore(15, Color.web("#0b484d")));
        exploreBtn.getStyleClass().add("home-btn-primary");
        exploreBtn.setOnAction(e -> navigator.showExplore(null));
        addHoverElevation(exploreBtn);

        Button createBtn = new Button("Create Post");
        createBtn.setGraphic(AppIcons.create(15, Color.web("#FEDAB8")));
        createBtn.getStyleClass().add("home-btn-secondary");
        createBtn.setOnAction(e -> navigator.showCreateActivity(null));
        addHoverElevation(createBtn);

        HBox ctaRow = new HBox(16, exploreBtn, createBtn);
        ctaRow.setAlignment(Pos.CENTER);
        ctaRow.setPadding(new Insets(12, 0, 0, 0));

        heroContent.getChildren().addAll(
                emblemOuter,
                kicker,
                mainHeading,
                subHeading,
                rule,
                description,
                ctaRow
        );

        hero.getChildren().addAll(campusImageView,heroContent);
        return hero;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. STATISTICS SECTION (Soft Slate #344F50 with Peach Buzz numbers)
    // ─────────────────────────────────────────────────────────────────────────
    private HBox buildStatisticsSection() {
        HBox statsCard = new HBox();
        statsCard.getStyleClass().add("home-stats-card");
        statsCard.setAlignment(Pos.CENTER);
        statsCard.setMaxWidth(1020);

        VBox c1 = buildStatCell(statActivitiesLabel, "TOTAL CAMPUS ACTIVITIES");
        Region div1 = createStatDivider();
        VBox c2 = buildStatCell(statOpenLabel, "OPEN FOR MEMBERS");
        Region div2 = createStatDivider();
        VBox c3 = buildStatCell(statHostsLabel, "STUDENT ORGANIZERS");
        Region div3 = createStatDivider();
        VBox c4 = buildStatCell(statCategoriesLabel, "CORE DISCIPLINES");

        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        HBox.setHgrow(c4, Priority.ALWAYS);

        statsCard.getChildren().addAll(c1, div1, c2, div2, c3, div3, c4);
        return statsCard;
    }

    private VBox buildStatCell(Label numLabel, String labelText) {
        VBox cell = new VBox(4);
        cell.getStyleClass().add("home-stat-cell");
        numLabel.getStyleClass().add("home-stat-num");

        Label label = new Label(labelText);
        label.getStyleClass().add("home-stat-lbl");

        cell.getChildren().addAll(numLabel, label);
        return cell;
    }

    private Region createStatDivider() {
        Region div = new Region();
        div.getStyleClass().add("home-stat-divider");
        return div;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 10. CATEGORY SECTION (Darker Slate Grav #1A3233 band with #264445 cards)
    // ─────────────────────────────────────────────────────────────────────────
    private VBox buildCategorySection() {
        VBox section = new VBox(24);
        section.setAlignment(Pos.TOP_CENTER);
        section.setMaxWidth(Double.MAX_VALUE);
        section.setStyle("-fx-background-color: #1A3233; "
                + "-fx-background-radius: 14px; "
                + "-fx-padding: 34 32 38 32; "
                + "-fx-border-color: rgba(214, 184, 156, 0.16); "
                + "-fx-border-radius: 14px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 14, 0, 0, 3);");

        VBox header = buildSectionHeader(
                "CAMPUS DISCIPLINES & EXPERIENCES",
                "Browse by Category",
                "Discover initiatives tailored to your ambitions. Each category connects you with like-minded peers."
        );

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.33);
        col2.setHgrow(Priority.ALWAYS);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.34);
        col3.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2, col3);

        VBox cardTravel = createCategoryCard("01", "Travel", "Weekend getaways, road trips, sunrise treks, and shared rides off campus.", Feather.MAP);
        VBox cardEvents = createCategoryCard("02", "Events", "Campus fests, symposiums, guest talks, and university gatherings.", Feather.AWARD);
        VBox cardSports = createCategoryCard("03", "Sports", "Football, badminton, cricket, and weekend fitness companions.", Feather.ACTIVITY);
        VBox cardHack = createCategoryCard("04", "Hackathons", "24-hour sprints, ideathons, and national coding competitions.", Feather.TERMINAL);
        VBox cardStudy = createCategoryCard("05", "Study Groups", "DSA problem-solving, core engineering subjects, and peer exam revision.", Feather.BOOK_OPEN);
        VBox cardEnt = createCategoryCard("06", "Entertainment", "Movie screenings, board game evenings, and acoustic jam circles.", Feather.FILM);

        grid.add(cardTravel, 0, 0);
        grid.add(cardEvents, 1, 0);
        grid.add(cardSports, 2, 0);
        grid.add(cardHack, 0, 1);
        grid.add(cardStudy, 1, 1);
        grid.add(cardEnt, 2, 1);

        section.getChildren().addAll(header, grid);
        return section;
    }

    private VBox createCategoryCard(String index, String title, String desc, Feather iconName) {
        VBox card = new VBox(12);
        card.getStyleClass().add("home-category-card");
        card.setPrefHeight(170);

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconWell = new StackPane();
        iconWell.getStyleClass().add("home-category-icon-well");
        FontIcon icon = AppIcons.icon(iconName, 18, Color.web("#FEDAB8"));
        iconWell.getChildren().add(icon);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("home-category-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label indexLabel = new Label(index);
        indexLabel.getStyleClass().add("home-category-index");

        topRow.getChildren().addAll(iconWell, titleLabel, spacer, indexLabel);

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("home-category-desc");
        descLabel.setWrapText(true);

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        Label actionLabel = new Label("Explore Discipline  →");
        actionLabel.getStyleClass().add("home-category-action");

        card.getChildren().addAll(topRow, descLabel, bottomSpacer, actionLabel);

        card.setOnMouseClicked(e -> navigator.showExplore(title));
        addCardHover(card);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 11. FEATURED ACTIVITIES (Contrasting Slate #223E3F band with #2E4C4D cards)
    // ─────────────────────────────────────────────────────────────────────────
    private VBox buildFeaturedSection() {
        VBox section = new VBox(24);
        section.setAlignment(Pos.TOP_CENTER);
        section.setMaxWidth(Double.MAX_VALUE);
        section.setStyle("-fx-background-color: #223E3F; "
                + "-fx-background-radius: 14px; "
                + "-fx-padding: 34 32 38 32; "
                + "-fx-border-color: rgba(214, 184, 156, 0.16); "
                + "-fx-border-radius: 14px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 14, 0, 0, 3);");

        VBox header = buildSectionHeader(
                "CURATED FOR YOU",
                "Featured Campus Initiatives",
                "Real active posts currently seeking collaborators and teammates across SRM-AP."
        );

        featuredCardsContainer.setAlignment(Pos.CENTER);
        featuredCardsContainer.setMaxWidth(Double.MAX_VALUE);

        Button viewAllBtn = new Button("View All Campus Activities  →");
        viewAllBtn.getStyleClass().add("home-btn-secondary");
        viewAllBtn.setOnAction(e -> navigator.showExplore(null));
        addHoverElevation(viewAllBtn);

        section.getChildren().addAll(header, featuredCardsContainer, viewAllBtn);
        return section;
    }

    private void renderFeaturedCards(List<Activity> activities) {
        featuredCardsContainer.getChildren().clear();

        if (activities == null || activities.isEmpty()) {
            Label noData = new Label("No activities currently in database. Be the first to create one!");
            noData.getStyleClass().add("home-section-subtitle");
            featuredCardsContainer.getChildren().add(noData);
            return;
        }

        int count = Math.min(3, activities.size());
        for (int i = 0; i < count; i++) {
            Activity act = activities.get(i);
            VBox card = buildFeaturedCard(act);
            HBox.setHgrow(card, Priority.ALWAYS);
            featuredCardsContainer.getChildren().add(card);
        }
    }

    private VBox buildFeaturedCard(Activity activity) {
        VBox card = new VBox(12);
        card.getStyleClass().add("home-featured-card");
        card.setPrefHeight(230);
        card.setMaxWidth(340);

        HBox badgesRow = new HBox(8);
        badgesRow.setAlignment(Pos.CENTER_LEFT);

        Label catBadge = new Label(activity.getCategory().toUpperCase());
        catBadge.getStyleClass().add("home-badge-cat");

        Label statusBadge = new Label("● " + activity.getStatus());
        statusBadge.getStyleClass().add("home-badge-open");

        badgesRow.getChildren().addAll(catBadge, statusBadge);

        Label titleLabel = new Label(activity.getTitle());
        titleLabel.getStyleClass().add("home-featured-title");
        titleLabel.setWrapText(true);

        String desc = activity.getDescription();
        if (desc != null && desc.length() > 95) {
            desc = desc.substring(0, 92).trim() + "...";
        }
        Label descLabel = new Label(desc != null ? desc : "No description provided.");
        descLabel.getStyleClass().add("home-featured-desc");
        descLabel.setWrapText(true);

        VBox metaBox = new VBox(4);
        HBox dateLoc = new HBox(12);
        dateLoc.setAlignment(Pos.CENTER_LEFT);

        Label dateLbl = new Label("📅 " + (activity.getActivityDate() != null ? activity.getActivityDate() : "TBD"));
        dateLbl.getStyleClass().add("home-featured-meta");

        Label locLbl = new Label("📍 " + (activity.getLocation() != null ? activity.getLocation() : "Campus"));
        locLbl.getStyleClass().add("home-featured-meta");
        dateLoc.getChildren().addAll(dateLbl, locLbl);

        int totalOccupied = DatabaseManager.getTotalCurrentMembers(activity.getActivityId());
        Label capLbl = new Label("👥 " + totalOccupied + "/" + activity.getMaxMembers() + " spots filled (Host: " + activity.getCreatedBy() + ")");
        capLbl.getStyleClass().add("home-featured-meta");
        metaBox.getChildren().addAll(dateLoc, capLbl);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        Button detailsBtn = new Button("View Details");
        detailsBtn.getStyleClass().add("home-btn-secondary");
        detailsBtn.setStyle("-fx-font-size: 11.5px; -fx-padding: 7 14;");
        detailsBtn.setOnAction(e -> navigator.showActivityDetails(activity, "HOME"));

        Button joinBtn = new Button("Join");
        joinBtn.getStyleClass().add("home-btn-primary");
        joinBtn.setStyle("-fx-font-size: 11.5px; -fx-padding: 7 16;");
        joinBtn.setOnAction(e -> {
            JoinRequestDialog dialog = new JoinRequestDialog(activity, navigator);
            dialog.showAndWait().ifPresent(submitted -> {
                if (submitted) {
                    refreshSnapshot();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("TeamUp — Request Sent");
                    alert.setHeaderText("Join Request Submitted!");
                    alert.setContentText("Your teammate request for '" + activity.getTitle() + "' was sent to the organizer (" + activity.getCreatedBy() + ").");
                    alert.showAndWait();
                }
            });
        });

        actionsRow.getChildren().addAll(detailsBtn, joinBtn);

        card.getChildren().addAll(badgesRow, titleLabel, descLabel, metaBox, spacer, actionsRow);
        addCardHover(card);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 12. HOW TEAMUP WORKS (Alternate Deep Forest #172D2E with #254041 cards)
    // ─────────────────────────────────────────────────────────────────────────
    private VBox buildHowItWorksSection() {
        VBox section = new VBox(24);
        section.setAlignment(Pos.TOP_CENTER);
        section.setMaxWidth(Double.MAX_VALUE);
        section.setStyle("-fx-background-color: #172D2E; "
                + "-fx-background-radius: 14px; "
                + "-fx-padding: 34 32 38 32; "
                + "-fx-border-color: rgba(214, 184, 156, 0.16); "
                + "-fx-border-radius: 14px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 14, 0, 0, 3);");

        VBox header = buildSectionHeader(
                "SIMPLE & INTUITIVE",
                "How TeamUp Works",
                "Connecting ambitious minds across SRM University-AP in three straightforward steps."
        );

        HBox stepsBox = new HBox(20);
        stepsBox.setAlignment(Pos.CENTER);
        stepsBox.setMaxWidth(Double.MAX_VALUE);

        VBox step1 = createStepCard("STEP 01", Feather.COMPASS, "Discover or Propose",
                "Browse open campus initiatives or post your own trip, project, hackathon, or study circle in seconds.");

        VBox step2 = createStepCard("STEP 02", Feather.USERS, "Connect with Teammates",
                "Review student details, align on dates, discuss objectives, and recruit motivated collaborators.");

        VBox step3 = createStepCard("STEP 03", Feather.AWARD, "Experience & Achieve",
                "Meet up on campus, collaborate, compete, or travel together. Build lasting memories and lifelong friendships.");

        HBox.setHgrow(step1, Priority.ALWAYS);
        HBox.setHgrow(step2, Priority.ALWAYS);
        HBox.setHgrow(step3, Priority.ALWAYS);

        stepsBox.getChildren().addAll(step1, step2, step3);

        section.getChildren().addAll(header, stepsBox);
        return section;
    }

    private VBox createStepCard(String stepNum, Feather iconName, String title, String desc) {
        VBox card = new VBox(14);
        card.getStyleClass().add("home-step-card");
        card.setPrefHeight(210);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconWell = new StackPane();
        iconWell.getStyleClass().add("home-step-icon-well");
        FontIcon icon = AppIcons.icon(iconName, 18, Color.web("#FEDAB8"));
        iconWell.getChildren().add(icon);

        Label stepBadge = new Label(stepNum);
        stepBadge.getStyleClass().add("home-step-badge");

        header.getChildren().addAll(iconWell, stepBadge);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("home-step-title");

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("home-step-desc");
        descLabel.setWrapText(true);

        card.getChildren().addAll(header, titleLabel, descLabel);
        addCardHover(card);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 13A. FINAL CALL TO ACTION (Rich Gradient Soft Slate #344F50)
    // ─────────────────────────────────────────────────────────────────────────
    private VBox buildCallToActionSection() {
        VBox ctaCard = new VBox(18);
        ctaCard.getStyleClass().add("home-cta-card");
        ctaCard.setAlignment(Pos.CENTER);
        ctaCard.setMaxWidth(1020);

        Label kicker = new Label("DON'T WAIT FOR A GROUP. BUILD ONE.");
        kicker.getStyleClass().add("home-cta-kicker");

        Label heading = new Label("Ready to Find Your People at SRM-AP?");
        heading.getStyleClass().add("home-cta-title");
        heading.setWrapText(true);

        Label desc = new Label("Whether you are planning a weekend road trip, assembling a 24-hour hackathon squad, or finding a daily badminton partner, your community is right here.");
        desc.getStyleClass().add("home-cta-desc");
        desc.setWrapText(true);
        desc.setMaxWidth(740);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button expBtn = new Button("Explore All Activities");
        expBtn.setGraphic(AppIcons.explore(15, Color.web("#172D2E")));
        expBtn.getStyleClass().add("home-btn-primary");
        expBtn.setOnAction(e -> navigator.showExplore(null));
        addHoverElevation(expBtn);

        Button postBtn = new Button("Create an Activity");
        postBtn.setGraphic(AppIcons.create(15, Color.web("#FEDAB8")));
        postBtn.getStyleClass().add("home-btn-secondary");
        postBtn.setOnAction(e -> navigator.showCreateActivity(null));
        addHoverElevation(postBtn);

        HBox btnRow = new HBox(16, expBtn, postBtn);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(10, 0, 0, 0));

        ctaCard.getChildren().addAll(kicker, heading, desc, btnRow);
        return ctaCard;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 13B. ELEGANT FOOTER (Deep Forest #172D2E)
    // ─────────────────────────────────────────────────────────────────────────
    private VBox buildFooterSection() {
        VBox footer = new VBox(18);
        footer.getStyleClass().add("home-footer");
        footer.setAlignment(Pos.CENTER);

        HBox brandRow = new HBox(10);
        brandRow.setAlignment(Pos.CENTER);

        FontIcon cap = AppIcons.graduationCap(18, Color.web("#FEDAB8"));
        Label brand = new Label("TeamUp");
        brand.getStyleClass().add("home-footer-brand");

        Label uni = new Label("· SRM University-AP Amaravati");
        uni.getStyleClass().add("home-footer-motto");

        brandRow.getChildren().addAll(cap, brand, uni);

        Label motto = new Label("\"Life is better when shared.\"  ·  Connect. Explore. Experience.");
        motto.getStyleClass().add("home-footer-motto");

        HBox linksRow = new HBox(12);
        linksRow.setAlignment(Pos.CENTER);

        Button fHome = new Button("Home");
        fHome.getStyleClass().add("home-footer-link");
        fHome.setOnAction(e -> scrollPane.setVvalue(0.0));

        Button fExplore = new Button("Explore Activities");
        fExplore.getStyleClass().add("home-footer-link");
        fExplore.setOnAction(e -> navigator.showExplore(null));

        Button fCreate = new Button("Create Activity");
        fCreate.getStyleClass().add("home-footer-link");
        fCreate.setOnAction(e -> navigator.showCreateActivity(null));

        Button fMy = new Button("My Activities");
        fMy.getStyleClass().add("home-footer-link");
        fMy.setOnAction(e -> navigator.showMyActivities());

        linksRow.getChildren().addAll(fHome, fExplore, fCreate, fMy);

        Label copy = new Label("© 2026 TeamUp · Classical University Edition · JavaFX 21 & SQLite");
        copy.getStyleClass().add("home-footer-copy");

        footer.getChildren().addAll(brandRow, motto, linksRow, copy);
        return footer;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATA REFRESH & NUMBER COUNTER ANIMATIONS
    // ─────────────────────────────────────────────────────────────────────────
    public void refreshSnapshot() {
        List<Activity> all = DatabaseManager.getAllActivities();
        int total = all != null ? all.size() : 0;
        int open = 0;
        Set<String> hosts = new HashSet<>();

        if (all != null) {
            for (Activity activity : all) {
                if ("Open".equalsIgnoreCase(activity.getStatus())) {
                    open++;
                }
                if (activity.getCreatedBy() != null && !activity.getCreatedBy().isBlank()) {
                    hosts.add(activity.getCreatedBy().trim().toLowerCase());
                }
            }
        }

        renderFeaturedCards(all);

        animateCounter(animActivities, total);
        animateCounter(animOpen, open);
        animateCounter(animHosts, hosts.size());
        animateCounter(animCategories, 6);
    }

    private void animateCounter(IntegerProperty property, int target) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(property, 0)),
                new KeyFrame(Duration.millis(1100), new KeyValue(property, target, Interpolator.EASE_OUT))
        );
        timeline.play();
    }

    private VBox buildSectionHeader(String kickerText, String titleText, String subtitleText) {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setMaxWidth(720);

        Label kicker = new Label(kickerText);
        kicker.getStyleClass().add("home-section-kicker");

        Label title = new Label(titleText);
        title.getStyleClass().add("home-section-heading");

        Region rule = new Region();
        rule.getStyleClass().add("home-section-rule");

        Label sub = new Label(subtitleText);
        sub.getStyleClass().add("home-section-subtitle");
        sub.setWrapText(true);

        header.getChildren().addAll(kicker, title, rule, sub);
        return header;
    }

    private void addHoverElevation(Button btn) {
        btn.setOnMouseEntered(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(160), btn);
            tt.setToY(-2.5);
            tt.play();
        });
        btn.setOnMouseExited(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(160), btn);
            tt.setToY(0);
            tt.play();
        });
    }

    private void addCardHover(Node card) {
        card.setOnMouseEntered(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(180), card);
            tt.setToY(-4);
            tt.play();
        });
        card.setOnMouseExited(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(180), card);
            tt.setToY(0);
            tt.play();
        });
    }

    private void playEntranceTransitions(Node hero, Node stats, Node categories) {
        animateEntrance(hero, 0);
        animateEntrance(stats, 140);
        animateEntrance(categories, 260);
    }

    private void animateEntrance(Node node, int delayMs) {
        node.setOpacity(0);
        node.setTranslateY(16);

        FadeTransition fade = new FadeTransition(Duration.millis(520), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMs));
        fade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition slide = new TranslateTransition(Duration.millis(520), node);
        slide.setFromY(16);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delayMs));
        slide.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide).play();
    }
}
