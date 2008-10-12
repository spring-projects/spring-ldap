package org.springframework.ldap;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;

import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;

public abstract class AbstractEc2InstanceLaunchingFactoryBean extends AbstractFactoryBean {
	private static final int SLEEP_TIME = 1000;

	private static final Log log = LogFactory.getLog(AbstractEc2InstanceLaunchingFactoryBean.class);

	private String imageName;

	private String awsKey;

	private String awsSecretKey;

	private String keyName;

	private String groupName;

	private Instance instance;

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public void setAwsKey(String awsKey) {
		this.awsKey = awsKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	protected final Object createInstance() throws Exception {
		Assert.hasLength(imageName, "ImageName must be set");
		Assert.hasLength(awsKey, "AwsKey must be set");
		Assert.hasLength(awsSecretKey, "AwsSecretKey must be set");
		Assert.hasLength(keyName, "KeyName must be set");
		Assert.hasLength(groupName, "GroupName must be set");

		log.info("Launching EC2 instance for image: " + imageName);

		Jec2 jec2 = new Jec2(awsKey, awsSecretKey);
		LaunchConfiguration launchConfiguration = new LaunchConfiguration(imageName);
		launchConfiguration.setKeyName(keyName);
		launchConfiguration.setSecurityGroup(Collections.singletonList(groupName));

		ReservationDescription reservationDescription = jec2.runInstances(launchConfiguration);
		instance = reservationDescription.getInstances().get(0);
		while (!instance.isRunning() && !instance.isTerminated()) {
			log.info("Instance still starting up; sleeping " + SLEEP_TIME + "ms");
			Thread.sleep(SLEEP_TIME);
			reservationDescription = jec2.describeInstances(Collections.singletonList(instance.getInstanceId())).get(0);
			instance = reservationDescription.getInstances().get(0);
		}

		if (instance.isRunning()) {
			log.info("EC2 instance is now running");
			return doCreateInstance(instance.getDnsName());
		}
		else {
			throw new IllegalStateException("Failed to start a new instance");
		}

	}

	protected abstract Object doCreateInstance(String dnsName) throws Exception;

	@Override
	protected void destroyInstance(Object ignored) throws Exception {
		if (this.instance != null) {
			log.info("Shutting down instance");
			Jec2 jec2 = new Jec2(awsKey, awsSecretKey);
			jec2.terminateInstances(Collections.singletonList(this.instance.getInstanceId()));
		}

	}
}
