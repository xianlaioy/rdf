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

package com.yoya.rdf.support.zbus;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.zbus.net.http.Message;

import com.yoya.rdf.Rdf;
import com.yoya.rdf.router.AbstractRequest;
import com.yoya.rdf.router.IHttpRequest;
import com.yoya.rdf.router.session.ISession;

/**
 * Created by baihw on 16-4-16.
 *
 * Zbus请求对象实现
 */
final class ZbusRequest extends AbstractRequest implements IHttpRequest{

	// 请求内容数据
	private byte[]	_bodyData;

	ZbusRequest( Message msg ){

		// 获取请求路径。
		String reqPath = msg.getRequestPath();
		if( null == reqPath || 0 == ( reqPath = reqPath.trim() ).length() ){
			reqPath = "/";
		}
		this._path = reqPath;

		// 获取所有请求头信息数据
		setHeaders( msg.getHead() );

		// 获取请求参数信息数据
		setParameters( msg.getRequestParams() );

		// 获取请求内容数据
		_bodyData = msg.getBody();
		// 解析请求体中的参数信息
		parseBody();

	}

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

	@Override
	public List<String> getUploadFiles( String uploadDir, int maxPostSize ){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	public File getUploadFile( String uploadFileName ){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	public ISession getSession(){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	protected ISession buildSession(){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	public Map<String, String> getCookies(){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	public String getCookie( String cookieName ){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	public String getCookie( String cookieName, String defValue ){
		throw new UnsupportedOperationException( "method not yet!" );
	}

	@Override
	public boolean hasCookie( String cookieName ){
		throw new UnsupportedOperationException( "method not yet!" );
	}

}
