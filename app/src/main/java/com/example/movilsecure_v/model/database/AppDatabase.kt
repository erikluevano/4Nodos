package com.example.movilsecure_v.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.movilsecure_v.model.entities.ZonaFrecuente
import com.example.movilsecure_v.model.repository.ZonaFrecuenteDao

@Database(entities = [ZonaFrecuente::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun zonaFrecuenteDao(): ZonaFrecuenteDao

    companion object {
        // La instancia es 'volatile' para asegurar que siempre esté actualizada
        // y sea visible para todos los hilos de ejecución.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Retorna la instancia si ya existe, si no, la crea de forma segura (synchronized)
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "movilsecure_database" // Nombre del archivo de la base de datos
                ).build()
                INSTANCE = instance
                // retorna la instancia
                instance
            }
        }
    }
}