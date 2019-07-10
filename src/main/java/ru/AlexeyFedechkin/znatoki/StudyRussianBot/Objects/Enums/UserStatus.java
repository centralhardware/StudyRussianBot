/*
 * Author: Fedechkin Alexey Borisovich
 * last modified: 22.06.19 12:55
 * Copyright (c) 2019
 */

package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums;

/**
 *enumeration describe user status.
 */
public enum UserStatus {
    NONE,
    /**
     * passing the rules
     */
    TESTING,
    /**
     * input count of word for testing
     */
    WAIT_COUNT_OF_WORD,
    /**
     * input activated key
     */
    WAIT_KEY,
    /**
     * input message to admins
     */
    WAIT_REPORT
}
