
package com.VaSeguro.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.VaSeguro.data.remote.RetrofitInstance
import com.VaSeguro.data.repository.AuthRepository.AuthRepositoryImpl
import com.VaSeguro.data.repository.ChatRepository.ChatRepositoryImpl
import com.VaSeguro.data.repository.RequestRepository.RequestRepositoryImpl
import com.VaSeguro.data.repository.Children.ChildrenRepositoryImpl
import com.VaSeguro.map.repository.SavedRoutesRepository
import com.VaSeguro.map.repository.StopPassengerRepository
import com.VaSeguro.data.repository.UserPreferenceRepository.UserPreferencesRepositoryImpl
import com.VaSeguro.data.repository.VehicleRepository.VehicleRepositoryImpl
import com.VaSeguro.map.repository.MapsApiRepositoryImpl
import com.VaSeguro.map.repository.RoutesApiRepositoryImpl
import com.VaSeguro.map.repository.SavedRoutesRepositoryImpl
import com.VaSeguro.map.repository.StopPassengerRepositoryImpl
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.graphics.createBitmap
import androidx.room.Room
import com.VaSeguro.data.repository.RouteRepository.RouteRepositoryImpl
import com.VaSeguro.data.repository.Stops.StopsRepository
import com.VaSeguro.data.repository.Stops.StopsRepositoryImpl
import com.VaSeguro.map.Supabase.SupabaseModule
import com.VaSeguro.map.repository.LocationRepository
import com.VaSeguro.map.repository.LocationRepositoryImpl
import com.VaSeguro.map.repository.StopRouteRepository
import com.VaSeguro.map.repository.StopRouteRepositoryImpl

private const val USER_PREFERENCE_NAME = "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCE_NAME)
class AppProvider(context: Context) {
    // Inicialización directa (sin lazy)
    private val userPreferencesRepository = UserPreferencesRepositoryImpl(context.dataStore)
    private val authRepository = AuthRepositoryImpl(RetrofitInstance.authService)
    private val mapsApiService = RetrofitInstance.mapsApiService
    private val routesApiService = RetrofitInstance.routesApiService
    private val savedRoutesService = RetrofitInstance.savedRoutesSevcie
    private val requestRepository= RequestRepositoryImpl(RetrofitInstance.requestService)
    private val mapsApiRepository = MapsApiRepositoryImpl(mapsApiService)
    private val routesApiRepository = RoutesApiRepositoryImpl(routesApiService)
    private val vehicleRespository = VehicleRepositoryImpl(RetrofitInstance.vehicleService)
    private val childrenRespository = ChildrenRepositoryImpl(RetrofitInstance.childrenService)
    private val chatRepository = ChatRepositoryImpl(RetrofitInstance.chatService,"https://sonoradinamita.live/")
    private val stopPassengerRepository = StopPassengerRepositoryImpl(RetrofitInstance.stopPassengerService, userPreferencesRepository)
    private val LocationRepository = LocationRepositoryImpl(SupabaseModule.supabaseClient)

    private val StopRouteRepository = StopRouteRepositoryImpl(
        RetrofitInstance.stopRouteService,
        userPreferencesRepository
    )

    private val savedRoutesRepository = SavedRoutesRepositoryImpl(savedRoutesService, userPreferencesRepository)


    private val StopsRepository = StopsRepositoryImpl(RetrofitInstance.stopsService)
    private val RoutesRepository= RouteRepositoryImpl(routeService=RetrofitInstance.routeService)
    private val appDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "va_seguro_db"
    ).fallbackToDestructiveMigration().build()
    fun provideUserPreferences() = userPreferencesRepository
    fun provideAuthRepository() = authRepository
    fun provideMapsApiRepository() = mapsApiRepository
    fun provideRoutesApiRepository() = routesApiRepository
    fun provideStopPassengerRepository() = stopPassengerRepository
    fun provideSavedRoutesRepository() = savedRoutesRepository
    fun provideRequestRepository() = requestRepository
    fun provideVehicleRepository() = vehicleRespository
    fun provideChildrenRepository() = childrenRespository
    fun provideChatRepository() = chatRepository
    fun provideLocationRepository() = LocationRepository
    fun provideStopRouteRepository()=StopRouteRepository
    fun provideStopsRepository(): StopsRepository = StopsRepository
    fun provideRoutesRepository(): RouteRepositoryImpl = RoutesRepository
    fun provideAppDatabase() = appDatabase
    fun provideUserDao() = appDatabase.userDao()
    fun provideChildDao() = appDatabase.childDao()
    fun provideRouteDao() = appDatabase.routeDao()
    fun provideStopDao() = appDatabase.stopDao()
    fun provideVehicleDao() = appDatabase.vehicleDao()
}

