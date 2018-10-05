package com.tabelos

import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode
import java.io.IOException
import java.util.*
import kotlin.concurrent.fixedRateTimer

class TabletList {
    internal var list: Vector<Tablet>

    init {
        list = Vector<Tablet>()
        val timer = fixedRateTimer(name = "ping-timer", initialDelay = 4000, period = 4000) {
            println("ping timer")
            pingAll()
        }

    }

    fun addTablet(tablet: Tablet) {
        list.add(tablet)
    }

    fun removeTablet(tablet: Tablet) {
        list.remove(tablet)
    }

    fun tabletCount(): Int {
        return list.size
    }

    fun pingAll() {
        for (i in 0 until list.size) {
            val tablet = list.get(i)
            val ws = tablet.webSocket
            if (ws != null) {
                try {
                    val charset = Charsets.UTF_8
                    val byteArray = "ping".toByteArray(charset)
                    ws.ping(byteArray)
                } catch (e: IOException) {
                    println("ping error.....")
                    try {
                        ws.close(CloseCode.InvalidFramePayloadData, "reqrement", true)
                    } catch (e1: IOException) {
                        removeTablet(tablet)
                    }
                }
            }
        }
    }

    fun sendToAll(str: String) {
        for (i in 0 until list.size) {
            val tablet = list.get(i)
            val ws = tablet.webSocket
            if (ws != null) {
                try {
                    ws.send(str)
                } catch (e: IOException) {
                    println("sending error.....")
                    try {
                        ws.close(CloseCode.InvalidFramePayloadData, "reqrement", true)
                    } catch (e1: IOException) {
                        removeTablet(tablet)
                    }
                }
            }
        }
    }

    fun disconectAll() {
        for (i in 0 until list.size) {
            val tablet = list.get(i)
            val ws = tablet.webSocket
            if (ws != null) {
                try {
                    ws.close(CloseCode.InvalidFramePayloadData, "reqrement", false)
                } catch (e: IOException) {
                    removeTablet(tablet)
                }

            }
        }
    }
}