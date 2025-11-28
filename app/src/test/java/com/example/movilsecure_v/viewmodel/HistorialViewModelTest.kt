package com.example.movilsecure_v.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.movilsecure_v.modelo.entidades.RegistroHistorial
import com.example.movilsecure_v.modelo.repositorio.HistorialRepositorio
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@ExperimentalCoroutinesApi
class HistorialViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockRepositorio: HistorialRepositorio
    private lateinit var viewModel: HistorialViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepositorio = mockk(relaxed = true)
        // ViewModel instantiation moved to each test method
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Pruebas para CU15: Ver historial ubicaciones ---

    @Test
    fun testCU15_verHistorialCuandoHayDatos_retornaListaCorrecta() = runTest(testDispatcher) {
        // Escenario: Ver historial de ubicaciones cuando hay datos
        // Resultado Esperado: Debe retornar la lista de ubicaciones en orden cronológico.

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val registro1 = RegistroHistorial(1, "Origen A", "Destino B", dateFormat.format(Date(1000)))
        val registro2 = RegistroHistorial(2, "Origen C", "Destino D", dateFormat.format(Date(2000)))
        val historialDelRepo = listOf(registro1, registro2)

        // Configurar el mock para que devuelva el historial
        coEvery { mockRepositorio.obtenerHistorial() } returns historialDelRepo

        // Acción: Instanciar ViewModel después de configurar el mock
        viewModel = HistorialViewModel(mockRepositorio)
        advanceUntilIdle() // Se mantiene para procesar el init block

        // Verificación
        // El repositorio debe haber sido llamado para obtener el historial (por el init block)
        coVerify(exactly = 1) { mockRepositorio.obtenerHistorial() }

        // El estado de carga debe ser false al finalizar
        assertFalse("isLoading debería ser false", viewModel.uiState.value.isLoading)
        // El error debe ser nulo
        assertNull("No debería haber mensaje de error", viewModel.uiState.value.error)
        // La lista del historial debe contener los elementos esperados
        assertEquals(2, viewModel.uiState.value.historial.size)
        assertEquals("Origen A", viewModel.uiState.value.historial[0].origen)
        assertEquals("Destino D", viewModel.uiState.value.historial[1].destino)
    }

    @Test
    fun testCU15_verHistorialCuandoNoHayUbicacionesVisitadas_retornaListaVacia() = runTest(testDispatcher) {
        // Escenario: Ver historial de ubicaciones cuando no hay ubicaciones visitadas
        // Resultado Esperado: Debe mostrar un mensaje indicando que no hay historial actualmente.
        // (En este caso, el ViewModel emitiría una lista vacía y la UI se encargaría del mensaje)

        // Configurar el mock para que devuelva una lista vacía
        coEvery { mockRepositorio.obtenerHistorial() } returns emptyList()

        // Acción: Instanciar ViewModel después de configurar el mock
        viewModel = HistorialViewModel(mockRepositorio)
        advanceUntilIdle() // Se mantiene para procesar el init block

        // Verificación
        coVerify(exactly = 1) { mockRepositorio.obtenerHistorial() }

        assertFalse("isLoading debería ser false", viewModel.uiState.value.isLoading)
        assertNull("No debería haber mensaje de error", viewModel.uiState.value.error)
        assertTrue("La lista del historial debería estar vacía", viewModel.uiState.value.historial.isEmpty())
    }

    @Test
    fun testCU15_falloAlCargarHistorial_muestraMensajeError() = runTest(testDispatcher) {
        // Escenario: Fallo al cargar el historial
        // Resultado Esperado: uiState.error se actualiza con el mensaje "Error al cargar la información".

        // Configurar el mock para que lance una excepción al obtener el historial
        coEvery { mockRepositorio.obtenerHistorial() } throws RuntimeException("Error de red simulado")

        // Acción: Instanciar ViewModel después de configurar el mock
        viewModel = HistorialViewModel(mockRepositorio)
        advanceUntilIdle() // Se mantiene para procesar el init block

        // Verificación
        coVerify(exactly = 1) { mockRepositorio.obtenerHistorial() }

        assertFalse("isLoading debería ser false", viewModel.uiState.value.isLoading)
        assertNotNull("Debería haber un mensaje de error", viewModel.uiState.value.error)
        assertEquals("Error al cargar la información", viewModel.uiState.value.error)
        assertTrue("La lista del historial debería estar vacía o no modificada", viewModel.uiState.value.historial.isEmpty())
    }

    @Test
    fun testCU15_guardarNuevoRegistroHistorial_llamaRepositorioYActualizaLista() = runTest(testDispatcher) {
        // Escenario: Guardar un nuevo registro de historial
        // Resultado Esperado: repositorio.guardarRegistro se llama, y luego se recarga el historial.

        val origen = "Mi Casa"
        val destino = "Hospital Central"
        val registrosIniciales = listOf(RegistroHistorial(1, "Otro Origen", "Otro Destino", "2023-01-01T10:00:00"))
        val registrosDespuesDeGuardar = listOf(
            registrosIniciales[0],
            RegistroHistorial(0, origen, destino, SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())) // ID 0 para mock
        )

        // Configurar el mock para la carga inicial y después de guardar
        coEvery { mockRepositorio.obtenerHistorial() } returnsMany listOf(registrosIniciales, registrosDespuesDeGuardar)
        coEvery { mockRepositorio.guardarRegistro(any()) } returns Unit

        // Primero, aseguramos que el historial se carga inicialmente (opcional, pero buena práctica)
        // Instanciar ViewModel después de configurar el mock inicial
        viewModel = HistorialViewModel(mockRepositorio)
        advanceUntilIdle() // Esperamos a que el init block termine su ejecución
        assertEquals(1, viewModel.uiState.value.historial.size)

        // Acción: Activar navegación para guardar un nuevo registro
        viewModel.activarNavegacion(origen, destino)
        advanceUntilIdle()

        // Verificación
        // El repositorio debe haber sido llamado para guardar el nuevo registro
        coVerify(exactly = 1) { mockRepositorio.guardarRegistro(match { it.origen == origen && it.destino == destino }) }
        // El repositorio debe haber sido llamado para obtener el historial dos veces (inicial por init block y después de guardar por el ViewModel)
        coVerify(exactly = 2) { mockRepositorio.obtenerHistorial() }

        assertFalse("isLoading debería ser false", viewModel.uiState.value.isLoading)
        assertNull("No debería haber mensaje de error", viewModel.uiState.value.error)
        // Verificar que la lista del historial se ha actualizado
        assertEquals(2, viewModel.uiState.value.historial.size)
        assertEquals(destino, viewModel.uiState.value.historial[1].destino)
    }

    @Test
    fun testCU15_falloAlGuardarRegistroHistorial_muestraMensajeError() = runTest(testDispatcher) {
        // Escenario: Fallo al guardar un nuevo registro de historial
        // Resultado Esperado: uiState.error se actualiza con el mensaje "Error al guardar el registro".

        val origen = "Casa"
        val destino = "Trabajo"

        // Configurar el mock para que lance una excepción al guardar el registro
        coEvery { mockRepositorio.guardarRegistro(any()) } throws RuntimeException("Error de DB simulado")
        // Configurar el mock para la carga inicial del historial (para que no falle ahí)
        coEvery { mockRepositorio.obtenerHistorial() } returns emptyList()

        // Aseguramos que el historial se carga inicialmente para tener un estado base (por el init block)
        // Instanciar ViewModel después de configurar el mock inicial
        viewModel = HistorialViewModel(mockRepositorio)
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.error)

        // Acción: Intentar guardar el registro
        viewModel.activarNavegacion(origen, destino)
        advanceUntilIdle()

        // Verificación
        coVerify(exactly = 1) { mockRepositorio.guardarRegistro(any()) }
        // Solo una llamada a obtenerHistorial() porque la segunda se evita por el error
        coVerify(exactly = 1) { mockRepositorio.obtenerHistorial() }

        assertFalse("isLoading debería ser false", viewModel.uiState.value.isLoading)
        assertNotNull("Debería haber un mensaje de error", viewModel.uiState.value.error)
        assertEquals("Error al guardar el registro", viewModel.uiState.value.error)
        assertTrue("La lista del historial no debería cambiar", viewModel.uiState.value.historial.isEmpty())
    }
}