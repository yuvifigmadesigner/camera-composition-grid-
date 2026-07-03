package com.example.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var gridOverlayView: GridOverlayView
    private lateinit var controlPanelView: View

    private var isLocked = false
    private lateinit var gridLayoutParams: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setupGridOverlay()
        setupControlPanel()
    }

    private fun createNotification(): Notification {
        val channelId = "overlay_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Grid Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Grid Overlay Active")
            .setContentText("Tap control panel to customize.")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
    }

    private fun setupGridOverlay() {
        gridOverlayView = GridOverlayView(this)
        
        gridLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        gridLayoutParams.gravity = Gravity.TOP or Gravity.START
        windowManager.addView(gridOverlayView, gridLayoutParams)
    }

    private fun setupControlPanel() {
        // Create a simple control panel view programmatically or inflate
        controlPanelView = LayoutInflater.from(this).inflate(R.layout.control_panel, null)
        
        val panelLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        panelLayoutParams.gravity = Gravity.BOTTOM or Gravity.END
        panelLayoutParams.x = 20
        panelLayoutParams.y = 20

        windowManager.addView(controlPanelView, panelLayoutParams)

        // Implement dragging for control panel
        controlPanelView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = panelLayoutParams.x
                        initialY = panelLayoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        panelLayoutParams.x = initialX - (event.rawX - initialTouchX).toInt()
                        panelLayoutParams.y = initialY - (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(controlPanelView, panelLayoutParams)
                        return true
                    }
                }
                return false
            }
        })

        // Setup UI logic
        val btnLock = controlPanelView.findViewById<Button>(R.id.btn_lock)
        btnLock.setOnClickListener {
            isLocked = !isLocked
            btnLock.text = if (isLocked) "Unlock" else "Lock"
            toggleGridLock()
        }

        val btnClose = controlPanelView.findViewById<Button>(R.id.btn_close)
        btnClose.setOnClickListener {
            stopSelf()
        }

        val opacitySlider = controlPanelView.findViewById<SeekBar>(R.id.slider_opacity)
        opacitySlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                gridOverlayView.setLineOpacity(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val thicknessSlider = controlPanelView.findViewById<SeekBar>(R.id.slider_thickness)
        thicknessSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                gridOverlayView.setLineThickness(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val spinnerMode = controlPanelView.findViewById<Spinner>(R.id.spinner_mode)
        val modes = arrayOf("Rule of Thirds", "Golden Spiral", "Diagonal", "V-Shape", "Pyramid", "S-Curve", "L-Shape", "Double Diagonal", "Radiating", "Tunnel", "Golden Triangle", "Golden Section", "Circular", "Cross", "Balance", "C-Shape", "Unbalanced", "Custom Frame Ratio")
        spinnerMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modes)
        spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                gridOverlayView.setMode(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Setup colors
        controlPanelView.findViewById<Button>(R.id.btn_color_white).setOnClickListener { gridOverlayView.setLineColor(Color.WHITE) }
        controlPanelView.findViewById<Button>(R.id.btn_color_black).setOnClickListener { gridOverlayView.setLineColor(Color.BLACK) }
        controlPanelView.findViewById<Button>(R.id.btn_color_neon).setOnClickListener { gridOverlayView.setLineColor(Color.parseColor("#39FF14")) }
        controlPanelView.findViewById<Button>(R.id.btn_color_yellow).setOnClickListener { gridOverlayView.setLineColor(Color.YELLOW) }
    }

    private fun toggleGridLock() {
        if (isLocked) {
            gridLayoutParams.flags = gridLayoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            gridOverlayView.setLocked(true)
        } else {
            gridLayoutParams.flags = gridLayoutParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            gridOverlayView.setLocked(false)
        }
        windowManager.updateViewLayout(gridOverlayView, gridLayoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::gridOverlayView.isInitialized) windowManager.removeView(gridOverlayView)
        if (::controlPanelView.isInitialized) windowManager.removeView(controlPanelView)
    }
}
