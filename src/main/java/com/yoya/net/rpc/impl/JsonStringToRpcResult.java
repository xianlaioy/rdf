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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yoya.struct.ICommandResult;
import com.yoya.struct.impl.SimpleCommandResult;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Created by baihw on 16-6-2.
 *
 * json字符串转换为rpcResult对象。
 */
final class JsonStringToRpcResult extends MessageToMessageDecoder<String>{

	@Override
	protected void decode( ChannelHandlerContext ctx, String rpcResultMsg, List<Object> out ) throws Exception{

		JSONObject jsonRoot = null;
		try{
			jsonRoot = JSON.parseObject( rpcResultMsg );
		}catch( Exception e ){
			throw new RuntimeException( "非法的请求数据格式！error:".concat( e.getMessage() ) );
		}

		ICommandResult rpcResult = null;
		Object data = jsonRoot.get( "data" );
		int code = jsonRoot.getIntValue( "code" );
		String msg = jsonRoot.getString( "msg" );
		rpcResult = new SimpleCommandResult( code, msg, data );
		out.add( rpcResult );
	}

} // end class
