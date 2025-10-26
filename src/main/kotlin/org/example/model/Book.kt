package org.example.model

import kotlinx.serialization.Serializable

/**
 * Класс Book представляет книгу в каталоге магазина
 * @property id Уникальный идентификатор книги
 * @property author Автор книги
 * @property title Название книги
 * @property publisher Издательство
 * @property publicationYear Год издания
 * @property price Цена книги
 * @property quantity Количество книг в наличии (дополнительное поле)
 */
@Serializable
data class Book(
    val id: Int,
    var author: String,
    var title: String,
    var publisher: String,
    var publicationYear: Int,
    var price: Double,
    var quantity: Int = 0
) {

    /**
     * Получить полную информацию о книге в виде строки
     * @return Строка с информацией о книге
     */
    fun getInformation(): String {
        return """
            |ID: $id
            |Автор: $author
            |Название: $title
            |Издательство: $publisher
            |Год издания: $publicationYear
            |Цена: $price руб.
            |В наличии: $quantity шт.
        """.trimMargin()
    }

    /**
     * Обновить цену книги
     * @param newPrice Новая цена
     */
    fun updatePrice(newPrice: Double) {
        require(newPrice >= 0) { "Цена не может быть отрицательной" }
        price = newPrice
    }

    /**
     * Проверить доступность книги
     * @return true если книга в наличии
     */
    fun isAvailable(): Boolean {
        return quantity > 0
    }

    /**
     * Уменьшить количество книг при продаже
     * @param count Количество продаваемых книг
     * @return true если операция успешна
     */
    fun decreaseQuantity(count: Int = 1): Boolean {
        return if (quantity >= count) {
            quantity -= count
            true
        } else {
            false
        }
    }

    /**
     * Увеличить количество книг при поступлении
     * @param count Количество поступивших книг
     */
    fun increaseQuantity(count: Int = 1) {
        require(count > 0) { "Количество должно быть положительным" }
        quantity += count
    }
}