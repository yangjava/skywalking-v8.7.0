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

import java.util.Map;
import org.apache.skywalking.apm.agent.core.context.AsyncSpan;
import org.apache.skywalking.apm.agent.core.context.tag.AbstractTag;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.network.trace.component.Component;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;

/**
 * The <code>AbstractSpan</code> represents the span's skeleton, which contains all open methods.
 */
// AbstractSpan 也是一个接口，其中定义了Span的基本行为，AbstractSpan定义了Span的骨架
public interface AbstractSpan extends AsyncSpan {
    /**
     * Set the component id, which defines in {@link ComponentsDefine}
     *
     * @return the span for chaining.
     */
    // 设置组件指定当前span表示的操作发生在哪个插件上。在ComponentsDefine中可以找到Skywalking目前支持的组件。
    AbstractSpan setComponent(Component component);
    // 设置SpanLayer，也就是当前Span所处的层次。指定当前span表示的操作所在的插件属于哪一种skywalking划分的类型
    // SpanLayer 是个枚举，可选项有 DB、RPC_FRAMEWORK、HTTP、MQ、CACHE。
    AbstractSpan setLayer(SpanLayer layer);

    /**
     * Set a key:value tag on the Span.
     *
     * @return this Span instance, for chaining
     * @deprecated use {@link #tag(AbstractTag, String)} in companion with {@link Tags#ofKey(String)} instead
     */
    @Deprecated
    AbstractSpan tag(String key, String value);

    /**
     *
     */
    // 为当前 Span 添加键值对的标签。一个 Span 可以投多个标签，AbstractTag 中就封装了 String 类型的 Key。
    AbstractSpan tag(AbstractTag<?> tag, String value);

    /**
     * Record an exception event of the current walltime timestamp.
     * 记录异常,时间使用当前本地时间
     *     wallTime:挂钟时间,本地时间
     *     serverTime:服务器时间
     * @param t any subclass of {@link Throwable}, which occurs in this span.
     * @return the Span, for chaining
     */
    // 记录当前 Span 中发生的关键日志，一个 Span 可以包含多条日志。
    AbstractSpan log(Throwable t);

    AbstractSpan errorOccurred();

    /**
     * @return true if the actual span is an entry span.
     */
    // 当前是否是入口 Span。
    boolean isEntry();

    /**
     * @return true if the actual span is an exit span.
     */
    // 当前是否是出口 Span。
    boolean isExit();

    /**
     * Record an event at a specific timestamp.
     *
     * @param timestamp The explicit timestamp for the log record.
     * @param event     the events
     * @return the Span, for chaining
     */
    AbstractSpan log(long timestamp, Map<String, ?> event);

    /**
     * Sets the string name for the logical operation this span represents.
     *
     * @return this Span instance, for chaining
     */
    // 设置操作名如果当前span的操作是一个http请求,那么operationName就是请求的url;一条sql语句,那么operationName就是sql;一个redis操作,那么operationName就是redis命令
    AbstractSpan setOperationName(String operationName);

    /**
     * Start a span.
     *
     * @return this Span instance, for chaining
     */
    // 开始Span。其实就是设置当前 Span的开始时间以及调用层级等信息。
    AbstractSpan start();

    /**
     * Get the id of span
     *
     * @return id value.
     */
    // 获得当前 Span 的编号，Span 编号是一个整数，在 TraceSegment 内唯一，从 0 开始自增，在创建 Span 对象时生成。
    int getSpanId();

    String getOperationName();

    /**
     * Reference other trace segment.
     *
     * @param ref segment ref
     */
    // 设置关联的 TraceSegment
    void ref(TraceSegmentRef ref);

    AbstractSpan start(long startTime);

    //什么叫peer,就是对端地址一个请求可能跨多个进程,操作多种中间件,那么每一次RPC,对面的服务的地址就是remotePeer每一次中间件的操作,中间件的地址就是remotePeer
    AbstractSpan setPeer(String remotePeer);

    /**
     * @return true if the span's owner(tracing context main thread) is been profiled.
     */
    boolean isProfiling();

    /**
     * Should skip analysis in the backend.
     */
    void skipAnalysis();
}
