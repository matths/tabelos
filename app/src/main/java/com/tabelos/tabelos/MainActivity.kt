package com.tabelos.tabelos

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.Window
import org.jetbrains.anko.*
import spark.kotlin.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        MainActivityUi().setContentView(this)

        runServer()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun runServer() {
        val http = ignite()

        http.port(8080)
        http.ipAddress("0.0.0.0")

        val maxThreads = 8
        val minThreads = 2
        val timeOutMillis = 30000
        http.threadPool(maxThreads, minThreads, timeOutMillis)

        http.get("/") {
            "Hello Spark Kotlin!"
        }
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}

class MainActivityUi : AnkoComponent<MainActivity> {

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {

        relativeLayout {
            webView {
                loadUrl("file:///android_asset/index.html")
            }
        }

    }

}