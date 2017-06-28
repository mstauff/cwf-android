package org.ldscd.callingworkflow.model.permissions.constants;

import org.ldscd.callingworkflow.constants.Operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ldscd.callingworkflow.constants.Operation.*;

/**
 * Domain level definition and associated events.
 */
public enum Domain {
    ORG_INFO (
        READ
    ),
    POTENTIAL_CALLING (
        CREATE,
        UPDATE,
        DELETE
    ),
    ACTIVE_CALLING (
        UPDATE,
        DELETE
    ),
    UNIT_GOOGLE_ACCOUNT (
        CREATE,
        UPDATE
    ),
    PRIESTHOOD_OFFICE (
        READ
    );

    /* Fields */
    private List<Operation> domains;
    /* Constructor */
    private Domain(Operation... operations) {
        this.domains = new ArrayList<>(Arrays.asList(operations));
    }
    /* Properties */
    public List<Operation> getDomains() {
        return domains;
    }
    /* Methods */
}