package com.example.movilsecure_v.modelo.repositorio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movilsecure_v.modelo.entidades.Medicamento

@Dao
interface MedicamentosDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun InsertarDatosMedicamento(medicamento: Medicamento)

    @Query("SELECT * FROM medicamentos ORDER BY nombre ASC")
    suspend fun ConsultarMedicamentos(): List<Medicamento>

    @Query("SELECT * FROM medicamentos WHERE ID = :id")
    suspend fun obtenerMedicamentoPorId(id: Int): Medicamento?

    @Query("DELETE FROM medicamentos WHERE ID = :id")
    suspend fun eliminarMedicamento(id: Int)
}
