package com.VaSeguro.map.data

/**
 * Representa una ubicación geográfica con coordenadas de latitud y longitud.
 */
data class Location(
    val lat: Double,
    val lng: Double
) {
    /**
     * Convierte la ubicación a un objeto LatLng de Google Maps
     */
    fun toLatLng() = com.google.android.gms.maps.model.LatLng(lat, lng)

    companion object {
        /**
         * Crea un objeto Location a partir de un LatLng
         */
        fun fromLatLng(latLng: com.google.android.gms.maps.model.LatLng): Location {
            return Location(latLng.latitude, latLng.longitude)
        }

        /**
         * Convierte una lista de objetos Location a una lista de LatLng
         */
        fun listToLatLngList(locations: List<Location>): List<com.google.android.gms.maps.model.LatLng> {
            return locations.map { it.toLatLng() }
        }
    }
}
