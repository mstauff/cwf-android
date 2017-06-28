package org.ldscd.callingworkflow.model.permissions;

/**
 * Security object to validate the calling objects.
 */
public class AuthorizableCalling extends Authorizable {
    /* Fields */
    AuthorizableOrg authorizableOrg;

    /* Constructor */
    public AuthorizableCalling(long unitNum, AuthorizableOrg authorizableOrg) {
        super(unitNum);
        this.authorizableOrg = authorizableOrg;
    }

    /* Properties */
    public AuthorizableOrg getAuthorizableOrg() {
        return authorizableOrg;
    }

    public void setAuthorizableOrg(AuthorizableOrg authorizableOrg) {
        this.authorizableOrg = authorizableOrg;
    }
}
