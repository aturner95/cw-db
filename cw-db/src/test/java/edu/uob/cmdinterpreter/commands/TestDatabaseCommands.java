package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.Token;
import edu.uob.cmdinterpreter.TokenType;
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
import java.util.Locale;

import static edu.uob.dbfilesystem.DBFileConstants.TABLE_EXT;
import static org.junit.jupiter.api.Assertions.*;

public class TestDatabaseCommands {

    private DBServer server;
    private File tempDbDir;
    private final String tempDbDirName = "dbtest";
    DBCmd cmd;

    public static final String STATUS_OK = "[OK]";
    public static final String STATUS_ERROR = "[ERROR]";

    @BeforeEach
    void setup(@TempDir File dbDir) throws Exception {
        tempDbDir = new File(tempDbDirName.toLowerCase());
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

    void clearDatabaseMetadata(String tablename) throws Exception{
        new DBTableFile().removeTableFromMetadata("dbtest", tablename);
    }

    @Test
    public void test_useCMD_databaseExists_statusCodeOK() throws Exception {
        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new UseCMD();
        cmd.setDatabaseName("dbtest");

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
        tempDbDir = new File(newTempDb.toLowerCase(Locale.ROOT));
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
        server.setUseDatabaseDirectory(tempDbDir);

        // given
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_OK));
        File tempFile = new File(tempDbDirName + File.separator + newTempTable.toLowerCase(Locale.ROOT) + ".tab");
        assertTrue(tempFile.exists());
        assertTrue(tempFile.isFile());

