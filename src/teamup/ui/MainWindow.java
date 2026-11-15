package teamup.ui;

import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import teamup.db.DatabaseManager;
import teamup.model.Activity;
import teamup.model.Student;
import teamup.model.StudentSession;
import teamup.ui.components.AppNavBar;
import teamup.ui.pages.*;

/**
 * MainWindow – Main application shell in JavaFX.
 *
 * Structure:
 *   MainWindow (BorderPane)
 *     ├── AppNavBar (top, single shared horizontal navigation bar, hidden during Welcome)
 *     └── Main Content (center, vertically scrollable page container)
 *
 * Strictly NO left navigation panel. Full available window width is utilized.
 */
public class MainWindow extends BorderPane implements PageNavigator {

    private final AppNavBar navBar;
    private final StackPane centerContainer;

    // Cached page views
    private final WelcomePage welcomePage;
    private final HomePage homePage;
    private final ExplorePage explorePage;
    private final CreateActivityPage createActivityPage;
    private final MyActivitiesPage myActivitiesPage;

    // Active session state
    private String currentStudentReg;
    private String currentStudentName;

    public MainWindow() {
        getStyleClass().add("root");

        // 1. Initialize active student identity from session or defaults
        currentStudentReg = StudentSession.getActiveStudentReg();
        currentStudentName = StudentSession.getActiveStudentName();

        // 2. Single global top navigation bar across all pages
        navBar = new AppNavBar(this, "HOME");
        setTop(navBar);

        // 3. Explicitly ensure NO left sidebar or panel exists
        setLeft(null);

        // 4. Center Container occupying full width
        centerContainer = new StackPane();
        centerContainer.getStyleClass().add("root");
        setCenter(centerContainer);

        // 5. Instantiate Pages
        welcomePage = new WelcomePage(this);
        homePage = new HomePage(this);
        explorePage = new ExplorePage(this);
        createActivityPage = new CreateActivityPage(this);
        myActivitiesPage = new MyActivitiesPage(this);

        // Start on Student Welcome and Identification Screen
        showWelcome();
    }

    public AppNavBar getNavBar() {
        return navBar;
    }

    private void ensureNavBarVisible() {
        navBar.setVisible(true);
        navBar.setManaged(true);
    }

    @Override
    public void showWelcome() {
        // Hide global navigation bar during student welcome & identification
        navBar.setVisible(false);
        navBar.setManaged(false);
        centerContainer.getChildren().setAll(welcomePage);
        welcomePage.prefill(currentStudentName, currentStudentReg);
        welcomePage.playEntranceAnimation();
    }

    @Override
    public void showHome() {
        ensureNavBarVisible();
        navBar.setActiveNav("HOME");
        centerContainer.getChildren().setAll(homePage);
        homePage.refreshSnapshot();
    }

    @Override
    public void showExplore(String categoryFilter) {
        ensureNavBarVisible();
        navBar.setActiveNav("EXPLORE");
        centerContainer.getChildren().setAll(explorePage);
        if (categoryFilter != null && !categoryFilter.isBlank()) {
            explorePage.selectCategory(categoryFilter);
        } else {
            explorePage.applyFilterAndSearch();
        }
    }

    @Override
    public void showCreateActivity(String preselectedCategory) {
        ensureNavBarVisible();
        navBar.setActiveNav("CREATE");
        centerContainer.getChildren().setAll(createActivityPage);
        createActivityPage.setCreateMode(preselectedCategory);
    }

    @Override
    public void showEditActivity(Activity activity, String returnPage) {
        if (activity == null) return;
        String currentReg = getCurrentStudentReg();
        if (activity.getCreatedBy() == null || !activity.getCreatedBy().equalsIgnoreCase(currentReg)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Permission Denied");
            alert.setHeaderText("Unauthorized Action");
            alert.setContentText("You can only edit activities that you created.");
            alert.showAndWait();
            return;
        }

        ensureNavBarVisible();
        navBar.setActiveNav("CREATE");
        centerContainer.getChildren().setAll(createActivityPage);
        createActivityPage.loadActivityForEdit(activity, returnPage);
    }

    @Override
    public void refreshAllPages() {
        if (homePage != null) {
            homePage.refreshSnapshot();
        }
        if (explorePage != null) {
            explorePage.applyFilterAndSearch();
        }
        if (myActivitiesPage != null) {
            myActivitiesPage.refreshActivities();
        }
    }

    @Override
    public void showMyActivities() {
        ensureNavBarVisible();
        navBar.setActiveNav("MY_ACTIVITIES");
        centerContainer.getChildren().setAll(myActivitiesPage);
        myActivitiesPage.refreshActivities();
    }

    @Override
    public void showActivityDetails(Activity activity, String returnPage) {
        ensureNavBarVisible();
        navBar.setActiveNav("DETAILS");
        ActivityDetailsPage detailsPage = new ActivityDetailsPage(activity, returnPage, this);
        centerContainer.getChildren().setAll(detailsPage);
    }

    @Override
    public String getCurrentStudentReg() {
        return currentStudentReg;
    }

    @Override
    public String getCurrentStudentName() {
        return currentStudentName;
    }

    @Override
    public void setCurrentStudent(String name, String regNumber) {
        if (regNumber != null && !regNumber.isBlank()) {
            this.currentStudentReg = regNumber.trim().toUpperCase();
        }
        if (name != null && !name.isBlank()) {
            this.currentStudentName = name.trim();
        }
        StudentSession.setSession(this.currentStudentName, this.currentStudentReg);
        navBar.updateStudentInfo(this.currentStudentName, this.currentStudentReg);
    }

    @Override
    public void setCurrentStudentReg(String regNumber) {
        if (regNumber != null && !regNumber.isBlank()) {
            this.currentStudentReg = regNumber.trim().toUpperCase();
            // Look up existing name if known in database
            Student dbStudent = DatabaseManager.getStudentByReg(this.currentStudentReg);
            if (dbStudent != null && dbStudent.getName() != null && !dbStudent.getName().isBlank()) {
                this.currentStudentName = dbStudent.getName();
            }
            StudentSession.setSession(this.currentStudentName, this.currentStudentReg);
            navBar.updateStudentInfo(this.currentStudentName, this.currentStudentReg);
        }
    }
}
