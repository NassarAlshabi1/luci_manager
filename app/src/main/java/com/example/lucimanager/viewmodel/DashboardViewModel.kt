package com.example.lucimanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lucimanager.model.RouterInfo
import com.example.lucimanager.R
import com.example.lucimanager.repository.NetworkRepository
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val routerInfo: RouterInfo) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NetworkRepository(application)

    private val _state = MutableLiveData<DashboardState>()
    val state: LiveData<DashboardState> = _state

    fun loadRouterInfo() {
        _state.value = DashboardState.Loading
        viewModelScope.launch {
            val result = repository.getRouterInfo()
            _state.value = if (result.isSuccess) {
                DashboardState.Success(result.getOrThrow())
            } else {
                DashboardState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.error_loading))
            }
        }
    }
}
