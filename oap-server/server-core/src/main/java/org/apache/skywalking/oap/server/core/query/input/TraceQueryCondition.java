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

package org.apache.skywalking.oap.server.core.query.input;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.analysis.manual.searchtag.Tag;
import org.apache.skywalking.oap.server.core.query.type.Pagination;
import org.apache.skywalking.oap.server.core.query.type.QueryOrder;
import org.apache.skywalking.oap.server.core.query.type.TraceState;

// Trace查询条件
@Getter
@Setter
public class TraceQueryCondition {
    // 服务Id
    private String serviceId;
    // 服务实例Id
    private String serviceInstanceId;
    // 链路Id
    private String traceId;
    // 端点
    private String endpointName;
    // 端点Id
    private String endpointId;
    // 时间区间
    private Duration queryDuration;
    // 持续时间
    private int minTraceDuration;
    // 持续时间
    private int maxTraceDuration;
    // 链路状态
    private TraceState traceState;
    // 排序
    private QueryOrder queryOrder;
    // 分页参数
    private Pagination paging;
    //  链路Tag,支持多个查询
    private List<Tag> tags;
}
