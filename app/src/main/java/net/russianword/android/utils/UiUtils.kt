package net.russianword.android.utils

import android.content.Context
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Pair
import android.util.TypedValue
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.Button
import net.russianword.android.R

/**
 * Created by igushs on 12/5/15.
 */

fun Button.makeBorderless() {
    val value = TypedValue()
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, value, true)
    setBackgroundResource(value.resourceId)
}

fun View.expandTouchAreaToParent() {
    post {
        val parent = parent
        if (parent !is View) return@post
        val hitRect = Rect()
        getHitRect(hitRect)
        hitRect.left = 0
        hitRect.right = parent.width
        hitRect.top = 0
        hitRect.bottom = parent.height
        parent.touchDelegate = TouchDelegate(hitRect, this)
    }
}

tailrec fun disableClip(v: ViewParent) {
    val parent = v.parent
    if (parent is ViewGroup) {
        parent.clipChildren = false
        parent.clipToPadding = false
    }
    if (parent != null)
        disableClip(parent)
}

fun Context.spanAsterisksWithAccentColor(s: CharSequence): CharSequence {
    val color = ContextCompat.getColor(this, R.color.accent)
    val asterisks = s.mapIndexed { i, c -> Pair(i, c) }.filter { it.second == '*' }.map { it.first }
    if (asterisks.size == 2) {
        val removedAsterisks = SpannableString(s.filter { it != '*' })
        removedAsterisks.setSpan(ForegroundColorSpan(color), asterisks[0], asterisks[1] - 1,
                                 Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return removedAsterisks
    }
    return s
}