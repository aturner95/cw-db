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

public class TestDatabaseCommands {

    private DBServer server;
    private File tempDbDir;
    private static final String tempDbDirName = "dbtest";
    private static final String fileExt = ".tab";
    DBCmd cmd;

    public static final String STATUS_OK = "[OK]";
    public static final String STATUS_ERROR = "[ERROR]";

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
    public void test_useCMD_databaseExists_statusCodeOK() throws Exception {
        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new UseCMD();
        cmd.setDatabaseName(tempDbDirName);

        // when
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_OK));
    }

    @Test
    public void test_useCMD_databaseDoesNotExists_statusCodeError() throws Exception {
        // given
        setup(new File("someDirThatDoesNotExist"));
        cmd = new UseCMD();
        cmd.addTableName(tempDbDirName);

        // when
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_ERROR));

    }

    @Test
    public void test_useCMD_noTableNamesInTree_statusCodeError() throws Exception {
        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new UseCMD();
        cmd.addTableName(tempDbDirName);

        // when
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_ERROR));
    }

    @Test
    public void test_createCMD_databaseCreated_statusCodeOK() throws Exception {
        // given
        String newTempDb = "newTempDb";

        cmd = new CreateCMD("DATABASE");
        cmd.setDatabaseName("newTempDb");

        // given
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_OK));
        tempDbDir = new File(newTempDb);
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
    }

    @Test
    public void test_createCMD_tableCreated_statusCodeOK() throws Exception {

        // given
        setup(tempDbDir);
        Files.createDirectory(tempDbDir.toPath());
        String newTempTable = "newTempTable";

        cmd = new CreateCMD("TABLE");
        cmd.addTableName(newTempTable);

        // given
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_OK));
        File tempFile = new File("dbtest" + File.separator + newTempTable + ".tab");
        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
    }

    @Test
    public void test_createCMD_tableCreatedWithColumnHeadings_statusCodeOK() throws Exception {

        // given
        setup(tempDbDir);
        Files.createDirectory(tempDbDir.toPath());
        String newTempTable = "marks";
        String fullPath = tempDbDirName + File.separator + newTempTable + fileExt;

        cmd = new CreateCMD("TABLE");
        cmd.addTableName(newTempTable);
        cmd.addColumnName("name");
        cmd.addColumnName("mark");
        cmd.addColumnName("pass");

        // given
        String resultMessage = cmd.query(server);
        DBTableFile dbFile = new DBTableFile();
        Table marks = dbFile.readDBFileIntoEntity(fullPath);

        // then
        File tempFile = new File(fullPath);
        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());
        assertTrue(resultMessage.contains(STATUS_OK));
        assertEquals(4, marks.getColHeadings().size());
        assertEquals("id", marks.getColHeadings().get(0).getColName());
        assertEquals("name", marks.getColHeadings().get(1).getColName());
        assertEquals("mark", marks.getColHeadings().get(2).getColName());
        assertEquals("pass", marks.getColHeadings().get(3).getColName());
    }

    @Test
    public void test_dropCMD_databaseDropped_statusCodeOK() throws Exception {

        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new DropCMD("DATABASE");
        cmd.setDatabaseName(tempDbDirName);

        // when
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_OK));
        assertFalse(tempDbDir.exists());
    }

    @Test
    public void test_dropCMD_tableDropped_statusCodeOK() throws Exception {

        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new DropCMD("TABLE");
        cmd.setDatabaseName(tempDbDirName);
        cmd.addTableName("table");
        File tempDBFile = new File(tempDbDirName + File.separator + "table.tab");
        assertTrue(tempDBFile.createNewFile());

        // given
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_OK));
        assertFalse(tempDBFile.exists());
    }

    @Test
    public void test_dropCMD_databaseDoesNotExist_statusCodeError() throws Exception {
        // given
        cmd = new DropCMD("DATABASE");
        cmd.setDatabaseName("doesNotExist");
        File tempDBFile = new File("doesNotExist" + File.separator + "table.tab");

        // when
        assertFalse(tempDBFile.exists());
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_ERROR));
        assertFalse(tempDBFile.exists());
    }

    @Test
    public void test_dropCMD_tableDoesNotExist_statusCodeERROR() throws Exception {

        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new DropCMD("TABLE");
        cmd.addTableName(tempDbDirName);
        // File tempDBFile = new File(tempDbDirName + File.separator + "table.tab");

        // when
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_ERROR));
    }

    @Test
    public void test_alterCMD_addedColumnFour_StatusCodeOK_headersAndRowsHaveSizeFour() throws Exception {

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
        String resultMessage = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);


        // ------------- then -------------
        assertTrue(resultMessage.contains(STATUS_OK));
        assertEquals(4, table.getColHeadings().size());
        assertEquals(4, table.getRows().get(0).getAttributes().size());
        assertEquals(4, table.getRows().get(1).getAttributes().size());
    }

    @Test
    public void test_alterCMD_dropColumnThree_statusCodeOK_tableHasTwoCols() throws Exception {

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
        String resultMessage = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);


        // ------------- then -------------
        assertTrue(resultMessage.contains(STATUS_OK));
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
    public void test_alterCMD_dropColumn_tableDoesNotExist_statusCodeError() throws Exception {

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
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_ERROR));
    }

    @Test
    public void test_alterCMD_addAttribute_attributeAlreadyExists_errorStatusError_tableUnchanged() throws Exception {
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
        String resultMessage = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);


        // ------------- then -------------
        assertTrue(resultMessage.contains(STATUS_ERROR));
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
    public void test_alterCMD_dropColumn_columnDoesNotExist_statusCodeError_tableUnchanged() throws Exception {
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
        String resultMessage = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);

        // ------------- then -------------
        assertTrue(resultMessage.contains(STATUS_ERROR));
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
    public void test_insertCMD_insertedNewEntity_statusCodeOK_tableContainsNewRecord() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "Country";
        String fullPath = tempDbDirName + File.separator + tableName + fileExt;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new InsertCMD();
        cmd.addTableName(tableName);
        // cmd.addVariable("3"); - this is automatically generated by the server
        cmd.addVariable("France");
        cmd.addVariable("Paris");


        // set up table and header
        Table table = new Table();
        TableHeader header = new TableHeader();
        header.setFileLocation(tempDBFile);
        header.setTableName(tableName);
        table.setHeader(header);

        // set up column headers
        List<ColumnHeader> colHeaders = new ArrayList<>();
        colHeaders.add(new ColumnHeader("id"));
        colHeaders.add(new ColumnHeader("Name"));
        colHeaders.add(new ColumnHeader("Capital"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Germany"));
        attr1.add(new Attribute("Berlin"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Spain"));
        attr2.add(new Attribute("Madrid"));
        Record row2 = new Record(attr2);
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String resultMessage = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);

        // ------------- then -------------
        assertTrue(resultMessage.contains(STATUS_OK));
        assertEquals(3, table.getColHeadings().size());
        assertEquals("id", table.getColHeadings().get(0).getColName());
        assertEquals("Name", table.getColHeadings().get(1).getColName());
        assertEquals("Capital", table.getColHeadings().get(2).getColName());

        assertEquals(3, table.getRows().size());
        assertEquals(3, table.getRows().get(0).getAttributes().size());
        assertEquals("1", table.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("Germany", table.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("Berlin", table.getRows().get(0).getAttributes().get(2).getValue());
        assertEquals(3, table.getRows().get(1).getAttributes().size());
        assertEquals("2", table.getRows().get(1).getAttributes().get(0).getValue());
        assertEquals("Spain", table.getRows().get(1).getAttributes().get(1).getValue());
        assertEquals("Madrid", table.getRows().get(1).getAttributes().get(2).getValue());
        assertEquals(3, table.getRows().get(2).getAttributes().size());
        assertEquals("3", table.getRows().get(2).getAttributes().get(0).getValue());
        assertEquals("France", table.getRows().get(2).getAttributes().get(1).getValue());
        assertEquals("Paris", table.getRows().get(2).getAttributes().get(2).getValue());
    }


    @Test
    public void test_insertCMD_tooManyAttributes_statusCodeError_tableUnchanged() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "Country";
        String fullPath = tempDbDirName + File.separator + tableName + fileExt;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new InsertCMD();
        cmd.addTableName(tableName);
        cmd.addVariable("3"); // this should be automatically generated by the server
        cmd.addVariable("France");
        cmd.addVariable("Paris");


        // set up table and header
        Table table = new Table();
        TableHeader header = new TableHeader();
        header.setFileLocation(tempDBFile);
        header.setTableName(tableName);
        table.setHeader(header);

        // set up column headers
        List<ColumnHeader> colHeaders = new ArrayList<>();
        colHeaders.add(new ColumnHeader("id"));
        colHeaders.add(new ColumnHeader("Name"));
        colHeaders.add(new ColumnHeader("Capital"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Germany"));
        attr1.add(new Attribute("Berlin"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Spain"));
        attr2.add(new Attribute("Madrid"));
        Record row2 = new Record(attr2);
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String resultMessage = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);

        // ------------- then -------------
        assertTrue(resultMessage.contains(STATUS_ERROR));
        assertEquals(3, table.getColHeadings().size());
        assertEquals("id", table.getColHeadings().get(0).getColName());
        assertEquals("Name", table.getColHeadings().get(1).getColName());
        assertEquals("Capital", table.getColHeadings().get(2).getColName());

        assertEquals(2, table.getRows().size());
        assertEquals(3, table.getRows().get(0).getAttributes().size());
        assertEquals("1", table.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("Germany", table.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("Berlin", table.getRows().get(0).getAttributes().get(2).getValue());
        assertEquals(3, table.getRows().get(1).getAttributes().size());
        assertEquals("2", table.getRows().get(1).getAttributes().get(0).getValue());
        assertEquals("Spain", table.getRows().get(1).getAttributes().get(1).getValue());
        assertEquals("Madrid", table.getRows().get(1).getAttributes().get(2).getValue());
    }


    @Test
    public void test_insertCMD_entityAlreadyExists_statusCodeError_tableUnchanged() throws Exception{
        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "Country";
        String fullPath = tempDbDirName + File.separator + tableName + fileExt;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new InsertCMD();
        cmd.addTableName(tableName);
        cmd.addVariable("2");
        cmd.addVariable("France");
        cmd.addVariable("Paris");


        // set up table and header
        Table table = new Table();
        TableHeader header = new TableHeader();
        header.setFileLocation(tempDBFile);
        header.setTableName(tableName);
        table.setHeader(header);

        // set up column headers
        List<ColumnHeader> colHeaders = new ArrayList<>();
        colHeaders.add(new ColumnHeader("Id"));
        colHeaders.add(new ColumnHeader("Name"));
        colHeaders.add(new ColumnHeader("Capital"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Germany"));
        attr1.add(new Attribute("Berlin"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Spain"));
        attr2.add(new Attribute("Madrid"));
        Record row2 = new Record(attr2);
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String resultMessage = cmd.query(server);
        table = file.readDBFileIntoEntity(fullPath);


        // ------------- then -------------
        assertTrue(resultMessage.contains(STATUS_ERROR));
        assertEquals(3, table.getColHeadings().size());
        assertEquals("Id", table.getColHeadings().get(0).getColName());
        assertEquals("Name", table.getColHeadings().get(1).getColName());
        assertEquals("Capital", table.getColHeadings().get(2).getColName());

        assertEquals(2, table.getRows().size());
        assertEquals(3, table.getRows().get(0).getAttributes().size());
        assertEquals("1", table.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("Germany", table.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("Berlin", table.getRows().get(0).getAttributes().get(2).getValue());
        assertEquals(3, table.getRows().get(1).getAttributes().size());
        assertEquals("2", table.getRows().get(1).getAttributes().get(0).getValue());
        assertEquals("Spain", table.getRows().get(1).getAttributes().get(1).getValue());
        assertEquals("Madrid", table.getRows().get(1).getAttributes().get(2).getValue());
    }

    @Test
    public void test_insertCMD_tableDoesNotExist_statusCodeError() throws Exception{
        // given
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "doesNotExist";
        String fullPath = tempDbDirName + File.separator + tableName + fileExt;
        File tempDBFile = new File(fullPath);
        assertFalse(tempDBFile.exists());

        cmd = new InsertCMD();
        cmd.addTableName(tableName);
        cmd.addVariable("2");
        cmd.addVariable("France");
        cmd.addVariable("Paris");

        // when
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_ERROR));
    }

    @Test
    public void test_joinCmd_tablesJoins_statusCodeOK() throws Exception {
        // ------------- given -------------
        // create dir and files
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableNameA = "PERSON";
        String tableNameB = "PET";
        String attributeNameA = "ID";
        String attributeNameB = "OWNERID";
        String fullPathA = tempDbDirName + File.separator + tableNameA + fileExt;
        String fullPathB = tempDbDirName + File.separator + tableNameB + fileExt;
        File tempDBFileA = new File(fullPathA);
        assertTrue(tempDBFileA.createNewFile());
        assertTrue(tempDBFileA.exists());
        assertTrue(tempDBFileA.isFile());
        File tempDBFileB = new File(fullPathB);
        assertTrue(tempDBFileB.createNewFile());
        assertTrue(tempDBFileB.exists());
        assertTrue(tempDBFileB.isFile());

        // set up command
        cmd = new JoinCMD();
        cmd.addTableName(tableNameA);
        cmd.addTableName(tableNameB);
        cmd.addColumnName(attributeNameA);
        cmd.addColumnName(attributeNameB);


        // set up table and header
        Table tableA = new Table();
        TableHeader headerA = new TableHeader();
        headerA.setFileLocation(tempDBFileA);
        headerA.setTableName(tableNameA);
        tableA.setHeader(headerA);
        Table tableB = new Table();
        TableHeader headerB = new TableHeader();
        headerB.setFileLocation(tempDBFileB);
        headerB.setTableName(tableNameB);
        tableB.setHeader(headerB);

        // set up column headers
        List<ColumnHeader> colHeadersA = new ArrayList<>();
        colHeadersA.add(new ColumnHeader("ID"));
        colHeadersA.add(new ColumnHeader("NAME"));
        colHeadersA.add(new ColumnHeader("LOCATION"));
        tableA.setColHeadings(colHeadersA);
        List<ColumnHeader> colHeadersB = new ArrayList<>();
        colHeadersB.add(new ColumnHeader("ID"));
        colHeadersB.add(new ColumnHeader("NAME"));
        colHeadersB.add(new ColumnHeader("OWNERID"));
        tableB.setColHeadings(colHeadersB);

        // set up row data
        List<Record> rows1 = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("Swindon"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("Cardiff"));
        Record row2 = new Record(attr2);
        rows1.add(row1);
        rows1.add(row2);
        tableA.setRows(rows1);

        List<Record> rows2 = new ArrayList<>();
        List<Attribute> attr3 = new ArrayList<>();
        attr3.add(new Attribute("1"));
        attr3.add(new Attribute("Scratch"));
        attr3.add(new Attribute("1"));
        Record row3 = new Record(attr3);

        List<Attribute> attr4 = new ArrayList<>();
        attr4.add(new Attribute("2"));
        attr4.add(new Attribute("Fluffy"));
        attr4.add(new Attribute("2"));
        Record row4 = new Record(attr4);
        rows2.add(row3);
        rows2.add(row4);
        tableB.setRows(rows2);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(tableA);
        file.storeEntityIntoDBFile(tableB);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        assertTrue(resultMessage.contains(STATUS_OK));
    }

    @Test
    public void test_joinCmd_tableBDoesNotExist_statusCodeError() throws Exception{

        // ------------- given -------------
        // create dir and files
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableNameA = "PERSON";
        String tableNameB = "PET";
        String attributeNameA = "ID";
        String attributeNameB = "OWNERID";
        String fullPathA = tempDbDirName + File.separator + tableNameA + fileExt;
        File tempDBFileA = new File(fullPathA);
        assertTrue(tempDBFileA.createNewFile());
        assertTrue(tempDBFileA.exists());
        assertTrue(tempDBFileA.isFile());
        // table B does not exist!

        // set up command
        cmd = new JoinCMD();
        cmd.addTableName(tableNameA);
        cmd.addTableName(tableNameB);
        cmd.addColumnName(attributeNameA);
        cmd.addColumnName(attributeNameB);


        // set up table and header
        Table tableA = new Table();
        TableHeader headerA = new TableHeader();
        headerA.setFileLocation(tempDBFileA);
        headerA.setTableName(tableNameA);
        tableA.setHeader(headerA);

        // set up column headers
        List<ColumnHeader> colHeadersA = new ArrayList<>();
        colHeadersA.add(new ColumnHeader("ID"));
        colHeadersA.add(new ColumnHeader("NAME"));
        colHeadersA.add(new ColumnHeader("LOCATION"));
        tableA.setColHeadings(colHeadersA);

        // set up row data
        List<Record> rows1 = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("Swindon"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("Cardiff"));
        Record row2 = new Record(attr2);
        rows1.add(row1);
        rows1.add(row2);
        tableA.setRows(rows1);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(tableA);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        assertTrue(resultMessage.contains(STATUS_ERROR));

    }

    @Test
    public void test_joinCmd_attributeBDoesNotExist_statusCodeError() throws Exception{
        // ------------- given -------------
        // create dir and files
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableNameA = "PERSON";
        String tableNameB = "PET";
        String attributeNameA = "ID";
        String attributeNameB = "DOESNOTEXIST";
        String fullPathA = tempDbDirName + File.separator + tableNameA + fileExt;
        String fullPathB = tempDbDirName + File.separator + tableNameB + fileExt;
        File tempDBFileA = new File(fullPathA);
        assertTrue(tempDBFileA.createNewFile());
        assertTrue(tempDBFileA.exists());
        assertTrue(tempDBFileA.isFile());
        File tempDBFileB = new File(fullPathB);
        assertTrue(tempDBFileB.createNewFile());
        assertTrue(tempDBFileB.exists());
        assertTrue(tempDBFileB.isFile());

        // set up command
        cmd = new JoinCMD();
        cmd.addTableName(tableNameA);
        cmd.addTableName(tableNameB);
        cmd.addColumnName(attributeNameA);
        cmd.addColumnName(attributeNameB);


        // set up table and header
        Table tableA = new Table();
        TableHeader headerA = new TableHeader();
        headerA.setFileLocation(tempDBFileA);
        headerA.setTableName(tableNameA);
        tableA.setHeader(headerA);
        Table tableB = new Table();
        TableHeader headerB = new TableHeader();
        headerB.setFileLocation(tempDBFileB);
        headerB.setTableName(tableNameB);
        tableB.setHeader(headerB);

        // set up column headers
        List<ColumnHeader> colHeadersA = new ArrayList<>();
        colHeadersA.add(new ColumnHeader("ID"));
        colHeadersA.add(new ColumnHeader("NAME"));
        colHeadersA.add(new ColumnHeader("LOCATION"));
        tableA.setColHeadings(colHeadersA);
        List<ColumnHeader> colHeadersB = new ArrayList<>();
        colHeadersB.add(new ColumnHeader("ID"));
        colHeadersB.add(new ColumnHeader("NAME"));
        colHeadersB.add(new ColumnHeader("OWNERID"));
        tableB.setColHeadings(colHeadersB);

        // set up row data
        List<Record> rows1 = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("Swindon"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("Cardiff"));
        Record row2 = new Record(attr2);
        rows1.add(row1);
        rows1.add(row2);
        tableA.setRows(rows1);

        List<Record> rows2 = new ArrayList<>();
        List<Attribute> attr3 = new ArrayList<>();
        attr3.add(new Attribute("1"));
        attr3.add(new Attribute("Scratch"));
        attr3.add(new Attribute("1"));
        Record row3 = new Record(attr3);

        List<Attribute> attr4 = new ArrayList<>();
        attr4.add(new Attribute("2"));
        attr4.add(new Attribute("Fluffy"));
        attr4.add(new Attribute("2"));
        Record row4 = new Record(attr4);
        rows2.add(row3);
        rows2.add(row4);
        tableB.setRows(rows2);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(tableA);
        file.storeEntityIntoDBFile(tableB);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        assertTrue(resultMessage.contains(STATUS_ERROR));
    }
}
