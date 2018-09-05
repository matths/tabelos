package com.tabelos.tabelos

import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.webView

class MainActivityUi : AnkoComponent<MainActivity> {

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {

        relativeLayout {
            webView {
                setWebViewClient(
                        WebClient()
                )
                settings.javaScriptEnabled = true
                loadUrl("https://" + Constants.HOSTNAME + ":"+ Constants.PORT+"/")
            }
        }

    }

}
