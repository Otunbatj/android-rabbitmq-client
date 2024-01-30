package com.xpresspay.sockettest

import android.app.Application
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

class TestApp : Application() {
    private var socket: Socket? = null

    init {
        try {
            socket = IO.socket("https://172.22.54.40:9611/")
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    fun getSocket(): Socket? {
        return socket
    }
}