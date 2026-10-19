package me.centralhardware.znatoki.studyRussianBot.objects

import kotlinx.coroutines.sync.Mutex
import me.centralhardware.znatoki.studyRussianBot.objects.enums.UserStatus

/** What an answer did: the word it was for, whether it was right, and what comes next. */
data class AnswerOutcome(val word: Word, val correct: Boolean, val next: Word?) {
    val finished get() = next == null
}

data class TelegramUser(val chatId: Long) {
    /** Held while an update changes this user's state, so double taps don't race. */
    val lock = Mutex()

    var currRule: Rule? = null
    var status: UserStatus = UserStatus.NONE
    var words: MutableList<Word> = mutableListOf()
    var wrongWords: MutableList<Word> = mutableListOf()
    var count: Int = 0

    fun reset() {
        status = UserStatus.NONE
        words.clear()
        wrongWords.clear()
        count = 0
    }

    /** Starts a test over [picked]. */
    fun start(picked: Collection<Word>) {
        status = UserStatus.TESTING
        words.clear()
        words.addAll(picked)
        count = picked.size
    }

    /**
     * Checks [text] against the current word. A right answer drops the word, a
     * wrong one moves it to the back of the queue to be asked again.
     */
    fun answer(text: String): AnswerOutcome {
        val word = words.removeAt(0)
        val correct = word.answer.equals(text.trim(), ignoreCase = true)
        if (!correct) {
            words.add(word)
            wrongWords.add(word)
        }
        return AnswerOutcome(word, correct, words.firstOrNull())
    }

    fun getTestingResult(): String {
        val result = wrongWords.map { it.name }.groupingBy { it }.eachCount()
        return buildString {
            append("всего слов в тестировании $count\n")
            append("слова, в которых допущены ошибки\n")
            result.forEach { (k, v) -> append("$k - $v\n") }
            append("всего ${result.size} слов\n")
        }
    }
}

fun profileText(right: Long, wrong: Long, checkedRules: List<String>): String {
    val all = right + wrong
    val rightPercent = if (all == 0L) 100 else (right * 100 / all).toInt()
    return buildString {
        append("Профиль:\n\n\n")
        append("слов отвечено правильно: $right\n")
        append("слов отвечено неправильно $wrong\n")
        append("процент правильных ответов $rightPercent%\n")
        append("пройденные правила:\n")
        append(checkedRules.joinToString("\n"))
    }
}
