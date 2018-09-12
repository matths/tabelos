package com.tabelos

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import fi.iki.elonen.NanoHTTPD
import org.jetbrains.anko.*
import javax.net.ssl.SSLServerSocketFactory

class MainActivity : Activity() {

    companion object{
        lateinit var appContext:Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        runServer()
        runClient()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
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
        MainActivityUi().setContentView(this)
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
