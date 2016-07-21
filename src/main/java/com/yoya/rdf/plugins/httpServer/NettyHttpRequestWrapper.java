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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.yoya.rdf.Rdf;
import com.yoya.rdf.router.AbstractRequest;
import com.yoya.rdf.router.IHttpRequest;
import com.yoya.rdf.router.session.ISession;
import com.yoya.rdf.router.session.SessionManger;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

/**
 * Created by baihw on 16-7-18.
 *
 * 基于Netty数据包装的请求对象。
 */
final class NettyHttpRequestWrapper extends AbstractRequest implements IHttpRequest{

	// 原始请求对象
	private final HttpRequest	_REQ;
	// 原始请求内容数据
	private ByteBuf				_BODY_DATA;
	// 原始请求内容字符串
	private String				_BODY_STRING;
	// Cookie存放集合
	private Map<String, String>	_cookies	= null;

	public NettyHttpRequestWrapper( Object nettyReq ){
		this._REQ = ( HttpRequest )nettyReq;
//		System.out.println( "VERSION:" + nettyReq.getProtocolVersion() );
//		System.out.println( "HOSTNAME:" + HttpHeaders.getHost( nettyReq, "unknow" ) );
//		System.out.println( "METHOD:" + nettyReq.getMethod() );
//		System.out.println( "URI:" + nettyReq.getUri() );
		String uri = this._REQ.getUri();
		if( null == uri ){
			uri = "/";
		}else{
			int ndx = uri.indexOf( '?' );
			if( -1 != ndx ){
				uri = uri.substring( 0, ndx ).trim();
				if( 0 == uri.length() ){
					uri = "/";
				}
			}
		}
		this._path = uri;
		// 获取头信息数据
		HttpHeaders headers = this._REQ.headers();
		if( null != headers && !headers.isEmpty() ){
			for( Map.Entry<String, String> h : headers ){
				String key = h.getKey();
				String value = h.getValue();
				this._headers.put( key, value );
			}
		}

		// 获取参数信息数据
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder( this._REQ.getUri() );
		Map<String, List<String>> params = queryStringDecoder.parameters();
		if( !params.isEmpty() ){
			for( Entry<String, List<String>> param : params.entrySet() ){
				String key = param.getKey();
				List<String> vals = param.getValue();
				this._parameters.put( key, null == vals ? null : vals.get( 0 ) );
			}
		}
	}

	@Override
	public String getBody(){
		return this._BODY_STRING;
	}

	@Override
	public byte[] getBodyData(){
		return this._BODY_DATA.array();
	}

	@Override
	public List<String> getUploadFiles( String uploadDir, int maxPostSize ){
		throw new UnsupportedOperationException();
	}

	@Override
	public File getUploadFile( String uploadFileName ){
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String> getCookies(){
		if( null == this._cookies ){
			_cookies = Collections.unmodifiableMap( buildCookies() );
		}
		return this._cookies;
	}

	@Override
	public String getCookie( String cookieName ){
		return getCookies().get( cookieName );
	}

	@Override
	public String getCookie( String cookieName, String defValue ){
		String value = getCookies().get( cookieName );
		return null == value ? defValue : value;
	}

	@Override
	public boolean hasCookie( String cookieName ){
		return getCookies().containsKey( cookieName );
	}

	/**
	 * 解析请求，构建Cookie数据。
	 * 
	 * @return cookie键值对数据
	 */
	protected Map<String, String> buildCookies(){
		Map<String, String> ckMap = new HashMap<String, String>();
		// 获取Cookie信息数据
		String cookieString = _REQ.headers().get( Names.COOKIE );
		if( null == cookieString || 0 == ( cookieString = cookieString.trim() ).length() )
			return ckMap;
		Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode( cookieString );
		if( !cookies.isEmpty() ){
			// Reset the cookies if necessary.
			for( Cookie cookie : cookies ){
				cookie.name();
				cookie.value();
				ckMap.put( cookie.name(), cookie.value() );
			}
		}
		return ckMap;
	}

	@Override
	protected ISession buildSession(){
		// 构建会话对象
		// 先从参数中查看是否有会话保持标识
		String sessionId = getParameter( ISession.KEY_SESSIONID );
		if( null == sessionId || 0 == ( sessionId = sessionId.trim() ).length() ){
			// 参数中找不到改为从cookie中获取
			sessionId = getCookie( ISession.KEY_SESSIONID );
		}

		ISession session = SessionManger.me().getSession( sessionId );
		return session;
	}

	HttpRequest getRaw(){
		return this._REQ;
	}

	void parseHttpContent( HttpContent httpContent ){
		// 获取请求内容数据
		this._BODY_DATA = httpContent.content();
		if( this._BODY_DATA.isReadable() ){
			this._BODY_STRING = this._BODY_DATA.toString( Rdf.me().getCharset() );
			DecoderResult decoderResult = this._REQ.getDecoderResult();
			if( decoderResult.isFailure() ){ throw new RuntimeException( decoderResult.cause() ); }
		}else{
			this._BODY_STRING = null;
		}

		if( httpContent instanceof LastHttpContent ){
			LastHttpContent trailer = ( LastHttpContent )httpContent;
			HttpHeaders trailingHeaders = trailer.trailingHeaders();
			if( !trailingHeaders.isEmpty() ){
				for( String name : trailingHeaders.names() ){
					List<String> values = trailingHeaders.getAll( name );
					for( String value : values ){
						System.out.println( "NettyHttpRequestWrapper: TRAILING HEADER - " + name + ":" + value );
					}
				}
			}
		}

		// 解析请求体中的参数数据。
		parseBody();
	}

	/**
	 * 解析请求内容
	 */
	void parseBody(){
		String bodyString = getBody();
		if( null == bodyString || bodyString.length() < 1 )
			return;
		String[] itemArr = bodyString.split( "&" );
		for( String item : itemArr ){
			item = item.trim();
			if( item.length() < 2 )
				continue;
			int ndx = item.indexOf( '=' );
			if( -1 == ndx )
				continue;
			String key = item.substring( 0, ndx );
			String value = item.substring( ndx + 1 );

			try{
				key = URLDecoder.decode( key, Rdf.me().getEncoding() );
				value = URLDecoder.decode( value, Rdf.me().getEncoding() );
			}catch( UnsupportedEncodingException e ){
			}

//            // 允许多个值的情况处理
//            String[] valueArr;
//            if( _parameters.containsKey( key ) ){
//                String[] oldValues = _parameters.get( key );
//                String[] newValues = Arrays.copyOf( oldValues, oldValues.length + 1 );
//                newValues[oldValues.length] = value;
//                valueArr = newValues;
//            }else{
//                valueArr = new String[]{ value };
//            }

			_parameters.put( key, value );
		}
	}

} // end class
