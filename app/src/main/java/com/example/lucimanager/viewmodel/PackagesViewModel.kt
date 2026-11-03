package com.example.lucimanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lucimanager.model.OpkgPackage
import com.example.lucimanager.R
import com.example.lucimanager.repository.NetworkRepository
import kotlinx.coroutines.launch

sealed class PackagesState {
    object Loading : PackagesState()
    data class Success(val packages: List<OpkgPackage>) : PackagesState()
    data class Error(val message: String) : PackagesState()
}

sealed class PackageActionState {
    object Idle : PackageActionState()
    object Loading : PackageActionState()
    data class Success(val message: String) : PackageActionState()
    data class Error(val message: String) : PackageActionState()
}

class PackagesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NetworkRepository(application)

    private val _state = MutableLiveData<PackagesState>()
    val state: LiveData<PackagesState> = _state

    private val _actionState = MutableLiveData<PackageActionState>(PackageActionState.Idle)
    val actionState: LiveData<PackageActionState> = _actionState

    private var allPackages = listOf<OpkgPackage>()
    private var currentQuery = ""

    fun loadInstalledPackages() {
        _state.value = PackagesState.Loading
        viewModelScope.launch {
            val result = repository.getInstalledPackages()
            if (result.isSuccess) {
                allPackages = result.getOrThrow()
                applyFilter()
            } else {
                _state.value = PackagesState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.error_loading))
            }
        }
    }

    fun searchPackages(query: String) {
        currentQuery = query
        if (query.length < 2) {
            applyFilter()
            return
        }
        _state.value = PackagesState.Loading
        viewModelScope.launch {
            val searchResult = repository.searchPackages(query)
            val installedResult = repository.getInstalledPackages()

            val installedNames = installedResult.getOrNull()?.map { it.name }?.toSet() ?: emptySet()

            if (searchResult.isSuccess) {
                val combined = searchResult.getOrThrow().map { pkg ->
                    pkg.copy(isInstalled = installedNames.contains(pkg.name))
                }
                _state.value = PackagesState.Success(combined)
            } else {
                _state.value = PackagesState.Error(searchResult.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.error_loading))
            }
        }
    }

    private fun applyFilter() {
        val filtered = if (currentQuery.isBlank()) {
            allPackages
        } else {
            allPackages.filter { it.name.contains(currentQuery, ignoreCase = true) }
        }
        _state.value = PackagesState.Success(filtered)
    }

    fun installPackage(packageName: String) {
        _actionState.value = PackageActionState.Loading
        viewModelScope.launch {
            val result = repository.installPackage(packageName)
            _actionState.value = if (result.isSuccess) {
                loadInstalledPackages()
                PackageActionState.Success(packageName)
            } else {
                PackageActionState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.packages_install_error))
            }
        }
    }

    fun removePackage(packageName: String) {
        _actionState.value = PackageActionState.Loading
        viewModelScope.launch {
            val result = repository.removePackage(packageName)
            _actionState.value = if (result.isSuccess) {
                loadInstalledPackages()
                PackageActionState.Success(packageName)
            } else {
                PackageActionState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.packages_remove_error))
            }
        }
    }

    fun updateLists() {
        _actionState.value = PackageActionState.Loading
        viewModelScope.launch {
            val result = repository.updatePackageLists()
            _actionState.value = if (result.isSuccess) {
                PackageActionState.Success(getApplication<Application>().getString(R.string.packages_lists_updated))
            } else {
                PackageActionState.Error(result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.error_generic))
            }
        }
    }

    fun clearActionState() {
        _actionState.value = PackageActionState.Idle
    }
}
