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

import com.yoya.rdf.router.IRequest;
import com.yoya.struct.ICommandResult;

/**
 * Created by baihw on 16-6-6.
 *
 * 服务规范接口。
 */
public interface IService{

	/**
	 * 此组件使用的配置组名称。
	 */
	String	CONFIG_GROUP	= "service";

	/**
	 * 此组件使用的实现者名称配置关键字。
	 */
	String	KEY_IMPL		= "impl";

	/**
	 * 启动服务
	 */
	void start();

	/**
	 * 停止服务
	 */
	void stop();

	/**
	 * 调用指定服务获取响应数据。
	 * 
	 * @param serviceId 服务唯一标识
	 * @param request 请求对象
	 * @return 响应对象
	 */
	ICommandResult call( String serviceId, IRequest request );

} // end class
