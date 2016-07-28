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

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by baihw on 16-5-26.
 *
 * netty服务端处理器
 */
final class NettyServerHandler extends ByteToMessageDecoder{

	// ssl环境
	private final SslContext	_SSL_CTX;
	// 是否探测ssl请求
	private final boolean		_detectSsl;
	// 是否探测gzip请求
	private final boolean		_detectGzip;

	/**
	 * 构造函数
	 * 
	 * @param sslContext ssl环境
	 */
	public NettyServerHandler( SslContext sslContext ){
		this( sslContext, false, false );
	}

	/**
	 * 构造函数
	 * 
	 * @param sslContext ssl环境
	 * @param detectSsl 检测ssl请求
	 * @param detectGzip 检测gzip请求
	 */
	public NettyServerHandler( SslContext sslContext, boolean detectSsl, boolean detectGzip ){
		this._SSL_CTX = sslContext;
		this._detectSsl = detectSsl;
		this._detectGzip = detectGzip;
	}

	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception{
		if( in.readableBytes() < 5 ){ return; }
		if( isSsl( in ) ){
			enableSsl( ctx );
		}else{
			final int magic1 = in.getUnsignedByte( in.readerIndex() );
			final int magic2 = in.getUnsignedByte( in.readerIndex() + 1 );
			if( isGzip( magic1, magic2 ) ){
				enableGzip( ctx );
			}else if( isHttp( magic1, magic2 ) ){
				switchToHttp( ctx );
			}else if( isService( magic1, magic2 ) ){
				switchToService( ctx );
			}else{
				System.out.println( "nknown protocol, discard everything and close the connection." );
				in.clear();
				ctx.close();
			}
		}
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception{
		cause.printStackTrace();
		ctx.close();
	}

	// 判断是否是ssl请求。
	private boolean isSsl( ByteBuf buf ){
		if( this._detectSsl ){ return SslHandler.isEncrypted( buf ); }
		return false;
	}

	// 启用ssl支持。
	private void enableSsl( ChannelHandlerContext ctx ){
		ChannelPipeline p = ctx.pipeline();
		p.addLast( "ssl", this._SSL_CTX.newHandler( ctx.alloc() ) );
		p.addLast( "nettyServerA", new NettyServerHandler( this._SSL_CTX, false, this._detectGzip ) );
		p.remove( this );
	}

	// 判断是否是gzip请求
	private boolean isGzip( int magic1, int magic2 ){
		if( this._detectGzip ){ return magic1 == 31 && magic2 == 139; }
		return false;
	}

	// 启用gzip支持。
	private void enableGzip( ChannelHandlerContext ctx ){
		ChannelPipeline p = ctx.pipeline();
		p.addLast( "gzipdeflater", ZlibCodecFactory.newZlibEncoder( ZlibWrapper.GZIP ) );
		p.addLast( "gzipinflater", ZlibCodecFactory.newZlibDecoder( ZlibWrapper.GZIP ) );
		p.addLast( "nettyServerB", new NettyServerHandler( this._SSL_CTX, this._detectSsl, false ) );
		p.remove( this );
	}

	// 判断是否是Http请求
	private static boolean isHttp( int magic1, int magic2 ){
		return magic1 == 'G' && magic2 == 'E' || // GET
				magic1 == 'P' && magic2 == 'O' || // POST
				magic1 == 'P' && magic2 == 'U' || // PUT
				magic1 == 'H' && magic2 == 'E' || // HEAD
				magic1 == 'O' && magic2 == 'P' || // OPTIONS
				magic1 == 'P' && magic2 == 'A' || // PATCH
				magic1 == 'D' && magic2 == 'E' || // DELETE
				magic1 == 'T' && magic2 == 'R' || // TRACE
				magic1 == 'C' && magic2 == 'O'; // CONNECT
	}

	// 切换处理器为http服务逻辑处理器
	private void switchToHttp( ChannelHandlerContext ctx ){
		ChannelPipeline p = ctx.pipeline();
		p.addLast( "decoder", new HttpRequestDecoder() );
		p.addLast( "encoder", new HttpResponseEncoder() );
		p.addLast( "deflater", new HttpContentCompressor() );
		p.addLast( "handler", new HttpHandler() );
		p.remove( this );
	}

	// 判断是否是service请求
	private static boolean isService( int magic1, int magic2 ){
		return '{' == magic1 && '\'' == magic2;
	}

	// 切换处理器为service服务逻辑处理器
	private void switchToService( ChannelHandlerContext ctx ){
		ChannelPipeline p = ctx.pipeline();
		p.addLast( "idleStateHandler", new IdleStateHandler( 0, 0, 180 ) );
		p.addLast( "msgDecoder", new NettyMessageDecoder() );
		p.addLast( "StringDecoder", new StringDecoder() );
		p.addLast( "handler", new ServiceHandler() );
		p.addLast( "msgEncoder", new NettyMessageEncoder() ) ;
		p.remove( this );
	}
	
} // end class
