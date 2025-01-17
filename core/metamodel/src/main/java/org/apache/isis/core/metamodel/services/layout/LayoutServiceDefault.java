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
package org.apache.isis.core.metamodel.services.layout;

import java.io.File;
import java.util.Objects;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.Marshaller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.menubars.MenuBars;
import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.layout.LayoutExportStyle;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.util.ZipWriter;
import org.apache.isis.commons.functional.Try;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.IsisModuleCoreMetamodel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named(IsisModuleCoreMetamodel.NAMESPACE + ".LayoutServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class LayoutServiceDefault implements LayoutService {

    private final SpecificationLoader specificationLoader;
    private final JaxbService jaxbService;
    private final GridService gridService;
    private final MenuBarsService menuBarsService;

    @Override
    public String toXml(final Class<?> domainClass, final LayoutExportStyle style) {
        final Grid grid = gridService.toGridForExport(domainClass, style);
        return gridToXml(grid);
    }

    @Override
    public byte[] toZip(final LayoutExportStyle style) {
        val domainObjectSpecs = specificationLoader.snapshotSpecifications()
        .filter(spec ->
                !spec.isAbstract()
                && (spec.isEntity() || spec.isViewModel()));

        val zipWriter = ZipWriter.ofFailureMessage("Unable to create zip of layouts");

        for (val objectSpec : domainObjectSpecs) {
            val domainClass = objectSpec.getCorrespondingClass();

            tryGridToXml(domainClass, style)
            .accept(failure->{
                log.warn("failed to generate layout XML for {}", domainClass);//, failure);
            },
            xmlIfAny->{
                xmlIfAny.ifPresent(xmlString->{
                    zipWriter.nextEntry(zipEntryNameFor(objectSpec), writer->
                        writer.writeCharactersUtf8(xmlString)
                    );
                });
            });
        }

        return zipWriter.toBytes();
    }

    @Override
    public String toMenuBarsXml(final MenuBarsService.Type type) {
        final MenuBars menuBars = menuBarsService.menuBars(type);

        return jaxbService.toXml(menuBars, _Maps.unmodifiable(
                Marshaller.JAXB_SCHEMA_LOCATION,
                menuBars.getTnsAndSchemaLocation()
                ));
    }

    // -- HELPER

    private Try<String> tryGridToXml(final Class<?> domainClass, final LayoutExportStyle style) {
        return Try.call(()->
            gridToXml(gridService.toGridForExport(domainClass, style)));
    }

    private String gridToXml(final @Nullable Grid grid) {
        if(grid==null) {
            return null;
        }
        return jaxbService.toXml(grid,
                _Maps.unmodifiable(
                        Marshaller.JAXB_SCHEMA_LOCATION,
                        Objects.requireNonNull(grid.getTnsAndSchemaLocation())
                        ));
    }

    private static String zipEntryNameFor(final ObjectSpecification objectSpec) {
        final String fqn = objectSpec.getFullIdentifier();
        return fqn.replace(".", File.separator)+".layout.xml";
    }

}
