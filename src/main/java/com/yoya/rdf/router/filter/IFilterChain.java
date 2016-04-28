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
 * 过滤器执行链规范接口
 */
public interface IFilterChain {

	/**
	 * 继续执行下一个过滤器动作。
	 *
	 * @param request 请求对象
	 * @param response 响应对象
	 * @return 是否结束，如果为true则整个请求流程将结束，不再执行后续动作。
	 * @throws Exception 异常信息
	 */
	public boolean doFilter( IRequest request, IResponse response ) throws Exception;

}
