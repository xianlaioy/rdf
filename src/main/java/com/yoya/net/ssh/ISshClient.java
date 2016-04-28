/*
 *  Copyright (c) 2016, baihw (javakf@163.com).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.yoya.net.ssh;

/**
 * Created by baihw on 16-4-23.
 *
 * sshClient规范接口。
 */
public interface ISshClient{

	/**
	 * 执行指定的命令，得到返回结果。
	 *
	 * @param command 命令文本
	 * @return 响应数据
	 * @throws SshException 当退出码不为0时表示执行出错，抛出次异常。
	 */
	String execCommand( String command ) throws SshException;
}
