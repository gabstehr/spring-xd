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

package org.springframework.xd.dirt.stream.completion;

import static org.springframework.xd.module.ModuleType.processor;
import static org.springframework.xd.module.ModuleType.sink;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.xd.dirt.module.ModuleDefinitionRepository;
import org.springframework.xd.dirt.stream.XDParser;
import org.springframework.xd.dirt.stream.dsl.CheckpointedStreamDefinitionException;
import org.springframework.xd.module.ModuleDefinition;
import org.springframework.xd.module.ModuleType;
import org.springframework.xd.rest.client.domain.CompletionKind;

/**
 * Provides completions for the case where the user has entered a pipe symbol and a module reference is expected next.
 * 
 * @author Eric Bottard
 */
@Component
public class ModulesAfterPipeRecoveryStrategy extends
		StacktraceFingerprintingCompletionRecoveryStrategy<CheckpointedStreamDefinitionException> {


	private ModuleDefinitionRepository moduleDefinitionRepository;

	@Autowired
	public ModulesAfterPipeRecoveryStrategy(XDParser parser, ModuleDefinitionRepository moduleDefinitionRepository) {
		super(parser, "file | filter |");
		this.parser = parser;
		this.moduleDefinitionRepository = moduleDefinitionRepository;
	}

	@Override
	public void addProposals(String start, CheckpointedStreamDefinitionException exception, CompletionKind kind,
			List<String> proposals) {

		addAllModulesOfType(start.endsWith(" ") ? start : start + " ", processor, proposals);
		addAllModulesOfType(start.endsWith(" ") ? start : start + " ", sink, proposals);
	}

	private void addAllModulesOfType(String beginning, ModuleType type, List<String> results) {
		Page<ModuleDefinition> mods = moduleDefinitionRepository.findByType(new PageRequest(0, 1000), type);
		for (ModuleDefinition mod : mods) {
			results.add(beginning + mod.getName());
		}
	}


}
