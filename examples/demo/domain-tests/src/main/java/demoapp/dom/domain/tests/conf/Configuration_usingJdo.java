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
package demoapp.dom.domain.tests.conf;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.persistence.jdo.datanucleus.IsisModulePersistenceJdoDatanucleus;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;

import demoapp.web.DemoAppManifestJdo;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({
    IsisModuleCoreRuntimeServices.class,
    IsisModulePersistenceJdoDatanucleus.class,
    IsisModuleTestingFixturesApplib.class,

    // demo domain
    DemoAppManifestJdo.class,
})
@PropertySources({
    @PropertySource(IsisPresets.NoTranslations),
    @PropertySource(IsisPresets.H2InMemory_withUniqueSchema),
})
@Profile("demo-jdo")
public class Configuration_usingJdo {

}
