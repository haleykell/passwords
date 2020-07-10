package me.haleykell.userdata;

/**
 * Website class, uses Username and Password objects to store a website's information
 * @author Haley Kell
 */

public class Website {

    /** website */
    private String website;
    /** username */
    private Username user;
    /** password */
    private Password pass;

    /**
     * Constructor
     * @param website to be stored
     * @param username to be stored
     * @param password to be stored
     */
    public Website(String website, Username username, Password password) {
        this.website = website;
        user = username;
        pass = password;
    }

    /**
     * Returns string representation of the entire object
     * @return website, username, and password
     */
    public String toStringUserPass() {
        String result = "website: " + website + "\n" + "  username: " + this.user.toString() + "\n" + "  password: " + this.pass.toString();
        return result;
    }

    /**
     * Sets the website
     * @param website to be stored
     */
    public void setWebsite(String website) { this.website = website; }

    /**
     * Sets the username
     * @param username for the website
     */
    public void setUsername(Username username) { this.user = username; }

    /**
     * Sets the password
     * @param password for the website
     */
    public void setPassword(Password password) { this.pass = password; }

    /**
     * Returns the username Object for the website
     * @return username
     */
    public Username getUsername() { return this.user; }

    /**
     * Returns the Password object for the website
     * @return password
     */
    public Password getPassword() { return this.pass; }

    /**
     * Returns string of website name
     * @return website
     */
    public String toString() { return website; }
}
