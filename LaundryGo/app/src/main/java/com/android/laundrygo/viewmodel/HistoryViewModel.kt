package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistoryItem(
    val orderId: String,
    val status: String,
    val date: String
)

class HistoryViewModel : ViewModel() {

    private val _historyList = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyList: StateFlow<List<HistoryItem>> = _historyList

    init {
        loadHistory()
    }

    private fun loadHistory() {
        // Ganti dengan pemanggilan dari Firestore jika sudah terhubung
        viewModelScope.launch {
            val dummyData = listOf(
                HistoryItem("LG012345678", "In Process", "Monday, 9 March 2024"),
                HistoryItem("LG012905231", "Done", "Sunday, 7 February 2024"),
                HistoryItem("LG054126723", "Done", "Friday, 23 January 2024")
            )
            _historyList.value = dummyData
        }
    }

    fun getStatusColor(status: String): androidx.compose.ui.graphics.Color {
        return when (status) {
            "Done" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
            "In Process" -> androidx.compose.ui.graphics.Color(0xFFFFA500)
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }
}
