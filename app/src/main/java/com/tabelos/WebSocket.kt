package com.tabelos

import fi.iki.elonen.NanoWSD.WebSocketFrame
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoWSD
import java.io.IOException

class WebSocket(val server: WebServer, handshakeRequest: IHTTPSession) : NanoWSD.WebSocket(handshakeRequest) {

    override protected fun onOpen() {
        System.out.println("onOpen " + this.handshakeRequest.remoteIpAddress)

        val sessionId: String? = this.handshakeRequest.cookies.read("session")
        val peerExists = (sessionId != null) && com.tabelos.State.peers.containsKey(sessionId)
        if (!peerExists) {
            this.close(WebSocketFrame.CloseCode.GoingAway, "session has gone", false)
        } else {
            val peer = com.tabelos.State.peers[sessionId]
            if (peer != null) {
                peer.webSocket = this
            }
        }
    }

    override protected fun onClose(code: CloseCode?, reason: String?, initiatedByRemote: Boolean) {
        System.out.println("onClose");
        val sessionId:String = this.handshakeRequest.cookies.read("session")
        com.tabelos.State.peers.remove(sessionId)
    }

    override  protected fun onMessage(message: WebSocketFrame) {
        System.out.println("onMessage" + message.textPayload);
        try {
            message.setUnmasked()
            sendFrame(message)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override protected fun onPong(pong: WebSocketFrame) {
        val charset = Charsets.UTF_8
        val msg = pong.binaryPayload.toString(charset)
        System.out.println("onPong: " + msg);
    }

    override protected fun onException(exception: IOException) {
        System.out.println("onException" + exception.message);
        this.close(WebSocketFrame.CloseCode.AbnormalClosure, exception.message, true)
    }

    override protected fun debugFrameReceived(frame: WebSocketFrame) {
        System.out.println("debugFrameReceived");
    }

    override protected fun debugFrameSent(frame: WebSocketFrame) {
        System.out.println("debugFrameSent");
    }
}