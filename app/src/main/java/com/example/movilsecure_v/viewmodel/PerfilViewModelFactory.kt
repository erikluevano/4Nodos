
package com.example.movilsecure_v.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.movilsecure_v.modelo.repositorio.RepositorioPerfil

class PerfilViewModelFactory(private val repositorio: RepositorioPerfil) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilVM::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PerfilVM(repositorio) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
