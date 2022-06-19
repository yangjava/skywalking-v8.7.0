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

package org.apache.skywalking.apm.agent.core.context.trace;

import java.util.LinkedList;
import java.util.List;
import org.apache.skywalking.apm.agent.core.conf.Config;
import org.apache.skywalking.apm.agent.core.context.ids.DistributedTraceId;
import org.apache.skywalking.apm.agent.core.context.ids.GlobalIdGenerator;
import org.apache.skywalking.apm.agent.core.context.ids.NewDistributedTraceId;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;

/**
 * {@link TraceSegment} is a segment or fragment of the distributed trace. See https://github.com/opentracing/specification/blob/master/specification.md#the-opentracing-data-model
 * A {@link TraceSegment} means the segment, which exists in current {@link Thread}. And the distributed trace is formed
 * by multi {@link TraceSegment}s, because the distributed trace crosses multi-processes, multi-threads. <p>
 */
// Trace Segment 是 SkyWalking 中特有的概念，通常指在支持多线程的语言中，一个线程中归属于同一个操作的所有 Span 的聚合。这些 Span 具有相同的唯一标识SegmentID。
// Trace不是一个具体的数据模型,而是多个Segment串起来表示的逻辑对象
public class TraceSegment {
    /**
     * The id of this trace segment. Every segment has its unique-global-id.
     */
    // 每个segment都有一个全局唯一的id
    private String traceSegmentId;

    /**
     * The refs of parent trace segments, except the primary one. For most RPC call, {@link #ref} contains only one
     * element, but if this segment is a start span of batch process, the segment faces multi parents, at this moment,
     * we only cache the first parent segment reference.
     * <p>
     * This field will not be serialized. Keeping this field is only for quick accessing.
     */
    // 此 Trace Segment 的上游引用。对于大多数上游是 RPC 调用的情况，Refs只有一个元素，但如果是消息队列或批处理框架，上游可能会是多个应用服务，所以就会存在多个元素。
    private TraceSegmentRef ref;

    /**
     * The spans belong to this trace segment. They all have finished. All active spans are hold and controlled by
     * "skywalking-api" module.
     */
    // 用于存储，从属于此 Trace Segment 的 Span 的集合。
    private List<AbstractTracingSpan> spans;

    /**
     * The <code>relatedGlobalTraceId</code> represent the related trace. Most time it related only one
     * element, because only one parent {@link TraceSegment} exists, but, in batch scenario, the num becomes greater
     * than 1, also meaning multi-parents {@link TraceSegment}. But we only related the first parent TraceSegment.
     */
    // 此 Trace Segment 的 Trace Id。大多数时候它只包含一个元素，但如果是消息队列或批处理框架，上游是多个应用服务，会存在多个元素。
    private DistributedTraceId relatedGlobalTraceId;
    // 是否忽略。如果Ignore 为true，则此Trace Segment 不会上传到SkyWalking 后端。
    private boolean ignore = false;
    // 从属于此 Trace Segment 的 Span 数量限制，初始化大小可以通过config.agent.span_limit_per_segment 参数来配置，默认长度为300。若超过配置值，在创建新的 Span 的时候，会变成 NoopSpan。NoopSpan 表示没有任何实际操作的 Span 实现，用于保持内存和GC成本尽可能低。
    private boolean isSizeLimited = false;
    // 此 Trace Segment 的创建时间。
    private final long createTime;

    /**
     * Create a default/empty trace segment, with current time as start time, and generate a new segment id.
     */
    // 创建一个空的trace segment,生成一个新的segment id
    public TraceSegment() {
        this.traceSegmentId = GlobalIdGenerator.generate();
        this.spans = new LinkedList<>();
        this.relatedGlobalTraceId = new NewDistributedTraceId();
        this.createTime = System.currentTimeMillis();
    }

    /**
     * Establish the link between this segment and its parents.
     *
     * @param refSegment {@link TraceSegmentRef}
     */
    // 设置当前segment的parent segment
    public void ref(TraceSegmentRef refSegment) {
        if (null == ref) {
            this.ref = refSegment;
        }
    }

    /**
     * Establish the line between this segment and the relative global trace id.
     */
    // 将当前segment关联到某一条trace上
    public void relatedGlobalTrace(DistributedTraceId distributedTraceId) {
        if (relatedGlobalTraceId instanceof NewDistributedTraceId) {
            this.relatedGlobalTraceId = distributedTraceId;
        }
    }

    /**
     * After {@link AbstractSpan} is finished, as be controller by "skywalking-api" module, notify the {@link
     * TraceSegment} to archive it.
     */
    // 添加span
    public void archive(AbstractTracingSpan finishedSpan) {
        spans.add(finishedSpan);
    }

    /**
     * Finish this {@link TraceSegment}. <p> return this, for chaining
     */
    // finish()需要传入isSizeLimited，SkyWalking中限制了Segment中Span的数量，默认是最大是300，上层调用finish()方法时会判断此时该Segment中的Span是否到达上限
    public TraceSegment finish(boolean isSizeLimited) {
        this.isSizeLimited = isSizeLimited;
        return this;
    }

    public String getTraceSegmentId() {
        return traceSegmentId;
    }

    /**
     * Get the first parent segment reference.
     */
    public TraceSegmentRef getRef() {
        return ref;
    }

    public DistributedTraceId getRelatedGlobalTrace() {
        return relatedGlobalTraceId;
    }

    public boolean isSingleSpanSegment() {
        return this.spans != null && this.spans.size() == 1;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    /**
     * This is a high CPU cost method, only called when sending to collector or test cases.
     *
     * @return the segment as GRPC service parameter
     */
    public SegmentObject transform() {
        SegmentObject.Builder traceSegmentBuilder = SegmentObject.newBuilder();
        traceSegmentBuilder.setTraceId(getRelatedGlobalTrace().getId());
        /*
         * Trace Segment
         */
        traceSegmentBuilder.setTraceSegmentId(this.traceSegmentId);
        // Don't serialize TraceSegmentReference

        // SpanObject
        for (AbstractTracingSpan span : this.spans) {
            traceSegmentBuilder.addSpans(span.transform());
        }
        traceSegmentBuilder.setService(Config.Agent.SERVICE_NAME);
        traceSegmentBuilder.setServiceInstance(Config.Agent.INSTANCE_NAME);
        traceSegmentBuilder.setIsSizeLimited(this.isSizeLimited);

        return traceSegmentBuilder.build();
    }

    @Override
    public String toString() {
        return "TraceSegment{" + "traceSegmentId='" + traceSegmentId + '\'' + ", ref=" + ref + ", spans=" + spans + "}";
    }

    public long createTime() {
        return this.createTime;
    }
}
