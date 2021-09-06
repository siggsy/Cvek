package com.siggsy.cvek.ui.schedule

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.siggsy.cvek.utils.getCurrentYear
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

private val MIN_DATE = LocalDate.of(Calendar.getInstance().getCurrentYear(), 8, 1)
private val MAX_DATE = LocalDate.of(Calendar.getInstance().getCurrentYear() + 1, 8, 1)
private val BETWEEN = ChronoUnit.MONTHS.between(MAX_DATE, MIN_DATE)

@ExperimentalPagerApi
@Preview
@Composable
fun Schedule(initialDate: LocalDate = LocalDate.now()) {
    val pagerState = rememberPagerState(
        pageCount = BETWEEN.toInt(),
        initialPage = ChronoUnit.MONTHS.between(initialDate, MIN_DATE).toInt()
    )
    HorizontalPager(state = pagerState) { page ->
        val currentMonth = MIN_DATE.plusMonths(page.toLong())
        Text("$currentMonth")
    }
}