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
module org.apache.isis.viewer.wicket.ui {
    exports org.apache.isis.viewer.wicket.ui.components.layout.bs.row;
    exports org.apache.isis.viewer.wicket.ui.components.entity.header;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.select2;
    exports org.apache.isis.viewer.wicket.ui.pages.common.datatables;
    exports org.apache.isis.viewer.wicket.ui.components.scalars.blobclob;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel;
    exports org.apache.isis.viewer.wicket.ui.util;
    exports org.apache.isis.viewer.wicket.ui.pages.common.serversentevents.js;
    exports org.apache.isis.viewer.wicket.ui.pages.mmverror;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.themepicker;
    exports org.apache.isis.viewer.wicket.ui.panels;
    exports org.apache.isis.viewer.wicket.ui.components.collectioncontents.summary;
    exports org.apache.isis.viewer.wicket.ui.pages.error;
    exports org.apache.isis.viewer.wicket.ui.components.scalars.composite;
    exports org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;
    exports org.apache.isis.viewer.wicket.ui.pages.common.fontawesome;
    exports org.apache.isis.viewer.wicket.ui.components.propertyheader;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.links;
    exports org.apache.isis.viewer.wicket.ui.components.voidreturn;
    exports org.apache.isis.viewer.wicket.ui.app.logout;
    exports org.apache.isis.viewer.wicket.ui.components.property;
    exports org.apache.isis.viewer.wicket.ui.pages.common.bootstrap.css;
    exports org.apache.isis.viewer.wicket.ui.components.empty;
    exports org.apache.isis.viewer.wicket.ui.components.scalars.passwd;
    exports org.apache.isis.viewer.wicket.ui.components.footer;
    exports org.apache.isis.viewer.wicket.ui.app.registry;
    exports org.apache.isis.viewer.wicket.ui.components.scalars.image;
    exports org.apache.isis.viewer.wicket.ui.components.standalonecollection;
    exports org.apache.isis.viewer.wicket.ui.components.header;
    exports org.apache.isis.viewer.wicket.ui.pages.common.viewer.js;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.zclip;
    exports org.apache.isis.viewer.wicket.ui.components.entity.icontitle;
    exports org.apache.isis.viewer.wicket.ui.components.tree;
    exports org.apache.isis.viewer.wicket.ui.components.scalars.choices;
    exports org.apache.isis.viewer.wicket.ui.components.layout.bs.tabs;
    exports org.apache.isis.viewer.wicket.ui.components.scalars.datepicker;
    exports org.apache.isis.viewer.wicket.ui.pages.login;
    exports org.apache.isis.viewer.wicket.ui.components;
    exports org.apache.isis.viewer.wicket.ui.components.unknown;
    exports org.apache.isis.viewer.wicket.ui.pages.common.sidebar.css;
    exports org.apache.isis.viewer.wicket.ui.components.collection.selector;
    exports org.apache.isis.viewer.wicket.ui.components.actionpromptsb;
    exports org.apache.isis.viewer.wicket.ui.components.about;
    exports org.apache.isis.viewer.wicket.ui.components.scalars.string;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs;
    exports org.apache.isis.viewer.wicket.ui.components.bookmarkedpages;
    exports org.apache.isis.viewer.wicket.ui.pages.common.livequery.js;
    exports org.apache.isis.viewer.wicket.ui.pages.voidreturn;
    exports org.apache.isis.viewer.wicket.ui.pages.accmngt.password_reset;
    exports org.apache.isis.viewer.wicket.ui.components.scalars;
    exports org.apache.isis.viewer.wicket.ui.pages;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.navbar;
    exports org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions;
    exports org.apache.isis.viewer.wicket.ui.pages.accmngt;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.checkbox;
    exports org.apache.isis.viewer.wicket.ui.pages.home;
    exports org.apache.isis.viewer.wicket.ui.components.collection;
    exports org.apache.isis.viewer.wicket.ui.components.layout.bs.col;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap;
    exports org.apache.isis.viewer.wicket.ui.components.actioninfo;
    exports org.apache.isis.viewer.wicket.ui.components.collection.bulk;
    exports org.apache.isis.viewer.wicket.ui.pages.entity;
    exports org.apache.isis.viewer.wicket.ui.components.scalars.bool;
    exports org.apache.isis.viewer.wicket.ui.pages.accmngt.register;
    exports org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;
    exports org.apache.isis.viewer.wicket.ui.components.entity.fieldset;
    exports org.apache.isis.viewer.wicket.ui.components.layout.bs;
    exports org.apache.isis.viewer.wicket.ui.components.tree.themes.bootstrap;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.entitysimplelink;
    exports org.apache.isis.viewer.wicket.ui.components.entity;
    exports org.apache.isis.viewer.wicket.ui.components.collectioncontents.unresolved;
    exports org.apache.isis.viewer.wicket.ui.components.value;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.buttons;
    exports org.apache.isis.viewer.wicket.ui.components.collectioncontents.multiple;
    exports org.apache.isis.viewer.wicket.ui.pages.standalonecollection;
    exports org.apache.isis.viewer.wicket.ui.components.tree.themes;
    exports org.apache.isis.viewer.wicket.ui.components.welcome;
    exports org.apache.isis.viewer.wicket.ui.pages.accmngt.signup;
    exports org.apache.isis.viewer.wicket.ui.validation;
    exports org.apache.isis.viewer.wicket.ui.components.actionprompt;
    exports org.apache.isis.viewer.wicket.ui.components.scalars.markup;
    exports org.apache.isis.viewer.wicket.ui.pages.about;
    exports org.apache.isis.viewer.wicket.ui.components.actionmenu;
    exports org.apache.isis.viewer.wicket.ui.pages.value;
    exports org.apache.isis.viewer.wicket.ui;
    exports org.apache.isis.viewer.wicket.ui.errors;
    exports org.apache.isis.viewer.wicket.ui.actionresponse;
    exports org.apache.isis.viewer.wicket.ui.components.collection.count;
    exports org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns;
    exports org.apache.isis.viewer.wicket.ui.components.layout.bs.clearfix;
    exports org.apache.isis.viewer.wicket.ui.components.collectioncontents.icons;
    exports org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent;
    exports org.apache.isis.viewer.wicket.ui.components.entity.collection;
    exports org.apache.isis.viewer.wicket.ui.components.scalars.value.fallback;
    exports org.apache.isis.viewer.wicket.ui.components.actions;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.github.openjson;
    requires commons.fileupload;
    requires glassfish.corba.omgapi;
    requires jakarta.activation;
    requires java.desktop;
    requires java.inject;
    requires java.sql;
    requires java.validation;
    requires javax.servlet.api;
    requires jquery.selectors;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.interaction;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.security.api;
    requires org.apache.isis.valuetypes.jodatime.integration;
    requires org.apache.isis.viewer.commons.applib;
    requires org.apache.isis.viewer.commons.model;
    requires org.apache.isis.viewer.commons.prism;
    requires org.apache.isis.viewer.commons.services;
    requires transitive org.apache.isis.viewer.wicket.model;
    requires org.apache.logging.log4j;
    requires org.apache.wicket.auth.roles;
    requires org.apache.wicket.core;
    requires org.apache.wicket.devutils;
    requires org.apache.wicket.extensions;
    requires org.apache.wicket.request;
    requires org.apache.wicket.util;
    requires org.danekja.jdk.serializable.functional;
    requires org.joda.time;
    requires org.slf4j;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires wicket.bootstrap.core;
    requires wicket.bootstrap.extensions;
    requires wicket.bootstrap.themes;
    requires wicket.webjars;
    requires wicketstuff.select2;

}