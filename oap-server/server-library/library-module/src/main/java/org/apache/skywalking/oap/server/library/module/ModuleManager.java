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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * The <code>ModuleManager</code> takes charge of all {@link ModuleDefine}s in collector.
 */
// 模块管理类，管理模块的生命周期
// Module 是 Skywalking 在 OAP 提供的一种管理功能特性的机制。通过 Module 机制，可以方便的定义模块，并且可以提供多种实现，在配置文件中任意选择实现。
public class ModuleManager implements ModuleDefineHolder {
    // 所有模块是否已经通过准备阶段
    private boolean isInPrepareStage = true;
    // 所有被加载的模块定义对象map
    private final Map<String, ModuleDefine> loadedModules = new HashMap<>();

    /**
     * Init the given modules
     */
    // 初始化所有配置的模块
    public void init(
        ApplicationConfiguration applicationConfiguration) throws ModuleNotFoundException, ProviderNotFoundException, ServiceNotProvidedException, CycleDependencyException, ModuleConfigException, ModuleStartException {
        // 获取配置类中的模块名
        String[] moduleNames = applicationConfiguration.moduleList();
        // SPI加载所有模块定义对象
        ServiceLoader<ModuleDefine> moduleServiceLoader = ServiceLoader.load(ModuleDefine.class);
        // SPI加载所有模块提供对象
        ServiceLoader<ModuleProvider> moduleProviderLoader = ServiceLoader.load(ModuleProvider.class);
        // 所有配置类中定义的模块，进行准备阶段
        HashSet<String> moduleSet = new HashSet<>(Arrays.asList(moduleNames));
        for (ModuleDefine module : moduleServiceLoader) {
            // 判断 ModuleDefine.name 是否在 ApplicationConfiguration 配置中存在, 不存在跳过
            if (moduleSet.contains(module.name())) {
                // 存在时, 分别调用每个 ModuleDefine#prepare 进行初始化, 此处主要工作为配置初始化, 完成后放入 loadedModules 中
                module.prepare(this, applicationConfiguration.getModuleConfiguration(module.name()), moduleProviderLoader);
                loadedModules.put(module.name(), module);
                moduleSet.remove(module.name());
            }
        }
        // Finish prepare stage
        // 准备阶段结束
        isInPrepareStage = false;

        if (moduleSet.size() > 0) {
            throw new ModuleNotFoundException(moduleSet.toString() + " missing.");
        }
        // 使用loadedModules 对 BootstrapFlow 进行初始化, BootstrapFlow 会对模块的依赖关系进行排序
        BootstrapFlow bootstrapFlow = new BootstrapFlow(loadedModules);
        // 调用每个模块的启动阶段
        bootstrapFlow.start(this);
        // 所有模块进入完成后通知阶段 所有模块都已经加载完成
        bootstrapFlow.notifyAfterCompleted();
    }

    // 判断是否有该模块
    @Override
    public boolean has(String moduleName) {
        return loadedModules.get(moduleName) != null;
    }

    // 通过模块名获取模块定义对象
    @Override
    public ModuleProviderHolder find(String moduleName) throws ModuleNotFoundRuntimeException {
        assertPreparedStage();
        ModuleDefine module = loadedModules.get(moduleName);
        if (module != null)
            return module;
        throw new ModuleNotFoundRuntimeException(moduleName + " missing.");
    }
    // 断言是否还在准备阶段，如果还在准备阶段，则抛出异常
    private void assertPreparedStage() {
        if (isInPrepareStage) {
            throw new AssertionError("Still in preparing stage.");
        }
    }
}
