package teamup.ui.components;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import teamup.ui.PageNavigator;
import teamup.ui.UITheme;

/**
 * Category card for the Home page. A click opens Explore with this category selected.
 */
public class CategoryCard extends VBox {

    private TranslateTransition lift;

    public CategoryCard(String categoryName, String description, PageNavigator navigator) {
        this("", categoryName, description, navigator);
    }

    public CategoryCard(String index, String categoryName, String description, PageNavigator navigator) {
        getStyleClass().add("category-card");
        setSpacing(12);
        setAlignment(Pos.TOP_LEFT);
        setPrefHeight(176);
        setMaxWidth(Double.MAX_VALUE);

        FontIcon icon = AppIcons.categoryIcon(categoryName, 18, UITheme.GOLD_DEEP);
        StackPane iconWell = new StackPane(icon);
        iconWell.getStyleClass().add("category-icon-well");

        Label titleLabel = new Label(categoryName);
        titleLabel.getStyleClass().add("category-card-title");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topRow = new HBox(12, iconWell, titleLabel, topSpacer);
        if (index != null && !index.isBlank()) {
            Label indexLabel = new Label(index);
            indexLabel.getStyleClass().add("category-card-index");
            topRow.getChildren().add(indexLabel);
        }
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("category-card-desc");
        descLabel.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label actionLabel = new Label("Explore  →");
        actionLabel.getStyleClass().add("category-card-action");

        getChildren().addAll(topRow, descLabel, spacer, actionLabel);

        setOnMouseClicked(e -> navigator.showExplore(categoryName));
        setOnMouseEntered(e -> {
            liftTo(-3);
            scaleIcon(iconWell, 1.06);
        });
        setOnMouseExited(e -> {
            liftTo(0);
            scaleIcon(iconWell, 1.0);
        });
    }

    private void liftTo(double y) {
        if (lift != null) {
            lift.stop();
        }
        lift = new TranslateTransition(Duration.millis(180), this);
        lift.setToY(y);
        lift.setInterpolator(Interpolator.EASE_BOTH);
        lift.play();
    }

    private void scaleIcon(StackPane iconWell, double scale) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(180), iconWell);
        pulse.setToX(scale);
        pulse.setToY(scale);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.play();
    }
}
