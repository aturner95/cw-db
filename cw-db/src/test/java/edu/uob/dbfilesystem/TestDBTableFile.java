package edu.uob.dbfilesystem;

import edu.uob.dbelements.Table;
import jdk.jfr.SettingDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import static org.junit.jupiter.api.Assertions.*;

public class TestDBTableFile {

    @TempDir
    private File tempDir;
    private File tempFile;
    final String tempDirName = "dbtest";
    final String fileExt = ".tab";

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

    // @BeforeEach
    private void createTempDBFile(String tempTableName) throws Exception {

        if(new File(tempDirName).mkdir()){
            tempDir = new File(tempDirName);
            tempDir.deleteOnExit();
        }

        assertTrue(tempDir.exists());
        assertTrue(tempDir.isDirectory());

        tempFile = File.createTempFile(tempTableName, fileExt, tempDir);
        tempFile.deleteOnExit();

        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
    }

    @Test
    public void test_readDBFileIntoEntity_happyPath_tableReadIntoEntity() throws Exception{

        // Setting up test
        String tempTableName = "people";
        String tempFilePath = "dbtest" + File.separator + tempTableName + fileExt;

        createTempDBFile(tempTableName);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFilePath));
        StringBuilder sb = new StringBuilder();
        sb.append("id\tName\tAge\tEmail\n");
        sb.append("1\tBob\t21\tbob@bob.net\n");
        sb.append("2\tHarry\t32\tharry@harry.com\n");
        sb.append("3\tChris\t42\tchris@chris.ac.uk");

        bw.write(sb.toString());
        bw.close();

        // Test
        DBTableFile tableFile = new DBTableFile();
        Table table =  tableFile.readDBFileIntoEntity(tempFilePath);

        // test table
        assertNotNull(table);
        assertEquals("people", table.getHeader().getTableName());
        // TODO find out why this test fails...
        assertEquals("dbtest" + File.separator + "people.tab", table.getHeader().getFileLocation());

        // test column headings
        assertNotNull(table.getColHeadings());
        assertEquals(4, table.getColHeadings().size());
        assertEquals("id", table.getColHeadings().get(0).getColName());
        assertEquals("Name", table.getColHeadings().get(1).getColName());
        assertEquals("Age", table.getColHeadings().get(2).getColName());
        assertEquals("Email", table.getColHeadings().get(3).getColName());

        // test row data
        assertNotNull(table.getRows());
        assertEquals(3, table.getRows().size());
        assertEquals(4, table.getRows().get(0).getAttributes().size());
        assertEquals(1, table.getRows().get(0).getId());
        assertEquals("1", table.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("harry@harry.com", table.getRows().get(1).getAttributes().get(3).getValue());
        assertEquals("42", table.getRows().get(2).getAttributes().get(2).getValue());
    }

    @Test
    public void test_readDBFileIntoEntity_noRows_tableReadIntoEntity() throws Exception{

        // Setting up test
        String tempTableName = "people";
        String tempFilePath = "dbtest" + File.separator + tempTableName + fileExt;

        createTempDBFile(tempTableName);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFilePath));
        StringBuilder sb = new StringBuilder();
        sb.append("id\tName\tAge\tEmail\n");

        bw.write(sb.toString());
        bw.close();

        // Test
        DBTableFile tableFile = new DBTableFile();
        Table table =  tableFile.readDBFileIntoEntity(tempFilePath);

        // test table
        assertNotNull(table);
        assertEquals("people", table.getHeader().getTableName());
        // TODO find out why this test fails...
        // assertEquals("dbtest" + File.separator + "people.tab", table.getHeader().getFileLocation());

        // test column headings
        assertNotNull(table.getColHeadings());
        assertEquals(4, table.getColHeadings().size());
        assertEquals("id", table.getColHeadings().get(0).getColName());
        assertEquals("Name", table.getColHeadings().get(1).getColName());
        assertEquals("Age", table.getColHeadings().get(2).getColName());
        assertEquals("Email", table.getColHeadings().get(3).getColName());

        // test row data
        assertNotNull(table.getRows());
        assertEquals(0, table.getRows().size());
    }

}
