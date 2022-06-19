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

package org.apache.skywalking.apm.agent.core.context.ids;

import java.util.UUID;

import org.apache.skywalking.apm.util.StringUtil;
// 全局ID生成器
public final class GlobalIdGenerator {
    // PROCESS_ID使用UUID生成器
    private static final String PROCESS_ID = UUID.randomUUID().toString().replaceAll("-", "");
    // 用ThreadLocal作为容器，当每个线程访问这个 THREAD_ID_SEQUENCE 变量时，ThreadLocal会为每个线程提供一份变量，各个线程互不影响。
    private static final ThreadLocal<IDContext> THREAD_ID_SEQUENCE = ThreadLocal.withInitial(
        () -> new IDContext(System.currentTimeMillis(), (short) 0));

    private GlobalIdGenerator() {
    }

    /**
     * Generate a new id, combined by three parts.
     * 生成一个新的id由三部分组成
     * <p>
     * The first one represents application instance id.
     * 第一部分表示应用实例的id
     * <p>
     * The second one represents thread id.
     * 第二部分表示线程id
     * <p>
     * The third one also has two parts, 1) a timestamp, measured in milliseconds 2) a seq, in current thread, between
     * 0(included) and 9999(included)
     * 第三部分包含两部分,一个毫秒级的时间戳+一个当前线程里的序号(0-9999不断循环)
     *
     * @return unique id to represent a trace or segment
     */
    // 生成新的ID,分为3个部分：应用实例ID(application instance id),线程ID(thread id),Context序列(timestamp+seq)
    public static String generate() {
        return StringUtil.join(
            '.',
            PROCESS_ID,
            String.valueOf(Thread.currentThread().getId()),
            String.valueOf(THREAD_ID_SEQUENCE.get().nextSeq())
        );
    }

    private static class IDContext {
        // 上次生成sequence的时间戳
        private long lastTimestamp;
        // 线程的序列号
        private short threadSeq;

        // Just for considering time-shift-back only.
        // 时钟回拨
        private long lastShiftTimestamp;
        private int lastShiftValue;

        private IDContext(long lastTimestamp, short threadSeq) {
            this.lastTimestamp = lastTimestamp;
            this.threadSeq = threadSeq;
        }
        // 时间戳 * 10000 + 线程的序列号
        private long nextSeq() {
            return timestamp() * 10000 + nextThreadSeq();
        }

        private long timestamp() {
            long currentTimeMillis = System.currentTimeMillis();
            // 发生了时钟回拨
            if (currentTimeMillis < lastTimestamp) {
                // Just for considering time-shift-back by Ops or OS. @hanahmily 's suggestion.
                if (lastShiftTimestamp != currentTimeMillis) {
                    lastShiftValue++;
                    lastShiftTimestamp = currentTimeMillis;
                }
                return lastShiftValue;
            } else {
                // 时钟正常
                lastTimestamp = currentTimeMillis;
                return lastTimestamp;
            }
        }

        private short nextThreadSeq() {
            if (threadSeq == 10000) {
                threadSeq = 0;
            }
            return threadSeq++;
        }
    }
}
