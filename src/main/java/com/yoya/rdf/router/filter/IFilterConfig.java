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

package com.yoya.rdf.router.filter;

import java.util.Iterator;

/**
 * Created by baihw on 16-4-14.
 *
 * 过滤器配置对象规范接口
 */
public interface IFilterConfig{

	/**
	 * 获取用户配置的过滤器名称。
	 *
	 * @return 过滤器名称
	 */
	public String getFilterName();

	/**
	 * 获取初始化参数名称集合。
	 *
	 * @return 参数名称集合
	 */
	public Iterator<String> getInitParameterNames();

	/**
	 * 获取指定名称的初始化参数。
	 *
	 * @param name 名称
	 * @return 值
	 */
	public String getInitParameter( String name );

	/**
	 * 获取指定名称的初始化参数。
	 *
	 * @param name 名称
	 * @param defValue 默认值
	 * @return 值
	 */
	public String getInitParameter( String name, String defValue );

}
