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

import com.yoya.rdf.router.filter.IRequestFilter;

/**
 * Created by baihw on 16-4-14.
 *
 * 统一的路由处理器规范接口。
 */
public interface IRouter<REQ extends IRequest, RES extends IResponse>{

	/**
	 * 此组件使用的配置组名称。
	 */
	String	CONFIG_GROUP	= "router";

	/**
	 * 次组件使用的实现者名称配置关键字。
	 */
	String	KEY_IMPL		= "impl";

	/**
	 * 配置工作基准目录。
	 * 
	 * @param workBase 工作基准目录
	 */
	void configWrokBase( String workBase );

	/**
	 * 请求路由处理
	 * 
	 * @param request 请求对象
	 * @param response 响应对象
	 */
	void route( REQ request, RES response );

	/**
	 * 增加一个指定访问路径拦截映射的过滤器对象
	 *
	 * @param url 访问路径
	 * @param filter 过滤器对象
	 */
	void addMappingFilter( String url, IRequestFilter filter );

}
