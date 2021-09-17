package com.siggsy.cvek.ui.schedule

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.siggsy.cvek.data.easistent.Week
import com.siggsy.cvek.utils.getCurrentYear
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

enum class Event(val color: Color) {
    TODAY(Color.Black),
    EXAM(Color.Red)
}

fun Color.addAlpha(alpha: Float): Color = Color(red, green, blue, alpha = alpha)

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalPagerApi
@Preview
@Composable
fun Calendar(
    minDate: LocalDate = LocalDate.of(Calendar.getInstance().getCurrentYear(), 8, 1),
    maxDate: LocalDate = LocalDate.of(Calendar.getInstance().getCurrentYear() + 1, 8, 1),
    selected: LocalDate = LocalDate.now(),
    events: Map<LocalDate, Event> = emptyMap(),
    onSelected: (LocalDate) -> Unit = { }
) {

    val monthPager = rememberPagerState(
        pageCount = ChronoUnit.MONTHS.between(minDate, maxDate).toInt(),
        initialPage = ChronoUnit.MONTHS.between(minDate, selected).toInt()
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(selected) }

    val rotateState = animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
    )

    val scope = rememberCoroutineScope()
    val onDateSelected: (LocalDate) -> Unit = {
        if (selectedDate.month != it.month) {
            scope.launch {
                monthPager.animateScrollToPage(ChronoUnit.MONTHS.between(minDate, it).toInt())
                selectedDate = it
            }
        } else {
            selectedDate = it
        }
        onSelected(it)
    }

    val currentMonth = minDate.plusMonths(monthPager.currentPage.toLong()).month.getDisplayName(TextStyle.FULL, Locale("sl"))

    Column {
        Card(elevation = 12.dp) {
            Column(horizontalAlignment = CenterHorizontally, ) {
                Card(onClick = { expanded = !expanded }, elevation = 0.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(6f)
                        ) {
                            Text(
                                text = currentMonth,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.size(8.dp))
                            Icon(
                                Icons.Default.ArrowDropDown, "",
                                modifier = Modifier
                                    .rotate(rotateState.value)
                                    .size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDateSelected(LocalDate.now()) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.DateRange, "",
                                Modifier
                                    .size(24.dp)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                            )
                        }

                    }
                }
                Spacer(modifier = Modifier.size(18.dp))
                WeekRow()
                Spacer(modifier = Modifier.size(8.dp))
                selectedDate = minDate.plusMonths(monthPager.currentPage.toLong()).let {
                    val yearMonth = YearMonth.of(it.year, it.month)
                    val day = if (yearMonth.lengthOfMonth() < selectedDate.dayOfMonth) {
                        yearMonth.lengthOfMonth()
                    } else {
                        selectedDate.dayOfMonth
                    }
                    LocalDate.of(it.year, it.month, day)
                }

                AnimatedVisibility(visible = expanded) {
                    HorizontalPager(
                        state = monthPager,
                    ) { page ->
                        DaysInMonth(selectedDate, minDate.plusMonths(page.toLong()), events, onClick = onDateSelected)
                    }
                }
                AnimatedVisibility(visible = !expanded) {
                    Preview(selectedDate, events, onClick = onDateSelected)
                }

                Spacer(Modifier.size(8.dp))
            }
        }
        var selected by remember { mutableStateOf(selectedDate) }
        val pagerState = PagerState(
            currentPage = ChronoUnit.DAYS.between(minDate, selectedDate).toInt(),
            pageCount = ChronoUnit.DAYS.between(minDate, maxDate).toInt()
        )

        //LaunchedEffect(selectedDate) {
        //    pagerState.animateScrollToPage(ChronoUnit.MONTHS.between(minDate, selectedDate).toInt())
        //    selected = selectedDate
        //}

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val listState = LazyListState()
            val daySchedule = schedules[page]
            Log.i("test", "$daySchedule")

            when (daySchedule.state) {
                EventState.LOADING -> LoadingIndicator()
                EventState.SET -> DayScheduleList(listState, daySchedule.events!!)
                EventState.UNSET -> { onDayNeeded(minDate.plusDays(page.toLong())); LoadingIndicator() }
            }
        }
    }
}

enum class EventState {
    LOADING, UNSET, SET
}
data class DaySchedule(
    val state: EventState = EventState.UNSET,
    val events: List<Week.SchoolHourEvent>? = null
)

@Composable
fun LoadingIndicator() {
    Text("Loading", modifier = Modifier.fillMaxWidth())
}

