/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.library.module;
// 模块定义类需要实现的接口，提供获取模块的服务类功能，通过该接口，可以获取模块Provider对象对应的Service持有接口，从而拿到模块Provider对象对应的服务对象
public interface ModuleProviderHolder {
    // 获取模块提供对象
    ModuleServiceHolder provider() throws DuplicateProviderException, ProviderNotFoundException;
}
