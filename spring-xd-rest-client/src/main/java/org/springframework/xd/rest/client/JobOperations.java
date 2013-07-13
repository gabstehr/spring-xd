/*
 * Copyright 2013 the original author or authors.
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

package org.springframework.xd.rest.client;

import java.util.List;

import org.springframework.xd.rest.client.domain.JobDefinitionResource;

/**
 * Interface defining operations available against streams.
 *
 * @author Glenn Renfro
 */
public interface JobOperations {

	/**
	 * Create a new Job, optionally deploying it.
	 */
	public JobDefinitionResource createJob(String name, String defintion, Boolean deploy);
	/**
	 * Destroy an existing stream.
	 */
	public void destroyJob(String name);

	/**
	 * Deploy an already created stream.
	 */
	public void deployJob(String name);

	/**
	 * Undeploy a deployed stream, retaining its definition.
	 */
	public void undeployJob(String name);

	/**
	 * List streams known to the system.
	 */
	public List<JobDefinitionResource> list();
}
