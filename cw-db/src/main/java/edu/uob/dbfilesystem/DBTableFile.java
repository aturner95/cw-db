package edu.uob.dbfilesystem;

import edu.uob.dbelements.Column;
import edu.uob.dbelements.Table;

import javax.lang.model.type.ArrayType;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DBTableFile {


    public void readInEntity(String tableName) throws IOException {

        String fileName = "db" + File.separator + tableName + ".tab";

        File fileToOpen = new File(fileName.toLowerCase(Locale.ROOT));
        if(fileToOpen.exists()){

            Table table;

            FileReader reader = new FileReader(fileToOpen);
            try(BufferedReader br = new BufferedReader(reader)){
                table = new Table(tableName, fileToOpen);

                String line;

                if((line = br.readLine()) != null){
                    if(!readColumnHeadingsIntoTable(table, line)){
                        //TODO do something
                    }
                }

                while((line = br.readLine()) != null){
                    // readRecordIntoTable(table, line);
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

//    /* default */ instanciateRow(String row){
//
//    }

    /* default */ boolean readColumnHeadingsIntoTable(Table table, String header) {

        // Thanks to: https://stackoverflow.com/questions/19575308/read-a-file-separated-by-tab-and-put-the-words-in-an-arraylist

        if (header != null && header.length() > 0) {
            String[] tabDelimitedCols = header.split("\t");
            List<Column> columns = new ArrayList<>();
            int colCounter = 0;

            for (String colAsString : tabDelimitedCols) {
                if (!colAsString.isEmpty()) {
                    colAsString = colAsString.trim();
                    Column tableCol = new Column();
                    tableCol.setColNumber(colCounter++);
                    tableCol.setColName(colAsString);
                    columns.add(tableCol);
                }
            }
            table.setColHeadings(columns);
            return true;
        }
        return false;
    }

}
