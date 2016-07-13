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

package com.yoya.ds.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.yoya.ds.IDSManager;
import com.yoya.rdf.Rdf;

/**
 * Created by baihw on 16-5-13.
 *
 * 基于Druid库实现的数据源管理对象。
 */
public class DruidDSManager implements IDSManager{

	/**
	 * 配置项名称：数据源名称列表。
	 */
	public static final String					CNF_DSNAMES				= "dsNames";

	/**
	 * 配置项名称：jdbc驱动名称。
	 */
	public static final String					CNF_JDBC_DRIVER			= "jdbcDriver";

	/**
	 * 配置项名称：jdbc连接地址。
	 */
	public static final String					CNF_JDBC_URL			= "jdbcUrl";

	/**
	 * 配置项名称：jdbc连接帐号。
	 */
	public static final String					CNF_JDBC_USER			= "jdbcUser";

	/**
	 * 配置项名称：jdbc连接密码。
	 */
	public static final String					CNF_JDBC_PASSWORD		= "jdbcPassword";

	/**
	 * 配置项名称：连接池启动时初始化的连接数。默认:1。
	 */
	public static final String					CNF_INITIAL_POOL_SIZE	= "initialPoolSize";

	/**
	 * 配置项名称：连接池中的最小连接数。默认:1。
	 */
	public static final String					CNF_MIN_POOL_SIZE		= "minPoolSize";

	/**
	 * 配置项名称：连接池中的最大连接数。默认:50。
	 */
	public static final String					CNF_MAX_POOL_SIZE		= "maxPoolSize";

	/**
	 * 配置项名称：申请连接时是否执行validationQuery检测连接是否有效，做了这个配置会降低性能。默认:false。
	 */
	public static final String					CNF_TEST_ON_BORROW		= "testOnBorrow";

	/**
	 * 配置项名称：归还连接时是否执行validationQuery检测连接是否有效，做了这个配置会降低性能。默认:false。
	 */
	public static final String					CNF_TEST_ON_RETURN		= "testOnReturn";

	/**
	 * 配置项名称：申请连接的时候检测，如果空闲时间大于maxIdleTime，执行validationQuery检测连接是否有效。建议配置为true，不影响性能，并且保证安全性。默认:true。
	 */
	public static final String					CNF_TEST_WHILE_IDLE		= "testWhileIdle";

	/**
	 * 配置项名称：连接的最大空闲时间(单位：毫秒)，超过最大空闲时间的连接将被关闭。单位：毫秒。默认:300000。
	 */
	public static final String					CNF_MAX_IDLE_TIME		= "maxIdleTime";

	/**
	 * 配置项名称：连接的最小空闲时间(单位：毫秒)，最小空闲时间的连接不会销毁也不会进行检测。单位：毫秒。默认:60000。
	 */
	public static final String					CNF_MIN_IDLE_TIME		= "minIdleTime";

	/**
	 * 配置项名称：用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用。默认：select 1。
	 */
	public static final String					CNF_VALIDATION_QUERY	= "validationQuery";

	/**
	 * 配置项名称：超时时间,获取连接时最大等待时间(单位：毫秒)
	 */
	public static final String					CNF_TIMEOUT				= "60000";

	/*************************************************************************************
	 ****************** 配置项默认值常量
	 *************************************************************************************/

	/**
	 * 默认的驱动名称:com.mysql.jdbc.Driver。
	 */
	public static final String					DEF_DRIVERCLASSNAME		= "com.mysql.jdbc.Driver";

	/**
	 * 默认的最大连接数:50。
	 */
	public static final String					DEF_MAXPOOLSIZE			= "50";

	/**
	 * 默认的最小连接数:1,连接池中最小的空闲的连接数，低于这个数量会自动创建新的连接。。
	 */
	public static final String					DEF_MINPOOLSIZE			= "1";

	/**
	 * 默认的初始化连接数:1。
	 */
	public static final String					DEF_INITIALPOOLSIZE		= "1";

	/**
	 * 用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用。
	 */
	public static final String					DEF_VALIDATIONQUERY		= "select 1";

	/**
	 * 申请连接时是否执行validationQuery检测连接是否有效，做了这个配置会降低性能。
	 */
	public static final boolean					DEF_TESTONBORROW		= false;

	/**
	 * 归还连接时是否执行validationQuery检测连接是否有效，做了这个配置会降低性能。
	 */
	public static final boolean					DEF_TESTONRETURN		= false;

	/**
	 * 申请连接的时候检测，如果空闲时间大于maxIdleTime，执行validationQuery检测连接是否有效。建议配置为true，不影响性能，并且保证安全性。
	 */
	public static final boolean					DEF_TESTWHILEIDLE		= true;

	/**
	 * 连接的最大空闲时间(单位：毫秒)，超过最大空闲时间的连接将被关闭。
	 */
	public static final String					DEF_MAXIDLETIME			= "300000";

	/**
	 * 连接的最小空闲时间(单位：毫秒)，最小空闲时间的连接不会销毁也不会进行检测。
	 */
	public static final String					DEF_MINIDLETIME			= "60000";

	/**
	 * 超时时间,获取连接时最大等待时间(单位：毫秒)
	 */
	public static final String					DEF_TIMEOUT				= "60000";

	/**
	 * 数据源存放容器。
	 */
	private final Map<String, DruidDataSource>	_DSMAP					= new ConcurrentHashMap<>( 1, 1.0f );;

//	// 默认数据源实例名称
//	private String							_default_dsName			= null;
	// 默认数据源实例
	private DataSource							_default_ds				= null;

