package org.example.ui

import androidx.compose.runtime.mutableStateOf

/**
 * Роли пользователей в системе
 */
enum class UserRole(val displayName: String, val icon: String) {
    ADMIN("Администратор", "👨‍💼"),
    CUSTOMER("Покупатель", "👤");

    /**
     * Проверить доступ к функционалу
     */
    fun canAccessCatalogManagement(): Boolean = this == ADMIN
    fun canAccessCustomerManagement(): Boolean = this == ADMIN
    fun canAccessOrderManagement(): Boolean = this == ADMIN
    fun canAccessWaitingList(): Boolean = this == ADMIN
    fun canAccessStatistics(): Boolean = this == ADMIN

    fun canViewCatalog(): Boolean = true
    fun canViewOrders(): Boolean = true
    fun canCreateOrder(): Boolean = true
}

/**
 * Управление текущей ролью пользователя
 */
object RoleManager {
    private val _currentRole = mutableStateOf(UserRole.ADMIN)
    val currentRole: androidx.compose.runtime.State<UserRole> = _currentRole

    /**
     * Установить текущую роль
     */
    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    /**
     * Получить текущую роль
     */
    fun getRole(): UserRole = _currentRole.value

    /**
     * Проверить, является ли пользователь администратором
     */
    fun isAdmin(): Boolean = _currentRole.value == UserRole.ADMIN

    /**
     * Проверить, является ли пользователь покупателем
     */
    fun isCustomer(): Boolean = _currentRole.value == UserRole.CUSTOMER
}