package me.centralhardware.znatoki.studyRussianBot

import me.centralhardware.znatoki.studyRussianBot.objects.TelegramUser
import me.centralhardware.znatoki.studyRussianBot.objects.Word
import me.centralhardware.znatoki.studyRussianBot.objects.enums.UserStatus
import me.centralhardware.znatoki.studyRussianBot.objects.profileText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestingFlowTest {
    private val a = Word("ёж", "..ж", "ё")
    private val b = Word("шёлк", "ш..лк", "ё")
    private val c = Word("щека", "щ..ка", "е")

    @Test
    fun `a right answer is recorded against the word just answered`() {
        val user = TelegramUser(1).apply { start(listOf(a, b)) }
        val first = user.answer("ё")
        assertEquals(a, first.word)
        assertTrue(first.correct)
        assertEquals(b, first.next)

        val last = user.answer("Ё")
        assertEquals(b, last.word, "the last word is reported too")
        assertTrue(last.finished)
    }

    @Test
    fun `a wrong answer requeues the word`() {
        val user = TelegramUser(1).apply { start(listOf(a, c)) }
        val o = user.answer("е")
        assertFalse(o.correct)
        assertEquals(a, o.word)
        assertEquals(c, o.next)
        assertEquals(listOf(c, a), user.words)
        assertEquals(listOf(a), user.wrongWords)
        assertEquals(2, user.count)
        assertEquals(UserStatus.TESTING, user.status)
    }

    @Test
    fun `result lists the mistakes`() {
        val user = TelegramUser(1).apply { start(listOf(a)) }
        user.answer("x")
        user.answer("x")
        user.answer("ё")
        val r = user.getTestingResult()
        assertTrue(r.contains("..ж - 2"))
        assertTrue(r.contains("всего слов в тестировании 1"))
    }

    @Test
    fun `reset clears the test`() {
        val user = TelegramUser(1).apply { start(listOf(a)) }
        user.reset()
        assertEquals(UserStatus.NONE, user.status)
        assertTrue(user.words.isEmpty())
        assertNull(user.words.firstOrNull())
    }

    @Test
    fun `last page follows the rule count`() {
        assertEquals(0, lastPage(0))
        assertEquals(0, lastPage(7))
        assertEquals(1, lastPage(8))
        assertEquals(4, lastPage(35))
        assertEquals(5, lastPage(36))
    }

    @Test
    fun `profile percent`() {
        assertTrue(profileText(3, 1, listOf("r1")).contains("процент правильных ответов 75%"))
        assertTrue(profileText(0, 0, emptyList()).contains("100%"))
    }
}
