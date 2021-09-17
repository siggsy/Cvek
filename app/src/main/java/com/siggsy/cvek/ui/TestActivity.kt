package com.siggsy.cvek.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.test.ui.theme.CvekTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.siggsy.cvek.CvekApp
import com.siggsy.cvek.data.easistent.Week
import com.siggsy.cvek.data.easistent.getTimeTable
import com.siggsy.cvek.data.easistent.getYearTimeTable
import com.siggsy.cvek.data.easistent.login
import com.siggsy.cvek.ui.schedule.*
import com.siggsy.cvek.utils.getCurrentYear
import com.siggsy.cvek.utils.network.default
import com.siggsy.cvek.utils.network.logger
import com.siggsy.cvek.utils.preferences.AuthPreferences
import com.siggsy.cvek.utils.preferences.User
import com.siggsy.cvek.utils.replace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.min

class TestActivity : ComponentActivity() {
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    @ExperimentalPagerApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val api = (application as CvekApp).httpClient
        setContent {
            CvekTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        val minDate = LocalDate.of(java.util.Calendar.getInstance().getCurrentYear(), 8, 1)
                        val maxDate = LocalDate.of(java.util.Calendar.getInstance().getCurrentYear() + 1, 8, 1)
                        var selected by remember { mutableStateOf(LocalDate.now()) }
                        var selectedSchedule by remember { mutableStateOf(LocalDate.now()) }
                        var schedules by remember { mutableStateOf<Week?>(null) }
                        Calendar(
                            minDate = minDate,
                            maxDate = maxDate,
                            selected = selected,
                            events = schedules.toEvents(),
                            onSelected = { selectedSchedule = it }
                        )
                        lifecycleScope.launch(Dispatchers.Main) {
                            schedules = api.getYearTimeTable().body()
                        }
                    }
                }
            }
        }
    }
}