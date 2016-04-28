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

import java.net.ContentHandlerFactory;

/**
 * Created by baihw on 16-4-25.
 */
public class TestSimpleSshClient{

	public static void main( String[] args ) throws SshException {
		ISshClient sshClient = new SimpleSshClient( "192.168.20.35", 22, "testUser1", "yoya.com" );
		System.out.println( sshClient.execCommand( "ls -al" ) );
	}
}
