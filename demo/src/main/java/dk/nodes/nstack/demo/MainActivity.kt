package dk.nodes.nstack.demo

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import dk.nodes.nstack.kotlin.inflater.NStackBaseContext
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = findNavController(R.id.nav_host)

        bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when(destination.id) {
                R.id.splashFragment -> hideBottomNav()
                else -> showBottomNav()
            }
        }
    }

    private fun hideBottomNav() {
        bottomNavigationView.visibility = View.GONE
    }

    private fun showBottomNav() {
        bottomNavigationView.visibility = View.VISIBLE
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(NStackBaseContext(newBase))
    }
}
