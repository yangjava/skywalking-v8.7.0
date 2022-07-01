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

import java.util.HashMap;
import java.util.Properties;

/**
 * Modulization configurations. The {@link ModuleManager} is going to start, lookup, start modules based on this.
 */
// OAP应用配置类
public class ApplicationConfiguration {
    // 模块定义配置map
    private HashMap<String, ModuleConfiguration> modules = new HashMap<>();
    // 模块配置名列表
    public String[] moduleList() {
        return modules.keySet().toArray(new String[0]);
    }
    // 添加模块定义配置
    public ModuleConfiguration addModule(String moduleName) {
        ModuleConfiguration newModule = new ModuleConfiguration();
        modules.put(moduleName, newModule);
        return newModule;
    }
    // 判断指定模块名是否存在模块定义配置map中
    public boolean has(String moduleName) {
        return modules.containsKey(moduleName);
    }
    // 获取模块定义配置
    public ModuleConfiguration getModuleConfiguration(String name) {
        return modules.get(name);
    }

    /**
     * The configurations about a certain module.
     */
    // 模块定义配置类
    public static class ModuleConfiguration {

        // 模块提供对象map
        private HashMap<String, ProviderConfiguration> providers = new HashMap<>();

        private ModuleConfiguration() {
        }
        // 获取模块提供配置
        public Properties getProviderConfiguration(String name) {
            return providers.get(name).getProperties();
        }
        // 是否存在模块提供配置
        public boolean has(String name) {
            return providers.containsKey(name);
        }
        // 添加模块提供配置
        public ModuleConfiguration addProviderConfiguration(String name, Properties properties) {
            ProviderConfiguration newProvider = new ProviderConfiguration(properties);
            providers.put(name, newProvider);
            return this;
        }
    }

    /**
     * The configuration about a certain provider of a module.
     */
    // 模块提供配置类
    public static class ProviderConfiguration {
        // 模块提供属性
        private Properties properties;

        ProviderConfiguration(Properties properties) {
            this.properties = properties;
        }

        private Properties getProperties() {
            return properties;
        }
    }
}
