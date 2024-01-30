package com.xpresspay.sockettest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.xpresspay.sockettest.model.TransactionNotification
import com.xpresspay.sockettest.service.NotificationService
import io.reactivex.Completable
import io.reactivex.CompletableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val RECEIVE_PATH = "/topic/notification"
const val SEND_PATH = "/topic/ack"

class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView
    lateinit var buttonConnect: Button
    lateinit var buttonSend: Button
    lateinit var inputIpAddress: EditText
    lateinit var inputPort: EditText
    private val TAG = MainActivity::class.java.simpleName
    private var stompClient: StompClient? = null
    private var mRestPingDisposable: Disposable? = null
    private var compositeDisposable: CompositeDisposable? = null
    private val mTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private lateinit var notificationService: Intent

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra("message")
            message?.let {
                toast(it)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.tv_message)
        buttonConnect = findViewById(R.id.btn_connect)
        buttonSend = findViewById(R.id.buttonSend)
        inputIpAddress = findViewById(R.id.input_ip)
        inputPort = findViewById(R.id.input_port)
        notificationService = Intent(this@MainActivity, NotificationService::class.java)

        val filter = IntentFilter("DisplayNotificationAction")
        ContextCompat.registerReceiver(
            this,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

//        GlobalScope.launch {
//            withContext(Dispatchers.IO) {
//                startService(
//                    notificationService
//                )
//            }
//        }


//        resetSubscriptions()
//
//        stompClient = Stomp.over(
//            Stomp.ConnectionProvider.OKHTTP,
//            "ws://80.88.8.239:5042/pwt-websocket/websocket"
//        )
//
//
//
//
//
//
        buttonSend.setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    startService(notificationService)
                }
            }
        }

        buttonConnect.setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    stopService(notificationService)
                }
            }
        }

    }

    private fun sendMessage(notification: TransactionNotification) {
        stompClient?.send(
            SEND_PATH,
            Gson().toJson(notification, TransactionNotification::class.java)
        )
            ?.compose(applySchedulers())
            ?.subscribe({ Log.d(TAG, "STOMP echo send successfully") }) { throwable ->
                Log.e(TAG, "Error send STOMP echo", throwable)
                toast(throwable.message ?: error("Unknown error occurred"))
            }?.let { compositeDisposable?.add(it) }
    }

    protected fun applySchedulers(): CompletableTransformer? {
        return CompletableTransformer { upstream: Completable ->
            upstream
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    private fun resetSubscriptions() {
        if (compositeDisposable != null) {
            compositeDisposable?.dispose()
        }
        compositeDisposable = CompositeDisposable()
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        stompClient?.disconnect()

        if (mRestPingDisposable != null) mRestPingDisposable?.dispose()
        if (compositeDisposable != null) compositeDisposable?.dispose()
        super.onDestroy()
    }


    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}