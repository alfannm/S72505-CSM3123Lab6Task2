package com.example.shakedetectorlab

// Android framework imports for sensors, UI, and lifecycle
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

// Math and utility imports
import kotlin.math.sqrt
import kotlin.random.Random

// MainActivity serves as the UI controller and sensor listener
class MainActivity : AppCompatActivity(), SensorEventListener {

    // Constants that define shake sensitivity and cooldown timing
    // SHAKE_THRESHOLD: minimum g-force required to consider a movement a "shake"
    // SHAKE_COOLDOWN: minimum time gap between shake detections (in milliseconds)
    private val SHAKE_THRESHOLD = 2.7f
    private val SHAKE_COOLDOWN = 800L

    // Variables to track shake timing and count
    // lastShakeTime prevents multiple detections from a single shake motion
    // shakeCount keeps track of total valid shakes detected
    private var lastShakeTime: Long = 0
    private var shakeCount = 0

    // Sensor-related components
    // sensorManager manages access to device sensors
    // accelerometer represents the device's accelerometer sensor
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // UI components
    // rootLayout allows background color changes on shake
    // tvShakeCount displays the number of shakes detected
    private lateinit var rootLayout: RelativeLayout
    private lateinit var tvShakeCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the UI layout defined in activity_main.xml
        setContentView(R.layout.activity_main)

        // 1. Link UI components to their XML counterparts
        rootLayout = findViewById(R.id.shakeRootLayout)
        tvShakeCount = findViewById(R.id.shakeCounterLabel)

        // 2. Initialize Sensor Manager from system services
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Retrieve the default accelerometer sensor from the device
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Check if the device actually supports an accelerometer
        // If not, notify the user with an error message
        if (accelerometer == null) {
            Toast.makeText(this, "Error: No Accelerometer found!", Toast.LENGTH_LONG).show()
        }
    }

    // Lifecycle method called when the app becomes visible
    // Used to start listening to sensor updates
    override fun onResume() {
        super.onResume()

        // Register sensor listener only if accelerometer exists
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    // Lifecycle method called when the app is paused or sent to background
    // Used to stop sensor updates to conserve battery
    override fun onPause() {
        super.onPause()

        // Unregister all sensor listeners
        sensorManager.unregisterListener(this)
    }

    // Called automatically whenever a sensor value changes
    // Accelerometer events can fire many times per second
    override fun onSensorChanged(event: SensorEvent?) {

        // Safety check: exit if event is null
        if (event == null) return

        // Ensure the event is from the accelerometer sensor
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {

            // 1. Extract raw acceleration values along X, Y, and Z axes
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // 2. Normalize acceleration values by Earth's gravity (9.81 m/s²)
            // This converts raw acceleration into g-force units
            val gX = x / 9.81f
            val gY = y / 9.81f
            val gZ = z / 9.81f

            // Calculate overall g-force magnitude using vector formula
            // gForce = sqrt(gX² + gY² + gZ²)
            val gForce = sqrt(
                (gX * gX + gY * gY + gZ * gZ).toDouble()
            ).toFloat()

            // 3. Check if calculated g-force exceeds shake threshold
            if (gForce > SHAKE_THRESHOLD) {

                // Get current system time
                val currentTime = System.currentTimeMillis()

                // Ensure enough time has passed since the last detected shake
                // This prevents multiple detections from a single shake
                if (currentTime - lastShakeTime > SHAKE_COOLDOWN) {
                    lastShakeTime = currentTime
                    handleShakeEvent()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Accuracy changes are not required for this lab implementation
    }

    // Handles all actions that occur when a valid shake is detected
    private fun handleShakeEvent() {

        // Increment the shake counter
        shakeCount++

        // Update the shake count display on screen
        tvShakeCount.text = "Shake Count: $shakeCount"

        // Generate a random RGB color
        val randomColor = Color.rgb(
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )

        // Apply the random color to the background layout
        rootLayout.setBackgroundColor(randomColor)

        // Display a short feedback message to the user
        Toast.makeText(this, "Shake Detected!", Toast.LENGTH_SHORT).show()
    }
}
