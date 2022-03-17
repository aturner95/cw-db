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
        dbDir.deleteOnExit();
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
        String resultString = cmd.query(server);

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
        String resultString = cmd.query(server);

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
        String resultString = cmd.query(server);

        // then
        assertNotNull(resultString);
    }
}
