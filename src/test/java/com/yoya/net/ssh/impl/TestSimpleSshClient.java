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

package com.yoya.net.ssh.impl;

import com.yoya.net.ssh.ISshClient;
import com.yoya.net.ssh.SshException;
import com.yoya.rdf.Rdf;

/**
 * Created by baihw on 16-4-25.
 */
public class TestSimpleSshClient{

	public static void testPwd() throws SshException{
		String host = Rdf.me().getConfig( "test_temp", "ssh1.host" );
		int port = Integer.parseInt( Rdf.me().getConfig( "test_temp", "ssh1.port" ) );
		String user = Rdf.me().getConfig( "test_temp", "ssh1.user" );
		String password = Rdf.me().getConfig( "test_temp", "ssh1.password" );

		ISshClient sshClient = new SimpleSshClient( host, port, user, password );
		String result = sshClient.execCommand( "ls -al ;" );
		System.out.println( "result:" + result );
	}

	public static void testKey() throws SshException{
		String host = Rdf.me().getConfig( "test_temp", "ssh1.host" );
		int port = Integer.parseInt( Rdf.me().getConfig( "test_temp", "ssh1.port" ) );
		String user = Rdf.me().getConfig( "test_temp", "ssh1.user" );
		String key = Rdf.me().getConfig( "test_temp", "ssh1.key" );

		ISshClient sshClient = new SimpleSshClient( host, port, key, user, null );
		String result = sshClient.execCommand( "ls -al ;" );
		System.out.println( "result:" + result );
	}

	public static void main( String[] args ) throws SshException{

		testPwd();

		testKey();

	}
}
