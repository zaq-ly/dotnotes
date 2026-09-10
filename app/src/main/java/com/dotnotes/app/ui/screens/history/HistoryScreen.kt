package com.dotnotes.app.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import com.dotnotes.app.ui.theme.isAppInDarkTheme
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.dotnotes.app.data.model.previewText
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.ui.theme.ReminderBadgeColors
import com.dotnotes.app.data.model.Note
import com.dotnotes.app.ui.i18n.LocalStrings
import com.dotnotes.app.ui.screens.notelist.NoteListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onNoteClick: (String) -> Unit,
    viewModel: NoteListViewModel = viewModel(
        factory = NoteListViewModel.Factory(DotNotesApp.instance.repository)
    )
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val notes by viewModel.notes.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedNoteIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    val pendingNotes = notes.filter { it.reminderTime != null && !it.isAlarmDismissed && !it.isArchived }
        .sortedBy { it.reminderTime }
    val completedNotes = notes.filter { it.isArchived || (it.reminderTime != null && it.isAlarmDismissed) }
        .sortedByDescending { it.updatedAt }

    val currentList = if (selectedTab == 0) pendingNotes else completedNotes

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = String.format(strings.selectedCount, selectedNoteIds.size),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { selectedNoteIds = emptySet() },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = strings.cancel)
                        }
                    },
                    actions = {
                        val currentTabIds = currentList.map { it.id }.toSet()
                        val isAllSelected = currentTabIds.isNotEmpty() && selectedNoteIds.containsAll(currentTabIds)
                        Box(modifier = Modifier.padding(end = 6.dp)) {
                            IconButton(onClick = {
                                selectedNoteIds = if (isAllSelected) {
                                    selectedNoteIds - currentTabIds
                                } else {
                                    selectedNoteIds + currentTabIds
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                    contentDescription = if (isAllSelected) strings.deselectAll else strings.selectAll,
                                    tint = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = strings.reminderHistory,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                        }
                    }
                )
            }
        },
        bottomBar = {
            // Floating Pill Action Bar in Selection Mode
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            if (selectedTab == 1) {
                                // Restore / Tampilkan ke Depan
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable {
                                            viewModel.restoreHistoryNotes(selectedNoteIds)
                                            Toast.makeText(context, strings.restoredToNotes, Toast.LENGTH_SHORT).show()
                                            selectedNoteIds = emptySet()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Unarchive,
                                        contentDescription = strings.restore,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${strings.restore} (${selectedNoteIds.size})",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Vertical Divider
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                )
                            }

                            // Delete Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable {
                                        viewModel.deleteNotes(selectedNoteIds, context)
                                        selectedNoteIds = emptySet()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = strings.delete,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${strings.delete} (${selectedNoteIds.size})",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Segmented Pill Tab Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 0: Pending / Aktif
                    val isTab0 = selectedTab == 0
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedTab = 0 },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isTab0) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${strings.pendingTasks} (${pendingNotes.size})",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isTab0) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isTab0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Tab 1: Completed / Selesai
                    val isTab1 = selectedTab == 1
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedTab = 1 },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isTab1) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${strings.completedTasks} (${completedNotes.size})",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isTab1) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isTab1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            val reminderFeedbackFormat = remember(strings.locale) { SimpleDateFormat("d MMM yyyy, HH:mm", strings.locale) }
            val handleDismissReminder: (Note) -> Unit = { note ->
                viewModel.dismissReminder(context, note.id) { nextTime ->
                    if (nextTime != null) {
                        val dateStr = reminderFeedbackFormat.format(Date(nextTime))
                        Toast.makeText(context, strings.reminderDoneRepeated.format(dateStr), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, strings.reminderDoneOnce, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == 0) strings.noPendingTasks else strings.noCompletedTasks,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList, key = { it.id }) { note ->
                        val isSelected = selectedNoteIds.contains(note.id)
                        HistoryCard(
                            note = note,
                            selectedTab = selectedTab,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) {
                                    selectedNoteIds = if (isSelected) selectedNoteIds - note.id else selectedNoteIds + note.id
                                } else {
                                    onNoteClick(note.id)
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedNoteIds = setOf(note.id)
                                }
                            },
                            onDismissReminder = { handleDismissReminder(note) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryCard(
    note: Note,
    selectedTab: Int,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissReminder: () -> Unit
) {
    val strings = LocalStrings.current
    val reminderFormat = remember(strings.locale) { SimpleDateFormat("dd MMM yyyy, hh:mm a", strings.locale) }
    val reminderTime = note.reminderTime ?: 0L
    val isOverdue = selectedTab == 0 && reminderTime < System.currentTimeMillis()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.5.dp,
            pressedElevation = 2.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.title.ifEmpty { strings.untitled },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else if (isOverdue) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = strings.overdue,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            val preview = note.previewText
            if (preview.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val isDark = isAppInDarkTheme()
                val isCompleted = selectedTab == 1
                val isAlarm = note.priority == 2 || isOverdue
                val pillBg = if (isCompleted) MaterialTheme.colorScheme.surfaceContainerHigh else ReminderBadgeColors.containerColor(isAlarm = isAlarm, isDark = isDark)
                val pillBorder = if (isCompleted) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f) else ReminderBadgeColors.borderColor(isAlarm = isAlarm, isDark = isDark)
                val pillContent = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else ReminderBadgeColors.contentColor(isAlarm = isAlarm, isDark = isDark)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = pillBg,
                    border = BorderStroke(1.dp, pillBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted)
                                Icons.Default.CheckCircle
                            else if (note.priority == 2)
                                Icons.Default.Alarm
                            else
                                Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = pillContent
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (reminderTime > 0) reminderFormat.format(Date(reminderTime)) else "",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = pillContent
                        )
                        if (note.repeatInterval != com.dotnotes.app.alarm.ReminderHelper.REPEAT_NONE && note.repeatInterval.isNotBlank()) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = pillContent
                            )
                        }
                    }
                }

                if (selectedTab == 0 && !isSelectionMode) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onDismissReminder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = strings.markDone,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
