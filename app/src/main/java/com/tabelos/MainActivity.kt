package com.tabelos

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import fi.iki.elonen.NanoHTTPD
import javax.net.ssl.SSLServerSocketFactory
import android.net.wifi.WifiManager
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONArray
import android.view.MotionEvent
import android.view.ViewGroup
import android.graphics.PixelFormat
import android.view.WindowManager
import android.view.Gravity
import android.widget.RelativeLayout

class MainActivity : Activity() {

    lateinit var statusBarBlocker:ViewGroup;

    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        goFullScreen()
        setRotationAnimation()

        super.onCreate(savedInstanceState)

        appContext = applicationContext

        createStatusBarBlocker()

        System.out.println("IP address " + getOwnIp())

        runServer()
        runClient()
    }

    override fun onPause() {
        super.onPause()
        val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, 0)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
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

    // disable back button
    override fun onBackPressed() {
        System.out.println("back button pressed")
    }

    // disable volume buttons
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        System.out.println("volume button pressed")
        val blockedKeys = listOf<Int>(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP)
        return if (blockedKeys.contains(event.getKeyCode())) {
            true
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    fun createStatusBarBlocker() {

        statusBarBlocker = object : ViewGroup(applicationContext) {
            override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            }

            override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
                return true
            }
        }

        val statusBarBlockerLayoutParams: WindowManager.LayoutParams = getLayoutParamsForStatusBarBlockerWithSize(
                WindowManager.LayoutParams.MATCH_PARENT,
                getStatusBarHeight()
        )
        getNavigationBarHeight();

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(statusBarBlocker, statusBarBlockerLayoutParams);

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }

    private fun setRotationAnimation() {
        window.attributes.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE
    }

    private fun goFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private fun getLayoutParamsForStatusBarBlockerWithSize(width: Int, height: Int): WindowManager.LayoutParams {
        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

        layoutParams.width = width
        layoutParams.height = height

        // this allows the view to be displayed over the status bar
        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        layoutParams.gravity = Gravity.TOP
        layoutParams.format = PixelFormat.TRANSLUCENT
        layoutParams.flags =
                // this is to keep button presses going to the background window
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        return layoutParams
    }

    private fun getStatusBarHeight(): Int {
        return getResourceDimension("status_bar_height", 25);
    }

    private fun getNavigationBarHeight(): Int {
        return getResourceDimension("navigation_bar_height", 48);
    }

    private fun getResourceDimension(identifier:String, default:Int = 0): Int {
        val resId = resources.getIdentifier(identifier, "dimen", "android")
        var dim = default
        if (resId > 0) {
            dim= resources.getDimensionPixelSize(resId)
        }
        System.out.println("DIMENSION OF "+identifier+" IS "+dim);
        return dim
    }

    private fun runServer() {
        try {
//            val webServer = WebServer(Central.HOSTNAME, Central.PORT)
            val webServer = WebServer("0.0.0.0", Central.PORT)
            val sslServerSocketFactory: SSLServerSocketFactory = NanoHTTPD.makeSSLSocketFactory(Central.KEYSTORE_PATH, Central.KEYSTORE_PASSWORD.toCharArray())
            webServer.makeSecure(sslServerSocketFactory, null);
            webServer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun runClient() {

        WebView.setWebContentsDebuggingEnabled(true)

        var layoutParams: ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        var webView = WebView(applicationContext)
        with(webView) {
            settings.setSupportMultipleWindows(false)
            settings.setSupportZoom(false)
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.javaScriptEnabled = true
        }
        webView.layoutParams = layoutParams
        webView.setInitialScale(1)
        webView.setWebViewClient(WebClient())
        webView.addJavascriptInterface(this, "JSInterface")
        webView.loadUrl("https://" + Central.HOSTNAME + ":" + Central.PORT + "/")
//        webView.loadUrl("file:///android_asset/setup.html");
//        webView.evaluateJavascript()

        var relativeLayout = RelativeLayout(this)
        relativeLayout.addView(webView)

        this.addContentView(relativeLayout, layoutParams)
    }

    @JavascriptInterface
    fun getOwnIp(): String {
        val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        val ipArray = arrayOf(
                ipAddress and 0xFF,
                ipAddress shr 8 and 0xFF,
                ipAddress shr 16 and 0xFF,
                ipAddress shr 24 and 0xFF
        )
        return JSONArray(ipArray).toString()
    }

}
