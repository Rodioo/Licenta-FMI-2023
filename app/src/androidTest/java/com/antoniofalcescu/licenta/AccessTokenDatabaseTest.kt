package com.antoniofalcescu.licenta

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.antoniofalcescu.licenta.repository.accessToken.AccessToken
import com.antoniofalcescu.licenta.repository.accessToken.AccessTokenDao
import com.antoniofalcescu.licenta.repository.accessToken.AccessTokenDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AccessTokenDatabaseTest {

    private lateinit var accessTokenDao: AccessTokenDao
    private lateinit var db: AccessTokenDatabase

    @Before
    fun createDb() {
        println("1")
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AccessTokenDatabase::class.java).allowMainThreadQueries().build()

        accessTokenDao = db.accessTokenDao
        println("2")
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        println("5")
        db.close()
        println("6")
    }

    @Test
    @Throws(Exception::class)
    fun saveAndGetAccessToken() {
        println("3")
        val savedAccessToken = AccessToken(value = "test-token")
        accessTokenDao.save(savedAccessToken)
        val gottenAccessToken = accessTokenDao.get()
        assertEquals(gottenAccessToken.value, savedAccessToken.value)
        println("4")
    }
}