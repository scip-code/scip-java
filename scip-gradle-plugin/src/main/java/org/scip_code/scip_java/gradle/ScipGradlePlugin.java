package org.scip_code.scip_java.gradle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.tasks.compile.JavaCompile;

public class ScipGradlePlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.afterEvaluate(this::configureProject);
  }

  private void configureProject(Project project) {
    // See https://github.com/gradle/gradle/issues/27260
    Settings settings = ((GradleInternal) (project.getGradle())).getSettings();

    if (settings.getDependencyResolutionManagement().getRepositories().isEmpty()) {
      // Inject Maven Central/local so the indexer (and plugins like protobuf that
      // resolve their own artifacts) can resolve dependencies even when the build
      // being indexed doesn't declare any repositories of its own.
      project.getRepositories().add(project.getRepositories().mavenCentral());
      project.getRepositories().add(project.getRepositories().mavenLocal());
    } else {
      // repositories are declared in settings instead, so the injection isn't needed (issue #847).
      project.getLogger().info("scip-java: not injecting Maven Central/local repositories");
    }

    Map<String, Object> extraProperties =
        project.getExtensions().getExtraProperties().getProperties();

    String targetRoot = requiredExtra(extraProperties, "scipTarget").toString();
    String sourceRoot = project.getRootDir().toString();

    // Compilation tasks we need to trigger to index all the sources we care
    // about. Built up as we detect the java and kotlin plugins.
    List<String> triggers = new ArrayList<>();

    if (project.getPlugins().hasPlugin("java")) {
      triggers.add("compileJava");
      triggers.add("compileTestJava");

      Object javacPluginDep = project.files(requiredExtra(extraProperties, "javacPluginJar"));
      boolean pluginAdded = tryAddJavacPlugin(project, javacPluginDep);

      project
          .getTasks()
          .withType(JavaCompile.class)
          .configureEach(
              task -> {
                // Disable incremental compilation so the random timestamp added
                // below forces a full recompile and Gradle doesn't cache stale
                // SCIP state.
                task.getOptions().setIncremental(false);

                if (pluginAdded) {
                  // Groovy build scripts can append interpolated strings
                  // (GString) to compilerArgs, so despite the List<String>
                  // signature the list may hold non-String elements. Iterate
                  // through a wildcard reference so elements are not implicitly
                  // cast to String, which throws ClassCastException (issue
                  // #801).
                  List<?> args = task.getOptions().getCompilerArgs();

                  // It's important we don't add the plugin configuration more
                  // than once, as javac considers that an error.
                  boolean alreadyAdded =
                      args.stream()
                          .anyMatch(arg -> String.valueOf(arg).startsWith("-Xplugin:scip"));
                  if (!alreadyAdded) {
                    // The random timestamp ensures the sources are _always_
                    // recompiled, so Gradle doesn't cache any state.
                    // TODO: before this plugin is published to Maven Central, we
                    // will need to revert this change - as it can have
                    // detrimental effect on people's builds.
                    task.getOptions()
                        .getCompilerArgs()
                        .add(
                            "-Xplugin:scip -targetroot:"
                                + targetRoot
                                + " -sourceroot:"
                                + sourceRoot
                                + " -randomtimestamp="
                                + System.nanoTime());
                  }
                }
              });
    }

    boolean isKotlinMultiplatform = false;
    for (Plugin<?> plugin : project.getPlugins()) {
      if (plugin.getClass().getName().contains("KotlinMultiplatform")) {
        isKotlinMultiplatform = true;
        break;
      }
    }

    if (project.getPlugins().hasPlugin("kotlin") || isKotlinMultiplatform) {
      if (isKotlinMultiplatform) {
        triggers.add("compileKotlinJvm");
        triggers.add("compileTestKotlinJvm");
      } else {
        triggers.add("compileKotlin");
        triggers.add("compileTestKotlin");
      }

      project
          .getTasks()
          .configureEach(task -> configureKotlinCompileTask(task, sourceRoot, targetRoot));
    }

    project.getTasks().create("scipCompileAll").dependsOn(triggers);

    project.getTasks().create("scipPrintDependencies", WriteDependencies.class);
  }

  /**
   * javac discovers {@code -Xplugin:} plugins from the annotation processor path when {@code
   * -processorpath} is set, and only falls back to the classpath when it isn't. Gradle populates
   * {@code -processorpath} from the {@code annotationProcessor} (main) and {@code
   * testAnnotationProcessor} (test) configurations, so whenever one of them declares a processor we
   * must add the SCIP javac plugin to that same configuration or the corresponding compile task
   * fails with "plug-in not found: scip". The two configurations are independent (test does not
   * extend main), so each is checked separately.
   */
  private static boolean hasAnnotationProcessors(Project project, String configurationName) {
    try {
      Configuration config = project.getConfigurations().getByName(configurationName);
      return config.isCanBeResolved() && !config.getDependencies().isEmpty();
    } catch (Exception exc) {
      return false;
    }
  }

  private static boolean tryAddJavacPlugin(Project project, Object javacPluginDep) {
    try {
      project.getDependencies().add("compileOnly", javacPluginDep);
      project.getDependencies().add("testCompileOnly", javacPluginDep);
      if (hasAnnotationProcessors(project, "annotationProcessor")) {
        project.getDependencies().add("annotationProcessor", javacPluginDep);
      }
      if (hasAnnotationProcessors(project, "testAnnotationProcessor")) {
        project.getDependencies().add("testAnnotationProcessor", javacPluginDep);
      }
      return true;
    } catch (Exception exc) {
      // The `compileOnly` configuration has likely already been resolved by
      // another plugin or buildscript, so we can no longer add new dependencies
      // to it. The project will be skipped (no SCIP output) and the post-build
      // check in `GradleBuildTool` will surface a clearer error.
      project
          .getLogger()
          .warn(
              "scip-java: failed to attach SCIP compiler plugin to project '"
                  + project.getName()
                  + "' ("
                  + exc.getClass().getSimpleName()
                  + ": "
                  + exc.getMessage()
                  + "). This subproject will not be indexed.");
      return false;
    }
  }

  /**
   * Kotlin sources are no longer indexed inside kotlinc: after each Kotlin compile task runs, its
   * sources and classpath are dumped to {@code <targetroot>/kotlin-configs/<task>.txt}, and the
   * scip-java CLI indexes them with the standalone Analysis API indexer after the build. This
   * removes any binary-compatibility coupling with the Kotlin compiler version of the build being
   * indexed.
   */
  private static void configureKotlinCompileTask(Task task, String sourceRoot, String targetRoot) {
    if (!task.getClass().getSimpleName().contains("KotlinCompile")) {
      return;
    }

    task.doLast(
        ignored -> {
          try {
            List<String> lines = new ArrayList<>();
            lines.add("sourceroot " + sourceRoot);

            // Referring to KotlinCompile directly triggers NoClassDefFoundError -
            // the plugin classpath is murky and we deliberately don't bundle the
            // Kotlin Gradle plugin. So we commit the sins of reflection.
            org.gradle.api.file.FileCollection libraries;
            try {
              libraries =
                  (org.gradle.api.file.FileCollection)
                      task.getClass().getMethod("getLibraries").invoke(task);
            } catch (ReflectiveOperationException exc) {
              libraries =
                  (org.gradle.api.file.FileCollection)
                      task.getClass().getMethod("getClasspath").invoke(task);
            }
            for (java.io.File entry : libraries.getFiles()) {
              lines.add("classpath " + entry.getAbsolutePath());
            }

            int sources = 0;
            for (java.io.File file : task.getInputs().getSourceFiles().getFiles()) {
              if (file.getName().endsWith(".kt") || file.getName().endsWith(".kts")) {
                lines.add("source " + file.getAbsolutePath());
                sources++;
              }
            }
            if (sources == 0) {
              return;
            }

            java.nio.file.Path configDir = java.nio.file.Paths.get(targetRoot, "kotlin-configs");
            java.nio.file.Files.createDirectories(configDir);
            String fileName = task.getPath().replaceAll("[^A-Za-z0-9]", "-") + ".txt";
            java.nio.file.Files.write(configDir.resolve(fileName), lines);
          } catch (Exception exc) {
            throw new RuntimeException(
                "scip-java: failed to extract Kotlin compile configuration for task '"
                    + task.getName()
                    + "': "
                    + exc,
                exc);
          }
        });
  }

  private static Object requiredExtra(Map<String, Object> extraProperties, String name) {
    Object value = extraProperties.get(name);
    if (value == null) {
      throw new IllegalStateException(
          name + " extra property must be set by the scip-java Gradle init script");
    }
    return value;
  }
}
