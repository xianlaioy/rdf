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
package com.yoya.net.impl.netty;

import com.yoya.net.IClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;

/**
 * Created by baihw on 16-5-29.
 * 
 * 基于netty实现的网络通信客户端。
 */
public class NettyClient implements IClient{

	// 远程主机地址
	private final String			_HOST;
	// 远程主机端口
	private final int				_PORT;

	// ssl
	private final SslContext		_SSL_CTX;

	// 事件处理线程池
	private final NioEventLoopGroup	_GROUP;

//	// 同步锁。
//	private final CountDownLatch	_LATCH	= new CountDownLatch( 1 );

	// 通信通道。
	private Channel					_channel;
	// 最后一次写入的异步结果对象。
	private ChannelFuture			_lastWriteFuture;

	/**
	 * 构造函数
	 * 
	 * @param host 服务端主机地址
	 * @param port 服务端主机端口
	 */
	public NettyClient( String host, int port ){
		this._HOST = host;
		this._PORT = port;
		this._SSL_CTX = null;
//		this._SSL_CTX = SslContextBuilder.forClient().trustManager( InsecureTrustManagerFactory.INSTANCE ).build() ;
		this._GROUP = new NioEventLoopGroup();
	}

	public void connect(){
		if( null != this._channel )
			return;

		Bootstrap b = new Bootstrap();
		b.group( this._GROUP );
		b.channel( NioSocketChannel.class );
		b.handler( new NettyClientInitializer( this._SSL_CTX ) );

		try{
			_channel = b.connect( this._HOST, this._PORT ).sync().channel();
		}catch( InterruptedException e ){
			throw new RuntimeException( e );
		}
	}

	public void send( Object msg ){
		_lastWriteFuture = _channel.writeAndFlush( msg );
	}

	public void disConnect(){
		try{
			_channel.closeFuture().sync();
			if( null != _lastWriteFuture ){
				_lastWriteFuture.sync();
			}
		}catch( InterruptedException e ){
			e.printStackTrace();
		}finally{
			this._GROUP.shutdownGracefully();
		}
		this._channel = null;
	}

	public String getHost(){
		return this._HOST;
	}

	public int getPort(){
		return this._PORT;
	}

} // end class
