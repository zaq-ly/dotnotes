package com.dotnotes.app.ui.screens.editor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.ui.i18n.LocalStrings
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String?,
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = viewModel(
        factory = NoteEditorViewModel.Factory(
            DotNotesApp.instance.repository,
            noteId,
            DotNotesApp.instance
        )
    )
) {
    val strings = LocalStrings.current
    val state by viewModel.state.collectAsState()
    val richTextState = rememberRichTextState()
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    LaunchedEffect(state.content, state.isLoading) {
        if (!state.isLoading && state.content.isNotEmpty()) {
            richTextState.setHtml(state.content)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (noteId == null) strings.newNote else strings.editNote,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.updateContent(richTextState.toHtml())
                        viewModel.save()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.updateContent(richTextState.toHtml())
                        viewModel.save()
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = strings.save, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // 1. Simplified, Modern Reminder Pill at TOP
                ModernReminderBar(
                    hasReminder = state.hasReminder,
                    reminderTime = state.reminderTime,
                    priority = state.priority,
                    dateFormat = dateFormat,
                    onToggleReminder = viewModel::setReminder,
                    onSetTime = viewModel::setReminderTime,
                    onSetPriority = viewModel::setPriority,
                    context = context
                )

                Spacer(Modifier.height(10.dp))

                // 2. Clean, Borderless Title Input
                TextField(
                    value = state.title,
                    onValueChange = viewModel::updateTitle,
                    placeholder = {
                        Text(
                            strings.noteTitleHint,
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // 3. Formatting Toolbar with Active Indicators
                val isBold = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold
                val isItalic = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic
                val isOrderedList = richTextState.isOrderedList
                val isUnorderedList = richTextState.isUnorderedList

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilledIconToggleButton(
                            checked = isBold,
                            onCheckedChange = {
                                richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            },
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedContentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp))
                        }

                        FilledIconToggleButton(
                            checked = isItalic,
                            onCheckedChange = {
                                richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            },
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedContentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(18.dp))
                        }

                        FilledIconToggleButton(
                            checked = isUnorderedList,
                            onCheckedChange = {
                                richTextState.toggleUnorderedList()
                            },
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedContentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Bullet List", modifier = Modifier.size(18.dp))
                        }

                        FilledIconToggleButton(
                            checked = isOrderedList,
                            onCheckedChange = {
                                richTextState.toggleOrderedList()
                            },
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedContentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.FormatListNumbered, contentDescription = "Numbered List", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // 4. Clean, Borderless RichTextEditor
                RichTextEditor(
                    state = richTextState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = {
                        Text(
                            strings.noteContentHint,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontSize = 16.sp
                        )
                    },
                    colors = RichTextEditorDefaults.richTextEditorColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
private fun ModernReminderBar(
    hasReminder: Boolean,
    reminderTime: Long?,
    priority: Int,
    dateFormat: SimpleDateFormat,
    onToggleReminder: (Boolean) -> Unit,
    onSetTime: (Long) -> Unit,
    onSetPriority: (Int) -> Unit,
    context: android.content.Context
) {
    val strings = LocalStrings.current

    if (!hasReminder) {
        // Subtle, elegant "Add Reminder" button
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.clickable {
                showDateTimePicker(context) { timeMillis ->
                    onSetTime(timeMillis)
                    onToggleReminder(true)
                }
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAlert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = strings.setReminder,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else {
        // Modern, single-line pill bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (priority == 2) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                // Priority Tag (Click to toggle Notification / Alarm)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (priority == 2) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.clickable {
                        onSetPriority(if (priority == 2) 1 else 2)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = if (priority == 2) Icons.Default.Alarm else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (priority == 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (priority == 2) strings.alarm else strings.notification,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (priority == 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Date/Time Text (Click to change)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            showDateTimePicker(context) { timeMillis ->
                                onSetTime(timeMillis)
                            }
                        }
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (reminderTime != null) dateFormat.format(Date(reminderTime)) else strings.setReminder,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Clear Button
                IconButton(
                    onClick = { onToggleReminder(false) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = strings.clearReminder,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun showDateTimePicker(context: android.content.Context, onResult: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    onResult(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
