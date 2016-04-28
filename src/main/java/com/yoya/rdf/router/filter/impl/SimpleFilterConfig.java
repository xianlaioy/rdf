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

package com.yoya.rdf.router.filter.impl;

import com.yoya.rdf.router.filter.IFilterConfig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by baihw on 16-4-15.
 *
 * 一个简单的过滤器配置对象默认实现。
 */
public class SimpleFilterConfig implements IFilterConfig{

	// 用户配置的过滤器名称。
	private final String				_filterName;

	// 初始化参数集合。
	private final Map<String, String>	_initParams;

	public SimpleFilterConfig( String name, Map<String, String> initParams ){
		_initParams = null == initParams ? new HashMap<String, String>( 10, 1.0f ) : initParams;
		_filterName = ( null == name || "".equals( name = name.trim() ) ) ? "" : name;
	}

	/**
	 * @return 用户配置的过滤器名称。
	 */
	public String getFilterName(){
		return _filterName;
	}

	/**
	 * @return 获取初始化参数名称集合。
	 */
	public Iterator<String> getInitParameterNames(){
		return _initParams.keySet().iterator();
	}

	/**
	 * 获取指定名称的初始化参数。
	 *
	 * @param name 参数名称
	 * @return 参数值
	 */
	public String getInitParameter( String name ){
		return _initParams.get( name );
	}

	/**
	 * 获取指定名称的初始化参数。
	 *
	 * @param name 参数名称
	 * @param defValue 默认值
	 * @return 参数值 / 默认值
	 */
	public String getInitParameter( String name, String defValue ){
		String result = _initParams.get( name );
		return null == result ? defValue : _initParams.get( name );
	}

}
