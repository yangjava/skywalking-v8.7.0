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

package org.apache.skywalking.oap.server.starter;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.RunningMode;
import org.apache.skywalking.oap.server.library.module.ApplicationConfiguration;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.starter.config.ApplicationConfigLoader;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;

/**
 * Starter core. Load the core configuration file, and initialize the startup sequence through {@link ModuleManager}.
 */
// 由server-starter和server-starter-es7调用server-bootstrap
// server-starter和server-starter-es7的区别在于maven中引入的存储模块Module不同
// server-starter      storage-elasticsearch-plugin
// server-starter-es7  storage-elasticsearch7-plugin
@Slf4j
public class OAPServerBootstrap {
    public static void start() {
        // // 初始化mode为init或者no-init,表示是否初始化例如:底层存储组件等
        String mode = System.getProperty("mode");
        RunningMode.setMode(mode);

        // 初始化ApplicationConfigurationd的加载器
        ApplicationConfigLoader configLoader = new ApplicationConfigLoader();
        // 初始化Module的加载管理器
        ModuleManager manager = new ModuleManager();
        try {
            // 加载yml生成ApplicationConfiguration配置
            ApplicationConfiguration applicationConfiguration = configLoader.load();
            // 初始化模块 通过spi获取所有Module实现,基于yml配置加载spi中存在的相关实现
            manager.init(applicationConfiguration);

            // 对 telemetry 模块进行特殊处理, 添加 uptime 的 Gauge, 用于标识服务开始运行
            manager.find(TelemetryModule.NAME)
                   .provider()
                   .getService(MetricsCreator.class)
                   .createGauge("uptime", "oap server start up time", MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE)
                   // Set uptime to second
                   .setValue(System.currentTimeMillis() / 1000d);
            // 如果是 Init 模式, 现在就直接退出, 否则就继续运行
            if (RunningMode.isInitMode()) {
                log.info("OAP starts up in init mode successfully, exit now...");
                System.exit(0);
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            System.exit(1);
        }
    }
}
