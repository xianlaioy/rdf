package com.yoya.rdf.router.application.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.yoya.rdf.router.application.IApplication;
import com.yoya.sql.SqlRunner;
/**
 * 
 * @author yongda
 * 基于关系型数据库的application实现。
 */
public class RdbApplication implements IApplication{

	/**
	 * 使用的表名称。
	 */
	public static final String	TABLE_NAME		= "sys_application";

	public static final String	TMP_SQL_INSERT	= "insert into `%s` ( `id`, `data` ) values( ?, ? ) ON DUPLICATE KEY UPDATE `data` = values( `data` );";
	public static final String	TMP_SQL_DELETE	= "delete from `%s` where `id`=?;";
	public static final String	TMP_SQL_SELECT	= "select `data` from `%s` where `id`=?;";

	private final String		_SQL_INSERT;
	private final String		_SQL_DELETE;
	private final String		_SQL_SELECT;
	private final String		_SQL_SELECT_NAMES;

	public RdbApplication(){
		_SQL_INSERT = String.format( TMP_SQL_INSERT, TABLE_NAME );
		_SQL_DELETE = String.format( TMP_SQL_DELETE, TABLE_NAME );
		_SQL_SELECT = String.format( TMP_SQL_SELECT, TABLE_NAME );
		_SQL_SELECT_NAMES = String.format( "select `id` from `%s`", TABLE_NAME );
	}

	@Override
	public IApplication setAttribute( String name, Object value ){
		String valueString = JSON.toJSONString( value );
		SqlRunner.impl().update( _SQL_INSERT, name, valueString );
		return this;
	}

	@Override
	public <T> T getAttribute( String name ){
		return getAttribute( name, null );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getAttribute( String name, T defValue ){
		Object value = SqlRunner.impl().queryScalar( _SQL_SELECT, name );
		if( null == value )
			return defValue;
		Object result = JSON.parse( String.valueOf( value ) );
		return null == result ? defValue : ( T )result;
	}

	@Override
	public Set<String> getAttributeNames(){
		List<Map<String, Object>> datas = SqlRunner.impl().queryMapList( _SQL_SELECT_NAMES );
		if( null == datas || datas.isEmpty() )
			return null;
		Set<String> result = new HashSet<>();
		datas.forEach( ( rowData ) -> {
			Object name = rowData.get( "id" );
			if( null != name ){
				result.add( String.valueOf( name ) );
			}
		} );
		return null;
	}

	@Override
	public IApplication removeAttribute( String name ){
		SqlRunner.impl().update( _SQL_DELETE, name );
		return this;
	}
	
}
