package teamup.ui.components;

import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * AppIcons – Factory for creating clean, consistent vector Ikonli icons in JavaFX.
 *
 * Uses Feather icon pack for a unified, modern, professional stroke appearance.
 * Absolutely no emojis or broken Unicode characters are used.
 */
public class AppIcons {

    public static FontIcon icon(Ikon ikon, int size, Color color) {
        FontIcon fontIcon = FontIcon.of(ikon, size);
        if (color != null) {
            fontIcon.setIconColor(color);
        }
        return fontIcon;
    }

    // ── Navigation Icons ──────────────────────────────────────────────────────
    public static FontIcon home(int size, Color color) {
        return icon(Feather.HOME, size, color);
    }

    public static FontIcon explore(int size, Color color) {
        return icon(Feather.COMPASS, size, color);
    }

    public static FontIcon create(int size, Color color) {
        return icon(Feather.PLUS_CIRCLE, size, color);
    }

    public static FontIcon myActivities(int size, Color color) {
        return icon(Feather.USER, size, color);
    }

    public static FontIcon menu(int size, Color color) {
        return icon(Feather.MENU, size, color);
    }

    // ── Meta & Action Icons ───────────────────────────────────────────────────
    public static FontIcon calendar(int size, Color color) {
        return icon(Feather.CALENDAR, size, color);
    }

    public static FontIcon location(int size, Color color) {
        return icon(Feather.MAP_PIN, size, color);
    }

    public static FontIcon members(int size, Color color) {
        return icon(Feather.USERS, size, color);
    }

    public static FontIcon user(int size, Color color) {
        return icon(Feather.USER, size, color);
    }

    public static FontIcon back(int size, Color color) {
        return icon(Feather.ARROW_LEFT, size, color);
    }

    public static FontIcon search(int size, Color color) {
        return icon(Feather.SEARCH, size, color);
    }

    public static FontIcon check(int size, Color color) {
        return icon(Feather.CHECK_CIRCLE, size, color);
    }

    public static FontIcon alert(int size, Color color) {
        return icon(Feather.ALERT_CIRCLE, size, color);
    }

    public static FontIcon edit(int size, Color color) {
        return icon(Feather.EDIT_2, size, color);
    }

    public static FontIcon delete(int size, Color color) {
        return icon(Feather.TRASH_2, size, color);
    }

    public static FontIcon settings(int size, Color color) {
        return icon(Feather.SETTINGS, size, color);
    }

    public static FontIcon mail(int size, Color color) {
        return icon(Feather.MAIL, size, color);
    }

    public static FontIcon phone(int size, Color color) {
        return icon(Feather.PHONE, size, color);
    }

    public static FontIcon graduationCap(int size, Color color) {
        return icon(Feather.AWARD, size, color);
    }

    // ── Category Icons ────────────────────────────────────────────────────────
    public static FontIcon categoryIcon(String category, int size, Color color) {
        if (category == null) return icon(Feather.TAG, size, color);
        String cat = category.trim().toLowerCase();
        if (cat.startsWith("travel")) return icon(Feather.MAP, size, color);
        if (cat.startsWith("event")) return icon(Feather.AWARD, size, color);
        if (cat.startsWith("sport")) return icon(Feather.ACTIVITY, size, color);
        if (cat.startsWith("hackathon")) return icon(Feather.TERMINAL, size, color);
        if (cat.startsWith("study")) return icon(Feather.BOOK_OPEN, size, color);
        if (cat.startsWith("entertainment")) return icon(Feather.FILM, size, color);
        return icon(Feather.TAG, size, color);
    }
}
