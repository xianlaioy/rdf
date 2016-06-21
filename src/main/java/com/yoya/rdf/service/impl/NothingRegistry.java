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
package com.yoya.rdf.service.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.yoya.rdf.service.AbstractRegistry;
import com.yoya.rdf.service.IRegistry;

/**
 * Created by baihw on 16-6-17.
 *
 * 无任何处理逻辑的服务注册中心。
 */
public class NothingRegistry extends AbstractRegistry implements IRegistry{

	// 接收查询请求时返回的地址信息
	private InetSocketAddress _address;

	public NothingRegistry(){
		try{
			this._address = new InetSocketAddress( InetAddress.getLocalHost().getHostAddress(), 9999 );
		}catch( UnknownHostException e ){
			this._address = new InetSocketAddress( "127.0.0.1", 9999 );
		}
	}

	@Override
	public void register( String id, InetSocketAddress address ){
	}

	@Override
	public void unRegister( String id, InetSocketAddress address ){
	}

	@Override
	public InetSocketAddress getAdress( String id ){
		return this._address;
	}

	@Override
	public boolean checkSign( String id, String data, String sign ){
		return true;
	}

} // end class
