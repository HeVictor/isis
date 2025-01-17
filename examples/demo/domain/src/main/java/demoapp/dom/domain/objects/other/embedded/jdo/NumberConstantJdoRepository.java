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
package demoapp.dom.domain.objects.other.embedded.jdo;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.val;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.other.embedded.ComplexNumber;

@Profile("demo-jdo")
@Service
public class NumberConstantJdoRepository
extends ValueHolderRepository<ComplexNumber, NumberConstantJdo> {

    protected NumberConstantJdoRepository() {
        super(NumberConstantJdo.class);
    }

    @Override
    protected NumberConstantJdo newDetachedEntity(ComplexNumber value) {
        val numConst = repositoryService.detachedEntity(new NumberConstantJdo());
        numConst.setName(((ComplexNumber.SimpleNamedComplexNumber)value).getName());
        numConst.setNumber(ComplexNumberJdo.of(value.getRe(), value.getIm()));
        return numConst;
    }

}
