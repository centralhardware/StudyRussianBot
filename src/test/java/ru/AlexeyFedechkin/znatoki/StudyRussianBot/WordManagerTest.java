package ru.AlexeyFedechkin.znatoki.StudyRussianBot;

import org.junit.Test;

import java.io.IOException;

public class WordManagerTest {

    @Test
    public void init() throws IOException {
        WordManager wordManager = new WordManager();
        wordManager.init();
    }
}