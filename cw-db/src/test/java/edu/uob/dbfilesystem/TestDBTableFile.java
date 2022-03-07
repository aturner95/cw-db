package edu.uob.dbfilesystem;

import edu.uob.dbelements.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestDBTableFile {

    @Test
    public void test_readColumnHeadingsIntoTable_happyPath_tableHeaderPopulated(){
        String header = "id\tName\tAge\tEmail";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertTrue(tableFile.readColumnHeadingsIntoTable(table, header));

        assertNotNull(table.getColHeadings());
        assertEquals(4, table.getColHeadings().size());
        assertEquals("id", table.getColHeadings().get(0).getColName());
        assertEquals("Name", table.getColHeadings().get(1).getColName());
        assertEquals("Age", table.getColHeadings().get(2).getColName());
        assertEquals("Email", table.getColHeadings().get(3).getColName());

    }

    @Test
    public void test_readColumnHeadingsIntoTable_spacesInHeader_tableHeaderPopulated(){
        String header = "id\tName \tAge\t Email";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertTrue(tableFile.readColumnHeadingsIntoTable(table, header));

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
    public void test_readColumnHeadingsIntoTable_spaceDelimited_singleColHeader(){
        String header = "id Name Age Email";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertTrue(tableFile.readColumnHeadingsIntoTable(table, header));

        assertNotNull(table.getColHeadings());
        assertEquals(1, table.getColHeadings().size());
        assertEquals("id Name Age Email", table.getColHeadings().get(0).getColName());

    }


    @Test
    public void test_readColumnHeadingsIntoTable_emptyLine_methodFalse(){
        String header = "";
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertFalse(tableFile.readColumnHeadingsIntoTable(table, header));
        assertNull(table.getColHeadings());

    }

    @Test
    public void test_readColumnHeadingsIntoTable_null_methodFalse(){
        String header = null;
        DBTableFile tableFile = new DBTableFile();
        Table table = new Table();

        assertFalse(tableFile.readColumnHeadingsIntoTable(table, header));
        assertNull(table.getColHeadings());

    }

}
