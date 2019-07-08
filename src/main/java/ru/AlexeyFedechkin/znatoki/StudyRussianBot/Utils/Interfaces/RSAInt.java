package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Utils.Interfaces;

public interface RSAInt {
    String generateKey(String message);

    boolean validateKey(String userName, String key);
}
