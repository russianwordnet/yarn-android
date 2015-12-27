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
 * Provides an [Observable] which issues the same items, but is subscribed in a background thread and observed in
 * Android main thread.
 *
 * @return observable that works asynchronously
 */
public fun <T> Observable<T>.asAsync() = subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())

/**
 * @return [Observable] that will handle [Throwable] of [E] with passed [handler] and emit no items after.
 */
public inline fun <reified E : Throwable, R> Observable<R>.handleError(crossinline handler: (E) -> Unit) =
        onErrorResumeNext f@{ e ->
            return@f when (e) {
                is E -> { handler(e); Observable.empty() }
                else -> Observable.error(e)
            }
        }

/**
 * @return [Observable] that will handle [Throwable] of [E] with passed [handler] and resume with another [Observable]
 *         returned by it.]
 */
public inline fun <reified E : Throwable, R> Observable<R>.handleErrorThen(crossinline handler: (E) -> Observable<R>) =
        onErrorResumeNext f@{ e ->
            return@f when (e) {
                is E -> handler(e)
                else -> Observable.error(e)
            }
        }

public inline fun <T> retryOnAnyError(crossinline fObservable: () -> Observable<T>) =
        fObservable().onErrorResumeNext {
            fObservable()
        }