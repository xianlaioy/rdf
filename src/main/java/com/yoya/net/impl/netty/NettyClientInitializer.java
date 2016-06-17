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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by baihw on 16-5-29.
 * 
 * netty网络通信客户端连接初始化事件处理器。
 */
final class NettyClientInitializer extends ChannelInitializer<SocketChannel>{

	private final SslContext _SSL_CTX;

	NettyClientInitializer( SslContext sslContext ){
		this._SSL_CTX = sslContext;
	}

	@Override
	protected void initChannel( SocketChannel ch ) throws Exception{
		ChannelPipeline p = ch.pipeline();
		if( null != this._SSL_CTX ){
			p.addLast( this._SSL_CTX.newHandler( ch.alloc(), "host", 6606 ) );
		}
		p.addLast( "idleStateHandler", new IdleStateHandler( 0, 0, 180 ) );
		p.addLast( "frameDecoder", new NettyMessageDecoder() );
		p.addLast( "StringDecoder", new StringDecoder() );

		p.addLast( "logicHandler", new NettyClientHandler() );
		p.addLast( "msgEncoder", new NettyMessageEncoder() ) ;
	}

} // end class
