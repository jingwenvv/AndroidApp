package com.cs4530spring2022.project1.util.models

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery


@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userTable: UserProfile?)

    @Query("DELETE FROM user_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM user_table ORDER BY id ASC")
    fun getAll(): LiveData<List<UserProfile?>?>?

    @Query("SELECT * FROM user_table WHERE id = :id")
    fun getUser(id: Int): UserProfile?

    @Query("Delete FROM user_table WHERE id = :id")
    suspend fun deleteUser(id: Int)

    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}