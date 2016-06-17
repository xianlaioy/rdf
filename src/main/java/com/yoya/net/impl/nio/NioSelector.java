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
package com.yoya.net.impl.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;

/**
 * Created by baihw on 16-5-25.
 * 
 * nio事件轮询线程
 */
final class NioSelector extends Thread{

	// 日志处理对象。
	private static final ILog	_LOG	= LogManager.getLog( NioSelector.class );

	// nio.Selector
	private final Selector		_SELECTOR;

	// 线程是否被唤醒。
	private final AtomicBoolean	_WAKEUP	= new AtomicBoolean( false );

	/**
	 * 构造函数
	 * 
	 * @param name 线程名称
	 * @throws IOException io异常
	 */
	public NioSelector( String name ) throws IOException{
		super( name );
		this._SELECTOR = Selector.open();
	}

	@Override
	public void run(){
		try{
			while( !isInterrupted() ){
				_WAKEUP.getAndSet( false );
				_SELECTOR.select();
				if( _WAKEUP.get() ){
					_SELECTOR.wakeup();
				}

				handleRegister();
				handleInterestOps();

				Iterator<SelectionKey> keys = _SELECTOR.selectedKeys().iterator();
				while( keys.hasNext() ){
					SelectionKey key = keys.next();
					keys.remove();
					if( !key.isValid() )
						continue;
					Object attachObj = key.attachment();
					if( null != attachObj && ( attachObj instanceof NioSession ) ){
						( ( NioSession )attachObj ).updateTime();
					}

					try{
						if( key.isAcceptable() ){
							handleAcceptEvent( key );
						}else if( key.isConnectable() ){
							handleConnectEvent( key );
						}else if( key.isReadable() ){
							handleReadEvent( key );
						}else if( key.isWritable() ){
							handleWriteEvent( key );
						}
					}catch( Throwable e ){
						disconnectWithException( key, e );
					}

				}// end while

				handleUnregister();
			}
		}catch( Exception e ){
			_LOG.error( e.toString() );
		}
	}

	private void disconnectWithException( final SelectionKey key, final Throwable e ){
		final NioSession session = ( NioSession )key.attachment();
		if( null == session ){
			try{
				key.channel().close();
			}catch( IOException e1 ){
				_LOG.error( e1.getMessage() );
			}
			key.cancel();
			return;
		}

	}

	protected void handleRegister(){

	}

	protected void handleInterestOps(){

	}

	protected void handleAcceptEvent( SelectionKey key ) throws IOException{

	}

	protected void handleConnectEvent( SelectionKey key ) throws IOException{

	}

	protected void handleReadEvent( SelectionKey key ) throws IOException{

	}

	protected void handleWriteEvent( SelectionKey key ) throws IOException{

	}

	protected void handleUnregister(){

	}

} // end class