        clearDatabaseMetadata("newtemptable");
    }

    @Test
    public void test_createCMD_tableCreatedWithColumnHeadings_statusCodeOK() throws Exception {

        // given
        setup(tempDbDir);
        Files.createDirectory(tempDbDir.toPath());
        String newTempTable = "marks";
        String fullPath = tempDbDirName + File.separator + newTempTable + TABLE_EXT;

        cmd = new CreateCMD("TABLE");
        cmd.addTableName(newTempTable);
        cmd.addColumnName("name");
        cmd.addColumnName("mark");
        cmd.addColumnName("pass");
        server.setUseDatabaseDirectory(tempDbDir);

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

        clearDatabaseMetadata("marks");
    }

    @Test
    public void test_dropCMD_databaseDropped_statusCodeOK() throws Exception {

        // given
        Files.createDirectory(tempDbDir.toPath());
        setup(tempDbDir);
        cmd = new DropCMD("DATABASE");
        cmd.setDatabaseName("dbtest");

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
        server.setUseDatabaseDirectory(tempDbDir);
        File tempDBFile = new File(tempDbDirName + File.separator + "table.tab");
        assertTrue(tempDBFile.createNewFile());

        // given
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_OK));
        assertFalse(tempDBFile.exists());
        clearDatabaseMetadata("table");
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
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new AlterCMD("ADD");
        cmd.addTableName(tableName);
        cmd.addColumnName(attrName);
        server.setUseDatabaseDirectory(tempDbDir);

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
        clearDatabaseMetadata("people");
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
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new AlterCMD("DROP");
        cmd.addTableName(tableName);
        cmd.addColumnName(attrName);
        server.setUseDatabaseDirectory(tempDbDir);

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
        clearDatabaseMetadata("people");
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
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertFalse(tempDBFile.exists());

        cmd = new AlterCMD("DROP");
        cmd.addTableName(tableName);
        cmd.addColumnName(attrName);

        // when
        String resultMessage = cmd.query(server);

        // then
        assertTrue(resultMessage.contains(STATUS_ERROR));
        clearDatabaseMetadata("people");
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
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
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

        clearDatabaseMetadata("people");
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
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
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
        clearDatabaseMetadata("people");
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
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
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
        server.setUseDatabaseDirectory(tempDbDir);


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
        file.addTableToMetadata(tempDbDirName, tableName.toLowerCase(Locale.ROOT), 3);

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

        clearDatabaseMetadata("country");
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
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
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

        clearDatabaseMetadata("country");
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
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
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

        clearDatabaseMetadata("country");
    }

    @Test
    public void test_insertCMD_tableDoesNotExist_statusCodeError() throws Exception{
        // given
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "doesNotExist";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
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
        String fullPathA = tempDbDirName + File.separator + tableNameA + TABLE_EXT;
        String fullPathB = tempDbDirName + File.separator + tableNameB + TABLE_EXT;
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
        server.setUseDatabaseDirectory(tempDbDir);

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

        clearDatabaseMetadata("pet");
        clearDatabaseMetadata("person");
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
        String fullPathA = tempDbDirName + File.separator + tableNameA + TABLE_EXT;
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
        String fullPathA = tempDbDirName + File.separator + tableNameA + TABLE_EXT;
        String fullPathB = tempDbDirName + File.separator + tableNameB + TABLE_EXT;
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
        clearDatabaseMetadata("pet");
        clearDatabaseMetadata("person");
    }



    @Test
    public void test_selectCmd_selectStarNoCondition_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "Country";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new SelectCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("*");
        server.setUseDatabaseDirectory(tempDbDir);


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

        // ------------- then -------------
        String expectedMessage = STATUS_OK + System.lineSeparator()
                + "Id\tName\tCapital" + System.lineSeparator()
                + "1\tGermany\tBerlin" + System.lineSeparator()
                + "2\tSpain\tMadrid" + System.lineSeparator();
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("country");
    }

    @Test
    public void test_selectCmd_selectStarNoRecords_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "Country";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new SelectCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("*");
        server.setUseDatabaseDirectory(tempDbDir);


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

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        String expectedMessage = STATUS_OK + System.lineSeparator()
                + "Id\tName\tCapital" + System.lineSeparator();
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("country");
    }


    // @Test TODO need to decide what to do about this situation...
    public void test_selectCmd_selectStarNoAttributes_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "Country";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new SelectCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("*");
        server.setUseDatabaseDirectory(tempDbDir);


        // set up table and header
        Table table = new Table();
        TableHeader header = new TableHeader();
        header.setFileLocation(tempDBFile);
        header.setTableName(tableName);
        table.setHeader(header);
        table.setColHeadings(new ArrayList<>());

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        String expectedMessage = STATUS_OK;
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("country");
    }


    @Test
    public void test_selectCmd_selectStarWithCondition_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "Country";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new SelectCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("*");
        cmd.addCondition(new ColumnHeader("Id"), "==", new Token(TokenType.LIT_NUM, "2"));
        server.setUseDatabaseDirectory(tempDbDir);

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

        // ------------- then -------------
        String expectedMessage = STATUS_OK + System.lineSeparator()
                + "Id\tName\tCapital" + System.lineSeparator()
                + "2\tSpain\tMadrid" + System.lineSeparator();
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("country");
    }

    @Test
    public void test_selectCmd_selectAttributesNotInOrder_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "marks";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new SelectCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("name");
        cmd.addColumnName("id");
        cmd.addCondition(new ColumnHeader("pass"), "==", new Token(TokenType.LIT_BOOL, "TRUE"));
        server.setUseDatabaseDirectory(tempDbDir);


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
        colHeaders.add(new ColumnHeader("mark"));
        colHeaders.add(new ColumnHeader("pass"));
        table.setColHeadings(colHeaders);

        List<Record> marksData = new ArrayList<>();
        List<Attribute> attr5 = new ArrayList<>();
        attr5.add(new Attribute("1"));
        attr5.add(new Attribute("Steve"));
        attr5.add(new Attribute("65"));
        attr5.add(new Attribute("TRUE"));
        Record row5 = new Record(attr5);

        List<Attribute> attr6 = new ArrayList<>();
        attr6.add(new Attribute("2"));
        attr6.add(new Attribute("Dave"));
        attr6.add(new Attribute("55"));
        attr6.add(new Attribute("TRUE"));
        Record row6 = new Record(attr6);

        List<Attribute> attr7 = new ArrayList<>();
        attr7.add(new Attribute("3"));
        attr7.add(new Attribute("Bob"));
        attr7.add(new Attribute("35"));
        attr7.add(new Attribute("FALSE"));
        Record row7 = new Record(attr7);

        List<Attribute> attr8 = new ArrayList<>();
        attr8.add(new Attribute("4"));
        attr8.add(new Attribute("Clive"));
        attr8.add(new Attribute("20"));
        attr8.add(new Attribute("FALSE"));
        Record row8 = new Record(attr8);

        marksData.add(row5);
        marksData.add(row6);
        marksData.add(row7);
        marksData.add(row8);
        table.setRows(marksData);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        String expectedMessage = STATUS_OK + System.lineSeparator()
                + "name\tid" + System.lineSeparator()
                + "Steve\t1" + System.lineSeparator()
                + "Dave\t2" + System.lineSeparator();
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("marks");
    }

    @Test
    public void test_selectCmd_selectAttrWithCondition_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "Country";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new SelectCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("Id");
        cmd.addColumnName("Name");
        cmd.addCondition(new ColumnHeader("Id"), "<=", new Token(TokenType.LIT_NUM, "2"));
        server.setUseDatabaseDirectory(tempDbDir);


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

        // ------------- then -------------
        String expectedMessage = STATUS_OK + System.lineSeparator()
                + "Id\tName" + System.lineSeparator()
                + "1\tGermany" + System.lineSeparator()
                + "2\tSpain" + System.lineSeparator();
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("country");
    }

    @Test
    public void test_selectCmd_selectAttrWithCondition2_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "students";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new SelectCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("name");
        cmd.addColumnName("grade");
        cmd.addCondition(new ColumnHeader("name"), "==", new Token(TokenType.LIT_STR, "\'Anna\'"));
        server.setUseDatabaseDirectory(tempDbDir);


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
        colHeaders.add(new ColumnHeader("grade"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("55"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("63"));
        Record row2 = new Record(attr2);
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        String expectedMessage = STATUS_OK + System.lineSeparator()
                + "name\tgrade" + System.lineSeparator()
                + "Anna\t55" + System.lineSeparator();
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("students");
    }

    @Test
    public void test_selectCmd_selectStarWithAndConditions_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "students";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new SelectCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("*");
        cmd.addCondition(new ColumnHeader("name"), "==", new Token(TokenType.LIT_STR, "\'Anna\'"));
        cmd.addConditionJoinOperator("AND");
        cmd.addCondition(new ColumnHeader("grade"), ">=", new Token(TokenType.LIT_NUM, "50"));
        server.setUseDatabaseDirectory(tempDbDir);


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
        colHeaders.add(new ColumnHeader("grade"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("45"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Anna"));
        attr2.add(new Attribute("63"));
        Record row2 = new Record(attr2);
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        String expectedMessage = STATUS_OK + System.lineSeparator()
                + "id\tname\tgrade" + System.lineSeparator()
                + "2\tAnna\t63" + System.lineSeparator();
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("students");
    }

    @Test
    public void test_selectCmd_selectAttributesWithOrConditions_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "students";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new SelectCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("id");
        cmd.addColumnName("name");
        cmd.addColumnName("grade");
        cmd.addCondition(new ColumnHeader("grade"), ">=", new Token(TokenType.LIT_NUM, "50"));
        cmd.addConditionJoinOperator("OR");
        cmd.addCondition(new ColumnHeader("pass"), "==", new Token(TokenType.LIT_BOOL, "TRUE"));
        server.setUseDatabaseDirectory(tempDbDir);


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
        colHeaders.add(new ColumnHeader("grade"));
        colHeaders.add(new ColumnHeader("pass"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("45"));
        attr1.add(new Attribute("TRUE"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("70"));
        attr2.add(new Attribute("FALSE"));
        Record row2 = new Record(attr2);
        rows.add(row1);
        rows.add(row2);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        String expectedMessage = STATUS_OK + System.lineSeparator()
                + "id\tname\tgrade" + System.lineSeparator()
                + "1\tAnna\t45" + System.lineSeparator()
                + "2\tBob\t70" + System.lineSeparator();
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("students");
    }

    @Test
    public void test_selectCmd_deleteCmdWithSingleCond_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "students";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new DeleteCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("id");
        cmd.addColumnName("name");
        cmd.addColumnName("grade");
        cmd.addColumnName("pass");
        cmd.addCondition(new ColumnHeader("pass"), "==", new Token(TokenType.LIT_BOOL, "FALSE"));
        server.setUseDatabaseDirectory(tempDbDir);

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
        colHeaders.add(new ColumnHeader("grade"));
        colHeaders.add(new ColumnHeader("pass"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("66"));
        attr1.add(new Attribute("TRUE"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("49"));
        attr2.add(new Attribute("TRUE"));
        Record row2 = new Record(attr2);

        List<Attribute> attr3 = new ArrayList<>();
        attr3.add(new Attribute("3"));
        attr3.add(new Attribute("Clive"));
        attr3.add(new Attribute("33"));
        attr3.add(new Attribute("FALSE"));
        Record row3 = new Record(attr3);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);


        // ------------- when -------------
        String resultMessage = cmd.query(server);
        Table testResult = file.readDBFileIntoEntity(fullPath);

        // ------------- then -------------
        String expectedMessage = STATUS_OK;
        assertEquals(expectedMessage, resultMessage);
        assertEquals(2, testResult.getRows().size());
        assertEquals("Anna", testResult.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("Bob", testResult.getRows().get(1).getAttributes().get(1).getValue());

        clearDatabaseMetadata("students");
    }

    @Test
    public void test_selectCmd_deleteCmdWithOrConds_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "students";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new DeleteCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("id");
        cmd.addColumnName("name");
        cmd.addColumnName("grade");
        cmd.addColumnName("pass");
        cmd.addCondition(new ColumnHeader("pass"), "==", new Token(TokenType.LIT_BOOL, "FALSE"));
        cmd.getConditionJoinOperators().add("OR");
        cmd.addCondition(new ColumnHeader("grade"), "<=", new Token(TokenType.LIT_NUM, "50"));
        server.setUseDatabaseDirectory(tempDbDir);


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
        colHeaders.add(new ColumnHeader("grade"));
        colHeaders.add(new ColumnHeader("pass"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("66"));
        attr1.add(new Attribute("TRUE"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("49"));
        attr2.add(new Attribute("TRUE"));
        Record row2 = new Record(attr2);

        List<Attribute> attr3 = new ArrayList<>();
        attr3.add(new Attribute("3"));
        attr3.add(new Attribute("Clive"));
        attr3.add(new Attribute("33"));
        attr3.add(new Attribute("FALSE"));
        Record row3 = new Record(attr3);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);


        // ------------- when -------------
        String resultMessage = cmd.query(server);
        Table testResult = file.readDBFileIntoEntity(fullPath);

        // ------------- then -------------
        String expectedMessage = STATUS_OK;
        assertEquals(expectedMessage, resultMessage);
        assertEquals(1, testResult.getRows().size());
        assertEquals("Anna", testResult.getRows().get(0).getAttributes().get(1).getValue());

        clearDatabaseMetadata("students");
    }

    @Test
    public void test_selectCmd_deleteCmdNoRows_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "students";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new DeleteCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("id");
        cmd.addColumnName("name");
        cmd.addColumnName("grade");
        cmd.addColumnName("pass");
        cmd.addCondition(new ColumnHeader("pass"), "==", new Token(TokenType.LIT_BOOL, "FALSE"));
        cmd.getConditionJoinOperators().add("OR");
        cmd.addCondition(new ColumnHeader("grade"), "<=", new Token(TokenType.LIT_NUM, "50"));
        server.setUseDatabaseDirectory(tempDbDir);


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
        colHeaders.add(new ColumnHeader("grade"));
        colHeaders.add(new ColumnHeader("pass"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);


        // ------------- when -------------
        String resultMessage = cmd.query(server);
        Table testResult = file.readDBFileIntoEntity(fullPath);

        // ------------- then -------------
        String expectedMessage = STATUS_OK;
        assertEquals(expectedMessage, resultMessage);
        assertEquals(0, testResult.getRows().size());

        clearDatabaseMetadata("students");
    }

    @Test
    public void test_selectCmd_deleteCmdNoRowsDeleted_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "students";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new DeleteCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("id");
        cmd.addColumnName("name");
        cmd.addColumnName("grade");
        cmd.addColumnName("pass");
        cmd.addCondition(new ColumnHeader("name"), "==", new Token(TokenType.LIT_STR, "Darren"));
        server.setUseDatabaseDirectory(tempDbDir);


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
        colHeaders.add(new ColumnHeader("grade"));
        colHeaders.add(new ColumnHeader("pass"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("66"));
        attr1.add(new Attribute("TRUE"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("49"));
        attr2.add(new Attribute("TRUE"));
        Record row2 = new Record(attr2);

        List<Attribute> attr3 = new ArrayList<>();
        attr3.add(new Attribute("3"));
        attr3.add(new Attribute("Clive"));
        attr3.add(new Attribute("33"));
        attr3.add(new Attribute("FALSE"));
        Record row3 = new Record(attr3);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);


        // ------------- when -------------
        String resultMessage = cmd.query(server);
        Table testResult = file.readDBFileIntoEntity(fullPath);

        // ------------- then -------------
        String expectedMessage = STATUS_OK;
        assertEquals(expectedMessage, resultMessage);
        assertEquals(3, testResult.getRows().size());
        assertEquals("Anna", testResult.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("Bob", testResult.getRows().get(1).getAttributes().get(1).getValue());
        assertEquals("Clive", testResult.getRows().get(2).getAttributes().get(1).getValue());

        clearDatabaseMetadata("students");
    }

    @Test
    public void test_selectCmd_updateCmd_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "students";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new UpdateCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("id");
        cmd.addColumnName("name");
        cmd.addColumnName("grade");
        cmd.addColumnName("pass");
        cmd.addNameValuePair("grade", "50");
        cmd.addCondition(new ColumnHeader("name"), "==", new Token(TokenType.LIT_STR, "Bob"));
        server.setUseDatabaseDirectory(tempDbDir);


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
        colHeaders.add(new ColumnHeader("grade"));
        colHeaders.add(new ColumnHeader("pass"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("66"));
        attr1.add(new Attribute("TRUE"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("49"));
        attr2.add(new Attribute("TRUE"));
        Record row2 = new Record(attr2);

        List<Attribute> attr3 = new ArrayList<>();
        attr3.add(new Attribute("3"));
        attr3.add(new Attribute("Clive"));
        attr3.add(new Attribute("33"));
        attr3.add(new Attribute("FALSE"));
        Record row3 = new Record(attr3);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);


        // ------------- when -------------
        String resultMessage = cmd.query(server);
        Table testResult = file.readDBFileIntoEntity(fullPath);

        // ------------- then -------------
        String expectedMessage = STATUS_OK;
        assertEquals(expectedMessage, resultMessage);
        assertEquals(3, testResult.getRows().size());

        assertEquals("1", testResult.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("Anna", testResult.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("66", testResult.getRows().get(0).getAttributes().get(2).getValue());
        assertEquals("TRUE", testResult.getRows().get(0).getAttributes().get(3).getValue());

        assertEquals("2", testResult.getRows().get(1).getAttributes().get(0).getValue());
        assertEquals("Bob", testResult.getRows().get(1).getAttributes().get(1).getValue());
        assertEquals("50", testResult.getRows().get(1).getAttributes().get(2).getValue());
        assertEquals("TRUE", testResult.getRows().get(1).getAttributes().get(3).getValue());

        assertEquals("3", testResult.getRows().get(2).getAttributes().get(0).getValue());
        assertEquals("Clive", testResult.getRows().get(2).getAttributes().get(1).getValue());
        assertEquals("33", testResult.getRows().get(2).getAttributes().get(2).getValue());
        assertEquals("FALSE", testResult.getRows().get(2).getAttributes().get(3).getValue());

        clearDatabaseMetadata("students");
    }

    @Test
    public void test_selectCmd_updateCmdNamePairList_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "students";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new UpdateCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("id");
        cmd.addColumnName("name");
        cmd.addColumnName("grade");
        cmd.addColumnName("pass");
        cmd.addNameValuePair("grade", "50");
        cmd.addNameValuePair("pass", "TRUE");
        cmd.addCondition(new ColumnHeader("name"), "==", new Token(TokenType.LIT_STR, "Bob"));
        cmd.addConditionJoinOperator("OR");
        cmd.addCondition(new ColumnHeader("name"), "==", new Token(TokenType.LIT_STR, "Clive"));
        server.setUseDatabaseDirectory(tempDbDir);

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
        colHeaders.add(new ColumnHeader("grade"));
        colHeaders.add(new ColumnHeader("pass"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("66"));
        attr1.add(new Attribute("TRUE"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("49"));
        attr2.add(new Attribute("TRUE"));
        Record row2 = new Record(attr2);

        List<Attribute> attr3 = new ArrayList<>();
        attr3.add(new Attribute("3"));
        attr3.add(new Attribute("Clive"));
        attr3.add(new Attribute("33"));
        attr3.add(new Attribute("FALSE"));
        Record row3 = new Record(attr3);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);


        // ------------- when -------------
        String resultMessage = cmd.query(server);
        Table testResult = file.readDBFileIntoEntity(fullPath);

        // ------------- then -------------
        String expectedMessage = STATUS_OK;
        assertEquals(expectedMessage, resultMessage);
        assertEquals(3, testResult.getRows().size());

        assertEquals("1", testResult.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("Anna", testResult.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("66", testResult.getRows().get(0).getAttributes().get(2).getValue());
        assertEquals("TRUE", testResult.getRows().get(0).getAttributes().get(3).getValue());

        assertEquals("2", testResult.getRows().get(1).getAttributes().get(0).getValue());
        assertEquals("Bob", testResult.getRows().get(1).getAttributes().get(1).getValue());
        assertEquals("50", testResult.getRows().get(1).getAttributes().get(2).getValue());
        assertEquals("TRUE", testResult.getRows().get(1).getAttributes().get(3).getValue());

        assertEquals("3", testResult.getRows().get(2).getAttributes().get(0).getValue());
        assertEquals("Clive", testResult.getRows().get(2).getAttributes().get(1).getValue());
        assertEquals("50", testResult.getRows().get(2).getAttributes().get(2).getValue());
        assertEquals("TRUE", testResult.getRows().get(2).getAttributes().get(3).getValue());

        clearDatabaseMetadata("students");
    }

    @Test
    public void test_selectCmd_updateCmdNoRowsFound_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir and file
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);
        String tableName = "students";
        String fullPath = tempDbDirName + File.separator + tableName + TABLE_EXT;
        File tempDBFile = new File(fullPath);
        assertTrue(tempDBFile.createNewFile());
        assertTrue(tempDBFile.exists());
        assertTrue(tempDBFile.isFile());

        // set up command
        cmd = new UpdateCMD();
        cmd.addTableName(tableName);
        cmd.addColumnName("id");
        cmd.addColumnName("name");
        cmd.addColumnName("grade");
        cmd.addColumnName("pass");
        cmd.addNameValuePair("grade", "50");
        cmd.addNameValuePair("pass", "TRUE");
        cmd.addCondition(new ColumnHeader("name"), "==", new Token(TokenType.LIT_STR, "Clive"));
        cmd.addConditionJoinOperator("AND");
        cmd.addCondition(new ColumnHeader("grade"), ">=", new Token(TokenType.LIT_STR, "48"));
        server.setUseDatabaseDirectory(tempDbDir);


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
        colHeaders.add(new ColumnHeader("grade"));
        colHeaders.add(new ColumnHeader("pass"));
        table.setColHeadings(colHeaders);

        // set up row data
        List<Record> rows = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("Anna"));
        attr1.add(new Attribute("66"));
        attr1.add(new Attribute("TRUE"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("Bob"));
        attr2.add(new Attribute("49"));
        attr2.add(new Attribute("TRUE"));
        Record row2 = new Record(attr2);

        List<Attribute> attr3 = new ArrayList<>();
        attr3.add(new Attribute("3"));
        attr3.add(new Attribute("Clive"));
        attr3.add(new Attribute("33"));
        attr3.add(new Attribute("FALSE"));
        Record row3 = new Record(attr3);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        table.setRows(rows);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(table);


        // ------------- when -------------
        String resultMessage = cmd.query(server);
        Table testResult = file.readDBFileIntoEntity(fullPath);

        // ------------- then -------------
        String expectedMessage = STATUS_OK;
        assertEquals(expectedMessage, resultMessage);
        assertEquals(3, testResult.getRows().size());

        assertEquals("1", testResult.getRows().get(0).getAttributes().get(0).getValue());
        assertEquals("Anna", testResult.getRows().get(0).getAttributes().get(1).getValue());
        assertEquals("66", testResult.getRows().get(0).getAttributes().get(2).getValue());
        assertEquals("TRUE", testResult.getRows().get(0).getAttributes().get(3).getValue());

        assertEquals("2", testResult.getRows().get(1).getAttributes().get(0).getValue());
        assertEquals("Bob", testResult.getRows().get(1).getAttributes().get(1).getValue());
        assertEquals("49", testResult.getRows().get(1).getAttributes().get(2).getValue());
        assertEquals("TRUE", testResult.getRows().get(1).getAttributes().get(3).getValue());

        assertEquals("3", testResult.getRows().get(2).getAttributes().get(0).getValue());
        assertEquals("Clive", testResult.getRows().get(2).getAttributes().get(1).getValue());
        assertEquals("33", testResult.getRows().get(2).getAttributes().get(2).getValue());
        assertEquals("FALSE", testResult.getRows().get(2).getAttributes().get(3).getValue());

        clearDatabaseMetadata("students");
    }

    @Test
    public void test_selectCmd_joinCmd_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);

        // create coursework.tab
        String tableNameA = "coursework";
        String fullPathA = tempDbDirName + File.separator + tableNameA + TABLE_EXT;
        File tempDBFileA = new File(fullPathA);
        assertTrue(tempDBFileA.createNewFile());
        assertTrue(tempDBFileA.exists());
        assertTrue(tempDBFileA.isFile());

        // create marks.tab
        String tableNameB = "marks";
        String fullPathB = tempDbDirName + File.separator + tableNameB + TABLE_EXT;
        File tempDBFileB = new File(fullPathB);
        assertTrue(tempDBFileB.createNewFile());
        assertTrue(tempDBFileB.exists());
        assertTrue(tempDBFileB.isFile());

        // set up command
        cmd = new JoinCMD();
        cmd.addTableName(tableNameA);
        cmd.addTableName(tableNameB);
        cmd.addColumnName("grade");
        cmd.addColumnName("id");
        server.setUseDatabaseDirectory(tempDbDir);

        // set up coursework table data
        Table course = new Table();
        TableHeader courseHeader = new TableHeader();
        courseHeader.setFileLocation(tempDBFileA);
        courseHeader.setTableName(tableNameA);
        course.setHeader(courseHeader);

        List<ColumnHeader> courseColHeaders = new ArrayList<>();
        courseColHeaders.add(new ColumnHeader("id"));
        courseColHeaders.add(new ColumnHeader("task"));
        courseColHeaders.add(new ColumnHeader("grade"));
        course.setColHeadings(courseColHeaders);

        List<Record> courseworkData = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("OXO"));
        attr1.add(new Attribute("3"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("DB"));
        attr2.add(new Attribute("1"));
        Record row2 = new Record(attr2);

        List<Attribute> attr3 = new ArrayList<>();
        attr3.add(new Attribute("3"));
        attr3.add(new Attribute("OXO"));
        attr3.add(new Attribute("4"));
        Record row3 = new Record(attr3);

        List<Attribute> attr4 = new ArrayList<>();
        attr4.add(new Attribute("4"));
        attr4.add(new Attribute("STAG"));
        attr4.add(new Attribute("2"));
        Record row4 = new Record(attr4);

        courseworkData.add(row1);
        courseworkData.add(row2);
        courseworkData.add(row3);
        courseworkData.add(row4);
        course.setRows(courseworkData);


        // set up marks table and data
        Table marks = new Table();
        TableHeader marksHeader = new TableHeader();
        marksHeader.setFileLocation(tempDBFileB);
        marksHeader.setTableName(tableNameB);
        marks.setHeader(marksHeader);

        List<ColumnHeader> markHeaders = new ArrayList<>();
        markHeaders.add(new ColumnHeader("id"));
        markHeaders.add(new ColumnHeader("name"));
        markHeaders.add(new ColumnHeader("mark"));
        markHeaders.add(new ColumnHeader("pass"));
        marks.setColHeadings(markHeaders);

        List<Record> marksData = new ArrayList<>();
        List<Attribute> attr5 = new ArrayList<>();
        attr5.add(new Attribute("1"));
        attr5.add(new Attribute("Steve"));
        attr5.add(new Attribute("65"));
        attr5.add(new Attribute("TRUE"));
        Record row5 = new Record(attr5);

        List<Attribute> attr6 = new ArrayList<>();
        attr6.add(new Attribute("2"));
        attr6.add(new Attribute("Dave"));
        attr6.add(new Attribute("55"));
        attr6.add(new Attribute("TRUE"));
        Record row6 = new Record(attr6);

        List<Attribute> attr7 = new ArrayList<>();
        attr7.add(new Attribute("3"));
        attr7.add(new Attribute("Bob"));
        attr7.add(new Attribute("35"));
        attr7.add(new Attribute("FALSE"));
        Record row7 = new Record(attr7);

        List<Attribute> attr8 = new ArrayList<>();
        attr8.add(new Attribute("4"));
        attr8.add(new Attribute("Clive"));
        attr8.add(new Attribute("20"));
        attr8.add(new Attribute("FALSE"));
        Record row8 = new Record(attr8);

        marksData.add(row5);
        marksData.add(row6);
        marksData.add(row7);
        marksData.add(row8);
        marks.setRows(marksData);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(marks);
        file.storeEntityIntoDBFile(course);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        String expectedMessage = STATUS_OK + System.lineSeparator() +
                "id\ttask\tname\tmark\tpass" + System.lineSeparator() +
                "1\tOXO\tBob\t35\tFALSE" + System.lineSeparator() +
                "2\tDB\tSteve\t65\tTRUE" + System.lineSeparator() +
                "3\tOXO\tClive\t20\tFALSE" + System.lineSeparator() +
                "4\tSTAG\tDave\t55\tTRUE" + System.lineSeparator();
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("marks");
        clearDatabaseMetadata("coursework");
    }

    @Test
    public void test_selectCmd_joinCmdNoResult_statusCodeOK() throws Exception {

        // ------------- given -------------
        // create dir
        Files.createDirectory(tempDbDir.toPath());
        assertTrue(tempDbDir.exists());
        assertTrue(tempDbDir.isDirectory());
        setup(tempDbDir);

        // create coursework.tab
        String tableNameA = "coursework";
        String fullPathA = tempDbDirName + File.separator + tableNameA + TABLE_EXT;
        File tempDBFileA = new File(fullPathA);
        assertTrue(tempDBFileA.createNewFile());
        assertTrue(tempDBFileA.exists());
        assertTrue(tempDBFileA.isFile());

        // create marks.tab
        String tableNameB = "marks";
        String fullPathB = tempDbDirName + File.separator + tableNameB + TABLE_EXT;
        File tempDBFileB = new File(fullPathB);
        assertTrue(tempDBFileB.createNewFile());
        assertTrue(tempDBFileB.exists());
        assertTrue(tempDBFileB.isFile());

        // set up command
        cmd = new JoinCMD();
        cmd.addTableName(tableNameA);
        cmd.addTableName(tableNameB);
        cmd.addColumnName("grade");
        cmd.addColumnName("id");
        server.setUseDatabaseDirectory(tempDbDir);

        // set up coursework table data
        Table course = new Table();
        TableHeader courseHeader = new TableHeader();
        courseHeader.setFileLocation(tempDBFileA);
        courseHeader.setTableName(tableNameA);
        course.setHeader(courseHeader);

        List<ColumnHeader> courseColHeaders = new ArrayList<>();
        courseColHeaders.add(new ColumnHeader("id"));
        courseColHeaders.add(new ColumnHeader("task"));
        courseColHeaders.add(new ColumnHeader("grade"));
        course.setColHeadings(courseColHeaders);

        List<Record> courseworkData = new ArrayList<>();
        List<Attribute> attr1 = new ArrayList<>();
        attr1.add(new Attribute("1"));
        attr1.add(new Attribute("OXO"));
        attr1.add(new Attribute("11"));
        Record row1 = new Record(attr1);

        List<Attribute> attr2 = new ArrayList<>();
        attr2.add(new Attribute("2"));
        attr2.add(new Attribute("DB"));
        attr2.add(new Attribute("11"));
        Record row2 = new Record(attr2);

        List<Attribute> attr3 = new ArrayList<>();
        attr3.add(new Attribute("3"));
        attr3.add(new Attribute("OXO"));
        attr3.add(new Attribute("41"));
        Record row3 = new Record(attr3);

        List<Attribute> attr4 = new ArrayList<>();
        attr4.add(new Attribute("4"));
        attr4.add(new Attribute("STAG"));
        attr4.add(new Attribute("21"));
        Record row4 = new Record(attr4);

        courseworkData.add(row1);
        courseworkData.add(row2);
        courseworkData.add(row3);
        courseworkData.add(row4);
        course.setRows(courseworkData);


        // set up marks table and data
        Table marks = new Table();
        TableHeader marksHeader = new TableHeader();
        marksHeader.setFileLocation(tempDBFileB);
        marksHeader.setTableName(tableNameB);
        marks.setHeader(marksHeader);

        List<ColumnHeader> markHeaders = new ArrayList<>();
        markHeaders.add(new ColumnHeader("id"));
        markHeaders.add(new ColumnHeader("name"));
        markHeaders.add(new ColumnHeader("mark"));
        markHeaders.add(new ColumnHeader("pass"));
        marks.setColHeadings(markHeaders);

        List<Record> marksData = new ArrayList<>();
        List<Attribute> attr5 = new ArrayList<>();
        attr5.add(new Attribute("1"));
        attr5.add(new Attribute("Steve"));
        attr5.add(new Attribute("65"));
        attr5.add(new Attribute("TRUE"));
        Record row5 = new Record(attr5);

        List<Attribute> attr6 = new ArrayList<>();
        attr6.add(new Attribute("2"));
        attr6.add(new Attribute("Dave"));
        attr6.add(new Attribute("55"));
        attr6.add(new Attribute("TRUE"));
        Record row6 = new Record(attr6);

        List<Attribute> attr7 = new ArrayList<>();
        attr7.add(new Attribute("3"));
        attr7.add(new Attribute("Bob"));
        attr7.add(new Attribute("35"));
        attr7.add(new Attribute("FALSE"));
        Record row7 = new Record(attr7);

        List<Attribute> attr8 = new ArrayList<>();
        attr8.add(new Attribute("4"));
        attr8.add(new Attribute("Clive"));
        attr8.add(new Attribute("20"));
        attr8.add(new Attribute("FALSE"));
        Record row8 = new Record(attr8);

        marksData.add(row5);
        marksData.add(row6);
        marksData.add(row7);
        marksData.add(row8);
        marks.setRows(marksData);

        DBTableFile file = new DBTableFile();
        file.storeEntityIntoDBFile(marks);
        file.storeEntityIntoDBFile(course);

        // ------------- when -------------
        String resultMessage = cmd.query(server);

        // ------------- then -------------
        String expectedMessage = STATUS_OK + System.lineSeparator() +
                "id\ttask\tname\tmark\tpass" + System.lineSeparator();
        assertEquals(expectedMessage, resultMessage);

        clearDatabaseMetadata("marks");
        clearDatabaseMetadata("coursework");
    }

}
