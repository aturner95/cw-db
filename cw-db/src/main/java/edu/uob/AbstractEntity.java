package edu.uob;

import java.io.*;

public class AbstractEntity {



    protected void readEntity() throws IOException {

        String dummyFileName = "db" + File.separator + "people.tab";

        File fileToOpen = new File(dummyFileName);
        if(fileToOpen.exists()){

            FileReader reader = new FileReader(fileToOpen);
            try(BufferedReader br = new BufferedReader(reader)){
                String line;
                while((line = br.readLine()) != null){
                    System.out.println(line);
                }
            } catch(FileNotFoundException fnfe){
                System.out.println(fnfe.getMessage());
            } catch(IOException ioe){
               System.out.println(ioe.getMessage());
            }
        } else{
            System.out.println("File not found!");
        }







    }

}
