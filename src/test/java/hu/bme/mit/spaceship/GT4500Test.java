package hu.bme.mit.spaceship;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Assumptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

class GT4500Test {

  private GT4500 ship;

  @BeforeEach
  public void init(){
    this.ship = new GT4500();
  }

  @Test
  void fireTorpedo_Single_Success(){
    // Arrange

    // Act
    boolean result = ship.fireTorpedo(FiringMode.SINGLE);

    // Assert
    assertEquals(true, result);
  }

  @Test
  void fireTorpedo_All_Success(){
    // Arrange

    // Act
    boolean result = ship.fireTorpedo(FiringMode.ALL);

    // Assert
    assertEquals(true, result);
  }

  /**
   * Test the ship using the command line interface.
   *
   * Input commands are provided as a parameter.
   * Expected outputs may be provided in another file.  In case the
   * output file does not exist, it is simply ignored and the test is
   * inconclusive.
   */
  @ParameterizedTest
  @MethodSource("provideTestFiles")
  void runCommandsFromFile_Success(File inputFile, File outputFile) throws IOException {
      // Arrange
      InputStream in = new FileInputStream(inputFile);
      OutputStream actualOut = new ByteArrayOutputStream();

      // Act
      CommandLineInterface.run(in, actualOut);

      // Assert
      if (! outputFile.exists()) {
        // No output file was provided; test is inconclusive but we
        // still get coverage metrics for the execution
        inconclusive();
      } else {
        try (InputStream expectedOut = new FileInputStream(outputFile)) {
          String expected = normalizeString(new String(expectedOut.readAllBytes(), StandardCharsets.UTF_8));
          String actual = normalizeString(actualOut.toString());
          assertEquals(expected, actual);
        }
      }
  }

  private static Stream<Arguments> provideTestFiles() throws IOException {
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/input*");

    Map<File, File> files = new HashMap<>(); // map of input-output files
    Files.walkFileTree(Paths.get("test-data/"), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (matcher.matches(path)) {
          File in = path.toFile();
          File out = Path.of(path.toString().replace("input", "output")).toFile();
          files.put(in, out);
        }
        return FileVisitResult.CONTINUE;
      }
    });

    return files.entrySet().stream().map((entry) -> Arguments.of(entry.getKey(), entry.getValue()));
  }

  /**
   * Utility method to force a test result to be 'inconclusive'.
   */
  private static void inconclusive() {
    Assumptions.assumeTrue(false, "Inconclusive");
  }

  /**
   * Normalize a string by stripping all leading and trailing whitespace
   * and replacing all whitespace with a single space.
   */
  private static String normalizeString(String s) {
    return s.strip().replaceAll("\\s+", " ");
  }
}