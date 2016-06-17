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
package com.yoya.net.rpc;

import com.yoya.rdf.router.IRequest;
import com.yoya.struct.ICommandResult;

/**
 * Created by baihw on 16-5-30.
 *
 * rpc客户端规范接口。
 */
public interface IRpcClient{

	/**
	 * 向服务端发送请求，并同步等待获取结果。
	 * 
	 * @param request 请求对象
	 * @return 响应对象
	 */
	ICommandResult send( IRequest request );

	/**
	 * 关闭建立的连接，释放资源 。
	 */
	void close();

} // end class
