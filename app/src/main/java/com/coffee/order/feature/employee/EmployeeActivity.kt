package com.coffee.order.feature.employee

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.coffee.order.R
import com.coffee.order.base.GlobalComposeHandler
import com.coffee.order.feature.auth.LoginActivity
import com.coffee.order.feature.employee.component.ConfirmLogoutBottomSheet
import com.coffee.order.feature.employee.fragment.order.OrderFragment
import com.coffee.order.network.TokenManager
import com.coffee.order.theme.CoffeeAdminTheme
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.collectAsState

class EmployeeActivity : AppCompatActivity() {
    private var navController = MutableStateFlow<NavController?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        TokenManager.init(applicationContext)

        if (!TokenManager.isLoggedIn() || TokenManager.isOwner()) {
            navigateToLogin()
            return
        }

        setContent {
            CoffeeAdminTheme {

                Box(modifier = Modifier.fillMaxSize()) {

                    Column(modifier = Modifier.fillMaxSize()) {

                        AndroidView(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            factory = { context ->
                                FragmentContainerView(context).apply {
                                    id = R.id.nav_host_fragment_fixed

                                    post {
                                        if (supportFragmentManager.findFragmentById(id) == null) {

                                            val navHostFragment =
                                                NavHostFragment.create(R.navigation.nav_graph)

                                            supportFragmentManager.beginTransaction()
                                                .replace(id, navHostFragment)
                                                .setPrimaryNavigationFragment(navHostFragment)
                                                .commitNow()

                                            navController.value = navHostFragment.navController
                                        }
                                    }
                                }
                            },
                        )
                        val controller by navController.collectAsState()
                        controller?.let {
                            EmployeeBottomNav(navController = it)
                        }
                    }

                    GlobalComposeHandler.GlobalComposeContent()
                }
            }
        }

    }

    @Composable
    fun EmployeeBottomNav(navController: NavController) {
        var currentDestinationId by remember { mutableIntStateOf(R.id.managementFragment) }
        var isVisible by remember { mutableStateOf(true) }

        val topLevelDestinations = setOf(
            R.id.managementFragment,
            R.id.historyFragment,
            R.id.settingFragment
        )

        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
                currentDestinationId = destination.id
                isVisible = destination.id in topLevelDestinations
            }
            navController.addOnDestinationChangedListener(listener)
            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }

        if (isVisible) {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(R.drawable.management),
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(R.string.tab_management)) },
                    selected = currentDestinationId == R.id.managementFragment,
                    onClick = {
                        if (currentDestinationId != R.id.managementFragment) {
                            navController.navigate(R.id.managementFragment)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(R.drawable.history), contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_history)) },
                    selected = currentDestinationId == R.id.historyFragment,
                    onClick = {
                        if (currentDestinationId != R.id.historyFragment) {
                            navController.navigate(R.id.historyFragment)
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(R.drawable.setting), contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_setting)) },
                    selected = currentDestinationId == R.id.settingFragment,
                    onClick = {
                        if (currentDestinationId != R.id.settingFragment) {
                            navController.navigate(R.id.settingFragment)
                        }
                    }
                )
            }
        }
    }

    fun navigateToCreateOrder(tableId: Long) {
        navController.value?.navigate(
            resId = R.id.orderFragment,
            args = OrderFragment.createBundle(tableId = tableId)
        )
    }

    fun logout() {
        GlobalComposeHandler.showGlobalBottomSheet {
            ConfirmLogoutBottomSheet(
                onConfirm = {
                    GlobalComposeHandler.hideGlobalBottomSheet()
                    TokenManager.clearSession()
                    navigateToLogin()
                },
                onCancel = {
                    GlobalComposeHandler.hideGlobalBottomSheet()
                }
            )
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}