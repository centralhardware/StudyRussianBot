/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Object.Enums;

/**
 *
 */
public enum UserStatus {
    NONE,
    TESTING, // passing the rules
    WAIT_COUNT_OF_WORD, // input count of word for testing
    WAIT_KEY // input activated key
}
