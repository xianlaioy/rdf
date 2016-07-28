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
package com.yoya.rdf.router.session;

import com.yoya.rdf.TestRdf;
import com.yoya.rdf.router.session.impl.MysqlSession;

/**
 * Created by baihw on 16-5-18.
 *
 */
public class TestSessionManger{

	public static void main( String[] args ){
		
		TestRdf.initRdfByRdbConfig();

		MysqlSession session = ( MysqlSession )SessionManger.me().getSession( "32f66f46bafc4ba298ec424ec9f92488" );
		System.out.println( "session:" + session.getAttributeNames() );
//		session.setAttribute( "isLogin", false ) ;
		System.out.println( session.getId() + ", session:" + session.getAttributeNames() );
		session.sync();
		
	}

} // end class
