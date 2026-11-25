package org.scip_code.scip_java.aggregator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.scip_code.scip.ToolInfo;

/**
 * Regression test for creating the temporary output file on file systems without POSIX permission
 * support, such as NTFS on Windows.
 */
class ScipWriterTest {

  @Test
  void writesOutputFile(@TempDir Path dir) throws Exception {
    Path output = dir.resolve("index.scip");
    ScipAggregatorOptions options =
        new ScipAggregatorOptions(
            List.of(),
            output,
            dir,
            new ScipAggregatorReporter() {},
            ToolInfo.getDefaultInstance(),
            false,
            List.of(),
            false,
            true,
            false);
    try (ScipWriter writer = new ScipWriter(options)) {
      writer.build();
    }
    assertTrue(Files.exists(output));
  }
}
