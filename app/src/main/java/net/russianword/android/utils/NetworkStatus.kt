package net.russianword.android.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.jetbrains.anko.connectivityManager

public fun Context.onNetworkStateChange(handler: (NetworkInfo) -> Unit): BroadcastReceiver {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) =
                connectivityManager.activeNetworkInfo?.let { handler(it) } ?: Unit
    }

    val filter = IntentFilter()
    filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
    registerReceiver(receiver, filter)

    return receiver
}