package org.ldscd.callingworkflow.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ldscd.callingworkflow.constants.ConflictCause;

import java.util.UUID;

/**
 * Represents a possible calling to be made.
 */
public class Calling {
    /* Fields */
    /* id is from LCR */
    private Long positionId;
    /* cwfId is and Id that if LCR doesn't have one we generate an Id. */
    private String cwfId;
    private Long memberId;
    private Long proposedIndId;
    private String activeDate;
    private transient DateTime activeDateTime;
    private Position position;
    private String existingStatus;
    private String proposedStatus;
    private String notes;
    private transient Long parentOrg;
    private transient ConflictCause conflictCause;
    private transient DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
    /* Constructors */
    public Calling() {}

    public Calling(Long positionId, String cwfId, Long memberId, Long proposedIndId, DateTime activeDateTime, Position position,
                   String existingStatus, String proposedStatus, String notes, Long parentOrg) {
        this.positionId = positionId;
        /* If we don't have a unique id then we create an internal one. */
        if(positionId == null || positionId == 0) {
            this.cwfId = (cwfId != null && cwfId.length() > 0) ? cwfId : UUID.randomUUID().toString();
        }
        this.memberId = memberId;
        this.proposedIndId = proposedIndId;
        this.activeDateTime = activeDateTime;
        this.activeDate = activeDateTime != null ? fmt.print(activeDateTime) : "";
        this.position = position;
        this.existingStatus = existingStatus;
        this.proposedStatus = proposedStatus;
        this.notes = notes;
        this.parentOrg = parentOrg;
    }

    /* Properties */
    public String getCallingId() {
        return positionId != null && positionId > 0 ? positionId.toString() : cwfId;
    }

    public Long getId() { return this.positionId; }
    public void setId(long positionId) {
        this.positionId = positionId;
    }

    public String getCwfId() {
        return cwfId;
    }
    public void setCwfId(String cwfId) {
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

    public DateTime getActiveDateTime() { return activeDateTime; }
    public void setActiveDateTime(DateTime activeDate) { this.activeDateTime = activeDateTime; }

    public String getActiveDate() { return activeDate; }
    public void setActiveDate(DateTime activeDate) {
        this.activeDate = fmt.print(activeDateTime); }

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
            if((this.positionId != null && this.positionId.equals(calling.positionId)) || !this.position.getAllowMultiple())  {
                result = true;
            } else if (this.cwfId != null) {
                result = this.cwfId.equals(calling.getCwfId());
            }
        }
        return result;
    }

    public void importCWFData(Calling sourceCalling) {
        cwfId = sourceCalling.getCwfId();
        existingStatus = sourceCalling.getExistingStatus();
        proposedIndId = sourceCalling.getProposedIndId();
        proposedStatus = sourceCalling.getProposedStatus();
        notes = sourceCalling.getNotes();
    }
}