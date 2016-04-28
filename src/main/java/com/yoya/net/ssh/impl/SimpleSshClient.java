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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Strings;
import com.jcraft.jsch.*;
import com.yoya.net.ssh.ISshClient;
import com.yoya.net.ssh.SshException;

/**
 * Created by baihw on 16-4-23.
 *
 * 一个简单的sshClient实现。
 */
public class SimpleSshClient implements ISshClient{

	private final String	_host;
	private final int		_port;
	private final String	_loginKey;
	private final String	_loginUser;
	private final String	_loginPwd;

	private final Session	_sshSession;

	/**
	 * 构造函数
	 *
	 * @param host 主机地址
	 * @param port 主机端口
	 * @param loginUser 登陆账号
	 * @param loginPwd 登陆密码
	 */
	public SimpleSshClient( String host, int port, String loginUser, String loginPwd ){
		this( host, port, null, loginUser, loginPwd );
	}

	/**
	 * 构造函数
	 *
	 * @param host 主机地址
	 * @param port 主机端口
	 * @param loginKey 登陆密钥
	 * @param loginUser 登陆账号
	 * @param loginPwd 登陆密码
	 */
	public SimpleSshClient( String host, int port, String loginKey, String loginUser, String loginPwd ){
		this._host = host;
		this._port = port;
		this._loginKey = loginKey;
		this._loginUser = loginUser;
		this._loginPwd = loginPwd;

		UserInfo loginUserInfo = new UserInfo(){
			private String _passPhrase = loginPwd;

			@Override
			public String getPassphrase(){
				return _passPhrase;
			}

			@Override
			public String getPassword(){
				return null;
			}

			@Override
			public boolean promptPassword( String message ){
//				System.out.println( "promptPassword:" + message );
				return true;
			}

			@Override
			public boolean promptPassphrase( String message ){
//				System.out.println( "promptPassphrase:" + message );
				return true;
			}

			@Override
			public boolean promptYesNo( String message ){
//				System.out.println( "promptYesNo:" + message );
				return true;
			}

			@Override
			public void showMessage( String message ){
				System.out.println( "showMessage:" + message );
			}
		};

		JSch jsch = new JSch();
//      jsch.setLogger( null );
//		jsch.addIdentity( key );
		try{
			_sshSession = jsch.getSession( loginUser, host, port );
			_sshSession.setUserInfo( loginUserInfo );
			_sshSession.setPassword( loginPwd );
		}catch( JSchException e ){
			throw new RuntimeException( e );
		}
	}

	public String execCommand( String command ) throws SshException {
		if( Strings.isNullOrEmpty( command ) )
			return null;

		int exitStatus = -1;
		StringBuilder sb = new StringBuilder();
		ByteArrayOutputStream errorOutStream = new ByteArrayOutputStream();

		try{
			_sshSession.connect( 5000 );
			ChannelExec channelExec = ( ChannelExec )_sshSession.openChannel( "exec" );
			channelExec.setCommand( command );
			channelExec.setInputStream( null );
			channelExec.setErrStream( errorOutStream );
			InputStream exec_inStream = channelExec.getInputStream();
			channelExec.connect();
			byte[] tmp = new byte[1024];
			while( true ){
				if( 0 != errorOutStream.size() ){
					sb.append( errorOutStream.toString() );
					break;
				}
				while( exec_inStream.available() > 0 ){
					int i = exec_inStream.read( tmp, 0, 1024 );
					if( i < 0 )
						break;
					sb.append( new String( tmp, 0, i ) );
				}
				if( channelExec.isClosed() ){
					exitStatus = channelExec.getExitStatus();
					break;
				}
				Thread.sleep( 1000 );
			}
			channelExec.disconnect();

		}catch( JSchException | InterruptedException | IOException e ){
			sb.append( e.getStackTrace().toString() );
			throw new RuntimeException( e );
		}finally{
			_sshSession.disconnect();
		}

		if( 0 != exitStatus ){ throw new SshException( exitStatus, sb.toString() ); }

		return sb.toString();
	}

}
