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
package com.yoya.rdf.router.application.impl;

import com.yoya.sql.SqlRunner;

/**
 * Created by baihw on 16-5-19.
 *
 * 基于关系型数据库实现的应用全局共享操作对象
 */
public class MysqlApplication extends RdbApplication{

	static{
		String sql = "SHOW TABLES LIKE '".concat( TABLE_NAME ).concat( "'; " );
		Object tableName = SqlRunner.impl().queryScalar( sql );
		if( null == tableName ){
			StringBuilder sb = new StringBuilder();
			sb.append( "CREATE TABLE `" ).append( TABLE_NAME ).append( "` (" );
			sb.append( "`id` VARCHAR(64) NOT NULL," );
			sb.append( "`data` MEDIUMTEXT," );
			sb.append( "`createTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," );
			sb.append( "`updateTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," );
			sb.append( "PRIMARY KEY (`id`)" );
			sb.append( ") ENGINE=InnoDB DEFAULT CHARSET=utf8;" );
			sql = sb.toString();

			SqlRunner.impl().update( sql );
		}
	}
	
} // end class
