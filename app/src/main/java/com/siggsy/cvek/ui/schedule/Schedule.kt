package com.siggsy.cvek.ui.schedule

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.siggsy.cvek.data.easistent.Week
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

enum class SpecialEvent {
    EXAM
}

@Composable
fun Event(
    from: LocalTime,
    to: LocalTime,
    name: String,
    teacher: String,
    className: String,
    specialEvents: Set<SpecialEvent>
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp)) {
            Text(
                text = "$from - $to",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = teacher,
                fontSize = 14.sp
            )
            Text(
                text = className,
                fontSize = 14.sp
            )

            if (specialEvents.isNotEmpty()) {
                Spacer(Modifier.size(10.dp))
                Divider()
                // TODO events
            }

        }
    }
}



@Preview
@Composable
fun Preview() {
    LazyColumn {
        items(12) {
            Event(
                LocalTime.of(10, 10),
                LocalTime.of(10, 30),
                "MAT",
                "Lojze Stefan",
                "210",
                emptySet()
            )
        }
    }
}