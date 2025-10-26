package org.example.data

import org.example.model.*

/**
 * Класс BookShopRepository - центральный репозиторий для работы с данными приложения
 * Предоставляет единую точку доступа ко всем данным и управляет их сохранением
 */
class BookShopRepository {

    private val dataManager = DataManager()
    private var appData: AppData = dataManager.loadData()

    // Флаг автоматического сохранения
    var autoSave: Boolean = true

    // Callback для уведомления об изменениях
    var onDataChanged: (() -> Unit)? = null

    /**
     * Сохранить данные
     */
    private fun save() {
        if (autoSave) {
            dataManager.saveData(appData)
        }
        onDataChanged?.invoke()
    }

    // ==================== РАБОТА С КАТАЛОГОМ ====================

    /**
     * Получить каталог книг
     */
    fun getCatalog(): Catalog = appData.catalog

    /**
     * Добавить книгу в каталог
     */
    fun addBook(author: String, title: String, publisher: String,
                publicationYear: Int, price: Double, quantity: Int = 0): Book {
        val book = appData.catalog.addBook(author, title, publisher, publicationYear, price, quantity)

        // Проверяем базу ожидания при добавлении книги
        if (quantity > 0) {
            checkWaitingListForBook(book)
        }

        save()
        return book
    }

    /**
     * Удалить книгу из каталога
     */
    fun removeBook(bookId: Int): Boolean {
        val result = appData.catalog.removeBook(bookId)
        if (result) save()
        return result
    }

    /**
     * Обновить количество книги в каталоге
     */
    fun updateBookQuantity(bookId: Int, quantity: Int): Boolean {
        val result = appData.catalog.updateCatalog(bookId, quantity)
        if (result) {
            // Проверяем базу ожидания при поступлении книги
            val book = appData.catalog.getBookById(bookId)
            book?.let { checkWaitingListForBook(it) }
            save()
        }
        return result
    }

    /**
     * Получить книгу по ID
     */
    fun getBookById(bookId: Int): Book? = appData.catalog.getBookById(bookId)

    /**
     * Получить все книги
     */
    fun getAllBooks(): List<Book> = appData.catalog.getAllBooks()

    /**
     * Поиск книг
     */
    fun searchBooks(query: String): List<Book> = appData.catalog.search(query)

    // ==================== РАБОТА С ПОКУПАТЕЛЯМИ ====================

    /**
     * Добавить покупателя
     */
    fun addCustomer(name: String, email: String, phone: String = ""): Customer {
        val customer = Customer(
            id = appData.nextCustomerId++,
            name = name,
            email = email,
            phone = phone
        )
        appData.customers.add(customer)
        save()
        return customer
    }

    /**
     * Удалить покупателя
     */
    fun removeCustomer(customerId: Int): Boolean {
        val result = appData.customers.removeIf { it.id == customerId }
        if (result) save()
        return result
    }

    /**
     * Обновить покупателя
     */
    fun updateCustomer(customerId: Int, name: String, email: String, phone: String): Boolean {
        val customer = getCustomerById(customerId)
        return if (customer != null) {
            customer.name = name
            customer.email = email
            customer.phone = phone
            save()
            true
        } else {
            false
        }
    }

    /**
     * Получить покупателя по ID
     */
    fun getCustomerById(customerId: Int): Customer? {
        return appData.customers.find { it.id == customerId }
    }

    /**
     * Получить всех покупателей
     */
    fun getAllCustomers(): List<Customer> = appData.customers.toList()

