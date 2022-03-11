package edu.uob;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

// PLEASE READ:
// The tests in this file will fail by default for a template skeleton, your job is to pass them
// and maybe write some more, read up on how to write tests at
// https://junit.org/junit5/docs/current/user-guide/#writing-tests
final class DBTests {

  private DBServer server;
  private File tempDbDir;
  private static final String tempDbDirName = "dbtest";
  // private static final String tableFileExt = ".tab";

  // we make a new server for every @Test (i.e. this method runs before every @Test test case)
  @BeforeEach
  void setup(@TempDir File dbDir) {
    // Notice the @TempDir annotation, this instructs JUnit to create a new temp directory somewhere
    // and proceeds to *delete* that directory when the test finishes.
    // You can read the specifics of this at
    // https://junit.org/junit5/docs/5.4.2/api/org/junit/jupiter/api/io/TempDir.html

    // If you want to inspect the content of the directory during/after a test run for debugging,
    // simply replace `dbDir` here with your own File instance that points to somewhere you know.
    // IMPORTANT: If you do this, make sure you rerun the tests using `dbDir` again to make sure it
    // still works and keep it that way for the submission.
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
  }

  // Here's a basic test for spawning a new server and sending an invalid command,
  // the spec dictates that the server respond with something that starts with `[ERROR]`
  // @Test
  public void test_invalidCommand_isAnError() {
    assertTrue(server.handleCommand("foo").startsWith("[ERROR]"));
  }

  // Add more unit tests or integration tests here.
  // Unit tests would test individual methods or classes whereas integration tests are geared
  // towards a specific usecase (i.e. creating a table and inserting rows and asserting whether the
  // rows are actually inserted)

  @Test
  public void test_handleCommand_databaseDirectoryDoesNotExist_responseStatusIsError() {
    File testDir = new File(tempDbDirName);
    setup(testDir);
    assertFalse(tempDbDir.exists());
    assertFalse(tempDbDir.isDirectory());
    assertTrue(server.handleCommand("foo").startsWith("[ERROR]"));
  }

  @Test
  public void test_handleCommand_databaseDirectoryExistsButNotDirectory_responseStatusIsError () throws Exception {
    File testDir = new File(tempDbDirName);
    assertTrue(testDir.createNewFile());
    setup(testDir);
    assertTrue(tempDbDir.exists());
    assertFalse(tempDbDir.isDirectory());
    assertTrue(server.handleCommand("foo").startsWith("[ERROR]"));
  }

  /*
   * This is a test specific to Task 5: Communication. I will assume that it will fail
   * at some point down the line...
   */
  @Test
  public void test_handleCommand_emptyDatabaseDirectoryExistsContains_responseStatusIsOK() throws Exception {
    Files.createDirectory(tempDbDir.toPath());
    setup(tempDbDir);
    assertTrue(tempDbDir.exists());
    assertTrue(tempDbDir.isDirectory());
    assertTrue(server.handleCommand("foo").startsWith("[OK]"));
  }

}
