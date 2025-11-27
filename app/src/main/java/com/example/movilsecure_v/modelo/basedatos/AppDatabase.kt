// ruta: app/src/main/java/com/example/movilsecure_v/modelo/basedatos/AppDatabase.kt
package com.example.movilsecure_v.modelo.basedatos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.movilsecure_v.modelo.dao.CitasDAO
import com.example.movilsecure_v.modelo.dao.HistorialDAO // <-- 1. IMPORTAR EL NUEVO DAO
import com.example.movilsecure_v.modelo.dao.MedicamentosDAO
import com.example.movilsecure_v.modelo.dao.PerfilDao
import com.example.movilsecure_v.modelo.dao.ZonaFrecuenteDao
import com.example.movilsecure_v.modelo.entidades.Cita
import com.example.movilsecure_v.modelo.entidades.Medicamento
import com.example.movilsecure_v.modelo.entidades.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.entidades.RegistroHistorial // <-- 2. IMPORTAR LA NUEVA ENTIDAD
import com.example.movilsecure_v.modelo.entidades.ZonaFrecuente

// Se sube la versión a 6 para reflejar la adición de la tabla de historial de navegación
@Database(
    entities = [
        ZonaFrecuente::class,
        PerfilAdultoMayor::class,
        Cita::class,
        Medicamento::class,
        RegistroHistorial::class // <-- 3. AÑADIR LA NUEVA ENTIDAD A LA LISTA
    ],
    version = 6, // <-- 4. INCREMENTAR LA VERSIÓN DE LA BASE DE DATOS
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun zonaFrecuenteDao(): ZonaFrecuenteDao
    abstract fun perfilDao(): PerfilDao
    abstract fun citasDao(): CitasDAO
    abstract fun medicamentosDAO(): MedicamentosDAO
    abstract fun historialDAO(): HistorialDAO // <-- 5. AÑADIR LA FUNCIÓN PARA EL NUEVO DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "movilsecure_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}