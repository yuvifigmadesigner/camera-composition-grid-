package com.example.overlay

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

class GridOverlayView(context: Context) : View(context) {

    private var boundingBox = RectF(100f, 100f, 800f, 1200f)
    private var isLocked = false
    private var currentMode = 0
    
    private val linePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    
    private val handlePaint = Paint().apply {
        color = Color.parseColor("#80FFFFFF")
        style = Paint.Style.FILL
    }
    
    private val handleRadius = 30f
    
    // Dragging state
    private var dragCorner = -1 // 0=TL, 1=TR, 2=BR, 3=BL, 4=Center
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialBox = RectF()

    fun setLocked(locked: Boolean) {
        isLocked = locked
        invalidate()
    }

    fun setMode(mode: Int) {
        currentMode = mode
        invalidate()
    }

    fun setLineOpacity(alpha: Int) {
        linePaint.alpha = alpha
        invalidate()
    }

    fun setLineThickness(thickness: Float) {
        linePaint.strokeWidth = thickness.coerceAtLeast(1f)
        invalidate()
    }

    fun setLineColor(color: Int) {
        val currentAlpha = linePaint.alpha
        linePaint.color = color
        linePaint.alpha = currentAlpha
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isLocked) return false

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragCorner = getHitCorner(x, y)
                if (dragCorner != -1) {
                    initialTouchX = x
                    initialTouchY = y
                    initialBox.set(boundingBox)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (dragCorner != -1) {
                    val dx = x - initialTouchX
                    val dy = y - initialTouchY

                    when (dragCorner) {
                        0 -> { // Top Left
                            boundingBox.left = (initialBox.left + dx).coerceAtMost(boundingBox.right - 100f)
                            boundingBox.top = (initialBox.top + dy).coerceAtMost(boundingBox.bottom - 100f)
                        }
                        1 -> { // Top Right
                            boundingBox.right = (initialBox.right + dx).coerceAtLeast(boundingBox.left + 100f)
                            boundingBox.top = (initialBox.top + dy).coerceAtMost(boundingBox.bottom - 100f)
                        }
                        2 -> { // Bottom Right
                            boundingBox.right = (initialBox.right + dx).coerceAtLeast(boundingBox.left + 100f)
                            boundingBox.bottom = (initialBox.bottom + dy).coerceAtLeast(boundingBox.top + 100f)
                        }
                        3 -> { // Bottom Left
                            boundingBox.left = (initialBox.left + dx).coerceAtMost(boundingBox.right - 100f)
                            boundingBox.bottom = (initialBox.bottom + dy).coerceAtLeast(boundingBox.top + 100f)
                        }
                        4 -> { // Center Move
                            boundingBox.offsetTo(initialBox.left + dx, initialBox.top + dy)
                        }
                    }
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dragCorner = -1
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getHitCorner(x: Float, y: Float): Int {
        val tolerance = 60f
        if (Math.hypot((x - boundingBox.left).toDouble(), (y - boundingBox.top).toDouble()) < tolerance) return 0
        if (Math.hypot((x - boundingBox.right).toDouble(), (y - boundingBox.top).toDouble()) < tolerance) return 1
        if (Math.hypot((x - boundingBox.right).toDouble(), (y - boundingBox.bottom).toDouble()) < tolerance) return 2
        if (Math.hypot((x - boundingBox.left).toDouble(), (y - boundingBox.bottom).toDouble()) < tolerance) return 3
        if (boundingBox.contains(x, y)) return 4
        return -1
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw bounding box
        canvas.drawRect(boundingBox, linePaint)
        
        // Draw handles if not locked
        if (!isLocked) {
            canvas.drawCircle(boundingBox.left, boundingBox.top, handleRadius, handlePaint)
            canvas.drawCircle(boundingBox.right, boundingBox.top, handleRadius, handlePaint)
            canvas.drawCircle(boundingBox.right, boundingBox.bottom, handleRadius, handlePaint)
            canvas.drawCircle(boundingBox.left, boundingBox.bottom, handleRadius, handlePaint)
        }

        // Clip to bounding box for inner drawing
        canvas.save()
        canvas.clipRect(boundingBox)
        
        drawMode(canvas)
        
        canvas.restore()
    }

    private fun drawMode(canvas: Canvas) {
        val L = boundingBox.left
        val T = boundingBox.top
        val R = boundingBox.right
        val B = boundingBox.bottom
        val W = boundingBox.width()
        val H = boundingBox.height()

        when (currentMode) {
            0 -> { // Rule of Thirds
                canvas.drawLine(L + W / 3f, T, L + W / 3f, B, linePaint)
                canvas.drawLine(L + 2f * W / 3f, T, L + 2f * W / 3f, B, linePaint)
                canvas.drawLine(L, T + H / 3f, R, T + H / 3f, linePaint)
                canvas.drawLine(L, T + 2f * H / 3f, R, T + 2f * H / 3f, linePaint)
            }
            1 -> { // Golden Spiral
                // Simplified golden spiral representation using arcs
                val phi = 1.618f
                var cx = L
                var cy = T
                var cw = W
                var ch = H
                var startAngle = 0f
                for (i in 0..4) {
                    val radius = if (cw > ch) ch else cw
                    val rectF = RectF(cx, cy, cx + radius * 2, cy + radius * 2)
                    canvas.drawArc(rectF, startAngle, 90f, false, linePaint)
                    if (cw > ch) {
                        cx += cw / phi
                        cw -= cw / phi
                    } else {
                        cy += ch / phi
                        ch -= ch / phi
                    }
                    startAngle += 90f
                }
            }
            2 -> { // Diagonal
                canvas.drawLine(L, T, R, B, linePaint)
            }
            3 -> { // V-Shape
                val midX = L + W / 2f
                canvas.drawLine(L, T, midX, B, linePaint)
                canvas.drawLine(R, T, midX, B, linePaint)
            }
            4 -> { // Pyramid
                val midX = L + W / 2f
                canvas.drawLine(L, B, midX, T, linePaint)
                canvas.drawLine(R, B, midX, T, linePaint)
                canvas.drawLine(L, B, R, B, linePaint)
            }
            5 -> { // S-Curve
                val path = Path()
                path.moveTo(R, T)
                path.cubicTo(L, T + H/4, R, T + 3*H/4, L, B)
                canvas.drawPath(path, linePaint)
            }
            6 -> { // L-Shape
                canvas.drawLine(L + W/4, T + H/4, L + W/4, B - H/4, linePaint)
                canvas.drawLine(L + W/4, B - H/4, R - W/4, B - H/4, linePaint)
            }
            7 -> { // Double Diagonal
                canvas.drawLine(L, T, R, B, linePaint)
                canvas.drawLine(R, T, L, B, linePaint)
            }
            8 -> { // Radiating
                val cx = L + W / 2f
                val cy = T + H / 2f
                canvas.drawLine(cx, cy, L, T, linePaint)
                canvas.drawLine(cx, cy, R, T, linePaint)
                canvas.drawLine(cx, cy, L, B, linePaint)
                canvas.drawLine(cx, cy, R, B, linePaint)
                canvas.drawLine(cx, cy, L, cy, linePaint)
                canvas.drawLine(cx, cy, R, cy, linePaint)
                canvas.drawLine(cx, cy, cx, T, linePaint)
                canvas.drawLine(cx, cy, cx, B, linePaint)
            }
            9 -> { // Tunnel
                for (i in 1..4) {
                    val insetW = W * 0.15f * i
                    val insetH = H * 0.15f * i
                    canvas.drawRect(L + insetW, T + insetH, R - insetW, B - insetH, linePaint)
                }
            }
            10 -> { // Golden Triangle
                canvas.drawLine(L, T, R, B, linePaint)
                // Perpendiculars
                val dx = R - L
                val dy = B - T
                val lengthSq = dx * dx + dy * dy
                val u = ((R - L) * dx + (T - T) * dy) / lengthSq // projecting TR to diagonal
                val ix1 = L + u * dx
                val iy1 = T + u * dy
                canvas.drawLine(R, T, ix1, iy1, linePaint)
                
                val v = ((L - L) * dx + (B - T) * dy) / lengthSq
                val ix2 = L + v * dx
                val iy2 = T + v * dy
                canvas.drawLine(L, B, ix2, iy2, linePaint)
            }
            11 -> { // Golden Section
                val phiW1 = W / 1.618f
                val phiW2 = W - phiW1
                val phiH1 = H / 1.618f
                val phiH2 = H - phiH1
                canvas.drawLine(L + phiW2, T, L + phiW2, B, linePaint)
                canvas.drawLine(L + phiW1, T, L + phiW1, B, linePaint)
                canvas.drawLine(L, T + phiH2, R, T + phiH2, linePaint)
                canvas.drawLine(L, T + phiH1, R, T + phiH1, linePaint)
            }
            12 -> { // Circular
                canvas.drawOval(RectF(L, T, R, B), linePaint)
            }
            13 -> { // Cross
                canvas.drawLine(L + W / 2f, T, L + W / 2f, B, linePaint)
                canvas.drawLine(L, T + H / 2f, R, T + H / 2f, linePaint)
            }
            14 -> { // Balance
                val bW = W * 0.2f
                val bH = H * 0.3f
                val padding = W * 0.15f
                canvas.drawRect(L + padding, T + padding, L + padding + bW, T + padding + bH, linePaint)
                canvas.drawRect(R - padding - bW, B - padding - bH, R - padding, B - padding, linePaint)
            }
            15 -> { // C-Shape
                val path = Path()
                path.moveTo(R - W/4, T + H/4)
                path.cubicTo(L, T, L, B, R - W/4, B - H/4)
                canvas.drawPath(path, linePaint)
            }
            16 -> { // Unbalanced
                canvas.drawRect(L + W*0.1f, T + H*0.1f, L + W*0.5f, T + H*0.4f, linePaint)
                canvas.drawRect(R - W*0.2f, B - H*0.2f, R - W*0.1f, B - H*0.1f, linePaint)
            }
            17 -> { // Custom Frame Ratio
                val padding = W * 0.1f
                canvas.drawRect(L + padding, T + padding * 2, R - padding, B - padding * 2, linePaint)
            }
        }
    }
}
