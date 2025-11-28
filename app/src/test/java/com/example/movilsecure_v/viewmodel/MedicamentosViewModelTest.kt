package com.example.movilsecure_v.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.movilsecure_v.modelo.entidades.Medicamento
import com.example.movilsecure_v.modelo.repositorio.RepositorioMedicamentos
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

@ExperimentalCoroutinesApi // Necesario para usar TestCoroutineDispatcher
class MedicamentosViewModelTest {

    // Regla para ejecutar tareas de LiveData de forma síncrona
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Dispatcher de prueba para controlar las coroutines
    private val testDispatcher = StandardTestDispatcher()

    // Mock del repositorio de medicamentos
    private lateinit var mockRepositorio: RepositorioMedicamentos
    // Instancia del ViewModel a probar
    private lateinit var viewModel: MedicamentosViewModel

    // Configuración inicial para cada prueba
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher) // Establece el dispatcher principal para las coroutines
        mockRepositorio = mockk(relaxed = true) // Crea un mock del repositorio (relaxed = true evita errores por llamadas no esperadas)
        viewModel = MedicamentosViewModel(mockRepositorio) // Inyecta el mock al ViewModel
    }

    // Limpieza después de cada prueba
    @After
    fun tearDown() {
        Dispatchers.resetMain() // Resetea el dispatcher principal
    }

    // --- Pruebas para CU12: Agregar medicamentos ---

    @Test
    fun testCU12_agregarMedicamentoNuevoCorrectamente() = runTest(testDispatcher) {
        // Escenario: Agregar un medicamento nuevo correctamente
        // Resultado Esperado: La función encargada de insertar el medicamento debe llamarse una sola vez.
        // El estado de la UI debe reflejar el éxito (formulario cerrado, diálogo de éxito, campos limpios).

        val medicamentoValido = Medicamento(
            ID = 0, // Corregido de id a ID
            nombre = "Aspirina",
            tipoMedicamento = "Pastillas",
            horaInicio = "08:00",
            frecuencia = "8",
            notificacionesActivas = true
        )

        // Configurar el mock: cuando se llame a GuardarDatosMedicamento, no hace nada (Unit)
        coEvery { mockRepositorio.GuardarDatosMedicamento(any()) } returns Unit
        // Configurar el mock para ObtenerMedicamentos, ya que se llama después de guardar
        coEvery { mockRepositorio.ObtenerMedicamentos() } returns emptyList() // O una lista actualizada si se quiere probar más a fondo

        // Acción
        viewModel.EnviarFormularioRegistro(medicamentoValido)
        advanceUntilIdle() // Asegura que todas las coroutines terminen

        // Verificación
        // El repositorio debe haber sido llamado para guardar el medicamento una vez
        coVerify(exactly = 1) { mockRepositorio.GuardarDatosMedicamento(eq(medicamentoValido)) }
        // Se debe haber llamado para obtener la lista (refresh)
        coVerify(exactly = 1) { mockRepositorio.ObtenerMedicamentos() }

        // El estado de la UI debe estar correcto
        assertFalse("El formulario debería estar oculto", viewModel.uiState.value.mostrandoFormulario)
        assertTrue("El diálogo de éxito debería mostrarse", viewModel.uiState.value.mostrandoDialogoExito)
        assertEquals("El nombre debería estar limpio", "", viewModel.uiState.value.nombre)
        assertNull("No debería haber mensaje de error", viewModel.uiState.value.mensajeError)
    }

    @Test
    fun testCU12_intentarAgregarMedicamentoConNombreVacio() = runTest(testDispatcher) {
        // Escenario: Intentar agregar medicamento con nombre vacío
        // Resultado Esperado: La función debe retornar error o no llamar al repositorio.

        val medicamentoConNombreVacio = Medicamento(
            ID = 0, // Corregido de id a ID
            nombre = "   ", // Espacios en blanco para simular vacío
            tipoMedicamento = "Pastillas",
            horaInicio = "08:00",
            frecuencia = "8",
            notificacionesActivas = true
        )

        // Acción
        viewModel.EnviarFormularioRegistro(medicamentoConNombreVacio)
        advanceUntilIdle()

        // Verificación
        // El repositorio NO debe haber sido llamado para guardar
        coVerify(exactly = 0) { mockRepositorio.GuardarDatosMedicamento(any()) }

        // El mensaje de error debe haberse actualizado
        assertNotNull("El mensaje de error no debería ser nulo", viewModel.uiState.value.mensajeError)
        assertEquals("El nombre del medicamento no puede estar vacío.", viewModel.uiState.value.mensajeError)
        // El formulario debería seguir mostrándose
        assertTrue("El formulario debería seguir mostrándose", viewModel.uiState.value.mostrandoFormulario)
        // El diálogo de éxito NO debería mostrarse
        assertFalse("El diálogo de éxito no debería mostrarse", viewModel.uiState.value.mostrandoDialogoExito)
    }

    // --- Pruebas para CU13: Consultar medicamentos ---

    @Test
    fun testCU13_consultarMedicamentosCuandoHayDatos() = runTest(testDispatcher) {
        // Escenario: Consultar medicamentos cuando hay datos
        // Resultado Esperado: Debe retornar una lista con el tamaño correcto (ej. 2 items).

        val medicamento1 = Medicamento(ID = 1, "Paracetamol", "Pastillas", "10:00", "6", true)
        // Ajustamos la hora de inicio de Ibuprofeno para que Paracetamol siempre sea el primero al ordenar.
        val medicamento2 = Medicamento(ID = 2, "Ibuprofeno", "Jarabe", "23:00", "8", true)
        val medicamentosDelRepo = listOf(medicamento1, medicamento2)

        // Configurar el mock para que devuelva la lista de medicamentos
        coEvery { mockRepositorio.ObtenerMedicamentos() } returns medicamentosDelRepo

        // Acción: Se llama a ObtenerListaMedicamentos para iniciar la carga
        viewModel.ObtenerListaMedicamentos()
        advanceUntilIdle()

        // Verificación
        // El repositorio debe haber sido llamado para obtener los medicamentos
        coVerify(exactly = 1) { mockRepositorio.ObtenerMedicamentos() }

        // La lista de medicamentos en el ViewModel debe contener 2 elementos
        assertEquals(2, viewModel.listaMedicamentos.value.size)
        // Verificamos que el primer elemento sea 'Paracetamol' después de ordenar
        assertEquals("Paracetamol", viewModel.listaMedicamentos.value.first().medicamento.nombre)
    }

    @Test
    fun testCU13_consultarMedicamentosCuandoNoHayDatos() = runTest(testDispatcher) {
        // Escenario: Consultar medicamentos cuando no hay datos
        // Resultado Esperado: Debe retornar una lista vacía.

        // Configurar el mock para que devuelva una lista vacía
        coEvery { mockRepositorio.ObtenerMedicamentos() } returns emptyList()

        // Acción
        viewModel.ObtenerListaMedicamentos()
        advanceUntilIdle()

        // Verificación
        // El repositorio debe haber sido llamado
        coVerify(exactly = 1) { mockRepositorio.ObtenerMedicamentos() }

        // La lista de medicamentos en el ViewModel debe estar vacía
        assertTrue("La lista de medicamentos debería estar vacía", viewModel.listaMedicamentos.value.isEmpty())
    }

    // --- Pruebas para CU14: Eliminar medicamento del horario ---

    @Test
    fun testCU14_eliminarMedicamentoExistentePorID() = runTest(testDispatcher) {
        // Escenario: Eliminar un medicamento existente por ID
        // Resultado Esperado: El repositorio debe recibir la orden de eliminar ese ID en específico.
        // La lista de medicamentos en el ViewModel debe actualizarse.

        val idAEliminar = 1

        // Configurar el mock para que eliminarMedicamento no haga nada (Unit)
        coEvery { mockRepositorio.eliminarMedicamento(any()) } returns Unit
        // Configurar el mock para ObtenerMedicamentos, ya que se llamará después de eliminar
        coEvery { mockRepositorio.ObtenerMedicamentos() } returns emptyList() // Simula que la lista queda vacía después de eliminar

        // Acción
        viewModel.eliminarMedicamento(idAEliminar)
        advanceUntilIdle()

        // Verificación
        // El repositorio debe haber sido llamado para eliminar con el ID correcto
        coVerify(exactly = 1) { mockRepositorio.eliminarMedicamento(eq(idAEliminar)) }
        // Se debe haber llamado para obtener la lista (refresh)
        coVerify(exactly = 1) { mockRepositorio.ObtenerMedicamentos() }
        // La lista de medicamentos en el ViewModel debería estar vacía (según nuestro mock)
        assertTrue("La lista de medicamentos debería estar vacía después de eliminar", viewModel.listaMedicamentos.value.isEmpty())
    }
}
