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

package com.yoya.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by baihw on 16-4-28.
 *
 * 配置对象抽象基类。
 */
public abstract class AbstractConfig implements IConfig{

	/**
	 * 配置数据存储容器。
	 */
	protected Map<String, Map<String, String>> _data = new HashMap<>();

	/**
	 * 增加配置项数据，非线程安全，如果需要应用在多线程环境下，请重写次方法。
	 * 
	 * @param group 配置组名称
	 * @param key 配置项名称
	 * @param value 配置值
	 */
	protected void putValue( String group, String key, String value ){
		Map<String, String> configMap = _data.get( group );
		if( null == configMap ){
			configMap = new HashMap<>();
			_data.put( group, configMap );
		}
		configMap.put( key, value );
	}

	@Override
	public String get( String key ){
		return get( DEF_GROUP, key );
	}

	@Override
	public String get( String group, String key ){
		Map<String, String> configMap = _data.get( group );
		if( null == configMap )
			return null;
		return configMap.get( key );
	}

	@Override
	public String toString(){
		return _data.toString();
	}

}
