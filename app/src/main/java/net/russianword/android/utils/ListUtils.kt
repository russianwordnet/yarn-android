package net.russianword.android.utils

/**
 * Created by igushs on 4/26/16.
 */

fun <T> Iterable<T>.batch(batchSize: Int): Iterable<Iterable<T>> = object : Iterable<Iterable<T>> {
    override fun iterator(): Iterator<Iterable<T>> {
        val originalIterator = this@batch.iterator()
        return generateSequence {
            val batch = originalIterator.asSequence().take(batchSize).toList()
            if (batch.isEmpty()) null else batch
        }.iterator()
    }
}