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

package com.yoya.rdf.support.servlet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.yoya.rdf.Rdf;
import com.yoya.rdf.router.AbstractRequest;
import com.yoya.rdf.router.IRequest;

/**
 * Created by baihw on 16-4-15.
 *
 * 基于HttpServletRequest包装的请求对象
 */
final class HttpServletRequestWrapper extends AbstractRequest implements IRequest{

	/**
	 * 框架中的request对象存放于servletRequest中的属性关键字.
	 */
	static final String					KEY_IREQUEST	= "__IREQUEST__";

	// servlet容器的请求对象
	private final HttpServletRequest	_REQ;

	// 查询字符串
	private String						_queryString	= null;

	// 请求内容数据。
	private byte[]						_bodyData;
	// 请求内容数据字符串。
	private String						_bodyString;

	/**
	 * 构造函数
	 * 
	 * @param request servlet容器的请求对象
	 */
	HttpServletRequestWrapper( HttpServletRequest request ){

		Objects.requireNonNull( request );

		this._REQ = request;
		this._queryString = request.getQueryString();

		// 导入header数据到当前request对象中
		Enumeration<String> headerNames = request.getHeaderNames();
		while( headerNames.hasMoreElements() ){
			String headerName = headerNames.nextElement();
			String headerValue = request.getHeader( headerName );
			this._headers.put( headerName, headerValue );
		}

		// 导入parameter数据到当前request对象中
		request.getParameterMap().entrySet().forEach( ( paramsEntry ) -> {
			String key = paramsEntry.getKey();
			String[] values = paramsEntry.getValue();
			String value = null == values ? null : values[0];
			this._parameters.put( key, value );
		} );

		// 处理Forward跳转的旧请求属性数据。
		Object oldRequest = request.getAttribute( KEY_IREQUEST );
		if( null != oldRequest && ( oldRequest instanceof IRequest ) ){
			IRequest oldReq = ( IRequest )oldRequest;
			Set<String> oldAttrNames = oldReq.getAttrNames();
			if( null != oldAttrNames ){
				for( String oldAttrName : oldAttrNames ){
					this._attributes.put( oldAttrName, oldReq.getAttr( oldAttrName ) );
				}
			}
			// 使用Forward之前的请求唯一标识
			this._requestId = oldReq.getRequestId();
		}

		// 导入属性数据
		Enumeration<String> attrNames = request.getAttributeNames();
		while( attrNames.hasMoreElements() ){
			String attrName = attrNames.nextElement();
			if( null == attrName || 0 == ( attrName = attrName.trim() ).length() || attrName.equals( KEY_IREQUEST ) )
				continue;
			this._attributes.put( attrName, request.getAttribute( attrName ) );
		}

	}

	/**
	 * 具体的请求路径,不包括上下文环境及过滤器拦截路径。
	 * 
	 * @param path 由入口拦截器计算出的不包含上下文环境及拦截路径的真实请求路径。
	 */
	@Override
	protected void setPath( String path ){
		super.setPath( path );
	}

	@Override
	public String getBody(){
		if( null == this._bodyString ){
			try{
				this._bodyString = new String( getBodyData(), Rdf.getEncoding() );
			}catch( UnsupportedEncodingException e ){
				this._bodyString = new String( getBodyData() );
			}
		}
		return this._bodyString;
	}

	@Override
	public byte[] getBodyData(){
		if( null == this._bodyData ){
			int cLen = _REQ.getContentLength();
			try( InputStream inStream = _REQ.getInputStream(); DataInputStream dis = new DataInputStream( inStream ); ){
				_bodyData = new byte[cLen];
				dis.readFully( _bodyData );
			}catch( IOException e ){
				throw new RuntimeException( e );
			}
		}
		return this._bodyData;
	}

}
