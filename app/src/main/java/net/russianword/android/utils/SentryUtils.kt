package net.russianword.android.utils

import com.joshdholtz.sentry.Sentry
import rx.Observable
import rx.lang.kotlin.onError

/**
 * Utils for interaction with Sentry.
 *
 * Created by igushs on 12/9/15.
 */

public fun <T> Observable<T>.withSentry() = onError { Sentry.captureException(it) }