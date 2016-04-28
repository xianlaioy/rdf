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

package com.yoya.rdf.router.filter.impl;

import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.IResponse;
import com.yoya.rdf.router.filter.IFilterChain;
import com.yoya.rdf.router.filter.IRequestFilter;

/**
 * Created by baihw on 16-4-15.
 *
 * 一个简单的执行链对象默认实现。
 */
public class SimpleFilterChain implements IFilterChain{

	// 待执行的过滤器链表
	private final IRequestFilter[]	_filters;
	// 待执行的过滤器数量
	private final int				_filterCount;
	// 当前待执行的过滤器索引
	private int						_index	= 0;

	/**
	 * 构造函数
	 *
	 * @param filters 过滤器执行链对象
	 */
	public SimpleFilterChain( IRequestFilter[] filters ){
		if( null == filters || filters.length < 1 ){
			_filters = null;
			_filterCount = 0;
		}else{
			_filters = filters;
			_filterCount = _filters.length;
		}
	}

	/**
	 * 继续执行下一个过滤器动作。
	 *
	 * @param request 请求对象
	 * @param response 响应对象
	 * @return 是否结束，如果为true则整个请求流程将结束，不再执行后续动作。
	 */

	@Override
	public boolean doFilter( IRequest request, IResponse response ) throws Exception{
		if( _filterCount > _index ){
			IRequestFilter filter = _filters[_index++];
			return filter.filter( request, response, this );
		}
		return false;
	}
}
