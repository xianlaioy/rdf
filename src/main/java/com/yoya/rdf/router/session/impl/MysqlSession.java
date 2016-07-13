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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoya.ds.DSManager;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.router.session.ISession;
import com.yoya.sql.IRecord;
import com.yoya.sql.ISqlRunner;
import com.yoya.sql.SqlRunner;
import com.yoya.sql.impl.SimpleSqlRunner;

/**
 * Created by baihw on 16-5-13.
 *
 * 基于数据库的用户会话实现。
 */
public class MysqlSession implements ISession{

	/**
	 * 使用的表名称。
	 */
	public static final String	TABLE_NAME		= "sys_session";

	// 日志处理对象。
	private static final ILog	_LOG			= LogManager.getLog( MysqlSession.class );

	private static final String	TMP_SQL_INSERT	= "insert into `%s` ( `id`, `data`, `createTime`, `updateTime` ) values( ?, ?, ?, ? );";
	private static final String	TMP_SQL_UPDATE	= "update `%s` set `data`=?, `updateTime`=? where `id`=?;";
	private static final String	TMP_SQL_UPDATE1	= "update `%s` set `updateTime`=? where `id`=?;";
	private static final String	TMP_SQL_DELETE	= "delete from `%s` where `id`=?;";
	private static final String	TMP_SQL_SELECT	= "select `data`, `createTime`, `updateTime` from `%s` where `id`=?;";

	// 唯一标识
	private final String		_ID;
	// 超时时间,单位：秒。
	private final long			_TIMEOUT;
	// 当前会话创建时间
	private final long			_CREATE_TIME;
	// 当前会话最后修改时间
	private final long			_UPDATE_TIME;
	// 会话数据存放容器
	private final JSONObject	_DATA;
	// 是否为新会话
	private boolean				_isNew;

	// 数据是否发生变化。
	private boolean				_dataHasChange	= false;

	public MysqlSession( String sessionId, int timeout ){

		if( 1 > timeout )
			throw new IllegalArgumentException( "timeout can not be less than 1." );

		// 将框架中约定的超时时间单位分钟转换为秒值。
		this._TIMEOUT = timeout * 60;
		this._UPDATE_TIME = Instant.now().getLong( ChronoField.INSTANT_SECONDS ); 

		if( null != sessionId && 0 != ( sessionId = sessionId.trim() ).length() ){

			ISqlRunner sqlRunner = new SimpleSqlRunner( DSManager.impl().getDS() );
			IRecord record = sqlRunner.queryRecord( String.format( TMP_SQL_SELECT, TABLE_NAME ), sessionId );
			if( null != record ){
				String updateTime = record.getString( "updateTime" );
				long idleTime = this._UPDATE_TIME - Integer.parseInt( updateTime );
				if( idleTime < this._TIMEOUT ){
					this._ID = sessionId;
					this._isNew = false;
					this._CREATE_TIME = record.getInt( "createTime" ); // Instant.ofEpochSecond( this._CREATE_TIME ) ;
					String dataValue = record.getString( "data" );
					this._DATA = JSON.parseObject( dataValue );
					return;
				}else{
					_LOG.debug( String.format( "session timeout! %d > %d ", idleTime, _TIMEOUT ) );
					sqlRunner.update( String.format( TMP_SQL_DELETE, TABLE_NAME ), sessionId );
				}
			}
		}

		this._ID = UUID.randomUUID().toString().replace( "-", "" );
		this._isNew = true;
		this._CREATE_TIME = this._UPDATE_TIME;
		this._DATA = new JSONObject();

	}

	@Override
	public boolean isNew(){
		return this._isNew;
	}

	@Override
	public String getId(){
		return this._ID;
	}

	@Override
	public long getCreationTime(){
		return this._CREATE_TIME;
	}

	@Override
	public long getLastAccessedTime(){
		return this._UPDATE_TIME;
	}

	@Override
	public ISession setAttribute( String name, Object value ){
		this._dataHasChange = true;
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
		this._dataHasChange = true;
		this._DATA.remove( name );
		return this;
	}

	@Override
	public ISession setMaxInactiveInterval( int interval ){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	public int getMaxInactiveInterval(){
		return ( int )( this._TIMEOUT / 60 );
	}

	@Override
	public void invalidate(){
		this._dataHasChange = true;
		this._DATA.clear();
	}

	/**
	 * 同步变化到数据库存储中。
	 */
	public void sync(){
		if( this._isNew ){
			SqlRunner.impl().update( String.format( TMP_SQL_INSERT, TABLE_NAME ), this._ID, this._DATA.toJSONString(), this._CREATE_TIME, this._UPDATE_TIME );
		}else if( this._dataHasChange ){
			SqlRunner.impl().update( String.format( TMP_SQL_UPDATE, TABLE_NAME ), this._DATA.toJSONString(), this._UPDATE_TIME, this._ID );
		}else{
			SqlRunner.impl().update( String.format( TMP_SQL_UPDATE1, TABLE_NAME ), this._UPDATE_TIME, this._ID );
		}
	}

	/**
	 * 检查初始化情况，如果是首次使用，则初始化需要的表结构。
	 */
	public static void checkInit(){
		String sql = "SHOW TABLES LIKE '".concat( TABLE_NAME ).concat( "'; " );
		Object tableName = SqlRunner.impl().queryScalar( sql );
		if( null == tableName ){
			StringBuilder sb = new StringBuilder();
			sb.append( "CREATE TABLE `" ).append( TABLE_NAME ).append( "` (" );
			sb.append( "`id` char(32) NOT NULL," );
			sb.append( "`data` MEDIUMTEXT," );
			sb.append( "`createTime` int NOT NULL," );
			sb.append( "`updateTime` int NOT NULL," );
			sb.append( "PRIMARY KEY (`id`)" );
			sb.append( ") ENGINE=InnoDB DEFAULT CHARSET=utf8;" );
			sql = sb.toString();

			SqlRunner.impl().update( sql );
		}
	}

	/**
	 * 14位长度的日期时间表示字符串。
	 */
	static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern( "yyyyMMddHHmmss" );

	/**
	 * @return 当前时间的14位长度表示字符串。
	 */
	static String getChar14DateTime(){
		return DTF.format( LocalDateTime.now() );
	}

} // end class
