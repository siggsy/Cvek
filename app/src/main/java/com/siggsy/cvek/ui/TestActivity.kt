package com.siggsy.cvek.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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


        val viewPager = findViewById<ViewPager2>(R.id.test_schedule)
        val date = LocalDate.MIN
        setupScheduleView(viewPager, LocalDate.now()) { adapter, localDate ->

        }
    }
}