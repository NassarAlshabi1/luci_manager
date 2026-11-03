package com.example.lucimanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lucimanager.model.LoginCredentials
import com.example.lucimanager.model.LuciSession
import com.example.lucimanager.R
import com.example.lucimanager.repository.NetworkRepository
import com.example.lucimanager.utils.NetworkUtils
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val session: LuciSession) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NetworkRepository(application)
    
    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    private val _savedCredentials = MutableLiveData<LoginCredentials?>()
    val savedCredentials: LiveData<LoginCredentials?> = _savedCredentials

    init {
        loadSavedCredentials()
    }

    fun login(ipAddress: String, username: String, password: String) {
        if (ipAddress.isBlank() || username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error(getApplication<Application>().getString(R.string.error_empty_fields))
            return
        }

        if (!NetworkUtils.isValidIPAddress(ipAddress)) {
            _loginState.value = LoginState.Error(getApplication<Application>().getString(R.string.error_invalid_ip))
            return
        }

        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            val credentials = LoginCredentials(ipAddress, username, password)
            val result = repository.login(credentials)
            
            _loginState.value = if (result.isSuccess) {
                LoginState.Success(result.getOrThrow())
            } else {
                LoginState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.error_login_failed))
            }
        }
    }

    private fun loadSavedCredentials() {
        _savedCredentials.value = repository.getSavedCredentials()
    }

    fun clearError() {
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Idle
        }
    }
}