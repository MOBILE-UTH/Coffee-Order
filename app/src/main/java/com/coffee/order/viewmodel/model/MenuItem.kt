package com.coffee.order.viewmodel.model

data class MenuItem(
    val menuItemId: Long = 0L,
    val name: String,
    val category: String,
    val price: Int,
    val image: ByteArray? = null
) {
    // Không so sánh image vì nó có thể rất lớn và không cần thiết cho việc xác định tính đồng nhất của MenuItem
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MenuItem

        if (menuItemId != other.menuItemId) return false
        if (price != other.price) return false
        if (name != other.name) return false
        if (category != other.category) return false

        return true
    }

    override fun hashCode(): Int {
        var result = menuItemId.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}
