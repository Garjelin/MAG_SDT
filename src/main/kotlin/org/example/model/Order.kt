package org.example.model

import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Перечисление возможных статусов заказа
 */
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
 * @property creationDate Дата создания заказа
 * @property status Статус заказа
 * @property quantity Количество книг в заказе
 */
data class Order(
    val id: Int,
    val customerId: Int,
    val bookId: Int,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    var status: OrderStatus = OrderStatus.CREATED,
    var quantity: Int = 1
) : Serializable {

    // Дата последнего обновления статуса
    var lastUpdateDate: LocalDateTime = creationDate
        private set

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
        lastUpdateDate = LocalDateTime.now()
        return status
    }

    /**
     * Обновить статус заказа
     * @param newStatus Новый статус
     */
    fun updateStatus(newStatus: OrderStatus) {
        status = newStatus
        lastUpdateDate = LocalDateTime.now()
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
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        return creationDate.format(formatter)
    }

    /**
     * Получить форматированную дату последнего обновления
     * @return Дата в формате dd.MM.yyyy HH:mm
     */
    fun getFormattedUpdateDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        return lastUpdateDate.format(formatter)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}