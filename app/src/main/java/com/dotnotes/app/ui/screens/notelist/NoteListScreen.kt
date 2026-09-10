package com.dotnotes.app.ui.screens.notelist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.dotnotes.app.data.model.previewText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import android.widget.Toast
import com.dotnotes.app.alarm.ReminderHelper
import com.dotnotes.app.ui.theme.isAppInDarkTheme
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.dotnotes.app.ui.theme.NoteColorThemes
import com.dotnotes.app.ui.theme.ReminderBadgeColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.data.model.Note
import com.dotnotes.app.ui.i18n.LocalStrings
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onNoteClick: (String) -> Unit,
    onNewNote: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: NoteListViewModel = viewModel(
        factory = NoteListViewModel.Factory(DotNotesApp.instance.repository)
    )
) {
    val strings = LocalStrings.current
    val notes by viewModel.notes.collectAsState()
    val visibleNotes = remember(notes) {
        notes.filter { !it.isArchived && !(it.reminderTime != null && it.isAlarmDismissed && it.autoArchive) }
    }
    val hasUpdate by viewModel.hasUpdate.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkForUpdate()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var selectedNoteIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedNoteIds.isNotEmpty()
    val context = androidx.compose.ui.platform.LocalContext.current


    Scaffold(
        topBar = {
            if (isSelectionMode) {
                // TopBar in Selection Mode (Pill Header)
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = {
                        Text(
                            text = String.format(strings.selectedCount, selectedNoteIds.size),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                        val isAllSelected = selectedNoteIds.size == visibleNotes.size && visibleNotes.isNotEmpty()
                        Box(modifier = Modifier.padding(end = 6.dp)) {
                            IconButton(onClick = {
                                selectedNoteIds = if (isAllSelected) emptySet() else visibleNotes.map { it.id }.toSet()
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
                // Google Pixel Search Bar Style Header with status bar padding
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.3.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            // History Icon (Pengingat Terlewat / Riwayat)
                            val overdueCount = notes.count {
                                it.reminderTime != null && it.reminderTime <= System.currentTimeMillis() && !it.isAlarmDismissed
                            }
                            IconButton(
                                onClick = onHistoryClick,
                                modifier = Modifier.size(38.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (overdueCount > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError
                                            ) {
                                                Text(overdueCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = strings.reminderHistory,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(2.dp))

                            // Settings / Update Icon Button
                            IconButton(
                                onClick = onSettingsClick,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (hasUpdate) Icons.Default.SystemUpdate else Icons.Default.Settings,
                                        contentDescription = if (hasUpdate) strings.updateAvailable else strings.settings,
                                        tint = if (hasUpdate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (hasUpdate) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(8.dp)
                                                .background(MaterialTheme.colorScheme.error, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
                            val selectedNotesList = visibleNotes.filter { selectedNoteIds.contains(it.id) }
                            val allPinned = selectedNotesList.isNotEmpty() && selectedNotesList.all { it.isPinned }

                            // Pin / Unpin Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable {
                                        val shouldPin = !allPinned
                                        viewModel.togglePinNotes(selectedNoteIds, shouldPin)
                                        selectedNoteIds = emptySet()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (allPinned) strings.unpin else strings.pin,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            )

                            // Archive Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable {
                                        viewModel.archiveNotes(selectedNoteIds)
                                        Toast.makeText(context, strings.notesArchived, Toast.LENGTH_SHORT).show()
                                        selectedNoteIds = emptySet()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = strings.archive,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            )

                            // Delete Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable {
                                        viewModel.deleteNotes(selectedNoteIds, context)
                                        selectedNoteIds = emptySet()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
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
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                // Pixel-style squircle FAB
                FloatingActionButton(
                    onClick = onNewNote,
                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = strings.newNote,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val reminderFeedbackFormat = remember(strings.locale) { SimpleDateFormat("d MMM yyyy, hh:mm a", strings.locale) }
            val handleDismissReminder: (Note) -> Unit = { note ->
                viewModel.dismissReminder(context, note.id) { nextTime ->
                    if (nextTime != null) {
                        val dateStr = reminderFeedbackFormat.format(Date(nextTime))
                        Toast.makeText(context, strings.reminderDoneRepeated.format(dateStr), Toast.LENGTH_SHORT).show()
                    } else {
                        val msg = if (note.autoArchive) strings.reminderDoneMovedToHistory else strings.reminderDoneOnce
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            if (visibleNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        strings.noNotesYet,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibleNotes, key = { it.id }) { note ->
                        val isSelected = selectedNoteIds.contains(note.id)
                        SelectableNoteCard(
                            note = note,
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

/**
 * Pixel Material You squircle note card with soft tonal styling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SelectableNoteCard(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissReminder: () -> Unit
) {
    val strings = LocalStrings.current
    val isDark = isAppInDarkTheme()
    val noteTheme = remember(note.colorTheme, isDark) {
        NoteColorThemes.getThemeColors(note.colorTheme, isDark)
    }
    val hasCustomTheme = note.colorTheme.isNotEmpty() && note.colorTheme != NoteColorThemes.DEFAULT

    Card(
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
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (hasCustomTheme) {
                noteTheme.background
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.5.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = note.title.ifEmpty { strings.untitled },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = if (hasCustomTheme) noteTheme.onSurface else MaterialTheme.colorScheme.onSurface,
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
                } else {
                    if (note.isPinned) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = strings.pinned,
                            modifier = Modifier.size(18.dp),
                            tint = if (hasCustomTheme) noteTheme.primary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            val preview = note.previewText
            if (preview.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (hasCustomTheme) noteTheme.onSurface.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }

            val hasActiveReminder = note.reminderTime != null && !note.isAlarmDismissed
            if (hasActiveReminder) {
                val isAlarm = note.priority == 2
                val hasRepeat = note.repeatInterval.isNotBlank() && note.repeatInterval != ReminderHelper.REPEAT_NONE
                val cardReminderFormat = remember(strings.locale) { SimpleDateFormat("d MMM, hh:mm a", strings.locale) }

                val badgeBg = if (hasCustomTheme) {
                    noteTheme.primary.copy(alpha = 0.08f)
                } else {
                    if (isDark) Color(0xFF2A2D34).copy(alpha = 0.40f) else Color(0xFFE5E8EF).copy(alpha = 0.45f)
                }
                val badgeContent = if (hasCustomTheme) {
                    noteTheme.primary.copy(alpha = 0.55f)
                } else {
                    if (isDark) Color(0xFFC4C7D0).copy(alpha = 0.50f) else Color(0xFF5E626B).copy(alpha = 0.55f)
                }

                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBg
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Icon(
                                imageVector = if (isAlarm) Icons.Default.Alarm else Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = badgeContent
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = cardReminderFormat.format(Date(note.reminderTime)),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Normal,
                                color = badgeContent
                            )
                            if (hasRepeat) {
                                Spacer(Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(9.dp),
                                    tint = badgeContent
                                )
                            }
                        }
                    }

                    if (!isSelectionMode) {
                        val doneColor = if (hasCustomTheme) noteTheme.primary.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onDismissReminder)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = strings.markDone,
                                modifier = Modifier.size(12.dp),
                                tint = doneColor
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = strings.markDone,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = doneColor
                            )
                        }
                    }
                }
            }
        }
    }
}
