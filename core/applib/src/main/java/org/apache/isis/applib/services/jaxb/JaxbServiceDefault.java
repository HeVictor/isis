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
package org.apache.isis.applib.services.jaxb;

import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.domain.DomainObjectList;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.jaxbadapters.PersistentEntitiesAdapter;
import org.apache.isis.applib.jaxbadapters.PersistentEntityAdapter;

@Service
public class JaxbServiceDefault extends JaxbService.Simple {

    @Override
    public Object fromXml(final JAXBContext jaxbContext, final String xml, final Map<String, Object> unmarshallerProperties) {
        try {
            Object pojo = internalFromXml(jaxbContext, xml, unmarshallerProperties);

            if(pojo instanceof DomainObjectList) {

                // go around the loop again, so can properly deserialize the contents
                DomainObjectList list = (DomainObjectList) pojo;
                JAXBContext jaxbContextForList = jaxbContextFor(list);

                return internalFromXml(jaxbContextForList, xml, unmarshallerProperties);
            }

            return pojo;

        } catch (final JAXBException ex) {
            throw new NonRecoverableException("Error unmarshalling XML", ex);
        }
    }

    @Override
    protected JAXBContext jaxbContextFor(final Object domainObject) {
        final Class<?> domainClass = domainObject.getClass();
        if(domainObject instanceof DomainObjectList) {
            DomainObjectList list = (DomainObjectList) domainObject;
            try {
                final String elementObjectType = list.getElementObjectType();
                final Class<?> elementType = metaModelService.fromObjectType(elementObjectType);
                if (elementType.getAnnotation(XmlJavaTypeAdapter.class) == null) {
                    return JAXBContext.newInstance(domainClass, elementType);
                } else {
                    return JAXBContext.newInstance(domainClass);
                }
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        return super.jaxbContextFor(domainObject);
    }


    @Override
    protected void configure(final Unmarshaller unmarshaller) {
        unmarshaller.setAdapter(PersistentEntityAdapter.class,
                serviceInjector.injectServicesInto(new PersistentEntityAdapter()));
        unmarshaller.setAdapter(PersistentEntitiesAdapter.class,
                serviceInjector.injectServicesInto(new PersistentEntitiesAdapter()));
    }

    @Override
    protected void configure(final Marshaller marshaller) {
        marshaller.setAdapter(PersistentEntityAdapter.class,
                serviceInjector.injectServicesInto(new PersistentEntityAdapter()));
        marshaller.setAdapter(PersistentEntitiesAdapter.class,
                serviceInjector.injectServicesInto(new PersistentEntitiesAdapter()));
    }


    @Inject ServiceRegistry serviceRegistry;
    @Inject ServiceInjector serviceInjector;
    @Inject MetaModelService metaModelService;
}

