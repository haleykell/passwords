package me.haleykell;

import me.haleykell.userdata.Password;
import me.haleykell.userdata.Username;
import me.haleykell.userdata.Website;

import java.io.*;
import java.util.ArrayList;

/**
 * Reads and parses the websites and their information
 * @author Haley Kell
 */

public class DataFile {

    /** .yml file location */
    String fileLoc = "data.yml";
    /** File */
    private File file = new File(fileLoc);
    /** Stores the parsed website information */
    ArrayList<Website> data = new ArrayList<>();

    /** Constructor */
    public DataFile() { readData(); }

    /**
     * Reads and parses the data
     */
    private void readData()
    {
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();

            while (line != null) {
                String[] words = line.split(" ");
                Website web;
                Password pass;
                Username user;

                String website = words[1];
                line = br.readLine();
                words = line.split(" ");

                user = new Username(words[3]);
                line = br.readLine();
                words = line.split(" ");

                pass = new Password(words[3]);
                web = new Website(website, user, pass);
                data.add(web);

                line = br.readLine();
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Returns the list of parsed websites
     * @return list of the websites from the file
     */
    public ArrayList<Website> getData() {
        return data;
    }

    /**
     * Writes the updated list to the .yml file
     * @param dataList list of websites from the file
     */
    public void writeFile(ArrayList<Website> dataList) {
        try {
            PrintWriter writer = new PrintWriter(fileLoc, "UTF-8");
            for (Website w : dataList) { writer.println(w.toStringUserPass()); }
            writer.close();
        }
        catch (IOException e) { e.printStackTrace(); }
    }
}
