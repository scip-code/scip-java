package org.scip_code.scip_java.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;

public class WriteDependencies extends DefaultTask {

  private static final String CROSS_REPO_BANNER =
      "This will not prevent a SCIP index from being created, but the symbols\n"
          + "extracted from this project won't be available for cross-repository navigation,\n"
          + "as this project doesn't define any Maven coordinates by which it can be referred back"
          + " to.";

  // Set on the configurations of AGP's build variants, such as debugCompileClasspath.
  private static final String AGP_BUILD_TYPE = "com.android.build.api.attributes.BuildTypeAttr";

  @TaskAction
  public void printResolvedDependencies() throws IOException {
    Project project = getProject();

    // Always set by the scip-java Gradle init script.
    Path depsOut =
        Paths.get(project.getExtensions().getExtraProperties().get("dependenciesOut").toString());
    Files.createDirectories(depsOut.getParent());

    String projectName = project.getName();
    String projectPath = project.getPath().replaceAll("[^a-z0-9A-Z_-]", "_");

    // Write to a per-project file so multi-module builds don't collide or
    // corrupt each other via parallel appends. The CLI globs every file whose
    // name ends with "dependencies.txt".
    Path dependenciesPath =
        depsOut.getFileName().toString().endsWith("dependencies.txt")
            ? depsOut.resolveSibling(projectPath + "." + depsOut.getFileName())
            : depsOut;

    // LinkedHashSet keeps first-seen order while dropping duplicates.
    Set<String> deps = new LinkedHashSet<>();

    // List the project itself as a dependency so we can assign its Maven
    // coordinates to the symbols it defines.
    try {
      PublishingExtension publishing =
          project.getExtensions().findByType(PublishingExtension.class);
      if (publishing != null) {
        for (MavenPublication publication :
            publishing.getPublications().withType(MavenPublication.class)) {
          try {
            SourceSet main =
                project.getExtensions().getByType(SourceSetContainer.class).getByName("main");
            main.getOutput().getClassesDirs().getFiles().stream()
                .map(File::getAbsolutePath)
                .sorted()
                .limit(1)
                .forEach(
                    classesDirectory ->
                        deps.add(
                            String.join(
                                "\t",
                                publication.getGroupId(),
                                publication.getArtifactId(),
                                publication.getVersion(),
                                classesDirectory)));
          } catch (Exception exception) {
            String publicationName =
                String.join(
                    ":",
                    publication.getGroupId(),
                    publication.getArtifactId(),
                    publication.getVersion());
            getLogger()
                .warn(
                    "Failed to extract `main` source set from publication `"
                        + publicationName
                        + "` in project `"
                        + projectName
                        + "`.\n"
                        + CROSS_REPO_BANNER
                        + "\nHere's the raw error message:\n  \""
                        + exception.getMessage()
                        + "\"\nContinuing without cross-repository support.");
          }
        }
      }
    } catch (Exception exception) {
      getLogger()
          .warn(
              "Failed to extract Maven publication from the project `"
                  + projectName
                  + "`.\n"
                  + CROSS_REPO_BANNER
                  + "\nHere's the raw error message ("
                  + exception.getClass()
                  + "):\n  \""
                  + exception.getMessage()
                  + "\"\nContinuing without cross-repository support.");
    }

    // Iterate over a copy: resolving a configuration can add new ones (AGP 9
    // does), which would otherwise throw ConcurrentModificationException.
    new ArrayList<>(project.getConfigurations())
        .forEach(
            conf -> {
              if (conf.isCanBeResolved()) {
                try {
                  if (conf.getAttributes().keySet().stream()
                      .anyMatch(attribute -> attribute.getName().equals(AGP_BUILD_TYPE))) {
                    // AGP's variant classpaths don't request an artifact type,
                    // so resolving them directly can't choose between the
                    // variants of other Android modules, and yields .aar files
                    // that the indexer can't read. Ask for the jars AGP
                    // compiles against instead, as AGP itself does.
                    ArtifactCollection artifacts =
                        conf.getIncoming()
                            .artifactView(
                                view -> {
                                  view.attributes(
                                      attributes ->
                                          attributes.attribute(
                                              ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE,
                                              "android-classes-jar"));
                                  // Unlike the branch below, this leaves out
                                  // project dependencies on purpose: they have
                                  // no Maven coordinates of their own, and a
                                  // published project writes its own line from
                                  // its own task.
                                  view.componentFilter(
                                      id -> id instanceof ModuleComponentIdentifier);
                                  // So an artifact that can't be transformed
                                  // only skips itself, not the configuration.
                                  view.lenient(true);
                                })
                            .getArtifacts();
                    artifacts
                        .getFailures()
                        .forEach(
                            failure ->
                                getLogger()
                                    .warn(
                                        "Skipping an artifact of configuration '"
                                            + conf.getName()
                                            + "' due to resolution failure: "
                                            + failure.getMessage()));
                    artifacts.forEach(
                        artifact -> {
                          ModuleComponentIdentifier id =
                              (ModuleComponentIdentifier) artifact.getId().getComponentIdentifier();
                          deps.add(
                              String.join(
                                  "\t",
                                  id.getGroup(),
                                  id.getModule(),
                                  id.getVersion(),
                                  artifact.getFile().getAbsolutePath()));
                        });
                  } else {
                    conf.getResolvedConfiguration()
                        .getResolvedArtifacts()
                        .forEach(
                            artifact ->
                                deps.add(
                                    String.join(
                                        "\t",
                                        artifact.getModuleVersion().getId().getGroup(),
                                        artifact.getModuleVersion().getId().getName(),
                                        artifact.getModuleVersion().getId().getVersion(),
                                        artifact.getFile().getAbsolutePath())));
                  }
                } catch (Exception exc) {
                  getLogger()
                      .warn(
                          "Skipping configuration '"
                              + conf.getName()
                              + "' due to resolution failure: "
                              + exc.getMessage());
                }
              }
            });

    Files.write(dependenciesPath, deps, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
  }
}
