package com.tabelos

import fi.iki.elonen.NanoWSD
import java.io.IOException
import java.io.InputStream
import java.net.URLConnection
import java.nio.charset.Charset


class WebServer(hostname: String, port: Int) : NanoWSD(hostname, port) {

    override fun openWebSocket(handshake: IHTTPSession): WebSocket {
        System.out.println("openWebSocket");
        return WebSocket(this, handshake)
    }

    override fun serveHttp(session: IHTTPSession): Response {
        System.out.println("serve");
        var uri: String = session.uri.removePrefix("/")
        if (uri.equals("")) {
            uri = "index.html"
        }
        return serveFile(uri)
    }

    fun serveFile(uri: String):Response {
        System.out.println("serveFile");
        var response:Response = NanoWSD.newFixedLengthResponse("<!doctype html><html><head><meta charset=\"utf-8\"><title>Tabelos</title></head><body>File not found: "+uri+"</body></html>\n")
        if (uri.contains(".")) {
            try {
                val webUri:String = "web/" + uri;
                val inputStream:InputStream = MainActivity.appContext.resources.assets.open(webUri);
                val fileExtension = webUri.substringAfterLast(".");
                val mimeType:String = when (fileExtension) {
                    "html" -> "text/html"
                    "css" -> "text/css"
                    "js" -> "application/javascript"
                    "ico" -> "image/x-icon"
                    else -> URLConnection.guessContentTypeFromStream(inputStream);
                }
                var content:String = inputStream.readBytes().toString(Charset.defaultCharset())
                response = NanoWSD.newFixedLengthResponse(Response.Status.OK, mimeType, content)
            } catch (ex:IOException) {
            }
        }
        return response
    }
}
