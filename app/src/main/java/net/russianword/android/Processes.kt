package net.russianword.android

import android.support.v4.app.Fragment

public fun fragmentToProcessId(f: Fragment?) = when (f) {
    is SentencesFragment -> "sentences"
    else -> null
}

public fun processToFragment(processId: String) = when (processId) {
    "sentences" -> SentencesFragment()
    else -> throw IllegalArgumentException("No such fragment")
}