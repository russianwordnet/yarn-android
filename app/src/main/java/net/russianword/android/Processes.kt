package net.russianword.android

import android.support.v4.app.Fragment
import org.jetbrains.anko.support.v4.withArguments

fun fragmentToProcessId(f: Fragment?) = when (f) {
    is SentencesFragment ->
        f.arguments[STAGE_KEY] as? String ?: throw IllegalStateException("No STAGE_KEY in arguments")
    else -> null
}

const val STAGE_KEY = "stage"

fun processToFragment(processId: String) = when (processId) {
    "sentences", "substitutions" -> SentencesFragment().withArguments(STAGE_KEY to processId)
    else -> throw IllegalArgumentException("No such fragment")
}