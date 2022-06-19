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

package org.apache.skywalking.apm.agent.core.conf.dynamic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
// AgentConfigChangeWatcher用于监听SkyWalking Agent的某项配置的值的变化
@Getter
public abstract class AgentConfigChangeWatcher {
    // Config key, should match KEY in the Table of Agent Configuration Properties.
    // 这个key来源于agent.config,也就是说只有agent配置文件中合法的key才能在这里被使用
    private final String propertyKey;

    public AgentConfigChangeWatcher(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * Notify the watcher, the new value received.
     *
     * @param value of new.
     */
    // 配置变更通知对应的watcher
    public abstract void notify(ConfigChangeEvent value);

    /**
     * @return current value of current config.
     */
    public abstract String value();

    @Override
    public String toString() {
        return "AgentConfigChangeWatcher{" +
            "propertyKey='" + propertyKey + '\'' +
            '}';
    }
    // 配置变更事件
    @Getter
    @RequiredArgsConstructor
    public static class ConfigChangeEvent {
        // 新的配置值
        private final String newValue;
        // 事件类型
        private final EventType eventType;
    }

    public enum EventType {
        ADD, MODIFY, DELETE
    }
}
