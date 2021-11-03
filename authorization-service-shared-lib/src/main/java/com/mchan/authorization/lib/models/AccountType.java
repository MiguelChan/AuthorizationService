package com.mchan.authorization.lib.models;

/**
 * Defines the Type of the account.
 * e.g. An Account can be the Type of "Classic" which stands for Username/Password
 * or it could be "Google" which have several kind of other addons in order to create the Profile.
 */
public enum AccountType {

    CLASSIC("Classic");

    private final String accountType;

    /**
     * .
     *
     * @return .
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * .
     *
     * @param accountType .
     */
    AccountType(String accountType) {
        this.accountType = accountType;
    }
}
