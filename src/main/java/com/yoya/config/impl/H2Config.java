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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by baihw on 16-8-7.
 * <p>
 * 基于H2数据库的配置对象实现。
 */
public class H2Config extends RdbConfig{

	// 数据库连接信息
	private static final String _DRIVER_CLASS_NAME = "org.h2.Driver";

	
	/**
	 * @author cd0281
	 * 20160727
	 * 特定的构造函数
	 * @param paramMap
	 */
	public H2Config(Map<String,String> paramMap){
		this( paramMap.get( "url" ), paramMap.get( "user" ), paramMap.get( "password" ), 
			(paramMap.get( "profileName" )==null || 0 == ( paramMap.get( "profileName" ).trim() ).length())?null:paramMap.get( "profileName" ), 
			(paramMap.get( "tablePrefix" )==null || 0 == ( paramMap.get( "tablePrefix" ).trim() ).length())?null:paramMap.get( "tablePrefix" ));
	}
	
	
	/**
	 * 构造函数
	 * 
	 * @param jdbcUrl 数据库连接地址
	 * @param jdbcUser 数据库用户名
	 * @param jdbcPassword 数据库密码
	 * @param profileName 当前配置对象对应的环境名称
	 * @param tablePrefix 配置表使用的表前缀字符串
	 */
	public H2Config( String jdbcUrl, String jdbcUser, String jdbcPassword, String profileName, String tablePrefix ){
		super( _DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPassword, profileName, tablePrefix );
	}

	/**
	 * 构造函数
	 *
	 * @param jdbcUrl 数据库连接地址
	 * @param jdbcUser 数据库用户名
	 * @param jdbcPassword 数据库密码
	 * @param profileName 当前配置对象对应的环境名称
	 */
	public H2Config( String jdbcUrl, String jdbcUser, String jdbcPassword, String profileName ){
		super( _DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPassword, profileName );
	}

	/**
	 * 构造函数
	 *
	 * @param jdbcUrl 数据库连接地址
	 * @param jdbcUser 数据库用户名
	 * @param jdbcPassword 数据库密码
	 */
	public H2Config( String jdbcUrl, String jdbcUser, String jdbcPassword ){
		super( _DRIVER_CLASS_NAME, jdbcUrl, jdbcUser, jdbcPassword );
	}

	/**
	 * @return 数据库中是否已经存在配置表
	 */
	protected boolean hasTable(){
		String querySql = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where table_name = '".concat( _TABLEFULLNAME ).concat( "'; " );
		try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( querySql ); ResultSet rs = pstmt.executeQuery(); ){
			if( rs.next() ){ return null != rs.getString( 1 ); }
			return false;
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}
	}

	@Override
	protected String getCreateTableSQL(){
		StringBuilder sb = new StringBuilder();
		sb.append( "CREATE TABLE `" ).append( _TABLEFULLNAME ).append( "` (" );
		sb.append( "`id` int NOT NULL AUTO_INCREMENT," );
		sb.append( "`profile` varchar(16) NOT NULL DEFAULT '" ).append( DEF_PROFILE_NAME ).append( "'," );
		sb.append( "`group` varchar(64) NOT NULL DEFAULT '" ).append( DEF_GROUP_NAME ).append( "'," );
		sb.append( "`key` varchar(192) NOT NULL," );
		sb.append( "`value` text NOT NULL," );
		sb.append( "`lastValue` text DEFAULT NULL," );
		sb.append( "`description` varchar(255) DEFAULT NULL," );
		sb.append( "`createTime` char(14) DEFAULT NULL," );
		sb.append( "`updateTime` char(14) DEFAULT NULL," );
		sb.append( "`createUser` varchar(16) DEFAULT NULL," );
		sb.append( "`updateUser` varchar(16) DEFAULT NULL," );
		sb.append( "PRIMARY KEY (`id`)," );
		sb.append( "UNIQUE KEY `profile_group_key` (`profile`,`group`,`key`) USING BTREE " );
		sb.append( ");" );
		String sql = sb.toString();
		return sql;
	}

} // end class
