package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects

import org.json.JSONObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource
import java.io.IOException

class WordTest {
    @Test
    @Throws(IOException::class)
    fun parse() {
        val notParse = "деструкц_я деструкцИя и r3 1 10"
        val parse = "деструкц_я деструкцИя и r3 1 1 0"
        assertNull(Word.parse(notParse))
        assertNotNull(Word.parse(parse))
        val wordString = Resource.getStringFromResources("word.json")
        val wordObject = JSONObject(wordString)
        val wordArray = wordObject.getJSONArray("word")
        for (i in 0 until wordArray.length()) {
            assertNotNull(Word.parse(wordArray.get(i) as String))
        }
    }
}