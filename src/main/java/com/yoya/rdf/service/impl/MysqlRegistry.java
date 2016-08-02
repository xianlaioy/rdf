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
package com.yoya.rdf.service.impl;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.service.AbstractRegistry;
import com.yoya.rdf.service.IRegistry;

/**
 * Created by baihw on 16-6-6.
 *
 * 基于mysql数据库实现的服务注册中心。
 */
final class MysqlRegistry extends AbstractRegistry implements IRegistry{

	// 日志对象
	private static final ILog	_LOG				= LogManager.getLog( MysqlRegistry.class );

	/**
	 * 服务注册表名称
	 */
	public static final String	TABLE_NAME			= "sys_registry";

	public static final String	TMP_SQL_INSERT		= "insert into `%s` ( `server_name`, `server_address` ) values( ?, ? );";
	public static final String	TMP_SQL_DELETE		= "delete from `%s` where `server_name`=? and `server_address`=?;";
	public static final String	TMP_SQL_SELECT		= "select `server_address` from `%s` where `server_name`=? limit 1;";
	public static final String	TMP_SQL_SELECT_SK	= "select `server_secretkey` from `%s` where `server_name`=? limit 1;";

	private final String		_JDBC_URL;
	private final String		_JDBC_USER;
	private final String		_JDBC_PASSWORD;

	private final String		_SQL_INSERT;
	private final String		_SQL_DELETE;
	private final String		_SQL_SELECT;
	private final String		_SQL_SELECT_SK;

	/**
	 * 检查数据库驱动支持。
	 */
	static{
		try{
			Class.forName( "com.mysql.jdbc.Driver" );
		}catch( ClassNotFoundException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * @author cd0281
	 * 20160727
	 * 特定的构造函数
	 * @param paramMap
	 */
	public MysqlRegistry(Map<String,String> paramMap){
		this( paramMap.get( "registry.url" ), paramMap.get( "registry.user" ), paramMap.get( "registry.password" ));
	}
	
	/**
	 * 构造函数
	 * 
	 * @param jdbcUrl 数据库连接地址
	 * @param jdbcUser 数据库用户名
	 * @param jdbcPassword 数据库密码
	 */
	public MysqlRegistry( String jdbcUrl, String jdbcUser, String jdbcPassword ){
		Objects.requireNonNull( jdbcUrl );
		Objects.requireNonNull( jdbcUser );
		Objects.requireNonNull( jdbcPassword );

		this._JDBC_URL = jdbcUrl;
		this._JDBC_USER = jdbcUser;
		this._JDBC_PASSWORD = jdbcPassword;

		try{
			getConn().close();
		}catch( Exception e ){
			throw new RuntimeException( e );
		}

		this._SQL_INSERT = String.format( TMP_SQL_INSERT, TABLE_NAME );
		this._SQL_DELETE = String.format( TMP_SQL_DELETE, TABLE_NAME );
		this._SQL_SELECT = String.format( TMP_SQL_SELECT, TABLE_NAME );
		this._SQL_SELECT_SK = String.format( TMP_SQL_SELECT_SK, TABLE_NAME );

		// 检查环境初始化情况并进行数据加载。
		checkInitAndLoadData();
	}

	/**
	 * @return 获取数据库连接
	 */
	protected Connection getConn(){
		try{
			return DriverManager.getConnection( this._JDBC_URL, this._JDBC_USER, this._JDBC_PASSWORD );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * @return 数据库中是否已经存在配置表
	 */
	protected boolean hasTable(){
		String querySql = "SHOW TABLES LIKE '".concat( TABLE_NAME ).concat( "'; " );
		try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( querySql ); ResultSet rs = pstmt.executeQuery(); ){
			if( rs.next() ){ return null != rs.getString( 1 ); }
			return false;
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 基于mysql数据库的表初始化方法。
	 */
	protected void initTables(){
		StringBuilder sb = new StringBuilder();
		sb.append( "CREATE TABLE `" ).append( TABLE_NAME ).append( "` (" );
		sb.append( "`id` int NOT NULL AUTO_INCREMENT," );
		sb.append( "`server_name` varchar(64) NOT NULL," );
		sb.append( "`server_address` varchar(32) NOT NULL," );
		sb.append( "`server_secretkey` varchar(32) NULL," );
		sb.append( "PRIMARY KEY (`id`)," );
		sb.append( "UNIQUE KEY `name_address_key` (`server_name`,`server_address`) USING BTREE " );
		sb.append( ") ENGINE=InnoDB DEFAULT CHARSET=utf8;" );
		String sql = sb.toString();

		try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( sql ); ){
			pstmt.executeUpdate();
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 检查配置表是否存在，如果不存在则先进行初始化。然后加载配置数据。
	 */
	protected void checkInitAndLoadData(){
		if( !hasTable() ){
			initTables();
		}
	}

	/**
	 * 转换通信地址为字符串形式
	 * 
	 * @param address 通信地址
	 * @return 地址字符串
	 */
	protected String addressToString( InetSocketAddress address ){
		return String.format( "%s:%d", address.getHostString(), address.getPort() );
	}

	/**
	 * 解析字符串形式地址
	 * 
	 * @param address 通信地址字符串
	 * @return 通信地址
	 */
	protected InetSocketAddress stringToAddress( String address ){
		if( null == address )
			return null;
		int ndx = address.indexOf( ":" );
		if( -1 == ndx )
			return null;
		String host = address.substring( 0, ndx ).trim();
		String port = address.substring( ndx + 1 ).trim();
		return new InetSocketAddress( host, Integer.parseInt( port ) );
	}

	@Override
	public void register( String id, InetSocketAddress address ){
		Objects.requireNonNull( id );
		Objects.requireNonNull( address );

		String server_address = addressToString( address );
		try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( _SQL_INSERT ); ){
			pstmt.setString( 1, id );
			pstmt.setString( 2, server_address );
			int rowCount = pstmt.executeUpdate();
			_LOG.debug( String.format( "address: %s, sqlResult: %d.", server_address, rowCount ) );
		}catch( SQLException e ){
			_LOG.warn( e.getMessage() );
		}
	}

	@Override
	public void unRegister( String id, InetSocketAddress address ){
		Objects.requireNonNull( id );
		Objects.requireNonNull( address );

		String server_address = addressToString( address );
		try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( _SQL_DELETE ); ){
			pstmt.setString( 1, id );
			pstmt.setString( 2, server_address );
			int rowCount = pstmt.executeUpdate();
			_LOG.debug( String.format( "address: %s, sqlResult: %d.", server_address, rowCount ) );
		}catch( SQLException e ){
			_LOG.error( e.getMessage() );
		}
	}

	@Override
	public InetSocketAddress getAdress( String id ){
		Objects.requireNonNull( id );

		String server_address = queryScalar( _SQL_SELECT, id );
		return stringToAddress( server_address );
	}

	@Override
	public boolean checkSign( String id, String data, String sign ){
		Objects.requireNonNull( id );
		Objects.requireNonNull( data );
		Objects.requireNonNull( sign );

		String sk = queryScalar( _SQL_SELECT_SK, id );
		if( null == sk ){
			// 如果没有设置密钥，则始终放行。
			return true;
		}

		// 使用密钥加密数据，判断结果是否一致。
		String dataSign = sign( sk, data );
		return dataSign.equals( sign );
	}

	// 查询1行1列数据
	private String queryScalar( String sql, String parameter ){
		ResultSet rs = null;
		try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( sql ); ){
			pstmt.setString( 1, parameter );
			rs = pstmt.executeQuery();
			if( rs.next() ){ return rs.getString( 1 ); }
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
		return null;
	}

} // end class
