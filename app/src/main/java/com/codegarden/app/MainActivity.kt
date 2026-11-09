package com.codegarden.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codegarden.app.notify.Reminders
import com.codegarden.app.ui.AppNavGraph
import com.codegarden.app.ui.theme.CodeGardenTheme
import com.codegarden.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private lateinit var vm: MainViewModel
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // 拿到通知权限后立刻把闹钟重新排上，无需重开 App
            if (granted) Reminders.scheduleAll(this, vm.state.value.settings)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        vm = ViewModelProvider(this)[MainViewModel::class.java]
        Reminders.createChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            CodeGardenTheme(dark = state.settings.darkMode) {
                AppNavGraph()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onAppResume()
    }
}
