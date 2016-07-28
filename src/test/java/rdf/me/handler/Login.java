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
package rdf.me.handler;

import com.yoya.rdf.router.IHttpRequest;
import com.yoya.rdf.router.IHttpRequestHandler;
import com.yoya.rdf.router.IHttpResponse;

/**
 * Created by baihw on 16-7-20.
 *
 * 登陆逻辑处理器。
 */
public class Login implements IHttpRequestHandler{

	@Override
	public void handle( IHttpRequest request, IHttpResponse response ){
		boolean isLogin = request.getSession().getAttribute( "isLogin" );
		if( isLogin ){
			response.setDataByJsonCMD( "already login!" );
			return;
		}
		String loginId = request.getParameter( "loginId" );
		String loginPwd = request.getParameter( "loginPwd" );
		if( "admin".equals( loginId ) && "123456".equals( loginPwd ) ){
			request.getSession().setAttribute( "isLogin", true );
			response.setDataByJsonCMD( "admin, you are login!" );
		}else{
			response.setDataByJsonCMD( IHttpResponse.CODE_NOT_AUTHORIZED, "password authentication failed!" );
		}
	}

}
