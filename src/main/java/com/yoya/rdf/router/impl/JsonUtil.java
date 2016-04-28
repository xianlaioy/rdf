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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Created by baihw on 16-4-15.
 *
 * json格式数据处理工具类。
 */
final class JsonUtil{

	public static final String	KEY_CODE	= "code";
	public static final String	KEY_MSG		= "msg";
	public static final String	KEY_DATA	= "data";

	// google gson 实例
	private static final Gson	_GSON		= new Gson();

	/**
	 * 对象转json字符串
	 *
	 * @param obj 对象
	 * @return json字符串
	 */
	public static String toJsonString( Object obj ){
		return _GSON.toJson( obj );
	}

	/**
	 * CMD格式的json字符串
	 *
	 * @param code 代码
	 * @param msg 消息
	 * @param data 数据
	 * @return json字符串
	 */
	public static String toJsonCMD( int code, String msg, Object data ){
		JsonObject resultObj = new JsonObject();
		resultObj.addProperty( KEY_CODE, code );
		resultObj.addProperty( KEY_MSG, msg );
		resultObj.add( KEY_DATA, _GSON.toJsonTree( data ) );
		return resultObj.toString();
	}

	/**
	 * 解析json字符串返回指定的对象
	 *
	 * @param jsonString json数据字符串
	 * @param dataType 要返回的数据类型
	 * @param <T> 数据类型限定对象
	 * @return 指定的数据类型实例
	 */
	public static <T> T fromJson( String jsonString, Class<T> dataType ){
		return _GSON.fromJson( jsonString, dataType );
	}

}
