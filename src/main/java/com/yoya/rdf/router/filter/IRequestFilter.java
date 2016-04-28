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

package com.yoya.rdf.router.filter;

import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.IResponse;

/**
 * Created by baihw on 16-4-14.
 *
 * 统一的请求过滤器规范接口。
 */
public interface IRequestFilter{

	/**
	 * 初始化动作处理。
	 *
	 * @param filterConfig 配置对象
	 */
	void init( IFilterConfig filterConfig );

	/**
	 * 请求过滤逻辑
	 *
	 * @param request 请求对象
	 * @param response 响应对象
	 * @param chain 过滤器执行链
	 * @return 如果返回true,则后续过滤器不再被继续执行。
	 * @throws Exception 异常对象
	 */
	boolean filter( IRequest request, IResponse response, IFilterChain chain ) throws Exception;

	/**
	 * 退出时的销毁动作处理。
	 */
	void destroy();

}
