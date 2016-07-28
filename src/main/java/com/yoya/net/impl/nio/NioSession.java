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
package com.yoya.net.impl.nio;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * Created by baihw on 16-5-25.
 * 
 * 连接会话保持对象
 */
final class NioSession{

	// 会话唯一标识
	private final String		_ID;
	// 套接字通道
	private final SocketChannel	_CHANNEL;
	// 会话最后更新时间
	private long				_updateTime	= System.currentTimeMillis();

	public NioSession( SocketChannel channel ){
		this._CHANNEL = channel;
		this._ID = UUID.randomUUID().toString();
	}

	public String id(){
		return this._ID;
	}

	public long getUpdateTime(){
		return this._updateTime;
	}

	public void updateTime(){
		this._updateTime = System.currentTimeMillis();
	}

	public String getRemoteAddress(){
		if( null == this._CHANNEL.socket() )
			return null;
		InetAddress addr = this._CHANNEL.socket().getInetAddress();
		if( addr == null )
			return null;
		return String.format( "%s:%d", addr.getHostAddress(), this._CHANNEL.socket().getPort() );
	}

	public String getLocalAddress(){
		if( null == this._CHANNEL.socket() )
			return null;

		SocketAddress addr = this._CHANNEL.socket().getLocalSocketAddress();
		if( null == addr )
			return null;

		return String.format( "%s", addr ).substring( 1 );
	}

	@Override
	public int hashCode(){
		return this._ID.hashCode();
	}

	@Override
	public boolean equals( Object obj ){
		if( obj instanceof NioSession ){ return this.hashCode() == ( ( NioSession )obj ).hashCode(); }
		return false;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append( "NioSession:{'id':'" ).append( this._ID );
		sb.append( "', 'remote':'', 'status': ''" );
		return sb.toString();
	}

} // end class
