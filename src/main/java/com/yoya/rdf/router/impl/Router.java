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
package com.yoya.rdf.router.impl;

import com.yoya.rdf.router.IRequest;

/**
 * Created by baihw on 16-7-20.
 *
 * 路由操作门面接口。
 */
public final class Router{

	// 当前线程对应的请求对象。
	private static final ThreadLocal<IRequest> _TL_REQ = new ThreadLocal<>();

	/**
	 * @return 当前环境的请求对象。
	 */
	public static IRequest getRequest(){
		return _TL_REQ.get();
	}

	/**
	 * 设置当前环境的请求对象。
	 * 
	 * @param request 请求对象
	 */
	static void setRequest( IRequest request ){
		if( null == _TL_REQ.get() ){
			_TL_REQ.set( request );
		}
	}

	/**
	 * 移除当前环境的请求对象。
	 */
	static void removeRequest(){
		_TL_REQ.remove();
	}

} // end class
