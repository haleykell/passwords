package me.haleykell;

import me.haleykell.userdata.Password;
import me.haleykell.userdata.Username;
import me.haleykell.userdata.Website;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Menu class, contains main functionality of the program
 * @author Haley Kell
 */

public class Menu {

    private static final int GET_INFO = 1;
    private static final int ADD_WEBSITE = 2;
    private static final int DELETE_WEBSITE = 3;
    private static final int CHANGE_USER = 4;
    private static final int CHANGE_PASS = 5;
    private static final int QUIT = 8;
    private static final int PRINT_ALL = 7;
    private static final int DELETE_ALL = 6;

    /** Constructor */
    public Menu() {}

    /**
     * Calls showMenu() and appropriate method based on response
     * @param dataList list of websites from file
     * @param input keyboard
     */
    public void menu(ArrayList<Website> dataList, Scanner input) {
        int response = showMenu(input);

        while (response != QUIT) {
            switch (response) {
                case GET_INFO:
                    getInfo(dataList, input);
                    break;
                case ADD_WEBSITE:
                    addWebsite(dataList, input);
                    break;
                case DELETE_WEBSITE:
                    deleteWebsite(dataList, input);
                    break;
                case CHANGE_USER:
                    changeUsername(dataList, input);
                    break;
                case CHANGE_PASS:
                    changePassword(dataList, input);
                    break;
                case DELETE_ALL:
                    deleteAll(dataList, input);
                    break;
                case PRINT_ALL:
                    printAllWebsites(dataList);
                    break;

            }
            response = showMenu(input);
        }

    }

    /**
     * Displays menu and receives response
     * @param input keyboard
     * @return response of user
     */
    private int showMenu(Scanner input) {
        System.out.println("Welcome to PasswordManager!");
        System.out.println("Choose from the following options:");
        System.out.println("1. Get a username and password for a website.");
        System.out.println("2. Add a website's username and password to the database.");
        System.out.println("3. Delete a website's username and password from the database.");
        System.out.println("4. Change a username for a website.");
        System.out.println("5. Change a password for a website");
        System.out.println("6. Delete all websites, usernames, and passwords.");
        System.out.println("7. Print all usernames and passwords in the database.");
        System.out.println("8. Quit.");

        int response = input.nextInt();

        return response;
    }

    /**
     * Displays username and password for a given website
     * @param data list of websites from file
     * @param input keyboard
     */
    private void getInfo(ArrayList<Website> data, Scanner input) {
        System.out.println();
        System.out.println("For what website would you like the username and password?");
        for (Website w : data) { System.out.println(w.toString()); }
        String response = input.next();

        for (Website w : data) {
            if (response.equalsIgnoreCase(w.toString())) { System.out.println(w.toStringUserPass()); }
        }
        System.out.println();
    }

    /**
     * Adds a website and its info to the list
     * @param data list of websites from file
     * @param input keyboard
     */
    private void addWebsite(ArrayList<Website> data, Scanner input) {
        System.out.println();
        System.out.println("What is the name of the website?");
        String website = input.next();
        System.out.println("What is the username?");
        String user = input.next();

        for (Website web : data) {
            if (web.toString().equalsIgnoreCase(website) && web.getUsername().toString().equalsIgnoreCase(user)) {
                System.out.println("This website and username already exist. Would you like to update the password?");
                if (input.next().equalsIgnoreCase("yes")) {
                    System.out.println("What is the new password?");
                    String pass = input.next();

                    web.setPassword(new Password(pass));

                    System.out.println("The password for website: " + website + " and username: " + user +
                            " has been updated with " + pass + ".");

                    return;
                }
                else return;
            }
        }

        System.out.println("What is the password?");
        String pass = input.next();

        Website w = new Website(website, new Username(user), new Password(pass));

        data.add(w);

        System.out.println("The website " + website + " with the username " + user +
                " and password " + pass + " has been added.");
        System.out.println();
    }

    /**
     * Deletes a given website and its info
     * @param data list of websites from file
     * @param input keyboard
     */
    private void deleteWebsite(ArrayList<Website> data, Scanner input) {
        System.out.println();
        System.out.println("What website should be deleted?");
        for (Website w : data) { System.out.println(w.toString()); }
        String response = input.next();

        Website remove = new Website("", new Username(""), new Password(""));
        for (Website w : data) {
            if (response.equalsIgnoreCase(w.toString())) remove = w;
        }

        data.remove(remove);
        System.out.println("The website " + response + " was deleted.");
        System.out.println();
    }

    /**
     * Deletes the entire list of websites
     * @param data list of websites from file
     * @param input keyboard
     */
    private void deleteAll(ArrayList<Website> data, Scanner input) {
        System.out.println("Are you sure you want to delete all the websites and their data?");
        if (input.next().equalsIgnoreCase("no")) return;
        data.clear();
        System.out.println("All websites deleted.");
    }

    /**
     * Prints all websites and their info to the console
     * @param data list of websites from file
     */
    private void printAllWebsites(ArrayList<Website> data) {
        System.out.println();
        for (Website w : data) { System.out.println(w.toStringUserPass()); }
        System.out.println();
    }

    /**
     * Changes the username of a given website and checks if the password needs to be changed
     * @param data list of websites from file
     * @param input keyboard
     */
    private void changeUsername(ArrayList<Website> data, Scanner input) {
        System.out.println();
        System.out.println("What is the name of the website?");
        for (Website w : data) { System.out.println(w.toString()); }
        String website = input.next();

        System.out.println("Did the password change as well?");
        if (input.next().equalsIgnoreCase("yes")) { changeWebsite(data, input, website); return; }

        System.out.println("What is the new username?");
        String user = input.next();

        for (int index = 0; index < data.size(); ++index) {
            if (data.get(index).toString().equalsIgnoreCase(website)) {
                data.get(index).setUsername(new Username(user));
            }
        }

        System.out.println("Username updated.");
        System.out.println();
    }

    /**
     * Changes the password of a given website and checks if the username needs to changed
     * @param data list of websites from file
     * @param input keyboard
     */
    private void changePassword(ArrayList<Website> data, Scanner input) {
        System.out.println();
        System.out.println("What is the name of the website?");
        for (Website w : data) { System.out.println(w.toString()); }
        String website = input.next();

        System.out.println("Did the username change as well?");
        if (input.next().equalsIgnoreCase("yes")) { changeWebsite(data, input, website); return; }

        System.out.println("What is the new password?");
        String pass = input.next();

        for (int index = 0; index < data.size(); ++index) {
            if (data.get(index).toString().equalsIgnoreCase(website)) {
                data.get(index).setPassword(new Password(pass));
            }
        }

        System.out.println("Password updated.");
        System.out.println();
    }

    /**
     * Called when both the username and password need to be updated
     * @param data list of websites from file
     * @param input keyboard
     * @param website to be changed
     */
    private void changeWebsite(ArrayList<Website> data, Scanner input, String website) {
        System.out.println();
        System.out.println("What is the new username?");
        String user = input.next();
        System.out.println("What is the new password?");
        String pass = input.next();

        for (int index = 0; index < data.size(); ++index) {
            if (data.get(index).toString().equalsIgnoreCase(website)) {
                data.get(index).setPassword(new Password(pass));
                data.get(index).setUsername(new Username(user));
            }
        }

        System.out.println("Username and Password updated.");
        System.out.println();
    }
}