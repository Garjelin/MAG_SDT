package org.example.ui.dialogs

import androidx.compose.foundation.background
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
import org.example.model.Book
import org.example.model.Customer
import org.example.model.Order

/**
 * Диалог создания нового заказа
 */
@Composable
fun CreateOrderDialog(
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: (Order) -> Unit
) {
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showBookPicker by remember { mutableStateOf(false) }

    var quantityError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Создать заказ",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Информационная панель
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
                            text = "Выберите покупателя и книгу для оформления заказа",
                            style = MaterialTheme.typography.caption,
                            color = Color(0xFF1976D2)
                        )
                    }
                }

                // Выбор покупателя
                Text(
                    text = "1. Выберите покупателя",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = { showCustomerPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCustomer != null) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedCustomer!!.name,
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = selectedCustomer!!.email,
                                    style = MaterialTheme.typography.caption,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Text("Выбрать покупателя")
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                // Выбор книги
                Text(
                    text = "2. Выберите книгу",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = { showBookPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedBook != null) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedBook!!.title,
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Medium
                                )
                                Row {
                                    Text(
                                        text = "${selectedBook!!.price} ₽",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedBook!!.isAvailable())
                                            "В наличии: ${selectedBook!!.quantity}"
                                        else
                                            "Нет в наличии",
                                        style = MaterialTheme.typography.caption,
                                        color = if (selectedBook!!.isAvailable()) Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                }
                            }
                        } else {
                            Text("Выбрать книгу")
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                // Количество
                if (selectedBook != null) {
                    Text(
                        text = "3. Укажите количество",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                quantity = it
                                quantityError = false
                            }
                        },
                        label = { Text("Количество *") },
                        isError = quantityError,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (quantityError) {
                        Text(
                            text = "Количество должно быть от 1 до ${selectedBook!!.quantity}",
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.caption
                        )
                    }

                    // Предпросмотр заказа
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        backgroundColor = Color(0xFFFFF3E0),
                        elevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Предпросмотр заказа",
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57C00)
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val qty = quantity.toIntOrNull() ?: 0
                            val total = selectedBook!!.price * qty

                            Text(
                                text = "Сумма: $total ₽",
                                style = MaterialTheme.typography.body2,
                                fontWeight = FontWeight.Bold
                            )

                            if (!selectedBook!!.isAvailable() || selectedBook!!.quantity < qty) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚠️ Книги нет в наличии. Покупатель будет добавлен в базу ожидания.",
                                    style = MaterialTheme.typography.caption,
                                    color = Color(0xFFF57C00)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Валидация
                    if (selectedCustomer == null || selectedBook == null) {
                        return@Button
                    }

                    val qty = quantity.toIntOrNull()
                    if (qty == null || qty < 1) {
                        quantityError = true
                        return@Button
                    }

                    // Создаем заказ
                    val order = repository.createOrder(
                        customerId = selectedCustomer!!.id,
                        bookId = selectedBook!!.id,
                        quantity = qty
                    )

                    if (order != null) {
                        onSuccess(order)
                        onDismiss()
                    }
                },
                enabled = selectedCustomer != null && selectedBook != null
            ) {
                Text("Создать заказ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )

    // Диалог выбора покупателя
    if (showCustomerPicker) {
        CustomerPickerDialog(
            repository = repository,
            onDismiss = { showCustomerPicker = false },
            onSelect = {
                selectedCustomer = it
                showCustomerPicker = false
            }
        )
    }

    // Диалог выбора книги
    if (showBookPicker) {
        BookPickerDialog(
            repository = repository,
            onDismiss = { showBookPicker = false },
            onSelect = {
                selectedBook = it
                quantity = "1"
                showBookPicker = false
            }
        )
    }
}

/**
 * Диалог выбора покупателя
 */
@Composable
fun CustomerPickerDialog(
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSelect: (Customer) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val customers = remember { repository.getAllCustomers() }

    val filteredCustomers = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            customers
        } else {
            repository.searchCustomers(searchQuery)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите покупателя") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredCustomers.isEmpty()) {
                    Text(
                        text = "Покупатели не найдены",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(filteredCustomers) { customer ->
                            CustomerPickerItem(customer, onClick = { onSelect(customer) })
                        }
                    }
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

@Composable
fun CustomerPickerItem(customer: Customer, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = customer.email,
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Диалог выбора книги
 */
@Composable
fun BookPickerDialog(
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSelect: (Book) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val books = remember { repository.getAllBooks() }

    val filteredBooks = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            books
        } else {
            repository.searchBooks(searchQuery)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите книгу") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredBooks.isEmpty()) {
                    Text(
                        text = "Книги не найдены",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(400.dp)
                    ) {
                        items(filteredBooks) { book ->
                            BookPickerItem(book, onClick = { onSelect(book) })
                        }
                    }
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

@Composable
fun BookPickerItem(book: Book, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${book.author} • ${book.price} ₽",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                }
            }

            Surface(
                color = if (book.isAvailable()) Color(0xFF4CAF50) else Color(0xFFF44336),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = if (book.isAvailable()) "${book.quantity}" else "Нет",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Диалог отмены заказа
 */
@Composable
fun CancelOrderDialog(
    order: Order,
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
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
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Отменить заказ?")
            }
        },
        text = {
            Column {
                Text("Вы действительно хотите отменить этот заказ?")

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    backgroundColor = Color(0xFFFFF3E0),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Заказ #${order.id}",
                            fontWeight = FontWeight.Bold
                        )
                        Text("Книга: ${book?.title ?: "Неизвестно"}")
                        Text("Покупатель: ${customer?.name ?: "Неизвестно"}")
                        Text("Статус: ${order.getStatusString()}")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    repository.cancelOrder(order.id)
                    onSuccess()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFF44336),
                    contentColor = Color.White
                )
            ) {
                Text("Отменить заказ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Назад")
            }
        }
    )
}

/**
 * Диалог завершения заказа
 */
@Composable
fun CompleteOrderDialog(
    order: Order,
    repository: BookShopRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
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
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Завершить заказ?")
            }
        },
        text = {
            Column {
                Text("Подтвердите выдачу книги покупателю:")

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    backgroundColor = Color(0xFFE8F5E9),
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Заказ #${order.id}",
                            fontWeight = FontWeight.Bold
                        )
                        Text("Книга: ${book?.title ?: "Неизвестно"}")
                        Text("Покупатель: ${customer?.name ?: "Неизвестно"}")
                        Text("Количество: ${order.quantity}")
                        Text("Сумма: ${(book?.price ?: 0.0) * order.quantity} ₽", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    repository.completeOrder(order.id)
                    onSuccess()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                Text("Завершить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Назад")
            }
        }
    )
}