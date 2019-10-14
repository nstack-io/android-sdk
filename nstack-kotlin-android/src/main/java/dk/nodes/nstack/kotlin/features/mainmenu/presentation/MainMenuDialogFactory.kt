package dk.nodes.nstack.kotlin.features.mainmenu.presentation

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.util.extensions.setNavigationBarColor

object MainMenuDialogFactory {
    fun createDialog(context: Context): BottomSheetDialog {
        return BottomSheetDialog(context, R.style.NstackBottomSheetTheme).apply {
            setNavigationBarColor()
            setContentView(R.layout.bottomsheet_main_menu)
        }
    }
}