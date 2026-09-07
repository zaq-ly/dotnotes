package com.dotnotes.app.ui.screens.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.alarm.ReminderHelper
import com.dotnotes.app.ui.i18n.LocalStrings
import com.dotnotes.app.ui.theme.NoteColorThemes
import com.dotnotes.app.ui.theme.PureWhite
import com.dotnotes.app.ui.theme.ReminderBadgeColors
import com.dotnotes.app.ui.theme.isAppInDarkTheme
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    val isDark = isAppInDarkTheme()
    val noteColors = remember(state.colorTheme, isDark) {
        NoteColorThemes.getThemeColors(state.colorTheme, isDark)
    }
    val richTextState = rememberRichTextState()

    var showReminderDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showRepeatMenu by remember { mutableStateOf(false) }
    var isContentInitialized by remember { mutableStateOf(false) }

    var isChecklistMode by remember { mutableStateOf(false) }
    var checklistItems by remember { mutableStateOf<List<ChecklistItem>>(emptyList()) }
    var focusItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && !isContentInitialized) {
            if (isContentChecklist(state.content)) {
                isChecklistMode = true
                checklistItems = parseChecklistItems(state.content)
            } else {
                isChecklistMode = false
                if (state.content.isNotEmpty()) {
                    richTextState.setHtml(state.content)
                }
            }
            isContentInitialized = true
        }
    }

    val customTextSelectionColors = remember(noteColors.primary) {
        TextSelectionColors(
            handleColor = noteColors.primary,
            backgroundColor = noteColors.primary.copy(alpha = 0.35f)
        )
    }

    fun saveAndExit() {
        val finalContent = if (isChecklistMode) {
            checklistToHtml(checklistItems)
        } else {
            richTextState.toHtml()
        }
        viewModel.updateContent(finalContent)
        viewModel.save()
        onBack()
    }

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Scaffold(
            containerColor = noteColors.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = noteColors.background,
                        titleContentColor = noteColors.onSurface,
                        navigationIconContentColor = noteColors.onSurface,
                        actionIconContentColor = noteColors.primary
                    ),
                    title = {
                        Text(
                            if (noteId == null) strings.newNote else strings.editNote,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = noteColors.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { saveAndExit() },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = noteColors.onSurface)
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            if (state.reminderTime == null) {
                                val nowCal = Calendar.getInstance().apply {
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                viewModel.setReminderTime(nowCal.timeInMillis)
                            }
                            showReminderDialog = true 
                        }) {
                            Icon(
                                imageVector = if (state.priority == 2 && state.hasReminder) Icons.Default.Alarm else Icons.Default.Notifications,
                                contentDescription = strings.reminder,
                                tint = if (state.hasReminder) noteColors.primary else noteColors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Box(modifier = Modifier.padding(end = 4.dp)) {
                            IconButton(onClick = { saveAndExit() }) {
                                Icon(Icons.Default.Check, contentDescription = strings.save, tint = noteColors.primary)
                            }
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
                    Surface(
                        color = noteColors.surface,
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
                                // 1. Bold
                                FormattingButton(
                                    icon = Icons.Default.FormatBold,
                                    contentDescription = "Bold",
                                    isActive = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                    }
                                )

                                // 2. Italic
                                FormattingButton(
                                    icon = Icons.Default.FormatItalic,
                                    contentDescription = "Italic",
                                    isActive = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                                    }
                                )

                                // 3. Bulleted List
                                FormattingButton(
                                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                                    contentDescription = "Bullet List",
                                    isActive = !isChecklistMode && richTextState.isUnorderedList,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        if (isChecklistMode) {
                                            val html = checklistItems.joinToString("") { "<li>${it.text}</li>" }
                                            richTextState.setHtml("<ul>$html</ul>")
                                            isChecklistMode = false
                                        } else {
                                            richTextState.toggleUnorderedList()
                                        }
                                    }
                                )

                                // 4. Numbered List
                                FormattingButton(
                                    icon = Icons.Default.FormatListNumbered,
                                    contentDescription = "Numbered List",
                                    isActive = !isChecklistMode && richTextState.isOrderedList,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        if (isChecklistMode) {
                                            val html = checklistItems.joinToString("") { "<li>${it.text}</li>" }
                                            richTextState.setHtml("<ol>$html</ol>")
                                            isChecklistMode = false
                                        } else {
                                            richTextState.toggleOrderedList()
                                        }
                                    }
                                )

                                // 5. Checklist Mode Toggle Button
                                FormattingButton(
                                    icon = if (isChecklistMode) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = strings.checklist,
                                    isActive = isChecklistMode,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        if (!isChecklistMode) {
                                            val html = richTextState.toHtml()
                                            val cleanHtml = html
                                                .replace("<br>", "\n")
                                                .replace("<br/>", "\n")
                                                .replace("<br />", "\n")
                                                .replace("</p>", "\n")
                                                .replace("</li>", "\n")
                                                .replace("</div>", "\n")
                                            val rawText = cleanHtml.replace(Regex("<[^>]*>"), "").trimEnd('\n')
                                            val lines = if (rawText.isBlank()) emptyList() else rawText.split("\n")
                                            checklistItems = if (lines.isEmpty()) {
                                                listOf(ChecklistItem())
                                            } else {
                                                lines.map { ChecklistItem(text = it.trim(), isChecked = false) }
                                            }
                                            isChecklistMode = true
                                        } else {
                                            val html = checklistItems.joinToString("") { "<p>${it.text}</p>" }
                                            richTextState.setHtml(html)
                                            isChecklistMode = false
                                        }
                                    }
                                )

                                // 6. Color Palette
                                FormattingButton(
                                    icon = Icons.Default.Palette,
                                    contentDescription = strings.noteColor,
                                    isActive = state.colorTheme != NoteColorThemes.DEFAULT,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        showColorPicker = true
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
                    CircularProgressIndicator(color = noteColors.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(vertical = 8.dp)
                ) {
                    // 1. Title Input (BasicTextField aligned with 16dp horizontal padding)
                    BasicTextField(
                        value = state.title,
                        onValueChange = viewModel::updateTitle,
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = noteColors.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(noteColors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        decorationBox = { innerTextField ->
                            if (state.title.isEmpty()) {
                                Text(
                                    strings.noteTitleHint,
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = noteColors.onSurface.copy(alpha = 0.4f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Metadata Row: Created Date • Reminder / Alarm (Pola A)
                    val createdDate = remember(state.createdAt) {
                        if (state.createdAt > 0L) Date(state.createdAt) else Date()
                    }
                    val formattedCreated = remember(createdDate) {
                        SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(createdDate)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = formattedCreated,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = noteColors.onSurface.copy(alpha = 0.45f)
                        )

                        if (state.hasReminder && state.reminderTime != null) {
                            val reminderDate = remember(state.reminderTime) { Date(state.reminderTime!!) }
                            val formattedReminder = remember(reminderDate) {
                                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(reminderDate)
                            }
                            val isAlarm = state.priority == 2

                            Text(
                                text = "  •  ",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = noteColors.onSurface.copy(alpha = 0.3f)
                            )
                            Icon(
                                imageVector = if (isAlarm) Icons.Default.Alarm else Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = noteColors.onSurface.copy(alpha = 0.55f)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = formattedReminder,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = noteColors.onSurface.copy(alpha = 0.55f)
                            )
                            if (state.repeatInterval != ReminderHelper.REPEAT_NONE && state.repeatInterval.isNotBlank()) {
                                Spacer(Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = noteColors.onSurface.copy(alpha = 0.55f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    if (isChecklistMode) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            checklistItems.forEach { item ->
                                val itemFocusRequester = remember(item.id) { FocusRequester() }
                                LaunchedEffect(focusItemId) {
                                    if (focusItemId == item.id) {
                                        itemFocusRequester.requestFocus()
                                        focusItemId = null
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            checklistItems = checklistItems.map {
                                                if (it.id == item.id) it.copy(isChecked = !item.isChecked) else it
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                            contentDescription = null,
                                            tint = if (item.isChecked) noteColors.primary else noteColors.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    BasicTextField(
                                        value = item.text,
                                        onValueChange = { newText ->
                                            if (newText.contains("\n")) {
                                                val lines = newText.split("\n")
                                                val firstPart = lines.firstOrNull().orEmpty()
                                                val secondPart = lines.drop(1).joinToString("\n")
                                                val newItem = ChecklistItem(text = secondPart, isChecked = false)
                                                val currentPos = checklistItems.indexOfFirst { it.id == item.id }
                                                val updated = checklistItems.toMutableList()
                                                if (currentPos >= 0) {
                                                    updated[currentPos] = item.copy(text = firstPart)
                                                    updated.add(currentPos + 1, newItem)
                                                } else {
                                                    updated.add(newItem)
                                                }
                                                checklistItems = updated
                                                focusItemId = newItem.id
                                            } else {
                                                checklistItems = checklistItems.map {
                                                    if (it.id == item.id) it.copy(text = newText) else it
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 6.dp)
                                            .focusRequester(itemFocusRequester)
                                            .onKeyEvent { keyEvent ->
                                                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace && item.text.isEmpty()) {
                                                    if (checklistItems.size > 1) {
                                                        val currentPos = checklistItems.indexOfFirst { it.id == item.id }
                                                        val prevItem = checklistItems.getOrNull(currentPos - 1)
                                                        checklistItems = checklistItems.filter { it.id != item.id }
                                                        if (prevItem != null) {
                                                            focusItemId = prevItem.id
                                                        }
                                                        return@onKeyEvent true
                                                    }
                                                }
                                                false
                                            },
                                        textStyle = TextStyle(
                                            fontSize = 16.sp,
                                            color = if (item.isChecked) noteColors.onSurface.copy(alpha = 0.45f) else noteColors.onSurface,
                                            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                            lineHeight = 22.sp
                                        ),
                                        cursorBrush = SolidColor(noteColors.primary),
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Sentences,
                                            autoCorrectEnabled = true,
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                val newItem = ChecklistItem(text = "", isChecked = false)
                                                val currentPos = checklistItems.indexOfFirst { it.id == item.id }
                                                val updated = checklistItems.toMutableList()
                                                if (currentPos >= 0) {
                                                    updated.add(currentPos + 1, newItem)
                                                } else {
                                                    updated.add(newItem)
                                                }
                                                checklistItems = updated
                                                focusItemId = newItem.id
                                            }
                                        ),
                                        decorationBox = { innerTextField ->
                                            if (item.text.isEmpty()) {
                                                Text(
                                                    text = strings.addChecklistItem,
                                                    fontSize = 16.sp,
                                                    color = noteColors.onSurface.copy(alpha = 0.35f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                    IconButton(
                                        onClick = {
                                            if (checklistItems.size > 1) {
                                                checklistItems = checklistItems.filter { it.id != item.id }
                                            } else {
                                                checklistItems = listOf(ChecklistItem())
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = strings.delete,
                                            tint = noteColors.onSurface.copy(alpha = 0.35f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Add Item Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newItem = ChecklistItem(text = "", isChecked = false)
                                        checklistItems = checklistItems + newItem
                                        focusItemId = newItem.id
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = noteColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = strings.addChecklistItem,
                                    fontSize = 15.sp,
                                    color = noteColors.primary.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // 2. Single Unified Rich Text Canvas
                        RichTextEditor(
                            state = richTextState,
                            placeholder = {
                                Text(
                                    strings.noteContentHint,
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = noteColors.onSurface.copy(alpha = 0.35f)
                                    )
                                )
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = noteColors.onSurface,
                                lineHeight = 24.sp
                            ),
                            colors = RichTextEditorDefaults.richTextEditorColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                cursorColor = noteColors.primary,
                                selectionColors = customTextSelectionColors
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }
                }
            }
        }
    }

    val dateOnlyFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
    val timeOnlyFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Reminder Pop-up Dialog
    if (showReminderDialog) {
        Dialog(
            onDismissRequest = { showReminderDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 420.dp)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Row: Icon + Label + Switch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val isDark = isAppInDarkTheme()
                        Icon(
                            imageVector = if (state.priority == 2 && state.hasReminder) Icons.Default.Alarm else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (state.hasReminder) ReminderBadgeColors.contentColor(isAlarm = state.priority == 2, isDark = isDark) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = strings.reminder,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        Switch(
                            checked = state.hasReminder,
                            onCheckedChange = { enabled ->
                                viewModel.setReminder(enabled)
                                if (enabled && (state.reminderTime == null || state.reminderTime!! <= System.currentTimeMillis())) {
                                    val nowCal = Calendar.getInstance().apply {
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    viewModel.setReminderTime(nowCal.timeInMillis)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                checkedBorderColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    // Expanded Options when Reminder is Enabled
                    if (state.hasReminder) {
                        Spacer(Modifier.height(16.dp))

                        // Split Date & Time Row (Compact Google Calendar Style)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Date Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .clickable(onClick = { showDatePicker = true })
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = dateOnlyFormat.format(Date(state.reminderTime ?: System.currentTimeMillis())),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Time Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = { showTimePicker = true })
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = timeOnlyFormat.format(Date(state.reminderTime ?: System.currentTimeMillis())),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Repeat Interval Selector (Google Calendar Style)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = { showRepeatMenu = true })
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = null,
                                        tint = if (state.repeatInterval != ReminderHelper.REPEAT_NONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = ReminderHelper.getRepeatLabel(state.repeatInterval, strings),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showRepeatMenu,
                                onDismissRequest = { showRepeatMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                val repeatOptions = listOf(
                                    ReminderHelper.REPEAT_NONE to strings.repeatNone,
                                    ReminderHelper.REPEAT_DAILY to strings.repeatDaily,
                                    ReminderHelper.REPEAT_WEEKLY to strings.repeatWeekly,
                                    ReminderHelper.REPEAT_MONTHLY to strings.repeatMonthly,
                                    ReminderHelper.REPEAT_YEARLY to strings.repeatYearly
                                )
                                repeatOptions.forEach { (optionKey, optionLabel) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = optionLabel,
                                                fontWeight = if (state.repeatInterval == optionKey) FontWeight.Bold else FontWeight.Normal,
                                                color = if (state.repeatInterval == optionKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            viewModel.setRepeatInterval(optionKey)
                                            showRepeatMenu = false
                                        },
                                        leadingIcon = if (state.repeatInterval == optionKey) {
                                            {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Priority Selection (Notification vs Alarm)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isDark = isAppInDarkTheme()
                            FilterChip(
                                selected = state.priority <= 1,
                                onClick = { viewModel.setPriority(1) },
                                modifier = Modifier.height(36.dp),
                                label = { Text(strings.notification, style = MaterialTheme.typography.labelMedium) },
                                leadingIcon = {
                                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = if (state.priority <= 1) BorderStroke(1.dp, ReminderBadgeColors.borderColor(isAlarm = false, isDark = isDark)) else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ReminderBadgeColors.containerColor(isAlarm = false, isDark = isDark),
                                    selectedLabelColor = ReminderBadgeColors.contentColor(isAlarm = false, isDark = isDark),
                                    selectedLeadingIconColor = ReminderBadgeColors.contentColor(isAlarm = false, isDark = isDark)
                                )
                            )

                            FilterChip(
                                selected = state.priority == 2,
                                onClick = { viewModel.setPriority(2) },
                                modifier = Modifier.height(36.dp),
                                label = { Text(strings.alarm, style = MaterialTheme.typography.labelMedium) },
                                leadingIcon = {
                                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = if (state.priority == 2) BorderStroke(1.dp, ReminderBadgeColors.borderColor(isAlarm = true, isDark = isDark)) else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ReminderBadgeColors.containerColor(isAlarm = true, isDark = isDark),
                                    selectedLabelColor = ReminderBadgeColors.contentColor(isAlarm = true, isDark = isDark),
                                    selectedLeadingIconColor = ReminderBadgeColors.contentColor(isAlarm = true, isDark = isDark)
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showReminderDialog = false }) {
                            Text(strings.save, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }

    // Google Calendar style separate Date & Time pickers
    if (showDatePicker) {
        val initialDateCal = remember(showDatePicker) {
            val localCal = Calendar.getInstance().apply {
                (state.reminderTime ?: System.currentTimeMillis()).let { timeInMillis = it }
            }
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(localCal.get(Calendar.YEAR), localCal.get(Calendar.MONTH), localCal.get(Calendar.DAY_OF_MONTH))
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateCal.timeInMillis
        )
        Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .widthIn(max = 400.dp)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = strings.selectDate,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                    DatePicker(
                        state = datePickerState,
                        title = null,
                        headline = null,
                        showModeToggle = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, end = 24.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(strings.cancel)
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { selectedDate ->
                                val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                    timeInMillis = selectedDate
                                }
                                val year = utcCal.get(Calendar.YEAR)
                                val month = utcCal.get(Calendar.MONTH)
                                val day = utcCal.get(Calendar.DAY_OF_MONTH)

                                val updatedCal = Calendar.getInstance().apply {
                                    (state.reminderTime ?: System.currentTimeMillis()).let { timeInMillis = it }
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, day)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                viewModel.setReminderTime(updatedCal.timeInMillis)
                                viewModel.setReminder(true)
                            }
                            showDatePicker = false
                        }) {
                            Text(strings.save, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val initialTimeCal = remember(showTimePicker) {
            Calendar.getInstance().apply {
                (state.reminderTime ?: System.currentTimeMillis()).let { timeInMillis = it }
            }
        }
        val timePickerState = rememberTimePickerState(
            initialHour = initialTimeCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = initialTimeCal.get(Calendar.MINUTE),
            is24Hour = false
        )
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .widthIn(max = 400.dp)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val remainingText = remember(timePickerState.hour, timePickerState.minute, state.reminderTime, state.priority, strings) {
                        ReminderHelper.formatRemainingTime(
                            targetHour = timePickerState.hour,
                            targetMinute = timePickerState.minute,
                            baseDateMillis = state.reminderTime,
                            isAlarm = state.priority == 2,
                            strings = strings
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = strings.selectTime,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = remainingText,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                            periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectorColor = MaterialTheme.colorScheme.primary,
                            clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text(strings.cancel)
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            val updatedCal = Calendar.getInstance().apply {
                                (state.reminderTime ?: System.currentTimeMillis()).let { timeInMillis = it }
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            viewModel.setReminderTime(updatedCal.timeInMillis)
                            viewModel.setReminder(true)
                            showTimePicker = false
                        }) {
                            Text(strings.save, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Color Swatch Picker Sheet (Option B)
    if (showColorPicker) {
        ModalBottomSheet(
            onDismissRequest = { showColorPicker = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = noteColors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = strings.noteColor,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(18.dp))

                val colorOptions = listOf(
                    NoteColorThemes.DEFAULT to strings.colorDefault,
                    NoteColorThemes.BLUE to strings.colorBlue,
                    NoteColorThemes.RED to strings.colorRed,
                    NoteColorThemes.YELLOW to strings.colorYellow,
                    NoteColorThemes.GREEN to strings.colorGreen
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorOptions) { (colorKey, colorLabel) ->
                        val themeSample = NoteColorThemes.getThemeColors(colorKey, isDark)
                        val isSelected = state.colorTheme == colorKey

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setColorTheme(colorKey)
                                    showColorPicker = false
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(themeSample.swatchColor)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        } else {
                                            Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (colorKey == NoteColorThemes.DEFAULT && !isDark) PureWhite else if (colorKey == NoteColorThemes.YELLOW && isDark) Color.Black else PureWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = colorLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormattingButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    activeContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    activeIconColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val containerColor = if (isActive) activeContainerColor else Color.Transparent
    val iconTint = if (isActive) activeIconColor else MaterialTheme.colorScheme.onSurfaceVariant

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

private const val CHECKLIST_MODE_HEADER = "<!--mode:checklist-->"

private fun isContentChecklist(content: String): Boolean {
    if (content.startsWith(CHECKLIST_MODE_HEADER)) return true
    val trimmed = content.trim()
    return trimmed.startsWith("<p>☐") || trimmed.startsWith("<p>☑") ||
           trimmed.startsWith("☐") || trimmed.startsWith("☑") ||
           trimmed.startsWith("[ ]") || trimmed.startsWith("[x]")
}

private fun parseChecklistItems(content: String): List<ChecklistItem> {
    val cleanHtml = content
        .removePrefix(CHECKLIST_MODE_HEADER)
        .replace("<br>", "\n")
        .replace("<br/>", "\n")
        .replace("<br />", "\n")
        .replace("</p>", "\n")
        .replace("</li>", "\n")
        .replace("</div>", "\n")
    val rawText = cleanHtml.replace(Regex("<[^>]*>"), "").trimEnd('\n')
    val lines = rawText.split("\n")
    val items = mutableListOf<ChecklistItem>()
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty() && lines.size > 1) continue
        val isChecked = trimmed.startsWith("☑") || trimmed.startsWith("[x]") || trimmed.startsWith("[X]") || trimmed.startsWith("✓")
        val cleanLine = when {
            trimmed.startsWith("☑ ") || trimmed.startsWith("☐ ") -> trimmed.substring(2)
            trimmed.startsWith("☑") || trimmed.startsWith("☐") -> trimmed.substring(1)
            trimmed.startsWith("[x] ") || trimmed.startsWith("[X] ") || trimmed.startsWith("[ ] ") -> trimmed.substring(4)
            trimmed.startsWith("[x]") || trimmed.startsWith("[X]") || trimmed.startsWith("[ ]") -> trimmed.substring(3)
            else -> line
        }
        items.add(ChecklistItem(text = cleanLine, isChecked = isChecked))
    }
    return if (items.isEmpty()) listOf(ChecklistItem()) else items
}

private fun checklistToHtml(items: List<ChecklistItem>): String {
    val sb = StringBuilder(CHECKLIST_MODE_HEADER)
    for (item in items) {
        val prefix = if (item.isChecked) "☑ " else "☐ "
        sb.append("<p>").append(prefix).append(item.text).append("</p>")
    }
    return sb.toString()
}


