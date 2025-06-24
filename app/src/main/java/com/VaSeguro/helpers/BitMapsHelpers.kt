package com.VaSeguro.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun Context.bitmapDescriptorFromVector(
    @DrawableRes resId: Int,
    heightDp: Int = 48,
    tintColor: Int? = null
): BitmapDescriptor {
    val drawable = ContextCompat.getDrawable(this, resId)
        ?: return BitmapDescriptorFactory.defaultMarker()

    val density = resources.displayMetrics.density
    val heightPx = (heightDp * density).toInt()

    // Obtener proporciones originales del drawable
    val ratio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
    val widthPx = (heightPx * ratio).toInt()

    // Tinte si aplica
    tintColor?.let { drawable.setTint(it) }

    // Crear bitmap con dimensiones proporcionales
    val bitmap = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}