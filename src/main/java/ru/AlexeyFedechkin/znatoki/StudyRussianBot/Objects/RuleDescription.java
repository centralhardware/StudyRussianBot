package ru.AlexeyFedechkin.znatoki.StudyRussianBot.Objects;

import java.util.Objects;

/**
 * Data about description of rule
 */
public class RuleDescription {
    private final String name;
    private final String description;
    private int pageNumber;
    private final int id;

    public RuleDescription(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public String getDescription() {
        return description;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleDescription that = (RuleDescription) o;
        return pageNumber == that.pageNumber &&
                id == that.id &&
                name.equals(that.name) &&
                description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, pageNumber, id);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public String toString() {
        return "RuleDescription{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", pageNumber=" + pageNumber +
                ", id=" + id +
                '}';
    }
}
