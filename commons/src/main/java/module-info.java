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
module org.apache.isis.commons {
    exports org.apache.isis.commons.binding;
    exports org.apache.isis.commons.collections;
    exports org.apache.isis.commons.concurrent;
    exports org.apache.isis.commons.functional;
    exports org.apache.isis.commons.handler;
    exports org.apache.isis.commons.having;
    exports org.apache.isis.commons.resource;
    exports org.apache.isis.commons.internal;
    exports org.apache.isis.commons.internal.assertions;
    exports org.apache.isis.commons.internal.base;
    exports org.apache.isis.commons.internal.binding;
    exports org.apache.isis.commons.internal.codec;
    exports org.apache.isis.commons.internal.collections.snapshot;
    exports org.apache.isis.commons.internal.collections;
    exports org.apache.isis.commons.internal.compare;
    exports org.apache.isis.commons.internal.concurrent;
    exports org.apache.isis.commons.internal.context;
    exports org.apache.isis.commons.internal.debug.xray.graphics;
    exports org.apache.isis.commons.internal.debug.xray;
    exports org.apache.isis.commons.internal.debug;
    exports org.apache.isis.commons.internal.delegate;
    exports org.apache.isis.commons.internal.exceptions;
    exports org.apache.isis.commons.internal.factory;
    exports org.apache.isis.commons.internal.functions;
    exports org.apache.isis.commons.internal.graph;
    exports org.apache.isis.commons.internal.hardening;
    exports org.apache.isis.commons.internal.hash;
    exports org.apache.isis.commons.internal.html;
    exports org.apache.isis.commons.internal.image;
    exports org.apache.isis.commons.internal.ioc;
    exports org.apache.isis.commons.internal.memento;
    exports org.apache.isis.commons.internal.os;
    exports org.apache.isis.commons.internal.primitives;
    exports org.apache.isis.commons.internal.proxy;
    exports org.apache.isis.commons.internal.reflection;
    exports org.apache.isis.commons.internal.resources;
    exports org.apache.isis.commons.internal.testing;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.module.jaxb;
    requires transitive java.desktop;
    requires transitive java.sql;
    requires transitive java.xml;
    requires transitive java.xml.bind;
    requires transitive lombok;
    requires transitive org.apache.logging.log4j;
    requires transitive org.jdom2;
    requires transitive org.jsoup;
    requires transitive org.yaml.snakeyaml;
    requires transitive spring.beans;
    requires transitive spring.context;
    requires transitive spring.core;

    // JAXB JUnit test
    opens org.apache.isis.commons.internal.resources to java.xml.bind;

}