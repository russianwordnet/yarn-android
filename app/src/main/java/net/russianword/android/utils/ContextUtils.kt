package net.russianword.android.utils

import android.content.Context
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.TypedValue
import net.russianword.android.R

/**
 * Utils for working with [Context].
 *
 * Created by igushs on 12/5/15.
 */

public fun Context.getActionBarSize(): Int {
    val tv = TypedValue();
    if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics())
    }
    Log.wtf("ContextUtils", "android.R.attr.actionBarSize cannot be found")
    throw RuntimeException("android.R.attr.actionBarSize cannot be found")
}

public fun Context.getActionBarColor(): Int = ContextCompat.getColor(this, R.color.logo_color)

public fun Context.getAndroidId() = Settings.Secure.getString(getContentResolver(),
                                                              Settings.Secure.ANDROID_ID);