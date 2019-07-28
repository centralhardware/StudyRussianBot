package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects.Enums

enum class UserStatus {
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