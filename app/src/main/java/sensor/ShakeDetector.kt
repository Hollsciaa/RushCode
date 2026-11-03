package fr.cnam.rushcode.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {

    private var shakeTimestamp: Long = 0
    // Sensibilité de la secousse : 2.7G est un bon compromis (ni trop sensible, ni trop dur)
    private val SHAKE_THRESHOLD_GRAVITY = 2.7F
    // Temps minimum entre deux détections pour éviter le "spam" (500 millisecondes)
    private val SHAKE_SLOP_TIME_MS = 500

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // On divise par la gravité terrestre pour avoir une valeur standardisée (Force G)
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            // Formule mathématique pour calculer la force G totale
            val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                val now = System.currentTimeMillis()

                // On ignore les secousses trop rapprochées
                if (shakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return
                }

                shakeTimestamp = now
                onShake() // On déclenche l'action envoyée par le ViewModel
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Pas besoin de gérer la précision pour une simple secousse
    }
}