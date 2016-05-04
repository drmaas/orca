/*
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.config;

import com.netflix.spinnaker.orca.Task;
import com.netflix.spinnaker.orca.batch.SpringBatchExecutionRunner;
import com.netflix.spinnaker.orca.batch.ExecutionListenerProvider;
import com.netflix.spinnaker.orca.batch.TaskTaskletAdapter;
import com.netflix.spinnaker.orca.batch.listeners.SpringBatchExecutionListenerProvider;
import com.netflix.spinnaker.orca.listeners.ExecutionListener;
import com.netflix.spinnaker.orca.listeners.StageListener;
import com.netflix.spinnaker.orca.pipeline.ExecutionRunner;
import com.netflix.spinnaker.orca.pipeline.StageDefinitionBuilder;
import com.netflix.spinnaker.orca.pipeline.persistence.ExecutionRepository;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
@ComponentScan("com.netflix.spinnaker.orca.batch.legacy")
public class SpringBatchConfiguration {
  @Bean
  ExecutionRunner springBatchExecutionRunner(Collection<StageDefinitionBuilder> stageDefinitionBuilders,
                                             ExecutionRepository executionRepository,
                                             JobLauncher jobLauncher,
                                             JobRegistry jobRegistry,
                                             JobBuilderFactory jobBuilderFactory,
                                             StepBuilderFactory stepBuilderFactory,
                                             TaskTaskletAdapter taskTaskletAdapter,
                                             Collection<Task> tasks,
                                             ExecutionListenerProvider executionListenerProvider) {
    return new SpringBatchExecutionRunner(
      stageDefinitionBuilders,
      executionRepository,
      jobLauncher,
      jobRegistry,
      jobBuilderFactory,
      stepBuilderFactory,
      taskTaskletAdapter,
      tasks,
      executionListenerProvider
    );
  }

  @Bean
  ExecutionListenerProvider springBatchStepExecutionListenerProvider(ExecutionRepository executionRepository,
                                                                     Collection<StageListener> stageListeners,
                                                                     Collection<ExecutionListener> executionListeners) {
    return new SpringBatchExecutionListenerProvider(executionRepository, stageListeners, executionListeners);
  }
}
