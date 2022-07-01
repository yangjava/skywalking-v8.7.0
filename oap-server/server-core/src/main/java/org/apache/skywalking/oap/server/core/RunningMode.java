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

import com.google.common.base.Strings;

/**
 * The running mode of the OAP server.
 */
// 运行Mode init和no-init
public class RunningMode {
    private static String MODE = "";

    private RunningMode() {
    }

    public static void setMode(String mode) {
        if (Strings.isNullOrEmpty(mode)) {
            return;
        }
        RunningMode.MODE = mode.toLowerCase();
    }

    /**
     * Init mode, do all initialization things, and process should exit.
     *
     * @return true if in this status
     */
    public static boolean isInitMode() {
        return "init".equals(MODE);
    }

    /**
     * No-init mode, the oap just starts up, but wouldn't do storage init.
     *
     * @return true if in this status.
     */
    public static boolean isNoInitMode() {
        return "no-init".equals(MODE);
    }
}
