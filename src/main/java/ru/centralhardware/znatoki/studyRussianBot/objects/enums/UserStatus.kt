package ru.centralhardware.znatoki.studyRussianBot.objects.enums

/**
 *user status
 *bot used state machine for control state of user
 */
enum class UserStatus {
    /**
     *user have not any acton or user just start bot
     */
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
}