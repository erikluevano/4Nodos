
package com.example.movilsecure_v.vista

import com.example.movilsecure_v.viewmodel.PerfilVM

// Esta clase actúa como un controlador o presentador para la UI de Perfil.
// Su constructor ahora requiere que se le pase una instancia de PerfilVM.
class FormPerfilUI(
    val viewModel: PerfilVM
) {

    fun mostrarOpcionRegistrar() {
        // Llama al ViewModel para cambiar el estado y mostrar el formulario
        viewModel.onMostrarFormulario()
    }

    // Este método ya no es necesario aquí, la UI principal decidirá qué mostrar
    fun mostrarFormulario() {
       // La UI reaccionará al estado `mostrandoFormulario` del `UiState`
    }

    // El método que la UI llamará cuando el usuario presione "Guardar"
    fun validarYGuardar() {
        viewModel.registrarPerfil()
    }

    fun cancelarRegistro() {
        // Llama al ViewModel para cambiar el estado y ocultar el formulario
        viewModel.onOcultarFormulario()
    }

    // El resto de los métodos se mantienen por si la especificación lo requiere,
    // pero la comunicación de errores/confirmaciones se maneja principalmente
    // a través de la observación del `UiState` en la UI de Compose.

    fun mostrarConfirmacion() {
        // La UI reaccionará al `mensajeConfirmacion` del `UiState`
    }

    fun mostrarErrorMensaje(mensaje: String) {
         // La UI reaccionará al `mensajeError` del `UiState`
    }

    fun mostrarErrorValidacion(detalles: String) {
        // La UI reaccionará al `mensajeError` del `UiState`
    }

    fun actualizarListaPerfiles() {
        // Esta lógica ahora está implícita en el ViewModel
    }
}
