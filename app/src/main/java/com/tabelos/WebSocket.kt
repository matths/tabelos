package com.tabelos

import fi.iki.elonen.NanoWSD.WebSocketFrame
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoWSD
import java.io.IOException


class WebSocket(val server: WebServer, handshakeRequest: IHTTPSession) : NanoWSD.WebSocket(handshakeRequest) {

    override protected fun onOpen() {
        System.out.println("onOpen" + this.handshakeRequest.remoteIpAddress);
    }

    override protected fun onClose(code: CloseCode?, reason: String?, initiatedByRemote: Boolean) {
        System.out.println("onClose");
    }

    override  protected fun onMessage(message: WebSocketFrame) {
        System.out.println("onMessage");
        try {
            message.setUnmasked()
            sendFrame(message)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override protected fun onPong(pong: WebSocketFrame) {
        System.out.println("onPong");
    }

    override protected fun onException(exception: IOException) {
        System.out.println("onException" + exception.message);
    }

    override protected fun debugFrameReceived(frame: WebSocketFrame) {
        System.out.println("debugFrameReceived");
    }

    override protected fun debugFrameSent(frame: WebSocketFrame) {
        System.out.println("debugFrameSent");
    }
}