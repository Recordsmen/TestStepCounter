package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.view.View
import android.widget.Spinner
import android.widget.ArrayAdapter





class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var change = false
        var start = findViewById<Button>(R.id.bn_startStop)
        start.setOnClickListener(View.OnClickListener {
            change = when (change) {
                false -> true
                true -> false
            }
            when (change){
                true -> start.text = "Stop"
                false -> start.text = "Start"
            }
        })
        val spinnerFPS = findViewById<Spinner>(R.id.spinner_fps)
        val adapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            this, R.array.dropDown,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFPS.adapter = adapter
        spinnerFPS.setSelection(3)
    }
}