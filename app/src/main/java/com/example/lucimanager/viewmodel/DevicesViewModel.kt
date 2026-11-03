package com.example.lucimanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lucimanager.model.ConnectedDevice
import com.example.lucimanager.R
import com.example.lucimanager.repository.NetworkRepository
import kotlinx.coroutines.launch

sealed class DevicesState {
    object Loading : DevicesState()
    data class Success(val devices: List<ConnectedDevice>) : DevicesState()
    data class Error(val message: String) : DevicesState()
}

class DevicesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NetworkRepository(application)

    private val _state = MutableLiveData<DevicesState>()
    val state: LiveData<DevicesState> = _state

    fun loadDevices() {
        _state.value = DevicesState.Loading
        viewModelScope.launch {
            val result = repository.getConnectedDevices()
            _state.value = if (result.isSuccess) {
                DevicesState.Success(result.getOrThrow())
            } else {
                DevicesState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.error_loading))
            }
        }
    }
}
