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
package org.springframework.ldap.configuration;

import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.ldap.ldif.batch.LdifAggregator;
import org.springframework.ldap.ldif.batch.MappingLdifReader;
import org.springframework.ldap.ldif.batch.MyMapper;


/**
 * Generated Java based configuration
 */
@Configuration
public class Batch20ApplicationContextTest2 {


    @Bean("itemReader1")
    public MappingLdifReader itemReader1(@Qualifier("recordMapper")
                                                 MyMapper recordMapper) {
        MappingLdifReader bean = new MappingLdifReader();
        bean.setResource(new FileSystemResource("src/test/resources/test.ldif"));
        bean.setRecordsToSkip(1);
        bean.setRecordMapper(recordMapper);
        return bean;
    }

    @Bean("taskExecutor")
    public SyncTaskExecutor taskExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean("jobLauncher")
    public SimpleJobLauncher jobLauncher(
            @Qualifier("jobRepository")
                    MapJobRepositoryFactoryBean jobRepository,
            @Qualifier("taskExecutor")
                    SyncTaskExecutor taskExecutor) throws Exception {
        SimpleJobLauncher bean = new SimpleJobLauncher();
        bean.setJobRepository((JobRepository) jobRepository.getObject());
        bean.setTaskExecutor(taskExecutor);
        return bean;
    }

    @Bean("itemReader2")
    public MappingLdifReader itemReader2(
            @Qualifier("recordMapper")
                    MyMapper recordMapper) {
        MappingLdifReader bean = new MappingLdifReader();
        bean.setResource(new FileSystemResource("src/test/resources/missing.ldif"));
        bean.setRecordsToSkip(1);
        bean.setRecordMapper(recordMapper);
        return bean;
    }

    @Bean("itemWriter")
    public FlatFileItemWriter itemWriter() {
        FlatFileItemWriter bean = new FlatFileItemWriter();
        bean.setResource(new FileSystemResource("target/test-outputs/output.ldif"));
        bean.setLineAggregator(new LdifAggregator());
        return bean;
    }

    @Bean("jobRepository")
    public MapJobRepositoryFactoryBean jobRepository(
            @Qualifier("transactionManager") ResourcelessTransactionManager transactionManager) {
        MapJobRepositoryFactoryBean bean = new MapJobRepositoryFactoryBean();
        bean.setTransactionManager(transactionManager);
        return bean;
    }

    @Bean("recordMapper")
    public MyMapper recordMapper() {
        return new MyMapper();
    }

}
