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

package com.yoya.sql;

import java.util.Map;
import java.util.Set;

/**
 * Created by baihw on 16-4-20.
 *
 * 数据记录条目规范接口
 */
public interface IRecord{

	/**
	 * 设置字段数据
	 *
	 * @param key 键名
	 * @param value 数据
	 * @return 当前对象s
	 */
	IRecord put( String key, Object value );

	/**
	 * 删除指定字段数据
	 *
	 * @param key 键名
	 * @return 当前对象
	 */
	IRecord remove( String key );

	/**
	 * 删除多个字段数据
	 *
	 * @param keys 键名列表
	 * @return 当前对象
	 */
	IRecord remove( String... keys );

	/**
	 * 删除数据数据
	 *
	 * @return 当前对象
	 */
	IRecord removeAll();

	/**
	 * 删除所有null数据。
	 *
	 * @return 当前对象
	 */
	IRecord removeNull();

	/**
	 * 保留指定字段数据，其它数据全部清除。
	 *
	 * @param keys 键名列表
	 * @return 当前对象
	 */
	IRecord keep( String... keys );

	/**
	 * @return 所有鍵名集合。
	 */
	Set<String> keys();

	/**
	 * 获取指定键名对应的字符串数据
	 *
	 * @param key 键名
	 * @param defValue 值为null时返回的默认数据
	 * @return 数据
	 */
	String getString( String key, String defValue );

	/**
	 * 获取指定键名对应的字符串数据
	 *
	 * @param key 键名
	 * @return 数据
	 */
	String getString( String key );

	/**
	 * 获取指定键名对应的整形数据
	 *
	 * @param key 键名
	 * @param defValue 值为null时返回的默认数据
	 * @return 数据
	 */
	int getInt( String key, int defValue );

	/**
	 * 获取指定键名对应的指定类型数据
	 *
	 * @param key 键名
	 * @param defValue 值为null时返回的默认数据
	 * @param <T> 数据类型
	 * @return 数据
	 */
	<T> T get( String key, T defValue );

	/**
	 * 获取指定键名对应的指定类型数据
	 *
	 * @param key 键名
	 * @param <T> 数据类型
	 * @return 数据
	 */
	<T> T get( String key );

	/**
	 * 获取指定键名对应的整形数据
	 *
	 * @param key 键名
	 * @return 数据
	 */
	int getInt( String key );

	/**
	 * @return 转换为标准map结构数据
	 */
	Map<String, Object> toMap();

}
