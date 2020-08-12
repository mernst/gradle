/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.instantexecution.serialization.codecs.transform

import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.internal.artifacts.transform.ArtifactTransformActionScheme
import org.gradle.api.internal.artifacts.transform.ArtifactTransformParameterScheme
import org.gradle.api.internal.artifacts.transform.DefaultTransformer
import org.gradle.api.internal.attributes.ImmutableAttributes
import org.gradle.api.internal.file.FileCollectionFactory
import org.gradle.api.internal.file.FileLookup
import org.gradle.api.internal.initialization.RootScriptDomainObjectContext
import org.gradle.api.tasks.FileNormalizer
import org.gradle.instantexecution.serialization.Codec
import org.gradle.instantexecution.serialization.ReadContext
import org.gradle.instantexecution.serialization.WriteContext
import org.gradle.instantexecution.serialization.readClassOf
import org.gradle.instantexecution.serialization.readNonNull
import org.gradle.internal.hash.ClassLoaderHierarchyHasher
import org.gradle.internal.hash.HashCode
import org.gradle.internal.isolation.Isolatable
import org.gradle.internal.isolation.IsolatableFactory
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.internal.service.ServiceRegistry
import org.gradle.internal.snapshot.ValueSnapshotter


internal
class DefaultTransformerCodec(
    private val buildOperationExecutor: BuildOperationExecutor,
    private val classLoaderHierarchyHasher: ClassLoaderHierarchyHasher,
    private val isolatableFactory: IsolatableFactory,
    private val valueSnapshotter: ValueSnapshotter,
    private val fileCollectionFactory: FileCollectionFactory,
    private val fileLookup: FileLookup,
    private val parameterScheme: ArtifactTransformParameterScheme,
    private val actionScheme: ArtifactTransformActionScheme
) : Codec<DefaultTransformer> {

    override suspend fun WriteContext.encode(value: DefaultTransformer) {
        writeClass(value.implementationClass)
        write(value.fromAttributes)
        writeClass(value.inputArtifactNormalizer)
        writeClass(value.inputArtifactDependenciesNormalizer)
        writeBoolean(value.isCacheable)

        // TODO - isolate now and discard node, if isolation is scheduled and has no dependencies
        // Write isolated parameters, if available, and discard the parameters
        if (value.isIsolated) {
            writeBoolean(true)
            writeBinary(value.isolatedParameters.secondaryInputsHash.toByteArray())
            write(value.isolatedParameters.isolatedParameterObject)
        } else {
            writeBoolean(false)
            write(value.parameterObject)
        }
    }

    override suspend fun ReadContext.decode(): DefaultTransformer? {
        val implementationClass = readClassOf<TransformAction<*>>()
        val fromAttributes = readNonNull<ImmutableAttributes>()
        val inputArtifactNormalizer = readClassOf<FileNormalizer>()
        val inputArtifactDependenciesNormalizer = readClassOf<FileNormalizer>()
        val isCacheable = readBoolean()

        val isolated = readBoolean()
        val parametersObject: TransformParameters?
        val isolatedParametersObject: DefaultTransformer.IsolatedParameters?
        if (isolated) {
            parametersObject = null
            val secondaryInputsHash = HashCode.fromBytes(readBinary())
            val isolatedParameters = readNonNull<Isolatable<TransformParameters>>()
            isolatedParametersObject = DefaultTransformer.IsolatedParameters(isolatedParameters, secondaryInputsHash)
        } else {
            parametersObject = read() as TransformParameters?
            isolatedParametersObject = null
        }
        return DefaultTransformer(
            implementationClass,
            parametersObject,
            isolatedParametersObject,
            fromAttributes,
            inputArtifactNormalizer,
            inputArtifactDependenciesNormalizer,
            isCacheable,
            buildOperationExecutor,
            classLoaderHierarchyHasher,
            isolatableFactory,
            valueSnapshotter,
            fileCollectionFactory,
            fileLookup,
            parameterScheme.inspectionScheme.propertyWalker,
            actionScheme.instantiationScheme,
            RootScriptDomainObjectContext.INSTANCE.model,
            isolate.owner.service(ServiceRegistry::class.java)
        )
    }
}
