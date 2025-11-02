package org.example.ui.screens

import androidx.compose.foundation.clickable
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
import org.example.model.Order
import org.example.model.OrderStatus
import org.example.ui.dialogs.CancelOrderDialog
import org.example.ui.dialogs.CompleteOrderDialog
import org.example.ui.dialogs.CreateOrderDialog

/**
 * Экран заказов
 * Отображает список всех заказов с возможностью управления
 */
@Composable
fun OrdersScreen(repository: BookShopRepository) {
    var orders by remember { mutableStateOf(repository.getAllOrders()) }
    var filterStatus by remember { mutableStateOf<OrderStatus?>(null) }
    var selectedCustomerId by remember { mutableStateOf<Int?>(null) }

    // Диалоги
    var showCreateDialog by remember { mutableStateOf(false) }
    var orderToCancel by remember { mutableStateOf<Order?>(null) }
    var orderToComplete by remember { mutableStateOf<Order?>(null) }
    var orderDetails by remember { mutableStateOf<Order?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Обновление списка при изменении данных
    LaunchedEffect(Unit) {
        repository.onDataChanged = {
            orders = repository.getAllOrders()
        }
    }

    // Фильтрация заказов
    val filteredOrders = remember(orders, filterStatus, selectedCustomerId) {
        var filtered = orders

        if (filterStatus != null) {
            filtered = filtered.filter { it.status == filterStatus }
        }

        if (selectedCustomerId != null) {
            filtered = filtered.filter { it.customerId == selectedCustomerId }
        }

        filtered.sortedByDescending { it.creationDate }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    text = "📋 Заказы",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )

                // Фильтр по покупателю
                if (selectedCustomerId != null) {
                    val customer = repository.getCustomerById(selectedCustomerId!!)
                    Surface(
                        color = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = customer?.name ?: "Неизвестно",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { selectedCustomerId = null },
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Фильтры по статусу
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterStatus == null,
                    onClick = { filterStatus = null },
                    label = "Все (${orders.size})"
                )
                FilterChip(
                    selected = filterStatus == OrderStatus.COMPLETED,
                    onClick = { filterStatus = OrderStatus.COMPLETED },
                    label = "Завершены (${orders.count { it.status == OrderStatus.COMPLETED }})"
                )
                FilterChip(
                    selected = filterStatus == OrderStatus.WAITING,
                    onClick = { filterStatus = OrderStatus.WAITING },
                    label = "В ожидании (${orders.count { it.status == OrderStatus.WAITING }})"
                )
                FilterChip(
                    selected = filterStatus == OrderStatus.AVAILABLE,
                    onClick = { filterStatus = OrderStatus.AVAILABLE },
                    label = "Доступны (${orders.count { it.status == OrderStatus.AVAILABLE }})"
                )
            }

            // Статистика
            Text(
                text = "Показано: ${filteredOrders.size} из ${orders.size}",
                style = MaterialTheme.typography.body2,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Список заказов
            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (orders.isEmpty()) "Нет заказов" else "Нет заказов с выбранным статусом",
                            style = MaterialTheme.typography.h6,
                            color = Color.Gray
                        )
                        if (orders.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Нажмите '+' чтобы создать заказ",
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
                    items(filteredOrders) { order ->
                        OrderCard(
                            order = order,
                            repository = repository,
                            onCancel = { orderToCancel = order },
                            onComplete = { orderToComplete = order },
                            onShowDetails = { orderDetails = order },
                            onFilterByCustomer = { selectedCustomerId = order.customerId }
                        )
                    }
                }
            }
        }

        // Кнопка создания заказа
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Создать заказ")
        }

        // Сообщение об успехе
        if (showSuccessMessage) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { showSuccessMessage = false }) {
                        Text("ОК", color = Color.White)
                    }
                }
            ) {
                Text(successMessage)
            }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                showSuccessMessage = false
            }
        }
    }

    // Диалоги
    if (showCreateDialog) {
        CreateOrderDialog(
            repository = repository,
            onDismiss = { showCreateDialog = false },
            onSuccess = { order ->
                orders = repository.getAllOrders()
                val book = repository.getBookById(order.bookId)
                successMessage = when (order.status) {
                    OrderStatus.COMPLETED -> "✓ Заказ создан и завершен"
                    OrderStatus.WAITING -> "⏳ Заказ создан. Покупатель добавлен в базу ожидания для книги '${book?.title ?: ""}'"
                    else -> "✓ Заказ создан"
                }
                showSuccessMessage = true
            }
        )
    }

    orderToCancel?.let { order ->
        CancelOrderDialog(
            order = order,
            repository = repository,
            onDismiss = { orderToCancel = null },
            onSuccess = {
                orders = repository.getAllOrders()
                successMessage = "✓ Заказ #${order.id} отменен"
                showSuccessMessage = true
            }
        )
    }

    orderToComplete?.let { order ->
        CompleteOrderDialog(
            order = order,
            repository = repository,
            onDismiss = { orderToComplete = null },
            onSuccess = {
                orders = repository.getAllOrders()
                successMessage = "✓ Заказ #${order.id} завершен"
                showSuccessMessage = true
            }
        )
    }

    orderDetails?.let { order ->
        OrderDetailsDialog(
            order = order,
            repository = repository,
            onDismiss = { orderDetails = null }
        )
    }
}

