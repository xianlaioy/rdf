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

import com.google.common.io.ByteStreams;
import com.yoya.rdf.Rdf;
import com.yoya.rdf.router.IHttpRequest;
import com.yoya.rdf.router.IHttpResponse;
import com.yoya.rdf.router.IRouter;
import com.yoya.rdf.router.impl.SimpleHttpResponse;
import com.yoya.rdf.router.impl.WebRouter;
import com.yoya.rdf.router.session.ISession;
import com.yoya.rdf.router.session.impl.MysqlSession;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

/**
 * Created by baihw on 16-7-17.
 *
 * http请求逻辑分发处理器。
 */
final class NettyHttpServerHandler extends SimpleChannelInboundHandler<Object>{

	// 路由管理器。
	private static final IRouter<IHttpRequest, IHttpResponse>	_ROUTER		= new WebRouter();

	// 请求包装对象
	private NettyHttpRequestWrapper								_request	= null;

	public NettyHttpServerHandler(){
	}

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, Object msg ) throws Exception{

		if( msg instanceof HttpRequest ){
			HttpRequest nettyReq = ( HttpRequest )msg;
			if( HttpHeaders.is100ContinueExpected( nettyReq ) ){
				send100Continue( ctx );
			}

			// 包装框架请求对象
			_request = new NettyHttpRequestWrapper( msg );
		}else if( null != _request && ( msg instanceof HttpContent ) ){

			_request.parseHttpContent( ( HttpContent )msg );
			SimpleHttpResponse response = new SimpleHttpResponse();

			// 路由请求。
			_ROUTER.route( _request, response );

			final ByteBuf nettyResBuf;
			final HttpResponseStatus nettyResStatus;
			// 处理响应结果。
			if( response.isOk() ){
				nettyResStatus = HttpResponseStatus.OK;

				// 根据响应数据类型进行响应处理。
				IHttpResponse.Type resultType = response.getDataType();
				if( null == resultType ){
					resultType = IHttpResponse.Type.TEXT;
				}
				if( !response.hasHeader( IHttpResponse.HEAD_CONTENT_TYPE ) )
					response.setHeader( Names.CONTENT_TYPE, resultType.getContentType() );

				// 下载响应特殊处理
				if( IHttpResponse.Type.STREAM == resultType ){
					nettyResBuf = Unpooled.copiedBuffer( ByteStreams.toByteArray( response.getDataInputStream() ) );
				}else{
					// 禁止浏览器缓存
					response.setHeader( "Pragma", "no-cache" );
					response.setHeader( "Cache-Control", "no-cache" );
					response.setHeader( "Access-Control-Allow-Origin", "*" );

					nettyResBuf = Unpooled.copiedBuffer( response.getDataString(), Rdf.me().getCharset() );
				}
			}else{
				nettyResStatus = new HttpResponseStatus( response.getStatus(), response.getError() );
				nettyResBuf = Unpooled.copiedBuffer( response.getError(), Rdf.me().getCharset() );
			}

			// Build the response object.
			FullHttpResponse nettyRes = new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, nettyResStatus, nettyResBuf );

			// 设置头信息
			response.getHeader().forEach( ( key, value ) -> {
				nettyRes.headers().set( key, value );
			} );

			ServerCookieEncoder cookieEncoder = ServerCookieEncoder.STRICT;
			// 设置cookie信息
			response.getCookie().forEach( ( name, value ) -> {
				nettyRes.headers().add( Names.SET_COOKIE, cookieEncoder.encode( name, value ) );
			} );

			// 设置会话标识持久化Cookie
			if( _request.hasSession() ){
				ISession session = _request.getSession();
				String ck_session_id;
				if( session.isNew() || null == ( ck_session_id = _request.getCookie( ISession.KEY_SESSIONID ) ) || 0 == ( ck_session_id = ck_session_id.trim() ).length() ){
					// 设置浏览器会话标识Cookie，浏览器关闭失效。
					Cookie sessionCookie = new DefaultCookie( ISession.KEY_SESSIONID, _request.getSession().getId() );
//					sessionCookie.setDomain( null );
					sessionCookie.setPath( "/" );
					sessionCookie.setMaxAge( -1 );
					sessionCookie.setHttpOnly( true );
					sessionCookie.setSecure( false );
					nettyRes.headers().add( Names.SET_COOKIE, cookieEncoder.encode( sessionCookie ) );
				}

				// 如果是MysqlSession，为避免频繁操作数据库，所以在请求结束时手工调用同步方法同步session数据到数据库。
				if( session instanceof MysqlSession ){
					( ( MysqlSession )session ).sync();
				}
			}

			// Decide whether to close the connection or not.
			boolean keepAlive = HttpHeaders.isKeepAlive( _request.getRaw() );
			if( keepAlive ){
				// Add 'Content-Length' header only for a keep-alive connection.
				nettyRes.headers().set( Names.CONTENT_LENGTH, nettyRes.content().readableBytes() );
				// Add keep alive header as per:
				// - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
				nettyRes.headers().set( Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE );
			}

			// Write the response.
			ctx.write( nettyRes );

			if( !keepAlive ){
				// If keep-alive is off, close the connection once the content is fully written.
				ctx.writeAndFlush( Unpooled.EMPTY_BUFFER ).addListener( ChannelFutureListener.CLOSE );
			}
		}

	}

	@Override
	public void channelReadComplete( ChannelHandlerContext ctx ) throws Exception{
		ctx.flush();
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception{
		cause.printStackTrace();
		ctx.close();
	}

	private static void send100Continue( ChannelHandlerContext ctx ){
		FullHttpResponse response = new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE );
		ctx.write( response );
	}

} // end class
