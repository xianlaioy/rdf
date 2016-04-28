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

package com.yoya.config.impl;

import com.yoya.config.IConfig;
import com.yoya.rdf.Rdf;
import com.yoya.sql.ISqlRunner;
import com.yoya.sql.impl.SimpleSqlRunner;
import org.zbus.kit.JsonKit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by baihw on 16-4-15.
 *
 * 一个简单的配置对象实现
 */
public class SimpleConfig implements IConfig{

	// 点号
	static final String	DOT					= ".";

	// 默认组前缀名称
	static final String	DEF_GROUP_PREFIX	= "rdf.";

	@Override
	public String get( String key ){
		Objects.requireNonNull( key );
		return System.getProperty( DEF_GROUP_PREFIX.concat( key ) );
	}

	@Override
	public String get( String group, String key ){
		Objects.requireNonNull( group );
		Objects.requireNonNull( key );
		return System.getProperty( group.concat( DOT ).concat( key ) );
	}

}
