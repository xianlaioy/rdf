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
package com.yoya.rdf.plugins.httpServer;

import java.net.InetSocketAddress;
import java.util.Map;

import com.yoya.rdf.RdfUtil;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.plugins.IHttpServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * Created by baihw on 16-7-17.
 *
 * 基于Netty实现的HTTP通信服务器插件。
 */
public class NettyHttpServer implements IHttpServer{

	// 日志处理对象。
	private static final ILog	_LOG		= LogManager.getLog( NettyHttpServer.class );

	/**
	 * 默认的服务监听端口
	 */
	public static final int		DEF_PORT	= 9998;

	// 服务绑定主机地址
	private String				_host;
	// 服务监听端口。
	private int					_port;

	// netty工作相关线程池、网络通道。
	private EventLoopGroup		_bossGroup;
	private EventLoopGroup		_workerGroup;
	private Channel				_ch;

	// 服务是否已经启动。
	private volatile boolean	_started	= false;

	@Override
	public void init( Map<String, String> params ){
		String bindAddress = params.get( "bindAddress" );
		if( null == bindAddress || 0 == ( bindAddress = bindAddress.trim() ).length() ){
			this._host = RdfUtil.ANYIP;
			this._port = DEF_PORT;
		}else{
			InetSocketAddress address = RdfUtil.parseAddress( bindAddress, DEF_PORT );
			this._host = address.getHostString();
			this._port = address.getPort();
		}

//		// 启动文件变化监控服务。
//		Path currPath = new File( Rdf.getWebRoot() ).toPath();
//		try{
//			WatchService currWatch = currPath.getFileSystem().newWatchService();
//			currPath.register( currWatch, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE );
//			while( true ){
//				WatchKey watchKey = currWatch.poll( 1, TimeUnit.SECONDS );
//				if( null != watchKey ){
//					watchKey.pollEvents().stream().forEach( event -> {
//						System.out.println( event.count() + ", " + ((Path)event.context()).toFile() + ":" + event.kind() );
//						watchKey.reset() ;
//					} );
//				}
//			}
//		}catch( IOException e ){
//			e.printStackTrace();
//		}catch( InterruptedException e ){
//			e.printStackTrace();
//		}
	}

	@Override
	public void destroy(){
		if( !this._started )
			return;
		this._started = false;
		_ch.close();

		_bossGroup.shutdownGracefully();
		_workerGroup.shutdownGracefully();
	}

	@Override
	public void start(){
		if( _started ){ return; }
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
					bootstrap.childHandler( new ChannelInitializer<SocketChannel>(){
						@Override
						protected void initChannel( SocketChannel ch ) throws Exception{
							ChannelPipeline p = ch.pipeline();
							p.addLast( new HttpRequestDecoder() );
							// Uncomment the following line if you don't want to handle HttpChunks.
							// p.addLast(new HttpObjectAggregator(1048576));
							p.addLast( new HttpResponseEncoder() );
							// Remove the following line if you don't want automatic content compression.
							// p.addLast(new HttpContentCompressor());
							p.addLast( new NettyHttpServerHandler() );
						}
					} );
					_ch = bootstrap.bind( _host, _port ).sync().channel();
					_LOG.info( "server start. on:" + _host + ":" + _port );
					_ch.closeFuture().sync();
				}catch( InterruptedException e ){
					_started = false;
					throw new RuntimeException( e );
				}finally{
					_LOG.info( "server stop. on:" + _host + ":" + _port );
					_bossGroup.shutdownGracefully();
					_workerGroup.shutdownGracefully();
				}
			}
		} );
	}

} // end class
