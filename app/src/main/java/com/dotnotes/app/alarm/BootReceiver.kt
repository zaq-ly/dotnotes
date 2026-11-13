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
        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON"
        )
        if (intent.action !in validActions) return

        val pendingResult = goAsync()
        val scheduler = AlarmScheduler(context)
        val repo = DotNotesApp.instance.repository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                repo.getNotesWithActiveReminders().forEach { note ->
                    val time = note.reminderTime ?: return@forEach
                    if (time > now) {
                        scheduler.schedule(note)
                    } else {
                        scheduler.triggerNow(note)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
