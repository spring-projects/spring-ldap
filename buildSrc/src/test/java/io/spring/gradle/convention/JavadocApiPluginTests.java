/*
 * Copyright 2006-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.spring.gradle.convention;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rob Winch
 */
public class JavadocApiPluginTests {
	Project rootProject;

	@AfterEach
	public void cleanup() throws Exception {
		if (rootProject != null) {
			FileUtils.deleteDirectory(rootProject.getProjectDir());
		}
	}

	@Test
	public void applyWhenNotOverrideThenPropertiesDefaulted() {
		rootProject = ProjectBuilder.builder().build();
		rootProject.getPlugins().apply(JavadocApiPlugin.class);

		Javadoc apiTask = (Javadoc) rootProject.getTasks().getByPath("api");

		assertThat(apiTask).isNotNull();
		assertThat(apiTask.getGroup()).isEqualTo("Documentation");
		assertThat(apiTask.getDescription()).isEqualTo("Generates aggregated Javadoc API documentation.");
		assertThat(apiTask.getMaxMemory()).isEqualTo("1024m");
		assertThat(apiTask.getDestinationDir()).isEqualTo(new File(rootProject.getBuildDir(), "api"));
	}

}
