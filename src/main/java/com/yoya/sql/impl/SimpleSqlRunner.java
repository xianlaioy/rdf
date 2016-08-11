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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.yoya.sql.IPageInfo;
import com.yoya.sql.IRecord;
import com.yoya.sql.IRecordList;
import com.yoya.sql.ISqlRunner;

/**
 * Created by baihw on 16-4-19.
 *
 * 一个简单的sql执行器实现
 */
public class SimpleSqlRunner implements ISqlRunner{

	/**
	 * mysql数据库驱动类
	 */
	public static final String											MYSQL_DRIVER			= "com.mysql.jdbc.Driver";

	/**
	 * mysql数据库连接url模板字符串
	 */
	public static final String											TMP_MYSQL_URL			= "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useOldAliasMetadataBehavior=true&useSSL=false";

	/**
	 * 查询记录数sql语句模板字符串
	 */
	public static final String											TMP_ROWCOUNT_QUERY		= "select count(1) from ( %s ) temp";

	/**
	 * 连接验证查询语句
	 */
	public static final String											VALIDATION_QUERY		= "select 1";

	/**
	 * 默认的数据库事务级别
	 */
	public static final int												DEF_TRANSACTION_LEVEL	= Connection.TRANSACTION_REPEATABLE_READ;

	// 查询执行对象
	private final QueryRunner											_QUERY;
	// Record结构的结果集处理对象
	private static final ResultSetHandler<IRecordList>					_RECORDLIST_HANDLER		= new RecordListHandler();
	// RecordList结构的结果集处理对象
	private static final ResultSetHandler<IRecord>						_RECORD_HANDLER			= new RecordHandler();
	// MapList结构的结果集处理对象
	private static final ResultSetHandler<List<Map<String, Object>>>	_MAPLIST_HANDLER		= new MapListHandler();
	// 单行单列结果集数据处理对象
	private static final ResultSetHandler<Object>						_SCALAR_HANDLER			= new ScalarHandler<Object>();

	// 默认的数据源对象。
	private final DataSource											_DS;
	// 线程本地数据库连接对象，用于数据库事务使用。
	private final ThreadLocal<Connection>								_THREADLOCAL			= new ThreadLocal<Connection>();

//	/**
//	 * 构造函数
//	 *
//	 * @param host 数据库主机
//	 * @param port 数据库端口
//	 * @param dbName 数据库名称
//	 * @param dbUser 数据库账号
//	 * @param dbPwd 数据库密码
//	 */
//	public SimpleSqlRunner( String host, int port, String dbName, String dbUser, String dbPwd ){
//		DruidDataSource ds = new DruidDataSource();
//		ds.setDriverClassName( MYSQL_DRIVER );
//		ds.setUrl( String.format( TMP_MYSQL_URL, host, port, dbName ) );
//		ds.setUsername( dbUser );
//		ds.setPassword( dbPwd );
//		ds.setInitialSize( 10 );
//		ds.setMinIdle( 10 );
//		ds.setMaxActive( 100 );
//		ds.setValidationQuery( VALIDATION_QUERY );
//		ds.setTestOnReturn( true );
//		ds.setFailFast( true );
//		List<Filter> filters = new ArrayList<>( 1 );
//		filters.add( new ConsoleSqlReport() );
//		ds.setProxyFilters( filters );
//
//		_QUERY = new QueryRunner( ds );
//		try{
////            ds.validateConnection( ds.getConnection() );
//			Object result = _QUERY.query( "select 1", _SCALAR_HANDLER );
//		}catch( SQLException e ){
//			throw new RuntimeException( e );
//		}
//	}

	/**
	 * 构造函数
	 * 
	 * @param ds 数据源对象
	 */
	public SimpleSqlRunner( DataSource ds ){
		_DS = ds;
		_QUERY = new QueryRunner( ds );
	}

	@Override
	public DBTYPE getDbType(){
		return DBTYPE.Mysql;
	}

