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
package com.yoya.net.impl.netty;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * Created by baihw on 16-5-26.
 *
 * netty消息编码器
 */
public final class NettyMessageEncoder extends MessageToMessageEncoder<CharSequence>{

	// 使用的字符集
	private final Charset _CHARSET;

	/**
	 * 使用系统默认字符集创建实例
	 */
	public NettyMessageEncoder(){
		this( Charset.defaultCharset() );
	}

	/**
	 * 使用指定字符集创建实例
	 * 
	 * @param charset
	 */
	public NettyMessageEncoder( Charset charset ){
		if( charset == null ){ throw new NullPointerException( "charset" ); }
		this._CHARSET = charset;
	}

	@Override
	protected void encode( ChannelHandlerContext ctx, CharSequence msg, List<Object> out ) throws Exception{
		if( null == msg || msg.length() == 0 ){ return; }

		ByteBuf outBuf = ByteBufUtil.encodeString( ctx.alloc(), CharBuffer.wrap( msg ), this._CHARSET );
		outBuf.writeBytes( NettyMessageDecoder.DELIMITER );
		out.add( outBuf );
	}

} // end class
