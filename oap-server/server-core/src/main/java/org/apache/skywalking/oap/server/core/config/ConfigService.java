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

package org.apache.skywalking.oap.server.core.config;

import lombok.Getter;
import org.apache.skywalking.oap.server.core.CoreModuleConfig;
import org.apache.skywalking.oap.server.library.module.Service;
// 配置服务
@Getter
public class ConfigService implements Service {
    // GRPC的IP地址
    private final String gRPCHost;
    // GRPC的端口号
    private final int gRPCPort;
    // 可以查询的TracesTags
    private final String searchableTracesTags;
    // 可以查询的LogsTags
    private final String searchableLogsTags;
    // 可以查询的AlarmTags
    private final String searchableAlarmTags;

    public ConfigService(CoreModuleConfig moduleConfig) {
        this.gRPCHost = moduleConfig.getGRPCHost();
        this.gRPCPort = moduleConfig.getGRPCPort();
        this.searchableTracesTags = moduleConfig.getSearchableTracesTags();
        this.searchableLogsTags = moduleConfig.getSearchableLogsTags();
        this.searchableAlarmTags = moduleConfig.getSearchableAlarmTags();
    }
}
