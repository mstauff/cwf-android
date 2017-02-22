package org.ldscd.callingworkflow.model;

import java.util.Date;

/**
 * Represents a possible calling to be made.
 */
public class Calling {
    /* Fields */
    private long memberId;
    private long proposedIndId;
    private Date activeDate;
    private long positionId;
    private long positionTypeId;
    private String position;
    private String existingStatus;
    private String proposedStatus;
    private Boolean hidden;
    private String notes;
    private Boolean editableByOrg;

    /* Constructors */
    public Calling() {}

    public Calling(long memberId, long proposedIndId, Date activeDate, long positionId, long positionTypeId, String position,
                   String existingStatus, String proposedStatus, Boolean hidden, String notes, Boolean editableByOrg) {
        this.memberId = memberId;
        this.proposedIndId = proposedIndId;
        this.activeDate = activeDate;
        this.positionId = positionId;
        this.positionTypeId = positionTypeId;
        this.position = position;
        this.existingStatus = existingStatus;
        this.proposedStatus = proposedStatus;
        this.hidden = hidden;
        this.notes = notes;
        this.editableByOrg = editableByOrg;
    }

    /* Properties */
    public long getMemberId() {
        return memberId;
    }
    public void setMemberId(long memberId) {
        this.memberId = memberId;
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

    public String getProposedStatus() {
        return proposedStatus;
    }
    public void setProposedStatus(String proposedStatus) {
        this.proposedStatus = proposedStatus;
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

    public Date getActiveDate() { return activeDate; }
    public void setActiveDate(Date activeDate) { this.activeDate = activeDate; }

    public long getPositionTypeId() {
        return positionTypeId;
    }
    public void setPositionTypeId(long positionTypeId) {
        this.positionTypeId = positionTypeId;
    }

    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }

    public String getExistingStatus() {
        return existingStatus;
    }
    public void setExistingStatus(String existingStatus) {
        this.existingStatus = existingStatus;
    }

    public Boolean getHidden() {
        return hidden;
    }
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
}