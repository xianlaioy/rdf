/**
 *  Copyright (c) 2015-2020, 白华伟 (bhw@wee0.com).
 */
package com.yoya.net;

/**
 * Created by baihw on 16-6-13.
 *
 * 关闭通信通道连接异常，抛出此异常将关闭此通道的网络连接。
 */
public final class CloseChannelException extends RuntimeException{

	private static final long serialVersionUID = -4421515626949678528L;

	public CloseChannelException( String msg ){
		super( msg );
	}

	public CloseChannelException( Throwable throwable ){
		super( throwable );
	}

	public CloseChannelException( String msg, Throwable throwable ){
		super( msg, throwable );
	}

} // end class