@Composable
fun DayScheduleList(listState: LazyListState, events: List<Week.SchoolHourEvent>) {
    LazyColumn(state = listState) {
        items(events.count()) { eventIndex ->
            val event = events[eventIndex]
            Event(
                LocalTime.of(10, 10),
                LocalTime.of(10, 30),
                event.subject.name,
                event.teachers.firstOrNull()?.name ?: "",
                event.classroom?.name ?: "",
                emptySet()
            )
        }
    }
}

@Composable
fun WeekRow() {
    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
        (1..7).forEach {
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    text = DayOfWeek.of(it).getDisplayName(TextStyle.NARROW, Locale("sl")).uppercase(),
                    modifier = Modifier.align(Center)
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun DaysInMonth(
    selected: LocalDate,
    currentMonth: LocalDate,
    events: Map<LocalDate, Event>,
    onClick: (LocalDate) -> Unit
) {
    val monthOffset = currentMonth.dayOfWeek.value - DayOfWeek.MONDAY.value
    val daysInMonthPrev = YearMonth.of(currentMonth.year, currentMonth.month - 1).lengthOfMonth()
    val daysInMonth = YearMonth.of(currentMonth.year, currentMonth.month).lengthOfMonth()
    LazyVerticalGrid(
        cells = GridCells.Fixed(7),
    ) {
        items(42) { dayIndex ->
            val (month, day, currMonth) = (dayIndex - monthOffset + 1).let {
                when {
                    it < 1 -> {
                        Triple(currentMonth.month - 1, it + daysInMonthPrev, false)
                    }
                    it in 1..daysInMonth -> {
                        Triple(currentMonth.month, it, true)
                    }
                    else -> {
                        Triple(currentMonth.month + 1, it - daysInMonth, false)
                    }
                }
            }

            val date = LocalDate.of(currentMonth.year, month, day)
            Day(
                day,
                event = events[date],
                selected = date == selected,
                currMonth = currMonth,
                onClick = { onClick(date) }
            )

        }
    }
}

@ExperimentalFoundationApi
@Composable
fun Preview(selected: LocalDate, events: Map<LocalDate, Event>, onClick: (LocalDate) -> Unit) {
    val offset = selected.dayOfWeek.value - DayOfWeek.MONDAY.value
    val daysInMonth = YearMonth.of(selected.year, selected.month).lengthOfMonth()
    val daysInMonthPrev = YearMonth.of(selected.year, selected.month - 1).lengthOfMonth()
    LazyVerticalGrid(
        cells = GridCells.Fixed(7),
    ) {
        items(7) { dayIndex ->
            val (month, day, currMonth) = (dayIndex - offset + selected.dayOfMonth).let {
                when {
                    it < 1 -> {
                        Triple(selected.month - 1, it + daysInMonthPrev, false)
                    }
                    it in 1..daysInMonth -> {
                        Triple(selected.month, it, true)
                    }
                    else -> {
                        Triple(selected.month + 1, it - daysInMonth, false)
                    }
                }
            }

            val date = LocalDate.of(selected.year, month, day)
            Day(
                day,
                event = events[date],
                selected = date == selected,
                currMonth = currMonth,
                onClick = { onClick(date) }
            )
        }
    }
}

@Composable
fun Day(
    day: Int,
    selected: Boolean = false,
    currMonth: Boolean = true,
    event: Event? = null,
    onClick: () -> Unit
) {

    val textColor = animateColorAsState(
        targetValue = if (selected) {
            Color.White
        } else {
            event?.color ?: Color.Black
        },
    )

    val backgroundColor = animateColorAsState(
        targetValue = if (selected) {
            event?.color ?: Color.Black
        } else {
            (event?.color ?: Color.White).addAlpha(0.1f)
        },
    )

    val alpha = animateFloatAsState(
        targetValue = if (currMonth) {
            1.0f
        } else {
            0.1f
        }
    )

    if (selected) {
        Pair(Color.White, event?.color ?: Color.Black)
    } else {
        if (event != null) {
            Pair(event.color, event.color.addAlpha(0.1f))
        } else {
            Pair(Color.Black, Color.White.addAlpha(0.1f))
        }
    }
    Box(
        modifier = Modifier
        .padding(8.dp)
        .size(32.dp)
        .fillMaxSize()
        .clip(CircleShape)
        .alpha(alpha.value)
        .background(backgroundColor.value)
        .clickable { onClick() }
    ) {
        Text(
            text = "$day",
            modifier = Modifier.align(Center),
            fontSize = 12.sp,
            color = textColor.value
        )
    }
}