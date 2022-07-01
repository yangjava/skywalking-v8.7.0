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
import java.util.Map;
import lombok.Setter;

/**
 * The <code>ModuleProvider</code> is an implementation of a {@link ModuleDefine}.
 * <p>
 * And each moduleDefine can have one or more implementation, which depends on `application.yml`
 */
// 模块提供抽象类，所有的模块提供类都需要继承该抽象类，一个模块定义可以配置多个模块提供类，通过在application.yml进行切换
public abstract class ModuleProvider implements ModuleServiceHolder {
    // 模块管理器
    @Setter
    private ModuleManager manager;
    // 模块定义对象
    @Setter
    private ModuleDefine moduleDefine;

    // 模块提供对应的服务对象map
    private final Map<Class<? extends Service>, Service> services = new HashMap<>();

    public ModuleProvider() {
    }

    protected final ModuleManager getManager() {
        return manager;
    }

    /**
     * @return the name of this provider.
     */
    // 获取服务提供实现类的name，需要子类实现
    public abstract String name();

    /**
     * @return the moduleDefine name
     */
    // 定义模块提供者所实现的模块定义类
    public abstract Class<? extends ModuleDefine> module();

    /**
     *
     */
    // 创建模块定义配置对象
    public abstract ModuleConfig createConfigBeanIfAbsent();

    /**
     * In prepare stage, the moduleDefine should initialize things which are irrelative other modules.
     */
    // 准备阶段（初始化与其他模块无关的事情）
    public abstract void prepare() throws ServiceNotProvidedException, ModuleStartException;

    /**
     * In start stage, the moduleDefine has been ready for interop.
     */
    // 启动阶段（该阶段模块间可以互相操作）
    public abstract void start() throws ServiceNotProvidedException, ModuleStartException;

    /**
     * This callback executes after all modules start up successfully.
     */
    // 完成后通知阶段（在所有模块成功启动后执行）
    public abstract void notifyAfterCompleted() throws ServiceNotProvidedException, ModuleStartException;

    /**
     * @return moduleDefine names which does this moduleDefine require?
     */
    // 该模块需要依赖的其他模块名
    public abstract String[] requiredModules();

    /**
     * Register an implementation for the service of this moduleDefine provider.
     */
    // 注册服务实现类
    @Override
    public final void registerServiceImplementation(Class<? extends Service> serviceType,
        Service service) throws ServiceNotProvidedException {
        if (serviceType.isInstance(service)) {
            this.services.put(serviceType, service);
        } else {
            throw new ServiceNotProvidedException(serviceType + " is not implemented by " + service);
        }
    }

    /**
     * Make sure all required services have been implemented.
     *
     * @param requiredServices must be implemented by the moduleDefine.
     * @throws ServiceNotProvidedException when exist unimplemented service.
     */
    // 确保所有服务被实现
    void requiredCheck(Class<? extends Service>[] requiredServices) throws ServiceNotProvidedException {
        if (requiredServices == null)
            return;

        for (Class<? extends Service> service : requiredServices) {
            if (!services.containsKey(service)) {
                throw new ServiceNotProvidedException("Service:" + service.getName() + " not provided");
            }
        }

        if (requiredServices.length != services.size()) {
            throw new ServiceNotProvidedException("The " + this.name() + " provider in " + moduleDefine.name() + " moduleDefine provide more service implementations than ModuleDefine requirements.");
        }
    }
    // 获取服务实现对象
    @Override
    public @SuppressWarnings("unchecked")
    <T extends Service> T getService(Class<T> serviceType) throws ServiceNotProvidedException {
        Service serviceImpl = services.get(serviceType);
        if (serviceImpl != null) {
            return (T) serviceImpl;
        }

        throw new ServiceNotProvidedException("Service " + serviceType.getName() + " should not be provided, based on moduleDefine define.");
    }

    ModuleDefine getModule() {
        return moduleDefine;
    }

    String getModuleName() {
        return moduleDefine.name();
    }
}
