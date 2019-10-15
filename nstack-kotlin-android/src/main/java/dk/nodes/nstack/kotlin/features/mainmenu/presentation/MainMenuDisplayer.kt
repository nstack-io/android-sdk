package dk.nodes.nstack.kotlin.features.mainmenu.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.managers.LiveEditManager

private enum class DisplayedFeature {
    NONE,
    MAIN_MENU,
    OTHER
}

/**
 * Presents the main menu only under certain conditions. Also tries to assure memory and state
 * safety when displaying the dialog
 */
internal class MainMenuDisplayer(private val liveEditManager: LiveEditManager) {

    private var currentlyDisplayedFeature: DisplayedFeature = DisplayedFeature.NONE
    private var mainMenuDialog: BottomSheetDialog? = null

    private val mainMenuClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.editTranslationsButton -> triggerLiveEdit()
        }
    }

    /**
     * Presents the main menu by default, but can also cancel subsequent actions caused by the
     * selection made in the dialog.
     */
    fun trigger(activity: Activity) = when (currentlyDisplayedFeature) {
        DisplayedFeature.NONE -> showMainMenu(activity)
        DisplayedFeature.MAIN_MENU -> Unit // Do nothing, keep the menu open
        DisplayedFeature.OTHER -> triggerLiveEdit()
    }

    @SuppressLint("InflateParams")
    private fun showMainMenu(activity: Activity) {
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.bottomsheet_main_menu, null, false)
        val editTranslationsButton: View = view.findViewById(R.id.editTranslationsButton)

        this.mainMenuDialog = BottomSheetDialog(activity, R.style.NstackBottomSheetTheme)
                .apply {
                    setContentView(view)
                    setOnCancelListener {
                        with (this@MainMenuDisplayer) {
                            mainMenuDialog = null
                            currentlyDisplayedFeature = DisplayedFeature.NONE
                        }
                    }
                    setCancelOnStop(activity)
                }
                .also { dialog ->
                    editTranslationsButton.setOnClickListener { view ->
                        mainMenuClickListener.onClick(view)
                        dialog.dismiss()
                    }

                    dialog.show()
                    this.currentlyDisplayedFeature = DisplayedFeature.MAIN_MENU
                }
    }

    private fun triggerLiveEdit() {
        val isEditingStillEnabled = liveEditManager.toggleLiveEdit()

        this.currentlyDisplayedFeature = if (isEditingStillEnabled) {
            DisplayedFeature.OTHER
        } else {
            DisplayedFeature.NONE
        }
    }
}

private fun BottomSheetDialog.setCancelOnStop(activity: Activity) {
    activity.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {

        override fun onActivityStopped(activity: Activity) {
            cancel()

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