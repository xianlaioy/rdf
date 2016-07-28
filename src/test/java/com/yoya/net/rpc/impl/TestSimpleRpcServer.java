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

import java.io.IOException;

import com.yoya.net.rpc.IRpcServer.IHandler;
import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.session.ISession;
import com.yoya.struct.ICommandResult;
import com.yoya.struct.impl.SimpleCommandResult;

/**
 * Created by baihw on 16-6-3.
 *
 */
public class TestSimpleRpcServer{

	public static void main( String[] args ) throws InterruptedException, IOException{
		final String SERVER_HOST = "127.0.0.1";
		final int SERVER_PORT = 6066;
		SimpleRpcServer server = new SimpleRpcServer( SERVER_HOST, SERVER_PORT, new IHandler(){

			@Override
			public ICommandResult onMessage( IRequest request ){
				ISession sess = request.getSession();
				String path = request.getPath();
				sess.setAttribute( "log", sess.getAttribute( "log" ) + ", " + path );
				System.out.println( "request:" + request );
				System.out.println( "sess:" + sess );
//				if( null == sess.getAttribute( "isLogin" ) ){ throw new RuntimeException( "非法访问!" ); }
				return new SimpleCommandResult( request.getPath() );
			}
		} );
		server.start();
		Thread.sleep( 60000 * 30 );
		server.close();
	}

} // end class
