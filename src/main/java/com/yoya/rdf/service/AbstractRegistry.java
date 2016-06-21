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
package com.yoya.rdf.service;

import java.nio.charset.Charset;
import java.util.Objects;

import com.google.common.hash.Hashing;
import com.yoya.rdf.Rdf;

/**
 * Created by baihw on 16-6-19.
 *
 * 封装一些通用处理逻辑的服务注册中心抽象基类。
 */
public abstract class AbstractRegistry implements IRegistry{

	@Override
	public String sign( String data ){
		Objects.requireNonNull( data );
		String salt = Rdf.me().getSK();
		return sign( salt, data );
	}

	/**
	 * 使用指定的密钥对数据进行签名。
	 * 
	 * @param salt 盐值
	 * @param data 数据
	 * @return 签名值
	 */
	protected String sign( String salt, String data ){
		String result = null;
		String saltMd5 = Hashing.md5().hashString( salt, Charset.defaultCharset() ).toString();
		String dataMd5 = Hashing.md5().hashString( data, Charset.defaultCharset() ).toString();
		StringBuilder sb = new StringBuilder();
		for( int i = 0, ilen = saltMd5.length(); i < ilen; i++ ){
			sb.append( saltMd5.charAt( i ) ).append( dataMd5.charAt( i ) );
		}
		data = sb.toString();
		result = Hashing.md5().hashString( data, Charset.defaultCharset() ).toString();
		return result;
	}

} // end class
