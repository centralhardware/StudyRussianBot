package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Interfaces;

import java.io.IOException;

public interface ResourceInt {
    String getStringByKey(String key);

    String getStringFromResources(String fileName) throws IOException;
}
