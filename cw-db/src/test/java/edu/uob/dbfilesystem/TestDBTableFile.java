package edu.uob.dbfilesystem;

import edu.uob.dbelements.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import static org.junit.jupiter.api.Assertions.*;

public class TestDBTableFile {

    @TempDir
    private File tempFolder;

    @Test
    public void test_readColumnHeadingsIntoEntity_happyPath_tableHeaderPopulated(){
        String header = "id\tName\tAge\tEmail";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertTrue(tableFile.readColumnHeadingsIntoEntity(table, header));

        assertNotNull(table.getColHeadings());
        assertEquals(4, table.getColHeadings().size());
        assertEquals("id", table.getColHeadings().get(0).getColName());
        assertEquals("Name", table.getColHeadings().get(1).getColName());
        assertEquals("Age", table.getColHeadings().get(2).getColName());
        assertEquals("Email", table.getColHeadings().get(3).getColName());

    }

    @Test
    public void test_readColumnHeadingsIntoEntity_spacesInHeader_tableHeaderPopulated(){
        String header = "id\tName \tAge\t Email";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertTrue(tableFile.readColumnHeadingsIntoEntity(table, header));

        assertNotNull(table.getColHeadings());
        assertEquals(4, table.getColHeadings().size());
        assertEquals("id", table.getColHeadings().get(0).getColName());
        assertEquals("Name", table.getColHeadings().get(1).getColName());
        assertEquals("Age", table.getColHeadings().get(2).getColName());
        assertEquals("Email", table.getColHeadings().get(3).getColName());

    }

     /**
     Don't believe it should be this function's responsibility to check colHeadings have
     any spaces in them
     */
    @Test
    public void test_readColumnHeadingsIntoEntity_spaceDelimited_singleColHeader(){
        String header = "id Name Age Email";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertTrue(tableFile.readColumnHeadingsIntoEntity(table, header));

        assertNotNull(table.getColHeadings());
        assertEquals(1, table.getColHeadings().size());
        assertEquals("id Name Age Email", table.getColHeadings().get(0).getColName());

    }


    @Test
    public void test_readColumnHeadingsIntoEntity_emptyLine_methodFalse(){
        String header = "";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertFalse(tableFile.readColumnHeadingsIntoEntity(table, header));
        assertNull(table.getColHeadings());

    }

    @Test
    public void test_readColumnHeadingsIntoEntity_nullString_methodFalse(){
        String header = null;
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertFalse(tableFile.readColumnHeadingsIntoEntity(table, header));
        assertNull(table.getColHeadings());
    }

    @Test
    public void test_readColumnHeadingsIntoEntity_nullTable_methodFalse(){
        String header = "id Name Age Email";
        DBTableFile tableFile = new DBTableFile();
        Table table = null;

        assertFalse(tableFile.readColumnHeadingsIntoEntity(table, header));
    }

    @Test
    public void test_readRecordIntoEntity_happyPath_rowAdded(){
        String row = "139	Dorchester	1800	3";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertTrue(tableFile.readRecordIntoEntity(table, row));
        assertEquals(1, table.getRows().size());
        assertEquals(139, table.getRows().get(0).getId());
        assertEquals(4, table.getRows().get(0).getAttributes().size());
        assertEquals("139", table.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("Dorchester", table.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("1800", table.getRows().get(0).getAttributes().get(2).getValue());
        assertEquals("3", table.getRows().get(0).getAttributes().get(3).getValue());
    }

    @Test
    public void test_readRecordIntoEntity_emptyRow_returnFalse(){
        String row = "";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertFalse(tableFile.readRecordIntoEntity(table, row));
    }

    @Test
    public void test_readRecordIntoEntity_rowNull_returnFalse(){
        String row = null;
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertFalse(tableFile.readRecordIntoEntity(table, row));
    }

    @Test
    public void test_readRecordIntoEntity_tableNull_returnFalse(){
        String row = "139	Dorchester	1800	3";
        DBTableFile tableFile = new DBTableFile();
        Table table = null;

        assertFalse(tableFile.readRecordIntoEntity(table, row));
    }

    @Test
    public void test_readRecordIntoEntity_twice_tableContainsTwoRows(){
        String row1 = "139	Dorchester	1800	3";
        String row2 = "9384	Plazza	3000	123";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertTrue(tableFile.readRecordIntoEntity(table, row1));
        assertTrue(tableFile.readRecordIntoEntity(table, row2));
        assertEquals(2, table.getRows().size());
        assertEquals(4, table.getRows().get(0).getAttributes().size());
        assertEquals(4, table.getRows().get(1).getAttributes().size());
    }

//    @Test
//    public void test_readDBFileIntoEntity_happyPath_entityCreated() throws Exception{
//
//        String testDb = "testdb";
//        tempFolder = new File(testDb);
//        tempFolder.mkdir();
//
//        assertTrue(tempFolder.exists());
//        assertTrue(tempFolder.isDirectory());
//
//
//        String testFileName = testDb + File.separator + "person.tab";
//        File tempDbFile = new File(testFileName);
//        if(!tempDbFile.createNewFile()){
//            throw new Exception();
//        }
//
//        assertTrue(tempDbFile.exists());
//
//
//        BufferedWriter bw = new BufferedWriter(new FileWriter(testFileName));
//        StringBuilder sb = new StringBuilder();
//        sb.append("id\tName\tHeight\tPurchaserID");
//        sb.append("1\tDorchester\t1800\t3");
//        sb.append("2\tPlaza\t1200\t1");
//        sb.append("3\tExcelsior\t1000\t2");
//        bw.write(sb.toString());
//        bw.close();
//
//        DBTableFile tableFile = new DBTableFile();
//        Table table = tableFile.readDBFileIntoEntity(testFileName);
//
//        assertNotNull(table);
//        assertNotNull(table.getHeader());
//        assertEquals(testDb, table.getHeader().getTableName());
//        assertEquals(testFileName, table.getHeader().getFileLocation());
//        assertEquals(4, table.getHeader().getNextId());
//
//        assertNotNull(table.getColHeadings());
//        assertNotNull(table.getRows());
//
//    }

}
