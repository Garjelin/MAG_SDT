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
import org.example.model.WaitingListRecord

/**
 * Экран базы ожидания
 * Отображает список покупателей, ожидающих поступления книг
 */
@Composable
fun WaitingListScreen(repository: BookShopRepository) {
    var records by remember { mutableStateOf(repository.getWaitingList().getAllRecords()) }
    var showOnlyActive by remember { mutableStateOf(true) }
    var groupByBook by remember { mutableStateOf(false) }
    var selectedBookId by remember { mutableStateOf<Int?>(null) }

    // Диалоги
    var recordToDelete by remember { mutableStateOf<WaitingListRecord?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Обновление списка при изменении данных
    LaunchedEffect(Unit) {
        repository.onDataChanged = {
            records = repository.getWaitingList().getAllRecords()
        }
    }

    // Фильтрация записей
    val displayedRecords = remember(records, showOnlyActive, selectedBookId) {
        var filtered = records

        if (showOnlyActive) {
            filtered = filtered.filter { !it.notified }
        }

        if (selectedBookId != null) {
            filtered = filtered.filter { it.bookId == selectedBookId }
        }

        filtered.sortedByDescending { it.registrationDate }
    }

    // Группировка по книгам
    val groupedByBook = remember(displayedRecords) {
        displayedRecords.groupBy { it.bookId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏳ База ожидания",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold
            )

            // Очистить уведомленные
            if (records.any { it.notified }) {
                TextButton(
                    onClick = { showClearDialog = true }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Очистить уведомленные")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Информационная панель
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            backgroundColor = Color(0xFFE3F2FD)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Автоматические уведомления",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        text = "При добавлении или пополнении книги покупатели автоматически уведомляются",
                        style = MaterialTheme.typography.caption,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }

        // Фильтры и переключатели
        WaitingListControls(
            showOnlyActive = showOnlyActive,
            onShowOnlyActiveChange = { showOnlyActive = it },
            groupByBook = groupByBook,
            onGroupByBookChange = { groupByBook = it },
            activeCount = records.count { !it.notified },
            totalCount = records.size
        )

        // Фильтр по книге
        if (selectedBookId != null) {
            val book = repository.getBookById(selectedBookId!!)
            Surface(
                color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = book?.title ?: "Неизвестно",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { selectedBookId = null },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Очистить фильтр",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Статистика
        Text(
            text = "Показано: ${displayedRecords.size} из ${records.size}",
            style = MaterialTheme.typography.body2,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Список записей
        if (displayedRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (records.isEmpty())
                            "База ожидания пуста"
                        else
                            "Нет записей с выбранными фильтрами",
                        style = MaterialTheme.typography.h6,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Все покупатели получили свои книги! 🎉",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
            }
        } else {
            if (groupByBook) {
                // Группированный вид
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedByBook.forEach { (bookId, bookRecords) ->
                        item {
                            BookWaitingGroup(
                                bookId = bookId,
                                records = bookRecords,
                                repository = repository,
                                onFilterByBook = { selectedBookId = bookId },
                                onDeleteRecord = { recordToDelete = it }
                            )
                        }
                    }
                }
            } else {
                // Обычный список
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedRecords) { record ->
                        WaitingListRecordCard(
                            record = record,
                            repository = repository,
                            onDelete = { recordToDelete = record },
                            onFilterByBook = { selectedBookId = record.bookId }
                        )
                    }
                }
            }
        }
    }

    // Диалог удаления записи
    recordToDelete?.let { record ->
        DeleteWaitingRecordDialog(
            record = record,
            repository = repository,
            onDismiss = { recordToDelete = null },
            onSuccess = { records = repository.getWaitingList().getAllRecords() }
        )
    }

    // Диалог очистки уведомленных
    if (showClearDialog) {
        ClearNotifiedDialog(
            repository = repository,
            onDismiss = { showClearDialog = false },
            onSuccess = { records = repository.getWaitingList().getAllRecords() }
        )
    }
}

/**
 * Панель управления базой ожидания
 */
@Composable
fun WaitingListControls(
    showOnlyActive: Boolean,
    onShowOnlyActiveChange: (Boolean) -> Unit,
    groupByBook: Boolean,
    onGroupByBookChange: (Boolean) -> Unit,
    activeCount: Int,
    totalCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Фильтры
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showOnlyActive,
                onClick = { onShowOnlyActiveChange(true) },
                label = "Активные ($activeCount)"
            )
            FilterChip(
                selected = !showOnlyActive,
                onClick = { onShowOnlyActiveChange(false) },
                label = "Все ($totalCount)"
            )
        }

        // Переключатель группировки
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = groupByBook,
                onCheckedChange = onGroupByBookChange
            )
            Text(
                text = "Группировать по книгам",
                style = MaterialTheme.typography.body2
            )
        }
    }
}

