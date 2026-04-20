package com.helpapp.therapy.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.helpapp.therapy.data.db.entities.UserContextEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserContextDao {
    @Query("SELECT * FROM user_context_variables WHERE id = :id LIMIT 1")
    fun observe(id: Int = UserContextEntity.SINGLETON_ID): Flow<UserContextEntity?>

    @Query("SELECT * FROM user_context_variables WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = UserContextEntity.SINGLETON_ID): UserContextEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserContextEntity)
}
