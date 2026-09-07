package com.dotnotes.app.ui.screens.alarm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val strings = LocalStrings.current
    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
    }
    var currentDate by remember {
        mutableStateOf(SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            currentDate = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121316))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP HEADER: Clock & Alarm Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alarm Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF601410),
                    border = BorderStroke(1.dp, Color(0xFF8C1D18))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color(0xFFFFB4AB),
                            modifier = Modifier.size(16.dp)
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
                        color = Color(0xFFE3E2E6)
                    )
                    Text(
                        text = currentDate,
                        fontSize = 12.sp,
                        color = Color(0xFF8E9099),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // 2. CENTER: DOMINANT NOTE CONTENT CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1B1C20)
                ),
                border = BorderStroke(1.dp, Color(0xFF33353A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Note Title
                    Text(
                        text = noteTitle.ifBlank { strings.untitled },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE3E2E6),
                        lineHeight = 36.sp
                    )

                    if (noteContent.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFF33353A))
                        Spacer(Modifier.height(16.dp))

                        // Note Description / Content
                        Text(
                            text = noteContent,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFC4C6D0),
                            lineHeight = 25.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 3. BOTTOM: Action Buttons (Large Pill Buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Snooze Button
                FilledTonalButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp),
                    shape = RoundedCornerShape(29.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2B2C32),
                        contentColor = Color(0xFFE3E2E6)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = strings.snooze,
                        modifier = Modifier.size(20.dp)
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
                        .height(58.dp),
                    shape = RoundedCornerShape(29.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB4AB),
                        contentColor = Color(0xFF690005)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.dismiss,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = strings.dismiss,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
