package com.example.myapplication

import android.content.Context
import android.hardware.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.view.View
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    private lateinit var spinner_fps: Spinner
    private lateinit var start: Button
    private lateinit var steps: TextView
    private lateinit var gyroscope: TextView
    private lateinit var accelerometer: TextView
    private lateinit var compass: TextView

    private var manager : SensorManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start = findViewById(R.id.btn_start_stop)
        spinner_fps = findViewById(R.id.spinner_fps)
        steps = findViewById(R.id.tv_steps)
        gyroscope = findViewById(R.id.tv_gyroscope)
        accelerometer = findViewById(R.id.tv_accelerometer)
        compass = findViewById(R.id.tv_compass)

        manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val listener: SensorEventListener = object: SensorEventListener{
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent?){
                if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
                    gyroscope.text = "Gyroscope: x: ${event?.values[0]}"
                    gyroscope.append(" y: ${event?.values[1]}")
                    gyroscope.append(" z: ${event?.values[2]}")
                }
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    accelerometer.text = "Accelerometer: x: ${event?.values[0]}"
                    accelerometer.append(" y: ${event?.values[1]}")
                    accelerometer.append(" z: ${event?.values[2]}")
                }
                if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    compass.text = "Compass: x: ${event?.values[0]}"
                    compass.append(" y: ${event?.values[1]}")
                    compass.append(" z: ${event?.values[2]}")
                }
            }

        }

        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            this, R.array.dropDown,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_fps.adapter = adapter
        spinner_fps.setSelection(3)

        var change = false
        var x_gyroscope = 0
        var y_gyroscope = 0
        var z_gyroscope = 0

        start.setOnClickListener(View.OnClickListener {
            change = when (change) {
                false -> true
                true -> false
            }
            when (change){
                true -> start.text = "Stop"
                false -> start.text = "Start"
            }
            when (change) {
                true -> {
                    val sensor_gyroscope = manager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                    val sensor_accelerometer = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    val sensor_magnetic = manager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

                    val check_gyroscope = manager?.registerListener(listener, sensor_gyroscope, SensorManager.SENSOR_DELAY_UI)
                    val check_accelerometer = manager?.registerListener(listener, sensor_accelerometer, SensorManager.SENSOR_DELAY_UI)
                    val check_magnetic = manager?.registerListener(listener, sensor_magnetic, SensorManager.SENSOR_DELAY_UI)

                    if (check_gyroscope == false) {
                        gyroscope.text ="Fuck Niggas"
                    }
                    if (check_accelerometer == false) {
                        accelerometer.text ="Fuck Niggas"
                    }
                    if (check_magnetic == false) {
                        compass.text ="Fuck Niggas"
                    }
                }
                false -> {
                    manager?.unregisterListener(listener)
                }
            }
        })






    }
}