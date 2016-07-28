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
package com.yoya.struct.impl;

import com.yoya.struct.ICommandResult;

/**
 * Created by baihw on 16-6-5.
 *
 * 框架提供的一个简单CommandResult对象。
 */
public final class SimpleCommandResult implements ICommandResult{

	// 响应代码
	private final int		_CODE;
	// 响应消息
	private final String	_MSG;
	// 响应数据
	private final Object	_DATA;

	/**
	 * 根据响应数据构建响应对象。
	 * 
	 * @param data 响应数据。
	 */
	public SimpleCommandResult( Object data ){
		this( CODE_OK, null, data );
	}

	/**
	 * 根据提示代码及提示信息构建响应对象实现。
	 * 
	 * @param code 响应代码
	 * @param message 响应消息
	 */
	public SimpleCommandResult( int code, String message ){
		this( code, message, null );
	}

	/**
	 * 根据提示代码及提示信息构建响应对象实现。
	 * 
	 * @param code 响应代码
	 * @param message 响应消息
	 * @param data 响应数据
	 */
	public SimpleCommandResult( int code, String message, Object data ){
		this._CODE = code;
		this._MSG = message;
		this._DATA = data;
	}

	@Override
	public int getCode(){
		return this._CODE;
	}

	@Override
	public String getMessage(){
		return this._MSG;
	}

	@Override
	public Object getData(){
		return this._DATA;
	}

	@Override
	public boolean isOk(){
		return CODE_OK == this._CODE;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append( "{'code':" ).append( _CODE );
		sb.append( ", 'msg':'" ).append( _MSG );
		sb.append( "', 'data':" ).append( _DATA );
		sb.append( "}" );
		return sb.toString();
	}

} // end class
