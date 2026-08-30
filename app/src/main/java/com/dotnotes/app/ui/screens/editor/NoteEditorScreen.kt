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
    val richTextState = rememberRichTextState()

    var blocks by remember { mutableStateOf<List<NoteBlock>>(listOf(NoteBlock())) }
    var focusedBlockId by remember { mutableStateOf<String?>(null) }
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }

    var showReminderDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
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

    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    )

    fun saveAndExit() {
        viewModel.updateContent(blocksToHtml(blocks))
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
                    val currentFocusedIndex = blocks.indexOfFirst { it.id == focusedBlockId }.let { if (it in blocks.indices) it else 0 }
                    val currentBlock = blocks.getOrNull(currentFocusedIndex) ?: blocks.firstOrNull() ?: NoteBlock()

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
                                // 1. Checkbox
                                FormattingButton(
                                    icon = if (currentBlock.type == BlockType.CHECKLIST) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = "Checklist",
                                    isActive = currentBlock.type == BlockType.CHECKLIST,
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
                        Icon(
                            imageVector = if (state.priority == 2 && state.hasReminder) Icons.Default.Alarm else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (state.hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                            thumbContent = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                checkedBorderColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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

                        Spacer(Modifier.height(16.dp))

                        // Priority Selection (Notification vs Alarm)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = state.priority <= 1,
                                onClick = { viewModel.setPriority(1) },
                                modifier = Modifier.height(36.dp),
                                label = { Text(strings.notification, style = MaterialTheme.typography.labelMedium) },
                                leadingIcon = {
                                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary
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
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.error,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.error
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
            Calendar.getInstance().apply {
                (state.reminderTime ?: System.currentTimeMillis()).let { timeInMillis = it }
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
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 380.dp)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = strings.selectDate,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
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
                            .padding(top = 8.dp, end = 8.dp),
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
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 380.dp)
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
                    TimePicker(state = timePickerState)
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
                        checkedColor = MaterialTheme.colorScheme.primary,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.primary
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
                        color = MaterialTheme.colorScheme.primary
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
                    // Soft keyboard Backspace deleted the sentinel on empty field!
                    onBackspaceOnEmpty()
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
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                else
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (block.type == BlockType.CHECKLIST && block.isChecked)
                    TextDecoration.LineThrough
                else
                    TextDecoration.None
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
                innerTextField()
            }
        )
    }
}
