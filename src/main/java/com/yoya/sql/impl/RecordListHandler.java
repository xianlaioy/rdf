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

import com.yoya.sql.IRecord;
import com.yoya.sql.IRecordList;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by baihw on 16-4-20.
 *
 * RecordList结果集处理器。
 */
final class RecordListHandler implements ResultSetHandler<IRecordList>{

	@Override
	public IRecordList handle( ResultSet rs ) throws SQLException{
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		String[] colNames = new String[colCount];
		for( int i = 1; i <= colCount; i++ ){
			String colName = rsmd.getColumnLabel( i );
			if( null == colName || 0 == colName.length() )
				colName = rsmd.getColumnName( i );
			colNames[i - 1] = colName;
		}

		IRecordList result = new SimpleRecordList();
		while( rs.next() ){
			IRecord record = new SimpleRecord();
			for( int i = 1; i <= colCount; i++ ){

				record.put( colNames[i - 1], rs.getObject( i ) );
			}
			result.add( record );
		}
		return result;
	}

}
