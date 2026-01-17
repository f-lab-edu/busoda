package com.chaeny.busoda.mvi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<UI_INTENT : UiIntent, UI_STATE : UiState, SIDE_EFFECT : SideEffect>(
    initialState: UI_STATE,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    private val _sideEffect: Channel<SIDE_EFFECT> = Channel(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    protected val currentState: UI_STATE
        get() = _uiState.value

    abstract fun onIntent(intent: UI_INTENT)

    protected fun setState(reduce: UI_STATE.() -> UI_STATE) {
        _uiState.update { currentState.reduce() }
    }

    protected fun postSideEffect(vararg effects: SIDE_EFFECT) {
        Log.d("BaseViewModel", "postSideEffect - ${effects.size}개")
        viewModelScope.launch {
            effects.forEach { effect ->
                Log.d("BaseViewModel", "SharedFlow.emit start - $effect")
                _sideEffect.send(effect)
                Log.d("BaseViewModel", "SharedFlow.emit finish - $effect")
            }
        }
    }
}
