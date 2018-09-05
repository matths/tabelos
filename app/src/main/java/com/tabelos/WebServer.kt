package com.tabelos

import fi.iki.elonen.NanoHTTPD

class WebServer(hostname:String, port: Int) : NanoHTTPD(hostname, port) {

    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        var msg = "<html><body><h1>Hello server</h1>\n"
        val parms = session.parms
        if (parms["username"] == null) {
            msg += "<form action='?' method='get'>\n"
            msg += "<p>Your name: <input type='text' name='username'></p>\n"
            msg += "</form>\n"
        } else {
            msg += "<p>Hello, " + parms["username"] + "!</p>"
        }
        return NanoHTTPD.newFixedLengthResponse("$msg</body></html>\n")
    }
}
