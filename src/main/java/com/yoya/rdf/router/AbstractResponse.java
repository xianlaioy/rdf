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

/**
 * Created by baihw on 16-6-12.
 *
 * 抽象的响应对象实现。
 */
public abstract class AbstractResponse implements IResponse{

	// 响应状态码, 默认为:200。
	protected int		_status	= CODE_OK;

	// 响应错误信息
	protected String	_error	= null;

	// 响应数据
	protected Object	_data	= null;

	@Override
	public IResponse setStatus( int status ){
		this._status = status;
		return this;
	}

	@Override
	public int getStatus(){
		return this._status;
	}

	@Override
	public IResponse setData( Object data ){
		this._data = data;
		return this;
	}

	@Override
	public Object getData(){
		return this._data;
	}

	@Override
	public String getDataString(){
		return null == this._data ? "" : String.valueOf( this._data );
	}

	@Override
	public IResponse setError( int status, String error, Object data ){
		this._status = status;
		this._error = error;
		this._data = data;
		return this;
	}

	@Override
	public IResponse setError( int status, String error ){
		this._status = status;
		this._error = error;
		return this;
	}

	@Override
	public String getError(){
		return this._error;
	}

	@Override
	public boolean isOk(){
		return CODE_OK == this._status;
	}

} // end class
