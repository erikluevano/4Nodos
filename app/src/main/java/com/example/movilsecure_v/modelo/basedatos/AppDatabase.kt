
package com.example.movilsecure_v.modelo.basedatos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.movilsecure_v.modelo.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.entidades.ZonaFrecuente
import com.example.movilsecure_v.modelo.repositorio.PerfilDao
import com.example.movilsecure_v.modelo.repositorio.ZonaFrecuenteDao

@Database(entities = [ZonaFrecuente::class, PerfilAdultoMayor::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun zonaFrecuenteDao(): ZonaFrecuenteDao
    abstract fun perfilDao(): PerfilDao

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
                // Para una app en producción, aquí se manejaría una migración custom.
                // Para este caso, una migración destructiva es suficiente.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
