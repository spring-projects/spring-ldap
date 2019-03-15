/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.test;

import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * Abstract FactoryBean superclass to use for automatically launching an EC2 instance before creating the actual target object.
 * This approach is particularly useful for integration testing purposes - the idea is to have particular EC2 images prepared
 * for running integration tests against certain server configurations, enabling integration tests aimed at e.g. a particluar
 * DB server to run transparently at the computer of each individual developer without having to have the actual server software
 * installed on their computers.
 * <p>
 * Public AMIs will need to be created, bundled and registered for each server setup. A subclass of this FactoryBean
 * is then added to create the actual target object (e.g. a DataSource), implementing the {link #doCreateInstance} method.
 * This method will be supplied the IP address of the instance that was created, enabling the subclass to configure the
 * created instance appropriately.
 * 
 * @author Mattias Hellborg Arthursson
 */
public abstract class AbstractEc2InstanceLaunchingFactoryBean extends AbstractFactoryBean {
    private static final int INSTANCE_START_SLEEP_TIME = 1000;

    private static final long DEFAULT_PREPARATION_SLEEP_TIME = 30000;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractEc2InstanceLaunchingFactoryBean.class);

    private String imageName;

    private String awsKey;

    private String awsSecretKey;

    private String keypairName;

    private String groupName;

    private Instance instance;

    private long preparationSleepTime = DEFAULT_PREPARATION_SLEEP_TIME;

    /**
     * Set the name of the AMI image to be launched.
     *
     * @param imageName the AMI image name.
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * Set the AWS key.
     *
     * @param awsKey the AWS key.
     */
    public void setAwsKey(String awsKey) {
        this.awsKey = awsKey;
    }

    /**
     * Set the AWS secret key.
     *
     * @param awsSecretKey the aws secret key.
     */
    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    /**
     * Set the name of the keypair.
     *
     * @param keypairName The keypair name.
     */
    public void setKeypairName(String keypairName) {
        this.keypairName = keypairName;
    }

    /**
     * Set the name of the access group. This group should be configured with the appropriate ports open for this test case to execute.
     *
     * @param groupName the group name.
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    protected final Object createInstance() throws Exception {
        Assert.hasLength(imageName, "ImageName must be set");
        Assert.hasLength(awsKey, "AwsKey must be set");
        Assert.hasLength(awsSecretKey, "AwsSecretKey must be set");
        Assert.hasLength(keypairName, "KeyName must be set");
        Assert.hasLength(groupName, "GroupName must be set");

        LOG.info("Launching EC2 instance for image: " + imageName);

        Jec2 jec2 = new Jec2(awsKey, awsSecretKey);
        LaunchConfiguration launchConfiguration = new LaunchConfiguration(imageName);
        launchConfiguration.setKeyName(keypairName);
        launchConfiguration.setSecurityGroup(Collections.singletonList(groupName));

        ReservationDescription reservationDescription = jec2.runInstances(launchConfiguration);
        instance = reservationDescription.getInstances().get(0);
        while (!instance.isRunning() && !instance.isTerminated()) {
            LOG.info("Instance still starting up; sleeping " + INSTANCE_START_SLEEP_TIME + "ms");
            Thread.sleep(INSTANCE_START_SLEEP_TIME);
            reservationDescription = jec2.describeInstances(Collections.singletonList(instance.getInstanceId())).get(0);
            instance = reservationDescription.getInstances().get(0);
        }

        if (instance.isRunning()) {
            LOG.info("EC2 instance is now running");
            if (preparationSleepTime > 0) {
                LOG.info("Sleeping " + preparationSleepTime + "ms allowing instance services to start up properly.");
                Thread.sleep(preparationSleepTime);
                LOG.info("Instance prepared - proceeding");
            }
            return doCreateInstance(instance.getDnsName());
        } else {
            throw new IllegalStateException("Failed to start a new instance");
        }

    }

    /**
     * Implement to create the actual target object.
     *
     * @param ip the ip address of the launched EC2 image.
     * @return the object to be returned by this FactoryBean.
     * @throws Exception if an error occurs during initialization.
     */
    protected abstract Object doCreateInstance(String ip) throws Exception;

    @Override
    protected void destroyInstance(Object ignored) throws Exception {
        if (this.instance != null) {
            LOG.info("Shutting down instance");
            Jec2 jec2 = new Jec2(awsKey, awsSecretKey);
            jec2.terminateInstances(Collections.singletonList(this.instance.getInstanceId()));
        }

    }
}
