package com.dotnotes.app.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dotnotes.app.DotNotesApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tagName = intent.getStringExtra(EXTRA_TAG_NAME) ?: return
        CoroutineScope(Dispatchers.IO).launch {
            DotNotesApp.instance.settingsDataStore.setDismissedUpdateTag(tagName)
        }
    }

    companion object {
        const val EXTRA_TAG_NAME = "extra_tag_name"
    }
}
