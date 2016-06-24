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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.yoya.net.CloseChannelException;
import com.yoya.net.rpc.IRpcServer;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.router.IRequest;
import com.yoya.struct.ICommandResult;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ThreadDeathWatcher;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Created by baihw on 16-5-30.
 *
 * 框架默认提供的一个rpc服务端实现。
 */
@ChannelHandler.Sharable
public class SimpleRpcServer extends SimpleChannelInboundHandler<IRequest> implements IRpcServer{

	// 日志处理对象。
	private static final ILog	_LOG		= LogManager.getLog( SimpleRpcServer.class );

	// 服务绑定主机
	private final String		_HOST;
	// 服务绑定端口
	private final int			_PORT;
	// 最大空闲时间，超过此时间将断开客户端网络连接。单位：（秒）。
	private final int			_MAX_IDLE;

	private final IHandler		_HANDLER;

	// 通道管理器
	private final ChannelGroup	_CH_GROUP	= new DefaultChannelGroup( GlobalEventExecutor.INSTANCE );

	private EventLoopGroup		_bossGroup;
	private EventLoopGroup		_workerGroup;
	private Channel				_ch;

	// 服务是否已经启动。
	protected volatile boolean	_started	= false;

	/**
	 * 绑定服务主机及端口的实例对象
	 * 
	 * @param host 主机地址
	 * @param port 主机端口
	 * @param handler 消息事件处理器
	 */
	public SimpleRpcServer( String host, int port, IHandler handler ){
		this( host, port, 180, handler );
	}

	/**
	 * 绑定服务主机及端口的实例对象
	 * 
	 * @param host 主机地址
	 * @param port 主机端口
	 * @param idle 空闲时间，单位：秒。
	 * @param handler 消息事件处理器
	 */
	public SimpleRpcServer( String host, int port, int idle, IHandler handler ){
		Objects.requireNonNull( handler );

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
		if( idle < 1 ){
			idle = DEF_IDLE;
		}
		this._MAX_IDLE = idle;
		this._HANDLER = handler;
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
					bootstrap.childHandler( new ChannelInitializer<SocketChannel>(){
						@Override
						protected void initChannel( SocketChannel ch ) throws Exception{
							ch.config().setAllowHalfClosure( true );
							ChannelPipeline p = ch.pipeline();
							p.addLast( "idleStateHandler", new IdleStateHandler( _MAX_IDLE, _MAX_IDLE, _MAX_IDLE, TimeUnit.SECONDS ) );
							p.addLast( "msgDecoder", new RpcMessageDecoder() );
							p.addLast( "stringDecoder", new StringDecoder() );
							p.addLast( "requestDecoder", new JsonStringToRequest() );
							p.addLast( "msgHandler", SimpleRpcServer.this );
							p.addLast( "stringEncoder", new RpcResultToJsonString() );
						}
					} );
					_ch = bootstrap.bind( _HOST, _PORT ).sync().channel();
					_LOG.debug( "server start. on:" + _HOST + ":" + _PORT );
					_ch.closeFuture().sync();
				}catch( InterruptedException e ){
					_started = false;
					throw new RuntimeException( e );
				}finally{
					_LOG.debug( "server stop. on:" + _HOST + ":" + _PORT );
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

		_CH_GROUP.close().awaitUninterruptibly();
		
		Future<?> f1 = _bossGroup.shutdownGracefully();
		Future<?> f2 = _workerGroup.shutdownGracefully();
		try{
			f1.await();
			f2.await();
			ThreadDeathWatcher.awaitInactivity( 1000, TimeUnit.MICROSECONDS );
		}catch( Exception e ){
		}
	}

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, IRequest request ) throws Exception{
//		_LOG.debug( ctx.channel().remoteAddress() + ", msg:" + request );
		Channel ch = ctx.channel();
		if( null == ch || !ch.isActive() ){
			_LOG.error( "channel is not active!" );
			return;
		}

		try{
			ICommandResult res = this._HANDLER.onMessage( request );
			ch.writeAndFlush( res );
		}catch( CloseChannelException e ){
			_LOG.warn( ctx.channel() + " 通道连接强制关闭，原因:" + e.getMessage() );
			ch.close();
		}catch( Exception e ){
			_LOG.error( e.getMessage() );
		}

	}

	@Override
	public void channelActive( ChannelHandlerContext ctx ) throws Exception{
		_CH_GROUP.add( ctx.channel() );
//		_LOG.debug( _CH_GROUP.size() + ", channel:" + ctx.channel() );
	}

	@Override
	public void channelInactive( ChannelHandlerContext ctx ) throws Exception{
		_CH_GROUP.remove( ctx.channel() );
//		_LOG.debug( _CH_GROUP.size() + ", channel:" + ctx.channel() );
	}

//	@Override
//	public void channelRegistered( ChannelHandlerContext ctx ) throws Exception{
//		_LOG.debug( _CH_GROUP.size() + ", channel:" + ctx.channel() );
//	}
//
//	@Override
//	public void channelUnregistered( ChannelHandlerContext ctx ) throws Exception{
//		_LOG.debug( _CH_GROUP.size() + ", channel:" + ctx.channel() );
//	}

	@Override
	public void userEventTriggered( ChannelHandlerContext ctx, Object evt ) throws Exception{
		if( evt instanceof ChannelInputShutdownEvent ){
			ctx.channel().close();
			_CH_GROUP.remove( ctx.channel() );
		}else if( evt instanceof IdleStateEvent ){
			IdleStateEvent idleEvt = ( IdleStateEvent )evt;
			if( IdleState.READER_IDLE == idleEvt.state() ){
				_LOG.debug( "read idle. close conn." );
			}else if( IdleState.WRITER_IDLE == idleEvt.state() ){
				_LOG.debug( "writer idle. close conn." );
			}
			ctx.channel().close();
		}
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception{
//		StringWriter errorSW = new StringWriter();
//		cause.printStackTrace( new PrintWriter( errorSW ) );
//		_LOG.error( errorSW.toString() );
		_LOG.error( cause.getMessage() );
		ctx.channel().close();
	}

} // end class
