package teamup.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * UITheme – TeamUp design system.
 *
 * Midnight Navy  #192B42  structure, sidebar, primary actions
 * Warm Ivory     #F7F3EA  canvas and quiet surfaces
 * Antique Gold   #B8955A  accent, focus, highlights
 * Soft Sand      #E8DFD0  borders and secondary surfaces
 * Muted Sage     #788575  secondary text
 * Charcoal       #302F2C  body text
 */
public class UITheme {

    // ── Classical Palette (Suresh Tailors Inspired - Home Page) ─────────────
    public static final Color SLATE_GRAV  = Color.web("#203C3D"); // Primary background
    public static final Color PEACH_BUZZ  = Color.web("#FEDAB8"); // Headings & accents
    public static final Color DEEP_FOREST = Color.web("#172D2E"); // Navigation & dark sections
    public static final Color SOFT_SLATE  = Color.web("#344F50"); // Cards & secondary surfaces
    public static final Color MUTED_PEACH = Color.web("#D6B89C"); // Subtle borders & details

    public static final String HEX_SLATE_GRAV  = "#203C3D";
    public static final String HEX_PEACH_BUZZ  = "#FEDAB8";
    public static final String HEX_DEEP_FOREST = "#172D2E";
    public static final String HEX_SOFT_SLATE  = "#344F50";
    public static final String HEX_MUTED_PEACH = "#D6B89C";

    public static final Color MIDNIGHT_NAVY = Color.web("#192B42");
    public static final Color NAVY_MID      = Color.web("#243A56");
    public static final Color WARM_IVORY    = Color.web("#F7F3EA");
    public static final Color IVORY_CARD    = Color.web("#FCFAF6");
    public static final Color ANTIQUE_GOLD  = Color.web("#B8955A");
    public static final Color GOLD_DEEP     = Color.web("#8C6E3B");
    public static final Color SOFT_SAND     = Color.web("#E8DFD0");
    public static final Color MUTED_SAGE    = Color.web("#788575");
    public static final Color CHARCOAL      = Color.web("#302F2C");
    public static final Color INK           = CHARCOAL;
    public static final Color MUTED         = MUTED_SAGE;
    public static final Color SUCCESS_GREEN = Color.web("#2E7D32");
    public static final Color DANGER_RED    = Color.web("#A33B3B");

    /** Shared widget colors. Names kept so existing screens keep compiling. */
    public static final Color CREAM_VANILLA = WARM_IVORY;
    public static final Color CHERRY_COLA   = MIDNIGHT_NAVY;
    public static final Color VANILLA_BEIGE = SOFT_SAND;
    public static final Color WARM_COCOA    = ANTIQUE_GOLD;
    public static final Color DARK_TEXT     = INK;
    public static final Color MUTED_TEXT    = MUTED;

    public static final String HEX_CREAM_VANILLA = "#F7F3EA";
    public static final String HEX_CHERRY_COLA   = "#192B42";
    public static final String HEX_VANILLA_BEIGE = "#E8DFD0";
    public static final String HEX_WARM_COCOA    = "#B8955A";
    public static final String HEX_DARK_TEXT     = "#302F2C";
    public static final String HEX_MUTED_TEXT    = "#788575";
    public static final String HEX_SUCCESS       = "#2E7D32";
    public static final String HEX_DANGER        = "#A33B3B";
    public static final String HEX_IVORY_CARD    = "#FCFAF6";
    public static final String HEX_GOLD          = "#B8955A";

    public static final Font FONT_HERO        = Font.font("Segoe UI", FontWeight.BOLD, 30);
    public static final Font FONT_TITLE       = Font.font("Segoe UI", FontWeight.BOLD, 22);
    public static final Font FONT_SECTION     = Font.font("Segoe UI", FontWeight.BOLD, 16);
    public static final Font FONT_CARD_TITLE  = Font.font("Segoe UI", FontWeight.BOLD, 16);
    public static final Font FONT_SUBTITLE    = Font.font("Segoe UI", FontWeight.NORMAL, 14);
    public static final Font FONT_BODY        = Font.font("Segoe UI", FontWeight.NORMAL, 13);
    public static final Font FONT_BODY_BOLD   = Font.font("Segoe UI", FontWeight.BOLD, 13);
    public static final Font FONT_SMALL       = Font.font("Segoe UI", FontWeight.NORMAL, 12);
    public static final Font FONT_BADGE       = Font.font("Segoe UI", FontWeight.BOLD, 11);

    public static Button createPrimaryButton(String text, Node graphic) {
        Button btn = new Button(text);
        if (graphic != null) btn.setGraphic(graphic);
        btn.getStyleClass().add("primary-button");
        return btn;
    }

    public static Button createSecondaryButton(String text, Node graphic) {
        Button btn = new Button(text);
        if (graphic != null) btn.setGraphic(graphic);
        btn.getStyleClass().add("secondary-button");
        return btn;
    }

    public static Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-section-title");
        return label;
    }

    public static Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    public static Label createCategoryBadge(String category, Node icon) {
        Label badge = new Label(category.toUpperCase());
        if (icon != null) badge.setGraphic(icon);
        badge.getStyleClass().add("badge-category");
        return badge;
    }

    public static Label createStatusBadge(String status) {
        boolean isOpen = "Open".equalsIgnoreCase(status);
        Label badge = new Label(isOpen ? "Open" : "Closed");
        badge.getStyleClass().add(isOpen ? "badge-open" : "badge-closed");
        return badge;
    }
}
