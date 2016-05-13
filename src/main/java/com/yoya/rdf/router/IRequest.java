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
import java.util.Set;

/**
 * Created by baihw on 16-4-14.
 *
 * 统一的请求对象规范接口
 */
public interface IRequest{

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
	 * @return 当前请求的唯一标识。
	 */
	String getRequestId();

	/**
	 * 获取请求路径,如: "/a/b"
	 *
	 * @return 具体的请求路径
	 */
	String getPath();

	/**
	 * 获取指定名称的请求头信息
	 *
	 * @param header 头名称
	 * @return 头名称对应的值
	 */
	String getHeader( String header );

	/**
	 * 获取所有请求头名称集合
	 *
	 * @return 头名称集合
	 */
	Set<String> getHeaderNames();

	/**
	 * 获取所有参数名称集合
	 *
	 * @return 参数名称集合
	 */
	Set<String> getParameterNames();

	/**
	 * 获取所有参数集合
	 *
	 * @return 参数集合
	 */
	Map<String, String> getParameters();

	/**
	 * 获取指定名称的参数首选值(如果有多个值返回第一个值)。
	 *
	 * @param parameterName 参数名称
	 * @return 参数值
	 */
	String getParameter( String parameterName );

	/**
	 * 设置Request属性值。
	 *
	 * @param attrName 属性名称
	 * @param attrValue 属性值
	 */
	void setAttr( String attrName, Object attrValue );

	/**
	 * 获取请求属性中的值对象
	 *
	 * @param <T> 类型
	 * @param attrName 属性名称
	 * @return 属性值
	 */
	<T> T getAttr( String attrName );

	/**
	 * 获取请求属性中的值对象
	 *
	 * @param <T> 类型
	 * @param attrName 属性名
	 * @param defValue 默认值
	 * @return 属性值 / 默认值
	 */
	<T> T getAttr( String attrName, T defValue );

	/**
	 * @return 请求属性中的所有键值对象集合。
	 */
	Map<String, Object> getAttrMap();

	/**
	 * @return 请求属性中的所有键名称集合。
	 */
	Set<String> getAttrNames();

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

}
