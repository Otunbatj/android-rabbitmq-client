package com.xpresspay.sockettest.service

import android.util.Log
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope


private const val HOST = "167.86.82.69"
private const val PORT = 5672
private const val USERNAME = "guest"
private const val PASSWORD = "guest"

class RabbitMQClient : MessageClient {
    var connection: Connection? = null

    override fun connect() {
        Log.d("RabbitMQClient", "Trying to connect")
        val factory = ConnectionFactory()
        factory.host = HOST
        factory.port = PORT
        factory.username = USERNAME
        factory.password = PASSWORD

        connection = factory.newConnection()
    }

    override fun disconnect() {
        connection?.let {
            it.close()
        }
    }

    fun createRabbitMQConnection(queueName: String?) {
        val factory = ConnectionFactory()
        factory.host = HOST
        factory.port = PORT
        factory.username = USERNAME
        factory.password = PASSWORD
        try {
            val connection = factory.newConnection()
            val channel: Channel = connection.createChannel()
            channel.queueDeclare(queueName, false, false, false, null)
            // Start consuming messages from the queue
            // channel.basicConsume(queueName, true, consumer);
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createRabbitMQListener(queueName: String?) {
        val factory = ConnectionFactory()
        factory.host = HOST
        factory.port = PORT
        factory.username = USERNAME
        factory.password = PASSWORD
        try {
            val connection = factory.newConnection()
            val channel = connection.createChannel()
            channel.basicConsume(queueName, true, object : DefaultConsumer(channel) {
                override fun handleDelivery(
                    consumerTag: String,
                    envelope: Envelope,
                    properties: AMQP.BasicProperties,
                    body: ByteArray,
                ) {
                    val message = String(body, charset("UTF-8"))
                    handleMessage(message) // Call the handleMessage method
                }
            })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun handleMessage(message: String) {
        // Process the received message
        Log.e("Received message: {}", message)
        // Additional processing logic can be added here
    }
}