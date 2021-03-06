/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.xd.dirt.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.xd.dirt.server.options.CommandLinePropertySourceOverridingListener;
import org.springframework.xd.dirt.server.options.SingleNodeOptions;
import org.springframework.xd.dirt.server.options.SingleNodeOptions.ControlTransport;
import org.springframework.xd.dirt.util.BannerUtils;

/**
 * Single Node XD Runtime.
 * 
 * @author Dave Syer
 * @author David Turanski
 */
public class SingleNodeApplication {

	private ConfigurableApplicationContext adminContext;

	private ConfigurableApplicationContext containerContext;

	@Value("${XD_CONTROL_TRANSPORT}")
	ControlTransport controlTransport;

	public static final String SINGLE_PROFILE = "single";

	public static void main(String[] args) {
		new SingleNodeApplication().run(args);
	}

	public SingleNodeApplication run(String... args) {

		System.out.println(BannerUtils.displayBanner(getClass().getSimpleName(), null));


		CommandLinePropertySourceOverridingListener<SingleNodeOptions> commandLineListener = new CommandLinePropertySourceOverridingListener<SingleNodeOptions>(
				new SingleNodeOptions());

		SpringApplicationBuilder admin =
				new SpringApplicationBuilder(SingleNodeOptions.class, ParentConfiguration.class,
						SingleNodeApplication.class)
						.listeners(commandLineListener)
						.profiles(AdminServerApplication.ADMIN_PROFILE, SINGLE_PROFILE)
						.child(SingleNodeOptions.class, AdminServerApplication.class)
						.listeners(commandLineListener);
		admin.run(args);

		SpringApplicationBuilder container = admin
				.sibling(SingleNodeOptions.class, ContainerServerApplication.class)
				.profiles(ContainerServerApplication.NODE_PROFILE, SINGLE_PROFILE)
				.listeners(commandLineListener)
				.web(false);
		container.run(args);

		adminContext = admin.context();
		containerContext = container.context();

		SingleNodeApplication singleNodeApp = adminContext.getBean(SingleNodeApplication.class);
		if (singleNodeApp.controlTransport == ControlTransport.local) {
			setUpControlChannels(adminContext, containerContext);
		}
		return this;
	}


	public void close() {
		if (containerContext != null) {
			containerContext.close();
		}
		if (adminContext != null) {
			adminContext.close();
			ApplicationContext parent = adminContext.getParent();
			if (parent instanceof ConfigurableApplicationContext) {
				((ConfigurableApplicationContext) parent).close();
			}
		}
	}

	public ConfigurableApplicationContext adminContext() {
		return adminContext;
	}

	public ConfigurableApplicationContext containerContext() {
		return containerContext;
	}

	private void setUpControlChannels(ApplicationContext adminContext,
			ApplicationContext containerContext) {

		MessageChannel containerControlChannel = containerContext.getBean(
				"containerControlChannel", MessageChannel.class);
		SubscribableChannel deployChannel = adminContext.getBean(
				"deployChannel", SubscribableChannel.class);
		SubscribableChannel undeployChannel = adminContext.getBean(
				"undeployChannel", SubscribableChannel.class);

		BridgeHandler handler = new BridgeHandler();
		handler.setOutputChannel(containerControlChannel);
		handler.setComponentName("xd.local.control.bridge");
		deployChannel.subscribe(handler);
		undeployChannel.subscribe(handler);
	}

}
