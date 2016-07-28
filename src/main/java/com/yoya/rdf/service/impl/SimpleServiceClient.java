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
package com.yoya.rdf.service.impl;

import java.net.InetSocketAddress;

import com.yoya.net.rpc.IRpcClient;
import com.yoya.net.rpc.impl.SimpleRpcClient;
import com.yoya.rdf.router.IRequest;
import com.yoya.struct.ICommandResult;

/**
 * Created by baihw on 16-6-13.
 *
 * 服务调用的客户端实现，负责进行一些统一的处理。
 */
final class SimpleServiceClient{

	// rpc通信客户端实例
	private final IRpcClient _CLIENT;

	/**
	 * 使用指定选项创建实例
	 * 
	 * @param address 服务端连接地址
	 * @param useSign 是否使用签名机制
	 * @param waitTimeout 最长等待时间，单位：毫秒。
	 */
	SimpleServiceClient( InetSocketAddress address, int waitTimeout ){
		this._CLIENT = new SimpleRpcClient( address.getHostString(), address.getPort(), waitTimeout );
	}

	ICommandResult call( IRequest request ){
		return this._CLIENT.send( request );
	}

	void close(){
		this._CLIENT.close();
	}

} // end class
