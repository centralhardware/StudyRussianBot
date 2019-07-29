package ru.alexeyFedechkin.znatoki.studyRussianBot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.alexeyFedechkin.znatoki.studyRussianBot.utils.RSA

class RSATest {
    private val key = "f05CRI93II5ToscYyOx9k6m+qBOkOCRjOtKV0xwGPOk5/kgPMT7/p4YZfv7JfXBuNM9pXp/t4zDVjq7NqE0FIFOF" +
            "FxXVKUd8UdqY5vbSX5vTx0VyPjvbKsqv9TJt72VSv/d2zrBCX8SUA/k1OtRyQJwbG6OcxFLIiW2xBFlco0eKuIyTXAIZt9OHXEXJJ/rnYk" +
            "DX2Qhvyl5YpRz4GzcESazik/YPF4XYQIhqcg3DBuwP5IeCa4eYEOcz7ajozexQapRKVLBN3LMvjSXoe9uy6W7leNHe/uF8q5oUT1yPGM/" +
            "12KPsV5u8b78QO9+0/rH1Qzh8Ojc87h56Alcxmg8rBg=="

    @Test
    fun generateKey() {
        assertEquals(key, RSA.generateKey("central"))
    }

    @Test
    fun validateKey() {
        assertTrue(RSA.validateKey("central", key))
    }
}