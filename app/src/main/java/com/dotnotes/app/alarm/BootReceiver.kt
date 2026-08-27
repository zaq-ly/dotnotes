package com.dotnotes.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dotnotes.app.DotNotesApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val scheduler = AlarmScheduler(context)
        val repo = DotNotesApp.instance.repository

        CoroutineScope(Dispatchers.IO).launch {
            repo.getNotesWithActiveReminders().forEach { note ->
                if (note.reminderTime != null && note.reminderTime > System.currentTimeMillis()) {
                    scheduler.schedule(note)
                }
            }
        }
    }
}
