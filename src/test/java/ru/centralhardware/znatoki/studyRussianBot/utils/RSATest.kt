package ru.centralhardware.znatoki.studyRussianBot.utils

import org.junit.Assert.assertTrue
import org.junit.Test

class RSATest {

    companion object{
        const val USERNAME = "username"
    }

    @Test
    fun generateKey() {
        RSA.generateKey(USERNAME)
    }

    @Test
    fun validateKey() {
        val key = RSA.generateKey(USERNAME)
        assertTrue(RSA.validateKey(USERNAME, key))
    }
}