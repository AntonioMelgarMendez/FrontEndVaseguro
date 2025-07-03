package com.VaSeguro.data.Dao.Message

import androidx.room.*
import com.VaSeguro.data.Entitys.Message.MessageEntity

@Dao
interface MessageDao {

    // Obtener mensajes entre dos usuarios
    @Query("SELECT * FROM messages WHERE (senderId = :user1Id AND receiverId = :user2Id) OR (senderId = :user2Id AND receiverId = :user1Id)")
    suspend fun getMessagesBetweenUsers(user1Id: String, user2Id: String): List<MessageEntity>

    // Insertar un mensaje
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    // Eliminar un mensaje
    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    // Eliminar un mensaje por ID
    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    // Obtener todos los mensajes
    @Query("SELECT * FROM messages")
    suspend fun getAllMessages(): List<MessageEntity>


}
