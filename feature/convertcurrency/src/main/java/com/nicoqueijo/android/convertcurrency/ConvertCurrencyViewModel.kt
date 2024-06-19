package com.nicoqueijo.android.convertcurrency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicoqueijo.android.convertcurrency.usecases.ConvertCurrencyUseCases
import com.nicoqueijo.android.core.Currency
import com.nicoqueijo.android.core.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConvertCurrencyViewModel @Inject constructor(
    private val useCases: ConvertCurrencyUseCases,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = ConvertCurrencyUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: ConvertCurrencyUiEvent) {
        when (event) {
            ConvertCurrencyUiEvent.RemoveAllCurrencies -> {
                updateDialogDisplay(toggle = true)
            }
            ConvertCurrencyUiEvent.ConfirmDialog -> {
                removeSelectedCurrencies()
                updateDialogDisplay(toggle = false)
            }
            ConvertCurrencyUiEvent.CancelDialog -> {
                updateDialogDisplay(toggle = false)
            }
        }
    }

    init {
        viewModelScope.launch(context = dispatcher) {
            _uiState.value = _uiState.value.copy(
                selectedCurrencies = useCases.retrieveSelectedCurrenciesUseCase()
            )
        }
    }

    private fun updateDialogDisplay(toggle: Boolean) {
        viewModelScope.launch(context = dispatcher) {
            _uiState.value = _uiState.value.copy(
                showDialog = toggle
            )
        }

    }

    private fun removeSelectedCurrencies() {
        viewModelScope.launch(context = dispatcher) {
            useCases.removeAllCurrenciesUseCase()
            _uiState.value = _uiState.value.copy(
                selectedCurrencies = useCases.retrieveSelectedCurrenciesUseCase()
            )
        }
    }
}

data class ConvertCurrencyUiState(
    val selectedCurrencies: List<Currency> = emptyList(),
    val focusedCurrency: Currency? = null, // unused for now
    val showDialog: Boolean = false,
)

sealed interface ConvertCurrencyUiEvent {
    data object RemoveAllCurrencies : ConvertCurrencyUiEvent
    data object ConfirmDialog : ConvertCurrencyUiEvent
    data object CancelDialog : ConvertCurrencyUiEvent
}