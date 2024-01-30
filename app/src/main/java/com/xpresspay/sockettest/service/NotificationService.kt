package com.xpresspay.sockettest.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.DeliverCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeoutException

private const val QUEUE_NAME = "1142016190000280"

class NotificationService : Service() {
    private lateinit var rabbitMQClient: RabbitMQClient
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    override fun onCreate() {
        super.onCreate()
        rabbitMQClient = RabbitMQClient()
        serviceScope.launch {
            try {
                rabbitMQClient.connect()
                startConsuming()
            } catch (e: IOException) {
                println("An error occurred while attempting to connect :: ${e.message}")
            } catch (e: TimeoutException) {
                println("Connection Timed Out :: ${e.message}")
            }
        }
    }

    private fun startConsuming() {
        serviceScope.launch {
            try {
                val channel = rabbitMQClient.connection?.createChannel()
                channel?.queueDeclare(QUEUE_NAME, false, false, true, null)
                val deliverCallback = DeliverCallback { _, delivery ->
                    val message = String(delivery.body, Charsets.UTF_8)
                    println("RabbitMQ :: Received message: $message")

                    Intent("DisplayNotificationAction").apply {
                        putExtra("message", message)
                    }.also { localIntent ->
                        sendBroadcast(localIntent)
                    }
                }

                val cancelCallback = CancelCallback {
                    println("Cancel Callback Triggered :: $it")
                }
                channel?.basicConsume(QUEUE_NAME, true, deliverCallback, cancelCallback)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}