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

package com.yoya.sql;

import java.util.List;
import java.util.Map;

/**
 * Created by baihw on 16-4-19.
 *
 * sql操作执行器规范接口
 */
public interface ISqlRunner{

	/**
	 * 默认的表主键字段名称，用于生成Sql的相关方法中没有指定主键字段名时使用的默认主键字段名称。
	 */
	String DEF_ID_NAME = "id";

    /**
     * @return 当前操作的数据库类型。
     */
    DBTYPE getDbType() ;

	/**
	 * 获取指定查询的首行首列值
	 *
	 * @param sql sql语句
	 * @param params sql语句参数列表
	 * @return 首行首列结果
	 */
	Object queryScalar( String sql, Object... params );

	/**
	 * 获取指定查询的首行首列值
	 *
	 * @param sql sql语句
	 * @return 首行首列结果
	 */
	Object queryScalar( String sql );

	/**
	 * 获取指定查询的分页信息
	 *
	 * @param pageSize 每页数据量
	 * @param sql sql语句
	 * @param params sql语句参数列表
	 * @return 分页信息
	 */
	IPageInfo queryPageInfo( int pageSize, String sql, Object... params );

	/**
	 * 获取指定查询的分页信息数据
	 * 
	 * @param pageSize 每页数据量
	 * @param sql sql语句
	 * @return 分页信息
	 */
	IPageInfo queryPageInfo( int pageSize, String sql );

	/**
	 * 执行执行sql查询获取IRecord结构的数据记录条目
	 * 
	 * @param sql sql语句
	 * @param params sql语句参数列表
	 * @return 数据记录条目
	 */
	IRecord queryRecord( String sql, Object... params );

	/**
	 * 执行执行sql查询获取IRecord结构的数据记录条目
	 * 
	 * @param sql sql语句
	 * @return 数据记录条目
	 */
	IRecord queryRecord( String sql );

	/**
	 * 执行执行sql查询获取IRecord结构的数据记录集合
	 * 
	 * @param sql sql语句
	 * @param params sql语句参数列表
	 * @return 数据记录集合
	 */
	IRecordList queryRecordList( String sql, Object... params );

	/**
	 * 执行执行sql查询获取IRecord结构的数据记录集合
	 * 
	 * @param sql sql语句
	 * @return 数据记录集合
	 */
	IRecordList queryRecordList( String sql );

	/**
	 * 执行sql查询获取mapList结构的结果集
	 *
	 * @param sql sql语句
	 * @param params sql语句参数列表
	 * @return 结果集
	 */
	List<Map<String, Object>> queryMapList( String sql, Object... params );

	/**
	 * 执行sql查询获取mapList结构的结果集
	 *
	 * @param sql sql语句
	 * @return 结果集
	 */
	List<Map<String, Object>> queryMapList( String sql );

	/**
	 * 执行sql更新语句获取受影响的行数。
	 *
	 * @param sql sql语句
	 * @param params sql语句参数列表
	 * @return 受影响的行数。
	 */
	int update( String sql, Object... params );

	/**
	 * 执行sql更新语句获取受影响的行数。
	 *
	 * @param sql sql语句
	 * @return 受影响的行数。
	 */
	int update( String sql );

	/**
	 * 批量执行sql获取返回结果。
	 *
	 * @param sql sql语句
	 * @param params 每行sql对应的参数列表。
	 * @return 每条sql执行后受影响的行数。
	 */
	int[] batch( String sql, Object[][] params );

	/**
	 * 根据map集合中的字段生成指定表的插入语句执行数据插入动作。
	 *
	 * @param table 表名
	 * @param fields 字段数据集合
	 * @return 受影响的记录数。
	 */
	int insert( String table, Map<String, Object> fields );

	/**
	 * 根据map集合中的字段生成指定表的更新语句执行数据更新动作。
	 *
	 * @param table 表名
	 * @param fields 字段数据集合
	 * @param idName 主键字段名称
	 * @return 受影响的记录数。
	 */
	int update( String table, Map<String, Object> fields, String idName );

	/**
	 * 根据map集合中的字段生成指定表的更新语句执行数据更新动作。
	 *
	 * @param table 表名
	 * @param fields 字段数据集合
	 * @return 受影响的记录数。
	 */
	int update( String table, Map<String, Object> fields );

	/**
	 * 数据库类型
	 */
	enum DBTYPE{
		Mysql
	}

}
