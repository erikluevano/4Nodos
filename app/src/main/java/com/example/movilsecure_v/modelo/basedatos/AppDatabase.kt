package com.example.movilsecure_v.modelo.basedatos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.movilsecure_v.modelo.entidades.Cita
import com.example.movilsecure_v.modelo.entidades.Medicamento
import com.example.movilsecure_v.modelo.entidades.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.entidades.ZonaFrecuente
import com.example.movilsecure_v.modelo.repositorio.CitasDAO
import com.example.movilsecure_v.modelo.repositorio.MedicamentosDAO
import com.example.movilsecure_v.modelo.repositorio.PerfilDao
import com.example.movilsecure_v.modelo.servicios.ZonaFrecuenteDao

// Se sube la versión a 5 para reflejar el cambio en la entidad Medicamento
@Database(entities = [ZonaFrecuente::class, PerfilAdultoMayor::class, Cita::class, Medicamento::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun zonaFrecuenteDao(): ZonaFrecuenteDao
    abstract fun perfilDao(): PerfilDao
    abstract fun citasDao(): CitasDAO
    abstract fun medicamentosDAO(): MedicamentosDAO

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
