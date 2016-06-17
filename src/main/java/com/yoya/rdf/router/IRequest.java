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
import java.util.Set;

import com.yoya.rdf.router.session.ISession;

/**
 * Created by baihw on 16-4-14.
 *
 * 统一的请求对象规范接口
 */
public interface IRequest{

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
	 * 获取所有请求头信息
	 * 
	 * @return 头信息集合
	 */
	Map<String, String> getHeaders();

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
	 * @return 当前请求会话对象。
	 */
	ISession getSession();

	/**
	 * @return 当前请求会话对象中是否有使用过session。
	 */
	boolean hasSession();

}
