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

package org.apache.isis.viewer.wicket.model.models;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.StringResourceStream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.routing.RoutingService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.NamedWithMimeType;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.metamodel.spec.ActionType;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;

import lombok.val;

public class ActionModel extends BookmarkableModel<ManagedObject> implements FormExecutorContext {

    private static final long serialVersionUID = 1L;

    private static final String NULL_ARG = "$nullArg$";
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("([^=]+)=(.+)");


    public ActionModel copy() {
        return new ActionModel(this);
    }

    // -- FACTORY METHODS
    
    /**
     * @param entityModel
     * @param action
     * @return
     */
    public static ActionModel create(EntityModel entityModel, ObjectAction action) {
        val homePageActionMemento = new ActionMemento(action);
        val actionModel = new ActionModel(entityModel, homePageActionMemento);
        return actionModel;
    }
    
    public static ActionModel createForPersistent(
            IsisWebAppCommonContext commonContext, 
            PageParameters pageParameters) {
        
        val entityModel = newEntityModelFrom(commonContext, pageParameters);
        val actionMemento = newActionMementoFrom(commonContext, pageParameters);
        val actionModel = new ActionModel(entityModel, actionMemento);
        actionModel.setArgumentsIfPossible(pageParameters);
        actionModel.setContextArgumentIfPossible(pageParameters);
        return actionModel;
    }

    /**
     * Factory method for creating {@link PageParameters}.
     *
     * see {@link #ActionModel(PageParameters, SpecificationLoader)}
     */
    public static PageParameters createPageParameters(ManagedObject adapter, ObjectAction objectAction) {

        val pageParameters = PageParametersUtils.newPageParameters();

        //        final String oidStr = concurrencyChecking == ConcurrencyChecking.CHECK?
        //                adapter.getOid().enString():
        //                    adapter.getOid().enStringNoVersion();

        val oidStr = ManagedObject._oid(adapter).enStringNoVersion();

        PageParameterNames.OBJECT_OID.addStringTo(pageParameters, oidStr);

        val actionType = objectAction.getType();
        PageParameterNames.ACTION_TYPE.addEnumTo(pageParameters, actionType);

        val actionOnTypeSpec = objectAction.getOnType();
        if (actionOnTypeSpec != null) {
            PageParameterNames.ACTION_OWNING_SPEC.addStringTo(pageParameters, actionOnTypeSpec.getFullIdentifier());
        }

        val actionId = determineActionId(objectAction);
        PageParameterNames.ACTION_ID.addStringTo(pageParameters, actionId);

        return pageParameters;
    }


    public static Entry<Integer, String> parse(final String paramContext) {
        final Matcher matcher = KEY_VALUE_PATTERN.matcher(paramContext);
        if (!matcher.matches()) {
            return null;
        }

        final int paramNum;
        try {
            paramNum = Integer.parseInt(matcher.group(1));
        } catch (final Exception e) {
            // ignore
            return null;
        }

        final String oidStr;
        try {
            oidStr = matcher.group(2);
        } catch (final Exception e) {
            return null;
        }

        return new Map.Entry<Integer, String>() {

            @Override
            public Integer getKey() {
                return paramNum;
            }

            @Override
            public String getValue() {
                return oidStr;
            }

            @Override
            public String setValue(final String value) {
                return null;
            }
        };
    }

    //////////////////////////////////////////////////
    // BookmarkableModel
    //////////////////////////////////////////////////

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        val adapter = getTargetAdapter();
        final ObjectAction objectAction = getAction();
        final PageParameters pageParameters = createPageParameters(
                adapter, objectAction);

        // capture argument values
        final ManagedObject[] argumentsAsArray = getArgumentsAsArray();
        for(val argumentAdapter: argumentsAsArray) {
            final String encodedArg = encodeArg(argumentAdapter);
            PageParameterNames.ACTION_ARGS.addStringTo(pageParameters, encodedArg);
        }

