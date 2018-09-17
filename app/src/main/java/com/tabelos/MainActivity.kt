package com.tabelos

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import fi.iki.elonen.NanoHTTPD
import javax.net.ssl.SSLServerSocketFactory
import android.net.wifi.WifiManager
import android.webkit.WebView
import android.view.ViewGroup
import android.graphics.PixelFormat
import android.view.WindowManager
import android.view.Gravity
import android.webkit.WebChromeClient
import android.widget.RelativeLayout

class MainActivity : Activity() {

    companion object{
        lateinit var appContext:Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState)

        appContext = applicationContext

        System.out.println("IP address " + getOwnIp())
        runServer()
        runClient()

        createStatusBarBlocker()
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

    fun createStatusBarBlocker() {

        val statusBarBlocker = object:ViewGroup(applicationContext) {
            override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            }
            override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
                return true
            }
        }

        var statusBarHeight = getStatusBarHeight()
        val localLayoutParams: WindowManager.LayoutParams = getLayoutParamsForStatusBarBlockerWithSize(
                WindowManager.LayoutParams.MATCH_PARENT,
                statusBarHeight
        )

        val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(statusBarBlocker, localLayoutParams);

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    private fun getLayoutParamsForStatusBarBlockerWithSize(width:Int, height:Int): WindowManager.LayoutParams {
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
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        var statusBarHeight = 60
        if (resId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resId)
        }
        return statusBarHeight
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    public fun getOwnIp():String {
        val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var ipAddress = wifiManager.connectionInfo.ipAddress
        val ipString =
                (ipAddress and 0xFF).toString() + "." +
                (ipAddress shr 8 and 0xFF) + "." +
                (ipAddress shr 16 and 0xFF) + "." +
                (ipAddress shr 24 and 0xFF);
        return ipString;
    }

    private fun runServer() {

        // download Bouncy Castle Provider 1.46 for JDK 1.5 to 1.8 from https://mvnrepository.com/artifact/org.bouncycastle/bcprov-ext-jdk15on/1.46
        // wget http://central.maven.org/maven2/org/bouncycastle/bcprov-ext-jdk15on/1.46/bcprov-ext-jdk15on-1.46.jar

        // use keytool from e.g.legacy JDK 1.6 and create keystore with a self signed certificate
        // /Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Commands/keytool -genkey -keyalg rsa -alias tabelos -keystore tabelos.jks -storepass password -storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath ~/Desktop/bcprov-ext-jdk15on-1.46.jar

        // copy keystore file into resource path
        // cp tabelos.jks /Users/[UserName]/AndroidStudioProjects/Tabelos/app/src/main/res/raw/tabelos.jks

        // read keystore resource to see if file is exported into build
        // val content = javaClass.getResource("/res/raw/tabelos.jks").readText()
        // println(content)

        // inspired by https://stackoverflow.com/questions/36553190/check-in-the-onreceivedsslerror-method-of-a-webviewclient-if-a-certificate-is
        try {
            val webServer = WebServer(Constants.HOSTNAME, Constants.PORT)
            val sslServerSocketFactory: SSLServerSocketFactory = NanoHTTPD.makeSSLSocketFactory(Constants.KEYSTORE_PATH, Constants.KEYSTORE_PASSWORD.toCharArray())
            webServer.makeSecure(sslServerSocketFactory, null);
            webServer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun runClient() {

        var layoutParams:ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

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
        webView.loadUrl("https://" + Constants.HOSTNAME + ":"+ Constants.PORT+"/")

        var relativeLayout = RelativeLayout(this)
        relativeLayout.addView(webView)

        this.addContentView(relativeLayout, layoutParams)
    }
}
