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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// 根据模块提供对象中的requiredModules方法，确定模块的初始化顺序（被依赖的模块先行加载）
class BootstrapFlow {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapFlow.class);

    private Map<String, ModuleDefine> loadedModules;
    // 按依赖顺序排序的模块提供对象列表
    private List<ModuleProvider> startupSequence;

    BootstrapFlow(Map<String, ModuleDefine> loadedModules) throws CycleDependencyException, ModuleNotFoundException {
        this.loadedModules = loadedModules;
        startupSequence = new LinkedList<>();
        // 被依赖的模块先行加载
        makeSequence();
    }

    @SuppressWarnings("unchecked")
    void start(
        ModuleManager moduleManager) throws ModuleNotFoundException, ServiceNotProvidedException, ModuleStartException {
        // 每个provider start之前会检查ModuleDefine 定义的服务类是否已经注册
        for (ModuleProvider provider : startupSequence) {
            LOGGER.info("start the provider {} in {} module.", provider.name(), provider.getModuleName());
            provider.requiredCheck(provider.getModule().services());

            provider.start();
        }
    }

    void notifyAfterCompleted() throws ServiceNotProvidedException, ModuleStartException {
        for (ModuleProvider provider : startupSequence) {
            provider.notifyAfterCompleted();
        }
    }

    private void makeSequence() throws CycleDependencyException, ModuleNotFoundException {
        List<ModuleProvider> allProviders = new ArrayList<>();
        // 判断所有被依赖的模块是否存在
        for (final ModuleDefine module : loadedModules.values()) {
            String[] requiredModules = module.provider().requiredModules();
            if (requiredModules != null) {
                for (String requiredModule : requiredModules) {
                    if (!loadedModules.containsKey(requiredModule)) {
                        throw new ModuleNotFoundException(
                            requiredModule + " module is required by "
                                + module.provider().getModuleName() + "."
                                + module.provider().name() + ", but not found.");
                    }
                }
            }

            allProviders.add(module.provider());
        }

        do {
            int numOfToBeSequenced = allProviders.size();
            for (int i = 0; i < allProviders.size(); i++) {
                ModuleProvider provider = allProviders.get(i);
                String[] requiredModules = provider.requiredModules();
                if (CollectionUtils.isNotEmpty(requiredModules)) {
                    // 是否所有依赖的模块都在startupSequence中
                    boolean isAllRequiredModuleStarted = true;
                    for (String module : requiredModules) {
                        // find module in all ready existed startupSequence
                        boolean exist = false;
                        for (ModuleProvider moduleProvider : startupSequence) {
                            if (moduleProvider.getModuleName().equals(module)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            isAllRequiredModuleStarted = false;
                            break;
                        }
                    }
                    // 所有依赖的模块都在startupSequence，则将该模块提供对象加入startupSequence
                    if (isAllRequiredModuleStarted) {
                        startupSequence.add(provider);
                        allProviders.remove(i);
                        i--;
                    }
                } else {
                    startupSequence.add(provider);
                    allProviders.remove(i);
                    i--;
                }
            }
            // 如果一次循环后，没有任何一个对象加入到startupSequence，则证明有循环依赖
            if (numOfToBeSequenced == allProviders.size()) {
                StringBuilder unSequencedProviders = new StringBuilder();
                allProviders.forEach(provider -> unSequencedProviders.append(provider.getModuleName())
                                                                     .append("[provider=")
                                                                     .append(provider.getClass().getName())
                                                                     .append("]\n"));
                throw new CycleDependencyException(
                    "Exist cycle module dependencies in \n" + unSequencedProviders.substring(0, unSequencedProviders
                        .length() - 1));
            }
        }
        while (allProviders.size() != 0); // 当提供对象列表不为空，则一直循环执行下去
    }
}
