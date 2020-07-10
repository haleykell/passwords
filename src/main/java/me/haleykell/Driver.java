package me.haleykell;

import me.haleykell.userdata.Website;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * This program allows the user to store and retrieve usernames and passwords. This is helpful for people who use lots
 * of different passwords or who struggle to remember passwords.
 * @author Haley Kell
 */

public class Driver {

    public static void main(String[] args) {

        DataFile data = new DataFile();
        ArrayList<Website> dataList = data.getData();

        Menu menu = new Menu();

        Scanner input = new Scanner(System.in);
        menu.menu(dataList, input);

        data.writeFile(dataList);
        System.out.println("Data saved.");
    }
}
