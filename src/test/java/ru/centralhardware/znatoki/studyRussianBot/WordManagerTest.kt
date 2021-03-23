package ru.centralhardware.znatoki.studyRussianBot

import junit.framework.Assert.assertEquals
import org.junit.Test

class WordManagerTest {

    @Test
    fun init() {
        WordManager.init()
    }

    @Test
    fun word() {
        WordManager.init()
        val word = WordManager.rules[0].words[0]
        assertEquals(word.wrightName, "жир" )
        assertEquals(word.name, "ж_р")
        assertEquals(word.section, "r1")
        assertEquals(word.answer, "и")
    }

}