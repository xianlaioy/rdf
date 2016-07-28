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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by baihw on 16-5-27.
 * 
 * service请求逻辑分发处理器
 */
final class ServiceHandler extends SimpleChannelInboundHandler<String>{

//	// 日志处理对象
//	private static final ILog			_LOG	= LogManager.getLog( ServiceHandler.class );
//
//	// 通道管理器
//	private static final ChannelGroup	_GROUP	= new DefaultChannelGroup( GlobalEventExecutor.INSTANCE );

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, String msg ) throws Exception{
		System.out.println( ctx.channel().remoteAddress() + ", msg:" + msg );
		ctx.channel().writeAndFlush( "[echo] " + msg + '\n' );
		if( "bye".equals( msg.toLowerCase() ) ){
			ctx.close();
		}
	}

	@Override
	public void userEventTriggered( ChannelHandlerContext ctx, Object evt ) throws Exception{
		if( evt instanceof IdleStateEvent ){
			// 心跳处理
		}
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception{
		cause.printStackTrace();
		ctx.close();
	}

} // end class
