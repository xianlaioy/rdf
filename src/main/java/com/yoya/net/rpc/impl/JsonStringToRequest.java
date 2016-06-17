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
package com.yoya.net.rpc.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.IRequestBuilder;
import com.yoya.rdf.router.impl.SimpleRequestBuilder;
import com.yoya.rdf.router.session.ISession;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.AttributeKey;

/**
 * Created by baihw on 16-6-2.
 *
 * json字符串转换为request对象。
 */
final class JsonStringToRequest extends MessageToMessageDecoder<String>{

	// 会话属性存储标志键
	private static final AttributeKey<String> _KEY_SID = AttributeKey.newInstance( "__SESSION_ID__" );

	@SuppressWarnings( "unchecked" )
	@Override
	protected void decode( ChannelHandlerContext ctx, String requestMsg, List<Object> out ) throws Exception{
		JSONObject jsonRoot = null;
		try{
			jsonRoot = JSON.parseObject( requestMsg );
		}catch( Exception e ){
			throw new RuntimeException( "非法的请求数据格式！error:".concat( e.getMessage() ) );
		}
		IRequestBuilder requestBuilder = new SimpleRequestBuilder();
		requestBuilder.setRequestId( jsonRoot.getString( "requestId" ) );
		requestBuilder.setPath( jsonRoot.getString( "path" ) );
		Map<String, String> headers = ( Map<String, String> )jsonRoot.get( "headers" );
		String sid = ctx.attr( _KEY_SID ).get();
		if( null == sid || 0 == ( sid = sid.trim() ).length() ){
			sid = UUID.randomUUID().toString().replace( "-", "" );
			ctx.attr( _KEY_SID ).set( sid );
		}
		requestBuilder.addHeaders( headers );
		requestBuilder.addHeader( ISession.KEY_SESSIONID, sid );
		Map<String, String> parameters = ( Map<String, String> )jsonRoot.get( "parameters" );
		requestBuilder.addParameters( parameters );
		IRequest request = requestBuilder.build();
		out.add( request );
	}

} // end class
