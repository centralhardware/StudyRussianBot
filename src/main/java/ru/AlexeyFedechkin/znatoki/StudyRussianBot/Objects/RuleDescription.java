package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects;

/**
 * Data about description of rule
 */
public class RuleDescription {
    private String name;
    private String description;
    private int pageNumber;
    private int id;

    public RuleDescription(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
