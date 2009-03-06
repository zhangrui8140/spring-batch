/*
 * Copyright 2006-2007 the original author or authors.
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

package org.springframework.batch.core.explore.support;

import java.util.List;
import java.util.Set;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;

/**
 * 
 * <p>
 * Implementation of {@link JobExplorer} using the injected DAOs.
 * <p>
 * 
 * @author Dave Syer
 * @author Lucas Ward
 * 
 * @see JobExplorer
 * @see JobInstanceDao
 * @see JobExecutionDao
 * @see StepExecutionDao
 * @since 2.0
 */
public class SimpleJobExplorer implements JobExplorer {

	private JobInstanceDao jobInstanceDao;

	private JobExecutionDao jobExecutionDao;
	
	private StepExecutionDao stepExecutionDao;

	/**
	 * Provide default constructor with low visibility in case user wants to use
	 * use aop:proxy-target-class="true" for AOP interceptor.
	 */
	SimpleJobExplorer() {
	}

	public SimpleJobExplorer(JobInstanceDao jobInstanceDao, JobExecutionDao jobExecutionDao, StepExecutionDao stepExecutionDao) {
		super();
		this.jobInstanceDao = jobInstanceDao;
		this.jobExecutionDao = jobExecutionDao;
		this.stepExecutionDao = stepExecutionDao;
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.explore.JobExplorer#findJobExecutions(org.springframework.batch.core.JobInstance)
	 */
	public List<JobExecution> getJobExecutions(JobInstance jobInstance) {
		List<JobExecution> executions = jobExecutionDao.findJobExecutions(jobInstance);
		for(JobExecution jobExecution:executions){
			getJobExecutionDependencies(jobExecution);
		}
		return executions;
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.explore.JobExplorer#findRunningJobExecutions(java.lang.String)
	 */
	public Set<JobExecution> findRunningJobExecutions(String jobName) {
		Set<JobExecution> executions = jobExecutionDao.findRunningJobExecutions(jobName);
		for(JobExecution jobExecution:executions){
			getJobExecutionDependencies(jobExecution);
		}
		return executions;
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.explore.JobExplorer#getJobExecution(java.lang.Long)
	 */
	public JobExecution getJobExecution(Long executionId) {
		JobExecution jobExecution = jobExecutionDao.getJobExecution(executionId);
		if (jobExecution==null) {
			return null;
		}
		getJobExecutionDependencies(jobExecution);
		return jobExecution;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.batch.core.explore.JobExplorer#getStepExecution(java.lang.Long)
	 */
	public StepExecution getStepExecution(Long jobExecutionId, Long executionId) {
		return stepExecutionDao.getStepExecution(getJobExecution(jobExecutionId), executionId);
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.explore.JobExplorer#getJobInstance(java.lang.Long)
	 */
	public JobInstance getJobInstance(Long instanceId) {
		return jobInstanceDao.getJobInstance(instanceId);
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.explore.JobExplorer#getLastJobInstances(java.lang.String, int)
	 */
	public List<JobInstance> getJobInstances(String jobName, int start, int count) {
		return jobInstanceDao.getJobInstances(jobName, start, count);
	}

	/*
	 * Find all dependencies for a JobExecution, including JobInstance (which requires JobParameters)
	 * plus StepExecutions
	 */
	private void getJobExecutionDependencies(JobExecution jobExecution){
		
		JobInstance jobInstance = jobInstanceDao.getJobInstance(jobExecution);
		stepExecutionDao.addStepExecutions(jobExecution);
		jobExecution.setJobInstance(jobInstance);
	}

}