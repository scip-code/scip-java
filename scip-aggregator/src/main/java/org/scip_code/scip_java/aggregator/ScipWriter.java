package org.scip_code.scip_java.aggregator;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import org.scip_code.scip.Index;

/** High-level utility methods to write SCIP index into the SCIP output stream. */
public class ScipWriter implements AutoCloseable {

  private final Path tmp;
  private final ScipOutputStream output;
  private final ScipAggregatorOptions options;

  public ScipWriter(ScipAggregatorOptions options) throws IOException {
    this.tmp = createTempFile();
    this.output = new ScipOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)));
    this.options = options;
  }

  /**
   * Creates the temporary output file with {@code rw-r--r--} permissions where the file system
   * supports POSIX permissions. Other file systems (for example NTFS on Windows) reject the POSIX
   * attribute, so the file is created with the default permissions there.
   */
  private static Path createTempFile() throws IOException {
    if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
      FileAttribute<?> permissions =
          PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"));
      return Files.createTempFile("scip-aggregator", "index.scip", permissions);
    }
    return Files.createTempFile("scip-aggregator", "index.scip");
  }

  public void emitTyped(Index index) {
    this.output.write(index.toByteArray());
  }

  public void build() throws IOException {
    close();
    Files.move(tmp, options.output(), StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public void close() throws IOException {
    output.flush();
  }

  public void flush() {
    try {
      output.flush();
    } catch (IOException e) {
      options.reporter().error(e);
    }
  }
}
