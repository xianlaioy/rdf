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
package com.yoya.rdf.router.session.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yoya.rdf.router.session.ISession;

/**
 * Created by baihw on 16-6-5.
 * 
 * 基于本地存储的单机版用户会话实现。（适用于长连接环境）
 */
public final class LocalSession implements ISession{

	// 超时时间,单位：秒。
	private static final long								_TIMEOUT	= 45;
	// 所有用户会话数据存储容器。
	private static final Cache<String, Map<String, Object>>	_DATAS		= CacheBuilder.newBuilder().expireAfterAccess( _TIMEOUT, TimeUnit.SECONDS ).build();

	// 唯一标识
	private final String									_ID;
	// 会话数据存储容器
	private final Map<String, Object>						_DATA;

	public static LocalSession getSession( String sessionId ){
		return new LocalSession( sessionId );
	}

	/**
	 * 构造函数
	 * 
	 * @param sessionId 会话标识
	 */
	private LocalSession( String sessionId ){
		if( null != sessionId && 0 != ( sessionId = sessionId.trim() ).length() ){
			this._ID = sessionId;
			Map<String, Object> data = _DATAS.getIfPresent( this._ID );
			if( null == data ){
				_DATAS.put( this._ID, new HashMap<>() );
			}
			this._DATA = _DATAS.getIfPresent( this._ID );
		}else{
			this._ID = UUID.randomUUID().toString().replace( "-", "" );
			_DATAS.put( this._ID, new HashMap<>() );
			this._DATA = _DATAS.getIfPresent( this._ID );
		}
	}

	@Override
	public boolean isNew(){
		return false;
	}

	@Override
	public String getId(){
		return this._ID;
	}

	@Override
	public long getCreationTime(){
		return 0;
	}

	@Override
	public long getLastAccessedTime(){
		return 0;
	}

	@Override
	public ISession setAttribute( String name, Object value ){
		this._DATA.put( name, value );
		return this;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getAttribute( String name ){
		Object result = this._DATA.get( name );
		return null == result ? null : ( T )result;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getAttribute( String name, T defValue ){
		Object result = this._DATA.get( name );
		return null == result ? defValue : ( T )result;
	}

	@Override
	public Set<String> getAttributeNames(){
		return Collections.unmodifiableSet( this._DATA.keySet() );
	}

	@Override
	public ISession removeAttribute( String name ){
		this._DATA.remove( name );
		return this;
	}

	@Override
	public ISession setMaxInactiveInterval( int interval ){
		return this;
	}

	@Override
	public int getMaxInactiveInterval(){
		return 0;
	}

	@Override
	public void invalidate(){
		_DATAS.invalidate( this._ID );
	}

	/**
	 * @return 强制从缓存中加载当前会话全部数据。
	 */
	protected Map<String, Object> getData(){
		return _DATAS.getIfPresent( this._ID );
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append( "{'sessionId':" ).append( this._ID );
		sb.append( ", 'data':" ).append( this._DATA );
		sb.append( "}" );
		return sb.toString();
	}

} // end class
