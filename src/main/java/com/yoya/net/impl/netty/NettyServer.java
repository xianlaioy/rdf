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

import java.io.IOException;

import com.yoya.net.IServer;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;

/**
 * Created by baihw on 16-5-26.
 *
 * 基于netty的网络服务端。
 */
public class NettyServer implements IServer{

	// 日志处理对象
	private static final ILog	_LOG		= LogManager.getLog( NettyServer.class );

	// 服务是否已经启动。
	protected volatile boolean	_started	= false;

	// 服务绑定主机
	private final String		_HOST;
	// 服务绑定端口
	private final int			_PORT;

	// ssl上下文
	private final SslContext	_SSL_CONTEXT;

	private EventLoopGroup		_bossGroup;
	private EventLoopGroup		_workerGroup;
	private Channel				_ch;

	/**
	 * 指定主机服务地址和服务端口的构造函数。
	 * 
	 * @param host 服务地址
	 * @param port 服务端口
	 */
	public NettyServer( String host, int port ){
		if( null == host || 0 == host.trim().length() ){
			this._HOST = DEF_HOST;
		}else{
			this._HOST = host;
		}
		if( port < 1 || port > 65535 ){
			this._PORT = DEF_PORT;
		}else{
			this._PORT = port;
		}
		this._SSL_CONTEXT = null;
//		try{
//			SelfSignedCertificate ssc = new SelfSignedCertificate();
//			this._SSL_CONTEXT = SslContextBuilder.forServer( ssc.certificate(), ssc.privateKey() ).build() ;
//		}catch( CertificateException | SSLException e ){
//			throw new RuntimeException( e ) ;
//		}
	}

	@Override
	public void start(){
		if( this._started )
			return;
		this._started = true;
		_bossGroup = new NioEventLoopGroup();
		_workerGroup = new NioEventLoopGroup();
		_bossGroup.execute( new Runnable(){

			@Override
			public void run(){

				try{
					ServerBootstrap bootstrap = new ServerBootstrap();
					bootstrap.group( _bossGroup, _workerGroup );
					bootstrap.channel( NioServerSocketChannel.class );
					bootstrap.option( ChannelOption.TCP_NODELAY, true );
					bootstrap.option( ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT );
//					bootstrap.handler( new NettyLogHandler() );
					bootstrap.childHandler( new NettyServerInitializer( _SSL_CONTEXT ) );
					_ch = bootstrap.bind( _HOST, _PORT ).sync().channel();
					_LOG.info( "server start. on:" + _HOST + ":" + _PORT );
					_ch.closeFuture().sync();
				}catch( InterruptedException e ){
					_started = false;
					throw new RuntimeException( e );
				}finally{
					_LOG.info( "server stop. on:" + _HOST + ":" + _PORT );
					_bossGroup.shutdownGracefully();
					_workerGroup.shutdownGracefully();
				}
			}

		} );
	}

	@Override
	public boolean isStarted(){
		return this._started;
	}

	@Override
	public void close() throws IOException{
		if( !this._started )
			return;
		this._started = false;
		_ch.close();

		_bossGroup.shutdownGracefully();
		_workerGroup.shutdownGracefully();
	}

} // end class
