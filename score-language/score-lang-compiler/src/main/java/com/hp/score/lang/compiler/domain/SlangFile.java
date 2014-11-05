package com.hp.score.lang.compiler.domain;
/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
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

/*
 * Created by orius123 on 05/11/14.
 */
import java.util.List;
import java.util.Map;

public class SlangFile {

    private List<Map<String, String>> imports;
    private Map<String, Object> flow;
    private List<Map> operations;
    private String namespace;

    public String getNamespace() {
        return namespace;
    }

    public Map<String, Object> getFlow() {
        return flow;
    }

    public List getImports() {
        return imports;
    }

    public List<Map> getOperations() {
        return operations;
    }
}
