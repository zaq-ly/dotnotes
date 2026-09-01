package com.dotnotes.app

import com.dotnotes.app.data.model.Note
import com.dotnotes.app.data.model.previewText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteModelTest {

    @Test
    fun previewText_stripsHtmlTags() {
        val note = Note(
            id = "test-1",
            title = "HTML Note",
            content = "<p><strong>Bold Title</strong> and <em>italic text</em></p>"
        )
        assertEquals("Bold Title and italic text", note.previewText)
    }

    @Test
    fun previewText_handlesLineBreaks() {
        val note = Note(
            id = "test-2",
            title = "Multiline Note",
            content = "<p>First Line</p><br/><p>Second Line</p>"
        )
        assertEquals("First Line", note.previewText)
    }

    @Test
    fun previewText_handlesEmptyContent() {
        val note = Note(id = "test-3", title = "Empty Note", content = "")
        assertEquals("", note.previewText)
    }

    @Test
    fun note_defaultValues_areCorrect() {
        val note = Note(title = "Default Check", content = "Content")
        assertTrue(note.id.isNotEmpty())
        assertFalse(note.isPinned)
        assertEquals(0, note.priority)
        assertFalse(note.isAlarmDismissed)
        assertEquals("DEFAULT", note.colorTheme)
        assertFalse(note.isDeleted)
        assertTrue(note.createdAt > 0)
        assertTrue(note.updatedAt > 0)
    }
}
