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

package com.yoya.rdf.router;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.yoya.rdf.router.session.ISession;

/**
 * Created by baihw on 16-4-16.
 *
 * 统一请求对象实现基类。
 */
public abstract class AbstractRequest implements IRequest{

	// 请求唯一标识
	protected String				_requestId;

	// 具体的请求路径
	protected String				_path;

	// 头信息存放集合
	protected Map<String, String>	_headers	= new HashMap<>();

	// 参数存放集合
	protected Map<String, String>	_parameters	= new HashMap<>();

	// 属性存放集合
	protected Map<String, Object>	_attributes	= new HashMap<>();

	// Cookie存放集合
	protected Map<String, String>	_cookies	= null;

	// 会话对象
	protected ISession				_session	= null;

	public AbstractRequest(){
		this._requestId = UUID.randomUUID().toString();
	}

	/**
	 * @return 当前请求的唯一标识。
	 */
	@Override
	public String getRequestId(){
		return this._requestId;
	}

	/**
	 * 设置具体的请求路径
	 *
	 * @param path 具体的请求路径
	 */
	protected void setPath( String path ){
		this._path = path;
	}

	/**
	 * 获取请求路径,如: "/a/b"
	 *
	 * @return 请求路径
	 */
	@Override
	public String getPath(){
		return this._path;
	}

	/**
	 * 设置请求头信息数据
	 * 
	 * @param headers 头信息数据
	 */
	protected void setHeaders( Map<String, String> headers ){
		Objects.requireNonNull( headers );
		this._headers.putAll( headers );
	}

	/**
	 * 设置请求头信息
	 * 
	 * @param headerName 头信息名称
	 * @param headerValue 头信息值
	 */
	protected void setHeader( String headerName, String headerValue ){
		this._headers.put( headerName, headerValue );
	}

	@Override
	public String getHeader( String header ){
		return this._headers.get( header );
	}

	@Override
	public Set<String> getHeaderNames(){
		return Collections.unmodifiableSet( this._headers.keySet() );
	}

	/**
	 * 设置参数集合
	 *
	 * @param parameters 参数集合数据
	 */
	protected void setParameters( Map<String, String> parameters ){
		Objects.requireNonNull( parameters );
		this._parameters.putAll( parameters );
	}

	@Override
	public Set<String> getParameterNames(){
		return Collections.unmodifiableSet( this._parameters.keySet() );
	}

	@Override
	public Map<String, String> getParameters(){
		return Collections.unmodifiableMap( this._parameters );
	}

	@Override
	public String getParameter( String parameterName ){
		return this._parameters.get( parameterName );
	}

	@Override
	public void setAttr( String attrName, Object attrValue ){
		Objects.requireNonNull( attrName );
		this._attributes.put( attrName, attrValue );
	}

	@Override
	public <T> T getAttr( String attrName ){
		return getAttr( attrName, null );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getAttr( String attrName, T defValue ){
		Objects.requireNonNull( attrName );
		Object result = this._attributes.get( attrName );
		return null == result ? defValue : ( T )result;
	}

	@Override
	public Map<String, Object> getAttrMap(){
		return Collections.unmodifiableMap( this._attributes );
	}

	@Override
	public Set<String> getAttrNames(){
		return Collections.unmodifiableSet( this._attributes.keySet() );
	}

	/**
	 * 需要子类实现的cookie数据集合构建方法。
	 * 
	 * @return cookie数据集合
	 */
	protected abstract Map<String, String> buildCookies();

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
	 * 需要子类实现的session对象构建方法。
	 * 
	 * @return session对象
	 */
	protected abstract ISession buildSession();

	@Override
	public ISession getSession(){
		if( null == this._session )
			this._session = buildSession();
		return this._session;
	}

	@Override
	public boolean hasSession(){
		return null != this._session;
	}

}
