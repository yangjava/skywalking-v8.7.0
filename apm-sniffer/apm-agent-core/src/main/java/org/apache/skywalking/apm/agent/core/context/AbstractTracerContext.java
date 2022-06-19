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

package org.apache.skywalking.apm.agent.core.context;

import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;

/**
 * The <code>AbstractTracerContext</code> represents the tracer context manager.
 */
// AbstractTracerContext代表链路追踪过程上下文管理器
// inject()和extract()方法是在跨进程传播数据时使用的
// capture()和continued()方法是在跨线程传播数据时使用的
public interface AbstractTracerContext {
    /**
     * Prepare for the cross-process propagation. How to initialize the carrier, depends on the implementation.
     *
     * @param carrier to carry the context for crossing process.
     */
    // 为跨进程传播做好准备.将数据放到contextCarrier中
    void inject(ContextCarrier carrier);

    /**
     * Build the reference between this segment and a cross-process segment. How to build, depends on the
     * implementation.
     *
     * @param carrier carried the context from a cross-process segment.
     */
    // 在当前segment和跨进程segment之间构建引用
    void extract(ContextCarrier carrier);

    /**
     * Capture a snapshot for cross-thread propagation. It's a similar concept with ActiveSpan.Continuation in
     * OpenTracing-java How to build, depends on the implementation.
     *
     * @return the {@link ContextSnapshot} , which includes the reference context.
     */
    // 在跨线程传播时拍摄快照
    ContextSnapshot capture();

    /**
     * Build the reference between this segment and a cross-thread segment. How to build, depends on the
     * implementation.
     *
     * @param snapshot from {@link #capture()} in the parent thread.
     */
    // 在当前segment和跨线程segment之间构建引用
    void continued(ContextSnapshot snapshot);

    /**
     * Get the global trace id, if needEnhance. How to build, depends on the implementation.
     *
     * @return the string represents the id.
     */
    // 获取全局traceId
    String getReadablePrimaryTraceId();

    /**
     * Get the current segment id, if needEnhance. How to build, depends on the implementation.
     *
     * @return the string represents the id.
     */
    // 获取当前segmentId
    String getSegmentId();

    /**
     * Get the active span id, if needEnhance. How to build, depends on the implementation.
     *
     * @return the string represents the id.
     */
    // 获取active span id
    int getSpanId();

    /**
     * Create an entry span
     *
     * @param operationName most likely a service name
     * @return the span represents an entry point of this segment.
     */
    // 创建EntrySpan
    AbstractSpan createEntrySpan(String operationName);

    /**
     * Create a local span
     *
     * @param operationName most likely a local method signature, or business name.
     * @return the span represents a local logic block.
     */
    // 创建LocalSpan
    AbstractSpan createLocalSpan(String operationName);

    /**
     * Create an exit span
     *
     * @param operationName most likely a service name of remote
     * @param remotePeer    the network id(ip:port, hostname:port or ip1:port1,ip2,port, etc.). Remote peer could be set
     *                      later, but must be before injecting.
     * @return the span represent an exit point of this segment.
     */
    // 创建ExitSpan
    AbstractSpan createExitSpan(String operationName, String remotePeer);

    /**
     * @return the active span of current tracing context(stack)
     */
    // 拿到active span
    AbstractSpan activeSpan();

    /**
     * Finish the given span, and the given span should be the active span of current tracing context(stack)
     *
     * @param span to finish
     * @return true when context should be clear.
     */
    // 停止span,传入的span应该是active span
    boolean stopSpan(AbstractSpan span);

    /**
     * Notify this context, current span is going to be finished async in another thread.
     *
     * @return The current context
     */
    // 等待异步span结束
    AbstractTracerContext awaitFinishAsync();

    /**
     * The given span could be stopped officially.
     *
     * @param span to be stopped.
     */
    // 关闭异步span
    void asyncStop(AsyncSpan span);

    /**
     * Get current correlation context
     */
    CorrelationContext getCorrelationContext();
}
