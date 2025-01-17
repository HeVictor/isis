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
package org.apache.isis.incubator.viewer.vaadin.ui.pages.main;

import java.util.function.Consumer;

import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.menubar.MenuBar;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.incubator.viewer.vaadin.model.action.ActionUiModelFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.model.decorator.Decorators;
import org.apache.isis.viewer.commons.applib.services.menu.MenuItemDto;
import org.apache.isis.viewer.commons.applib.services.menu.MenuVisitor;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "of")
//@Log4j2
class MenuBuilderVaa implements MenuVisitor {

    private final MetaModelContext commonContext;
    private final Consumer<ManagedAction> menuActionEventHandler;
    private final MenuBar menuBar;

    private MenuItem currentTopLevelMenu = null;
    private ActionUiModelFactoryVaa actionUiModelFactory = new ActionUiModelFactoryVaa();

    @Override
    public void addTopLevel(MenuItemDto menuDto) {

        if(menuDto.isTertiaryRoot()) {
            currentTopLevelMenu = menuBar.addItem(Decorators.getUser()
                    .decorateWithAvatar(new Label(), commonContext));
        } else {
            currentTopLevelMenu = menuBar.addItem(Decorators.getMenu()
                    .decorateTopLevel(new Label(menuDto.getName())));
        }
    }

    @Override
    public void addSubMenu(MenuItemDto menu) {
        val managedAction = menu.getManagedAction();

        val actionUiModel = actionUiModelFactory.newActionUiModel(managedAction);
        currentTopLevelMenu.getSubMenu()
        .addItem(actionUiModel.createMenuUiComponent(), e->menuActionEventHandler.accept(managedAction));
    }

    @Override
    public void addSectionSpacer() {
        val sectionSpacer = new Hr();
        val menuItem = currentTopLevelMenu.getSubMenu().addItem(sectionSpacer);
        menuItem.setEnabled(false);
        val menuItemElement = menuItem.getElement();
        menuItemElement.setAttribute("class", "section-spacer");
    }

    @Override
    public void addSectionLabel(String named) {
        val sectionLabel = new Label(named);
        sectionLabel.addClassName("section-label");
        val menuItem = currentTopLevelMenu.getSubMenu().addItem(sectionLabel);
        menuItem.setEnabled(false);
        val menuItemElement = menuItem.getElement();
        menuItemElement.setAttribute("class", "section-label");
    }

}