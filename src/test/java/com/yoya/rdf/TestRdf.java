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

package com.yoya.rdf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zbus.broker.Broker;
import org.zbus.broker.ZbusBroker;
import org.zbus.rpc.RpcFactory;
import org.zbus.rpc.RpcProcessor;
import org.zbus.rpc.direct.Service;
import org.zbus.rpc.direct.ServiceConfig;

import com.yoya.config.IConfig;
import com.yoya.config.impl.MysqlConfig;

/**
 * Created by baihw on 16-4-28.
 * 
 * 
 */
public class TestRdf{

	// 标记是否已经初始化过
	private static volatile AtomicBoolean _hasInit = new AtomicBoolean( false );

	/**
	 * 基于RdbConfig的框架初始化方法
	 */
	public static void initRdfByRdbConfig(){
		if( _hasInit.get() )
			return;
		_hasInit.set( true );
		String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/rdf_test_db?useUnicode=true&characterEncoding=utf8&useOldAliasMetadataBehavior=true&useSSL=false";
		String jdbcUser = "rdf_test_user";
		String jdbcPassword = "rdf_test_password";
		IConfig config = new MysqlConfig( jdbcUrl, jdbcUser, jdbcPassword );
		Rdf.me().init( config );

		// 测试进程结束时退出框架清理资源的回调方法触发。
		Runtime.getRuntime().addShutdownHook( new Thread(){
			@Override
			public void run(){
				Rdf.me().destroy();
			}
		} );
	}

	public static interface Test1{
		void run();
	}

	public static void runServer() throws IOException, InterruptedException{
		final CountDownLatch cdl = new CountDownLatch( 10 );
		Test1 t1 = new Test1(){

			@Override
			public void run(){
				cdl.countDown();
				System.out.println( "count:" + cdl.getCount() );
			}
		};

		RpcProcessor processor = new RpcProcessor();
		processor.addModule( "t1", t1 );
		ServiceConfig sc = new ServiceConfig();
		sc.setMessageProcessor( processor );
		sc.setServerPort( 8099 );
		Service srv = new Service( sc );
		srv.start();

		new Thread(){
			public void run(){
				try{
					cdl.await();
					System.out.println( "all win!" );
					srv.close();
				}catch( InterruptedException | IOException e ){
					e.printStackTrace();
				}
			};
		}.start();

	}

	public static void runClient(){
		try{
			Broker broker = new ZbusBroker( "127.0.0.1:8099" );
			RpcFactory factory = new RpcFactory( broker );
			Test1 t1 = factory.getService( Test1.class );
			t1.run();

			broker.close();
		}catch( Exception e ){
			e.printStackTrace();
		}
	}

	public static void runServer1(){
		try{
			ExecutorService service = Executors.newCachedThreadPool();
			AsynchronousChannelGroup acg = AsynchronousChannelGroup.withCachedThreadPool( service, 2 );
			AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open( acg );
			assc.setOption( StandardSocketOptions.SO_REUSEADDR, true );
			assc.setOption( StandardSocketOptions.SO_RCVBUF, 16 * 1024 );
			assc.bind( new InetSocketAddress( "127.0.0.1", 8099 ), 100 );
			assc.accept( null, new CompletionHandler<AsynchronousSocketChannel, Void>(){
				@Override
				public void completed( AsynchronousSocketChannel result, Void attachment ){
					ByteBuffer byteBuffer = ByteBuffer.allocate( 32 );
					try{
						result.read( byteBuffer ).get();
					}catch( InterruptedException | ExecutionException e ){
						e.printStackTrace();
					}
					byteBuffer.flip();
					System.out.println( "rec:" + byteBuffer.get() );

					assc.accept( null, this );
				}

				@Override
				public void failed( Throwable exc, Void attachment ){
					System.out.println( "exc:" + exc );

					assc.accept( null, this );
				}
			} );

		}catch( IOException e ){
			e.printStackTrace();
		}

		System.out.println( "server start on 127.0.0.1:8099." );
	}

	public static void runClient1(){
		try{

			for( int i = 0; i < 10; i++ ){
				AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
				Future<?> result = client.connect( new InetSocketAddress( "127.0.0.1", 8099 ) );
				Object data = result.get();
				System.out.println( "data:" + data );

				ByteBuffer byteBuffer = ByteBuffer.allocate( 32 );
				byteBuffer.put( ( byte )11 );
				byteBuffer.flip();
				client.write( byteBuffer );
				client.close();
			}

		}catch( IOException | InterruptedException | ExecutionException e ){
			e.printStackTrace();
		}

	}

	public static void main( String[] args ) throws IOException, InterruptedException{

		runServer();

		runClient();

//		runServer1();
//
//		runClient1();

	}

}
