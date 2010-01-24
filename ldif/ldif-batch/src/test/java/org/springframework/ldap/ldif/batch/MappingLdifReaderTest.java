package org.springframework.ldap.ldif.batch;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.AbstractJobTests;
import org.springframework.batch.test.AssertFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:applicationContext-test2.xml"})
public class MappingLdifReaderTest extends AbstractJobTests {
	private static Log log = LogFactory.getLog(MappingLdifReaderTest.class);
	
	private Resource expected;
	private Resource actual;
	
	public MappingLdifReaderTest() {
		try {
			expected = new UrlResource("file:src/test/resources/expectedOutput.ldif");
			actual = new UrlResource("file:target/test-outputs/output.ldif");
		} catch (MalformedURLException e) {
			log.error(e);
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
