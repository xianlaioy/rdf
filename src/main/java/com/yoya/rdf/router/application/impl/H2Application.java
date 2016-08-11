package com.yoya.rdf.router.application.impl;

import com.yoya.sql.SqlRunner;
/**
 * 
 * @author yongda
 * 基于h2数据库实现的application
 */
public class H2Application extends RdbApplication{
	
	static{
		String sql = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where table_name = '".concat( TABLE_NAME ).concat( "'; " );
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
}
