package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Objects;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SuppressWarnings("HardCodedStringLiteral")
public class WordTest {

    @Test
    public void parse() throws IOException {
        String notParse = "деструкц_я деструкцИя и r3 1 10";
        assertNull(Word.parse(notParse));
        var wordString = getStringFromResources();
        var wordObject = new JSONObject(wordString);
        var wordArray = wordObject.getJSONArray("word");
        var words = new ArrayList<Word>();
        for (int i = 0; i < wordArray.length(); i++) {
            assertNotNull(Word.parse((String) wordArray.get(i)));
        }
    }

    /**
     * get string from resource folder file
     * @return string from file that placed in resource folder
     */
    private String getStringFromResources() throws IOException {
        var classLoader = ClassLoader.getSystemClassLoader();
        var inputStream = classLoader.getResourceAsStream("word.json");
        var writer = new StringWriter();
        IOUtils.copy(Objects.requireNonNull(inputStream), writer);
        return writer.toString();
    }
}