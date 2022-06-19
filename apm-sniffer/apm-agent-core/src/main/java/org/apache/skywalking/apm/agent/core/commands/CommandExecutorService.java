/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.skywalking.apm.agent.core.commands;

import java.util.HashMap;
import java.util.Map;
import org.apache.skywalking.apm.agent.core.boot.BootService;
import org.apache.skywalking.apm.agent.core.boot.DefaultImplementor;
import org.apache.skywalking.apm.agent.core.commands.executor.ConfigurationDiscoveryCommandExecutor;
import org.apache.skywalking.apm.agent.core.commands.executor.NoopCommandExecutor;
import org.apache.skywalking.apm.agent.core.commands.executor.ProfileTaskCommandExecutor;
import org.apache.skywalking.apm.network.trace.component.command.BaseCommand;
import org.apache.skywalking.apm.network.trace.component.command.ConfigurationDiscoveryCommand;
import org.apache.skywalking.apm.network.trace.component.command.ProfileTaskCommand;

/**
 * Command executor service, acts like a routing executor that controls all commands' execution, is responsible for
 * managing all the mappings between commands and their executors, one can simply invoke {@link #execute(BaseCommand)}
 * and it will routes the command to corresponding executor.
 * <p>
 * Registering command executor for new command in {@link #commandExecutorMap} is required to support new command.
 */
// 命令执行器控制所有的命令执行，主要是通过Map<命令，命令执行器>中查找到命令执行器，然后执行
@DefaultImplementor
public class CommandExecutorService implements BootService, CommandExecutor {
    // key: 命令的名字 value:对应的命令执行器
    private Map<String, CommandExecutor> commandExecutorMap;

    // 准备: 创建commandExecutorMap
    @Override
    public void prepare() throws Throwable {
        commandExecutorMap = new HashMap<String, CommandExecutor>();

        // Profile task executor
        // 性能追踪命令执行器
        commandExecutorMap.put(ProfileTaskCommand.NAME, new ProfileTaskCommandExecutor());

        //Get ConfigurationDiscoveryCommand executor.
        // 配置变更命令执行器
        commandExecutorMap.put(ConfigurationDiscoveryCommand.NAME, new ConfigurationDiscoveryCommandExecutor());
    }

    @Override
    public void boot() throws Throwable {

    }

    @Override
    public void onComplete() throws Throwable {

    }

    @Override
    public void shutdown() throws Throwable {

    }

    // 执行命令
    @Override
    public void execute(final BaseCommand command) throws CommandExecutionException {
        executorForCommand(command).execute(command);
    }

    private CommandExecutor executorForCommand(final BaseCommand command) {
        // 根据command类型找到对应的命令执行器
        final CommandExecutor executor = commandExecutorMap.get(command.getCommand());
        if (executor != null) {
            return executor;
        }
        // 都不存在执行空命令执行器
        return NoopCommandExecutor.INSTANCE;
    }
}
