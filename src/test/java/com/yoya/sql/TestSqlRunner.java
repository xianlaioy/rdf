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

import java.sql.SQLException;

import com.yoya.sql.ISqlRunner.TxMethod;

/**
 * Created by baihw on 16-5-26.
 */
public class TestSqlRunner{

	public static void main( String[] args ){
		SqlRunner.impl().tx( new TxMethod(){
			@Override
			public boolean run() throws SQLException{
				SqlRunner.impl().update( "" );
				SqlRunner.impl().update( "" );
				SqlRunner.impl().update( "" );
				SqlRunner.impl().update( "" );
				SqlRunner.impl().update( "" );
				return true;
			}
		} );
	}

} // end class
