package com.tabelos

object State {

    const val HOSTNAME = "127.0.0.1"
    const val PORT = 8443
    const val KEYSTORE_PATH = "/res/raw/tabelos.jks"
    const val KEYSTORE_PASSWORD = "password"

    var ipAddress:Array<Int>? = null
    val peers:Peers = Peers();
}