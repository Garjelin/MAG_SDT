package org.example.model

import kotlinx.serialization.Serializable

/**
 * Класс Customer представляет покупателя
 * @property id Уникальный идентификатор покупателя
 * @property name Имя покупателя
 * @property email Email для связи
 * @property phone Номер телефона (опционально)
 * @property registrationDate Дата регистрации
 */
@Serializable
data class Customer(
    val id: Int,
    var name: String,
    var email: String,
    var phone: String = "",
    val registrationDate: Long = System.currentTimeMillis()
) {

    // Список уведомлений для покупателя (не сериализуется автоматически)
    @kotlinx.serialization.Transient
    private val notifications = mutableListOf<String>()

    /**
     * Зарегистрировать заказ для покупателя
     * @param book Книга для заказа
     * @return Сообщение о результате
     */
    fun registerOrder(book: Book): String {
        return if (book.isAvailable()) {
            "Заказ на книгу '${book.title}' успешно оформлен"
        } else {
            "Книга '${book.title}' отсутствует. Вы добавлены в список ожидания"
        }
    }

    /**
     * Получить уведомление о поступлении книги
     * @param bookTitle Название книги
     */
    fun receiveNotification(bookTitle: String) {
        val message = "Книга '$bookTitle' поступила в наличие!"
        notifications.add(message)
    }

    /**
     * Получить все уведомления
     * @return Список уведомлений
     */
    fun getNotifications(): List<String> {
        return notifications.toList()
    }

    /**
     * Очистить уведомления
     */
    fun clearNotifications() {
        notifications.clear()
    }

    /**
     * Получить количество непрочитанных уведомлений
     * @return Количество уведомлений
     */
    fun getNotificationCount(): Int {
        return notifications.size
    }

    /**
     * Проверить корректность email
     * @return true если email корректен
     */
    fun isEmailValid(): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    /**
     * Получить краткую информацию о покупателе
     * @return Строка с информацией
     */
    fun getInfo(): String {
        return "ID: $id | Имя: $name | Email: $email" +
                if (phone.isNotEmpty()) " | Тел: $phone" else ""
    }
}