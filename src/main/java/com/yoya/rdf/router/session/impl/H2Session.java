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

import com.yoya.sql.SqlRunner;

/**
 * Created by baihw on 16-8-8.
 *
 * 基于H2数据库的用户会话实现。
 */
public class H2Session extends RdbSession{

	public H2Session( String sessionId, int timeout ){
		super( sessionId, timeout );
	}

	/**
	 * 初次加载时检查初始化情况，如果是首次使用，则初始化需要的表结构。
	 */
	static{
		String sql = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where table_name = '".concat( TABLE_NAME ).concat( "'; " );
		Object tableName = SqlRunner.impl().queryScalar( sql );
		if( null == tableName ){
			StringBuilder sb = new StringBuilder();
			sb.append( "CREATE TABLE `" ).append( TABLE_NAME ).append( "` (" );
			sb.append( "`id` char(32) NOT NULL," );
			sb.append( "`data` MEDIUMTEXT," );
			sb.append( "`createTime` int NOT NULL," );
			sb.append( "`updateTime` int NOT NULL," );
			sb.append( "PRIMARY KEY (`id`)" );
			sb.append( ");" );
			sql = sb.toString();

			SqlRunner.impl().update( sql );
		}
	}

} // end class
