package edu.uob.dbfilesystem;

import edu.uob.dbelements.Attribute;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Record;
import edu.uob.dbelements.Table;

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

            // record.setId(Long.valueOf(tabDelimitedRow[0]));

            for (String s : tabDelimitedRow) {
                Attribute attr = new Attribute();
                attr.setValue(s);
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

    // TODO currently this method simply creates and writes to a new file from scratch. If file exists should we just add new rows?
    public boolean storeEntityIntoDBFile(Table table, String dbFilePath){

        File fileToOpen = new File(dbFilePath.toLowerCase(Locale.ROOT));

        if(table != null) {

            if(!fileToOpen.exists()){
                try {
                    fileToOpen.createNewFile();
                } catch(IOException ioe){
                    System.out.println(ioe.getMessage());
                    return false;
                }
            }

            if(!storeColumnHeaderIntoDBFile(table.getColHeadings(), fileToOpen)){
                // TODO handle exception
                System.out.println("Exception storing col headers to DB file");
                return false;
            }

            for(Record rec: table.getRows()){
                if(!storeRecordIntoDBFile(rec, fileToOpen)){
                    // TODO handle exception
                    System.out.println("Exception storing row to DB file");
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    // @TODO can #storeColomnHeaderIntoDBFile and #storeRecordIntoDBFile be factorised to use common code?
    /* default */ boolean storeColumnHeaderIntoDBFile(List<ColumnHeader> colHeaders, File dbFile) {

        if(colHeaders != null && colHeaders.size() > 0) {

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(dbFile))) {
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < colHeaders.size(); i++) {
                    sb.append(colHeaders.get(i).getColName());
                    if (i < colHeaders.size() - 1) {
                        sb.append("\t");
                    }
                }

                bw.write(sb.toString());
                return true;

            } catch (IOException ioe) {
                return false;
            }
        }
        return false;
    }

    // @TODO can #storeColomnHeaderIntoDBFile and #storeRecordIntoDBFile be factorised to use common code?
    /* default */ boolean storeRecordIntoDBFile(Record record, File dbFile){

        if(record != null && record.getAttributes() != null) {

            if(record.getAttributes().size() == 0){
                return true;
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(dbFile, true))) {
                List<Attribute> attributes = record.getAttributes();
                StringBuilder sb = new StringBuilder();
                sb.append("\n");

                for (int i = 0; i < attributes.size(); i++) {
                    sb.append(attributes.get(i).getValue());
                    if (i < attributes.size() - 1) {
                        sb.append("\t");
                    }
                }

                bw.append(sb.toString());
                return true;

            } catch (IOException ioe) {
                return false;
            }
        } return false;
    }

}
