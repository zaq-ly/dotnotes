package com.dotnotes.app.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.content, state.isLoading) {
        if (!state.isLoading && state.content.isNotEmpty()) {
            richTextState.setHtml(state.content)
        }
    }

    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Scaffold(
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
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
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // 1. Google Calendar Style Reminder Card (Split Date & Time Row)
                    GoogleCalendarReminderCard(
                        hasReminder = state.hasReminder,
                        reminderTime = state.reminderTime,
                        priority = state.priority,
                        onToggleReminder = { enabled ->
                            viewModel.setReminder(enabled)
                            if (enabled && state.reminderTime == null) {
                                showDatePicker = true
                            }
                        },
                        onOpenDatePicker = { showDatePicker = true },
                        onOpenTimePicker = { showTimePicker = true },
                        onSetPriority = viewModel::setPriority
                    )

                    Spacer(Modifier.height(10.dp))

                    // 2. Clean, Borderless Title Input (Supports double-tap selection)
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
                            unfocusedIndicatorColor = Color.Transparent,
                            selectionColors = customTextSelectionColors
                        )
                    )

                    // 3. Formatting Toolbar with Non-Focus-Stealing Buttons
                    val isBold = richTextState.currentSpanStyle.fontWeight != null && richTextState.currentSpanStyle.fontWeight!!.weight >= FontWeight.Bold.weight
                    val isItalic = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic
                    val isOrderedList = richTextState.isOrderedList
                    val isUnorderedList = richTextState.isUnorderedList

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FormattingButton(
                                icon = Icons.Default.FormatBold,
                                contentDescription = "Bold",
                                isActive = isBold,
                                onClick = {
                                    richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                }
                            )

                            FormattingButton(
                                icon = Icons.Default.FormatItalic,
                                contentDescription = "Italic",
                                isActive = isItalic,
                                onClick = {
                                    richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                                }
                            )

                            FormattingButton(
                                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                                contentDescription = "Bullet List",
                                isActive = isUnorderedList,
                                onClick = {
                                    richTextState.toggleUnorderedList()
                                }
                            )

                            FormattingButton(
                                icon = Icons.Default.FormatListNumbered,
                                contentDescription = "Numbered List",
                                isActive = isOrderedList,
                                onClick = {
                                    richTextState.toggleOrderedList()
                                }
                            )
                        }
                    }

                    // 4. Clean, Borderless RichTextEditor (Supports persistent scrolling and double-tap selection)
                    RichTextEditor(
                        state = richTextState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 350.dp),
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
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    // Google Calendar style separate Date & Time pickers
    val currentCalendar = remember(state.reminderTime) {
        Calendar.getInstance().apply {
            if (state.reminderTime != null) timeInMillis = state.reminderTime!!
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentCalendar.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        val updatedCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                        }
                        viewModel.setReminderTime(updatedCal.timeInMillis)
                        viewModel.setReminder(true)
                    }
                    showDatePicker = false
                }) {
                    Text(strings.save, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(strings.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = currentCalendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentCalendar.get(Calendar.MINUTE),
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = strings.selectTime,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val updatedCal = Calendar.getInstance().apply {
                        if (state.reminderTime != null) timeInMillis = state.reminderTime!!
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                    }
                    viewModel.setReminderTime(updatedCal.timeInMillis)
                    viewModel.setReminder(true)
                    showTimePicker = false
                }) {
                    Text(strings.save, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun FormattingButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val iconTint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Google Calendar style reminder card with split date and time pills.
 */
@Composable
private fun GoogleCalendarReminderCard(
    hasReminder: Boolean,
    reminderTime: Long?,
    priority: Int,
    onToggleReminder: (Boolean) -> Unit,
    onOpenDatePicker: () -> Unit,
    onOpenTimePicker: () -> Unit,
    onSetPriority: (Int) -> Unit
) {
    val strings = LocalStrings.current
    val dateOnlyFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
    val timeOnlyFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (hasReminder)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Header Row: Icon + Label + Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (priority == 2 && hasReminder) Icons.Default.Alarm else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = strings.reminder,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = hasReminder,
                    onCheckedChange = onToggleReminder,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Expanded Options when Reminder is Enabled
            if (hasReminder) {
                Spacer(Modifier.height(8.dp))

                // Split Date & Time Row (Google Calendar Style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .weight(1.3f)
                            .clickable(onClick = onOpenDatePicker)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (reminderTime != null) dateOnlyFormat.format(Date(reminderTime)) else strings.selectDate,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }

                    // Time Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onOpenTimePicker)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (reminderTime != null) timeOnlyFormat.format(Date(reminderTime)) else strings.selectTime,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Priority Selection (Notification vs Alarm)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = priority <= 1,
                        onClick = { onSetPriority(1) },
                        label = { Text(strings.notification, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    FilterChip(
                        selected = priority == 2,
                        onClick = { onSetPriority(2) },
                        label = { Text(strings.alarm, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.error,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }
}
