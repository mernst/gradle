/*
 * Copyright 2007 the original author or authors.
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

package org.gradle.api.dependencies

import java.awt.Point
import org.apache.ivy.core.IvyPatternHelper
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor
import org.gradle.api.UnknownDependencyNotation
import org.gradle.api.internal.dependencies.DependencyDescriptorFactory
import org.gradle.api.internal.project.DefaultProject
import org.gradle.util.HelperUtil
import org.gradle.util.JUnit4GroovyMockery
import org.jmock.lib.legacy.ClassImposteriser
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/**
 * @author Hans Dockter
 */
class ModuleDependencyTest {
    static final Set TEST_CONF_SET = ['something']
    static final DefaultProject TEST_PROJECT = new DefaultProject()
    static final String TEST_DESCRIPTOR = "junit:junit:4.4"

    DependencyDescriptorFactory dependencyDescriptorFactoryMock
    ModuleDependency moduleDependency

    DefaultDependencyDescriptor expectedDependencyDescriptor

    JUnit4GroovyMockery context = new JUnit4GroovyMockery()

    @Before public void setUp() {
        context.setImposteriser(ClassImposteriser.INSTANCE)
        dependencyDescriptorFactoryMock = context.mock(DependencyDescriptorFactory)
        moduleDependency = new ModuleDependency(TEST_CONF_SET, TEST_DESCRIPTOR, TEST_PROJECT)
        moduleDependency.dependencyDescriptorFactory = dependencyDescriptorFactoryMock
        expectedDependencyDescriptor = HelperUtil.getTestDescriptor()
    }

    @Test public void testModuleDependency() {
        assertEquals(TEST_CONF_SET, moduleDependency.confs)
        assertEquals(TEST_DESCRIPTOR, moduleDependency.userDependencyDescription)
        assertSame(TEST_PROJECT, moduleDependency.project)
        assert !moduleDependency.force
    }

    @Test (expected = UnknownDependencyNotation) public void testSingleString() {
        new ModuleDependency(TEST_CONF_SET, "singlestring", TEST_PROJECT)
    }

    @Test (expected = UnknownDependencyNotation) public void testMissingVersion() {
        new ModuleDependency(TEST_CONF_SET, "junit:junit", TEST_PROJECT)
    }

    @Test (expected = UnknownDependencyNotation) public void testArtifactNotation() {
        new ModuleDependency(TEST_CONF_SET, "junit:junit:3.8.2@jar", TEST_PROJECT)
    }

    @Test (expected = UnknownDependencyNotation) public void testArtifactNotationWithClassifier() {
        new ModuleDependency(TEST_CONF_SET, "junit:junit:3.8.2:jdk14@jar", TEST_PROJECT)
    }

    @Test (expected = UnknownDependencyNotation) public void testUnknownType() {
        new ModuleDependency(TEST_CONF_SET, new Point(3, 4), TEST_PROJECT)
    }

    @Test public void testCreateDependencyDescriptor() {
        context.checking {
            one(dependencyDescriptorFactoryMock).createDescriptor(TEST_DESCRIPTOR, moduleDependency.force,
                    true, false, TEST_CONF_SET, moduleDependency.excludeRules); will(returnValue(expectedDependencyDescriptor))
        }
        assertSame(expectedDependencyDescriptor, moduleDependency.createDepencencyDescriptor())
    }

    @Test public void testExclude() {
        String expectedOrg = 'org'
        String expectedModule = 'module'
        String expectedOrg2 = 'org2'
        String expectedModule2 = 'module2'
        ModuleDependency moduleDependency = new ModuleDependency(TEST_CONF_SET, TEST_DESCRIPTOR, TEST_PROJECT)
        moduleDependency.exclude(org: expectedOrg, module: expectedModule)
        moduleDependency.exclude(org: expectedOrg2, module: expectedModule2)
        moduleDependency.force = true
        assertEquals(2, moduleDependency.excludeRules.size())
        assertEquals(moduleDependency.excludeRules[0].getAttribute(IvyPatternHelper.ORGANISATION_KEY),
                expectedOrg)
        assertEquals(moduleDependency.excludeRules[0].getAttribute(IvyPatternHelper.MODULE_KEY),
                expectedModule)
        assertEquals(moduleDependency.excludeRules[1].getAttribute(IvyPatternHelper.ORGANISATION_KEY),
                expectedOrg2)
        assertEquals(moduleDependency.excludeRules[1].getAttribute(IvyPatternHelper.MODULE_KEY),
                expectedModule2)
    }


}

