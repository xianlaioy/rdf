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

import java.util.*;

import com.yoya.sql.IRecord;

/**
 * Created by baihw on 16-4-20.
 *
 * 一个简单的数据记录条目实现
 */
public class SimpleRecord implements IRecord{

	// 数据存放容器。
	private final Map<String, Object> _fieldValues;

	/**
	 * 构造函数
	 */
	public SimpleRecord(){
		this._fieldValues = new HashMap<>( 10, 1.0f );
	}

	/**
	 * 设置字段数据
	 * 
	 * @param key 键名
	 * @param value 数据
	 * @return 当前对象s
	 */
	public IRecord put( String key, Object value ){
		this._fieldValues.put( key, value );
		return this;
	}

	/**
	 * 删除指定字段数据
	 * 
	 * @param key 键名
	 * @return 当前对象
	 */
	public IRecord remove( String key ){
		this._fieldValues.remove( key );
		return this;
	}

	/**
	 * 删除多个字段数据
	 * 
	 * @param keys 键名列表
	 * @return 当前对象
	 */
	public IRecord remove( String... keys ){
		if( null != keys && 0 != keys.length ){
			for( String key : keys )
				this._fieldValues.remove( key );
		}
		return this;
	}

	/**
	 * 删除数据数据
	 * 
	 * @return 当前对象
	 */
	public IRecord removeAll(){
		this._fieldValues.clear();
		return this;
	}

	/**
	 * 删除所有null数据。
	 * 
	 * @return 当前对象
	 */
	public IRecord removeNull(){
		for( Iterator<Map.Entry<String, Object>> it = _fieldValues.entrySet().iterator(); it.hasNext(); ){
			Map.Entry<String, Object> e = it.next();
			if( null == e.getKey() || null == e.getValue() )
				it.remove();
		}
		return this;
	}

	/**
	 * 保留指定字段数据，其它数据全部清除。
	 * 
	 * @param keys 键名列表
	 * @return 当前对象
	 */
	public IRecord keep( String... keys ){
		if( null != keys && 0 != keys.length ){
			Map<String, Object> newData = new HashMap<String, Object>( keys.length );
			for( String key : keys ){
				newData.put( key, _fieldValues.get( key ) );
			}
			this._fieldValues.clear();
			this._fieldValues.putAll( newData );
		}
		return this;
	}

	/**
	 * @return 所有鍵名集合。
	 */
	public Set<String> keys(){
		return Collections.unmodifiableSet( _fieldValues.keySet() );
	}

	/**
	 * 获取指定键名对应的字符串数据
	 * 
	 * @param key 键名
	 * @param defValue 值为null时返回的默认数据
	 * @return 数据
	 */
	public String getString( String key, String defValue ){
		Object value = this._fieldValues.get( key );
		return null == value ? defValue : String.valueOf( value );
	}

	/**
	 * 获取指定键名对应的字符串数据
	 * 
	 * @param key 键名
	 * @return 数据
	 */
	public String getString( String key ){
		return getString( key, null );
	}

	/**
	 * 获取指定键名对应的整形数据
	 * 
	 * @param key 键名
	 * @param defValue 值为null时返回的默认数据
	 * @return 数据
	 */
	public int getInt( String key, int defValue ){
		String value = getString( key, null );
		return null == value ? defValue : Integer.parseInt( value );
	}

	/**
	 * 获取指定键名对应的指定类型数据
	 * 
	 * @param key 键名
	 * @param defValue 值为null时返回的默认数据
	 * @param <T> 数据类型
	 * @return 数据
	 */
	@SuppressWarnings( "unchecked" )
	public <T> T get( String key, T defValue ){
		Object result = _fieldValues.get( key );
		return ( T )( null == result ? defValue : result );
	}

	/**
	 * 获取指定键名对应的指定类型数据
	 *
	 * @param key 键名
	 * @param <T> 数据类型
	 * @return 数据
	 */
	public <T> T get( String key ){
		return get( key, null );
	}

	/**
	 * 获取指定键名对应的整形数据
	 *
	 * @param key 键名
	 * @return 数据
	 */
	public int getInt( String key ){
		return getInt( key, -1 );
	}

	/**
	 * @return 转换为标准map结构数据
	 */
	public Map<String, Object> toMap(){
		return Collections.unmodifiableMap( this._fieldValues );
	}

	@Override
	public String toString(){
		return _fieldValues.toString();
	}

	public boolean equals( Object obj ){
		if( !( obj instanceof SimpleRecord ) )
			return false;
		if( obj == this )
			return true;
		return this._fieldValues.equals( ( ( SimpleRecord )obj )._fieldValues );
	}

	public int hashCode(){
		return null == _fieldValues ? 0 : _fieldValues.hashCode();
	}

}
