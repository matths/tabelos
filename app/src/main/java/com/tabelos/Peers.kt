package com.tabelos

import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer

class Peers() : HashMap<String, Peer>() {

    init {
        val timer = fixedRateTimer(name = "ping-timer", initialDelay = 4000, period = 4000) {
            println("ping timer")
            pingAll()
        }
    }

    fun newPeer():String {
        val hash = newHash()
        this[hash] = Peer()
        return hash
    }

    fun md5(str:String):String {
        val mdEnc = MessageDigest.getInstance("MD5")
        return BigInteger(1, mdEnc.digest(str.toByteArray())).toString(16);
    }

    fun newHash():String {
        var hash: String? = null;
        while (hash == null || this[hash] != null) {
            hash = md5("peer_" + System.currentTimeMillis()) // plus some random?
        }
        return hash
    }

    fun pingAll() {
        this.forEach {
            val id = it.key
            val peer = it.value
            val ws = peer.webSocket
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
                        this.remove(id)
                    }
                }
            }
        }
    }

    fun sendToAll(str: String) {
        this.forEach {
            val id = it.key
            val peer = it.value
            val ws = peer.webSocket
            if (ws != null) {
                try {
                    ws.send(str)
                } catch (e: IOException) {
                    println("sending error.....")
                    try {
                        ws.close(CloseCode.InvalidFramePayloadData, "reqrement", true)
                    } catch (e1: IOException) {
                        remove(id)
                    }
                }
            }
        }
    }

    fun disconectAll() {
        this.forEach {
            val id = it.key
            val peer = it.value
            val ws = peer.webSocket
            if (ws != null) {
                try {
                    ws.close(CloseCode.InvalidFramePayloadData, "reqrement", false)
                } catch (e: IOException) {
                    remove(id)
                }
            }
        }
    }
}