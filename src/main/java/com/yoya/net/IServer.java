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
package com.yoya.net;

import java.io.Closeable;

/**
 * Created by baihw on 16-5-25.
 *
 * 网络通信服务端规范接口。
 */
public interface IServer extends Closeable{

	/**
	 * 默认主机
	 */
	String	DEF_HOST	= "0.0.0.0";

	/**
	 * 默认端口
	 */
	int		DEF_PORT	= 9999;

	/**
	 * 默认超时时间。
	 */
	int		DEF_IDLE	= 180;

	/**
	 * 启动服务
	 */
	void start();

	/**
	 * @return 服务是否已经启动
	 */
	boolean isStarted();

} // end class
