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

import java.io.InputStream;
import java.util.Map;

/**
 * Created by baihw on 16-6-12.
 *
 * 特定于Http协议的响应对象规范接口
 */
public interface IHttpResponse extends IResponse{

	/**
	 * HTTP协议头中的响应数据类型对应的头信息名称
	 */
	String HEAD_CONTENT_TYPE = "Content-Type";

//	/**
//	 * HTTP协议头中的响应数据类型头信息对应设置值
//	 */
//	@SuppressWarnings( { "unchecked", "serial", "rawtypes" } )
//	Map<Type, String>	CONTENT_TYPES		= new HashMap(){
//												{
//													put( Type.JSON, "application/json;charset=UTF-8" );
//													put( Type.JS, "application/json;charset=UTF-8" );
//													put( Type.JSON, "application/json;charset=UTF-8" );
//													put( Type.JSON, "application/json;charset=UTF-8" );
//													put( Type.JSON, "application/json;charset=UTF-8" );
//												}
//											};

	/**
	 * 设置响应头数据
	 *
	 * @param header 头信息名称
	 * @param value 头信息值
	 * @return 当前对象
	 */
	IResponse setHeader( String header, String value );

	/**
	 * 获取响应头数据。
	 *
	 * @return 响应头数据。
	 */
	Map<String, String> getHeader();

	/**
	 * 获取指定名称的响应头数据。
	 *
	 * @param headerName 头名称
	 * @return 头数据
	 */
	String getHeader( String headerName );

	/**
	 * 检查是否存在指定名称的响应头
	 *
	 * @param headerName 头名称
	 * @return 存在返回 true / 不存在返回 false
	 */
	boolean hasHeader( String headerName );

	/**
	 * 设置响应cookie数据
	 * 
	 * @param name cookie名称
	 * @param value cookie值
	 * @return 当前对象
	 */
	IResponse setCookie( String name, String value );

	/**
	 * 获取 响应cookie数据
	 * 
	 * @return cookie数据
	 */
	Map<String, String> getCookie();

	/**
	 * 获取返回类型的HTTP协议响应头信息字符串。
	 *
	 * @return HTTP协议响应内容类型
	 */
	String getContentType();

	/**
	 * 响应数据类型
	 */
	enum Type{

		/**
		 * json数据
		 */
		JSON( "application/json;charset=UTF-8" ),

		/**
		 * javascript数据
		 */
		JS( "text/javascript;charset=UTF-8" ),

		/**
		 * jsoup数据
		 */
		JSOUP( "text/javascript;charset=UTF-8" ),

		/**
		 * xml数据
		 */
		XML( "text/xml;charset=UTF-8" ),

		/**
		 * Html格式数据
		 */
		HTML( "text/html;charset=UTF-8" ),

		/**
		 * 简单文本数据
		 */
		TEXT( "text/plain;charset=UTF-8" ),

		/**
		 * 二进制流文件
		 */
		STREAM( "application/octet-stream" );

		/**
		 * 类型对应的Content-Type值。
		 */
		private final String _CONTENT_TYPE;

		Type( String contentType ){
			this._CONTENT_TYPE = contentType;
		}

		/**
		 * @return 当前类型对应的Content-Type值。
		 */
		public String getContentType(){
			return this._CONTENT_TYPE;
		}

	}

	/**
	 * 获取响应数据类型
	 *
	 * @return 响应数据类型
	 */
	Type getDataType();

	/**
	 * 设置响应数据,重复设置以最后一次设置的为最终结果，后边的设置会覆盖前边的设置。
	 *
	 * @param dataType 数据类型
	 * @param data 数据
	 * @return 当前对象
	 */
	IResponse setData( Type dataType, String data );

	/**
	 * 设置响应数据流,重复设置以最后一次设置的为最终结果，后边的设置会覆盖前边的设置。
	 *
	 * @param contentType http协议响应头信息中的数据类型
	 * @param dataInputStream 数据流
	 * @return 当前对象
	 */
	IResponse setDataInputStream( String contentType, InputStream dataInputStream );

	/**
	 * 获取响应数据流。
	 *
	 * @return 响应数据流。
	 */
	InputStream getDataInputStream();

	/**
	 * 设置一个标准CMD格式的json响应数据。C:code, M:message, D:data.
	 *
	 * @param code 响应代码
	 * @param msg 响应消息
	 * @param data 响应数据
	 * @return CMD格式的json响应数据
	 */
	IResponse setDataByJsonCMD( int code, String msg, Object data );

	/**
	 * 设置一个标准CMD格式的json响应数据。code: 200, msg: null
	 *
	 * @param data 响应数据
	 * @return CMD格式的json响应数据
	 */
	IResponse setDataByJsonCMD( Object data );

	/**
	 * 设置一个标准CMD格式的json响应数据。通常用于响应错误信息。data: null.
	 *
	 * @param code 响应代码
	 * @param msg 响应消息
	 * @return CMD格式的json响应数据
	 */
	IResponse setDataByJsonCMD( int code, String msg );

} // end class
