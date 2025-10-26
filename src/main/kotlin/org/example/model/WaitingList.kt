package org.example.model

import kotlinx.serialization.Serializable

/**
 * Класс WaitingListRecord представляет одну запись в базе ожидания
 * @property recordId Уникальный идентификатор записи
 * @property customerId ID покупателя
 * @property bookId ID книги
 * @property registrationDate Дата регистрации в базе ожидания (timestamp)
 * @property notified Флаг уведомления (был ли покупатель уведомлен)
 * @property notificationDate Дата уведомления покупателя (timestamp)
 */
@Serializable
data class WaitingListRecord(
    val recordId: Int,
    val customerId: Int,
    val bookId: Int,
    val registrationDate: Long = System.currentTimeMillis(),
    var notified: Boolean = false,
    var notificationDate: Long? = null
) {

    /**
     * Пометить покупателя как уведомленного
     */
    fun markAsNotified() {
        notified = true
        notificationDate = System.currentTimeMillis()
    }

    /**
     * Получить форматированную дату регистрации
     * @return Дата в формате dd.MM.yyyy HH:mm
     */
    fun getFormattedRegistrationDate(): String {
        return formatTimestamp(registrationDate)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
        return format.format(date)
    }
}

/**
 * Класс WaitingList управляет базой ожидания
 * Хранит записи о покупателях, ожидающих поступления книг
 */
@Serializable
data class WaitingList(
    private val records: MutableList<WaitingListRecord> = mutableListOf(),
    private var nextRecordId: Int = 1
) {

    /**
     * Добавить покупателя в список ожидания книги
     * @param customer Покупатель
     * @param book Книга
     * @return Созданная запись
     */
    fun addToWaiting(customer: Customer, book: Book): WaitingListRecord {
        // Проверяем, не ожидает ли уже этот покупатель эту книгу
        val existing = records.find {
            it.customerId == customer.id && it.bookId == book.id && !it.notified
        }

        if (existing != null) {
            return existing
        }

        val record = WaitingListRecord(
            recordId = nextRecordId++,
            customerId = customer.id,
            bookId = book.id
        )
        records.add(record)
        return record
    }

    /**
     * Уведомить всех покупателей, ожидающих определенную книгу
     * @param book Книга, которая поступила
     * @param customers Список всех покупателей
     * @return Список уведомленных покупателей
     */
    fun notifyCustomers(book: Book, customers: List<Customer>): List<Customer> {
        val waitingForBook = records.filter {
            it.bookId == book.id && !it.notified
        }

        val notifiedCustomers = mutableListOf<Customer>()

        waitingForBook.forEach { record ->
            val customer = customers.find { it.id == record.customerId }
            customer?.let {
                it.receiveNotification(book.title)
                record.markAsNotified()
                notifiedCustomers.add(it)
            }
        }

        return notifiedCustomers
    }

    /**
     * Получить все записи для определенной книги
     * @param bookId ID книги
     * @return Список записей
     */
    fun getRecordsForBook(bookId: Int): List<WaitingListRecord> {
        return records.filter { it.bookId == bookId }
    }

    /**
     * Получить все записи для определенного покупателя
     * @param customerId ID покупателя
     * @return Список записей
     */
    fun getRecordsForCustomer(customerId: Int): List<WaitingListRecord> {
        return records.filter { it.customerId == customerId }
    }

    /**
     * Получить все активные (неуведомленные) записи
     * @return Список активных записей
     */
    fun getActiveRecords(): List<WaitingListRecord> {
        return records.filter { !it.notified }
    }

    /**
     * Получить все записи
     * @return Список всех записей
     */
    fun getAllRecords(): List<WaitingListRecord> {
        return records.toList()
    }

    /**
     * Удалить запись из базы ожидания
     * @param recordId ID записи
     * @return true если запись удалена
     */
    fun removeRecord(recordId: Int): Boolean {
        return records.removeIf { it.recordId == recordId }
    }

    /**
     * Удалить все уведомленные записи
     * @return Количество удаленных записей
     */
    fun clearNotifiedRecords(): Int {
        val beforeSize = records.size
        records.removeIf { it.notified }
        return beforeSize - records.size
    }

    /**
     * Получить количество ожидающих покупателей для книги
     * @param bookId ID книги
     * @return Количество ожидающих
     */
    fun getWaitingCount(bookId: Int): Int {
        return records.count { it.bookId == bookId && !it.notified }
    }

    /**
     * Очистить всю базу ожидания
     */
    fun clear() {
        records.clear()
        nextRecordId = 1
    }
}