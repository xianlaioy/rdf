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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.yoya.net.IServer;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;

/**
 * Created by baihw on 16-5-25.
 *
 */
public class NioServer implements IServer{

	// 日志处理对象
	private static final ILog	_LOG				= LogManager.getLog( IServer.class );

	/**
	 * 默认的工作线程数量。
	 */
	public static final int		DEF_WORKER_COUNT	= 64;

	// 服务是否已经启动。
	protected volatile boolean	_started			= false;

	// 服务线程标识名称
	private String				_name				= "SelectorGroup";
	// 事件轮询线程名称前缀
	private String				_selectorNamePrefix	= "Selector";
	// 事件轮询线程数量
	private int					_selectorCount;
	// 事件轮询线程组
	private NioSelector[]		_selectors;
	// 用以处理业务逻辑的工作线程池。
	private ExecutorService		_workerPool;
	// 工作线程数量
	private int					_workerCount		= DEF_WORKER_COUNT;

	/**
	 * 构造函数。
	 */
	public NioServer(){
		ThreadPoolExecutor executor = new ThreadPoolExecutor( _workerCount, _workerCount, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>() );
		this._workerPool = executor;
		this._selectorCount = defaultSelectorCount();
		this._selectors = new NioSelector[this._selectorCount];
		for( int i = 0; i < this._selectorCount; i++ ){
			String selectorName = String.format( "%s-%s-%d", this._name, this._selectorNamePrefix, i );
			try{
				this._selectors[i] = new NioSelector( selectorName );
			}catch( IOException e ){
				throw new RuntimeException( e );
			}
		}
	}

	@Override
	public void start(){
		if( this._started )
			return;
		this._started = true;
		for( NioSelector selector : this._selectors )
			selector.start();

		_LOG.info( String.format( "%s started. (selectorCount:%d, workCount:%d)", this._name, this._selectorCount, this._workerCount ) );
	}

	public boolean isStarted(){
		return this._started;
	}

	@Override
	public void close() throws IOException{
		if( !this._started )
			return;

		for( NioSelector selector : this._selectors )
			selector.interrupt();

		_workerPool.shutdown();
		this._started = false;
		_LOG.info( String.format( "%s stopped. (selectorCount:%d, workCount:%d)", this._name, this._selectorCount, this._workerCount ) );
	}

	/**
	 * @return 计算默认的轮询者数量。
	 */
	protected static int defaultSelectorCount(){
		int c = Runtime.getRuntime().availableProcessors() >> 1;
		if( c <= 0 )
			c = 1;
		return c;
	}

} // end class
