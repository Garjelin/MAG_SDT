package org.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.data.BookShopRepository
import org.example.model.Book

/**
 * Экран каталога книг
 * Отображает список всех книг в магазине
 */
@Composable
fun CatalogScreen(repository: BookShopRepository) {
    var books by remember { mutableStateOf(repository.getAllBooks()) }
    var searchQuery by remember { mutableStateOf("") }

    // Обновление списка при изменении данных
    LaunchedEffect(Unit) {
        repository.onDataChanged = {
            books = repository.getAllBooks()
        }
    }

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

        // Строка поиска
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                books = if (it.isEmpty()) {
                    repository.getAllBooks()
                } else {
                    repository.searchBooks(it)
                }
            },
            label = { Text("Поиск книг") },
            placeholder = { Text("Введите название, автора или издательство") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        // Статистика
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Всего книг: ${books.size}",
                style = MaterialTheme.typography.body1,
                color = Color.Gray
            )
            Text(
                text = "В наличии: ${books.count { it.isAvailable() }}",
                style = MaterialTheme.typography.body1,
                color = Color.Gray
            )
        }

        // Список книг
        if (books.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Каталог пуст",
                    style = MaterialTheme.typography.h6,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(books) { book ->
                    BookCard(book)
                }
            }
        }
    }

    // Кнопка добавления книги (будет реализована в следующей итерации)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { /* TODO: Открыть диалог добавления книги */ },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить книгу")
        }
    }
}

/**
 * Карточка книги
 */
@Composable
fun BookCard(book: Book) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
            }
        }
    }
}