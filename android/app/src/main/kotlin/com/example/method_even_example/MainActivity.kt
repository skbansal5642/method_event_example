package com.example.method_even_example

import android.annotation.SuppressLint
import io.flutter.embedding.android.FlutterActivity

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import java.util.*
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.os.Handler
import android.os.Looper
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import java.text.SimpleDateFormat
import io.flutter.plugin.common.EventChannel

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "platform_channel"
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "showToast" -> call.argument<String>("message")?.let { showCustomToast(it) };
                "getBatteryLevel" -> getBatteryLevel(result);
                else -> result.notImplemented();
            }

        }

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, "timer_event").setStreamHandler(
            TimerStream
        )

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, "count_event").setStreamHandler(
            CountHandler
        )
    }

    private fun showCustomToast(message: String) {
        println(message)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getBatteryLevel(result: MethodChannel.Result) {
        val batteryLevel: Int
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(
                null, IntentFilter(
                    Intent.ACTION_BATTERY_CHANGED
                )
            )
            batteryLevel =
                intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(
                    BatteryManager.EXTRA_SCALE,
                    -1
                )
        }

        if (batteryLevel != -1) {
            result.success(batteryLevel)
        } else {
            result.error("UNAVAILABLE", "Battery level not available.", null)
        }
    }

    object TimerStream : EventChannel.StreamHandler {
        // Handle event in main thread.
        private var handler = Handler(Looper.getMainLooper())

        // Declare our eventSink later it will be initialized
        private var eventSink: EventChannel.EventSink? = null

        override fun onListen(p0: Any?, sink: EventChannel.EventSink) {
            eventSink = sink
            // every second send the time
            val r: Runnable = object : Runnable {
                override fun run() {
                    handler.post {
                        val dateFormat = SimpleDateFormat("HH:mm:ss")
                        val time = dateFormat.format(Date())
                        eventSink?.success(time)
                    }
                    handler.postDelayed(this, 1000)
                }
            }
            handler.postDelayed(r, 1000)
        }

        override fun onCancel(p0: Any) {
            eventSink = null
        }
    }

    object CountHandler : EventChannel.StreamHandler {
        // Handle event in main thread.
        private var handler = Handler(Looper.getMainLooper())

        // Declare our eventSink later it will be initialized
        private var eventSink: EventChannel.EventSink? = null

        @SuppressLint("SimpleDateFormat")
        override fun onListen(p0: Any?, sink: EventChannel.EventSink) {
            eventSink = sink
            var counter = 0
            // every second send the time
            val r: Runnable = object : Runnable {
                override fun run() {
                    handler.post {
                        counter = counter + 1
                        eventSink?.success(counter)
                    }
                    handler.postDelayed(this, 1000)
                }
            }
            handler.postDelayed(r, 1000)
        }

        override fun onCancel(p0: Any?) {
            eventSink = null
        }
    }
}
