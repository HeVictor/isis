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
package org.apache.isis.incubator.viewer.javafx.ui.main;

import java.util.function.Consumer;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.incubator.viewer.javafx.model.context.UiContextFx;
import org.apache.isis.viewer.commons.applib.services.menu.MenuItemDto;
import org.apache.isis.viewer.commons.applib.services.menu.MenuVisitor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

@RequiredArgsConstructor(staticName = "of")
@Log4j2
public class MenuBuilderFx implements MenuVisitor {

    private final UiContextFx uiContext;
    private final MenuBar menuBar;
    private final Consumer<ManagedAction> menuActionEventHandler;

    private Menu currentTopLevelMenu = null;

    @Override
    public void addTopLevel(MenuItemDto menuDto) {
        log.debug("top level menu {}", menuDto.getName());

        menuBar.getMenus()
        .add(currentTopLevelMenu = new Menu(menuDto.getName()));
    }

    @Override
    public void addSubMenu(MenuItemDto menuDto) {
        val managedAction = menuDto.getManagedAction();

        log.debug("sub menu {}", menuDto.getName());

        val actionUiModel = uiContext.getActionUiModelFactory().newActionUiModel(uiContext, managedAction);
        val menuItem = actionUiModel.createMenuUiComponent();
        menuItem.setOnAction(e->menuActionEventHandler.accept(managedAction));
        currentTopLevelMenu.getItems().add(menuItem);
    }

    @Override
    public void addSectionSpacer() {
        log.debug("menu spacer");
        currentTopLevelMenu.getItems().add(new SeparatorMenuItem());
    }

    @Override
    public void addSectionLabel(String named) {
        log.debug("section label  {}", named);
        val menuItem = new MenuItem(named);
        currentTopLevelMenu.getItems().add(menuItem);
        menuItem.setDisable(true);
    }

}
