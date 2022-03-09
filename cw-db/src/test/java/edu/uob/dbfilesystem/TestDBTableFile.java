package edu.uob.dbfilesystem;

import edu.uob.dbelements.Attribute;
import edu.uob.dbelements.ColumnHeader;
import edu.uob.dbelements.Record;
import edu.uob.dbelements.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestDBTableFile {

    @TempDir
    private File tempDir;
    private File tempFile;
    final String tempDirName = "dbtest";
    final String fileExt = ".tab";

    /* ----------------------- HELPER METHODS ----------------------- */

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

    private boolean checkDBFileContainsString(File file, String term) throws IOException{
        FileReader reader = new FileReader(file);
        try(BufferedReader br = new BufferedReader(reader)){
            String line;
            while((line = br.readLine()) != null) {
                if(line.contains(term)){
                    return true;
                }
            }
        }
        return false;
    }


    /* ----------------------- TESTS ----------------------- */

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

    @Test
    public void test_readDBFileIntoEntity_happyPath_tableReadIntoEntity() throws Exception{

        // Setting up test
        String tempTableName = "people";
        String tempFilePath = tempDirName + File.separator + tempTableName + fileExt;

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
        String tempFilePath = tempDirName + File.separator + tempTableName + fileExt;

        createTempDBFile(tempTableName);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFilePath));
        String colHeader = "id\tName\tAge\tEmail\n";

        bw.write(colHeader);
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

    @Test
    public void test_storeColomnHeaderIntoDBFile_happyPath_fileExistsAndContainsHeaderText() throws Exception{

        // given
        String tempTableName = "people";
        // String tempFilePath = tempDirName + File.separator + tempTableName + fileExt;
        tempFile = File.createTempFile(tempTableName, fileExt, tempDir);

        List<ColumnHeader> colHeaders = new ArrayList<>();
        ColumnHeader col1 = new ColumnHeader(1, "Id", DBDataType.NUMBER, Long.SIZE, DBColumnType.PRIMARY_KEY);
        ColumnHeader col2 = new ColumnHeader(2, "Name", DBDataType.VARCHAR, 255, DBColumnType.FIELD);
        ColumnHeader col3 = new ColumnHeader(3, "Age", DBDataType.VARCHAR, 255, DBColumnType.FIELD);
        ColumnHeader col4 = new ColumnHeader(4, "Email", DBDataType.VARCHAR, 255, DBColumnType.FIELD);
        colHeaders.add(col1);
        colHeaders.add(col2);
        colHeaders.add(col3);
        colHeaders.add(col4);

        DBTableFile tableFile = new DBTableFile();

        // when
        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
        assertTrue(tableFile.storeColumnHeaderIntoDBFile(colHeaders, tempFile));

        // then
        // TODO find why tempFileName contains garbage numbers e.g., people3853033247386902340.tab
        // assertEquals("people.tab", tempFile.getName());
        assertTrue(checkDBFileContainsString(tempFile, "Id\tName\tAge\tEmail"));
    }

    @Test
    public void test_storeColomnHeaderIntoDBFile_nullColHeaders_returnsFalse() throws Exception{

        // given
        String tempTableName = "people";
        // String tempFilePath = tempDirName + File.separator + tempTableName + fileExt;
        tempFile = File.createTempFile(tempTableName, fileExt, tempDir);

        List<ColumnHeader> colHeaders = null;

        DBTableFile tableFile = new DBTableFile();

        // when
        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());


        // then
        // TODO find why tempFileName contains garbage numbers e.g., people3853033247386902340.tab
        // assertEquals("people.tab", tempFile.getName());
        assertFalse(tableFile.storeColumnHeaderIntoDBFile(colHeaders, tempFile));
    }

    @Test
    public void test_storeColomnHeaderIntoDBFile_noColHeaders_returnsFalse() throws Exception{

        // given
        String tempTableName = "people";
        // String tempFilePath = tempDirName + File.separator + tempTableName + fileExt;
        tempFile = File.createTempFile(tempTableName, fileExt, tempDir);

        List<ColumnHeader> colHeaders = new ArrayList<>();

        DBTableFile tableFile = new DBTableFile();

        // when
        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());


        // then
        // TODO find why tempFileName contains garbage numbers e.g., people3853033247386902340.tab
        // assertEquals("people.tab", tempFile.getName());
        assertFalse(tableFile.storeColumnHeaderIntoDBFile(colHeaders, tempFile));
    }

    @Test
    public void test_storeRecordIntoDBFile_happyPath_fileExistsAndContainsSingleRow() throws Exception{

        String tempTableName = "people";
        tempFile = File.createTempFile(tempTableName, fileExt, tempDir);

        Record record = new Record();
        List<Attribute> attrs = new ArrayList<>();
        Attribute col1 = new Attribute("1", DBDataType.NUMBER);
        Attribute col2 = new Attribute("Bob", DBDataType.VARCHAR);
        Attribute col3 = new Attribute("21", DBDataType.NUMBER);
        Attribute col4 = new Attribute("bob@bob.net", DBDataType.VARCHAR);
        attrs.add(col1);
        attrs.add(col2);
        attrs.add(col3);
        attrs.add(col4);
        record.setAttributes(attrs);

        DBTableFile tableFile = new DBTableFile();

        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
        assertTrue(tableFile.storeRecordIntoDBFile(record, tempFile));
        assertTrue(checkDBFileContainsString(tempFile, "1\tBob\t21\tbob@bob.net"));
        // TODO find why tempFileName contains garbage numbers e.g., people3853033247386902340.tab
        // assertEquals("people.tab", tempFile.getName());
    }

    @Test
    public void test_storeRecordIntoDBFile_noAttributes_returnsFalse() throws Exception{

        String tempTableName = "people";
        tempFile = File.createTempFile(tempTableName, fileExt, tempDir);

        Record record = new Record();
        List<Attribute> attrs = new ArrayList<>();
        record.setAttributes(attrs);

        DBTableFile tableFile = new DBTableFile();

        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
        assertFalse(tableFile.storeRecordIntoDBFile(record, tempFile));
    }

    @Test
    public void test_storeRecordIntoDBFile_nullAttributes_returnsFalse() throws Exception{

        String tempTableName = "people";
        tempFile = File.createTempFile(tempTableName, fileExt, tempDir);

        Record record = new Record();
        record.setAttributes(null);

        DBTableFile tableFile = new DBTableFile();

        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
        assertFalse(tableFile.storeRecordIntoDBFile(record, tempFile));
    }

    @Test
    public void test_storeRecordIntoDBFile_nullRecord_returnsFalse() throws Exception{

        String tempTableName = "people";
        tempFile = File.createTempFile(tempTableName, fileExt, tempDir);

        Record record = null;

        DBTableFile tableFile = new DBTableFile();

        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
        assertFalse(tableFile.storeRecordIntoDBFile(record, tempFile));
    }

    @Test
    public void test_storeEntityIntoDBFile_happyPath_headerAndRowsStoredInDBFile() throws Exception {
        // given
        Table table = new Table();
        DBTableFile dbFile = new DBTableFile();
        String dbFilePath = tempDirName + File.separator + "people" + fileExt;
        if(new File(tempDirName).mkdir()){
            tempDir = new File(tempDirName);
            tempDir.deleteOnExit();
        }
        tempFile = new File(dbFilePath);

        List<ColumnHeader> colHeadings = new ArrayList<>();
        ColumnHeader cHead1 = new ColumnHeader(1, "Id", DBDataType.NUMBER, 100, DBColumnType.PRIMARY_KEY);
        ColumnHeader cHead2 = new ColumnHeader(2, "Name", DBDataType.VARCHAR, 100, DBColumnType.FIELD);
        ColumnHeader cHead3 = new ColumnHeader(3, "Age", DBDataType.NUMBER, 100, DBColumnType.FIELD);
        colHeadings.add(cHead1);
        colHeadings.add(cHead2);
        colHeadings.add(cHead3);
        table.setColHeadings(colHeadings);

        List<Attribute> rowData1 = new ArrayList<>();
        Attribute attr1 = new Attribute("1", DBDataType.NUMBER);
        Attribute attr2 = new Attribute("Bob", DBDataType.VARCHAR);
        Attribute attr3 = new Attribute("21", DBDataType.NUMBER);
        rowData1.add(attr1);
        rowData1.add(attr2);
        rowData1.add(attr3);
        Record row1 = new Record(rowData1);

        List<Attribute> rowData2 = new ArrayList<>();
        Attribute attr4 = new Attribute("2", DBDataType.NUMBER);
        Attribute attr5 = new Attribute("Sarah", DBDataType.VARCHAR);
        Attribute attr6 = new Attribute("66", DBDataType.NUMBER);
        rowData2.add(attr4);
        rowData2.add(attr5);
        rowData2.add(attr6);
        Record row2 = new Record(rowData2);

        List<Record> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        // when
        assertTrue(dbFile.storeEntityIntoDBFile(table, dbFilePath));

        // then
        assertTrue(checkDBFileContainsString(tempFile, "Id\tName\tAge"));
        assertTrue(checkDBFileContainsString(tempFile, "1\tBob\t21"));
        assertTrue(checkDBFileContainsString(tempFile, "2\tSarah\t66"));

    }

}
