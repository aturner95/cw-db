package edu.uob.dbfilesystem;

import edu.uob.dbelements.Attribute;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Record;
import edu.uob.dbelements.Table;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DBTableFile {

    public Table readDBFileIntoEntity(String dbFilePath) throws IOException {

        File fileToOpen = new File(dbFilePath.toLowerCase(Locale.ROOT));
        String tableName = getTableName(fileToOpen);

        if(fileToOpen.exists()){
            Table table;

            FileReader reader = new FileReader(fileToOpen);
            try(BufferedReader br = new BufferedReader(reader)){
                table = new Table(tableName, fileToOpen);

                String line;

                if((line = br.readLine()) != null) {
                    if (!readColumnHeadingsIntoEntity(table, line)) {
                        //TODO handle error
                    }
                }
                while((line = br.readLine()) != null){
                    if(!readRecordIntoEntity(table, line)){
                        // TODO handle error
                    }
                }

                return table;
            }
        }
        throw new IOException();
    }

    /* default */ boolean readColumnHeadingsIntoEntity(Table table, String header) {

        if (table != null && header != null && header.length() > 0) {
            String[] tabDelimitedCols = header.split("\t");
            List<ColumnHeader> columns = new ArrayList<>();
            int colCounter = 0;

            for (String colAsString : tabDelimitedCols) {
                if (!colAsString.isEmpty()) {
                    colAsString = colAsString.trim();
                    ColumnHeader tableCol = new ColumnHeader();
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

    /* default */ boolean readRecordIntoEntity(Table table, String row) {

        if (table != null && row != null && row.length() > 0) {
            String[] tabDelimitedRow = row.split("\t");
            Record record = new Record();
            List<Attribute> listOfAttributes = new ArrayList<>();

            record.setId(Long.valueOf(tabDelimitedRow[0]));

            for(int i = 1; i < tabDelimitedRow.length; i++){
                Attribute attr = new Attribute();
                attr.setValue(tabDelimitedRow[i]);
                listOfAttributes.add(attr);
            }
            record.setAttributes(listOfAttributes);
            table.getRows().add(record);
            return true;
        }
        return false;
    }

    /* default */ String getTableName(File dbFilePath){
        String fileName;

        fileName = dbFilePath.getName();
        return fileName.substring(0, fileName.indexOf('.'));
    }

}
