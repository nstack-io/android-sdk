package dk.nodes.nstack.kotlin.features.mainmenu.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.managers.LiveEditManager
import dk.nodes.nstack.kotlin.util.extensions.setNavigationBarColor

private enum class DisplayedFeature {
    NONE,
    MAIN_MENU,
    LIVE_EDIT
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
            R.id.editTranslationsButton -> startLiveEdit()
        }
    }

    /**
     * Presents the main menu by default, but can also cancel subsequent actions caused by the
     * selection made in the dialog.
     */
    fun trigger(activity: Activity) {

        activity.setOnStopAction {
            mainMenuDialog?.cancel()
            liveEditManager.reset()
        }

        return when (currentlyDisplayedFeature) {
            DisplayedFeature.NONE -> showMainMenu(activity)
            DisplayedFeature.MAIN_MENU -> Unit // Do nothing, keep the menu open
            DisplayedFeature.LIVE_EDIT -> stopLiveEdit()
        }
    }

    @SuppressLint("InflateParams")
    private fun showMainMenu(activity: Activity) {
        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.bottomsheet_main_menu, null, false)
        val editTranslationsButton: View = view.findViewById(R.id.editTranslationsButton)

        this.mainMenuDialog = BottomSheetDialog(activity, R.style.NstackBottomSheetTheme)
            .apply {
                setNavigationBarColor()
                setContentView(view)
                setOnCancelListener {
                    with(this@MainMenuDisplayer) {
                        mainMenuDialog = null
                        currentlyDisplayedFeature = DisplayedFeature.NONE
                    }
                }
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

    private fun startLiveEdit() {
        liveEditManager.turnLiveEditOn()
        currentlyDisplayedFeature = DisplayedFeature.LIVE_EDIT
    }

    private fun stopLiveEdit() {
        liveEditManager.reset()
        currentlyDisplayedFeature = DisplayedFeature.NONE
    }
}

private fun Activity.setOnStopAction(action: (Activity) -> Unit) {
    val application = applicationContext as Application

    application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityStopped(activity: Activity) {

            val isMatchingActivityWithStopAction = activity === this@setOnStopAction

            if (isMatchingActivityWithStopAction) {
                action(this@setOnStopAction)

                application.unregisterActivityLifecycleCallbacks(this)
            }
        }

        // Unused

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
    )
}