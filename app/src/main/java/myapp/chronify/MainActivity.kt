/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package myapp.chronify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import myapp.chronify.data.PreferencesKey
import myapp.chronify.ui.element.AppBottomBar
import myapp.chronify.ui.navigation.AppNavHost
import myapp.chronify.ui.theme.bluesimple.BlueSimpleTheme

class MainActivity : ComponentActivity() {
    private val preferencesRepository by lazy {
        (application as DataContainerApplication).preferencesRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 可以在这里处理首次安装的初始化逻辑
        // lifecycleScope.launch {
        //     handleFirstLaunch()
        // }

        setContent {
            BlueSimpleTheme {
                val navController = rememberNavController()
                val currentRoute =
                    navController.currentBackStackEntryAsState().value?.destination?.route

                Scaffold(
                    bottomBar = {
                        AppBottomBar(
                            currentRoute = currentRoute,
                            onNavigateToRoute = { route ->
                                navController.navigate(route) {
                                    // Pop up to the start destination of the graph to avoid building up repeat destinations on the back stack as users select items
                                    // 避免创建重复的后退栈
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            })
                    },
                ) { innerPadding ->
                    // 使用Surface避免导航时的屏幕闪烁
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        AppNavHost(navController = navController)
                    }
                }
            }
        }
    }

    // private suspend fun handleFirstLaunch() {
    //     val isFirstLaunch = preferencesRepository.getPreference(
    //         PreferencesKey.AppSettings.IsFirstLaunch
    //     ).first()
    //
    //     if (isFirstLaunch) {
    //         // 首次启动的特殊初始化
    //         preferencesRepository.updatePreference(
    //             PreferencesKey.AppSettings.IsFirstLaunch,
    //             false
    //         )
    //         // 可以在这里执行其他首次启动的初始化操作
    //     }
    // }

}

