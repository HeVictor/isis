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
package org.apache.isis.extensions.executionlog.applib.app;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.extensions.executionlog.applib.IsisModuleExtExecutionLogApplib;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntry;
import org.apache.isis.extensions.executionlog.applib.dom.ExecutionLogEntryRepository;
import org.springframework.lang.Nullable;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@Named(ExecutionLogMenu.LOGICAL_TYPE_NAME)
@DomainService(nature = NatureOfService.VIEW)
@DomainServiceLayout(
    menuBar = DomainServiceLayout.MenuBar.SECONDARY,
    named = "Activity"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class ExecutionLogMenu {

    public static final String LOGICAL_TYPE_NAME =
            IsisModuleExtExecutionLogApplib.NAMESPACE + ".ExecutionLogMenu";

    public static abstract class ActionDomainEvent<T>
            extends IsisModuleExtExecutionLogApplib.ActionDomainEvent<T> { }


    final ExecutionLogEntryRepository<? extends ExecutionLogEntry> executionLogEntryRepository;
    final ClockService clockService;


    @Action(
            domainEvent = findMostRecent.DomainEvent.class,
            semantics = SemanticsOf.SAFE,
            typeOf = ExecutionLogEntry.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="20")
    public class findMostRecent {
        public class DomainEvent extends ActionDomainEvent<findMostRecent> { }

        @MemberSupport public List<? extends ExecutionLogEntry> act() {
            return executionLogEntryRepository.findMostRecent();
        }
    }


    @Action(
            domainEvent = findExecutions.DomainEvent.class,
            semantics = SemanticsOf.SAFE,
            typeOf = ExecutionLogEntry.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="30")
    public class findExecutions {
        public class DomainEvent extends ActionDomainEvent<findExecutions> { }

        @MemberSupport public List<? extends ExecutionLogEntry> act(
                final @Nullable LocalDate from,
                final @Nullable LocalDate to) {
            return executionLogEntryRepository.findByFromAndTo(from, to);
        }
        @MemberSupport public LocalDate default0Act() {
            return now().minusDays(7);
        }
        @MemberSupport public LocalDate default1Act() {
            return now();
        }
    }



    @Action(
            domainEvent = findAll.DomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING,
            semantics = SemanticsOf.SAFE,
            typeOf = ExecutionLogEntry.class
    )
    @ActionLayout(cssClassFa = "fa-search", sequence="40")
    public class findAll {
        public class DomainEvent extends ActionDomainEvent<findAll> { }

        @MemberSupport public List<? extends ExecutionLogEntry> act() {
            return executionLogEntryRepository.findAll();
        }
    }




    private LocalDate now() {
        return clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
    }
}
