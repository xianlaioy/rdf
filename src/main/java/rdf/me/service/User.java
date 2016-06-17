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
package rdf.me.service;

import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.IRequestHandler;
import com.yoya.rdf.router.IResponse;
import com.yoya.sql.SqlRunner;

/**
 * Created by baihw on 16-6-6.
 *
 * 用户相关操作请求处理服务实现。
 */
public class User implements IRequestHandler{

	@Override
	public void handle( IRequest request, IResponse response ){
		System.out.println( "User handle...." );
		Object result = SqlRunner.impl().queryScalar( "select server_address from sys_registry" );
		System.out.println( "sqlResult:" + result );
		response.setData( "reply to: " + request.getPath() );
	}

} // end class
