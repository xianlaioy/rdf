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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * Created by baihw on 16-6-2.
 *
 * rpc消息解码器
 */
final class RpcMessageDecoder extends DelimiterBasedFrameDecoder{

	/**
	 * rpc消息结束分割符。
	 */
	public static final byte[] DELIMITER = "@END@".getBytes();

	public RpcMessageDecoder(){
		this( 1024 * 1024, Unpooled.copiedBuffer( DELIMITER ) );
	}

	public RpcMessageDecoder( int maxFrameLength, ByteBuf delimiter ){
		super( maxFrameLength, delimiter );
	}
} // end class
