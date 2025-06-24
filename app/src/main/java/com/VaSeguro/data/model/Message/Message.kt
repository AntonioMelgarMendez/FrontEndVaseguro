package com.VaSeguro.data.model.Message

data class Message(
  val id: Long = System.currentTimeMillis(),
  val content: String,
  val isUser: Boolean = true,
  val timestamp: String
)