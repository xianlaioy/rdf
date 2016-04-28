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

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.yoya.rdf.Rdf;
import com.yoya.rdf.router.IResponse;
import com.yoya.rdf.router.impl.SimpleRequest;
import com.yoya.rdf.router.impl.SimpleResponse;
import org.zbus.kit.log.Logger;
import org.zbus.kit.log.impl.JdkLoggerFactory;
import org.zbus.net.Server;
import org.zbus.net.core.IoAdaptor;
import org.zbus.net.core.IoBuffer;
import org.zbus.net.core.SelectorGroup;
import org.zbus.net.core.Session;
import org.zbus.net.http.Message;
import org.zbus.net.http.MessageCodec;

import java.io.IOException;

/**
 * Created by baihw on 16-4-16.
 *
 * 基于zbus的服务端实现
 */
public class ZbusServer{

	// 服务监听端口
	private final int	_PORT;

	// 服务对象
	private Server		_server;
	// 逻辑处理器
	private IoAdaptor	_adaptor;

	/**
	 * 构造函数
	 * 
	 * @param port 服务监听端口
	 * @param routeWorkBase 路由管理器工作基准目录
	 */
	public ZbusServer( int port, String routeWorkBase ){

		if( null == routeWorkBase || 0 == ( routeWorkBase = routeWorkBase.trim() ).length() )
			throw new RuntimeException( "routeWorkBase参数必须正确设置！" );
		System.setProperty( "rdf.routeWorkBase", routeWorkBase );

		// 执行框架初始化流程
		Rdf.me().init();

		this._PORT = port;

		// 日志设置不依赖外部日志库。
		Logger.setLoggerFactory( new JdkLoggerFactory() );

		SelectorGroup selectorGroup = new SelectorGroup();
		_server = new org.zbus.net.Server( selectorGroup );
		_adaptor = new IoAdaptor(){
			MessageCodec msgCodec = new MessageCodec();

			@Override
			public Object decode( IoBuffer buff ){
				return msgCodec.decode( buff );
			}

			@Override
			public IoBuffer encode( Object msg ){
				return msgCodec.encode( msg );
			}

			@Override
			protected void onMessage( Object message, Session sess ) throws IOException{

				Message msg = ( Message )message;

				// 包装请求对象
				ZbusRequest request = new ZbusRequest( msg );
				// 创建初始的响应对象。
				SimpleResponse response = new SimpleResponse();

				// 调用框架路由请求处理逻辑。
				Rdf.me().route( request, response );

				// 包装响应对象
				Message result = new Message();
				result.setId( msg.getId() );
				result.setResponseStatus( response.getStatus() );
				result.setHead( response.getHeader() );

				// 根据响应数据类型进行响应处理。
				IResponse.Type resultType = response.getDataType();
				if( null == resultType ){
					resultType = IResponse.Type.TEXT;
				}

				if( !response.hasHeader( IResponse.HEAD_CONTENT_TYPE ) )
					result.setHead( Message.CONTENT_TYPE, resultType.getContentType() );

				// 下载响应特殊处理
				if( IResponse.Type.STREAM == resultType ){
					result.setBody( ByteStreams.toByteArray( response.getDataInputStream() ) );

				}else{
					// 禁止浏览器缓存
					result.setHead( "Pragma", "no-cache" );
					result.setHead( "Cache-Control", "no-cache" );
					result.setHead( "Access-Control-Allow-Origin", "*" );

					result.setBody( response.getData() );
				}

				// 输出响应数据
				sess.write( result );

			}
		};
	}

	/**
	 * 启动服务
	 */
	public void start(){
		try{
			_server.start( _PORT, _adaptor );
		}catch( IOException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 停止服务
	 */
	public void stop(){
		try{
			_server.close();
		}catch( IOException e ){
			throw new RuntimeException( e );
		}
	}

}