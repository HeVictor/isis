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
module org.apache.isis.core.config {
    exports org.apache.isis.core.config;
    exports org.apache.isis.core.config.applib;
    exports org.apache.isis.core.config.beans.aoppatch;
    exports org.apache.isis.core.config.beans;
    exports org.apache.isis.core.config.converters;
    exports org.apache.isis.core.config.datasources;
    exports org.apache.isis.core.config.environment;
    exports org.apache.isis.core.config.messages;
    exports org.apache.isis.core.config.metamodel.facets;
    exports org.apache.isis.core.config.metamodel.services;
    exports org.apache.isis.core.config.metamodel.specloader;
    exports org.apache.isis.core.config.presets;
    exports org.apache.isis.core.config.progmodel;
    exports org.apache.isis.core.config.util;
    exports org.apache.isis.core.config.validators;
    exports org.apache.isis.core.config.viewer.web;

    requires jakarta.activation;
    requires java.annotation;
    requires java.persistence;
    requires java.sql;
    requires java.validation;
    requires java.ws.rs;
    requires java.inject;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.logging.log4j;
    requires org.eclipse.persistence.core;
    requires org.hibernate.validator;
    requires spring.aop;
    requires spring.beans;
    requires spring.boot;
    requires spring.context;
    requires spring.core;
    requires spring.tx;

    uses org.apache.isis.core.config.beans.IsisBeanTypeClassifier;

    opens org.apache.isis.core.config to spring.core, org.hibernate.validator;
    opens org.apache.isis.core.config.environment to spring.core;
}