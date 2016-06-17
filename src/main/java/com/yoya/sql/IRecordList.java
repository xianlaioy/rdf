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

import java.util.List;
import java.util.Map;

/**
 * Created by baihw on 16-4-20.
 *
 * 数据记录集合规范接口
 */
public interface IRecordList{
	/**
	 * 增加一条记录数据
	 *
	 * @param record 记录数据
	 * @return 当前对象
	 */
	IRecordList add( IRecord record );

	/**
	 * 移除指定记录数据
	 *
	 * @param record 记录数据
	 * @return 当前对象
	 */
	IRecordList remove( IRecord record );

	/**
	 * 移除指定索引位置的记录数据
	 *
	 * @param index 索引位置
	 * @return 当前对象
	 */
	IRecordList remove( int index );

	/**
	 * 删除所有null数据
	 *
	 * @return 当前对象
	 */
	IRecordList removeNull();

	/**
	 * @return 记录数量
	 */
	int size();

	/**
	 * 获取指定索引位置的数据记录。
	 *
	 * @param index 索引位置
	 * @return 数据记录
	 */
	IRecord get( int index );

	/**
	 * @return 转换为标准list结构数据
	 */
	List<Map<String, Object>> toListMap();
}