/**
 * Группа ожидающих для одной книги
 */
@Composable
fun BookWaitingGroup(
    bookId: Int,
    records: List<WaitingListRecord>,
    repository: BookShopRepository,
    onFilterByBook: () -> Unit,
    onDeleteRecord: (WaitingListRecord) -> Unit
) {
    val book = repository.getBookById(bookId)
    val activeCount = records.count { !it.notified }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = if (book?.isAvailable() == true) Color(0xFFE8F5E9) else Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок группы
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = book?.title ?: "Неизвестная книга",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (book != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${book.author} • ${book.publisher}",
                            style = MaterialTheme.typography.body2,
                            color = Color.Gray
                        )
                        Text(
                            text = "${book.price} ₽",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Статус книги
                Surface(
                    color = if (book?.isAvailable() == true) Color(0xFF4CAF50) else Color(0xFFF44336),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (book?.isAvailable() == true)
                            "В наличии: ${book.quantity}"
                        else
                            "Нет в наличии",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Статистика ожидающих
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(0xFFFF9800).copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ожидают: $activeCount",
                            style = MaterialTheme.typography.caption,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (records.any { it.notified }) {
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Уведомлено: ${records.count { it.notified }}",
                                style = MaterialTheme.typography.caption,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Список покупателей
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                records.forEach { record ->
                    CustomerWaitingItem(
                        record = record,
                        repository = repository,
                        onDelete = { onDeleteRecord(record) }
                    )
                }
            }
        }
    }
}

/**
 * Элемент покупателя в группе
 */
@Composable
fun CustomerWaitingItem(
    record: WaitingListRecord,
    repository: BookShopRepository,
    onDelete: () -> Unit
) {
    val customer = repository.getCustomerById(record.customerId)

    Surface(
        color = if (record.notified) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = if (record.notified) Color(0xFF4CAF50) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = customer?.name ?: "Неизвестно",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = record.getFormattedRegistrationDate(),
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (record.notified) {
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Уведомлен",
                                style = MaterialTheme.typography.caption,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Карточка записи в базе ожидания (обычный вид)
 */
@Composable
fun WaitingListRecordCard(
    record: WaitingListRecord,
    repository: BookShopRepository,
    onDelete: () -> Unit,
    onFilterByBook: () -> Unit
) {
    val customer = repository.getCustomerById(record.customerId)
    val book = repository.getBookById(record.bookId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = if (record.notified) Color(0xFFE8F5E9) else Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Заголовок
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Запись #${record.recordId}",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )

                        if (record.notified) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFF4CAF50),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Уведомлен",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Книга
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = book?.title ?: "Неизвестно",
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.Medium
                            )
                            if (book != null) {
                                Text(
                                    text = book.author,
                                    style = MaterialTheme.typography.caption,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Покупатель
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = customer?.name ?: "Неизвестно",
                                style = MaterialTheme.typography.body2
                            )
                            if (customer != null) {
                                Text(
                                    text = customer.email,
                                    style = MaterialTheme.typography.caption,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Дата и статус книги
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = record.getFormattedRegistrationDate(),
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )

                        if (book != null) {
                            Surface(
                                color = if (book.isAvailable()) Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = if (book.isAvailable()) "В наличии" else "Нет в наличии",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }
                }

                // Кнопка удаления
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Диалог удаления записи из базы ожидания
 */
@Composable
fun DeleteWaitingRecordDialog(
    record: WaitingListRecord,
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val customer = repository.getCustomerById(record.customerId)
    val book = repository.getBookById(record.bookId)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Удалить запись?")
            }
        },
        text = {
            Column {
                Text("Вы действительно хотите удалить эту запись из базы ожидания?")

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    backgroundColor = Color(0xFFFFF3E0),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Запись #${record.recordId}",
                            fontWeight = FontWeight.Bold
                        )
                        Text("Покупатель: ${customer?.name ?: "Неизвестно"}")
                        Text("Книга: ${book?.title ?: "Неизвестно"}")
                        if (record.notified) {
                            Text(
                                text = "✓ Покупатель был уведомлен",
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    repository.removeFromWaitingList(record.recordId)
                    onSuccess()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFF44336),
                    contentColor = Color.White
                )
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Диалог очистки уведомленных записей
 */
@Composable
fun ClearNotifiedDialog(
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val notifiedCount = repository.getWaitingList().getAllRecords().count { it.notified }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Очистить уведомленные записи?") },
        text = {
            Column {
                Text("Будут удалены все записи, по которым покупатели уже были уведомлены.")

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    backgroundColor = Color(0xFFE3F2FD),
                    elevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Будет удалено записей: $notifiedCount",
                            style = MaterialTheme.typography.body2,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    repository.getWaitingList().clearNotifiedRecords()
                    repository.forceSave()
                    onSuccess()
                    onDismiss()
                }
            ) {
                Text("Очистить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}