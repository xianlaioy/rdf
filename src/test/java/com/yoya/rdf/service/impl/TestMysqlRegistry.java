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

import java.net.InetSocketAddress;

import com.yoya.rdf.Rdf;
import com.yoya.rdf.TestRdf;

/**
 * Created by baihw on 16-6-6.
 *
 * 测试基于mysql数据库实现的服务注册中心。
 */
public class TestMysqlRegistry{

	public static void main( String[] args ){
		TestRdf.initRdfByRdbConfig();

		String jdbcUrl = Rdf.me().getConfig( "doService", "registry.jdbcUrl" );
		String jdbcUser = Rdf.me().getConfig( "doService", "registry.jdbcUser" );
		String jdbcPassword = Rdf.me().getConfig( "doService", "registry.jdbcPassword" );

		MysqlRegistry registry = new MysqlRegistry( jdbcUrl, jdbcUser, jdbcPassword );

		InetSocketAddress address = null;
//		address = new InetSocketAddress( "192.168.0.112", 6066 );
//
//		registry.register( "hddy", address );

		address = registry.getAdress( "hddy" );
		System.out.println( "address:" + address );

		registry.unRegister( "hddy", address );
		System.out.println( "address:" + registry.getAdress( "hddy" ) );

	}

} // end class
