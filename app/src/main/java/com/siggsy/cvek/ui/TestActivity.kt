package com.siggsy.cvek.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import com.example.test.ui.theme.CvekTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.siggsy.cvek.ui.schedule.Schedule

class TestActivity : ComponentActivity() {
    @ExperimentalPagerApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CvekTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Schedule()
                }
            }
        }
    }
}