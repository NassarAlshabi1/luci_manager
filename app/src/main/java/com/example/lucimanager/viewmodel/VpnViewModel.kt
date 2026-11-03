package com.example.lucimanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lucimanager.model.VpnConnection
import com.example.lucimanager.R
import com.example.lucimanager.repository.NetworkRepository
import kotlinx.coroutines.launch

sealed class VpnState {
    object Loading : VpnState()
    data class Success(val connections: List<VpnConnection>) : VpnState()
    data class Error(val message: String) : VpnState()
}

sealed class VpnToggleState {
    object Idle : VpnToggleState()
    data class Loading(val vpnName: String) : VpnToggleState()
    data class Success(val vpnName: String) : VpnToggleState()
    data class Error(val message: String) : VpnToggleState()
}

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NetworkRepository(application)

    private val _state = MutableLiveData<VpnState>()
    val state: LiveData<VpnState> = _state

    private val _toggleState = MutableLiveData<VpnToggleState>(VpnToggleState.Idle)
    val toggleState: LiveData<VpnToggleState> = _toggleState

    fun loadVpnConnections() {
        _state.value = VpnState.Loading
        viewModelScope.launch {
            val result = repository.getVpnConnections()
            _state.value = if (result.isSuccess) {
                VpnState.Success(result.getOrThrow())
            } else {
                VpnState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.error_loading))
            }
        }
    }

    fun toggleVpn(vpn: VpnConnection, enable: Boolean) {
        _toggleState.value = VpnToggleState.Loading(vpn.name)
        viewModelScope.launch {
            val result = repository.toggleVpn(vpn, enable)
            if (result.isSuccess) {
                _toggleState.value = VpnToggleState.Success(vpn.name)
                loadVpnConnections()
            } else {
                _toggleState.value = VpnToggleState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.vpn_toggle_error))
            }
        }
    }
}
