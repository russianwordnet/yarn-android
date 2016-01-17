package net.russianword.android.utils

class ListMultiMap<K, V>(
        val content: Map<K, List<V>>
) : Map<K, V> {

    private class SimpleMapEntry<K, V>(override val key: K,
                                       override val value: V
    ) : Map.Entry<K, V>

    override val entries: Set<Map.Entry<K, V>> = content.flatMap {
        val k = it.key
        it.value.map { SimpleMapEntry(k, it) }
    }.toSet()

    override val keys: Set<K> = content.keys
    override val size: Int = content.entries.sumBy { it.value.size }
    override val values: Collection<V> = content.flatMap { it.value }

    override fun containsKey(key: K): Boolean = key in content

    override fun containsValue(value: V): Boolean = value in values

    override fun get(key: K): V? = content[key]?.lastOrNull()

    override fun isEmpty(): Boolean = content.isEmpty()

}