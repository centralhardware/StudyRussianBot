package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects

class RuleDescription {
    val name: String
    val description: String
    var pageNumber: Byte = 0
    val id: Int

    constructor(name: String, description: String, id: Int){
        this.name = name
        this.description = description
        this.id = id
    }
}