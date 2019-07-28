package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects

import java.util.ArrayList
import java.util.HashMap

class UserStatistic {
     var totalSend = ArrayList<Int>()
     var totalReceived = ArrayList<Int>()
     var userSend = HashMap<Long, ArrayList<Int>>()
     var userReceived = HashMap<Long, ArrayList<Int>>()
     var totalCountOfSend: Int = 0
     var totalCountReceived: Int = 0
     var userCountSent = HashMap<Long, Int>()
     var userReceivedSent = HashMap<Long, Int>()
}