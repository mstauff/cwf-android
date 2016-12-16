package org.ldscd.callingworkflow.model;

/**
 * Represents a possible calling to be made.
 */
public class Calling {
    /* Fields */
    private long currentIndId;
    private long proposedIndId;
    private long positionId;
    private String completedStatuses;
    private String name;
    private String description;
    private long parentOrg;
    private String notes;
    private Boolean editableByOrg;

    /* Constructors */
    public Calling() {}
    public Calling(long currentIndId, long proposedIndId, long positionId, String completedStatuses, String name, String description, long parentOrg, String notes, Boolean editableByOrg) {
        this.currentIndId = currentIndId;
        this.proposedIndId = proposedIndId;
        this.positionId = positionId;
        this.completedStatuses = completedStatuses;
        this.name = name;
        this.description = description;
        this.parentOrg = parentOrg;
        this.notes = notes;
        this.editableByOrg = editableByOrg;
    }

    /* Properties */
    public long getCurrentIndId() {
        return currentIndId;
    }
    public void setCurrentIndId(long currentIndId) {
        this.currentIndId = currentIndId;
    }

    public long getProposedIndId() {
        return proposedIndId;
    }
    public void setProposedIndId(long proposedIndId) {
        this.proposedIndId = proposedIndId;
    }

    public long getPositionId() {
        return positionId;
    }
    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }

    public String getCompletedStatuses() {
        return completedStatuses;
    }
    public void setCompletedStatuses(String completedStatuses) {
        this.completedStatuses = completedStatuses;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public long getParentOrg() {
        return parentOrg;
    }
    public void setParentOrg(long parentOrg) {
        this.parentOrg = parentOrg;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getEditableByOrg() {
        return editableByOrg;
    }
    public void setEditableByOrg(Boolean editableByOrg) {
        this.editableByOrg = editableByOrg;
    }
}