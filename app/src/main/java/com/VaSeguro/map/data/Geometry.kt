package com.VaSeguro.map.data

/**
 * Contiene información geométrica para un lugar.
 */
data class Geometry(
    val location: Location
) {
    companion object {
        /**
         * Crea un objeto Geometry a partir de un LatLng
         */
        fun fromLatLng(latLng: com.google.android.gms.maps.model.LatLng): Geometry {
            return Geometry(Location.fromLatLng(latLng))
        }
    }
}

