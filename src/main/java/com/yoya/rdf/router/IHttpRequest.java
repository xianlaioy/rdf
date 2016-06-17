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

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by baihw on 16-6-2.
 *
 * 特定于Http协议的统一请求对象规范接口
 */
public interface IHttpRequest extends IRequest{

	/**
	 * 支持的请求方法
	 *
	 * @author baihw
	 *
	 */
	enum Method{

		/** 向指定资源提交数据进行处理请求（例如提交表单或者上传文件）。数据被包含在请求体中。POST请求可能会导致新的资源的建立和/或已有资源的修改。 **/
		POST,

		/** 向特定的资源发出请求。注意：GET方法不应当被用于产生“副作用”的操作中。 **/
		GET,

		/** 向指定资源位置上传其最新内容。 **/
		PUT,

		/** 请求服务器删除Request-URI所标识的资源。 **/
		DELETE,

		/** 向服务器索要与GET请求相一致的响应，只不过响应体将不会被返回。这一方法可以在不必传输整个响应内容的情况下，就可以获取包含在响应消息头中的元信息。 **/
		HEAD,

		/** 返回服务器针对特定资源所支持的HTTP请求方法 **/
		OPTIONS,

		/** 回显服务器收到的请求，主要用于测试或诊断。 **/
		TRACE,

		/** HTTP/1.1协议中预留给能够将连接改为管道方式的代理服务器。 **/
		CONNECT

	}

	/**
	 * 获取客户端请求数据字符串
	 *
	 * @return 字符串形式解析出的消息内容数据
	 */
	String getBody();

	/**
	 * 获取客户端请求数据字节
	 *
	 * @return 消息内容原始数据
	 */
	byte[] getBodyData();

	/**
	 * 获取上传文件的相对路径集合的方法。
	 *
	 * @param uploadDir 上传文件保存的目录名称
	 * @param maxPostSize 上传文件的大小限制
	 * @return 最终保存的相对路径，获取文件最终路径时使用getUploadFile方法传入此路径获取。
	 */
	List<String> getUploadFiles( String uploadDir, int maxPostSize );

	/**
	 * 根据上传文件时返回的相对路径获取文件的最终路径。
	 *
	 * @param uploadFileName 上传文件时返回的相对路径
	 * @return 最终文件路径。
	 */
	File getUploadFile( String uploadFileName );

	/**
	 * 获取请求中所有的cookie数据集合
	 * 
	 * @return cookie数据集合
	 */
	Map<String, String> getCookies();

	/**
	 * 获取请求中指定名称的cookie数据
	 * 
	 * @param cookieName cookie名称
	 * @return cookie值
	 */
	String getCookie( String cookieName );

	/**
	 * 获取请求中指定名称的cookie数据
	 * 
	 * @param cookieName cookie名称
	 * @param defValue 值为null时返回的默认值
	 * @return cookie值
	 */
	String getCookie( String cookieName, String defValue );
	
	/**
	 * 检查是否存在指定名称的cookie信息
	 * 
	 * @param cookieName cookie名称
	 * @return 存在返回 true / 不存在返回 false
	 */
	boolean hasCookie( String cookieName );

}
