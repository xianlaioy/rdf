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

import java.sql.*;
import java.util.*;

import com.yoya.config.AbstractConfig;
import com.yoya.net.ssh.SshException;
import com.yoya.rdf.Rdf;
import com.yoya.sql.IRecord;
import com.yoya.sql.IRecordList;

/**
 * Created by baihw on 16-4-28.
 * <p>
 * 基于关系型数据库的配置对象实现。
 */
public class RdbConfig extends AbstractConfig{

	/**
	 * 配置数据表名称。
	 */
	public static final String	TABLE_NAME				= "config";

	/**
	 * Mysql数据库驱动类名称。
	 */
	public static final String	MYSQL_DRIVER_CLASS_NAME	= "com.mysql.jdbc.Driver";

	/**
	 * 默认的数据库表前缀字符串。
	 */
	public static final String	DEF_TABLE_PREFIX		= "rc_";

	/**
	 * 默认的环境名称字符串。
	 */
	public static final String	DEF_PROFILE_NAME		= "local";

//	// 数据库操作对象
//	private final ISqlRunner	_SQLRUNNER;

	private final String		_DRIVER_CLASS_NAME;
	private final String		_JDBC_URL;
	private final String		_JDBC_USER;
	private final String		_JDBC_PASSWORD;

	// 数据库最终完整表名称。
	private final String		_TABLEFULLNAME;

	// 当前配置对象对应的环境名称。
	private final String		_PROFILENAME;

	// 查询配置数据的sql语句。
	private final String		_QUERYPROFILEDATASQL;

	/**
	 * 构造函数
	 * 
	 * @param driverClassName 数据库驱动类名称
	 * @param jdbcUrl 数据库连接地址
	 * @param jdbcUser 数据库用户名
	 * @param jdbcPassword 数据库密码
	 * @param tablePrefix 配置表使用的表前缀字符串
	 * @param profileName 当前配置对象对应的环境名称
	 */
	public RdbConfig( String driverClassName, String jdbcUrl, String jdbcUser, String jdbcPassword, String tablePrefix, String profileName ){

		Objects.requireNonNull( jdbcUrl );
		Objects.requireNonNull( jdbcUser );
		Objects.requireNonNull( jdbcPassword );

		if( null == driverClassName )
			driverClassName = MYSQL_DRIVER_CLASS_NAME;
		if( !MYSQL_DRIVER_CLASS_NAME.equals( driverClassName ) ){ throw new RuntimeException( "暂不支持mysql以外的数据库!" ); }

		this._DRIVER_CLASS_NAME = driverClassName;
		this._JDBC_URL = jdbcUrl;
		this._JDBC_USER = jdbcUser;
		this._JDBC_PASSWORD = jdbcPassword;

		// 检查数据库连接。
		try{
			Class.forName( this._DRIVER_CLASS_NAME );
			getConn().close();
		}catch( Exception e ){
			throw new RuntimeException( e );
		}

		this._TABLEFULLNAME = null == tablePrefix ? DEF_TABLE_PREFIX.concat( TABLE_NAME ) : tablePrefix.concat( TABLE_NAME );
		this._QUERYPROFILEDATASQL = "select `group`, `key`, `value` from ".concat( _TABLEFULLNAME ).concat( " where profile=?" );
		this._PROFILENAME = null == profileName ? DEF_PROFILE_NAME : profileName;

		// 检查环境初始化情况并进行数据加载。
		checkInitAndLoadData();
	}

	/**
	 * 构造函数
	 *
	 * @param driverClassName 数据库驱动类名称
	 * @param jdbcUrl 数据库连接地址
	 * @param jdbcUser 数据库用户名
	 * @param jdbcPassword 数据库密码
	 */
	public RdbConfig( String driverClassName, String jdbcUrl, String jdbcUser, String jdbcPassword ){
		this( driverClassName, jdbcUrl, jdbcUser, jdbcPassword, null, null );
	}

//	public RdbConfig( String driverClassName, String url, String userName, String password ){
//
//	}

