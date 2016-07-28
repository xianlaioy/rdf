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

/**
 * Created by baihw on 16-5-29.
 * 
 * 网络通信客户端规范接口。
 */
public interface IClient{

	/**
	 * 使用默认设置建立通信连接
	 */
	void connect();

	/**
	 * 发送消息
	 * 
	 * @param msg 消息对象
	 */
	void send( Object msg );

	/**
	 * 断开通信连接
	 */
	void disConnect();

	/**
	 * @return 当前连接的主机地址
	 */
	String getHost();

	/**
	 * @return 当前连接的端口
	 */
	int getPort();

} // end class
