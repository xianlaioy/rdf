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

package com.yoya.rdf.router.session;

import java.util.Set;

/**
 * Created by baihw on 16-5-13.
 *
 * 用户会话对象操作规范接口
 */
public interface ISession{

	/**
	 * sessionid标识关键字,如果通过url重写保持session需要使用此参数名提供,cookie中也一样.
	 */
	String KEY_SESSIONID = "RDF_SID";

	/**
	 * 是否为新创建的会话对象,通常第一次请求或者用户禁用了Cookie时会导致会话每次都是新建状态.
	 *
	 * @return true / false
	 */
	boolean isNew();

	/**
	 * 获取会话唯一标识
	 *
	 * @return 会话唯一标识
	 */
	String getId();

	/**
	 * 获取会话创建时间
	 *
	 * @return 会话创建时间
	 */
	long getCreationTime();

	/**
	 * 获取会话最后访问时间
	 *
	 * @return 会话最后访问时间
	 */
	long getLastAccessedTime();

	/**
	 * 设置会话属性值
	 *
	 * @param name 名称
	 * @param value 值
	 * @return 当前对象
	 */
	ISession setAttribute( String name, Object value );

	/**
	 * 获取会话属性对象
	 *
	 * @param <T> 类型
	 * @param name 名称
	 * @return 值
	 */
	<T> T getAttribute( String name );

	/**
	 * 获取会话属性对象
	 *
	 * @param <T> 类型
	 * @param name 名称
	 * @param defValue 值为null时的默认值
	 * @return 值
	 */
	<T> T getAttribute( String name, T defValue );

	/**
	 * 获取会话属性名称集合
	 *
	 * @return 属性名称集合
	 */
	Set<String> getAttributeNames();

	/**
	 * 移除指定名称的会话属性
	 *
	 * @param name 名称
	 * @return 当前实例
	 */
	ISession removeAttribute( String name );

	/**
	 * 设置会话最大存活时间,单位:秒
	 *
	 * @param interval 最大存活时间
	 * @return 当前实例
	 */
	ISession setMaxInactiveInterval( int interval );

	/**
	 * 获取会话最大存活时间,单位：分钟
	 * 
	 * @return 会话最大存活时间,单位：分钟
	 */
	int getMaxInactiveInterval();

	/**
	 * 使会话状态失效,所有会话数据将被清除.
	 */
	void invalidate();

}
