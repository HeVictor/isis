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
package /*${java-package}*/;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.commons.functional.IndexedConsumer;

import demoapp.dom.types.Samples;
import lombok.RequiredArgsConstructor;
import lombok.val;

/*${generated-file-notice}*/
//tag::class[]
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class /*${showcase-name}*/Holder_actionReturningArray {

    private final /*${showcase-name}*/Holder holder;

    public /*${showcase-type}*/[] act() {
        val array = new long[(int)samples.stream().count()];
        samples.stream()
            .forEach(IndexedConsumer.zeroBased((index, value)->array[index] = value));
        return array;
    }

    @Inject
    Samples</*${showcase-type-boxed}*/> samples;

}
//end::class[]
