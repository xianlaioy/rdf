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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.yoya.sql.IRecord;
import com.yoya.sql.IRecordList;
import com.yoya.sql.ISqlRunner;

/**
 * Created by baihw on 16-4-20.
 */
public class TestSimpleSqlRunner{

	private static ISqlRunner _SR;

	@BeforeClass
	public static void beforeTest(){
		// 初始化数据库操作对象。
		System.out.println( "before=>_SR:" + _SR );
		System.out.println();
	}

	@AfterClass
	public static void afterTest(){
		System.out.println( "after=>_SR:" + _SR );
		System.out.println();
	}

	@Test
	public void testQueryPageInfo(){
		System.out.println( _SR.queryPageInfo( 1, 10, "select * from t_job_info" ) );
	}

	@Test
	public void testQueryRecord(){
		IRecord record = _SR.queryRecord( "select * from t_job_info where id=?", "3b832d96-6877-4281-b215-416917321646" );
		System.out.println( "record:" + record );
	}

	@Test
	public void testQueryRecordList(){
		IRecordList recordList = _SR.queryRecordList( "select * from t_job_info" );
		System.out.println( "recordList:" + recordList );
	}

}
