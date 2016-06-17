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
package com.yoya.rdf.service.impl;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.yoya.rdf.TestRdf;
import com.yoya.rdf.router.impl.SimpleRequestBuilder;
import com.yoya.struct.ICommandResult;

/**
 * Created by baihw on 16-6-6.
 *
 * 测试服务实现。
 */
public class TestSimpleService{

	public static void main( String[] args ){
		TestRdf.initRdfByRdbConfig();

		SimpleService ss = new SimpleService();
		ss.start();

		Stopwatch sw = Stopwatch.createStarted();

		ICommandResult cr = null;

		cr = ss.call( "rdf-registry", SimpleRequestBuilder.create().setPath( "/User" ).build() );
		System.out.println( "cr:" + cr );
		
		System.out.println( "time:" + sw.stop().elapsed( TimeUnit.MILLISECONDS ) );
		sw.reset().start() ;

		for( int i = 0; i < 10; i++ ){
			cr = ss.call( "rdf-registry", SimpleRequestBuilder.create().setPath( "path-" + i ).build() );
			System.out.println( "cr:" + cr );
		}

		System.out.println( "time:" + sw.elapsed( TimeUnit.MILLISECONDS ) );

	}

} // end class
