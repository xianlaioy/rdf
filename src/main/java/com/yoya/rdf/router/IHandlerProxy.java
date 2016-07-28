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
package com.yoya.rdf.router;

import java.util.Set;

/**
 * Created by baihw on 16-4-15.
 *
 * 统一的请求处理器代理执行者规范接口
 */
public interface IHandlerProxy{

	/**
	 * 没有指定要执行的方法时，采用的默认方法名
	 */
	String	DEF_METHOD		= "handle";

	/**
	 * 每个处理器都会被增加的一个获取所有导出方法名的方法名称。
	 */
	String	KEY_METHODNAMES	= "_methodNames";

	/**
	 * 是否存在指定名称的外部可访问方法
	 *
	 * @param methodName 方法名
	 * @return true / false
	 */
	boolean hasMethod( String methodName );

	/**
	 * 获取所有导出的外部可访问方法名称。
	 *
	 * @return 方法名称集合。
	 */
	Set<String> getMethodNames();

	/**
	 * 执行指定的方法处理逻辑。
	 *
	 * @param methodName 方法名
	 * @param request 请求对象
	 * @param response 响应对象
	 */
	void doMethod( String methodName, IRequest request, IResponse response );

} // end class
