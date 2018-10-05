package com.tabelos

import android.app.AlertDialog
import android.net.http.SslCertificate
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class WebClient : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        view?.loadUrl(url)
        return true
    }

    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
    }

    // credits to @Heath Borders at http://stackoverflow.com/questions/20228800/how-do-i-validate-an-android-net-http-sslcertificate-with-an-x509trustmanager
    fun getX509Certificate(sslCertificate: SslCertificate): Certificate? {
        val bundle = SslCertificate.saveState(sslCertificate)
        val bytes = bundle.getByteArray("x509-certificate")
        return if (bytes == null) {
            null
        } else {
            try {
                val certFactory = CertificateFactory.getInstance("X.509")
                certFactory.generateCertificate(ByteArrayInputStream(bytes))
            } catch (e: CertificateException) {
                null
            }

        }
    }

    fun isKnownSelfSignedCert(certFromWebServer: SslCertificate): Boolean {

        val keystoreStream = NanoHTTPD::class.java.getResourceAsStream(Central.KEYSTORE_PATH)
        val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
        keystore.load(keystoreStream, Central.KEYSTORE_PASSWORD.toCharArray())
        val certFromKeyStore: Certificate = keystore.getCertificate("tabelos")

        val certFromKeyStoreAsX509: X509Certificate = certFromKeyStore as X509Certificate
        val certFromWebServerAsX509: Certificate? = this.getX509Certificate(certFromWebServer)

        try {
            certFromKeyStoreAsX509.verify(certFromWebServerAsX509?.publicKey)
        } catch (ex: Exception) {
            when (ex) {
                is CertificateException, is NoSuchAlgorithmException, is InvalidKeyException, is NoSuchProviderException, is SignatureException -> return false
                else -> throw ex
            }
        }
        return true
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        if (SslError.SSL_UNTRUSTED == error.primaryError && this.isKnownSelfSignedCert(error.certificate)) {
            handler.proceed() // Ignore SSL certificate errors
        } else {
            var message = "SSL Certificate error."
            when (error.primaryError) {
                SslError.SSL_UNTRUSTED -> message = "The certificate authority is not trusted."
                SslError.SSL_EXPIRED -> message = "The certificate has expired."
                SslError.SSL_IDMISMATCH -> message = "The certificate Hostname mismatch."
                SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid."
            }
            message += " Do you want to continue anyway?"

            val builder = AlertDialog.Builder(view.context)
            builder.setTitle("SSL Certificate Error")
            builder.setMessage(message)
            builder.setPositiveButton("continue") { dialog, which -> handler.proceed() }
            builder.setNegativeButton("cancel") { dialog, which -> handler.cancel() }
            val dialog = builder.create()
            dialog.show()
        }
    }
}
