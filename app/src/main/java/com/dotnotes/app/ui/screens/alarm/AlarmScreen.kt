package com.dotnotes.app.ui.screens.alarm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotnotes.app.ui.i18n.LocalStrings
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlarmScreen(
    noteTitle: String,
    noteContent: String = "",
    autoArchive: Boolean = true,
    hasRepeat: Boolean = false,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onStopRecurring: () -> Unit = onDismiss
) {
    val strings = LocalStrings.current
    var currentTime by remember(strings.locale) {
        mutableStateOf(SimpleDateFormat("hh:mm a", strings.locale).format(Date()))
    }
    var currentDate by remember(strings.locale) {
        mutableStateOf(SimpleDateFormat("EEEE, dd MMMM", strings.locale).format(Date()))
    }

    LaunchedEffect(strings.locale) {
        while (true) {
            currentTime = SimpleDateFormat("hh:mm a", strings.locale).format(Date())
            currentDate = SimpleDateFormat("EEEE, dd MMMM", strings.locale).format(Date())
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111215))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP HEADER: Clock & Alarm Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alarm Pill
                Surface(
                    shape = RoundedCornerShape(100),
                    color = Color(0xFF421412),
                    border = BorderStroke(1.dp, Color(0xFF6E201B))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color(0xFFFFB4AB),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = strings.alarm.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB4AB),
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                // Small Clock & Date
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currentTime,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE4E6ED)
                    )
                    Text(
                        text = currentDate,
                        fontSize = 12.sp,
                        color = Color(0xFF8F929D),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // 2. CENTER: DOMINANT NOTE CONTENT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 10.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF191B20)
                ),
                border = BorderStroke(1.dp, Color(0xFF2C2E36))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Note Title
                    Text(
                        text = noteTitle.ifBlank { strings.untitled },
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE4E6ED),
                        lineHeight = 34.sp
                    )

                    if (noteContent.isNotBlank()) {
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFF2C2E36), thickness = 0.8.dp)
                        Spacer(Modifier.height(14.dp))

                        // Note Description / Content
                        Text(
                            text = noteContent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFB8BAC4),
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Notice: Auto-Archive vs Keep on Home (Clean Minimalist Pill)
            Surface(
                shape = RoundedCornerShape(100),
                color = Color(0xFF1C1E24),
                border = BorderStroke(0.8.dp, Color(0xFF2C2F38))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (autoArchive) Icons.Default.Archive else Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = if (autoArchive) Color(0xFFA8C7FA) else Color(0xFFC4C7D0),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (autoArchive) strings.alarmAutoArchiveNotice else strings.alarmKeepOnHomeNotice,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (autoArchive) Color(0xFFA8C7FA) else Color(0xFFC4C7D0)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // 3. BOTTOM: Action Buttons (Large Pill Buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Snooze Button
                FilledTonalButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF26282F),
                        contentColor = Color(0xFFE4E6ED)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = strings.snooze,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = strings.snooze,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB4AB),
                        contentColor = Color(0xFF690005)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.dismiss,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = strings.dismiss,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (hasRepeat) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(100),
                    color = Color(0xFF1C1E24),
                    border = BorderStroke(0.8.dp, Color(0xFF2C2F38)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100))
                        .clickable(onClick = onStopRecurring)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFFFFB4AB),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = strings.stopRecurringPermanently,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFB4AB)
                        )
                    }
                }
            }
        }
    }
}
