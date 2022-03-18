package edu.uob.cmdinterpreter.commands;

import edu.uob.DBServer;
import edu.uob.cmdinterpreter.commands.UseCMD;
import edu.uob.cmdinterpreter.commands.abstractcmd.DBCmd;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestDatabaseCommands {

    private DBServer server;
    private File tempDbDir;
    private static final String tempDbDirName = "dbtest";
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
}
