package com.anwesh.uiprojects.ringtolineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.anwesh.uiprojects.linkedringtolineview.LinkedRingToLineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view : LinkedRingToLineView = LinkedRingToLineView.create(this)
        fullScreen()
        view.addAnimationListener({createToast("animation ${it + 1} is complete")}, {createToast("animation ${it + 1} is reset")})
    }

    fun createToast(msg : String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}
