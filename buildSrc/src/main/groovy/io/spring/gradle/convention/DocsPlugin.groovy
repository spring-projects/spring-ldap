package io.spring.gradle.convention

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.PluginManager
import org.gradle.api.tasks.bundling.Zip

/**
 * Aggregates asciidoc, javadoc, and deploying of the docs into a single plugin
 */
public class DocsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {

		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(BasePlugin);
		pluginManager.apply("org.asciidoctor.jvm.convert");
		pluginManager.apply(JavadocApiPlugin);

		project.getGradle().afterProject(new Action<Project>() {
			@Override
			public void execute(Project p) {
				RepositoryHandler repositories = p.getRepositories();
				if (repositories.isEmpty()) {
					repositories.mavenCentral();
					repositories.maven(repo -> {
						repo.setUrl(URI.create("https://repo.spring.io/release"));
					});
				}
			}
		});

		Task docsZip = project.tasks.create('docsZip', Zip) {
			dependsOn 'api'
			group = 'Distribution'
			archiveBaseName = project.rootProject.name
			archiveClassifier = 'docs'
			description = "Builds -${archiveClassifier.get()} archive containing all " +
				"Docs for deployment at docs.spring.io"
			from(project.tasks.asciidoctor.outputs) {
				into 'reference'
				include '**'
			}
			from(project.tasks.api.outputs) {
				into 'api'
			}
			into 'docs'
			duplicatesStrategy 'exclude'
		}

		Task docs = project.tasks.create("docs") {
			group = 'Documentation'
			description 'An aggregator task to generate all the documentation'
			dependsOn docsZip
		}
		project.tasks.assemble.dependsOn docs
	}
}
