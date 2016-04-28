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

import com.yoya.rdf.router.IResponse;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by baihw on 16-4-15.
 *
 * 简单的响应对象实现。
 */
public class SimpleResponse implements IResponse{

	// 响应状态码, 默认为:200。
	private int					_statusCode	= 200;
	// 响应头信息
	private Map<String, String>	_headers	= new HashMap<>();
	// 响应数据类型。
	private Type				_dataType;
	// 响应数据。
	private String				_data;
	// 响应数据流。
	private InputStream			_dataStream;

	@Override
	public IResponse setStatus( int statusCode ){
		this._statusCode = statusCode;
		return this;
	}

	@Override
	public int getStatus(){
		return this._statusCode;
	}

	@Override
	public IResponse setHeader( String header, String value ){
		this._headers.put( header, value );
		return this;
	}

	@Override
	public Map<String, String> getHeader(){
		return this._headers;
	}

	@Override
	public String getHeader( String headerName ){
		return this._headers.get( headerName );
	}

	@Override
	public boolean hasHeader( String headerName ){
		return this._headers.containsKey( headerName );
	}

	@Override
	public IResponse setData( Type dataType, String data ){
		Objects.requireNonNull( dataType, "dataType can not be null!" );
		Objects.requireNonNull( data, "data can not be null!" );
//		if( null != this._dataType )
//			throw new IllegalStateException( "duplicate set data is not allowed!" );
		this._dataType = dataType;
		this._data = data;
		return this;
	}

	@Override
	public IResponse setDataByJsonCMD( int code, String msg, Object data ){
		setData( Type.JSON, JsonUtil.toJsonCMD( code, msg, data ) );
		return this;
	}

	@Override
	public IResponse setDataByJsonCMD( Object data ){
		setDataByJsonCMD( CODE_OK, null, data );
		return this;
	}

	@Override
	public IResponse setDataByJsonCMD( int code, String msg ){
		setDataByJsonCMD( code, msg, null );
		return this;
	}

	@Override
	public IResponse setDataInputStream( String contentType, InputStream dataInputStream ){
		this._dataType = Type.STREAM;
		this._headers.put( HEAD_CONTENT_TYPE, null == contentType ? "application/octet-stream" : contentType );
		this._dataStream = dataInputStream;
		return this;
	}

	@Override
	public Type getDataType(){
		return this._dataType;
	}

	@Override
	public String getData(){
		return this._data;
	}

	@Override
	public InputStream getDataInputStream(){
		return this._dataStream;
	}

	@Override
	public String getContentType(){
		return this._headers.get( HEAD_CONTENT_TYPE );
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append( "{statusCode:" ).append( _statusCode );
		sb.append( ", headers:" ).append( _headers );
		sb.append( ", dataType:" ).append( _dataType );
		sb.append( ", data:" ).append( _data );
		sb.append( "}" );
		return sb.toString();
	}
}
