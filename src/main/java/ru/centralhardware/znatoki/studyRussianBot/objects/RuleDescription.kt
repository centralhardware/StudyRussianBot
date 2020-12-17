package ru.centralhardware.znatoki.studyRussianBot.objects

/**
 *data class that contain structure of rule description
 */
data class RuleDescription(
        /**
         *name of rule that for which rule
         */
        val name: String,
        /**
         *description text
         */
        val description: String,
        /**
         *id of description
         */
        val id: Int) {
    /**
     *number of page which is located rule
     */
    var pageNumber: Int = 0

}