package com.coffee.order

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.coffee.order.base.GlobalComposeHandler
import com.coffee.order.databinding.ActivityMainBinding
import com.coffee.order.fragment.order.OrderFragment

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        val topLevelDestinationIds = setOf(
            R.id.managementFragment,
            R.id.historyFragment,
            R.id.settingFragment,
        )

        navController.apply {
            addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id in topLevelDestinationIds) {
                    binding.bottomNav.visibility = android.view.View.VISIBLE
                } else {
                    binding.bottomNav.visibility = android.view.View.GONE
                }
            }
        }
        appBarConfiguration = AppBarConfiguration(topLevelDestinationIds)
        binding.bottomNav.setupWithNavController(navController)
        binding.globalCompose.setContent { GlobalComposeHandler.GlobalComposeContent() }
    }


    fun navigateToCreateOrder(tableId: Long) {
        navController.navigate(
            resId = R.id.orderFragment,
            args = OrderFragment.createBundle(tableId = tableId)
        )
    }

}