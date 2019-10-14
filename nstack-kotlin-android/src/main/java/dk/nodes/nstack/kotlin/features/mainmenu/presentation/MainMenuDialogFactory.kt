package dk.nodes.nstack.kotlin.features.mainmenu.presentation

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import dk.nodes.nstack.R

object MainMenuDialogFactory {

    @SuppressLint("InflateParams")
    fun create(context: Context): BottomSheetDialog {
        return BottomSheetDialog(context, R.style.NstackBottomSheetTheme)
                .apply {
                    setContentView(R.layout.bottomsheet_main_menu)
                }
    }

}