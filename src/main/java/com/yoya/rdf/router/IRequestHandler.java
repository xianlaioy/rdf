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

/**
 * Created by baihw on 16-6-13.
 *
 * 统一的服务请求处理器规范接口，此接口用于标识类文件为请求处理器实现类，无必须实现的方法。
 *
 * 实现类中所有符合public void xxx( IRequest request, IResponse response )签名规则的方法都将被自动注册到对应的请求处理器中。
 * 
 * 如下所示：
 *
 * public void login( IRequest request, IResponse response ){};
 *
 * public void logout( IRequest request, IResponse response ){} ;
 *
 * 上边的login, logout将被自动注册到请求处理器路径中。
 *
 * 假如类名为LoginHnadler, 则注册的处理路径为:/Loginhandler/login, /Loginhandler/logout.
 *
 * 注意：大小写敏感。
 * 
 */
public interface IRequestHandler{

	/**
	 * 当请求中没有明确指定处理方法时，默认执行的请求处理方法。
	 *
	 * @param request 请求对象
	 * @param response 响应对象
	 */
	void handle( IRequest request, IResponse response );

} // end class
