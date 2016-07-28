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
package com.yoya.rdf.plugin;

import java.io.IOException;

import com.yoya.rdf.TestRdf;
import com.yoya.rdf.plugins.ISMS;

/**
 * 
 **/
public class TestPluginLoader{

	public static void main( String[] args ) throws IOException{
		TestRdf.initRdfByRdbConfig();
		ISMS sms = PluginLoader.impl().getPluginImpl( ISMS.class, "" );
		System.out.println( "sms:" + sms );
		if( null != sms ){
			sms.sendMessage( "id-001", "data-001", "mobile-001", "mobile-002" );
		}
	}

} // end class
