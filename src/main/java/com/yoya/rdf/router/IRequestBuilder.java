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

import java.util.Map;

/**
 * Created by baihw on 16-6-3.
 *
 * 请求对象构建器规范接口
 */
public interface IRequestBuilder{

	/**
	 * 设置请求唯一标识
	 * 
	 * @param requestId 请求唯一标识
	 * @return 构建器对象
	 */
	IRequestBuilder setRequestId( String requestId );

	/**
	 * 设置请求路径
	 * 
	 * @param path 请求路径
	 * @return 构建器对象
	 */
	IRequestBuilder setPath( String path );

	/**
	 * 增加头信息数据
	 * 
	 * @param name 头信息名称
	 * @param value 头信息值
	 * @return 构建器对象
	 */
	IRequestBuilder addHeader( String name, String value );

	/**
	 * 批量增加头信息数据
	 * 
	 * @param headers 头信息数据
	 * @return 构建器对象
	 */
	IRequestBuilder addHeaders( Map<String, String> headers );

	/**
	 * 增加参数信息数据
	 * 
	 * @param name 参数名称
	 * @param value 参数值
	 * @return 构建器对象
	 */
	IRequestBuilder addParameter( String name, String value );

	/**
	 * 批量增加参数信息数据
	 * 
	 * @param parameters 参数数据
	 * @return 构建器对象
	 */
	IRequestBuilder addParameters( Map<String, String> parameters );

	/**
	 * 根据设置信息，构建请求对象实例。
	 * 
	 * @return 请求对象实例
	 */
	IRequest build();

} // end class
