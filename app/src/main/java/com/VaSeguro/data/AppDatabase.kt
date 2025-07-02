package com.VaSeguro.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.VaSeguro.data.Dao.Children.ChildDao
import com.VaSeguro.data.Dao.Message.MessageDao
import com.VaSeguro.data.Dao.Route.RouteDao
import com.VaSeguro.data.Dao.Stops.StopDao
import com.VaSeguro.data.Dao.User.UserDao
import com.VaSeguro.data.Dao.Vehicle.VehicleDao
import com.VaSeguro.data.Entitys.Children.ChildEntity
import com.VaSeguro.data.Entitys.Message.MessageEntity
import com.VaSeguro.data.Entitys.Routes.RouteEntity
import com.VaSeguro.data.Entitys.Stops.StopEntity
import com.VaSeguro.data.Entitys.User.UserEntity
import com.VaSeguro.data.Entitys.Vehicle.VehicleEntity

@Database(
    entities = [
        UserEntity::class,
        ChildEntity::class,
        RouteEntity::class,
        StopEntity::class,
        VehicleEntity::class,
        MessageEntity::class
    ],
    version = 5 // Increase version if you add new tables
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun childDao(): ChildDao
    abstract fun routeDao(): RouteDao
    abstract fun stopDao(): StopDao
    abstract fun vehicleDao():VehicleDao
    abstract fun messageDao(): MessageDao
}