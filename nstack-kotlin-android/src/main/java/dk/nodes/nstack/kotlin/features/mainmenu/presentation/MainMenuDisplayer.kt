package dk.nodes.nstack.kotlin.features.mainmenu.presentation

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialog

private enum class DisplayedFeature {
    NONE,
    MAIN_MENU,
    TRANSLATION_EDIT,
    TRANSLATION_MENUS
}

/**
 * Presents the main menu only under certain conditions. Also tries to assure memory and state
 * safety when displaying the dialog
 */
internal class MainMenuDisplayer {

    private var currentlyDisplayedFeature: DisplayedFeature = DisplayedFeature.NONE
    private var mainMenuDialog: BottomSheetDialog? = null

    init {
        this.currentlyDisplayedFeature.value = DisplayedFeature.NONE
    }

    fun display(activity: Activity) = when(currentlyDisplayedFeature) {
        DisplayedFeature.NONE -> displayFeature(DisplayedFeature.MAIN_MENU) { showMainMenu(activity) }
        DisplayedFeature.TRANSLATION_EDIT -> displayFeature(DisplayedFeature.NONE) { disableEditing(activity) }
        DisplayedFeature.MAIN_MENU -> { /* Do nothing, keep the menu open */ }
        DisplayedFeature.TRANSLATION_MENUS -> { /* Do nothing, keep the menu open */ }
    }

    private inline fun displayFeature(newFeature: DisplayedFeature, display: () -> Unit) {
        this.currentlyDisplayedFeature = newFeature
        display()
    }

    private fun showMainMenu(activity: Activity) {
        this.mainMenuDialog = MainMenuDialogFactory
                .create(context = activity)
                .apply {
                    setOnCancelListener { this@MainMenuDisplayer.mainMenuDialog = null }
                    setOnDismissListener { this@MainMenuDisplayer.mainMenuDialog = null }
                }
                .also { dialog ->
                    setDismissWithActivity(dialog, activity)
                    dialog.show()
                }
    }

    private fun disableEditing(activity: Activity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun setDismissWithActivity(dialog: BottomSheetDialog, activity: Activity) {
        activity.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {

            override fun onActivityStopped(activity: Activity) {
                dialog.dismiss()

                activity.unregisterActivityLifecycleCallbacks(this)
            }

            // Unused

            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
        })
    }
}