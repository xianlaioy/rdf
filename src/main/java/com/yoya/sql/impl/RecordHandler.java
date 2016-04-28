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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.yoya.sql.IRecord;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Created by baihw on 16-4-20.
 *
 * Record结果集处理器。
 */
final class RecordHandler implements ResultSetHandler<IRecord>{

	@Override
	public IRecord handle( ResultSet rs ) throws SQLException{
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		if( rs.next() ){
			IRecord record = new SimpleRecord();
			for( int i = 1; i <= cols; i++ ){
				String columnName = rsmd.getColumnLabel( i );
				if( null == columnName || 0 == columnName.length() ){
					columnName = rsmd.getColumnName( i );
				}
				record.put( columnName, rs.getObject( i ) );
			}
			return record;
		}
		return null;
	}

}
