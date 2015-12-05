package net.russianword.android.utils

import android.support.design.widget.NavigationView
import android.view.ViewManager
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
