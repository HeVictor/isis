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
package org.apache.isis.extensions.secman.applib.tenancy.dom.mixins;

import java.util.Collection;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancyRepository;
import org.apache.isis.extensions.secman.applib.tenancy.dom.mixins.ApplicationTenancy_addChild.DomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "children",
        named = "Add",
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationTenancy_addChild {

    public static class DomainEvent
            extends IsisModuleExtSecmanApplib.ActionDomainEvent<ApplicationTenancy_addChild> {}

    @Inject private ApplicationTenancyRepository applicationTenancyRepository;

    private final ApplicationTenancy target;

    @MemberSupport public ApplicationTenancy act(final ApplicationTenancy child) {
        applicationTenancyRepository.setParentOnTenancy(child, target);
        return target;
    }

    @MemberSupport public Collection<? extends ApplicationTenancy> choicesChild() {
        val choices =_Lists.newArrayList(applicationTenancyRepository.getRootTenancies());
        choices.removeIf(this::containsTarget); // ensure tree structure (nodes cannot contain themselves)
        return choices; }

    @MemberSupport public String disableAct() {
        return choicesChild().isEmpty()? "No root (non-paranted) tenancies available": null; }


    // -- HELPER

    /**
     *  For given t traverses up the tree hierarchy in search of a match.
     */
    private boolean containsTarget(final ApplicationTenancy t) {
        var node = t;
        while(node!=null) {
            if(Objects.equals(node, target)) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

}
