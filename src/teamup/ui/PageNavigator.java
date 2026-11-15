package teamup.ui;

import teamup.model.Activity;

/**
 * PageNavigator – Navigation contract between MainWindow and child JavaFX screens.
 */
public interface PageNavigator {

    /** Navigates to the Home page. */
    void showHome();

    /**
     * Navigates to Explore Activities.
     * @param categoryFilter category name to filter by (e.g. "Travel", "Sports"), or "All"/null for all.
     */
    void showExplore(String categoryFilter);

    /**
     * Navigates to Create Activity form.
     * @param preselectedCategory preselected category name or null for default.
     */
    void showCreateActivity(String preselectedCategory);

    /** Navigates to My Activities page. */
    void showMyActivities();

    /**
     * Navigates to the dedicated Activity Details page for a selected activity.
     * @param activity the Activity object to display
     * @param returnPage the page key to return to ("EXPLORE" or "MY_ACTIVITIES")
     */
    void showActivityDetails(Activity activity, String returnPage);

    /**
     * Navigates to the Edit Activity form prepopulated with existing activity details.
     * @param activity the Activity object to edit
     * @param returnPage the page key to return to ("MY_ACTIVITIES", "EXPLORE", or "DETAILS")
     */
    void showEditActivity(Activity activity, String returnPage);

    /** Refreshes cached data across Home, Explore, and My Activities pages. */
    void refreshAllPages();

    /** Navigates to the Student Welcome and Identification screen. */
    void showWelcome();

    /** Gets the currently active student registration number in the app session. */
    String getCurrentStudentReg();

    /** Gets the currently active student full name in the app session. */
    String getCurrentStudentName();

    /** Sets the current student registration number for the session. */
    void setCurrentStudentReg(String regNumber);

    /** Sets the full student identity (both name and registration number) for the session. */
    void setCurrentStudent(String name, String regNumber);
}
