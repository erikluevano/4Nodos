package com.example.movilsecure_v.modelo.basedatos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.movilsecure_v.modelo.entidades.Cita
import com.example.movilsecure_v.modelo.entidades.PerfilAdultoMayor
import com.example.movilsecure_v.modelo.entidades.ZonaFrecuente
import com.example.movilsecure_v.modelo.repositorio.CitasDAO
import com.example.movilsecure_v.modelo.repositorio.PerfilDao
import com.example.movilsecure_v.modelo.servicios.ZonaFrecuenteDao

// Se añade Cita a las entidades, se sube la versión y se registra el TypeConverter
@Database(entities = [ZonaFrecuente::class, PerfilAdultoMayor::class, Cita::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun zonaFrecuenteDao(): ZonaFrecuenteDao
    abstract fun perfilDao(): PerfilDao
    // Se añade el nuevo DAO
    abstract fun citasDao(): CitasDAO

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
