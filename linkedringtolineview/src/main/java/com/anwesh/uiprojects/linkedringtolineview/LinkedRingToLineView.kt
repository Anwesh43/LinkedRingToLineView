package com.anwesh.uiprojects.linkedringtolineview

/**
 * Created by anweshmishra on 13/08/18.
 */

import android.app.Activity
import android.view.View
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.view.MotionEvent

val nodes : Int = 5

fun Canvas.drawLRLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    val gap : Float = w / nodes
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = Color.parseColor("#4CAF50")
    save()
    translate(gap/2 + (i * gap) * sc1, gap / 2 + (h / 2 - gap / 2) * sc1)
    val path : Path = Path()
    for (j in 0..360) {
        val x : Float = (gap / 2) * Math.cos(j * Math.PI/180).toFloat()
        val y : Float = (gap / 2) * (1 -sc2) * Math.sin(j * Math.PI/180).toFloat()
        if (j == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    drawPath(path, paint)
    restore()
}

class LinkedRingToLineView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)
    var animationListener : AnimationListener? = null

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    fun addAnimationListener(onComplete : (Int) -> Unit, onReset : (Int) -> Unit) {
        animationListener = AnimationListener(onComplete, onReset)
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.1f * this.dir
            if (Math.abs(this.scale - this.prevScale) > 1) {
                this.scale = this.prevScale + this.dir
                this.dir = 0f
                this.prevScale = this.scale
                cb(this.prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (this.dir == 0f) {
                this.dir = 1 - 2 * this.prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {
        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LRLNode(var i : Int, val state : State = State()) {

        private var next : LRLNode? = null

        private var prev : LRLNode? = null

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = LRLNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawLRLNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LRLNode {
            var curr : LRLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedRingToLine(var i : Int) {

        private var root : LRLNode = LRLNode(0)
        private var curr : LRLNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : LinkedRingToLineView) {

        private val animator : Animator = Animator(view)
        private val lrtl : LinkedRingToLine = LinkedRingToLine(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            lrtl.draw(canvas, paint)
            animator.animate {
                lrtl.update {i, scl ->
                    animator.stop()
                    when (scl) {
                        0f -> view.animationListener?.onReset?.invoke(i)
                        1f -> view.animationListener?.onComplete?.invoke(i)
                    }
                }
            }
        }

        fun handleTap() {
            lrtl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LinkedRingToLineView {
            val view : LinkedRingToLineView = LinkedRingToLineView(activity)
            activity.setContentView(view)
            return view
        }
    }

    data class AnimationListener(var onComplete : (Int) -> Unit, var onReset : (Int) -> Unit)
}