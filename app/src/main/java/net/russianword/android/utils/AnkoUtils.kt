package net.russianword.android.utils

import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.view.ViewManager
import net.russianword.android.R
import org.jetbrains.anko.custom.ankoView

/**
 * Utils for Anko DSL.
 *
 * Created by igushs on 12/5/15.
 */

/**
 * Creates a [NavigationView] with no additional initialization.
 */
public fun ViewManager.navigationView() = navigationView { }

/**
 * DSL method to create a [NavigationView] and initialize it.
 */
public inline fun ViewManager.navigationView(init: NavigationView.() -> Unit) = ankoView({ NavigationView(it) }, init)

/**
 * Creates a [CardView] with the default background color.
 */
public  fun ViewManager.cardView(init: CardView.() -> Unit) {
    ankoView({ CardView(it) }) {
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background_color))
        init()
    }
}