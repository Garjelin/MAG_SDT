package org.example.model

import java.io.Serializable

/**
 * Класс Catalog управляет каталогом книг в магазине
 * Предоставляет методы для добавления, удаления, поиска и обновления книг
 */
class Catalog : Serializable {

    private val books = mutableListOf<Book>()
    private var nextBookId = 1

    /**
     * Добавить книгу в каталог
     * @param author Автор
     * @param title Название
     * @param publisher Издательство
     * @param publicationYear Год издания
     * @param price Цена
     * @param quantity Количество в наличии
     * @return Добавленная книга
     */
    fun addBook(
        author: String,
        title: String,
        publisher: String,
        publicationYear: Int,
        price: Double,
        quantity: Int = 0
    ): Book {
        val book = Book(
            id = nextBookId++,
            author = author,
            title = title,
            publisher = publisher,
            publicationYear = publicationYear,
            price = price,
            quantity = quantity
        )
        books.add(book)
        return book
    }

    /**
     * Добавить готовую книгу в каталог
     * @param book Книга для добавления
     * @return Добавленная книга с обновленным ID
     */
    fun addBook(book: Book): Book {
        val newBook = book.copy(id = nextBookId++)
        books.add(newBook)
        return newBook
    }

    /**
     * Обновить каталог - увеличить количество книги
     * @param bookId ID книги
     * @param quantity Количество поступивших книг
     * @return true если операция успешна
     */
    fun updateCatalog(bookId: Int, quantity: Int): Boolean {
        val book = getBookById(bookId)
        return if (book != null) {
            book.increaseQuantity(quantity)
            true
        } else {
            false
        }
    }

    /**
     * Удалить книгу из каталога
     * @param bookId ID книги
     * @return true если книга удалена
     */
    fun removeBook(bookId: Int): Boolean {
        return books.removeIf { it.id == bookId }
    }

    /**
     * Получить книгу по ID
     * @param bookId ID книги
     * @return Книга или null
     */
    fun getBookById(bookId: Int): Book? {
        return books.find { it.id == bookId }
    }

    /**
     * Получить все книги из каталога
     * @return Список всех книг
     */
    fun getAllBooks(): List<Book> {
        return books.toList()
    }

    /**
     * Получить только доступные книги (в наличии)
     * @return Список доступных книг
     */
    fun getAvailableBooks(): List<Book> {
        return books.filter { it.isAvailable() }
    }

    /**
     * Поиск книг по автору
     * @param author Имя автора (или часть имени)
     * @return Список найденных книг
     */
    fun searchByAuthor(author: String): List<Book> {
        return books.filter {
            it.author.contains(author, ignoreCase = true)
        }
    }

    /**
     * Поиск книг по названию
     * @param title Название (или часть названия)
     * @return Список найденных книг
     */
    fun searchByTitle(title: String): List<Book> {
        return books.filter {
            it.title.contains(title, ignoreCase = true)
        }
    }

    /**
     * Поиск книг по издательству
     * @param publisher Издательство (или часть названия)
     * @return Список найденных книг
     */
    fun searchByPublisher(publisher: String): List<Book> {
        return books.filter {
            it.publisher.contains(publisher, ignoreCase = true)
        }
    }

    /**
     * Поиск книг по году издания
     * @param year Год издания
     * @return Список найденных книг
     */
    fun searchByYear(year: Int): List<Book> {
        return books.filter { it.publicationYear == year }
    }

    /**
     * Поиск книг в диапазоне цен
     * @param minPrice Минимальная цена
     * @param maxPrice Максимальная цена
     * @return Список найденных книг
     */
    fun searchByPriceRange(minPrice: Double, maxPrice: Double): List<Book> {
        return books.filter { it.price in minPrice..maxPrice }
    }

    /**
     * Универсальный поиск по всем полям
     * @param query Поисковый запрос
     * @return Список найденных книг
     */
    fun search(query: String): List<Book> {
        return books.filter { book ->
            book.author.contains(query, ignoreCase = true) ||
                    book.title.contains(query, ignoreCase = true) ||
                    book.publisher.contains(query, ignoreCase = true) ||
                    book.publicationYear.toString().contains(query)
        }
    }

    /**
     * Сортировать книги по цене
     * @param ascending По возрастанию (true) или убыванию (false)
     * @return Отсортированный список книг
     */
    fun sortByPrice(ascending: Boolean = true): List<Book> {
        return if (ascending) {
            books.sortedBy { it.price }
        } else {
            books.sortedByDescending { it.price }
        }
    }

    /**
     * Сортировать книги по названию
     * @return Отсортированный список книг
     */
    fun sortByTitle(): List<Book> {
        return books.sortedBy { it.title }
    }

    /**
     * Сортировать книги по автору
     * @return Отсортированный список книг
     */
    fun sortByAuthor(): List<Book> {
        return books.sortedBy { it.author }
    }

    /**
     * Сортировать книги по году издания
     * @param ascending По возрастанию (true) или убыванию (false)
     * @return Отсортированный список книг
     */
    fun sortByYear(ascending: Boolean = false): List<Book> {
        return if (ascending) {
            books.sortedBy { it.publicationYear }
        } else {
            books.sortedByDescending { it.publicationYear }
        }
    }

    /**
     * Получить общее количество книг в каталоге
     * @return Количество различных книг
     */
    fun getTotalBooksCount(): Int {
        return books.size
    }

    /**
     * Получить общее количество экземпляров всех книг
     * @return Сумма всех количеств
     */
    fun getTotalQuantity(): Int {
        return books.sumOf { it.quantity }
    }

    /**
     * Получить общую стоимость всех книг в каталоге
     * @return Сумма (цена * количество) всех книг
     */
    fun getTotalValue(): Double {
        return books.sumOf { it.price * it.quantity }
    }

    /**
     * Очистить каталог
     */
    fun clear() {
        books.clear()
        nextBookId = 1
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}