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

package org.apache.skywalking.oap.server.core;

import java.util.ArrayList;
import java.util.List;
import org.apache.skywalking.oap.server.core.analysis.meter.MeterSystem;
import org.apache.skywalking.oap.server.core.cache.NetworkAddressAliasCache;
import org.apache.skywalking.oap.server.core.cache.ProfileTaskCache;
import org.apache.skywalking.oap.server.core.command.CommandService;
import org.apache.skywalking.oap.server.core.config.ConfigService;
import org.apache.skywalking.oap.server.core.config.DownSamplingConfigService;
import org.apache.skywalking.oap.server.core.config.IComponentLibraryCatalogService;
import org.apache.skywalking.oap.server.core.config.NamingControl;
import org.apache.skywalking.oap.server.core.management.ui.template.UITemplateManagementService;
import org.apache.skywalking.oap.server.core.oal.rt.OALEngineLoaderService;
import org.apache.skywalking.oap.server.core.profile.ProfileTaskMutationService;
import org.apache.skywalking.oap.server.core.query.AggregationQueryService;
import org.apache.skywalking.oap.server.core.query.AlarmQueryService;
import org.apache.skywalking.oap.server.core.query.BrowserLogQueryService;
import org.apache.skywalking.oap.server.core.query.EventQueryService;
import org.apache.skywalking.oap.server.core.query.LogQueryService;
import org.apache.skywalking.oap.server.core.query.MetadataQueryService;
import org.apache.skywalking.oap.server.core.query.MetricsMetadataQueryService;
import org.apache.skywalking.oap.server.core.query.MetricsQueryService;
import org.apache.skywalking.oap.server.core.query.ProfileTaskQueryService;
import org.apache.skywalking.oap.server.core.query.TopNRecordsQueryService;
import org.apache.skywalking.oap.server.core.query.TopologyQueryService;
import org.apache.skywalking.oap.server.core.query.TraceQueryService;
import org.apache.skywalking.oap.server.core.remote.RemoteSenderService;
import org.apache.skywalking.oap.server.core.remote.client.RemoteClientManager;
import org.apache.skywalking.oap.server.core.server.GRPCHandlerRegister;
import org.apache.skywalking.oap.server.core.server.JettyHandlerRegister;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.core.storage.model.IModelManager;
import org.apache.skywalking.oap.server.core.storage.model.ModelCreator;
import org.apache.skywalking.oap.server.core.storage.model.ModelManipulator;
import org.apache.skywalking.oap.server.core.worker.IWorkerInstanceGetter;
import org.apache.skywalking.oap.server.core.worker.IWorkerInstanceSetter;
import org.apache.skywalking.oap.server.library.module.ModuleDefine;

/**
 * Core module definition. Define all open services to other modules.
 */
public class CoreModule extends ModuleDefine {
    public static final String NAME = "core";

    public CoreModule() {
        super(NAME);
    }

    @Override
    public Class[] services() {
        List<Class> classes = new ArrayList<>();
        // 配置服务
        classes.add(ConfigService.class);
        // 降采样
        classes.add(DownSamplingConfigService.class);
        // 名称管理服务
        classes.add(NamingControl.class);
        classes.add(IComponentLibraryCatalogService.class);

        // 远程工作实例获取类
        classes.add(IWorkerInstanceGetter.class);
        // 远程工作实例配置类
        classes.add(IWorkerInstanceSetter.class);

        classes.add(MeterSystem.class);

        //添加服务注册类
        addServerInterface(classes);
        addReceiverInterface(classes);
        addInsideService(classes);
        addCacheService(classes);
        addQueryService(classes);
        addProfileService(classes);
        addOALService(classes);
        addManagementService(classes);

        classes.add(CommandService.class);

        return classes.toArray(new Class[]{});
    }

    private void addManagementService(List<Class> classes) {
        classes.add(UITemplateManagementService.class);
    }

    private void addProfileService(List<Class> classes) {
        classes.add(ProfileTaskMutationService.class);
        classes.add(ProfileTaskQueryService.class);
        classes.add(ProfileTaskCache.class);
    }

    private void addOALService(List<Class> classes) {
        classes.add(OALEngineLoaderService.class);
    }

    private void addQueryService(List<Class> classes) {
        classes.add(TopologyQueryService.class);
        classes.add(MetricsMetadataQueryService.class);
        classes.add(MetricsQueryService.class);
        classes.add(TraceQueryService.class);
        classes.add(LogQueryService.class);
        classes.add(MetadataQueryService.class);
        classes.add(AggregationQueryService.class);
        classes.add(AlarmQueryService.class);
        classes.add(TopNRecordsQueryService.class);
        classes.add(BrowserLogQueryService.class);
        classes.add(EventQueryService.class);
    }

    // 负责内置GRPC和jetty服务器,并允许向服务器注册handler接收不同业务消息
    private void addServerInterface(List<Class> classes) {
        // 负责GRPC协议处理
        classes.add(GRPCHandlerRegister.class);
        // 负责Jetty协议处理
        classes.add(JettyHandlerRegister.class);
    }

    private void addInsideService(List<Class> classes) {
        classes.add(ModelCreator.class);
        classes.add(IModelManager.class);
        classes.add(ModelManipulator.class);
        classes.add(RemoteClientManager.class);
        classes.add(RemoteSenderService.class);
    }

    // 负责读取数据时的存储层缓存,降低对存储的压力
    private void addCacheService(List<Class> classes) {
        classes.add(NetworkAddressAliasCache.class);
    }

    // SourceReceiver底层通过分发器指定流式数据的Processor处理器
    private void addReceiverInterface(List<Class> classes) {
        classes.add(SourceReceiver.class);
    }
}
