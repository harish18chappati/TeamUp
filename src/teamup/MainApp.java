package teamup;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import teamup.db.DatabaseManager;
import teamup.ui.MainWindow;

/**
 * MainApp – JavaFX Application Lifecycle Handler.
 * Initializes SQLite database on startup, configures the Scene with CSS styling, and shuts down cleanly.
 */
public class MainApp extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        // ── 1. Initialize SQLite Database & Tables ────────────────────────────
        System.out.println("[App] Initializing SQLite database...");
        DatabaseManager.initialise();
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println("[App] Starting JavaFX Application UI...");

        // Create main application window shell
        MainWindow mainWindow = new MainWindow();

        // Scene with default dimensions (1180 x 740)
        Scene scene = new Scene(mainWindow, 1180, 740);

        // Attach design system stylesheet
        String cssPath = getClass().getResource("/teamup/ui/styles.css") != null
                ? getClass().getResource("/teamup/ui/styles.css").toExternalForm()
                : getClass().getResource("ui/styles.css") != null
                ? getClass().getResource("ui/styles.css").toExternalForm()
                : null;

        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        } else {
            // Fallback to relative or classloader path
            try {
                scene.getStylesheets().add(MainApp.class.getResource("/teamup/ui/styles.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("[App] Warning: styles.css could not be loaded via getResource: " + e.getMessage());
            }
        }

        primaryStage.setTitle("TeamUp – Campus Activity and Teammate Finder | SRM University-AP");
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("[App] TeamUp UI launched successfully.");
    }

    @Override
    public void stop() throws Exception {
        // ── Clean Database Shutdown ──────────────────────────────────────────
        System.out.println("[App] Closing database connection on application exit...");
        DatabaseManager.closeConnection();
        super.stop();
    }
}
