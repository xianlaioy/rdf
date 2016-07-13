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
import com.yoya.rdf.router.filter.IFilterConfig;
import com.yoya.rdf.router.filter.IRequestFilter;

/**
 * Created by baihw on 16-7-2.
 *
 * 用户登陆状态检查过滤器。
 */
public class LoginFilter implements IRequestFilter{

	@Override
	public void init( IFilterConfig filterConfig ){
		// TODO Auto-generated method stub

	}

	@Override
	public boolean filter( IRequest request, IResponse response, IFilterChain chain ) throws Exception{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void destroy(){
		// TODO Auto-generated method stub

	}

}  // end class