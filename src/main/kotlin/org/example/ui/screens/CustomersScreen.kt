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
import org.example.model.Customer
import org.example.ui.dialogs.AddCustomerDialog
import org.example.ui.dialogs.DeleteCustomerDialog
import org.example.ui.dialogs.EditCustomerDialog

/**
 * Экран покупателей
 * Отображает список всех покупателей магазина с возможностью управления
 */
@Composable
fun CustomersScreen(repository: BookShopRepository) {
    var customers by remember { mutableStateOf(repository.getAllCustomers()) }
    var searchQuery by remember { mutableStateOf("") }
    var sortByName by remember { mutableStateOf(true) }

    // Диалоги
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var customerDetails by remember { mutableStateOf<Customer?>(null) }

    // Обновление списка при изменении данных
    LaunchedEffect(Unit) {
        repository.onDataChanged = {
            customers = repository.getAllCustomers()
        }
    }

    // Применение фильтров и сортировки
    val displayedCustomers = remember(customers, searchQuery, sortByName) {
        var filtered = customers

        // Поиск
        if (searchQuery.isNotEmpty()) {
            filtered = repository.searchCustomers(searchQuery)
        }

        // Сортировка
        if (sortByName) {
            filtered.sortedBy { it.name }
        } else {
            filtered.sortedByDescending { it.registrationDate }
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
                text = "👥 Покупатели",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Панель управления
            CustomersControlPanel(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                sortByName = sortByName,
                onSortChange = { sortByName = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Статистика
            Text(
                text = "Найдено: ${displayedCustomers.size} из ${customers.size}",
                style = MaterialTheme.typography.body1,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Список покупателей
            if (displayedCustomers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (customers.isEmpty()) "Нет зарегистрированных покупателей" else "Покупатели не найдены",
                            style = MaterialTheme.typography.h6,
                            color = Color.Gray
                        )
                        if (customers.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Нажмите '+' чтобы добавить покупателя",
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
                    items(displayedCustomers) { customer ->
                        CustomerCard(
                            customer = customer,
                            repository = repository,
                            onEdit = { customerToEdit = customer },
                            onDelete = { customerToDelete = customer },
                            onShowDetails = { customerDetails = customer }
                        )
                    }
                }
            }
        }

        // Кнопка добавления покупателя
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить покупателя")
        }
    }

    // Диалоги
    if (showAddDialog) {
        AddCustomerDialog(
            repository = repository,
            onDismiss = { showAddDialog = false },
            onSuccess = { customers = repository.getAllCustomers() }
        )
    }

    customerToEdit?.let { customer ->
        EditCustomerDialog(
            customer = customer,
            repository = repository,
            onDismiss = { customerToEdit = null },
            onSuccess = { customers = repository.getAllCustomers() }
        )
    }

    customerToDelete?.let { customer ->
        DeleteCustomerDialog(
            customer = customer,
            repository = repository,
            onDismiss = { customerToDelete = null },
            onSuccess = { customers = repository.getAllCustomers() }
        )
    }

    customerDetails?.let { customer ->
        CustomerDetailsDialog(
            customer = customer,
            repository = repository,
            onDismiss = { customerDetails = null }
        )
    }
}

/**
 * Панель управления покупателями
 */
@Composable
fun CustomersControlPanel(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortByName: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    Column {
        // Строка поиска
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Поиск покупателей") },
            placeholder = { Text("Введите имя или email") },
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

        // Сортировка
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onSortChange(true) },
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = if (sortByName) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent
                )
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (sortByName) MaterialTheme.colors.primary else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "По имени",
                    color = if (sortByName) MaterialTheme.colors.primary else Color.Gray
                )
            }

            OutlinedButton(
                onClick = { onSortChange(false) },
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = if (!sortByName) MaterialTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent
                )
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (!sortByName) MaterialTheme.colors.primary else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "По дате регистрации",
                    color = if (!sortByName) MaterialTheme.colors.primary else Color.Gray
                )
            }
        }
    }
}

/**
 * Карточка покупателя с кнопками управления
 */
@Composable
fun CustomerCard(
    customer: Customer,
    repository: BookShopRepository,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowDetails: () -> Unit
) {
    val orders = repository.getOrdersByCustomer(customer.id)
    val activeOrders = orders.count { it.isActive() }
    val completedOrders = orders.count { it.status == org.example.model.OrderStatus.COMPLETED }

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
                    // Имя
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = customer.email,
                            style = MaterialTheme.typography.body2,
                            color = Color.Gray
                        )
                    }

                    // Телефон (если есть)
                    if (customer.phone.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                            )
                        }
                    }

                    // Статистика заказов
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Всего заказов
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Заказов: ${orders.size}",
                                    style = MaterialTheme.typography.caption,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }

                        // Активные заказы
                        if (activeOrders > 0) {
                            Surface(
                                color = Color(0xFFFFF3E0),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Активных: $activeOrders",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = Color(0xFFF57C00),
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Завершенные заказы
                        if (completedOrders > 0) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Завершено: $completedOrders",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = Color(0xFF388E3C),
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }
                }

                // Кнопки управления
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Подробнее
                    IconButton(
                        onClick = onShowDetails,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Подробнее",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Редактировать
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Удалить
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
}

/**
 * Диалог с подробной информацией о покупателе
 */
@Composable
fun CustomerDetailsDialog(
    customer: Customer,
    repository: BookShopRepository,
    onDismiss: () -> Unit
) {
    val orders = repository.getOrdersByCustomer(customer.id)
    val waitingRecords = repository.getWaitingList().getRecordsForCustomer(customer.id)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Информация о покупателе",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Основная информация
                Card(
                    backgroundColor = Color(0xFFF5F5F5),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Email: ${customer.email}",
                            style = MaterialTheme.typography.body2
                        )
                        if (customer.phone.isNotEmpty()) {
                            Text(
                                text = "Телефон: ${customer.phone}",
                                style = MaterialTheme.typography.body2
                            )
                        }
                        Text(
                            text = "ID: ${customer.id}",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                        Text(
                            text = "Зарегистрирован: ${formatTimestamp(customer.registrationDate)}",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                    }
                }

                // Статистика заказов
                Text(
                    text = "Статистика заказов",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatCard("Всего", orders.size.toString(), Color(0xFF2196F3))
                    StatCard("Активных", orders.count { it.isActive() }.toString(), Color(0xFFFF9800))
                    StatCard("Завершено", orders.count { it.status == org.example.model.OrderStatus.COMPLETED }.toString(), Color(0xFF4CAF50))
                }

                // База ожидания
                if (waitingRecords.isNotEmpty()) {
                    Text(
                        text = "В базе ожидания",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ожидает поступления ${waitingRecords.size} ${if (waitingRecords.size == 1) "книги" else "книг"}",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

/**
 * Компонент для отображения статистики
 */
@Composable
fun StatCard(label: String, value: String, color: Color) {
    Card(
        backgroundColor = color.copy(alpha = 0.1f),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.caption,
                color = color
            )
        }
    }
}

/**
 * Форматирование timestamp
 */
private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
    return format.format(date)
}