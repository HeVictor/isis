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
package org.apache.isis.viewer.commons.services.branding;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.IsisConfiguration.Viewer.Wicket.Application;
import org.apache.isis.viewer.commons.applib.services.branding.BrandingUiModel;
import org.apache.isis.viewer.commons.applib.services.branding.BrandingUiService;
import org.apache.isis.viewer.commons.services.IsisModuleViewerCommonsServices;

@Service
@Named(IsisModuleViewerCommonsServices.NAMESPACE + ".BrandingUiServiceDefault")
@Priority(PriorityPrecedence.LATE)
@Qualifier("Default")
public class BrandingUiServiceDefault
implements BrandingUiService {

    private final Application appConfig;

    @Inject
    public BrandingUiServiceDefault(final IsisConfiguration isisConfiguration) {
        //TODO application name/logo borrowed from Wicket's configuration,
        // we might generalize this config option to all viewers
        this.appConfig = isisConfiguration.getViewer().getWicket().getApplication();
    }

    @Override
    public BrandingUiModel getHeaderBranding() {
        return BrandingUiModel.of(
                appConfig.getName(),
                appConfig.getBrandLogoHeader().orElse(null));
    }

    @Override
    public BrandingUiModel getSignInBranding() {
        return BrandingUiModel.of(
                appConfig.getName(),
                appConfig.getBrandLogoSignin().orElse(null));
    }

}
