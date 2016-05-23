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
package com.yoya.rdf.router.application;

import java.util.Set;

/**
 * Created by baihw on 16-5-19.
 *
 * 应用全局共享对象操作规范接口
 */
public interface IApplication{

	/**
	 * 此组件使用的配置组名称。
	 */
	String	CONFIG_GROUP	= "sqlRunner";

	/**
	 * 次组件使用的实现者名称配置关键字。
	 */
	String	KEY_IMPL		= "impl";

	/**
	 * 设置会话属性值
	 * 
	 * @param name 名称
	 * @param value 值
	 * @return 当前对象
	 */
	void setAttribute( String name, Object value );

	/**
	 * 获取全局属性对象
	 * 
	 * @param <T> 类型
	 * @param name 名称
	 * @return 值
	 */
	<T> T getAttribute( String name );

	/**
	 * 获取全局属性对象
	 * 
	 * @param <T> 类型
	 * @param name 名称
	 * @param defValue 值为null时的默认值
	 * @return 值
	 */
	<T> T getAttribute( String name, T defValue );

	/**
	 * 获取全局属性名称集合
	 * 
	 * @return 属性名称集合
	 */
	Set<String> getAttributeNames();

	/**
	 * 移除指定名称的全局属性
	 * 
	 * @param name 名称
	 * @return 当前实例
	 */
	void removeAttribute( String name );

} // end class
