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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.yoya.config.AbstractConfig;

/**
 * Created by baihw on 16-4-28.
 * <p>
 * 基于Mysql数据库的配置对象实现。
 */
public class MysqlConfig extends AbstractConfig{

	/**
	 * 配置数据表名称。
	 */
	public static final String	TABLE_NAME			= "config";

	/**
	 * 默认的数据库表前缀字符串。
	 */
	public static final String	DEF_TABLE_PREFIX	= "sys_";

	// 数据库驱动类名称
	private static final String	_DRIVER_CLASS_NAME	= "com.mysql.jdbc.Driver";
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
	 * @author cd0281
	 * 20160727
	 * 特定的构造函数
	 * @param paramMap
	 */
	public MysqlConfig(Map<String,String> paramMap){
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
	public MysqlConfig( String jdbcUrl, String jdbcUser, String jdbcPassword, String profileName, String tablePrefix ){

		Objects.requireNonNull( jdbcUrl );
		Objects.requireNonNull( jdbcUser );
		Objects.requireNonNull( jdbcPassword );

		this._JDBC_URL = jdbcUrl;
		this._JDBC_USER = jdbcUser;
		this._JDBC_PASSWORD = jdbcPassword;

		// 检查数据库连接。
		try{
			Class.forName( _DRIVER_CLASS_NAME );
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
	 * @param jdbcUrl 数据库连接地址
	 * @param jdbcUser 数据库用户名
	 * @param jdbcPassword 数据库密码
	 * @param profileName 当前配置对象对应的环境名称
	 */
	public MysqlConfig( String jdbcUrl, String jdbcUser, String jdbcPassword, String profileName ){
		this( jdbcUrl, jdbcUser, jdbcPassword, profileName, null );
	}

	/**
	 * 构造函数
	 *
	 * @param jdbcUrl 数据库连接地址
	 * @param jdbcUser 数据库用户名
	 * @param jdbcPassword 数据库密码
	 */
	public MysqlConfig( String jdbcUrl, String jdbcUser, String jdbcPassword ){
		this( jdbcUrl, jdbcUser, jdbcPassword, null, null );
	}

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
		StringBuilder sb = new StringBuilder();
//      sb.append("DROP TABLE IF EXISTS `").append(_TABLEFULLNAME).append("`;");
//      _SQLRUNNER.update(sb.toString());
//      sb.delete(0, sb.length());

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

		// 全局配置信息
		params.add( new String[]{ "global", "AK", "rdf-registry", "应用唯一标识" } );
		params.add( new String[]{ "global", "SK", "123456", "应用超级权限访问密钥" } );
		params.add( new String[]{ "global", "encoding", "UTF-8", "应用编码" } );
		params.add( new String[]{ "global", "homeDir", "", "应用工作主目录" } );

		// web服务配置信息
		params.add( new String[]{ "web", "workBase", "rdf.me.handler", "web路由管理器进行请求处理方法扫描的工作路径，通常为业务处理逻辑文件所在根路径。" } );
		params.add( new String[]{ "web", "ignoreUrl", ".+(?i)\\.(jsp|html|css|js|json|ico|png|gif|woff|map)$", "web路由管理器忽略不处理的请求路径正则表达式。" } );
		params.add( new String[]{ "web", "filterNames", "loginFilter", "过滤器名称列表，名称唯一，不可重复，对相同url进行拦截的过滤器执行顺序以列表中的排列顺序为准。" } );
		params.add( new String[]{ "web", "loginFilter.class", "com.yoya.rdf.router.filter.impl.LoginFilter", "指定过滤器的实现类完全限定类名，需要实现IRequestFilter接口。" } );
		params.add( new String[]{ "web", "loginFilter.url", "/*", "指定过滤器拦截的url，如果没有指定拦截的url，则拦截器会被初始化但是无法处理请求。" } );
		params.add( new String[]{ "web", "loginFilter.ignore", "/login", "指定过滤器拦截的自定义参数：忽略登陆检查的路径。" } );

		// session配置信息
		params.add( new String[]{ "session", "impl", "MysqlSession", "会话管理器使用的实现名称。" } );
		params.add( new String[]{ "session", "timeout", "45", "会话最大不活动时间，超过此时间会话将失效。（单位：分钟）" } );
		params.add( new String[]{ "session", "domain", "", "会话域，需要支持多个应用共享登陆状态时将此值设为主域。（如：www.xxx.com）" } );

		// application配置信息
		params.add( new String[]{ "application", "impl", "", "应用全局共享数据管理器使用的实现名称。" } );

		// 服务调用相关配置信息。
		params.add( new String[]{ "service", "impl", "simple", "服务调用管理器使用的实现名称。默认为系统提供的simple实现。" } );
		params.add( new String[]{ "service", "registry", "", "服务注册中心使用的实现名称。默认为系统提供的nothing实现" } );
//		params.add( new String[]{ "service", "registry", "serviceRegistry", "服务注册中心使用的实现名称。基于service服务实现。适用于大中型网络环境。此实现需要预先部署身份认证服务" } );
//		params.add( new String[]{ "service", "registry.url", "127.0.0.1:9998", "服务网络通信地址。默认为:127.0.0.1:9998。" } );
//		params.add( new String[]{ "service", "registry", "mysqlRegistry", "服务注册中心使用的实现名称。基于关系型数据库Mysql的实现。适用于小型网络环境。" } );
//		params.add( new String[]{ "service", "registry.url", "jdbc:mysql://127.0.0.1:3386/rdf_test_db?useUnicode=true&characterEncoding=utf8&useOldAliasMetadataBehavior=true&useSSL=false", "jdbc连接地址。" } );
//		params.add( new String[]{ "service", "registry.user", "rdf_test_user", "jdbc连接帐号。" } );
//		params.add( new String[]{ "service", "registry.password", "rdf_test_password", "jdbc连接密码。" } );

		params.add( new String[]{ "service", "useSign", "true", "是否在通信过程中使用签名机制。默认为true，如果项目处于足够安全的可信任环境，可以设置为false。" } );
		params.add( new String[]{ "service", "waitTimeout", "60000", "服务调用客户端等待超时时间，单位：毫秒。默认为1分钟(60000)。" } );

		params.add( new String[]{ "service", "enable", "true", "是否启动网络通信服务。默认为true，如果项目只是调用其它服务而自身不提供服务，可以设置为false。" } );
		params.add( new String[]{ "service", "bindAddress", "0.0.0.0:9999", "服务绑定的主机地址及端口。默认为0.0.0.0:9999。" } );
		params.add( new String[]{ "service", "exportAddress", "", "服务导出地址。默认为检测到绑定成功的主机地址及端口。当使用外部的负载均衡器时，应该配置为负载器地址。" } );
		params.add( new String[]{ "service", "workBase", "rdf.me.service", "服务路由管理器进行请求处理方法扫描的工作路径，通常为服务处理逻辑文件所在根路径。" } );

		// 数据源管理器
		params.add( new String[]{ "plugin", "loader", "", "插件加载器使用的实现名称，默认为框架提供的simple实现。" } );
		params.add( new String[]{ "plugin", "loader.workbase", "", "插件加载器工作目录，通常不指定则使用应用主目录下的plugins目录，最终默认值参考具体的加载器实现文档。" } );
		params.add( new String[]{ "plugin", "loader.url", "", "插件加载器请求的网络地址，默认值参考具体的加载器实现文档。" } );
		params.add( new String[]{ "plugin", "loader.user", "", "插件加载器发请求时使用的的账号。" } );
		params.add( new String[]{ "plugin", "loader.password", "", "插件加载器发请求时使用的的密码。" } );
		params.add( new String[]{ "plugin", "autoUpdate", "true", "插件自动更新功能是否开启，默认开启。" } );
		params.add( new String[]{ "plugin", "names", "p1,p2", "使用到的插件名称列表，多个以逗号隔开。" } );
		params.add( new String[]{ "plugin", "p1.interface", "", "指定名称为p1的插件门面接口名称。" } );
		params.add( new String[]{ "plugin", "p1.impls", "", "插件实现者列表，键值对形式对应实现者名称及实现类，多个以逗号隔开。如果有名称为default的实现视为默认实现，没有则以第1个出现的实现作为默认实现。" } );

		// 数据源管理器
		params.add( new String[]{ "dsManager", "impl", "druid", "数据源管理器使用的实现名称。默认为基于阿里开源的druid库的实现。" } );
		params.add( new String[]{ "dsManager", "dsNames", "ds1", "数据源名称列表,多个数据源名称请用逗号隔开。" } );
		params.add( new String[]{ "dsManager", "ds1.jdbcDriver", "com.mysql.jdbc.Driver", "jdbc驱动名称。" } );
		params.add( new String[]{ "dsManager", "ds1.jdbcUrl", "jdbc:mysql://127.0.0.1:3306/rdf_test_db?useUnicode=true&characterEncoding=utf8&useOldAliasMetadataBehavior=true&useSSL=false", "jdbc连接地址。" } );
		params.add( new String[]{ "dsManager", "ds1.jdbcUser", "rdf_test_user", "jdbc连接帐号。" } );
		params.add( new String[]{ "dsManager", "ds1.jdbcPassword", "rdf_test_password", "jdbc连接密码。" } );
		params.add( new String[]{ "dsManager", "ds1.initialPoolSize", "1", "连接池启动时初始化的连接数。默认:1。" } );
		params.add( new String[]{ "dsManager", "ds1.minPoolSize", "1", "连接池中的最小连接数。默认:1" } );
		params.add( new String[]{ "dsManager", "ds1.maxPoolSize", "50", "连接池中的最大连接数。默认:50。" } );
		params.add( new String[]{ "dsManager", "ds1.testOnBorrow", "false", "申请连接时是否执行validationQuery检测连接是否有效，做了这个配置会降低性能。默认:false。" } );
		params.add( new String[]{ "dsManager", "ds1.testOnReturn", "false", "归还连接时是否执行validationQuery检测连接是否有效，做了这个配置会降低性能。默认:false。" } );
		params.add( new String[]{ "dsManager", "ds1.testWhileIdle", "true", "申请连接的时候检测，如果空闲时间大于maxIdleTime，执行validationQuery检测连接是否有效。建议配置为true，不影响性能，并且保证安全性。默认:true。" } );
		params.add( new String[]{ "dsManager", "ds1.maxIdleTime", "300000", "连接的最大空闲时间(单位：毫秒)，超过最大空闲时间的连接将被关闭。单位：毫秒。默认:300000。" } );
		params.add( new String[]{ "dsManager", "ds1.minIdleTime", "60000", "连接的最小空闲时间(单位：毫秒)，最小空闲时间的连接不会销毁也不会进行检测。单位：毫秒。默认:60000。" } );
		params.add( new String[]{ "dsManager", "ds1.validationQuery", "select 1", "用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用。默认：select 1。" } );

		params.add( new String[]{ "sqlRunner", "impl", "simple", "sql操作执行器使用的实现名称。默认为系统提供的simple实现。" } );

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
					String value = rs.getString( i + 1 );
					if( null != value )
						value = value.trim();
					rowData.put( colNames[i], value );
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
		String querySql = "SHOW TABLES LIKE '".concat( _TABLEFULLNAME ).concat( "'; " );
		try( Connection conn = getConn(); PreparedStatement pstmt = conn.prepareStatement( querySql ); ResultSet rs = pstmt.executeQuery(); ){
			if( rs.next() ){ return null != rs.getString( 1 ); }
			return false;
		}catch( SQLException e ){
			throw new RuntimeException( e );
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
