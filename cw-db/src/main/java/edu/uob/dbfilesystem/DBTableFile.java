package edu.uob.dbfilesystem;

import edu.uob.dbelements.abstractelements.AbstractColumnData;
import edu.uob.dbelements.Attribute;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Record;
import edu.uob.dbelements.Table;
import edu.uob.exceptions.DBException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DBTableFile {

    public Table readDBFileIntoEntity(String dbFilePath) throws IOException, DBException {

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
                        throw new DBException("Unable to read column headers from DB file");
                    }
                }
                while((line = br.readLine()) != null){
                    if(!readRecordIntoEntity(table, line)){
                        throw new DBException("Unable to read rows from DB file");
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
            // int colCounter = 0;

            for (String colAsString : tabDelimitedCols) {
                if (!colAsString.isEmpty()) {
                    colAsString = colAsString.trim();
                    ColumnHeader tableCol = new ColumnHeader(colAsString);
                    // tableCol.setColNumber(colCounter++);
                    // tableCol.setColName();
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

            for (String s : tabDelimitedRow) {
                Attribute attr = new Attribute(s);
                // attr.setValue(s);
                listOfAttributes.add(attr);
            }
            record.setAttributes(listOfAttributes);
            table.getRows().add(record);
            return true;
        }
        return false;
    }

    /* default */ String getTableName(File dbFilePath){

        if(dbFilePath != null) {
            String fileName = dbFilePath.getName();
            if (fileName.contains(".")) {
                return fileName.substring(0, fileName.indexOf('.'));
            } else {
                return dbFilePath.getName();
            }
        }
        return null;
    }

    // TODO currently this method simply creates and writes to a new file from scratch. If file exists should we just add new rows?
    public boolean storeEntityIntoDBFile(Table table) throws DBException {

        if(table != null && table.getHeader() != null && table.getHeader().getFileLocation()!= null) {
            String dbFilePath = table.getHeader().getFileLocation().getPath();
            File fileToOpen = new File(dbFilePath.toLowerCase(Locale.ROOT));

            if (!fileToOpen.exists()) {
                if(!createDBFile(fileToOpen)){
                    throw new DBException("Unable to read file: " + fileToOpen.getName());
                }
            }

            if (!storeColumnHeaderIntoDBFile(table.getColHeadings(), fileToOpen)) {
                throw new DBException("Unable to store column headers in DB file");
            }

            for (Record rec : table.getRows()) {
                if (!storeRecordIntoDBFile(rec.getAttributes(), fileToOpen)) {
                    throw new DBException("Unable to store rows in DB file");
                }
            }
            return true;
        }
        throw new DBException();
    }

    /* default */ boolean createDBFile(File dbFile){

        if (dbFile != null) {
            try {
                if(!dbFile.createNewFile()){
                    return false;
                }
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    /* default */ boolean storeColumnHeaderIntoDBFile(List<ColumnHeader> colHeaders, File dbFile) {

        if(colHeaders != null && colHeaders.size() > 0) {
            return storeDataIntoDBFile(colHeaders, DBRowType.COLUMN_HEADER, dbFile);
        }
        return false;
    }

    /* default */ boolean storeRecordIntoDBFile(List<Attribute> record, File dbFile){

        if(record != null) {
            if(record.size() == 0){
                return true;
            }
            return storeDataIntoDBFile(record, DBRowType.RECORD, dbFile);
        }
        return false;
    }

    private boolean storeDataIntoDBFile(List<? extends AbstractColumnData> rowData, DBRowType rowType, File dbFile) {

        boolean appendMode;
        boolean newLine;

        if (rowType == DBRowType.COLUMN_HEADER){
            appendMode = false;
            newLine = false;
        } else if(rowType == DBRowType.RECORD){
            appendMode = true;
            newLine = true;
        } else {
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dbFile, appendMode))) {
            StringBuilder sb = new StringBuilder();
            if(newLine){
                sb.append(System.lineSeparator());
            }

            for (int i = 0; i < rowData.size(); i++) {
                sb.append(rowData.get(i).getData());
                if (i < rowData.size() - 1) {
                    sb.append("\t");
                }
            }
            bw.append(sb.toString());
            return true;

        } catch (IOException ioe) {
            return false;
        }
    }

    public boolean containsDbTable(String databaseName, String tableName) throws Exception {
        File fileToOpen = new File("databases" + File.separator + "databases.data");

        FileReader reader = new FileReader(fileToOpen);
        try(BufferedReader br = new BufferedReader(reader)){
            String line;

            while((line = br.readLine()) != null){
                String dbFile = databaseName + ":" + tableName;
                if(line.toUpperCase().contains(dbFile.toUpperCase())){
                    return true;
                }
            }

            return false;
        } catch(FileNotFoundException fnf){

            throw new DBException("Unable to find databases.data metadata file");
        }
    }

    public boolean addTableToMetadata(String databaseName, String tableName) throws Exception {
        return (addTableToMetadata(databaseName, tableName, 1));
    }

    public boolean addTableToMetadata(String databaseName, String tableName, int sequence) throws Exception {

        File dbFile = new File("databases" + File.separator + "databases.data");
        boolean appendMode = true;

        if(containsDbTable(databaseName, tableName)) {
            throw new DBException.DBTableExistsException(tableName);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(dbFile, appendMode))) {

            bw.append(System.lineSeparator() + databaseName + ":" + tableName + ":" + sequence + System.lineSeparator());
            return true;

        } catch (IOException ioe) {
            return false;
        }
    }

    public boolean removeTableFromMetadata(String databaseName, String tableName) throws FileNotFoundException{

        File file = new File("databases" + File.separator + "databases.data");
        FileReader reader = new FileReader(file);
        StringBuilder rewrite = null;

        try(BufferedReader br = new BufferedReader(reader)){

            rewrite = new StringBuilder();
            String line;

            while((line = br.readLine()) != null){
                String dbFile = databaseName + ":" + tableName;
                if(!line.toLowerCase(Locale.ROOT).contains(dbFile.toLowerCase(Locale.ROOT))) {
                    rewrite.append(line);
                }
            }

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {

            bw.write(rewrite.toString().toLowerCase(Locale.ROOT));
            return true;

        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns current sequence and increments
     * @param databaseName
     * @param tableName
     * @return
     * @throws FileNotFoundException
     */
    public int nextSeq(String databaseName, String tableName) throws Exception{

        File fileToOpen = new File("databases" + File.separator + "databases.data");
        FileReader reader = new FileReader(fileToOpen);

        try(BufferedReader br = new BufferedReader(reader)){

            String line;

            while((line = br.readLine()) != null){
                String dbFile = databaseName + ":" + tableName;
                if(line.toLowerCase(Locale.ROOT).contains(dbFile.toLowerCase(Locale.ROOT))) {
                    String [] split = line.split(":");
                    // db:table:seq
                    int indexOfSeq = 2;
                    int currentSeq = Integer.valueOf(split[indexOfSeq]).intValue();
                    removeTableFromMetadata(databaseName, tableName);
                    addTableToMetadata(databaseName,tableName, currentSeq+1);

                    return currentSeq;
                }
            }
            throw new IOException("seq attribute not found!");
        }
    }


}
