package com.notifymind.filter

import android.content.Context
import kotlin.math.ln

class Predictor(context: Context) {
    private val prefs = context.getSharedPreferences("notifymind", Context.MODE_PRIVATE)
    private val db = AppDatabase.get(context)

    fun getThreshold(): Float = prefs.getFloat("threshold", 0.7f)

    suspend fun predict(item: NotificationItem): Float {
        val votes = db.dao().getVotes()
        if (votes.size < 10) return 0.5f // not enough data

        val keep = votes.filter { it.vote == 1 }
        val dismiss = votes.filter { it.vote == 0 }

        val words = tokenize(item)
        var logKeep = ln(keep.size.toDouble() / votes.size)
        var logDismiss = ln(dismiss.size.toDouble() / votes.size)

        val vocab = (keep + dismiss).flatMap { tokenize(it) }.toSet().size

        words.forEach { w ->
            val kCount = keep.count { tokenize(it).contains(w) } + 1
            val dCount = dismiss.count { tokenize(it).contains(w) } + 1
            logKeep += ln(kCount.toDouble() / (keep.size + vocab))
            logDismiss += ln(dCount.toDouble() / (dismiss.size + vocab))
        }

        val pKeep = 1 / (1 + Math.exp(logDismiss - logKeep))
        return pKeep.toFloat()
    }

    private fun tokenize(item: NotificationItem): List<String> {
        val text = "${item.packageName} ${item.title} ${item.text}".lowercase()
        return text.split(Regex("\W+")).filter { it.length > 2 }
    }
}
