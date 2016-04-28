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

package com.yoya.rdf.log.impl;

import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.ILogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by baihw on 16-4-14.
 *
 * 简单日志对象生产工厂类。
 */
public final class SimpleLogFactory implements ILogFactory{

	/**
	 * 日志对象缓存容器。
	 */
	private static final Map<String, ILog> _LOGS = new ConcurrentHashMap<>();

	/**
	 * 获取日志对象实例
	 *
	 * @param category 日志类别名称
	 * @return 日志对象
	 */
	@Override
	public ILog getLog( String category ){
		ILog result = _LOGS.get( category );
		if( null == result ){
			result = new SimpleLog( category );
			ILog putLog = _LOGS.putIfAbsent( category, result );
			if( null != putLog )
				result = putLog;
		}
		return result;
	}

}
