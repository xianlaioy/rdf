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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

/**
 * Created by baihw on 16-5-26.
 *
 * http请求逻辑分发处理器。
 */
final class HttpHandler extends SimpleChannelInboundHandler<Object>{

	private HttpRequest			_request;
	private final StringBuilder	buf	= new StringBuilder();

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, Object msg ) throws Exception{
		if( msg instanceof HttpRequest ){
			HttpRequest request = this._request = ( HttpRequest )msg;
			if( HttpHeaders.is100ContinueExpected( request ) ){
				send100Continue( ctx );
			}

			buf.setLength( 0 );
			buf.append( "WELCOME TO THE WILD WILD WEB SERVER\r\n" );
			buf.append( "===================================\r\n" );

			buf.append( "VERSION: " ).append( request.getProtocolVersion() ).append( "\r\n" );
			buf.append( "HOSTNAME: " ).append( HttpHeaders.getHost( request, "unknown" ) ).append( "\r\n" );
			buf.append( "REQUEST_METHOD: " ).append( request.getMethod() ).append( "\r\n" );
			buf.append( "REQUEST_URI: " ).append( request.getUri() ).append( "\r\n\r\n" );

			HttpHeaders headers = request.headers();
			if( !headers.isEmpty() ){
				for( Map.Entry<String, String> h : headers ){
					String key = h.getKey();
					String value = h.getValue();
					buf.append( "HEADER: " ).append( key ).append( " = " ).append( value ).append( "\r\n" );
				}
				buf.append( "\r\n" );
			}

			QueryStringDecoder queryStringDecoder = new QueryStringDecoder( request.getUri() );
			Map<String, List<String>> params = queryStringDecoder.parameters();
			if( !params.isEmpty() ){
				for( Entry<String, List<String>> p : params.entrySet() ){
					String key = p.getKey();
					List<String> vals = p.getValue();
					for( String val : vals ){
						buf.append( "PARAM: " ).append( key ).append( " = " ).append( val ).append( "\r\n" );
					}
				}
				buf.append( "\r\n" );
			}

			appendDecoderResult( buf, request );
		}

		if( msg instanceof HttpContent ){
			HttpContent httpContent = ( HttpContent )msg;
			ByteBuf content = httpContent.content();
			if( content.isReadable() ){
				buf.append( "CONTENT: " );
				buf.append( content.toString( CharsetUtil.UTF_8 ) );
				buf.append( "\r\n" );
				appendDecoderResult( buf, this._request );
			}

			if( msg instanceof LastHttpContent ){
				buf.append( "END OF CONTENT\r\n" );

				LastHttpContent trailer = ( LastHttpContent )msg;
				if( !trailer.trailingHeaders().isEmpty() ){
					buf.append( "\r\n" );
					for( String name : trailer.trailingHeaders().names() ){
						for( String value : trailer.trailingHeaders().getAll( name ) ){
							buf.append( "TRAILING HEADER: " );
							buf.append( name ).append( " = " ).append( value ).append( "\r\n" );
						}
					}
					buf.append( "\r\n" );
				}

				if( !writeResponse( trailer, ctx ) ){
					// If keep-alive is off, close the connection once the content is fully written.
					ctx.writeAndFlush( Unpooled.EMPTY_BUFFER ).addListener( ChannelFutureListener.CLOSE );
				}
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

	private boolean writeResponse( HttpObject currentObj, ChannelHandlerContext ctx ){
		// Decide whether to close the connection or not.
		boolean keepAlive = HttpHeaders.isKeepAlive( this._request );
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, currentObj.getDecoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer( buf.toString(), CharsetUtil.UTF_8 ) );

		response.headers().set( Names.CONTENT_TYPE, "text/plain; charset=UTF-8" );

		if( keepAlive ){
			// Add 'Content-Length' header only for a keep-alive connection.
			response.headers().set( Names.CONTENT_LENGTH, response.content().readableBytes() );
			// Add keep alive header as per:
			// - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
			response.headers().set( Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE );
		}

		// Encode the cookie.
		String cookieString = this._request.headers().get( Names.COOKIE );
		if( cookieString != null ){
//			Set<Cookie> cookies = io.netty.handler.codec.http.CookieDecoder.decode( cookieString ); 
			Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode( cookieString );
			if( !cookies.isEmpty() ){
				// Reset the cookies if necessary.
				for( Cookie cookie : cookies ){
					response.headers().add( Names.SET_COOKIE, ServerCookieEncoder.STRICT.encode( cookie ) );
				}
			}

		}else{
			// Browser sent no cookie. Add some.
//			response.headers().add( Names.SET_COOKIE, ServerCookieEncoder.encode( "key1", "value1" ) );
//			response.headers().add( Names.SET_COOKIE, ServerCookieEncoder.encode( "key2", "value2" ) );
			ServerCookieEncoder cookieEncoder = ServerCookieEncoder.STRICT;
			response.headers().add( Names.SET_COOKIE, cookieEncoder.encode( "key1", "value1" ) );
			response.headers().add( Names.SET_COOKIE, cookieEncoder.encode( "key2", "value2" ) );
		}

		// Write the response.
		ctx.write( response );

		return keepAlive;
	}

	private static void appendDecoderResult( StringBuilder buf, HttpObject o ){
		DecoderResult result = o.getDecoderResult();
		if( result.isSuccess() ){ return; }

		buf.append( ".. WITH DECODER FAILURE: " );
		buf.append( result.cause() );
		buf.append( "\r\n" );
	}

} // end class
