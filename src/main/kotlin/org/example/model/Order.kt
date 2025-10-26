package org.example.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Перечисление возможных статусов заказа
 */
@Serializable
enum class OrderStatus {
    CREATED,      // Создан
    WAITING,      // В ожидании (книга отсутствует)
    AVAILABLE,    // Доступна (книга поступила)
    COMPLETED,    // Завершен
    CANCELLED     // Отменен
}

/**
 * Класс Order представляет заказ покупателя
 * @property id Уникальный идентификатор заказа
 * @property customerId ID покупателя
 * @property bookId ID книги
 * @property creationDate Дата создания заказа (timestamp)
 * @property status Статус заказа
 * @property quantity Количество книг в заказе
 * @property lastUpdateDate Дата последнего обновления статуса (timestamp)
 */
@Serializable
data class Order(
    val id: Int,
    val customerId: Int,
    val bookId: Int,
    val creationDate: Long = System.currentTimeMillis(),
    var status: OrderStatus = OrderStatus.CREATED,
    var quantity: Int = 1,
    var lastUpdateDate: Long = System.currentTimeMillis()
) {

    /**
     * Оформить заказ
     * @param bookAvailable Доступна ли книга в наличии
     * @return Новый статус заказа
     */
    fun placeOrder(bookAvailable: Boolean): OrderStatus {
        status = if (bookAvailable) {
            OrderStatus.COMPLETED
        } else {
            OrderStatus.WAITING
        }
        lastUpdateDate = System.currentTimeMillis()
        return status
    }

    /**
     * Обновить статус заказа
     * @param newStatus Новый статус
     */
    fun updateStatus(newStatus: OrderStatus) {
        status = newStatus
        lastUpdateDate = System.currentTimeMillis()
    }

    /**
     * Завершить заказ
     */
    fun complete() {
        updateStatus(OrderStatus.COMPLETED)
    }

    /**
     * Отменить заказ
     */
    fun cancel() {
        updateStatus(OrderStatus.CANCELLED)
    }

    /**
     * Пометить книгу как доступную
     */
    fun markAsAvailable() {
        if (status == OrderStatus.WAITING) {
            updateStatus(OrderStatus.AVAILABLE)
        }
    }

    /**
     * Проверить, активен ли заказ
     * @return true если заказ ещё не завершен и не отменен
     */
    fun isActive(): Boolean {
        return status != OrderStatus.COMPLETED && status != OrderStatus.CANCELLED
    }

    /**
     * Проверить, находится ли заказ в ожидании
     * @return true если статус WAITING
     */
    fun isWaiting(): Boolean {
        return status == OrderStatus.WAITING
    }

    /**
     * Получить строковое представление статуса на русском
     * @return Статус на русском языке
     */
    fun getStatusString(): String {
        return when (status) {
            OrderStatus.CREATED -> "Создан"
            OrderStatus.WAITING -> "В ожидании"
            OrderStatus.AVAILABLE -> "Доступна"
            OrderStatus.COMPLETED -> "Завершен"
            OrderStatus.CANCELLED -> "Отменен"
        }
    }

    /**
     * Получить форматированную дату создания
     * @return Дата в формате dd.MM.yyyy HH:mm
     */
    fun getFormattedCreationDate(): String {
        return formatTimestamp(creationDate)
    }

    /**
     * Получить форматированную дату последнего обновления
     * @return Дата в формате dd.MM.yyyy HH:mm
     */
    fun getFormattedUpdateDate(): String {
        return formatTimestamp(lastUpdateDate)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
        return format.format(date)
    }
}