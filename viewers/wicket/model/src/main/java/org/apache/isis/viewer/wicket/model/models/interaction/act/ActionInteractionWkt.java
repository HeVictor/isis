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
package org.apache.isis.viewer.wicket.model.models.interaction.act;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.wicket.model.ChainingModel;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.isis.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;

/**
 * The parent (container) model of multiple <i>parameter models</i> which implement
 * {@link ChainingModel}.
 * <pre>
 * IModel[ActionInteraction] ... placeOrder(X x, Yy)
 * |
 * +-- ParameterUiModel ... bound to X x (ParameterNegotiationModel)
 * +-- ParameterUiModel ... bound to Y y (ParameterNegotiationModel)
 * </pre>
 * This action might be associated with a <i>Collection</i> that acts as its multi-select
 * defaults provider. This is modeled with {@link #associatedWithCollectionModelIfAny}.
 *
 * @implSpec the state of pending parameters ParameterNegotiationModel is held transient,
 * that means it does not survive a serialization/de-serialization cycle; instead
 * is recreated with parameter defaults
 *
 * @see ChainingModel
 */
public class ActionInteractionWkt
extends HasBookmarkedOwnerAbstract<ActionInteraction> {

    private static final long serialVersionUID = 1L;

    private final String memberId;
    private final Where where;
    private Can<ParameterUiModelWkt> childModels;
    private @Nullable EntityCollectionModel associatedWithCollectionModelIfAny;

//    /**
//     * Returns a new <i>Action Interaction</i> binding to the parent {@link BookmarkedObjectWkt}
//     * of given {@link ActionModel}.
//     */
//    public static ActionInteractionWkt bind(
//            final ActionModel actionModel,
//            final Where where) {
//        return new ActionInteractionWkt(
//                actionModel.getParentUiModel().bookmarkedObjectModel(),
//                actionModel.getMetaModel().getId(),
//                where,
//                null);
//    }

    public ActionInteractionWkt(
            final BookmarkedObjectWkt bookmarkedObject,
            final String memberId,
            final Where where,
            final EntityCollectionModel associatedWithCollectionModelIfAny) {
        super(bookmarkedObject);
        this.memberId = memberId;
        this.where = where;
        this.associatedWithCollectionModelIfAny = associatedWithCollectionModelIfAny;
    }

    @Override
    protected ActionInteraction load() {
        parameterNegotiationModel =
                _Lazy.threadSafe(()->actionInteraction().startParameterNegotiation());

        return associatedWithCollectionModelIfAny!=null
                ? associatedWithCollectionModelIfAny.getDataTableModel()
                        .startAssociatedActionInteraction(memberId, where)
                : ActionInteraction.start(getBookmarkedOwner(), memberId, where);

    }

    public final ActionInteraction actionInteraction() {
        return getObject();
    }

    public final ObjectAction getMetaModel() {
        return actionInteraction().getMetamodel().orElseThrow();
    }

    // -- LAZY BINDING

    public Stream<ParameterUiModelWkt> streamParameterUiModels() {
        if(childModels==null) {
            this.childModels = _2877.createChildModels(this);
        }
        if(childModels==null) {
            final int paramCount = actionInteraction().getMetamodel().get().getParameterCount();
            final int tupleIndex = 0;
            this.childModels = IntStream.range(0, paramCount)
                    .mapToObj(paramIndex -> new ParameterUiModelWkt(this, paramIndex, tupleIndex))
                    .collect(Can.toCan());
        }
        return childModels.stream();
    }

    // -- PARAMETER NEGOTIATION WITH MEMOIZATION (TRANSIENT)

    private transient _Lazy<Optional<ParameterNegotiationModel>> parameterNegotiationModel;

    public final ParameterNegotiationModel parameterNegotiationModel() {
        _Assert.assertTrue(this.isAttached(), "model is not attached");
        return parameterNegotiationModel.get()
                .orElseThrow(()->_Exceptions.noSuchElement(memberId));
    }

    public void resetParametersToDefault() {
        parameterNegotiationModel.clear();
    }



}
