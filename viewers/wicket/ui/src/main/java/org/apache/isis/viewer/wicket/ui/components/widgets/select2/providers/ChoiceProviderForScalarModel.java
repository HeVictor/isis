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
package org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public abstract class ChoiceProviderForScalarModel
extends ChoiceProviderAbstract {

    private static final long serialVersionUID = 1L;

    @Getter @Accessors(fluent = true)
    private final ScalarModel scalarModel;

    @Override
    protected final boolean isRequired() {
        return scalarModel().isRequired();
    }

    /** whether this adapter is dependent on previous (pending) arguments */
    public boolean dependsOnPreviousArgs() {
        return true;
    }

    @Override
    protected ObjectMemento mementoFromId(final String id) {
        val memento = Bookmark.parse(id)
                .map(getCommonContext()::mementoForBookmark)
                .orElse(null); // FIXME if can't recreated from bookmark, there might be a bug
        return memento;
    }

    // -- DEPS

    @Override
    public IsisAppCommonContext getCommonContext() {
        return scalarModel().getCommonContext();
    }

}
