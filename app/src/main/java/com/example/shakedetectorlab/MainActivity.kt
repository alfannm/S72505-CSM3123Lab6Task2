package com.example.shakedetectorlab

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : AppCompatActivity(), SensorEventListener {

    // Constants for shake detection
    private val SHAKE_THRESHOLD = 2.7f
    private val SHAKE_COOLDOWN = 800L

    // Variables to track state
    private var lastShakeTime: Long = 0
    private var shakeCount = 0

    // Sensor components
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // UI components
    private lateinit var rootLayout: RelativeLayout
    private lateinit var tvShakeCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Link UI components
        rootLayout = findViewById(R.id.shakeRootLayout)
        tvShakeCount = findViewById(R.id.shakeCounterLabel)

        // 2. Initialize Sensor Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Check if device actually has an accelerometer
        if (accelerometer == null) {
            Toast.makeText(this, "Error: No Accelerometer found!", Toast.LENGTH_LONG).show()
        }
    }

    // Lifecycle Method: Start listening when app is open
    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Lifecycle Method: Stop listening when app is closed/paused to save battery
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // This method runs every time the sensor updates (very fast!)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // 1. Extract X, Y, Z values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // 2. Calculate G-Force (Normalize gravity: divide by 9.81)
            val gX = x / 9.81f
            val gY = y / 9.81f
            val gZ = z / 9.81f

            // Formula: gForce = sqrt(gX^2 + gY^2 + gZ^2)
            val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

            // 3. Detect Shake
            if (gForce > SHAKE_THRESHOLD) {
                val currentTime = System.currentTimeMillis()
                // Check cooldown (ensure 800ms has passed since last shake)
                if (currentTime - lastShakeTime > SHAKE_COOLDOWN) {
                    lastShakeTime = currentTime
                    handleShakeEvent()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used for this lab
    }

    // Action to perform when a valid shake is detected
    private fun handleShakeEvent() {
        // Increment counter
        shakeCount++
        tvShakeCount.text = "Shake Count: $shakeCount"

        // Change background to a random color
        val randomColor = Color.rgb(
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )
        rootLayout.setBackgroundColor(randomColor)

        // Show feedback toast
        Toast.makeText(this, "Shake Detected!", Toast.LENGTH_SHORT).show()
    }
}