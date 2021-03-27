package com.siggsy.cvek.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.siggsy.cvek.R
import com.siggsy.cvek.data.easistent.EasistentApi
import com.siggsy.cvek.ui.schedule.setupScheduleView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val viewPager: ViewPager = findViewById(R.id.test_schedule)
        setupScheduleView(viewPager, LocalDate.now()) { adapter, localDate ->
            GlobalScope.launch(Dispatchers.IO) {
                // Get easistent api
                val eaApi: EasistentApi = TODO()
                val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                val week = eaApi.getTimeTable(date, date)
                Log.i("test", week.toString())
                this@TestActivity.runOnUiThread {
                    adapter.setEvents(week)
                }
            }
        }
    }
}