/**
 * Компонент фильтра-чипа
 */
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Surface(
        color = if (selected) MaterialTheme.colors.primary else Color.LightGray,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.height(32.dp)
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = label,
                color = if (selected) Color.White else Color.DarkGray,
                style = MaterialTheme.typography.caption
            )
        }
    }
}

/**
 * Карточка заказа с кнопками управления
 */
@Composable
fun OrderCard(
    order: Order,
    repository: BookShopRepository,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    onShowDetails: () -> Unit,
    onFilterByCustomer: () -> Unit
) {
    val customer = repository.getCustomerById(order.customerId)
    val book = repository.getBookById(order.bookId)

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
                    // Заголовок с ID и статусом
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Заказ #${order.id}",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )

                        // Статус
                        Surface(
                            color = getStatusColor(order.status),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = order.getStatusString(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Информация о книге
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = book?.title ?: "Неизвестно",
                            style = MaterialTheme.typography.body1
                        )
                    }

                    // Информация о покупателе
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onFilterByCustomer)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = customer?.name ?: "Неизвестно",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.primary
                        )
                    }

                    // Количество, сумма и дата
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Кол-во: ${order.quantity}",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                            )
                            if (book != null) {
                                Text(
                                    text = "Сумма: ${book.price * order.quantity} ₽",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = order.getFormattedCreationDate(),
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                    }
                }

                // Кнопки управления
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
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

                    // Завершить (только для статуса AVAILABLE)
                    if (order.status == OrderStatus.AVAILABLE) {
                        IconButton(
                            onClick = onComplete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Завершить",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Отменить (только для активных заказов)
                    if (order.isActive()) {
                        IconButton(
                            onClick = onCancel,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Отменить",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Диалог с подробной информацией о заказе
 */
@Composable
fun OrderDetailsDialog(
    order: Order,
    repository: BookShopRepository,
    onDismiss: () -> Unit
) {
    val customer = repository.getCustomerById(order.customerId)
    val book = repository.getBookById(order.bookId)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Заказ #${order.id}")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Статус
                Surface(
                    color = getStatusColor(order.status),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = order.getStatusString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Покупатель
                Card(
                    backgroundColor = Color(0xFFF5F5F5),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Покупатель",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                        Text(
                            text = customer?.name ?: "Неизвестно",
                            fontWeight = FontWeight.Bold
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

                // Книга
                Card(
                    backgroundColor = Color(0xFFF5F5F5),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Книга",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                        Text(
                            text = book?.title ?: "Неизвестно",
                            fontWeight = FontWeight.Bold
                        )
                        if (book != null) {
                            Text(
                                text = "${book.author} • ${book.publisher}, ${book.publicationYear}",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Детали заказа
                Card(
                    backgroundColor = Color(0xFFE3F2FD),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Количество:")
                            Text(
                                text = "${order.quantity} шт.",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Цена за единицу:")
                            Text(
                                text = "${book?.price ?: 0.0} ₽",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.primary
                            )
                        }
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Итого:",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(book?.price ?: 0.0) * order.quantity} ₽",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.primary
                            )
                        }
                    }
                }

                // Даты
                Column {
                    Text(
                        text = "Создан: ${order.getFormattedCreationDate()}",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    Text(
                        text = "Обновлен: ${order.getFormattedUpdateDate()}",
                        style = MaterialTheme.typography.caption,
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
 * Получить цвет для статуса заказа
 */
fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.CREATED -> Color(0xFF2196F3)      // Синий
        OrderStatus.WAITING -> Color(0xFFFF9800)      // Оранжевый
        OrderStatus.AVAILABLE -> Color(0xFF9C27B0)    // Фиолетовый
        OrderStatus.COMPLETED -> Color(0xFF4CAF50)    // Зеленый
        OrderStatus.CANCELLED -> Color(0xFFF44336)    // Красный
    }
}