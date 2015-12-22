package net.russianword.android

import android.support.v4.app.Fragment

/**
 * Created by igushs on 12/15/15.
 */

public fun fragmentToProcessId(f: Fragment?) = when (f) {
    is SentencesFragment -> "sentences"
    else -> null
}