	/**
	 * 获取指定查询的首行首列值
	 *
	 * @param sql sql语句
	 * @param params sql语句参数列表
	 * @return 结果
	 */
	public Object queryScalar( String sql, Object... params ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.query( conn, sql, _SCALAR_HANDLER, params );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	/**
	 * 获取指定查询的首行首列值
	 *
	 * @param sql sql语句
	 * @return 结果
	 */
	public Object queryScalar( String sql ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.query( conn, sql, _SCALAR_HANDLER );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	@Override
	public IPageInfo queryPageInfo( int pageNO, int pageSize, String sql, Object... params ){
		Objects.requireNonNull( sql );
		if( 1 > pageSize )
			throw new IllegalArgumentException( "pageSize can not be less than 1." );

		Connection conn = null;
		try{
			conn = getConn();
			String countSql = String.format( TMP_ROWCOUNT_QUERY, sql );
			Object rcObj = null;
			if( null == params || 0 == params.length ){
				rcObj = _QUERY.query( conn, countSql, _SCALAR_HANDLER );
			}else{
				rcObj = _QUERY.query( conn, countSql, _SCALAR_HANDLER, params );
			}

			if( null == rcObj )
				return null;

			// 根据数据库类型生成对应的分页查询语句,获取当前页数据.
			String dbName = conn.getMetaData().getDatabaseProductName();
			String pageSql = buildPageSql( dbName, sql, pageNO, pageSize );
			IRecordList pageData = null;
			if( null == params || 0 == params.length ){
				pageData = _QUERY.query( conn, pageSql, _RECORDLIST_HANDLER );
			}else{
				pageData = _QUERY.query( conn, pageSql, _RECORDLIST_HANDLER, params );
			}

			// 包装分页数据返回
			SimplePageInfo result = new SimplePageInfo();
			int rowCount = Integer.parseInt( String.valueOf( rcObj ) );
			result.setRowCount( rowCount );
			int pageCount = rowCount / pageSize + ( ( rowCount % pageSize ) > 0 ? 1 : 0 );
			result.setPageCount( pageCount );
			result.setPageSize( pageSize );
			result.setPageData( pageData );
			return result;
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	@Override
	public IPageInfo queryPageInfo( int pageNO, int pageSize, String sql ){
		return queryPageInfo( pageNO, pageSize, sql, ( Object )null );
	}

	/**
	 * 根据不同的数据库类型构建正确的分页sql.
	 * 
	 * @param dbName 数据库名称
	 * @param sql 原始sql
	 * @param pageNO 分页页码
	 * @param pageSize 分页大小
	 * @return 分页sql
	 */
	protected String buildPageSql( String dbName, String sql, int pageNO, int pageSize ){
		String result = sql;
		switch( dbName ){
			// TODO: 这里增加对各种数据库类型的支持逻辑.
//			case "" :
//				break ;
			// 没有匹配到的数据库统一使用mysql分页方式的'limit ?, ?'sql语句.
			default:
				int offset = ( pageNO - 1 ) * pageSize;
				result = String.format( "%s limit %d, %d", sql, offset, pageSize );
				break;
		}

		return result;
	}

	@Override
	public IRecord queryRecord( String sql, Object... params ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.query( conn, sql, _RECORD_HANDLER, params );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	@Override
	public IRecord queryRecord( String sql ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.query( conn, sql, _RECORD_HANDLER );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	@Override
	public IRecordList queryRecordList( String sql, Object... params ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.query( conn, sql, _RECORDLIST_HANDLER, params );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	@Override
	public IRecordList queryRecordList( String sql ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.query( conn, sql, _RECORDLIST_HANDLER );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	/**
	 * 执行sql查询获取mapList结构的结果集
	 *
	 * @param sql sql语句
	 * @param params sql语句参数列表
	 * @return 结果集
	 */
	public List<Map<String, Object>> queryMapList( String sql, Object... params ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.query( conn, sql, _MAPLIST_HANDLER, params );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	/**
	 * 执行sql查询获取mapList结构的结果集
	 *
	 * @param sql sql语句
	 * @return 结果集
	 */
	public List<Map<String, Object>> queryMapList( String sql ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.query( conn, sql, _MAPLIST_HANDLER );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	/**
	 * 执行sql更新语句获取受影响的行数。
	 *
	 * @param sql sql语句
	 * @param params sql语句参数列表
	 * @return 受影响的行数。
	 */
	public int update( String sql, Object... params ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.update( conn, sql, params );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	/**
	 * 执行sql更新语句获取受影响的行数。
	 *
	 * @param sql sql语句
	 * @return 受影响的行数。
	 */
	public int update( String sql ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.update( conn, sql );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	/**
	 * 批量执行sql获取返回结果。
	 * 
	 * @param sql sql语句
	 * @param params 每行sql对应的参数列表。
	 * @return 每条sql执行后受影响的行数。
	 */
	public int[] batch( String sql, Object[][] params ){
		Objects.requireNonNull( sql );
		Connection conn = null;
		try{
			conn = getConn();
			return _QUERY.batch( conn, sql, params );
		}catch( SQLException e ){
			throw new RuntimeException( e );
		}finally{
			closeConn( conn );
		}
	}

	@Override
	public boolean tx( int transactionLevel, TxMethod method ){
		Connection conn = _THREADLOCAL.get();
		if( null != conn ){ throw new RuntimeException( "当前版本暂不支持嵌套事务的执行!" ); }

		Boolean autoCommit = null;
		try{
			conn = _DS.getConnection();
			boolean isSupport = conn.getMetaData().supportsTransactions();
			if( !isSupport ){ throw new RuntimeException( "当前连接的数据库不支持事务!" ); }
//			boolean isSupportLevel = conn.getMetaData().supportsTransactionIsolationLevel( transactionLevel ) ;
			_THREADLOCAL.set( conn );
			autoCommit = conn.getAutoCommit();
			conn.setTransactionIsolation( transactionLevel );
			conn.setAutoCommit( false );
			boolean result = method.run();
			if( result )
				conn.commit();
			else
				conn.rollback();

			return result;
		}catch( SQLException e ){
			if( null != conn ){
				try{
					conn.rollback();
				}catch( SQLException e1 ){
					System.err.println( e1 );
				}
			}
		}finally{
			try{
				if( null != conn ){
					if( null != autoCommit ){
						conn.setAutoCommit( autoCommit );
					}
					conn.close();
				}
			}catch( SQLException se ){
				System.err.println( se );
			}finally{
				_THREADLOCAL.remove();
			}
		}

		return false;
	}

	@Override
	public boolean tx( TxMethod method ){
		return tx( DEF_TRANSACTION_LEVEL, method );
	}

	/**
	 * 根据map集合中的字段生成指定表的更新语句执行数据更新动作。
	 *
	 * @param table 表名
	 * @param fields 字段数据集合
	 * @param idName 主键字段名称
	 * @return 受影响的记录数。
	 */
	public int update( String table, Map<String, Object> fields, String idName ){
		Objects.requireNonNull( table, "table can not be null!" );
		Objects.requireNonNull( fields, "fields can not be null!" );

		final int fieldCount = fields.size();
		if( 1 > fieldCount ){ throw new RuntimeException( "fields can not be empty!" ); }

		Object[] params = new Object[fieldCount];
		int param_curr_index = 0;

		// 规范中要求每个表都有一个主键标识，名称为idName。
		params[fieldCount - 1] = fields.remove( idName );

		StringBuilder sb = new StringBuilder();
		sb.append( "update " ).append( table ).append( " set " );
		Iterator<String> fieldNames = fields.keySet().iterator();
		while( fieldNames.hasNext() ){
			String fieldName = fieldNames.next();
			sb.append( fieldName ).append( "=?, " );
			params[param_curr_index++] = fields.get( fieldName );
		}
		sb.deleteCharAt( sb.length() - 2 );
		sb.append( "where " ).append( idName ).append( "=? ;" );
		String sql = sb.toString();

		return update( sql, params );
	}

	/**
	 * 根据map集合中的字段生成指定表的更新语句执行数据更新动作。
	 *
	 * @param table 表名
	 * @param fields 字段数据集合
	 * @return 受影响的记录数。
	 */
	public int update( String table, Map<String, Object> fields ){
		return update( table, fields, "id" );
	}

	/**
	 * 根据map集合中的字段生成指定表的插入语句执行数据插入动作。
	 *
	 * @param table 表名
	 * @param fields 字段数据集合
	 * @return 受影响的记录数。
	 */
	public int insert( String table, Map<String, Object> fields ){

		Objects.requireNonNull( table, "table can not be null!" );
		Objects.requireNonNull( fields, "fields can not be null!" );

		final int fieldCount = fields.size();
		if( 1 > fieldCount ){ throw new RuntimeException( "fields can not be empty!" ); }

		Object[] params = new Object[fieldCount];
		int param_curr_index = 0;

		StringBuilder sb = new StringBuilder();
		sb.append( "insert into " ).append( table ).append( " ( " );
		Iterator<String> fieldNames = fields.keySet().iterator();
		while( fieldNames.hasNext() ){
			String fieldName = fieldNames.next();
			sb.append( fieldName ).append( ", " );
			params[param_curr_index++] = fields.get( fieldName );
		}
		sb.deleteCharAt( sb.length() - 2 );
		sb.append( ") values ( ?" );
		for( int i = 1; i < fieldCount; i++ )
			sb.append( ", ?" );
		sb.append( " ) ;" );
		String sql = sb.toString();

		return update( sql, params );
	}

	/**
	 * 根据map集合中的字段生成指定表的删除语句执行数据删除动作。
	 *
	 * @param table 表名
	 * @param fields 字段数据集合
	 * @return 受影响的记录数。
	 */
	public int delete( String table, Map<String, Object> fields ){
		Objects.requireNonNull( table, "table can not be null!" );
		Objects.requireNonNull( fields, "fields can not be null!" );

		final int fieldCount = fields.size();
		if( 1 > fieldCount ){ throw new RuntimeException( "fields can not be empty!" ); }

		Object[] params = new Object[fieldCount];
		int param_curr_index = 0;

		StringBuilder sb = new StringBuilder();
		sb.append( "delete from " ).append( table ).append( " where 1=1 " );
		Iterator<String> fieldNames = fields.keySet().iterator();
		while( fieldNames.hasNext() ){
			String fieldName = fieldNames.next();
			params[param_curr_index] = fields.get( fieldName );

			if( fieldName.startsWith( "or_" ) ){
				sb.append( "or " );
				fieldName = fieldName.substring( 3 );
			}else{
				sb.append( "and " );
			}
			if( fieldName.endsWith( "_like" ) ){
				fieldName = fieldName.substring( 0, fieldName.length() - 5 );
				sb.append( fieldName ).append( " like ? " );
				Object likeValObj = params[param_curr_index];
				if( null == likeValObj ){
					params[param_curr_index] = "%%";
				}else{
					params[param_curr_index] = "%".concat( String.valueOf( likeValObj ) ).concat( "%" );
				}
			}else{
				sb.append( fieldName ).append( "=? " );
			}

			param_curr_index++;
		}
		sb.append( ";" );
		String sql = sb.toString();

		if( null != sql )
			return 0;
		return update( sql, params );
	}

	/**
	 * 获取数据库连接对象
	 * 
	 * @return 数据库连接对象
	 * @throws SQLException 数据库连接对象获取异常
	 */
	private Connection getConn() throws SQLException{
		Connection conn = _THREADLOCAL.get();
		if( null == conn ){
			conn = _DS.getConnection();
		}
		return conn;
	}

	/**
	 * 关闭数据库连接对象
	 * 
	 * @param conn 数据库连接对象
	 */
	private void closeConn( Connection conn ){
		if( null == _THREADLOCAL.get() ){
			if( null != conn ){
				try{
					conn.close();
				}catch( SQLException e ){
					throw new RuntimeException( e );
				}
			}
		}
	}

	/**
	 * 在控制台输出执行的sql语句。
	 * 
	 * @param sql sql语句
	 * @param params sql参数
	 */
	protected void printSql( String sql, Object[] params ){
		if( null == sql )
			return;
		if( null == params || 0 == params.length ){
			System.out.println( sql );
			return;
		}

//        for( Object param : params )
//            sql = sql.replaceFirst( "\\?", null == param ? "null" : String.valueOf( param ) );
//        System.out.println( "printSql:".concat( sql ) );

		int par_index = 0;
		Matcher matcher = Pattern.compile( "\\?" ).matcher( sql );
		boolean isMatch = matcher.find();
		if( !isMatch ){
			System.out.println( sql );
			return;
		}
		StringBuffer sb = new StringBuffer();
		while( isMatch ){
			Object parValue = params[par_index++];
			String parValueStr = null == parValue ? null : String.valueOf( parValue );
			if( parValue instanceof String )
				parValueStr = "'".concat( parValueStr ).concat( "'" );
			// 修正jdk中Matcher的替换实现中遇到"$"符号报错的问题.
			if( -1 != parValueStr.indexOf( "$".intern() ) )
				parValueStr = parValueStr.replaceAll( "\\$".intern(), "\\\\\\$".intern() );
			matcher.appendReplacement( sb, parValueStr );
			isMatch = matcher.find();
		}

		System.out.println( "printSql:".concat( sb.toString() ) );
	}

}
