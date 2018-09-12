package com.tabelos

import android.view.ViewGroup
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.webView

class MainActivityUi : AnkoComponent<MainActivity> {

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {

        relativeLayout {
            webView {
                setInitialScale(1)

                settings.setSupportMultipleWindows(false)
                settings.setSupportZoom(false)
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.javaScriptEnabled = true

                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

                setWebViewClient(
                        WebClient()
                )

                loadUrl("https://" + Constants.HOSTNAME + ":"+ Constants.PORT+"/")
            }
        }

    }

}
