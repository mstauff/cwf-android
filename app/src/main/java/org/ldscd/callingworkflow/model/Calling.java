package org.ldscd.callingworkflow.model;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ldscd.callingworkflow.constants.CallingStatus;
import org.ldscd.callingworkflow.constants.ConflictCause;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a possible calling to be made.
 */
public class Calling implements Serializable {
    private static final long serialVersionUID = 1393285656423297834L;
    /* Fields */
    /* id is from LCR */
    private Long positionId;
    /* cwfId is and Id that if LCR doesn't have one we generate an Id. */
    private String cwfId;
    /* cwfOnly is true for callings created in CWF that haven't been saved/don't exist in LCR */
    private boolean cwfOnly;
    private Long memberId;
    private Long proposedIndId;
    private String activeDate;
    private transient DateTime activeDateTime;
    private Position position;
    private String existingStatus;
    private CallingStatus proposedStatus;
    private String notes;
    private transient Long parentOrg;
    private transient ConflictCause conflictCause;
    private transient DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
    /* Constructors */
    public Calling() {}

    public Calling(Calling calling) {
        this(calling.getId(), calling.getCwfId(), calling.isCwfOnly(), calling.getMemberId(), calling.getProposedIndId(),
                calling.getActiveDateTime(), calling.getPosition(), calling.getExistingStatus(),
                calling.getProposedStatus(), calling.getNotes(), calling.getParentOrg());
    }

    public Calling(Long positionId, String cwfId, boolean cwfOnly, Long memberId, Long proposedIndId, DateTime activeDateTime, Position position,
                   String existingStatus, CallingStatus proposedStatus, String notes, Long parentOrg) {
        this.positionId = positionId;
        /* If we don't have a unique id then we create an internal one. */
        if(positionId == null || positionId == 0) {
            this.cwfId = (cwfId != null && cwfId.length() > 0) ? cwfId : UUID.randomUUID().toString();
        }
        this.cwfOnly = cwfOnly;
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

    public boolean isCwfOnly() { return cwfOnly; }
    public void setCwfOnly(boolean cwfOnly) { this.cwfOnly = cwfOnly; }

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

    public CallingStatus getProposedStatus() {
        return proposedStatus;
    }
    public void setProposedStatus(CallingStatus proposedStatus) {
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

    /* This method is a custom comparator for internal equivalent.  It is not the same as the Object equals method. */
    public boolean isEqualsTo(Calling calling) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return (getCallingId().equals(((Calling) o).getCallingId()));
    }

    @Override
    public int hashCode() {
        return getCallingId().hashCode();
    }

    public void importCWFData(Calling sourceCalling) {
        cwfId = sourceCalling.getCwfId();
        existingStatus = sourceCalling.getExistingStatus();
        proposedIndId = sourceCalling.getProposedIndId();
        proposedStatus = sourceCalling.getProposedStatus();
        notes = sourceCalling.getNotes();
    }

    @Override
    public String toString() {
        return this.position.getName();
    }
}