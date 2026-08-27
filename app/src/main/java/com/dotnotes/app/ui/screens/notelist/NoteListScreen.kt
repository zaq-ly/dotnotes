package com.dotnotes.app.ui.screens.notelist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.data.model.Note
import com.dotnotes.app.ui.i18n.LocalStrings
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onNoteClick: (String) -> Unit,
    onNewNote: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: NoteListViewModel = viewModel(
        factory = NoteListViewModel.Factory(DotNotesApp.instance.repository)
    )
) {
    val strings = LocalStrings.current
    val notes by viewModel.notes.collectAsState()
    val pinnedNotes = notes.filter { it.isPinned }
    val otherNotes = notes.filter { !it.isPinned }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.appName) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = strings.settings)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewNote) {
                Icon(Icons.Default.Add, contentDescription = strings.newNote)
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(strings.noNotesYet, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (pinnedNotes.isNotEmpty()) {
                    item {
                        Text(
                            strings.pinned,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(pinnedNotes, key = { it.id }) { note ->
                        TelegramSwipeNoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onDelete = { viewModel.deleteNote(note.id) },
                            onTogglePin = { viewModel.togglePin(note) }
                        )
                    }
                    item {
                        Text(
                            strings.others,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                }
                items(otherNotes, key = { it.id }) { note ->
                    TelegramSwipeNoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onDelete = { viewModel.deleteNote(note.id) },
                        onTogglePin = { viewModel.togglePin(note) }
                    )
                }
            }
        }
    }
}

/**
 * Telegram-style bouncy swipe container:
 * - Slide Right: Delete (Spring animation + icon scaling pop)
 * - Slide Left: Pin / Unpin (Elastic spring bounce back)
 */
@Composable
private fun TelegramSwipeNoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val strings = LocalStrings.current
    val thresholdPx = 220f

    val isSwipingRight = offsetX.value > 0
    val isSwipingLeft = offsetX.value < 0
    val swipeProgress = (abs(offsetX.value) / thresholdPx).coerceIn(0f, 1.5f)
    val iconScale = 0.6f + (swipeProgress * 0.5f).coerceAtMost(0.6f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Background Action Layer (Telegram-style revealed actions)
        if (isSwipingRight) {
            // Delete Background (Red / Error)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.scale(iconScale)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = strings.delete,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = strings.delete,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        } else if (isSwipingLeft) {
            // Pin/Unpin Background (Primary / Accent)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.scale(iconScale)
                ) {
                    Text(
                        text = if (note.isPinned) strings.unpin else strings.pin,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Foreground Card with Telegram spring drag gesture
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetX.value > thresholdPx) {
                                    // Trigger Delete with smooth glide
                                    offsetX.animateTo(
                                        targetValue = 1000f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                    onDelete()
                                } else if (offsetX.value < -thresholdPx) {
                                    // Trigger Pin/Unpin and spring bounce back
                                    onTogglePin()
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                } else {
                                    // Release back to center
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                // Apply resistance when pulling far
                                val newOffset = offsetX.value + dragAmount * 0.85f
                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                }
        ) {
            CleanNoteCard(
                note = note,
                onClick = onClick
            )
        }
    }
}

/**
 * Clean Note Card without cluttered action buttons in footer.
 */
@Composable
private fun CleanNoteCard(
    note: Note,
    onClick: () -> Unit
) {
    val strings = LocalStrings.current
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val reminderFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title.ifEmpty { strings.untitled },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.isPinned) {
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = strings.pinned,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (note.content.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = note.content.replace(Regex("<[^>]*>"), ""),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (note.reminderTime != null && !note.isAlarmDismissed) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (note.priority == 2)
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                    else
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (note.priority == 2) Icons.Default.Alarm else Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (note.priority == 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = reminderFormat.format(Date(note.reminderTime)),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (note.priority == 2) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = "${strings.edited} ${dateFormat.format(Date(note.updatedAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
