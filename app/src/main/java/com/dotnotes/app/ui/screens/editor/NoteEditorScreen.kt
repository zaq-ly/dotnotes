package com.dotnotes.app.ui.screens.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    var isChecklistMode by remember { mutableStateOf(false) }
    var checklistItems by remember { mutableStateOf<List<ChecklistItem>>(listOf(ChecklistItem())) }

    var showReminderDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isContentInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && !isContentInitialized) {
            if (state.content.isNotEmpty()) {
                if (isContentChecklist(state.content)) {
                    isChecklistMode = true
                    checklistItems = parseChecklist(state.content)
                } else {
                    richTextState.setHtml(state.content)
                }
            }
            isContentInitialized = true
        }
    }

    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    )

    fun applyFormatting(spanStyle: SpanStyle) {
        richTextState.toggleSpanStyle(spanStyle)
    }

    fun saveAndExit() {
        val contentToSave = if (isChecklistMode) {
            checklistToHtml(checklistItems)
        } else {
            richTextState.toHtml()
        }
        viewModel.updateContent(contentToSave)
        viewModel.save()
        onBack()
    }

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (noteId == null) strings.newNote else strings.editNote,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { saveAndExit() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showReminderDialog = true }) {
                            Icon(
                                imageVector = if (state.priority == 2 && state.hasReminder) Icons.Default.Alarm else Icons.Default.Notifications,
                                contentDescription = strings.reminder,
                                tint = if (state.hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = { saveAndExit() }) {
                            Icon(Icons.Default.Check, contentDescription = strings.save, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = WindowInsets.isImeVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    val isBold = richTextState.currentSpanStyle.fontWeight != null && richTextState.currentSpanStyle.fontWeight!!.weight >= FontWeight.Bold.weight
                    val isItalic = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic
                    val isOrderedList = richTextState.isOrderedList
                    val isUnorderedList = richTextState.isUnorderedList

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                    ) {
                        Column {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FormattingButton(
                                    icon = if (isChecklistMode) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = "Checklist",
                                    isActive = isChecklistMode,
                                    onClick = {
                                        if (isChecklistMode) {
                                            val text = checklistItems.filter { it.text.isNotEmpty() || checklistItems.size == 1 }.joinToString("\n") { it.text }
                                            richTextState.setText(text)
                                            isChecklistMode = false
                                        } else {
                                            val currentText = richTextState.annotatedString.text
                                            checklistItems = parseChecklist(currentText)
                                            isChecklistMode = true
                                        }
                                    }
                                )

                                FormattingButton(
                                    icon = Icons.Default.FormatBold,
                                    contentDescription = "Bold",
                                    isActive = isBold,
                                    onClick = {
                                        applyFormatting(SpanStyle(fontWeight = FontWeight.ExtraBold))
                                    }
                                )

                                FormattingButton(
                                    icon = Icons.Default.FormatItalic,
                                    contentDescription = "Italic",
                                    isActive = isItalic,
                                    onClick = {
                                        applyFormatting(SpanStyle(fontStyle = FontStyle.Italic))
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
                    }
                }
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // 1. Clean, Borderless Title Input
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

                    Spacer(Modifier.height(4.dp))

                    if (isChecklistMode) {
                        // 2. Interactive Material 3 Checklist Mode
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            checklistItems.forEachIndexed { index, item ->
                                ChecklistItemRow(
                                    item = item,
                                    onTextChange = { newText ->
                                        if (newText.contains("\n")) {
                                            val parts = newText.split("\n")
                                            val updated = checklistItems.toMutableList()
                                            updated[index] = item.copy(text = parts[0])
                                            for (i in 1 until parts.size) {
                                                updated.add(index + i, ChecklistItem(text = parts[i], isChecked = false))
                                            }
                                            checklistItems = updated
                                        } else {
                                            val updated = checklistItems.toMutableList()
                                            updated[index] = item.copy(text = newText)
                                            checklistItems = updated
                                        }
                                    },
                                    onCheckedChange = { checked ->
                                        val updated = checklistItems.toMutableList()
                                        updated[index] = item.copy(isChecked = checked)
                                        checklistItems = updated
                                    },
                                    onDelete = {
                                        if (checklistItems.size > 1) {
                                            val updated = checklistItems.toMutableList()
                                            updated.removeAt(index)
                                            checklistItems = updated
                                        } else {
                                            val updated = checklistItems.toMutableList()
                                            updated[0] = item.copy(text = "", isChecked = false)
                                            checklistItems = updated
                                        }
                                    },
                                    onEnterPressed = {
                                        val updated = checklistItems.toMutableList()
                                        updated.add(index + 1, ChecklistItem(text = "", isChecked = false))
                                        checklistItems = updated
                                    }
                                )
                            }

                            // Add Item Button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        checklistItems = checklistItems + ChecklistItem(text = "", isChecked = false)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "+ Tambah item",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.height(24.dp))
                        }
                    } else {
                        // 2. Clean, Borderless RichTextEditor
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
                                unfocusedIndicatorColor = Color.Transparent,
                                selectionColors = customTextSelectionColors
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    val dateOnlyFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
    val timeOnlyFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Reminder Pop-up Dialog
    if (showReminderDialog) {
        Dialog(onDismissRequest = { showReminderDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    // Header Row: Icon + Label + Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (state.priority == 2 && state.hasReminder) Icons.Default.Alarm else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (state.hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = strings.reminder,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = state.hasReminder,
                            onCheckedChange = { enabled ->
                                viewModel.setReminder(enabled)
                                if (enabled && state.reminderTime == null) {
                                    viewModel.setReminderTime(System.currentTimeMillis())
                                }
                            },
                            modifier = Modifier.scale(0.85f),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Expanded Options when Reminder is Enabled
                    if (state.hasReminder) {
                        Spacer(Modifier.height(12.dp))

                        // Split Date & Time Row (Compact Google Calendar Style)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Date Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .clickable(onClick = { showDatePicker = true })
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (state.reminderTime != null) dateOnlyFormat.format(Date(state.reminderTime!!)) else strings.selectDate,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Time Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = { showTimePicker = true })
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (state.reminderTime != null) timeOnlyFormat.format(Date(state.reminderTime!!)) else strings.selectTime,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Priority Selection (Notification vs Alarm)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = state.priority <= 1,
                                onClick = { viewModel.setPriority(1) },
                                modifier = Modifier.height(28.dp),
                                label = { Text(strings.notification, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(13.dp))
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            FilterChip(
                                selected = state.priority == 2,
                                onClick = { viewModel.setPriority(2) },
                                modifier = Modifier.height(28.dp),
                                label = { Text(strings.alarm, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(13.dp))
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.error,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showReminderDialog = false }) {
                            Text(strings.save, fontWeight = FontWeight.SemiBold)
                        }
                    }
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

data class ChecklistItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val isChecked: Boolean = false
)

private fun parseChecklist(content: String): List<ChecklistItem> {
    val items = mutableListOf<ChecklistItem>()
    val cleanHtml = content.replace("<br>", "\n").replace("<br/>", "\n").replace("</p>", "\n").replace("</li>", "\n")
    val rawText = cleanHtml.replace(Regex("<[^>]*>"), "").trimEnd()
    val lines = rawText.split("\n")
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty() && lines.size == 1) continue
        val isChecked = trimmed.startsWith("☑") || trimmed.startsWith("[x]") || trimmed.startsWith("[X]") || trimmed.startsWith("✓")
        val cleanLine = when {
            trimmed.startsWith("☑ ") || trimmed.startsWith("☐ ") -> trimmed.substring(2)
            trimmed.startsWith("☑") || trimmed.startsWith("☐") -> trimmed.substring(1)
            trimmed.startsWith("[x] ") || trimmed.startsWith("[X] ") || trimmed.startsWith("[ ] ") -> trimmed.substring(4)
            trimmed.startsWith("[x]") || trimmed.startsWith("[X]") || trimmed.startsWith("[ ]") -> trimmed.substring(3)
            else -> trimmed
        }
        if (cleanLine.isNotEmpty() || lines.size == 1) {
            items.add(ChecklistItem(text = cleanLine, isChecked = isChecked))
        }
    }
    if (items.isEmpty()) {
        items.add(ChecklistItem(text = "", isChecked = false))
    }
    return items
}

private fun checklistToHtml(items: List<ChecklistItem>): String {
    val sb = StringBuilder()
    for (item in items) {
        if (item.text.isNotEmpty() || items.size == 1) {
            val prefix = if (item.isChecked) "☑ " else "☐ "
            sb.append("<p>").append(prefix).append(item.text).append("</p>")
        }
    }
    return sb.toString()
}

private fun isContentChecklist(content: String): Boolean {
    return content.contains("☐") || content.contains("☑") || content.contains("[ ]") || content.contains("[x]") || content.contains("[X]")
}

@Composable
private fun ChecklistItemRow(
    item: ChecklistItem,
    onTextChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEnterPressed: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Material 3 Checkbox
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.size(36.dp)
        )

        Spacer(Modifier.width(4.dp))

        // Text Field for Item Description
        BasicTextField(
            value = item.text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = if (item.isChecked)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                else
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (item.isChecked)
                    TextDecoration.LineThrough
                else
                    TextDecoration.None
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onEnterPressed() }
            ),
            decorationBox = { innerTextField ->
                if (item.text.isEmpty()) {
                    Text(
                        text = "Item catatan / belanja...",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                }
                innerTextField()
            }
        )

        // Delete button for this item (subtle)
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