	protected Connection getConn(){
		try{
			return DriverManager.getConnection( this._JDBC_URL, this._JDBC_USER, this._JDBC_PASSWORD );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 初始化数据库表结构。
	 */
	protected void initTables(){
		if( MYSQL_DRIVER_CLASS_NAME.equals( this._DRIVER_CLASS_NAME ) ){
			initTablesByMysql();
		}else{
			throw new RuntimeException( "暂不支持Mysql以外的其他类型数据库!" );
		}
	}

	/**
	 * 基于mysql数据库的表初始化方法。
	 */
	protected void initTablesByMysql(){
		StringBuilder sb = new StringBuilder();
//        sb.append("DROP TABLE IF EXISTS `").append(_TABLEFULLNAME).append("`;");
//        _SQLRUNNER.update(sb.toString());
//        sb.delete(0, sb.length());

		sb.append( "CREATE TABLE `" ).append( _TABLEFULLNAME ).append( "` (" );
		sb.append( "`id` int NOT NULL AUTO_INCREMENT," );
		sb.append( "`profile` varchar(16) NOT NULL DEFAULT '" ).append( DEF_PROFILE_NAME ).append( "'," );
		sb.append( "`group` varchar(64) NOT NULL DEFAULT '" ).append( DEF_GROUP ).append( "'," );
		sb.append( "`key` varchar(256) NOT NULL," );
		sb.append( "`value` text NOT NULL," );
		sb.append( "`lastValue` text DEFAULT NULL," );
		sb.append( "`description` varchar(255) DEFAULT NULL," );
		sb.append( "`createTime` char(14) DEFAULT NULL," );
		sb.append( "`updateTime` char(14) DEFAULT NULL," );
		sb.append( "`createUser` varchar(16) DEFAULT NULL," );
		sb.append( "`updateUser` varchar(16) DEFAULT NULL," );
		sb.append( "PRIMARY KEY (`id`)," );
		sb.append( "UNIQUE KEY `profile_group_key` (`profile`,`group`,`key`) USING BTREE " );
		sb.append( ") ENGINE=InnoDB DEFAULT CHARSET=utf8;" );
		String sql = sb.toString();

		try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( sql ); ){
			pstmt.executeUpdate();
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}

	}

	/**
	 * 初始化默认配置数据。
	 */
	protected void initDefaultData(){
		String sql = "insert into `".concat( _TABLEFULLNAME ).concat( "` ( `group`, `key`, `value`, `description` ) values( ?, ?, ?, ? )" );
		List<String[]> params = new ArrayList<>();

		params.add( new String[]{ "global", "AK", "", "应用唯一标识" } );
		params.add( new String[]{ "global", "SK", "", "应用超级权限访问密钥" } );
		params.add( new String[]{ "global", Rdf.KEY_ENCODING, "UTF-8", "应用使用的编码" } );

		params.add( new String[]{ "router", "impl", "simple", "路由管理器使用的实现名称。默认为系统提供的simple实现。" } );
		params.add( new String[]{ "router", "workBase", "rdf.me.handler", "路由管理器进行请求处理方法扫描的工作路径，通常为业务处理逻辑文件所在根路径。" } );
		params.add( new String[]{ "router", "ignoreUrl", ".+(?i)\\.(html|css|js|json|ico|png|gif|woff|map)$", "路由管理器忽略不处理的请求路径正则表达式。" } );

		try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( sql ); ){
			for( String[] rowParams : params ){
				for( int i = 1; i < 5; i++ ){
					pstmt.setString( i, rowParams[i - 1] );
				}
				pstmt.addBatch();
			}
			pstmt.executeBatch();
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}

	}

	/**
	 * 从数据库中加载数据。
	 */
	protected void loadData(){
		ResultSet rs = null;
		try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( _QUERYPROFILEDATASQL ); ){
			pstmt.setString( 1, _PROFILENAME );
			rs = pstmt.executeQuery();

			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();
			String[] colNames = new String[colCount];
			for( int i = 1; i <= colCount; i++ ){
				String colName = meta.getColumnLabel( i );
				if( null == colName || 0 == colName.length() )
					colName = meta.getColumnName( i );
				colNames[i - 1] = colName;
			}

			List<Map<String, String>> datas = new ArrayList<>();
			while( rs.next() ){
				Map<String, String> rowData = new HashMap<>( colCount );
				for( int i = 0; i < colCount; i++ ){
					rowData.put( colNames[i], rs.getString( i + 1 ) );
				}
				datas.add( rowData );
			}

			// 锁定配置数据，进行数据更新。
			synchronized( _data ){
				this._data.clear();

				datas.forEach( ( rowMap ) -> {
					String group = rowMap.get( "group" );
					String key = rowMap.get( "key" );
					String value = rowMap.get( "value" );
					putValue( group, key, value );
				} );

			}
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			if( null != rs ){
				try{
					rs.close();
				}catch( SQLException e ){
					throw new RuntimeException( e );
				}
			}
		}

	}

	/**
	 * @return 数据库中是否已经存在配置表
	 */
	protected boolean hasTable(){
		if( MYSQL_DRIVER_CLASS_NAME.equals( this._DRIVER_CLASS_NAME ) ){
			String querySql = "SHOW TABLES LIKE '".concat( _TABLEFULLNAME ).concat( "'; " );
			try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( querySql ); ResultSet rs = pstmt.executeQuery(); ){
				if( rs.next() ){ return null != rs.getString( 1 ); }
				return false;
			}catch( SQLException e ){
				throw new RuntimeException( e );
			}
		}else{
			throw new RuntimeException( "暂不支持Mysql以外的其他类型数据库!" );
		}
	}

	/**
	 * 检查配置表是否存在，如果不存在则先进行初始化。然后加载配置数据。
	 */
	protected void checkInitAndLoadData(){
		if( !hasTable() ){
			// 初始化默认表结构。
			initTables();
			// 初始化默认表数据。
			initDefaultData();
		}
		// 加载配置数据。
		loadData();
	}

}
