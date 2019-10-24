package dk.nodes.nstack.demo.splash

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dk.nodes.nstack.demo.R
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.Result
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashFragment : Fragment(R.layout.fragment_splash) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                when (val result = NStack.appOpen()) {
                    is Result.Success ->  Log.d("AppOpenResult: Success", result.toString())
                    is Result.Error -> Log.d("AppOpenResult: Error", result.toString())
                }
                withContext(Dispatchers.Main) {
                    findNavController(view).navigate(R.id.mainFragment)
                }
            }
        }
    }
}