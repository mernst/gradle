/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.internal.io

import spock.lang.Specification

import static org.gradle.internal.io.IoConsumer.wrap

class IoConsumerTest extends Specification {

    def "executed when it doesn't throw"() {
        when:
        String encountered = null
        wrap({ String payload -> encountered = payload }).accept("lajos")
        then:
        encountered == "lajos"
    }

    def "can throw RuntimeException directly"() {
        when:
        wrap({ String payload -> throw new RuntimeException(payload) }).accept("lajos")
        then:
        def runtimeEx = thrown RuntimeException
        runtimeEx.message == "lajos"
    }

    def "can throw IOException, but it gets wrapped"() {
        when:
        wrap({ String payload -> throw new IOException(payload) }).accept("lajos")
        then:
        def ioEx = thrown UncheckedIOException
        ioEx.cause instanceof IOException
        ioEx.cause.message == "lajos"
    }
}
