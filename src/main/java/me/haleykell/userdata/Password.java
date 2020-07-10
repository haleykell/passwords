package me.haleykell.userdata;

/**
 * Stores the password
 * @author Haley Kell
 */

public class Password {

    /** password */
    private String password;

    /**
     * Constructor
     * @param password to be stored
     */
    public Password(String password) { this.password = password; }

    /**
     * Returns the password
     * @return password
     */
    @Override
    public String toString() { return password; }
}
