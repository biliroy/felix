/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.ipojo.manipulator.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.manipulator.MetadataProvider;
import org.apache.felix.ipojo.metadata.Element;

/**
 * A {@code EmptyMetadataProvider} simply returns an empty list of Element.
 *
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class EmptyMetadataProvider implements MetadataProvider {

    public List<Element> getMetadatas() throws IOException {
        // Do not use Collections.emptyList() as the manipulator
        // adds discovered components in this list
        // TODO maybe the manipulator can have it's own list add we simply addAll() ?
        return new ArrayList<Element>();
    }
}