    /**
     * Поиск покупателей по имени или email
     */
    fun searchCustomers(query: String): List<Customer> {
        return appData.customers.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
        }
    }

    // ==================== РАБОТА С ЗАКАЗАМИ ====================

    /**
     * Создать заказ
     */
    fun createOrder(customerId: Int, bookId: Int, quantity: Int = 1): Order? {
        val customer = getCustomerById(customerId)
        val book = getBookById(bookId)

        if (customer == null || book == null) {
            return null
        }

        val order = Order(
            id = appData.nextOrderId++,
            customerId = customerId,
            bookId = bookId,
            quantity = quantity
        )

        // Определяем статус заказа
        if (book.quantity >= quantity) {
            // Книга доступна - выполняем заказ
            order.placeOrder(true)
            book.decreaseQuantity(quantity)
        } else {
            // Книги нет - добавляем в список ожидания
            order.placeOrder(false)
            appData.waitingList.addToWaiting(customer, book)
        }

        appData.orders.add(order)
        save()
        return order
    }

    /**
     * Отменить заказ
     */
    fun cancelOrder(orderId: Int): Boolean {
        val order = getOrderById(orderId)
        return if (order != null && order.isActive()) {
            order.cancel()
            save()
            true
        } else {
            false
        }
    }

    /**
     * Завершить заказ
     */
    fun completeOrder(orderId: Int): Boolean {
        val order = getOrderById(orderId)
        return if (order != null && order.status == OrderStatus.AVAILABLE) {
            order.complete()

            // Уменьшаем количество книги
            val book = getBookById(order.bookId)
            book?.decreaseQuantity(order.quantity)

            save()
            true
        } else {
            false
        }
    }

    /**
     * Получить заказ по ID
     */
    fun getOrderById(orderId: Int): Order? {
        return appData.orders.find { it.id == orderId }
    }

    /**
     * Получить все заказы
     */
    fun getAllOrders(): List<Order> = appData.orders.toList()

    /**
     * Получить заказы покупателя
     */
    fun getOrdersByCustomer(customerId: Int): List<Order> {
        return appData.orders.filter { it.customerId == customerId }
    }

    /**
     * Получить заказы по книге
     */
    fun getOrdersByBook(bookId: Int): List<Order> {
        return appData.orders.filter { it.bookId == bookId }
    }

    /**
     * Получить активные заказы
     */
    fun getActiveOrders(): List<Order> {
        return appData.orders.filter { it.isActive() }
    }

    // ==================== РАБОТА С БАЗОЙ ОЖИДАНИЯ ====================

    /**
     * Получить базу ожидания
     */
    fun getWaitingList(): WaitingList = appData.waitingList

    /**
     * Добавить в список ожидания
     */
    fun addToWaitingList(customerId: Int, bookId: Int): WaitingListRecord? {
        val customer = getCustomerById(customerId)
        val book = getBookById(bookId)

        if (customer == null || book == null) {
            return null
        }

        val record = appData.waitingList.addToWaiting(customer, book)
        save()
        return record
    }

    /**
     * Удалить запись из базы ожидания
     */
    fun removeFromWaitingList(recordId: Int): Boolean {
        val result = appData.waitingList.removeRecord(recordId)
        if (result) save()
        return result
    }

    /**
     * Проверить базу ожидания для конкретной книги
     * Уведомляет покупателей и обновляет заказы
     */
    private fun checkWaitingListForBook(book: Book) {
        if (!book.isAvailable()) return

        // Уведомляем покупателей из базы ожидания
        val notifiedCustomers = appData.waitingList.notifyCustomers(book, appData.customers)

        if (notifiedCustomers.isNotEmpty()) {
            println("✓ Уведомлено покупателей: ${notifiedCustomers.size} для книги '${book.title}'")

            // Обновляем статусы заказов
            appData.orders
                .filter { it.bookId == book.id && it.status == OrderStatus.WAITING }
                .forEach { it.markAsAvailable() }
        }
    }

    /**
     * Получить количество ожидающих для книги
     */
    fun getWaitingCount(bookId: Int): Int {
        return appData.waitingList.getWaitingCount(bookId)
    }

    // ==================== СТАТИСТИКА ====================

    /**
     * Получить общую статистику
     */
    fun getStatistics(): Map<String, Any> {
        return mapOf(
            "totalBooks" to appData.catalog.getTotalBooksCount(),
            "totalQuantity" to appData.catalog.getTotalQuantity(),
            "totalValue" to appData.catalog.getTotalValue(),
            "totalCustomers" to appData.customers.size,
            "totalOrders" to appData.orders.size,
            "activeOrders" to appData.orders.count { it.isActive() },
            "completedOrders" to appData.orders.count { it.status == OrderStatus.COMPLETED },
            "waitingOrders" to appData.orders.count { it.status == OrderStatus.WAITING },
            "waitingListSize" to appData.waitingList.getActiveRecords().size
        )
    }

    // ==================== УПРАВЛЕНИЕ ДАННЫМИ ====================

    /**
     * Принудительно сохранить данные
     */
    fun forceSave(): Boolean {
        return dataManager.saveData(appData)
    }

    /**
     * Перезагрузить данные из файла
     */
    fun reload() {
        appData = dataManager.loadData()
        onDataChanged?.invoke()
    }

    /**
     * Экспортировать данные
     */
    fun exportData(file: java.io.File): Boolean {
        return dataManager.exportData(file, appData)
    }

    /**
     * Импортировать данные
     */
    fun importData(file: java.io.File): Boolean {
        val imported = dataManager.importData(file)
        return if (imported != null) {
            appData = imported
            save()
            true
        } else {
            false
        }
    }

    /**
     * Создать резервную копию
     */
    fun createBackup(): Boolean {
        return dataManager.createManualBackup(appData)
    }

    /**
     * Очистить все данные
     */
    fun clearAllData() {
        appData = AppData()
        save()
    }
}