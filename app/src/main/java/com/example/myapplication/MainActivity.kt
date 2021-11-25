package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import java.lang.Math.toDegrees
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

const val TAG = "StepCounter"
const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
const val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 2


class MainActivity : AppCompatActivity() {

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    private lateinit var spinner_fps: Spinner
    private lateinit var start: Button
    private lateinit var steps: TextView
    private lateinit var gyroscope: TextView
    private lateinit var accelerometer: TextView
    private lateinit var compass: TextView

    private var manager: SensorManager? = null

    private var floatGravity = FloatArray(3)
    private var floatGeoMagnetic = FloatArray(3)
    private var floatOrientation = FloatArray(3)
    private var floatRotationMatrix = FloatArray(9)

    var countSteps:String = "Steps: 0 "
    var change:Boolean = false
    var fps = arrayOf("30", "40", "50", "60", "70")
    var selectedFps = 60
    var stepsCount = 0

    val datalistener = OnDataPointListener { dataPoint ->
        for (field in dataPoint.dataType.fields)
        {
            val value = dataPoint.getValue(field)
            Log.i(TAG, "Detected DataPoint field: ${field.name}")
            Log.i(TAG, "Detected DataPoint value: $value")
            stepsCount = stepsCount+value.asInt()
            countSteps = stepsCount.toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION
        )

        start = findViewById(R.id.btn_start_stop)
        spinner_fps = findViewById(R.id.spinner_fps)
        steps = findViewById(R.id.tv_steps)
        gyroscope = findViewById(R.id.tv_gyroscope)
        accelerometer = findViewById(R.id.tv_accelerometer)
        compass = findViewById(R.id.tv_compass)

        manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val listener: SensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent?) {

                if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
                    gyroscope.text = "Gyroscope: x: ${event?.values[0]}"
                    gyroscope.append(" y: ${event?.values[1]}")
                    gyroscope.append(" z: ${event?.values[2]}")
                }

                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    accelerometer.text = "Accelerometer: x: ${event?.values[0]}"
                    accelerometer.append(" y: ${event?.values[1]}")
                    accelerometer.append(" z: ${event?.values[2]}")
                    floatGravity = event.values
                }

                if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    floatGeoMagnetic = event.values
                    SensorManager.getRotationMatrix(
                        floatRotationMatrix,
                        null,
                        floatGravity,
                        floatGeoMagnetic
                    )

                    SensorManager.getOrientation(floatRotationMatrix, floatOrientation)

                    var radians =
                        ((toDegrees(floatOrientation[0].toDouble()) + 360).toFloat() % 360)

                    compass.text =
                        when (radians) {
                            in 0.0..22.0 -> "Compass: North $radians "
                            in 22.0..67.0 -> "Compass: North East $radians"
                            in 67.0..112.0 -> "Compass: East $radians"
                            in 112.0..157.0 -> "Compass: South East $radians"
                            in 157.0..202.0 -> "Compass: South $radians"
                            in 202.0..247.0 -> "Compass: South West $radians"
                            in 247.0..292.0 -> "Compass: West $radians"
                            in 292.0..337.0 -> "Compass: North West $radians"
                            in 337.0..360.0 -> "Compass: North $radians"
                            else -> "Compass: Compass nor active"
                        }
                }
            }
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fps)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_fps.adapter = adapter
        spinner_fps.setSelection(3)

        selectedFps = when (spinner_fps.selectedItem) {
            "30" -> 33
            "40" -> 25
            "50" -> 20
            "60" -> 16
            "70" -> 14
            else -> 0
        }

        Fitness.getSensorsClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .findDataSources(
                DataSourcesRequest.Builder()
                    .setDataTypes(DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .setDataSourceTypes(DataSource.TYPE_RAW)
                    .build()
            )
            .addOnSuccessListener { dataSources ->
                dataSources.forEach {
                    Log.i(TAG, "Data source found: ${it.streamIdentifier}")
                    Log.i(TAG, "Data Source type: ${it.dataType.name}")

                    if (it.dataType == DataType.AGGREGATE_STEP_COUNT_DELTA) {
                        Log.i(TAG, "Data source for STEP_COUNT_DELTA found!")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Find data sources request failed", e)
            }


        start.setOnClickListener(View.OnClickListener {
            change = when (change) {
                false -> true
                true -> false
            }

            when (change) {
                true -> start.text = "Stop"
                false -> start.text = "Start"
            }

            when (change) {
                true -> steps.text = countSteps
                false -> steps.text = countSteps
            }

            when (change) {
                true -> {
                    Fitness.getSensorsClient(
                        this,
                        GoogleSignIn.getAccountForExtension(this, fitnessOptions)
                    )
                        .add(
                            SensorRequest.Builder()
                                .setDataType(DataType.AGGREGATE_STEP_COUNT_DELTA) // Can't be omitted.
                                .setSamplingRate(selectedFps.toLong(),TimeUnit.MILLISECONDS )
                                .build(),
                            datalistener
                        )
                        .addOnSuccessListener {
                            Log.i(TAG, "Listener registered!")
                        }
                        .addOnFailureListener {
                            Log.e(TAG, "Listener not registered.")
                        }
                }
                false -> {
                    Fitness.getSensorsClient(
                        this,
                        GoogleSignIn.getAccountForExtension(this, fitnessOptions)
                    )
                        .remove(datalistener)
                        .addOnSuccessListener {
                            Log.i(TAG, "Listener was removed!")
                        }
                        .addOnFailureListener {
                            Log.i(TAG, "Listener was not removed.")
                        }
                }
            }
            when (change) {
                true -> {
                    val sensor_gyroscope = manager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                    val sensor_accelerometer = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    val sensor_magnetic = manager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

                    val check_gyroscope = manager?.registerListener(
                        listener,
                        sensor_gyroscope,
                        SensorManager.SENSOR_DELAY_UI
                    )
                    val check_accelerometer = manager?.registerListener(
                        listener,
                        sensor_accelerometer,
                        SensorManager.SENSOR_DELAY_UI
                    )
                    val check_magnetic = manager?.registerListener(
                        listener,
                        sensor_magnetic,
                        SensorManager.SENSOR_DELAY_UI
                    )

                    if (check_gyroscope == false) {
                        gyroscope.text = "Gyroscope don't working"
                    }
                    if (check_accelerometer == false) {
                        accelerometer.text = "Accelerometer don't working"
                    }
                    if (check_magnetic == false) {
                        compass.text = "Compass don't working"
                    }
                }
                false -> {
                    manager?.unregisterListener(listener)
                }
            }
        })

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.d(TAG, " GoogleFitRequestPermissions")
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                account,
                fitnessOptions

            )

        } else {
            Log.d(TAG, " GoogleFit ")

            accessGoogleFit()
        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "requestCode = " + requestCode + ", resultCode = " + resultCode);

        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {

                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> { accessGoogleFit()

                    Log.i(TAG, "Request Success") }
            }
            else -> {
                Log.i(TAG, "No success")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun accessGoogleFit() {

        val end = LocalDateTime.now()
        val start = end.minusYears(1)
        val endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond()
        val startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener({ response ->

                Log.i(TAG, "OnSuccess()")
            })
            .addOnFailureListener({ e -> Log.d(TAG, "OnFailure()", e) })

    }
}