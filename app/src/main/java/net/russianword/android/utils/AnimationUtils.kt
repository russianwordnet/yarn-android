package net.russianword.android.utils

import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation

/**
 * Created by igushs on 12/22/15.
 */

const val APPEAR_DURATION = 300L
const val DISAPPEAR_DURATION = 200L
const val APPEAR_DELTA_Y = 600
const val DISAPPEAR_DELTA_Y = 200

public fun View.disappearToTop() = animateDisappear(0, -DISAPPEAR_DELTA_Y)

public fun View.animateDisappear(toX: Int, toY: Int) {
    val translate = TranslateAnimation(0f, toX.toFloat(), 0f, toY.toFloat())
    val alpha = AlphaAnimation(1.0f, 0.0f)

    val animationSet = AnimationSet(true)
    listOf(translate, alpha).forEach {
        it.duration = DISAPPEAR_DURATION
        it.isFillEnabled = true
        it.fillAfter = true
        animationSet.addAnimation(it)
    }

    animationSet.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            (parent as? ViewGroup)?.let { it.post { it.removeView(this@animateDisappear) } }
        }

        override fun onAnimationRepeat(animation: Animation?) = Unit
        override fun onAnimationStart(animation: Animation?) = Unit
    })

    startAnimation(animationSet)
}

public fun View.appearFromBottom() = animateAppear(0, APPEAR_DELTA_Y)

public fun View.animateAppear(fromX: Int, fromY: Int) {
    val translate = TranslateAnimation(fromX.toFloat(), 0f, fromY.toFloat(), 0f)
    val alpha = AlphaAnimation(0f, 1f)

    val animationSet = AnimationSet(true)
    listOf(translate, alpha).forEach {
        it.duration = APPEAR_DURATION
        it.fillAfter = true
        it.fillBefore = true
        animationSet.addAnimation(it)
    }

    startAnimation(animationSet)
}