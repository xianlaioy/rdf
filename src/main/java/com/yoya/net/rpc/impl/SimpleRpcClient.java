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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.yoya.net.rpc.IRpcClient;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.router.IRequest;
import com.yoya.struct.ICommandResult;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by baihw on 16-5-30.
 *
 * 框架默认提供的一个rpc客户端实现。
 */
@ChannelHandler.Sharable
public class SimpleRpcClient extends SimpleChannelInboundHandler<ICommandResult> implements IRpcClient{
	// 日志处理对象。
	private static final ILog		_LOG				= LogManager.getLog( SimpleRpcClient.class );

	/**
	 * 默认的最长等待时间。
	 */
	public static final int			DEF_WAIT_TIMEOUT	= 1000;

	// 服务端主机地址
	private final String			_HOST;
	// 服务端主机端口
	private final int				_PORT;

	// 最长等待时间，单位：毫秒。
	private final int				_WAIT_TIMEOUT;

	// 响应数据
	private volatile ICommandResult	_rpcResult;

	private Bootstrap				_bootStrap;
	private EventLoopGroup			_group;
	private Channel					_ch;
	private CountDownLatch			_lock				= null;
	
	/**
	 * 构建与指定主机地址与端口建立连接的客户端实现。
	 * 
	 * @param host 主机地址
	 * @param port 主机端口
	 */
	public SimpleRpcClient( String host, int port ){
		this( host, port, DEF_WAIT_TIMEOUT ) ;
	}

	/**
	 * 构建与指定主机地址与端口建立连接的客户端实现。
	 * 
	 * @param host 主机地址
	 * @param port 主机端口
	 * @param waitTimeout 最长等待时间，单位：毫秒。
	 */
	public SimpleRpcClient( String host, int port, int waitTimeout ){
		this._HOST = host;
		this._PORT = port;
		this._WAIT_TIMEOUT = waitTimeout;

		this._group = new NioEventLoopGroup();
		// 创建并初始化 Netty 客户端 Bootstrap 对象
		this._bootStrap = new Bootstrap();
		_bootStrap.group( _group );
		_bootStrap.channel( NioSocketChannel.class );
		_bootStrap.option( ChannelOption.TCP_NODELAY, true );
		_bootStrap.handler( new ChannelInitializer<SocketChannel>(){
			@Override
			public void initChannel( SocketChannel ch ) throws Exception{
				ch.config().setAllowHalfClosure( true );
				ChannelPipeline p = ch.pipeline();
//				// 客户端暂时不加入超时处理，由服务端在超时时主动断开客户端，客户端采取被动关闭策略。
//				p.addLast( "idleHandler", new IdleStateHandler( 600, 600, 600, TimeUnit.MILLISECONDS ) );
				p.addLast( "frameDecoder", new RpcMessageDecoder() );
				p.addLast( "StringDecoder", new StringDecoder() );
				p.addLast( "responseDecoder", new JsonStringToRpcResult() );
				p.addLast( "logicHandler", SimpleRpcClient.this );
				p.addLast( "msgEncoder", new RequestToJsonString() );
			}
		} );
	}

	/**
	 * 使用默认的服务端主机及端口参数与服务端建立连接
	 */
	public synchronized void connect(){
		if( isConnected() )
			return;
		try{
//			this._group = new NioEventLoopGroup();
			// 连接 RPC 服务端
			ChannelFuture future = this._bootStrap.connect( this._HOST, this._PORT ).sync();
			_ch = future.channel();
		}catch( InterruptedException e ){
			throw new RuntimeException( "new error", e );
		}
		_LOG.debug( "connect ok, address:".concat( _ch.remoteAddress().toString() ) );
	}

	/**
	 * @return 是否与服务端保持连接中
	 */
	public boolean isConnected(){
		return null != _ch && _ch.isActive();
	}

	/**
	 * 向服务端发送请求，并同步等待获取结果。
	 * 
	 * @param request 请求对象
	 * @return 响应对象
	 */
	public ICommandResult send( IRequest request ){
		try{
			_lock = new CountDownLatch( 1 );
			this._rpcResult = null;
			if( !isConnected() )
				connect();
			_ch.writeAndFlush( request ).sync();
			_lock.await( _WAIT_TIMEOUT, TimeUnit.MILLISECONDS );
		}catch( Exception e ){
			throw new RuntimeException( "send error", e );
		}finally{
			_lock.countDown();
			_lock = null;
		}
		return this._rpcResult;
	}

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, ICommandResult rpcResult ) throws Exception{
		this._rpcResult = rpcResult;
		_lock.countDown();
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception{
		StringWriter errorSW = new StringWriter();
		cause.printStackTrace( new PrintWriter( errorSW ) );
		_LOG.error( errorSW.toString() );
		ctx.close();
	}

	@Override
	public void userEventTriggered( ChannelHandlerContext ctx, Object evt ) throws Exception{
		Channel ch = ctx.channel();
		if( evt instanceof ChannelInputShutdownEvent || evt instanceof IdleStateEvent ){
			if( evt instanceof IdleStateEvent ){
				IdleStateEvent idleEvt = ( IdleStateEvent )evt;
				if( IdleState.READER_IDLE == idleEvt.state() ){
					_LOG.debug( "read idle. close conn." );
				}else if( IdleState.WRITER_IDLE == idleEvt.state() ){
					_LOG.debug( "writer idle. close conn." );
				}
			}
			ch.close();
			this._lock = null;
			this._rpcResult = null;
		}
	}

	public void close(){
		if( null != this._ch ){
			_LOG.debug( "connect close, ".concat( _ch.remoteAddress().toString() ) );
			this._ch.close();
		}
		this._lock = null;
		this._rpcResult = null;
		this._group.shutdownGracefully();
		this._group = null;
	}

} // end class
