package dk.nodes.nstack.kotlin.features.mainmenu.presentation

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Presents the main menu only under certain conditions. Also tries to assure memory and state
 * safety when displaying the menu
 */
internal class MainMenuDisplayer {

    private var mainMenuDialog: BottomSheetDialog? = null

    fun display(activity: Activity) {

        if (mainMenuDialog != null || activity.isFinishing) {
            return
        }

        this.mainMenuDialog = MainMenuDialogFactory
                .create(context = activity)
                .apply {
                    setOnCancelListener { this@MainMenuDisplayer.mainMenuDialog = null  }
                    setOnDismissListener { this@MainMenuDisplayer.mainMenuDialog = null }
                }
                .also { dialog ->
                    setDismissWithActivity(dialog, activity)
                    dialog.show()
                }
    }

    private fun setDismissWithActivity(dialog: BottomSheetDialog, activity: Activity) {
        activity.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks{

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