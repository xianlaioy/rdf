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

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

import java.net.SocketAddress;

import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Created by baihw on 16-5-27.
 * 
 * netty通信相关日志处理器
 */

final class NettyLogHandler extends ChannelDuplexHandler{

	// 日志处理对象
	private static final ILog _LOG = LogManager.getLog( "NettyLogHandler" );

	@Override
	public void channelRegistered( ChannelHandlerContext ctx ) throws Exception{
		_LOG.info( contextFormat( ctx ) );
		super.channelRegistered( ctx );
	}

	@Override
	public void channelUnregistered( ChannelHandlerContext ctx ) throws Exception{
		_LOG.info( contextFormat( ctx ) );
		super.channelUnregistered( ctx );
	}

	@Override
	public void channelActive( ChannelHandlerContext ctx ) throws Exception{
		_LOG.info( contextFormat( ctx ) );
		super.channelActive( ctx );
	}

	@Override
	public void channelInactive( ChannelHandlerContext ctx ) throws Exception{
		_LOG.info( contextFormat( ctx ) );
		super.channelInactive( ctx );
	}

	@Override
	public void userEventTriggered( ChannelHandlerContext ctx, Object evt ) throws Exception{
		_LOG.info( contextFormat( ctx ) );
		super.userEventTriggered( ctx, evt );
	}

	@Override
	public void bind( ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise future ) throws Exception{
		_LOG.info( contextFormat( ctx ) );
		super.bind( ctx, localAddress, future );
	}

	@Override
	public void connect( ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise future ) throws Exception{
		_LOG.info( contextFormat( ctx ) );
		super.connect( ctx, remoteAddress, localAddress, future );
	}

	@Override
	public void disconnect( ChannelHandlerContext ctx, ChannelPromise future ) throws Exception{
		_LOG.info( contextFormat( ctx ) );
		super.disconnect( ctx, future );
	}

	@Override
	public void deregister( ChannelHandlerContext ctx, ChannelPromise future ) throws Exception{
		_LOG.info( contextFormat( ctx ) );
		super.deregister( ctx, future );
	}

	@Override
	public void close( ChannelHandlerContext ctx, ChannelPromise future ) throws Exception{
		_LOG.info( contextFormat( ctx ) );
		super.close( ctx, future );
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception{
		_LOG.error( contextFormat( ctx ).concat( ",exception: " ).concat( cause.toString() ) );
		super.exceptionCaught( ctx, cause );
	}

//	@Override
//	public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception{
//		_LOG.debug( String.format( "channel: %s, msg: %s", ctx.channel(), formatMessage( "read", msg ) ) );
//		super.channelRead( ctx, msg );
//	}
//
//	@Override
//	public void write( ChannelHandlerContext ctx, Object msg, ChannelPromise promise ) throws Exception{
//		_LOG.debug( String.format( "channel: %s, msg: %s", ctx.channel(), formatMessage( "write", msg ) ) );
//		super.write( ctx, msg, promise );
//	}
//
//	@Override
//	public void flush( ChannelHandlerContext ctx ) throws Exception{
//		_LOG.debug( String.format( "channel: %s", ctx.channel() ) );
//		super.flush( ctx );
//	}

	/**
	 * 格式化日志
	 * 
	 * @param ctx 通道
	 * @return 最终消息文本
	 */
	private String contextFormat( ChannelHandlerContext ctx ){
		Channel ch = ctx.channel();
		StringBuilder sb = new StringBuilder();
		sb.append( "channel:" );
		sb.append( "{local:" ).append( ch.localAddress() );
		sb.append( ",remote:" ).append( ch.remoteAddress() );
		sb.append( "}" );
		return sb.toString();
	}

	protected String formatMessage( String eventName, Object msg ){
		if( msg instanceof ByteBuf ){
			return formatByteBuf( eventName, ( ByteBuf )msg );
		}else if( msg instanceof ByteBufHolder ){
			return formatByteBufHolder( eventName, ( ByteBufHolder )msg );
		}else{
			return formatNonByteBuf( eventName, msg );
		}
	}

	private String formatByteBuf( String eventName, ByteBuf msg ){
		int length = msg.readableBytes();
		if( length == 0 ){
			StringBuilder buf = new StringBuilder( eventName.length() + 4 );
			buf.append( eventName ).append( ": 0B" );
			return buf.toString();
		}else{
			int rows = length / 16 + ( length % 15 == 0 ? 0 : 1 ) + 4;
			StringBuilder buf = new StringBuilder( eventName.length() + 2 + 10 + 1 + 2 + rows * 80 );

			buf.append( eventName ).append( ": " ).append( length ).append( 'B' ).append( NEWLINE );
			appendPrettyHexDump( buf, msg );

			return buf.toString();
		}
	}

	private String formatNonByteBuf( String eventName, Object msg ){
		return eventName + ": " + msg;
	}

	private String formatByteBufHolder( String eventName, ByteBufHolder msg ){
		String msgStr = msg.toString();
		ByteBuf content = msg.content();
		int length = content.readableBytes();
		if( length == 0 ){
			StringBuilder buf = new StringBuilder( eventName.length() + 2 + msgStr.length() + 4 );
			buf.append( eventName ).append( ", " ).append( msgStr ).append( ", 0B" );
			return buf.toString();
		}else{
			int rows = length / 16 + ( length % 15 == 0 ? 0 : 1 ) + 4;
			StringBuilder buf = new StringBuilder( eventName.length() + 2 + msgStr.length() + 2 + 10 + 1 + 2 + rows * 80 );

			buf.append( eventName ).append( ": " ).append( msgStr ).append( ", " ).append( length ).append( 'B' ).append( NEWLINE );
			appendPrettyHexDump( buf, content );

			return buf.toString();
		}
	}

} // end class
