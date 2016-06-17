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
package com.yoya.net.rpc.impl;

import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.impl.SimpleRequestBuilder;
import com.yoya.struct.ICommandResult;

/**
 * Created by baihw on 16-6-3.
 *
 */
public class TestSimpleRpcClient{

	public static void main( String[] args ){

		SimpleRpcClient client = new SimpleRpcClient( "127.0.0.1", 6066 );
		long currTime = System.currentTimeMillis();
		try{
			for( int i = 0; i < 1000; i++ ){
				IRequest req = new SimpleRequestBuilder().setPath( "path-" + i ).build();
				ICommandResult result = client.send( req );
				System.out.println( i + ", result:" + result );
				if( 0 == ( i % 3 ) ){
					Thread.sleep( 1200 );
				}
			}
		}catch( Exception e ){
			System.err.println( e.getMessage() );
		}finally{
			System.out.println( "time:" + ( System.currentTimeMillis() - currTime ) );
			client.close();
		}

	}

} // end class
