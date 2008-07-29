/*
 * Copyright 2007-2008 the original author or authors.
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
package org.gradle.api.internal.dependencies;

import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.publish.PublishEngine;
import org.gradle.api.dependencies.ResolverContainer;

import java.io.File;
import java.util.List;

/**
 * @author Hans Dockter
 */
public interface IDependencyPublisher {
    void publish(List<String> configurations,
                            ResolverContainer resolvers,
                            ModuleDescriptor moduleDescriptor,
                            boolean uploadModuleDescriptor,
                            File ivyFile,
                            BaseDependencyManager dependencyManager,
                            PublishEngine publishEngine);
}
