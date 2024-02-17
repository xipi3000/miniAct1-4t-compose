package com.example.miniact1_4rt_compose


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.miniact1_4rt_compose.ui.theme.MiniAct4rtcomposeTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var color = false
    private var maxLight: Float = 0F
    private var lastUpdate: Long = 0
    private var oldLux: Float = 0F
    val luxState = mutableStateOf("")
    val accelExistance = mutableStateOf("")
    val luxExistance = mutableStateOf("")
    val accelColor = mutableStateOf(Color.Red)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiniAct4rtcomposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }
    }

    @Composable
    fun Greeting() {

        registerSensors()
        Column() {
            Box(
                modifier = Modifier
                    .weight(2F)
                    .fillMaxWidth()
                    .background(accelColor.value)
            ) {

            }
            Box(
                modifier = Modifier
                    .weight(2F)
                    .fillMaxWidth()
            ) {
                Text(accelExistance.value)
            }
            Box(
                modifier = Modifier
                    .weight(2F)
                    .fillMaxWidth()
                    .background(Color.Yellow)
            ) {
                Column {
                    Text(luxExistance.value)
                    Text(luxState.value)
                }
            }

        }
    }


    private fun registerAccel() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
            )
            accelExistance.value = getString(R.string.accelerometer_exists)
        } else {
            accelExistance.value = getString(R.string.no_accelerometer)
        }

    }


    private fun registerSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        registerAccel()
        registerLight()
    }

    private fun registerLight() {
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lastUpdate = System.currentTimeMillis()
        if (lightSensor != null) {
            maxLight = lightSensor.maximumRange

            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_FASTEST
            )
            luxExistance.value = getString(R.string.has_light) + maxLight

        } else {
            luxExistance.value = getString(R.string.no_light)
        }


    }


    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event)
        } else {
            getLight(event)
        }
    }


    private fun getLight(event: SensorEvent) {
        val values = event.values
        // Light
        val lux = values[0]
        val lowLight = maxLight / 3
        val highLight = maxLight * 2 / 3

        val actualTime = System.currentTimeMillis()

        if (Math.abs(lux - oldLux) >= 200) {
            oldLux = lux;
            if (actualTime - lastUpdate < 1000) {
               return
            }
            val displayText = getString(R.string.light_value)

            if (lux < lowLight) {
                luxState.value = displayText + getString(R.string.low_light)
            } else if (lux > highLight) {
                luxState.value = displayText + getString(R.string.high_light)
            } else {
                luxState.value = displayText + getString(R.string.medium_light)
            }
        }
    }

    private fun getAccelerometer(event: SensorEvent) {
        val values = event.values
        // Movement
        val x = values[0]
        val y = values[1]
        val z = values[2]
        val accelerationSquareRoot = (x * x + y * y + z * z
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH))
        val actualTime = System.currentTimeMillis()
        if (accelerationSquareRoot >= 200) {
            if (actualTime - lastUpdate < 1000) {
                return
            }
            lastUpdate = actualTime
            Toast.makeText(this, R.string.shuffed, Toast.LENGTH_SHORT).show()
            if (color) {
                accelColor.value = Color.Green
            } else {
                accelColor.value = Color.Red
            }
            color = !color
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onResume() {
        // unregister listener
        super.onResume()
        registerSensors()

    }


    override fun onPause() {
        // unregister listener
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MiniAct4rtcomposeTheme {
            Greeting()
        }
    }

}

