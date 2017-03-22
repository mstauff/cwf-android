package org.ldscd.callingworkflow.model;

import com.google.gson.annotations.Expose;

import org.ldscd.callingworkflow.constants.ConflictCause;

import java.util.Date;

/**
 * Represents a possible calling to be made.
 */
public class Calling {
    /* Fields */
    private Long id;
    private Long cwfId;
    private Long memberId;
    private Long proposedIndId;
    private Date activeDate;
    private Position position;
    private String existingStatus;
    private String proposedStatus;
    private String notes;
    private Boolean editableByOrg;
    private transient Long parentOrg;
    @Expose(serialize = false, deserialize = false)
    private ConflictCause conflictCause;

    /* Constructors */
    public Calling() {}

    public Calling(Long id, Long cwfId, Long memberId, Long proposedIndId, Date activeDate, Position position,
                   String existingStatus, String proposedStatus, String notes, Boolean editableByOrg, Long parentOrg) {
        this.id = id;
        this.cwfId = cwfId;
        this.memberId = memberId;
        this.proposedIndId = proposedIndId;
        this.activeDate = activeDate;
        this.position = position;
        this.existingStatus = existingStatus;
        this.proposedStatus = proposedStatus;
        this.notes = notes;
        this.editableByOrg = editableByOrg;
        this.parentOrg = parentOrg;
    }

    /* Properties */
    public Long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public Long getCwfId() {
        return cwfId;
    }
    public void setCwfId(Long cwfId) {
        this.cwfId = cwfId;
    }

    public Long getMemberId() {
        return memberId;
    }
    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public Long getProposedIndId() {
        return proposedIndId;
    }
    public void setProposedIndId(long proposedIndId) {
        this.proposedIndId = proposedIndId;
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

    public Position getPosition() {
        return position;
    }
    public void setPosition(Position position) {
        this.position = position;
    }

    public String getExistingStatus() {
        return existingStatus;
    }
    public void setExistingStatus(String existingStatus) {
        this.existingStatus = existingStatus;
    }

    public Long getParentOrg() {
        return parentOrg;
    }
    public void setParentOrg(Long parentOrg) {
        this.parentOrg = parentOrg;
    }

    public ConflictCause getConflictCause() {
        return this.conflictCause;
    }
    public void setConflictCause(ConflictCause conflictCause) {
        this.conflictCause = conflictCause;
    }

    public boolean equals(Calling calling) {
        boolean result = false;
        if(this.parentOrg.equals(calling.parentOrg) && this.getPosition().equals(calling.getPosition())) {
            if(this.id != null && this.id == calling.id)  {
                result = true;
            } else if (this.cwfId != null) {
                result = this.cwfId == calling.getCwfId();
            }
        }
        return result;
    }
}