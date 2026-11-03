package com.example.lucimanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lucimanager.model.WifiNetwork
import com.example.lucimanager.R
import com.example.lucimanager.repository.NetworkRepository
import kotlinx.coroutines.launch

sealed class WifiState {
    object Loading : WifiState()
    data class Success(val networks: List<WifiNetwork>) : WifiState()
    data class Error(val message: String) : WifiState()
}

sealed class WifiEditState {
    object Idle : WifiEditState()
    object Saving : WifiEditState()
    data class Success(val message: String) : WifiEditState()
    data class Error(val message: String) : WifiEditState()
}

class WifiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NetworkRepository(application)

    private val _state = MutableLiveData<WifiState>()
    val state: LiveData<WifiState> = _state

    private val _editState = MutableLiveData<WifiEditState>(WifiEditState.Idle)
    val editState: LiveData<WifiEditState> = _editState

    fun loadWifiNetworks() {
        _state.value = WifiState.Loading
        viewModelScope.launch {
            val result = repository.getWifiNetworks()
            _state.value = if (result.isSuccess) {
                WifiState.Success(result.getOrThrow())
            } else {
                WifiState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.error_loading))
            }
        }
    }

    fun updateWifiNetwork(network: WifiNetwork) {
        _editState.value = WifiEditState.Saving
        viewModelScope.launch {
            val result = repository.updateWifiNetwork(network)
            _editState.value = if (result.isSuccess) {
                loadWifiNetworks()
                WifiEditState.Success(getApplication<Application>().getString(R.string.wifi_apply_success))
            } else {
                WifiEditState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.wifi_apply_error))
            }
        }
    }

    fun clearEditState() {
        _editState.value = WifiEditState.Idle
    }
}
