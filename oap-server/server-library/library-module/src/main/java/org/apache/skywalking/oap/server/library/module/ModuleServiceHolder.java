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
// 模块提供类需要实现的接口，提供注册服务实现、获取服务对象的功能
public interface ModuleServiceHolder {
    // 注册服务实现对象
    void registerServiceImplementation(Class<? extends Service> serviceType,
        Service service) throws ServiceNotProvidedException;
    // 获取服务实现对象
    <T extends Service> T getService(Class<T> serviceType) throws ServiceNotProvidedException;
}
