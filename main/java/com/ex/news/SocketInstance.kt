package com.ex.news

import com.github.nkzawa.engineio.client.transports.WebSocket
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import java.lang.RuntimeException
import java.net.URISyntaxException

class SocketInstance {
    private var iSocket: Socket

    init{
        try {
            val opts: IO.Options = IO.Options()
            opts.transports = arrayOf(WebSocket.NAME)


            iSocket = IO.socket(URL, opts)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }
    val SocketInstance: Socket
        get() = iSocket

    companion object {
        private const val URL = "https://vlog-io.herokuapp.com/"
    }
}