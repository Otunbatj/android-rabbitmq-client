package com.xpresspay.sockettest.service

import java.io.IOException
import java.util.concurrent.TimeoutException
import kotlin.jvm.Throws

interface MessageClient {
    @Throws(IOException::class, TimeoutException::class)
    fun connect()

    @Throws(IOException::class)
    fun disconnect()
}