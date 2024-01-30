package com.xpresspay.sockettest.model

data class TransactionNotification(
    val amount: String,
    val channel: String,
    val created_date: String,
    val details: String,
    val paid_date: String,
    val paid_time: String,
    val payer_account_bvn: String,
    val payer_account_name: String,
    val payer_account_number: String,
    val payer_bank: PayerBank,
    val recipient_account_number: String,
    val trans_key: String,
    val transaction_id: String,
    val transaction_reference: String,
    val type: String
)