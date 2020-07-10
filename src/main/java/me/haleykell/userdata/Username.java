package me.haleykell.userdata;

/**
 * Stores the username
 * @author Haley Kell
 */

public class Username {

    /** username */
    private String username;

    /**
     * Constructor
     * @param username to be stored
     */
    public Username(String username) { this.username = username; }

    /**
     * Returns the username
     * @return username
     */
    @Override
    public String toString() { return username; }
}
