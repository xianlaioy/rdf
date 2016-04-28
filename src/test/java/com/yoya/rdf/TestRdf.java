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

package com.yoya.rdf;

import com.yoya.sql.ISqlRunner;
import com.yoya.sql.impl.SimpleSqlRunner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by baihw on 16-4-28.
 *
 *
 */
public class TestRdf{

	public static void main( String[] args ){

		Rdf.me().init();

		String jobId = "3b832d96-6877-4281-b215-416917321646";
		String jobName = "test_job1";
		String jobType = "python";
		String jobDescribe = "update....";
		String jobScript = "ls -al";

		ISqlRunner _SR = new SimpleSqlRunner( "127.0.0.1", 3386, "yoya_itman", "yoya_itman", "itman_yoya" );
		System.out.println( _SR.queryMapList( "select * from t_job_info where id=?", jobId ) );

		// 有id,修改。
		Map<String, Object> fieldMap = new LinkedHashMap<>( 7, 1.0f );
		fieldMap.put( "id", jobId );
		fieldMap.put( "job_name", jobName );
		fieldMap.put( "job_type", jobType );
		fieldMap.put( "job_describe", jobDescribe );
		fieldMap.put( "job_script", jobScript );
		fieldMap.put( "edit_time", System.currentTimeMillis() );
		fieldMap.put( "edit_user", "login_user" );
		int rowCount = _SR.update( "t_job_info", fieldMap );

		System.out.println( "rowCount:" + rowCount );
	}

}