	public DruidDSManager(){
		Map<String, String> configMap = Rdf.me().getConfigGroup( CONFIG_GROUP );

		String dsNamesValue = configMap.get( CNF_DSNAMES );
		String[] dsNames = dsNamesValue.split( "," );
		for( String dsName : dsNames ){
			if( null == dsName || 0 == ( dsName = dsName.trim() ).length() )
				continue;

			DruidDataSource ds = new DruidDataSource();
			ds.setName( dsName );
			ds.setUrl( configMap.get( getConfigName( dsName, CNF_JDBC_URL ) ) );
			ds.setUsername( configMap.get( getConfigName( dsName, CNF_JDBC_USER ) ) );
			ds.setPassword( configMap.get( getConfigName( dsName, CNF_JDBC_PASSWORD ) ) );

			String driverClassName = configMap.get( getConfigName( dsName, CNF_JDBC_DRIVER ) );
			if( null == driverClassName || 0 == ( driverClassName = driverClassName.trim() ).length() )
				driverClassName = DEF_DRIVERCLASSNAME;
			ds.setDriverClassName( driverClassName );

			String maxActive = configMap.get( getConfigName( dsName, CNF_MAX_POOL_SIZE ) );
			if( null == maxActive || 0 == ( maxActive = maxActive.trim() ).length() )
				maxActive = DEF_MAXPOOLSIZE;
			ds.setMaxActive( Integer.parseInt( maxActive ) );

			String minIdle = configMap.get( getConfigName( dsName, CNF_MIN_POOL_SIZE ) );
			if( null == minIdle || 0 == ( minIdle = minIdle.trim() ).length() )
				minIdle = DEF_MINPOOLSIZE;
			ds.setMinIdle( Integer.parseInt( minIdle ) );

			String initialSize = configMap.get( getConfigName( dsName, CNF_INITIAL_POOL_SIZE ) );
			if( null == initialSize || 0 == ( initialSize = initialSize.trim() ).length() )
				initialSize = DEF_INITIALPOOLSIZE;
			ds.setInitialSize( Integer.parseInt( initialSize ) );

			String validationQuery = configMap.get( getConfigName( dsName, CNF_VALIDATION_QUERY ) );
			if( null == validationQuery || 0 == ( validationQuery = validationQuery.trim() ).length() )
				validationQuery = DEF_VALIDATIONQUERY;
			ds.setValidationQuery( validationQuery );

			boolean testOnBorrow = "true".equals( configMap.get( getConfigName( dsName, CNF_TEST_ON_BORROW ) ) ) ? true : DEF_TESTONBORROW;
			ds.setTestOnBorrow( testOnBorrow );

			boolean testOnReturn = "true".equals( configMap.get( getConfigName( dsName, CNF_TEST_ON_RETURN ) ) ) ? true : DEF_TESTONRETURN;
			ds.setTestOnReturn( testOnReturn );

			boolean testWhileIdle = "false".equals( configMap.get( getConfigName( dsName, CNF_TEST_WHILE_IDLE ) ) ) ? false : DEF_TESTWHILEIDLE;
			ds.setTestWhileIdle( testWhileIdle );

			String minEvictableIdleTimeMillis = configMap.get( getConfigName( dsName, CNF_MAX_IDLE_TIME ) );
			if( null == minEvictableIdleTimeMillis || "".equals( minEvictableIdleTimeMillis = minEvictableIdleTimeMillis.trim() ) )
				minEvictableIdleTimeMillis = DEF_MAXIDLETIME;
			ds.setMinEvictableIdleTimeMillis( Long.parseLong( minEvictableIdleTimeMillis ) );

			String timeBetweenEvictionRunsMillis = configMap.get( getConfigName( dsName, CNF_MAX_IDLE_TIME ) );
			if( null == timeBetweenEvictionRunsMillis || "".equals( timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis.trim() ) )
				timeBetweenEvictionRunsMillis = DEF_MINIDLETIME;
			ds.setTimeBetweenEvictionRunsMillis( Long.parseLong( timeBetweenEvictionRunsMillis ) );

			String maxWait = configMap.get( getConfigName( dsName, CNF_TIMEOUT ) );
			if( null == maxWait || "".equals( maxWait = maxWait.trim() ) )
				maxWait = DEF_TIMEOUT;
			ds.setMaxWait( Long.parseLong( maxWait ) );
			ds.setUseUnfairLock( true );
			ds.setFailFast( true );

			ds.setProxyFilters( Arrays.asList( new ConsoleSqlReport() ) );

			try{
				ds.init();
				if( null == _default_ds ){
					_default_ds = ds;
//					_default_dsName = dsName;
				}
				this._DSMAP.put( dsName, ds );
			}catch( SQLException e ){
				throw new RuntimeException( e );
			}
		}

	}

	@Override
	public Connection getConn( String dsName ){
		DataSource ds = _DSMAP.get( dsName );
		try{
			return ds.getConnection();
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}
	}

	@Override
	public Connection getConn(){
		try{
			return _default_ds.getConnection();
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}
	}

	@Override
	public DataSource getDS( String dsName ){
		return _DSMAP.get( dsName );
	}

	@Override
	public DataSource getDS(){
		return _default_ds;
	}

	/**
	 * 获取完整的配置项名称
	 * 
	 * @param dsName 数据源名称
	 * @param configKey 配置项名称
	 * @return 完整的配置项名称
	 */
	protected String getConfigName( String dsName, String configKey ){
		return dsName.concat( "." ).concat( configKey );
	}

	@Override
	public void init( Map<String, String> params ){
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy(){
		_DSMAP.values().forEach( ( ds ) -> {
			ds.close();
		} );
	}

}
