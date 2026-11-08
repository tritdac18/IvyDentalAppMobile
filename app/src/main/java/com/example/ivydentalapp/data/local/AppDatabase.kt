package com.example.ivydentalapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ivydentalapp.data.local.dao.PatientDao
import com.example.ivydentalapp.data.local.dao.ReceptionistDao
import com.example.ivydentalapp.data.local.dao.PaymentDao
import com.example.ivydentalapp.data.local.dao.ServiceDao
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import com.example.ivydentalapp.data.local.entity.InvoiceEntity
import com.example.ivydentalapp.data.local.entity.PatientAllergyEntity
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import com.example.ivydentalapp.data.local.entity.PaymentEntity
import com.example.ivydentalapp.data.local.entity.ServiceEntity

@Database(
    entities = [
        PatientEntity::class,
        AppointmentEntity::class,
        InvoiceEntity::class,
        PatientAllergyEntity::class,
        ReceptionistEntity::class,
        PaymentEntity::class,
        ServiceEntity::class,
        com.example.ivydentalapp.data.local.entity.NotificationEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun appointmentDao(): com.example.ivydentalapp.data.local.dao.AppointmentDao
    abstract fun invoiceDao(): com.example.ivydentalapp.data.local.dao.InvoiceDao
    abstract fun receptionistDao(): ReceptionistDao
    abstract fun paymentDao(): PaymentDao
    abstract fun serviceDao(): ServiceDao
    abstract fun notificationDao(): com.example.ivydentalapp.data.local.dao.NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                android.util.Log.d("AppDatabase", "Creating new database instance...")
                
                val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
                    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                        database.execSQL("CREATE TABLE IF NOT EXISTS `tbl_notifications` (`notification_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `user_id` INTEGER NOT NULL, `user_type` TEXT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `is_read` INTEGER NOT NULL, `created_at` TEXT NOT NULL)")
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ivy_dental_database"
                )
                .addMigrations(MIGRATION_8_9)
                .fallbackToDestructiveMigration() // Still fallback if migration fails or other version mismatch
                .build()
                INSTANCE = instance
                android.util.Log.d("AppDatabase", "Database instance created successfully")
                instance
            }
        }
    }
}
