package org.springframework.gradle.maven;

import java.io.File;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

public class PublishLocalPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPlugins().withType(MavenPublishPlugin.class).all(new Action<>() {
			@Override
			public void execute(MavenPublishPlugin mavenPublish) {
				PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
				publishing.getRepositories().maven(new Action<>() {
					@Override
					public void execute(MavenArtifactRepository maven) {
						maven.setName("local");
						maven.setUrl(new File(project.getRootProject().getBuildDir(), "publications/repos"));
					}
				});
			}
		});
	}
}
