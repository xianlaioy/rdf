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

import java.util.concurrent.atomic.AtomicBoolean;

import com.yoya.config.IConfig;
import com.yoya.config.impl.RdbConfig;

/**
 * Created by baihw on 16-4-28.
 *
 *
 */
public class TestRdf{

	// 标记是否已经初始化过
	private static volatile AtomicBoolean _hasInit = new AtomicBoolean( false );

	/**
	 * 基于RdbConfig的框架初始化方法
	 */
	public static void initRdfByRdbConfig(){
		if( _hasInit.get() )
			return;
		_hasInit.set( true );
		String driverClassName = "com.mysql.jdbc.Driver";
		String jdbcUrl = "jdbc:mysql://127.0.0.1:3386/rdf_test_db?useUnicode=true&characterEncoding=utf8&useOldAliasMetadataBehavior=true&useSSL=false";
		String jdbcUser = "rdf_test_user";
		String jdbcPassword = "rdf_test_password";
		IConfig config = new RdbConfig( driverClassName, jdbcUrl, jdbcUser, jdbcPassword );
		Rdf.me().init( config );

		// 测试进程结束时退出框架清理资源的回调方法触发。
		Runtime.getRuntime().addShutdownHook( new Thread(){
			@Override
			public void run(){
				Rdf.me().stop();
			}
		} );
	}

}
