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

package com.yoya.sql.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.yoya.sql.IRecord;
import com.yoya.sql.IRecordList;

/**
 * Created by baihw on 16-4-20.
 *
 * 简单的数据记录集合实现。
 */
public class SimpleRecordList implements IRecordList{

	// 记录存储容器。
	private List<IRecord> _records;

	/**
	 * 构造函数
	 */
	public SimpleRecordList(){
		this._records = new ArrayList<>();
	}

	/**
	 * 增加一条记录数据
	 * 
	 * @param record 记录数据
	 * @return 当前对象
	 */
	public IRecordList add( IRecord record ){
		Objects.requireNonNull( record );
		this._records.add( record );
		return this;
	}

	/**
	 * 移除指定记录数据
	 * 
	 * @param record 记录数据
	 * @return 当前对象
	 */
	public IRecordList remove( IRecord record ){
		Objects.requireNonNull( record );
		this._records.remove( record );
		return this;
	}

	/**
	 * 移除指定索引位置的记录数据
	 * 
	 * @param index 索引位置
	 * @return 当前对象
	 */
	public IRecordList remove( int index ){
		this._records.remove( index );
		return this;
	}

	/**
	 * 删除所有null数据
	 * 
	 * @return 当前对象
	 */
	public IRecordList removeNull(){
		for( int i = 0, iLen = _records.size(); i < iLen; i++ ){
			if( null == _records.get( i ) ){
				_records.remove( i );
				i--;
				iLen--;
			}
		}
		return this;
	}

	/**
	 * @return 记录数量
	 */
	public int size(){
		return this._records.size();
	}

	/**
	 * 获取指定索引位置的数据记录。
	 * 
	 * @param index 索引位置
	 * @return 数据记录
	 */
	public IRecord get( int index ){
		return this._records.get( index );
	}

	/**
	 * @return 转换为标准list结构数据
	 */
	public List<Map<String, Object>> toListMap(){
		List<Map<String, Object>> result = new ArrayList<>( this._records.size() );
		this._records.forEach( ( record ) -> {
			if( null != record )
				result.add( record.toMap() );
		} );
		return result;
	}

	@Override
	public String toString(){
		return toListMap().toString();
	}

}
