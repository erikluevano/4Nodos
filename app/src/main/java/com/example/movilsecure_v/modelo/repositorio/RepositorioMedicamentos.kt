package com.example.movilsecure_v.modelo.repositorio

import com.example.movilsecure_v.modelo.dao.MedicamentosDAO
import com.example.movilsecure_v.modelo.entidades.Medicamento

class RepositorioMedicamentos(private val medicamentosDAO: MedicamentosDAO) {

    suspend fun ObtenerMedicamentos(): List<Medicamento> {
        return medicamentosDAO.ConsultarMedicamentos()
    }

    suspend fun GuardarDatosMedicamento(medicamento: Medicamento) {
        medicamentosDAO.InsertarDatosMedicamento(medicamento)
    }

    suspend fun obtenerMedicamentoPorId(id: Int): Medicamento? {
        return medicamentosDAO.obtenerMedicamentoPorId(id)
    }

    suspend fun eliminarMedicamento(id: Int) {
        medicamentosDAO.eliminarMedicamento(id)
    }
}
