package com.VaSeguro.data.model
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.VaSeguro.R

data class TripSlide(
    val id: Int,
    val title: String,
    val description: String,
    val imageRes: Int,
    val textOffsetY: Int = (-80)
)
object TripData {
    val slides = listOf(
        TripSlide(
            1,
            "We take care of \nevery kilometer",
            "of your trip",
            R.drawable.bus_title,
            -80,
        ),
        TripSlide(
            2,
            "While they \ntravel you see",
            "them arrive",
            R.drawable.family_title,
            -50
        ),
        TripSlide(
            3,
            "Because \nsecurity can",
            "be digital",
            R.drawable.school_title,
            -80,
        )
    )
}
