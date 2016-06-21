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
 * Created by baihw on 16-4-14.
 *
 * 统一的响应对象规范接口
 */
public interface IResponse{

	/**
	 * 设置响应状态码
	 *
	 * @param status 状态码
	 * @return 当前对象
	 */
	IResponse setStatus( int status );

	/**
	 * @return 响应状态码
	 */
	int getStatus();

	/**
	 * 设置响应数据
	 * 
	 * @param data 响应数据对象
	 * @return 当前对象
	 */
	IResponse setData( Object data );

	/**
	 * 获取响应数据
	 *
	 * @return 响应数据
	 */
	Object getData();

	/**
	 * @return 获取文本类型响应数据
	 */
	String getDataString();

	/**
	 * 设置完整的数据
	 * 
	 * @param status 响应代码
	 * @param error 错误消息
	 * @param data 错误详情数据
	 * @return 当前对象
	 */
	IResponse setError( int status, String error, Object data );

	/**
	 * 设置响应错误信息
	 * 
	 * @param status 错误代码
	 * @param error 错误信息
	 * @return 当前对象
	 */
	IResponse setError( int status, String error );

	/**
	 * @return 获取错误信息
	 */
	String getError();

	/**
	 * @return 是否为成功的数据响应。
	 */
	boolean isOk();

	/**
	 * 200响应代码:正常响应
	 */
	public static final int	CODE_OK					= 200;

	/**
	 * 401响应代码:无效的请求
	 */
	public static final int	CODE_INVALID_REQUEST	= 400;

	/**
	 * 401响应代码:未授权
	 */
	public static final int	CODE_NOT_AUTHORIZED		= 401;

	/**
	 * 403响应代码:禁止访问
	 */
	public static final int	CODE_FORBIDDEN_ACCESS	= 403;

	/**
	 * 404响应代码:资源找不到
	 */
	public static final int	CODE_NOT_FOUND			= 404;

	/**
	 * 500响应代码
	 */
	public static final int	CODE_INTERNAL_ERROR		= 500;

}
