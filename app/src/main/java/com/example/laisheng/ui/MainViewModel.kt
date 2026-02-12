package com.example.laisheng.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.laisheng.data.local.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(private val userPrefs: UserPrefs) : ViewModel() {
    // 0 = System, 1 = Light, 2 = Dark
    private val _themeMode = MutableStateFlow(userPrefs.getThemeMode())
    val themeMode = _themeMode.asStateFlow()

    fun setThemeMode(mode: Int) {
        userPrefs.saveThemeMode(mode)
        _themeMode.value = mode
    }
}

class MainViewModelFactory(private val userPrefs: UserPrefs) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(userPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
