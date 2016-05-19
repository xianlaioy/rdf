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

package com.yoya.rdf.router.impl;

import com.yoya.rdf.Rdf;
import com.yoya.rdf.router.AbstractRequest;
import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.session.ISession;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by baihw on 16-4-15.
 *
 * 简单的请求对象实现
 */
public class SimpleRequest extends AbstractRequest implements IRequest{

//    // 请求类型
//    private RequestMethod _requestMethod;

//	// 请求主机名
//	private String			_host;
//
//	// 请求端口
//	private int				_port;
//
//	// 请求资源地址
//	private String			_uri;
//
//	// 主求上下文路径
//	private String			_contextPath;
//
//	// url参数列表字符串
//	private String			_queryString;
//
//	// 请求客户端IP地址
//	private String			_clientIP;
//
//	// 内容类型
//	private String			_contentType;
//
//	// 内容长度
//	private int				_contentLength;

	// 请求内容数据
	private byte[] _bodyData;

	/**
	 * 设置body原始数据
	 *
	 * @param bodyData body原始数据
	 */
	void setBody( byte[] bodyData ){
		this._bodyData = bodyData;
	}

	@Override
	public String getBody(){
		if( null == this._bodyData )
			return null;
		try{
			return new String( this._bodyData, Rdf.me().getEncoding() );
		}catch( UnsupportedEncodingException e ){
			return new String( this._bodyData );
		}
	}

	@Override
	public byte[] getBodyData(){
		return this._bodyData;
	}

	@Override
	public List<String> getUploadFiles( String uploadDir, int maxPostSize ){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	public File getUploadFile( String uploadFileName ){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	public ISession getSession() {
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	protected Map<String, String> buildCookies(){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	protected ISession buildSession(){
		throw new UnsupportedOperationException( "method not yet!" );
	}

}
