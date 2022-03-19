package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import edu.uob.dbelements.*;
import edu.uob.dbelements.Record;
import edu.uob.dbfilesystem.DBTableFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestDatabaseCommands {

    private DBServer server;
    private File tempDbDir;
    private static final String tempDbDirName = "dbtest";
    private static final String fileExt = ".tab";
    DBCmd cmd;

    @BeforeEach
    void setup(@TempDir File dbDir) {
        tempDbDir = new File(tempDbDirName);
        tempDbDir.deleteOnExit();
        server = new DBServer(dbDir);
    }

    @AfterEach
    void teardown(){
        if(tempDbDir.exists()){
            File[] files = tempDbDir.listFiles();
            if(files != null) {
                for (File file : files) {
                    if(file != null && file.exists()) {
                        assertTrue(file.delete());
                    }
                }
            }
            assertTrue(tempDbDir.delete());
        }
        cmd = null;
    }

    @Test
    public void test_useCMD_databaseExists_stringReturned() throws Exception {
        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new UseCMD();
        cmd.addTableName(tempDbDirName);

        // given
        Object resultString = cmd.query(server);

        // then
        assertNotNull(resultString);
        assertTrue(resultString instanceof String);
    }

    @Test
    public void test_useCMD_databaseDoesNotExists_nullReturned() throws Exception {
        // given
        setup(new File("someDirThatDoesNotExist"));
        cmd = new UseCMD();
        cmd.addTableName(tempDbDirName);

        // given
        Object resultString = cmd.query(server);

        // then
        assertNull(resultString);
    }

    @Test
    public void test_useCMD_noTableNamesInTree_nullReturned() throws Exception {
        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new UseCMD();
        cmd.addTableName(tempDbDirName);

        // given
        Object resultString = cmd.query(server);

        // then
        assertNotNull(resultString);
    }

    @Test
    public void test_createCMD_databaseCreated_emptyStringReturned() throws Exception {
        // given
        String newTempDb = "newTempDb";

        cmd = new CreateCMD();
        cmd.setDatabaseName("newTempDb");

        // given
        Object resultString = cmd.query(server);

        // then
        assertNotNull(resultString);
        assertTrue(resultString instanceof String);
        tempDbDir = new File(newTempDb);
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
    }

    @Test
    public void test_createCMD_tableCreated_emptyStringReturned() throws Exception {

        // given
        setup(tempDbDir);
        Files.createDirectory(tempDbDir.toPath());
        String newTempTable = "newTempTable";

        cmd = new CreateCMD();
        cmd.addTableName(newTempTable);

        // when
        Object resultString = cmd.query(server);

        // then
        assertNotNull(resultString);
        assertTrue(resultString instanceof String);
        File tempFile = new File("dbtest" + File.separator + newTempTable + ".tab");
        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
    }

    @Test
    public void test_dropCMD_databaseDropped_emptyStringReturned() throws Exception {

        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new DropCMD();
        cmd.setDatabaseName(tempDbDirName);

        // given
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        String resultString = cmd.query(server);

        // then
        assertNotNull(resultString);
        assertTrue(resultString instanceof String);
        assertTrue(!tempDbDir.exists());
    }

    @Test
    public void test_dropCMD_tableDropped_emptyStringReturned() throws Exception {

        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new DropCMD();
        cmd.setDatabaseName(tempDbDirName);
        File tempDBFile = new File(tempDbDirName + File.separator + "table.tab");
        assertTrue(tempDBFile.createNewFile());

        // when
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());
        Object resultString = cmd.query(server);

        // then
        assertNotNull(resultString);
        assertTrue(resultString instanceof String);
        assertTrue(!tempDBFile.exists());
    }

    @Test
    public void test_dropCMD_databaseDoesNotExist_nullReturned() throws Exception {
        // given
        cmd = new DropCMD();
        cmd.setDatabaseName("doesNotExist");
        File tempDBFile = new File("doesNotExist" + File.separator + "table.tab");

        // when
        assertFalse(tempDBFile.exists());
        Object resultString = cmd.query(server);

        // then
        assertNull(resultString);
        assertFalse(tempDBFile.exists());
    }

    @Test
    public void test_dropCMD_tableDoesNotExist_nullReturned() throws Exception {

        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new DropCMD();
        cmd.addTableName(tempDbDirName);
        File tempDBFile = new File(tempDbDirName + File.separator + "table.tab");

        // when
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        assertFalse(tempDBFile.exists());

        Object resultString = cmd.query(server);

        // then
        assertNull(resultString);
    }

    @Test
    public void test_alterCMD_addedColumnFour_headersAndRowsHaveSizeFour() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "people";
        String attrName = "newCol";
        String fullPath = tempDbDirName + File.separator + tableName + fileExt;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new AlterCMD("ADD");
        cmd.addTableName(tableName);
        cmd.addColumnName(attrName);

        // set up table and header
        Table table = new Table();
        TableHeader header = new TableHeader();
        header.setFileLocation(tempDBFile);
        header.setTableName(tableName);
        table.setHeader(header);

        // set up column headers
        List<ColumnHeader> colHeaders = new ArrayList<>();
        colHeaders.add(new ColumnHeader("id"));
        colHeaders.add(new ColumnHeader("name"));
        colHeaders.add(new ColumnHeader("email"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("email@email.com"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("email2@email.com"));
        Record row2 = new Record(attr2);
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String result = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);
        // ------------- then -------------
        assertTrue(result instanceof String);
        assertEquals(4, table.getColHeadings().size());
        assertEquals(4, table.getRows().get(0).getAttributes().size());
        assertEquals(4, table.getRows().get(1).getAttributes().size());
    }

    @Test
    public void test_alterCMD_dropColumnThree_tableHasTwoCols() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "people";
        String attrName = "email";
        String fullPath = tempDbDirName + File.separator + tableName + fileExt;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new AlterCMD("DROP");
        cmd.addTableName(tableName);
        cmd.addColumnName(attrName);

        // set up table and header
        Table table = new Table();
        TableHeader header = new TableHeader();
        header.setFileLocation(tempDBFile);
        header.setTableName(tableName);
        table.setHeader(header);

        // set up column headers
        List<ColumnHeader> colHeaders = new ArrayList<>();
        colHeaders.add(new ColumnHeader("id"));
        colHeaders.add(new ColumnHeader("name"));
        colHeaders.add(new ColumnHeader("email"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("email@email.com"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("email2@email.com"));
        Record row2 = new Record(attr2);
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String result = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);
        // ------------- then -------------
        assertTrue(result instanceof String);
        assertEquals(2, table.getColHeadings().size());
        assertEquals("id", table.getColHeadings().get(0).getColName());
        assertEquals("name", table.getColHeadings().get(1).getColName());
        assertEquals(2, table.getRows().get(0).getAttributes().size());
        assertEquals("1", table.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("Anna", table.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals(2, table.getRows().get(1).getAttributes().size());
        assertEquals("2", table.getRows().get(1).getAttributes().get(0).getValue());
        assertEquals("Bob", table.getRows().get(1).getAttributes().get(1).getValue());
    }


    @Test
    public void test_alterCMD_dropColumn_tableDoesNotExist_nullReturned() throws Exception {

        // given
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "people";
        String attrName = "email";
        String fullPath = tempDbDirName + File.separator + tableName + fileExt;
        File tempDBFile = new File(fullPath);
        assertFalse(tempDBFile.exists());

        cmd = new AlterCMD("DROP");
        cmd.addTableName(tableName);
        cmd.addColumnName(attrName);

        // when
        String result = cmd.query(server);

        // then
        assertNull(result);
    }

    @Test
    public void test_alterCMD_addAttribute_attributeAlreadyExists_tableUnchanged() throws Exception {
        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "people";
        String attrName = "email";
        String fullPath = tempDbDirName + File.separator + tableName + fileExt;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new AlterCMD("ADD");
        cmd.addTableName(tableName);
        cmd.addColumnName(attrName);

        // set up table and header
        Table table = new Table();
        TableHeader header = new TableHeader();
        header.setFileLocation(tempDBFile);
        header.setTableName(tableName);
        table.setHeader(header);

        // set up column headers
        List<ColumnHeader> colHeaders = new ArrayList<>();
        colHeaders.add(new ColumnHeader("id"));
        colHeaders.add(new ColumnHeader("name"));
        colHeaders.add(new ColumnHeader("email"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("anna@email.com"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("bob@email.com"));
        Record row2 = new Record(attr2);
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String result = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);
        // ------------- then -------------
       //  assertNull(result);
        assertEquals(3, table.getColHeadings().size());
        assertEquals("id", table.getColHeadings().get(0).getColName());
        assertEquals("name", table.getColHeadings().get(1).getColName());
        assertEquals("email", table.getColHeadings().get(2).getColName());
        assertEquals(3, table.getRows().get(0).getAttributes().size());
        assertEquals("1", table.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("Anna", table.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("anna@email.com", table.getRows().get(0).getAttributes().get(2).getValue());
        assertEquals(3, table.getRows().get(1).getAttributes().size());
        assertEquals("2", table.getRows().get(1).getAttributes().get(0).getValue());
        assertEquals("Bob", table.getRows().get(1).getAttributes().get(1).getValue());
        assertEquals("bob@email.com", table.getRows().get(1).getAttributes().get(2).getValue());
    }

    @Test
    public void test_alterCMD_dropColumn_columnDoesNotExist_tableUnchanged() throws Exception {
        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "people";
        String attrName = "doesNotExist";
        String fullPath = tempDbDirName + File.separator + tableName + fileExt;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new AlterCMD("DROP");
        cmd.addTableName(tableName);
        cmd.addColumnName(attrName);

        // set up table and header
        Table table = new Table();
        TableHeader header = new TableHeader();
        header.setFileLocation(tempDBFile);
        header.setTableName(tableName);
        table.setHeader(header);

        // set up column headers
        List<ColumnHeader> colHeaders = new ArrayList<>();
        colHeaders.add(new ColumnHeader("id"));
        colHeaders.add(new ColumnHeader("name"));
        colHeaders.add(new ColumnHeader("email"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("anna@email.com"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("bob@email.com"));
        Record row2 = new Record(attr2);
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String result = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);
        // ------------- then -------------
        // assertNull(result);
        assertEquals(3, table.getColHeadings().size());
        assertEquals("id", table.getColHeadings().get(0).getColName());
        assertEquals("name", table.getColHeadings().get(1).getColName());
        assertEquals("email", table.getColHeadings().get(2).getColName());
        assertEquals(3, table.getRows().get(0).getAttributes().size());
        assertEquals("1", table.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("Anna", table.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("anna@email.com", table.getRows().get(0).getAttributes().get(2).getValue());
        assertEquals(3, table.getRows().get(1).getAttributes().size());
        assertEquals("2", table.getRows().get(1).getAttributes().get(0).getValue());
        assertEquals("Bob", table.getRows().get(1).getAttributes().get(1).getValue());
        assertEquals("bob@email.com", table.getRows().get(1).getAttributes().get(2).getValue());
    }
}
