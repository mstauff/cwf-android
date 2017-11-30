package org.ldscd.callingworkflow.constants;

/**
 * CRUD event enum.  Create, Read, Update, Delete, Release
 */
public enum Operation {
    CREATE("Create"),
    READ("Read"),
    UPDATE("Update"),
    DELETE("Delete"),
    RELEASE("Release"),
    DELETE_LCR("Delete LCR");

    String selection;
    Operation(String selection) {
        this.selection = selection;
    }
    public String getOperation() {
        return this.selection;
    }
}
