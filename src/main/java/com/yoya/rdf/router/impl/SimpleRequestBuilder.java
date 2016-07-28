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

import java.util.HashMap;
import java.util.Map;

import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.IRequestBuilder;

/**
 * Created by baihw on 16-6-3.
 *
 * 框架提供的 一个简单的请求对象构建器实现。
 */
public class SimpleRequestBuilder implements IRequestBuilder{

	private String				_requestId;
	private String				_path;
	private Map<String, String>	_headers	= new HashMap<>();
	private Map<String, String>	_parameters	= new HashMap<>();

	@Override
	public IRequestBuilder setRequestId( String requestId ){
		this._requestId = requestId;
		return this;
	}

	@Override
	public IRequestBuilder setPath( String path ){
		this._path = path;
		return this;
	}

	@Override
	public IRequestBuilder addHeader( String name, String value ){
		this._headers.put( name, value );
		return this;
	}

	@Override
	public IRequestBuilder addHeaders( Map<String, String> headers ){
		if( null == headers )
			return this;
		this._headers.putAll( headers );
		return this;
	}

	@Override
	public IRequestBuilder addParameter( String name, String value ){
		this._parameters.put( name, value );
		return this;
	}

	@Override
	public IRequestBuilder addParameters( Map<String, String> parameters ){
		if( null == parameters )
			return this;
		this._parameters.putAll( parameters );
		return this;
	}

	@Override
	public IRequest build(){
		SimpleRequest result = new SimpleRequest( this._requestId );
		result.setPath( this._path );
		result.setHeaders( this._headers );
		result.setParameters( this._parameters );
		return result;
	}

	/**
	 * @return 创建一个请求对象构建器实例
	 */
	public static IRequestBuilder create(){
		return new SimpleRequestBuilder();
	}

	/**
	 * @return 创建一个请求对象构建器实例
	 */
	public static IRequestBuilder copy( IRequest request ){
		SimpleRequestBuilder result = new SimpleRequestBuilder();
		result.setRequestId( request.getRequestId() );
		result.setPath( request.getPath() );
		result.addHeaders( request.getHeaders() );
		result.addParameters( request.getParameters() );
		return result;
	}

} // end class
