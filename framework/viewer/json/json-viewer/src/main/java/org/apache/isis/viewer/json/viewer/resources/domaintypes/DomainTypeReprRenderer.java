/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import java.util.List;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.Rel;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;
import org.codehaus.jackson.node.NullNode;

import com.google.common.base.Strings;

public class DomainTypeReprRenderer extends ReprRendererAbstract<DomainTypeReprRenderer, ObjectSpecification> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.DOMAIN_TYPE);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, LinkFollower linkFollower, JsonRepresentation representation) {
            return new DomainTypeReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    public static LinkBuilder newLinkToBuilder(ResourceContext resourceContext, Rel rel, ObjectSpecification objectSpec) {
        String typeFullName = objectSpec.getFullIdentifier();
        String url = "domainTypes/" + typeFullName;
        return LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.DOMAIN_TYPE, url);
    }

    private ObjectSpecification objectSpecification;

    public DomainTypeReprRenderer(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    public DomainTypeReprRenderer with(ObjectSpecification objectSpecification) {
        this.objectSpecification = objectSpecification;
        return cast(this);
    }

    public JsonRepresentation render() {

        if(objectSpecification == null) {
            throw new IllegalStateException("ObjectSpecification not specified");
        } 

        // self
        if(includesSelf) {
            final JsonRepresentation selfLink = newLinkToBuilder(getResourceContext(), Rel.SELF, objectSpecification).build();
            getLinks().arrayAdd(selfLink);
        }
        
        representation.mapPut("canonicalName", objectSpecification.getFullIdentifier());
        addMembers();
        
        addTypeActions();

        putExtensionsNames();
        putExtensionsDescriptionIfAvailable();
        putExtensionsIfService();

        return representation;
    }

    private void addMembers() {
        final JsonRepresentation membersList = JsonRepresentation.newArray();
        representation.mapPut("members", membersList);
        final List<ObjectAssociation> associations = objectSpecification.getAssociations();
        for (ObjectAssociation association : associations) {
            if(association.isOneToOneAssociation()) {
                OneToOneAssociation property = (OneToOneAssociation) association;
                final LinkBuilder linkBuilder = PropertyDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.PROPERTY, objectSpecification, property);
                membersList.arrayAdd(linkBuilder.build());
            } else if(association.isOneToManyAssociation()) {
                OneToManyAssociation collection = (OneToManyAssociation) association;
                final LinkBuilder linkBuilder = CollectionDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.PROPERTY, objectSpecification, collection);
                membersList.arrayAdd(linkBuilder.build());
            }
        }
        final List<ObjectAction> actions = objectSpecification.getObjectActions(Contributed.INCLUDED);
        for (ObjectAction action : actions) {
            final LinkBuilder linkBuilder = ActionDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.ACTION, objectSpecification, action);
            membersList.arrayAdd(linkBuilder.build());
        }
    }

    private JsonRepresentation getTypeActions() {
        JsonRepresentation typeActions = representation.getArray("typeActions");
        if(typeActions == null) {
            typeActions = JsonRepresentation.newArray();
            representation.mapPut("typeActions", typeActions);
        }
        return typeActions;
    }

    private void addTypeActions() {
        getTypeActions().arrayAdd(linkToIsSubtypeOf());
        getTypeActions().arrayAdd(linkToIsSupertypeOf());
        getTypeActions().arrayAdd(linkToNewTransientInstance());
    }

    private JsonRepresentation linkToIsSubtypeOf() {
        final String url = "domainTypes/" + objectSpecification.getFullIdentifier() + "/typeactions/isSubtypeOf/invoke";
        
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.TYPE_ACTION, RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = argumentsTo(getResourceContext(), "supertype", null);
        JsonRepresentation link = linkBuilder.withArguments(arguments).withId("isSubtypeOf").build();
        return link;
    }

    private JsonRepresentation linkToIsSupertypeOf() {
        final String url = "domainTypes/" + objectSpecification.getFullIdentifier() + "/typeactions/isSupertypeOf/invoke";
        
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.TYPE_ACTION, RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = argumentsTo(getResourceContext(), "subtype", null);
        JsonRepresentation link = linkBuilder.withArguments(arguments).withId("isSupertypeOf").build();
        return link;
    }

    private JsonRepresentation linkToNewTransientInstance() {
        final String url = "domainTypes/" + objectSpecification.getFullIdentifier() + "/typeactions/newTransientInstance/invoke";
        
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.TYPE_ACTION, RepresentationType.TYPE_ACTION_RESULT, url);
        JsonRepresentation link = linkBuilder.withId("newTransientInstance").build();
        return link;
    }

    public static JsonRepresentation argumentsTo(ResourceContext resourceContext, String paramName, final ObjectSpecification objectSpec) {
        final JsonRepresentation arguments = JsonRepresentation.newMap();
        final JsonRepresentation link = JsonRepresentation.newMap();
        arguments.mapPut(paramName, link);
        if(objectSpec != null) {
            link.mapPut("href", resourceContext.urlFor("domainTypes/" + objectSpec.getFullIdentifier()));
        } else {
            link.mapPut("href", NullNode.instance);
        }
        return arguments;
    }

    
    protected void putExtensionsNames() {
        String singularName = objectSpecification.getSingularName();
        getExtensions().mapPut("friendlyName", singularName);

        String pluralName = objectSpecification.getPluralName();
        getExtensions().mapPut("pluralName", pluralName);
    }

    protected void putExtensionsDescriptionIfAvailable() {
        String description = objectSpecification.getDescription();
        if(!Strings.isNullOrEmpty(description)) {
            getExtensions().mapPut("description", description);
        }
    }

    protected void putExtensionsIfService() {
        getExtensions().mapPut("isService", objectSpecification.isService());
    }

}