        return pageParameters;
    }

    @Override
    public PageParameters getPageParameters() {
        return getPageParametersWithoutUiHints();
    }


    @Override
    public String getTitle() {
        val adapter = getTargetAdapter();
        final ObjectAction objectAction = getAction();

        final StringBuilder buf = new StringBuilder();
        final ManagedObject[] argumentsAsArray = getArgumentsAsArray();
        for(val argumentAdapter: argumentsAsArray) {
            if(buf.length() > 0) {
                buf.append(",");
            }
            buf.append(abbreviated(titleOf(argumentAdapter), 8));
        }

        return adapter.titleString(null) + "." + objectAction.getName() + (buf.length()>0?"(" + buf.toString() + ")":"");
    }

    @Override
    public boolean hasAsRootPolicy() {
        return true;
    }

    //////////////////////////////////////////////////
    // helpers
    //////////////////////////////////////////////////


    private static String titleOf(ManagedObject argumentAdapter) {
        return argumentAdapter!=null?argumentAdapter.titleString(null):"";
    }

    private static String abbreviated(final String str, final int maxLength) {
        return str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }


    private static String determineActionId(final ObjectAction objectAction) {
        final Identifier identifier = objectAction.getIdentifier();
        if (identifier != null) {
            return identifier.toNameParmsIdentityString();
        }
        // fallback (used for action sets)
        return objectAction.getId();
    }

    private final EntityModel entityModel;
    private final ActionMemento actionMemento;

    /**
     * Lazily populated in {@link #getArgumentModel(ActionParameterMemento)}
     */
    private final Map<Integer, ActionArgumentModel> arguments = _Maps.newHashMap();

    
    private static ActionMemento newActionMementoFrom(
            IsisWebAppCommonContext commonContext,
            PageParameters pageParameters) {
        
        final ObjectSpecId owningSpec = ObjectSpecId.of(PageParameterNames.ACTION_OWNING_SPEC.getStringFrom(pageParameters));
        final ActionType actionType = PageParameterNames.ACTION_TYPE.getEnumFrom(pageParameters, ActionType.class);
        final String actionNameParms = PageParameterNames.ACTION_ID.getStringFrom(pageParameters);
        return new ActionMemento(owningSpec, actionType, actionNameParms, commonContext.getSpecificationLoader());
    }


    private static EntityModel newEntityModelFrom(
            IsisWebAppCommonContext commonContext,
            PageParameters pageParameters) {
        
        val rootOid = oidFor(pageParameters);
        if(rootOid.isTransient()) {
            return null;
        } else {
            val memento = commonContext.mementoFor(rootOid);
            return EntityModel.ofMemento(commonContext, memento);
        }
    }

    private static RootOid oidFor(final PageParameters pageParameters) {
        final String oidStr = PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
        return Oid.unmarshaller().unmarshal(oidStr, RootOid.class);
    }


    private ActionModel(EntityModel entityModel, ActionMemento actionMemento) {
        super(entityModel.getCommonContext());
        this.entityModel = entityModel;
        this.actionMemento = actionMemento;
    }

    @Override
    public EntityModel getParentEntityModel() {
        return entityModel;
    }

    /**
     * Copy constructor, as called by {@link #copy()}.
     * @param commonContext 
     */
    private ActionModel(ActionModel actionModel) {
        super(actionModel.getCommonContext());
        this.entityModel = actionModel.entityModel;
        this.actionMemento = actionModel.actionMemento;

        primeArgumentModels();
        val argumentModelByIdx = actionModel.arguments;
        for (val argumentModel : argumentModelByIdx.entrySet()) {
            setArgument(argumentModel.getKey(), argumentModel.getValue().getObject());
        }
    }

    private void setArgumentsIfPossible(
            final PageParameters pageParameters) {
        final List<String> args = PageParameterNames.ACTION_ARGS.getListFrom(pageParameters);

        final ObjectAction action = actionMemento.getAction(getSpecificationLoader());
        final List<ObjectSpecification> parameterTypes = action.getParameterTypes();

        for (int paramNum = 0; paramNum < args.size(); paramNum++) {
            final String encoded = args.get(paramNum);
            setArgument(paramNum, parameterTypes.get(paramNum), encoded);
        }
    }

    private ObjectAction getAction() {
        return getActionMemento().getAction(getSpecificationLoader());
    }


    public boolean hasParameters() {
        return getAction().getParameterCount() > 0;
    }

    private boolean setContextArgumentIfPossible(final PageParameters pageParameters) {
        final String paramContext = PageParameterNames.ACTION_PARAM_CONTEXT.getStringFrom(pageParameters);
        if (paramContext == null) {
            return false;
        }

        final ObjectAction action = actionMemento.getAction(getSpecificationLoader());
        final List<ObjectSpecification> parameterTypes = action.getParameterTypes();
        final int parameterCount = parameterTypes.size();

        final Map.Entry<Integer, String> mapEntry = parse(paramContext);

        final int paramNum = mapEntry.getKey();
        if (paramNum >= parameterCount) {
            return false;
        }

        final String encoded = mapEntry.getValue();
        setArgument(paramNum, parameterTypes.get(paramNum), encoded);

        return true;
    }

    private void setArgument(final int paramNum, final ObjectSpecification argSpec, final String encoded) {
        val argumentAdapter = decodeArg(argSpec, encoded);
        setArgument(paramNum, argumentAdapter);
    }

    private String encodeArg(ManagedObject adapter) {
        if(adapter == null) {
            return NULL_ARG;
        }

        final ObjectSpecification objSpec = adapter.getSpecification();
        if(objSpec.isEncodeable()) {
            final EncodableFacet encodeable = objSpec.getFacet(EncodableFacet.class);
            return encodeable.toEncodedString(adapter);
        }

        return ManagedObject._oid(adapter).enStringNoVersion();
    }

    private ManagedObject decodeArg(final ObjectSpecification objSpec, final String encoded) {
        if(NULL_ARG.equals(encoded)) {
            return null;
        }

        if(objSpec.isEncodeable()) {
            final EncodableFacet encodeable = objSpec.getFacet(EncodableFacet.class);
            return encodeable.fromEncodedString(encoded);
        }

        try {
            val rootOid = RootOid.deStringEncoded(encoded);
            return ManagedObject._adapterOfRootOid(super.getSpecificationLoader(), rootOid);
        } catch (final Exception e) {
            return null;
        }
    }

    private void setArgument(int paramNum, ManagedObject argumentAdapter) {
        
        final ObjectAction action = actionMemento.getAction(getSpecificationLoader());
        final ObjectActionParameter actionParam = action.getParameters().get(paramNum);
        final ActionParameterMemento apm = new ActionParameterMemento(actionParam);
        final ActionArgumentModel actionArgumentModel = getArgumentModel(apm);
        actionArgumentModel.setObject(argumentAdapter);
    }


    public ActionArgumentModel getArgumentModel(final ActionParameterMemento apm) {
        final int i = apm.getNumber();
        ActionArgumentModel actionArgumentModel = arguments.get(i);
        if (actionArgumentModel == null) {
            actionArgumentModel = new ScalarModel(entityModel, apm);
            final int number = actionArgumentModel.getParameterMemento().getNumber();
            arguments.put(number, actionArgumentModel);
        }
        return actionArgumentModel;
    }

    public ManagedObject getTargetAdapter() {
        return entityModel.load();
    }

    public ActionMemento getActionMemento() {
        return actionMemento;
    }

    @Override
    protected ManagedObject load() {

        // from getObject()/reExecute
        detach(); // force re-execute

        // TODO: think we need another field to determine if args have been populated.
        val results = executeAction();

        return results;
    }

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly)
    // for any other value for Where
    public static final Where WHERE_FOR_ACTION_INVOCATION = Where.ANYWHERE;

    private ManagedObject executeAction() {

        val targetAdapter = getTargetAdapter();
        final ManagedObject[] arguments = getArgumentsAsArray();
        final ObjectAction action = getAction();

        // if this action is a mixin, then it will fill in the details automatically.
        val mixedInAdapter = (ManagedObject)null;
        val resultAdapter =
                action.executeWithRuleChecking(
                        targetAdapter, mixedInAdapter, arguments,
                        InteractionInitiatedBy.USER,
                        WHERE_FOR_ACTION_INVOCATION);

        final Stream<RoutingService> routingServices = super.getServiceRegistry()
                .select(RoutingService.class)
                .stream();

        val resultPojo = resultAdapter != null ? resultAdapter.getPojo() : null;

        return routingServices
                .filter(routingService->routingService.canRoute(resultPojo))
                .map(routingService->routingService.route(resultPojo))
                .filter(_NullSafe::isPresent)
                .map(super.getPojoToAdapter())
                .filter(_NullSafe::isPresent)
                .findFirst()
                .orElse(resultAdapter);

    }

    public String getReasonDisabledIfAny() {

        val targetAdapter = getTargetAdapter();
        final ObjectAction objectAction = getAction();

        final Consent usability =
                objectAction.isUsable(
                        targetAdapter,
                        InteractionInitiatedBy.USER,
                        Where.OBJECT_FORMS);
        final String disabledReasonIfAny = usability.getReason();
        return disabledReasonIfAny;
    }


    public boolean isVisible() {

        val targetAdapter = getTargetAdapter();
        val objectAction = getAction();

        final Consent visibility =
                objectAction.isVisible(
                        targetAdapter,
                        InteractionInitiatedBy.USER,
                        Where.OBJECT_FORMS);
        return visibility.isAllowed();
    }


    public String getReasonInvalidIfAny() {
        val targetAdapter = getTargetAdapter();
        final ManagedObject[] proposedArguments = getArgumentsAsArray();
        final ObjectAction objectAction = getAction();
        final Consent validity = objectAction.isProposedArgumentSetValid(targetAdapter, proposedArguments,
                InteractionInitiatedBy.USER);
        return validity.isAllowed() ? null : validity.getReason();
    }

    @Override
    public void setObject(final ManagedObject object) {
        throw new UnsupportedOperationException("target adapter for ActionModel cannot be changed");
    }

    public ManagedObject[] getArgumentsAsArray() {
        if(this.arguments.size() < getAction().getParameterCount()) {
            primeArgumentModels();
        }

        final ObjectAction objectAction = getAction();
        final ManagedObject[] arguments = new ManagedObject[objectAction.getParameterCount()];
        for (int i = 0; i < arguments.length; i++) {
            final ActionArgumentModel actionArgumentModel = this.arguments.get(i);
            arguments[i] = actionArgumentModel.getObject();
        }
        return arguments;
    }

    @Override
    public void reset() {
    }

    public void clearArguments() {
        for (final ActionArgumentModel actionArgumentModel : arguments.values()) {
            actionArgumentModel.reset();
        }
    }

    /**
     * Bookmarkable if the {@link ObjectAction action} has a {@link BookmarkPolicyFacet bookmark} policy
     * of {@link BookmarkPolicy#AS_ROOT root}, and has safe {@link ObjectAction#getSemantics() semantics}.
     */
    public boolean isBookmarkable() {
        final ObjectAction action = getAction();
        final BookmarkPolicyFacet bookmarkPolicy = action.getFacet(BookmarkPolicyFacet.class);
        final boolean safeSemantics = action.getSemantics().isSafeInNature();
        return bookmarkPolicy.value() == BookmarkPolicy.AS_ROOT && safeSemantics;
    }

    // //////////////////////////////////////

    /**
     * Simply executes the action.
     *
     * Previously there was exception handling code here also, but this has now been centralized
     * within FormExecutorAbstract
     */
    public ManagedObject execute() {
        final ManagedObject resultAdapter = this.getObject();
        return resultAdapter;
    }


    // //////////////////////////////////////

    public static IRequestHandler redirectHandler(final Object value) {
        if(value instanceof java.net.URL) {
            final java.net.URL url = (java.net.URL) value;
            return new RedirectRequestHandler(url.toString());
        }
        if(value instanceof LocalResourcePath) {
            final LocalResourcePath localResourcePath = (LocalResourcePath) value;
            return new RedirectRequestHandler(localResourcePath.getPath());
        }
        return null;
    }

    public static IRequestHandler downloadHandler(final Object value) {
        if(value instanceof Clob) {
            final Clob clob = (Clob)value;
            return handlerFor(resourceStreamFor(clob), clob);
        }
        if(value instanceof Blob) {
            final Blob blob = (Blob)value;
            return handlerFor(resourceStreamFor(blob), blob);
        }
        return null;
    }

    private static IResourceStream resourceStreamFor(final Blob blob) {
        final IResourceStream resourceStream = new AbstractResourceStream() {

            private static final long serialVersionUID = 1L;

            @Override
            public InputStream getInputStream() throws ResourceStreamNotFoundException {
                return new ByteArrayInputStream(blob.getBytes());
            }

            @Override
            public String getContentType() {
                return blob.getMimeType().toString();
            }

            @Override
            public void close() throws IOException {
            }
        };
        return resourceStream;
    }

    private static IResourceStream resourceStreamFor(final Clob clob) {
        final IResourceStream resourceStream = new StringResourceStream(clob.getChars(), clob.getMimeType().toString());
        return resourceStream;
    }

    private static IRequestHandler handlerFor(final IResourceStream resourceStream, final NamedWithMimeType namedWithMimeType) {
        final ResourceStreamRequestHandler handler =
                new ResourceStreamRequestHandler(resourceStream, namedWithMimeType.getName());
        handler.setContentDisposition(ContentDisposition.ATTACHMENT);
        return handler;
    }

    // //////////////////////////////////////

    public List<ActionParameterMemento> primeArgumentModels() {
        final ObjectAction objectAction = getAction();

        final List<ObjectActionParameter> parameters = objectAction.getParameters();
        final List<ActionParameterMemento> mementos = buildParameterMementos(parameters);
        for (final ActionParameterMemento apm : mementos) {
            getArgumentModel(apm);
        }

        return mementos;
    }


    private static List<ActionParameterMemento> buildParameterMementos(final List<ObjectActionParameter> parameters) {
        final List<ActionParameterMemento> parameterMementoList =
                _Lists.map(parameters, ActionParameterMemento::new);
        // we copy into a new array list otherwise we get lazy evaluation =
        // reference to a non-serializable object
        return _Lists.newArrayList(parameterMementoList);
    }


    //////////////////////////////////////////////////

    @Override
    public PromptStyle getPromptStyle() {
        final ObjectAction objectAction = getAction();
        final ObjectSpecification objectActionOwner = objectAction.getOnType();
        if(objectActionOwner.isManagedBean()) {
            // tried to move this test into PromptStyleFacetFallback,
            // however it's not that easy to lookup the owning type
            final PromptStyleFacet facet = getFacet(PromptStyleFacet.class);
            if (facet != null) {
                final PromptStyle promptStyle = facet.value();
                if (promptStyle.isDialog()) {
                    // could be specified explicitly.
                    return promptStyle;
                }
            }
            return PromptStyle.DIALOG;
        }
        if(objectAction.getParameterCount() == 0) {
            // a bit of a hack, the point being that the UI for dialog correctly handles no-args,
            // whereas for INLINE it would render a form with no fields
            return PromptStyle.DIALOG;
        }
        final PromptStyleFacet facet = getFacet(PromptStyleFacet.class);
        if(facet == null) {
            // don't think this can happen actually, see PromptStyleFacetFallback
            return PromptStyle.INLINE;
        }
        final PromptStyle promptStyle = facet.value();
        if (promptStyle == PromptStyle.AS_CONFIGURED) {
            // I don't think this can happen, actually...
            // when the metamodel is built, it should replace AS_CONFIGURED with one of the other prompts
            // (see PromptStyleConfiguration and PromptStyleFacetFallback)
            return PromptStyle.INLINE;
        }
        return promptStyle;
    }

    public <T extends Facet> T getFacet(final Class<T> facetType) {
        final FacetHolder facetHolder = getAction();
        return facetHolder.getFacet(facetType);
    }


    //////////////////////////////////////////////////

    private InlinePromptContext inlinePromptContext;

    /**
     * Further hint, to support inline prompts...
     */
    @Override
    public InlinePromptContext getInlinePromptContext() {
        return inlinePromptContext;
    }

    public void setInlinePromptContext(InlinePromptContext inlinePromptContext) {
        this.inlinePromptContext = inlinePromptContext;
    }


}
