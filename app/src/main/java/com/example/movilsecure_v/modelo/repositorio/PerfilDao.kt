
package com.example.movilsecure_v.modelo.repositorio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movilsecure_v.modelo.PerfilAdultoMayor
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {

    @Query("SELECT * FROM perfiles_adulto_mayor ORDER BY nombre ASC")
    fun getAllPerfiles(): Flow<List<PerfilAdultoMayor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerfil(perfil: PerfilAdultoMayor)

    @Query("DELETE FROM perfiles_adulto_mayor WHERE id = :perfilId")
    suspend fun deletePerfilById(perfilId: String)
}
