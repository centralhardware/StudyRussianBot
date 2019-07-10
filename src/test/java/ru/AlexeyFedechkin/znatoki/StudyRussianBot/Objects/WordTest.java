package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects;

import org.json.JSONObject;
import org.junit.Test;
import ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Resource;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SuppressWarnings("HardCodedStringLiteral")
public class WordTest {

    private final Resource resource = new Resource();

    @Test
    public void parse() throws IOException {
        String notParse = "деструкц_я деструкцИя и r3 1 10";
        String parse = "деструкц_я деструкцИя и r3 1 1 0";
        assertNull(Word.parse(notParse));
        assertNotNull(Word.parse(parse));
        var wordString = resource.getStringFromResources("word.json");
        var wordObject = new JSONObject(wordString);
        var wordArray = wordObject.getJSONArray("word");
        for (int i = 0; i < wordArray.length(); i++) {
            assertNotNull(Word.parse((String) wordArray.get(i)));
        }
    }

}