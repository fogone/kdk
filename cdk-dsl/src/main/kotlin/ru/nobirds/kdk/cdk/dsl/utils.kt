package ru.nobirds.kdk.cdk.dsl

class ItemsBuilder<T>(private val items: MutableList<T> = mutableListOf()) {
    fun item(item: T) {
        this.items.add(item)
    }

    fun build(): List<T> = items
}

inline fun <T> buildItems(builder: @CdkDsl ItemsBuilder<T>.() -> Unit): List<T> {
    return ItemsBuilder<T>().apply(builder).build()
}
