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

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.yoya.struct.ICommandResult;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * Created by baihw on 16-6-2.
 *
 * response对象转换为json字符串。
 */
@Sharable
final class RpcResultToJsonString extends MessageToMessageEncoder<ICommandResult>{

	@Override
	protected void encode( ChannelHandlerContext ctx, ICommandResult rpcResult, List<Object> out ) throws Exception{
		JSONObject jsonRoot = new JSONObject();
		jsonRoot.put( "code", rpcResult.getCode() );
		jsonRoot.put( "msg", rpcResult.getMessage() );
		jsonRoot.put( "data", rpcResult.getData() );
		String outString = jsonRoot.toJSONString();
		ByteBuf outBuf = ByteBufUtil.encodeString( ctx.alloc(), CharBuffer.wrap( outString ), Charset.defaultCharset() );
		outBuf.writeBytes( RpcMessageDecoder.DELIMITER );
		out.add( outBuf );
	}

} // end class
