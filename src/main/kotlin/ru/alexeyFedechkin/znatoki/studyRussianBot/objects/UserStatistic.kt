package ru.alexeyFedechkin.znatoki.studyRussianBot.objects

import java.util.*

/**
 *transfer statistic data
 */
class UserStatistic {
    /**
     *List with data on the number of total transmitted messages per hour
     */
    var totalSend: ArrayList<Int> = ArrayList<Int>()
    /**
     *List with data on the number of total received messages per hour
     */
    var totalReceived: ArrayList<Int> = ArrayList<Int>()
    /**
     *List with data on the number of all transmitted messages for users per hour
     */
    var userSend: HashMap<Long, ArrayList<Int>> = HashMap()
    /**
     *List with data on the number of all received messages for all users per hour
     */
    var userReceived: HashMap<Long, ArrayList<Int>> = HashMap()
    /**
     *total transmitted
     */
    var totalCountOfSend: Int = 0
    /**
     *total received
     */
    var totalCountReceived: Int = 0
    /**
     *List with data of total transmitted for all user per hour
     */
    var userCountSent: HashMap<Long, Int> = HashMap()
    /**
     *List with data of total received for all user per hour
     */
    var userReceivedSent: HashMap<Long, Int> = HashMap()
}