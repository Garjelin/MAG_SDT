package org.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.data.BookShopRepository
import org.example.model.Book
import org.example.ui.dialogs.AddBookDialog
import org.example.ui.dialogs.DeleteBookDialog
import org.example.ui.dialogs.EditBookDialog

/**
 * Экран каталога книг
 * Отображает список всех книг в магазине с возможностью управления
 */
@Composable
fun CatalogScreen(repository: BookShopRepository) {
    var books by remember { mutableStateOf(repository.getAllBooks()) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.TITLE) }
    var showOnlyAvailable by remember { mutableStateOf(false) }

    // Диалоги
    var showAddDialog by remember { mutableStateOf(false) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    // Обновление списка при изменении данных
    LaunchedEffect(Unit) {
        repository.onDataChanged = {
            books = repository.getAllBooks()
        }
    }

    // Применение фильтров и сортировки
    val displayedBooks = remember(books, searchQuery, sortOption, showOnlyAvailable) {
        var filtered = books

        // Поиск
        if (searchQuery.isNotEmpty()) {
            filtered = repository.searchBooks(searchQuery)
        }

        // Фильтр по наличию
        if (showOnlyAvailable) {
            filtered = filtered.filter { it.isAvailable() }
        }

        // Сортировка
        when (sortOption) {
            SortOption.TITLE -> filtered.sortedBy { it.title }
            SortOption.AUTHOR -> filtered.sortedBy { it.author }
            SortOption.YEAR_DESC -> filtered.sortedByDescending { it.publicationYear }
            SortOption.YEAR_ASC -> filtered.sortedBy { it.publicationYear }
            SortOption.PRICE_ASC -> filtered.sortedBy { it.price }
            SortOption.PRICE_DESC -> filtered.sortedByDescending { it.price }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок
            Text(
                text = "📚 Каталог книг",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Панель управления
            CatalogControlPanel(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                sortOption = sortOption,
                onSortChange = { sortOption = it },
                showOnlyAvailable = showOnlyAvailable,
                onShowOnlyAvailableChange = { showOnlyAvailable = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Статистика
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Найдено: ${displayedBooks.size}",
                    style = MaterialTheme.typography.body1,
                    color = Color.Gray
                )
                Text(
                    text = "Всего книг: ${books.size} | В наличии: ${books.count { it.isAvailable() }}",
                    style = MaterialTheme.typography.body1,
                    color = Color.Gray
                )
            }

            // Список книг
            if (displayedBooks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (books.isEmpty()) "Каталог пуст" else "Книги не найдены",
                            style = MaterialTheme.typography.h6,
                            color = Color.Gray
                        )
                        if (books.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Нажмите '+' чтобы добавить книгу",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedBooks) { book ->
                        BookCard(
                            book = book,
                            repository = repository,
                            onEdit = { bookToEdit = book },
                            onDelete = { bookToDelete = book }
                        )
                    }
                }
            }
        }

        // Кнопка добавления книги
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить книгу")
        }
    }

    // Диалоги
    if (showAddDialog) {
        AddBookDialog(
            repository = repository,
            onDismiss = { showAddDialog = false },
            onSuccess = { books = repository.getAllBooks() }
        )
    }

    bookToEdit?.let { book ->
        EditBookDialog(
            book = book,
            repository = repository,
            onDismiss = { bookToEdit = null },
            onSuccess = { books = repository.getAllBooks() }
        )
    }

    bookToDelete?.let { book ->
        DeleteBookDialog(
            book = book,
            repository = repository,
            onDismiss = { bookToDelete = null },
            onSuccess = { books = repository.getAllBooks() }
        )
    }
}

/**
 * Панель управления каталогом
 */
@Composable
fun CatalogControlPanel(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortOption: SortOption,
    onSortChange: (SortOption) -> Unit,
    showOnlyAvailable: Boolean,
    onShowOnlyAvailableChange: (Boolean) -> Unit
) {
    Column {
        // Строка поиска
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Поиск книг") },
            placeholder = { Text("Введите название, автора или издательство") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Фильтры и сортировка
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Сортировка
            var sortExpanded by remember { mutableStateOf(false) }

            Box {
                OutlinedButton(
                    onClick = { sortExpanded = true },
                    modifier = Modifier.width(300.dp)
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(sortOption.label)
                }

                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    SortOption.values().forEach { option ->
                        DropdownMenuItem(
                            onClick = {
                                onSortChange(option)
                                sortExpanded = false
                            }
                        ) {
                            Text(option.label)
                        }
                    }
                }
            }

            // Фильтр по наличию
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showOnlyAvailable,
                    onCheckedChange = onShowOnlyAvailableChange
                )
                Text(
                    text = "Только в наличии",
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

/**
 * Карточка книги с кнопками управления
 */
@Composable
fun BookCard(
    book: Book,
    repository: BookShopRepository,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val waitingCount = repository.getWaitingCount(book.id)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Основная информация
                Column(modifier = Modifier.weight(1f)) {
                    // Название
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Автор
                    Text(
                        text = "Автор: ${book.author}",
                        style = MaterialTheme.typography.body1
                    )

                    // Издательство и год
                    Text(
                        text = "${book.publisher}, ${book.publicationYear}",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Цена и количество
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${book.price} ₽",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )

                        // Статус наличия
                        Surface(
                            color = if (book.isAvailable()) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = if (book.isAvailable()) "В наличии: ${book.quantity}" else "Нет в наличии",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.caption
                            )
                        }

                        // Индикатор базы ожидания
                        if (waitingCount > 0) {
                            Surface(
                                color = Color(0xFFFF9800),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Ожидают: $waitingCount",
                                        color = Color.White,
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                            }
                        }
                    }
                }

                // Кнопки управления
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Редактировать
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            tint = MaterialTheme.colors.primary
                        )
                    }

                    // Удалить
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Варианты сортировки
 */
enum class SortOption(val label: String) {
    TITLE("По названию (А-Я)"),
    AUTHOR("По автору (А-Я)"),
    YEAR_DESC("По году (новые)"),
    YEAR_ASC("По году (старые)"),
    PRICE_ASC("По цене (дешевые)"),
    PRICE_DESC("По цене (дорогие)")
}