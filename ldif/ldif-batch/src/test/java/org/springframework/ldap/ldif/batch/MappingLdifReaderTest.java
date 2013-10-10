/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.ldif.batch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.AbstractJobTests;
import org.springframework.batch.test.AssertFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.net.MalformedURLException;

import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:applicationContext-test2.xml"})
public class MappingLdifReaderTest extends AbstractJobTests {
	private static Logger log = LoggerFactory.getLogger(MappingLdifReaderTest.class);
	
	private Resource expected;
	private Resource actual;
	
	public MappingLdifReaderTest() {
		try {
			expected = new UrlResource("file:src/test/resources/expectedOutput.ldif");
			actual = new UrlResource("file:target/test-outputs/output.ldif");
		} catch (MalformedURLException e) {
			log.error("Unexpected error", e);
		}
	}
	
	@Before
	public void checkFiles() {
		Assert.isTrue(expected.exists(), "Expected does not exist.");
	}
	
	@Test
	public void testValidRun() {
		try {
			JobExecution jobExecution = this.launchStep("step1");
			
			//Ensure job completed successfully.
			Assert.isTrue(jobExecution.getExitStatus().equals(ExitStatus.COMPLETED), "Step Execution did not complete normally: " + jobExecution.getExitStatus());

			//Check output.
			Assert.isTrue(actual.exists(), "Actual does not exist.");
			AssertFile.assertFileEquals(expected.getFile(), actual.getFile());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testResourceNotExists() {
		JobExecution jobExecution = this.launchStep("step2");
		
		Assert.isTrue(jobExecution.getExitStatus().getExitCode().equals("FAILED"), "The job exit status is not FAILED.");
		Assert.isTrue(jobExecution.getExitStatus().getExitDescription().contains("Failed to initialize the reader"), "The job failed for the wrong reason.");
	}
}
