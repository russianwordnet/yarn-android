package net.russianword.android.utils

import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Utils for interaction with RxJava/RxKotlin.
 *
 * Created by igushs on 12/5/15.
 */

/**
 * Provides an observable which issues the same items, but is subscribed in a background thread and observed in
 * Android main thread.
 *
 * @return observable that works asynchronously
 */
public fun <T> Observable<T>.asAsync() = subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())