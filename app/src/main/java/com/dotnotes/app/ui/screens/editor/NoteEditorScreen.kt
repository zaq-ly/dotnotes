package com.dotnotes.app.ui.screens.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.BorderStroke
import com.dotnotes.app.ui.theme.NoteColorThemes
import com.dotnotes.app.ui.theme.NoteThemeColors
import com.dotnotes.app.ui.theme.PureWhite
import com.dotnotes.app.ui.theme.ReminderBadgeColors
import com.dotnotes.app.alarm.ReminderHelper
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
import androidx.compose.material3.TimePickerDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
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
import java.util.TimeZone
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
    val isDark = isSystemInDarkTheme()
    val noteColors = remember(state.colorTheme, isDark) {
        NoteColorThemes.getThemeColors(state.colorTheme, isDark)
    }
    val richTextState = rememberRichTextState()

    var blocks by remember { mutableStateOf<List<NoteBlock>>(listOf(NoteBlock())) }
    var focusedBlockId by remember { mutableStateOf<String?>(null) }
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    var showReminderDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showRepeatMenu by remember { mutableStateOf(false) }
    var isContentInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && !isContentInitialized) {
            if (state.content.isNotEmpty()) {
                blocks = parseContentToBlocks(state.content)
            }
            isContentInitialized = true
        }
    }

    LaunchedEffect(focusedBlockId) {
        focusedBlockId?.let { id ->
            try {
                focusRequesters[id]?.requestFocus()
            } catch (e: Exception) {
                // Prevent crash if layout is attaching/detaching
            }
        }
    }

    val customTextSelectionColors = remember(noteColors.primary) {
        TextSelectionColors(
            handleColor = noteColors.primary,
            backgroundColor = noteColors.primary.copy(alpha = 0.35f)
        )
    }

    fun saveAndExit() {
        viewModel.updateContent(blocksToHtml(blocks))
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
                    val currentFocusedIndex = blocks.indexOfFirst { it.id == focusedBlockId }.let { if (it in blocks.indices) it else 0 }
                    val currentBlock = blocks.getOrNull(currentFocusedIndex) ?: blocks.firstOrNull() ?: NoteBlock()

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
                                // 1. Checkbox
                                FormattingButton(
                                    icon = if (currentBlock.type == BlockType.CHECKLIST) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = "Checklist",
                                    isActive = currentBlock.type == BlockType.CHECKLIST,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        val idx = blocks.indexOfFirst { it.id == focusedBlockId }.let { if (it in blocks.indices) it else 0 }
                                        if (idx in blocks.indices) {
                                            val updated = blocks.toMutableList()
                                            val target = updated[idx]
                                            updated[idx] = target.copy(
                                                type = if (target.type == BlockType.CHECKLIST) BlockType.PARAGRAPH else BlockType.CHECKLIST,
                                                isChecked = false
                                            )
                                            blocks = updated
                                        }
                                    }
                                )

                                // 2. Bold
                                FormattingButton(
                                    icon = Icons.Default.FormatBold,
                                    contentDescription = "Bold",
                                    isActive = currentBlock.isBold,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        val idx = blocks.indexOfFirst { it.id == focusedBlockId }.let { if (it in blocks.indices) it else 0 }
                                        if (idx in blocks.indices) {
                                            val updated = blocks.toMutableList()
                                            val target = updated[idx]
                                            updated[idx] = target.copy(isBold = !target.isBold)
                                            blocks = updated
                                        }
                                    }
                                )

                                // 3. Italic
                                FormattingButton(
                                    icon = Icons.Default.FormatItalic,
                                    contentDescription = "Italic",
                                    isActive = currentBlock.isItalic,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        val idx = blocks.indexOfFirst { it.id == focusedBlockId }.let { if (it in blocks.indices) it else 0 }
                                        if (idx in blocks.indices) {
                                            val updated = blocks.toMutableList()
                                            val target = updated[idx]
                                            updated[idx] = target.copy(isItalic = !target.isItalic)
                                            blocks = updated
                                        }
                                    }
                                )

                                // 4. Bulleted List
                                FormattingButton(
                                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                                    contentDescription = "Bullet List",
                                    isActive = currentBlock.type == BlockType.BULLET,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        val idx = blocks.indexOfFirst { it.id == focusedBlockId }.let { if (it in blocks.indices) it else 0 }
                                        if (idx in blocks.indices) {
                                            val updated = blocks.toMutableList()
                                            val target = updated[idx]
                                            updated[idx] = target.copy(
                                                type = if (target.type == BlockType.BULLET) BlockType.PARAGRAPH else BlockType.BULLET
                                            )
                                            blocks = updated
                                        }
                                    }
                                )

                                // 5. Numbered List
                                FormattingButton(
                                    icon = Icons.Default.FormatListNumbered,
                                    contentDescription = "Numbered List",
                                    isActive = currentBlock.type == BlockType.NUMBERED,
                                    activeContainerColor = noteColors.primary.copy(alpha = 0.18f),
                                    activeIconColor = noteColors.primary,
                                    onClick = {
                                        val idx = blocks.indexOfFirst { it.id == focusedBlockId }.let { if (it in blocks.indices) it else 0 }
                                        if (idx in blocks.indices) {
                                            val updated = blocks.toMutableList()
                                            val target = updated[idx]
                                            updated[idx] = target.copy(
                                                type = if (target.type == BlockType.NUMBERED) BlockType.PARAGRAPH else BlockType.NUMBERED
                                            )
                                            blocks = updated
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Minimal Neutral Reminder Indicator (Above Title, Clickable)
                    if (state.hasReminder && state.reminderTime != null) {
                        val reminderDate = Date(state.reminderTime!!)
                        val formattedReminder = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(reminderDate)
                        val isAlarm = state.priority == 2
                        val isDark = isSystemInDarkTheme()

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Color(0xFF27272A) else Color(0xFFF4F4F5),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF3F3F46) else Color(0xFFE4E4E7)),
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 4.dp)
                                .clickable { showReminderDialog = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAlarm) Icons.Default.Alarm else Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = formattedReminder,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color(0xFFD4D4D8) else Color(0xFF52525B)
                                )
                                if (state.repeatInterval != ReminderHelper.REPEAT_NONE && state.repeatInterval.isNotBlank()) {
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                        tint = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
                                    )
                                }
                            }
                        }
                    }

                    // 1. Title Input
                    TextField(
                        value = state.title,
                        onValueChange = viewModel::updateTitle,
                        placeholder = {
                            Text(
                                strings.noteTitleHint,
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = noteColors.onSurface.copy(alpha = 0.4f)
                                )
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = noteColors.onSurface
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

                    // 2. Unified Note Canvas (Text paragraphs + Checklists + Lists)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        blocks.forEachIndexed { index, block ->
                            val requester = focusRequesters.getOrPut(block.id) { FocusRequester() }
                            val numberedIndex = if (block.type == BlockType.NUMBERED) getNumberedIndex(blocks, index) else 1

                            NoteBlockRow(
                                block = block,
                                numberedIndex = numberedIndex,
                                noteColors = noteColors,
                                focusRequester = requester,
                                onFocus = { focusedBlockId = block.id },
                                onTextChange = { newText ->
                                    val curIndex = blocks.indexOfFirst { it.id == block.id }
                                    if (curIndex != -1) {
                                        if (newText.contains("\n")) {
                                            val parts = newText.split("\n")
                                            val updated = blocks.toMutableList()
                                            val firstPart = parts[0]
                                            if (block.type != BlockType.PARAGRAPH && firstPart.isEmpty()) {
                                                updated[curIndex] = block.copy(text = "", type = BlockType.PARAGRAPH)
                                                val newBlock = NoteBlock(text = "", type = BlockType.PARAGRAPH)
                                                updated.add(curIndex + 1, newBlock)
                                                blocks = updated
                                                focusedBlockId = newBlock.id
                                            } else {
                                                updated[curIndex] = block.copy(text = firstPart)
                                                val nextType = block.type
                                                var lastAddedId = block.id
                                                for (i in 1 until parts.size) {
                                                    val newBlock = NoteBlock(
                                                        text = parts[i],
                                                        type = nextType,
                                                        isChecked = false,
                                                        isBold = block.isBold,
                                                        isItalic = block.isItalic
                                                    )
                                                    updated.add(curIndex + i, newBlock)
                                                    lastAddedId = newBlock.id
                                                }
                                                blocks = updated
                                                focusedBlockId = lastAddedId
                                            }
                                        } else {
                                            val updated = blocks.toMutableList()
                                            updated[curIndex] = block.copy(text = newText)
                                            blocks = updated
                                        }
                                    }
                                },
                                onCheckedChange = { checked ->
                                    val curIndex = blocks.indexOfFirst { it.id == block.id }
                                    if (curIndex != -1) {
                                        val updated = blocks.toMutableList()
                                        updated[curIndex] = block.copy(isChecked = checked)
                                        blocks = updated
                                    }
                                },
                                onBackspaceOnEmpty = {
                                    val curIndex = blocks.indexOfFirst { it.id == block.id }
                                    if (curIndex != -1) {
                                        val updated = blocks.toMutableList()
                                        if (block.type != BlockType.PARAGRAPH) {
                                            updated[curIndex] = block.copy(type = BlockType.PARAGRAPH, text = "")
                                            blocks = updated
                                            focusedBlockId = block.id
                                        } else if (blocks.size > 1) {
                                            val prevIndex = (curIndex - 1).coerceAtLeast(0)
                                            val prevId = blocks[prevIndex].id
                                            updated.removeAt(curIndex)
                                            blocks = updated
                                            focusedBlockId = prevId
                                        }
                                    }
                                }
                            )
                        }

                        Spacer(Modifier.height(48.dp))
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
                        val isDark = isSystemInDarkTheme()
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
                            val isDark = isSystemInDarkTheme()
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
                    Text(
                        text = strings.selectTime,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
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

enum class BlockType {
    PARAGRAPH,
    CHECKLIST,
    BULLET,
    NUMBERED
}

data class NoteBlock(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val type: BlockType = BlockType.PARAGRAPH,
    val isChecked: Boolean = false,
    val isBold: Boolean = false,
    val isItalic: Boolean = false
)

private fun getNumberedIndex(blocks: List<NoteBlock>, index: Int): Int {
    var count = 1
    var i = index - 1
    while (i >= 0 && i < blocks.size && blocks[i].type == BlockType.NUMBERED) {
        count++
        i--
    }
    return count
}

private fun parseContentToBlocks(content: String): List<NoteBlock> {
    if (content.isEmpty()) {
        return listOf(NoteBlock())
    }
    val cleanHtml = content
        .replace("<br>", "\n")
        .replace("<br/>", "\n")
        .replace("<br />", "\n")
        .replace("</p>", "\n")
        .replace("</li>", "\n")
        .replace("</div>", "\n")
    val rawText = cleanHtml.replace(Regex("<[^>]*>"), "").trimEnd('\n')
    val lines = rawText.split("\n")
    val blocks = mutableListOf<NoteBlock>()
    for (line in lines) {
        val trimmed = line.trim()
        val isChecked = trimmed.startsWith("☑") || trimmed.startsWith("[x]") || trimmed.startsWith("[X]") || trimmed.startsWith("✓")
        val isUnchecked = trimmed.startsWith("☐") || trimmed.startsWith("[ ]")
        val isBullet = trimmed.startsWith("•") || trimmed.startsWith("- ") || trimmed.startsWith("* ")
        val isNumbered = trimmed.matches(Regex("^\\d+\\..*"))

        val type = when {
            isChecked || isUnchecked -> BlockType.CHECKLIST
            isBullet -> BlockType.BULLET
            isNumbered -> BlockType.NUMBERED
            else -> BlockType.PARAGRAPH
        }

        val cleanLine = when {
            trimmed.startsWith("☑ ") || trimmed.startsWith("☐ ") -> trimmed.substring(2)
            trimmed.startsWith("☑") || trimmed.startsWith("☐") -> trimmed.substring(1)
            trimmed.startsWith("[x] ") || trimmed.startsWith("[X] ") || trimmed.startsWith("[ ] ") -> trimmed.substring(4)
            trimmed.startsWith("[x]") || trimmed.startsWith("[X]") || trimmed.startsWith("[ ]") -> trimmed.substring(3)
            trimmed.startsWith("• ") || trimmed.startsWith("- ") || trimmed.startsWith("* ") -> trimmed.substring(2)
            trimmed.startsWith("•") -> trimmed.substring(1)
            isNumbered -> trimmed.substringAfter(". ").substringAfter(".")
            else -> line
        }

        val isBold = line.contains("<b>") || line.contains("<strong>")
        val isItalic = line.contains("<i>") || line.contains("<em>")

        blocks.add(NoteBlock(text = cleanLine, type = type, isChecked = isChecked, isBold = isBold, isItalic = isItalic))
    }
    return if (blocks.isEmpty()) listOf(NoteBlock()) else blocks
}

private fun blocksToHtml(blocks: List<NoteBlock>): String {
    val sb = StringBuilder()
    for (b in blocks) {
        var styledText = b.text.replace("\u200B", "")
        if (b.isBold) styledText = "<b>$styledText</b>"
        if (b.isItalic) styledText = "<i>$styledText</i>"

        when (b.type) {
            BlockType.CHECKLIST -> {
                val prefix = if (b.isChecked) "☑ " else "☐ "
                sb.append("<p>").append(prefix).append(styledText).append("</p>")
            }
            BlockType.BULLET -> {
                sb.append("<p>• ").append(styledText).append("</p>")
            }
            BlockType.NUMBERED -> {
                sb.append("<p>1. ").append(styledText).append("</p>")
            }
            BlockType.PARAGRAPH -> {
                sb.append("<p>").append(styledText).append("</p>")
            }
        }
    }
    return sb.toString()
}

@Composable
private fun NoteBlockRow(
    block: NoteBlock,
    numberedIndex: Int,
    noteColors: NoteThemeColors,
    focusRequester: FocusRequester,
    onFocus: () -> Unit,
    onTextChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onBackspaceOnEmpty: () -> Unit
) {
    // When text is empty, seed with zero-width space (\u200B) so Android soft keyboard backspace always triggers
    val initialText = if (block.text.isEmpty()) "\u200B" else block.text
    var textValue by remember(block.id, block.text) {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (block.type == BlockType.CHECKLIST) 1.dp else 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (block.type) {
            BlockType.CHECKLIST -> {
                Checkbox(
                    checked = block.isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = noteColors.primary,
                        checkmarkColor = noteColors.onPrimary,
                        uncheckedColor = noteColors.onSurface.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            BlockType.BULLET -> {
                Text(
                    text = "•",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = noteColors.primary
                    ),
                    modifier = Modifier.padding(start = 6.dp, end = 10.dp)
                )
            }
            BlockType.NUMBERED -> {
                Text(
                    text = "$numberedIndex.",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = noteColors.primary
                    ),
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                )
            }
            BlockType.PARAGRAPH -> {}
        }

        BasicTextField(
            value = textValue,
            onValueChange = { newTfv ->
                val raw = newTfv.text
                if (raw.contains("\n")) {
                    // Enter key pressed
                    val cleaned = raw.replace("\u200B", "")
                    onTextChange(cleaned)
                } else if (raw.isEmpty()) {
                    if (block.text.isEmpty()) {
                        // Soft keyboard Backspace deleted the sentinel on empty field!
                        onBackspaceOnEmpty()
                    } else {
                        // User deleted last character: reset text to empty and restore sentinel
                        textValue = TextFieldValue(text = "\u200B", selection = TextRange(1))
                        onTextChange("")
                    }
                } else {
                    val cleaned = raw.replace("\u200B", "")
                    val safeText = if (cleaned.isEmpty()) "\u200B" else cleaned
                    val safeSelection = TextRange(
                        newTfv.selection.start.coerceIn(0, safeText.length),
                        newTfv.selection.end.coerceIn(0, safeText.length)
                    )
                    textValue = newTfv.copy(
                        text = safeText,
                        selection = safeSelection
                    )
                    onTextChange(cleaned)
                }
            },
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onFocus() }
                .onKeyEvent { keyEvent ->
                    // Hardware backspace fallback
                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace && (block.text.isEmpty() || textValue.text == "\u200B")) {
                        onBackspaceOnEmpty()
                        true
                    } else {
                        false
                    }
                },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = if (block.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (block.isItalic) FontStyle.Italic else FontStyle.Normal,
                color = if (block.type == BlockType.CHECKLIST && block.isChecked)
                    noteColors.onSurface.copy(alpha = 0.45f)
                else
                    noteColors.onSurface,
                textDecoration = if (block.type == BlockType.CHECKLIST && block.isChecked)
                    TextDecoration.LineThrough
                else
                    TextDecoration.None
            ),
            cursorBrush = SolidColor(noteColors.primary),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default
            ),
            decorationBox = { innerTextField ->
                val actual = textValue.text.replace("\u200B", "")
                if (actual.isEmpty()) {
                    Text(
                        text = when (block.type) {
                            BlockType.CHECKLIST -> "Item checklist..."
                            BlockType.BULLET -> "Daftar butir..."
                            BlockType.NUMBERED -> "Daftar bernomor..."
                            BlockType.PARAGRAPH -> "Tulis catatan..."
                        },
                        fontSize = 16.sp,
                        color = noteColors.onSurface.copy(alpha = 0.35f)
                    )
                }
                innerTextField()
            }
        )
    }
}
