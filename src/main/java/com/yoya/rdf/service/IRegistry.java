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
package com.yoya.rdf.service;

import java.net.InetSocketAddress;

/**
 * Created by baihw on 16-6-6.
 *
 * 统一的服务注册中心规范接口。
 * 
 * 
 * 注册中心管理着所有服务的身份信息(AK)及通信密钥(SK); 所有服务的网络通信地址。 所有服务的授权访问信息（各服务之间的访问权限）。
 * 
 * 典型的工作流程如下：
 * 
 * .服务启动时向注册中心注册自己的服务地址。（有独立负载均衡器的也可以提前录入负载均衡器地址）
 * 
 * .发起服务调用时，先从注册中心获取被调用服务的网络通信地址。
 * 
 * .使用获取到的服务方网络通信地址建立连接。
 * 
 * .连接建立后，使用自己的通信密钥(SK)加密检查签名信息的字符串('CHECK_SIGN'+时间戳)以验证身份。
 * 
 * .身份验证成功后，开始调用服务方提供的服务。
 */
public interface IRegistry{

	/**
	 * 注册服务
	 * 
	 * @param id 服务标识
	 * @param address 主机地址
	 */
	void register( String id, InetSocketAddress address );

	/**
	 * 取消注册服务
	 * 
	 * @param id 服务标识
	 * @param address 主机地址
	 */
	void unRegister( String id, InetSocketAddress address );

	/**
	 * 获取指定标识的服务可用主机地址
	 * 
	 * @param id 服务标识
	 * @return 主机地址
	 */
	InetSocketAddress getAdress( String id );

	/**
	 * 为指定数据计算签名
	 * 
	 * @param data 签名数据
	 * @return 签名结果
	 */
	String sign( String data );

	/**
	 * 检查指定应用传递来的签名是否正确
	 * 
	 * @param id 签名者标识
	 * @param data 原始数据
	 * @param sign 签名信息
	 * @return true / false
	 */
	boolean checkSign( String id, String data, String sign );

} // end class
