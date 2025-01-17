/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.config.metamodel.facets;

import org.apache.isis.core.config.IsisConfiguration;

import lombok.NonNull;

public final class ParameterConfigOptions {

    public static enum DependentDefaultsPolicy {
        /**
         * Allows user provided parameters in the UI to be overwritten via defaults semantics.
         */
        UPDATE_DEPENDENT,
        /**
         * Forbids user provided parameters in the UI to be overwritten via defaults semantics.
         */
        PRESERVE_CHANGES;
        public boolean isUpdateDependent() { return this == UPDATE_DEPENDENT; }
        public boolean isPreserveChanges() { return this == PRESERVE_CHANGES; }

        public static DependentDefaultsPolicy defaultsIfNotSpecifiedOtherwise() {
            return UPDATE_DEPENDENT; // backwards compatibility
        }
    }


    // -- FACTORIES

    public static DependentDefaultsPolicy dependentDefaultsPolicy(
            final @NonNull IsisConfiguration configuration) {
        return configuration.getApplib().getAnnotation().getParameter().getDependentDefaultsPolicy();
    }


}
