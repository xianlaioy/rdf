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

package com.yoya.ds;

import javax.sql.DataSource;

import com.yoya.rdf.plugin.IPlugin;

import java.sql.Connection;

/**
 * Created by baihw on 16-5-13.
 *
 * 数据源管理对象规范接口。
 */
public interface IDSManager extends IPlugin{

	/**
	 * 此组件使用的配置组名称。
	 */
	String	CONFIG_GROUP	= "dsManager";

	/**
	 * 次组件使用的实现者名称配置关键字。
	 */
	String	KEY_IMPL		= "impl";

	/**
	 * 获取指定名称的数据源的数据库连接对象。
	 * 
	 * @param dsName 数据源名称
	 * @return 数据库连接对象。
	 */
	Connection getConn( String dsName );

	/**
	 * @return 默认数据源的数据库连接对象。
	 */
	Connection getConn();

	/**
	 * 获取指定名称的数据源对象。
	 * 
	 * @param dsName 数据源名称
	 * @return 数据源对象
	 */
	DataSource getDS( String dsName );

	/**
	 * @return 返回默认的数据源对象。
	 */
	DataSource getDS();

}
