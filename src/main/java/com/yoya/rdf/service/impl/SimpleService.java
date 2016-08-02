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
package com.yoya.rdf.service.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.yoya.net.CloseChannelException;
import com.yoya.net.rpc.IRpcServer;
import com.yoya.net.rpc.IRpcServer.IHandler;
import com.yoya.net.rpc.impl.SimpleRpcServer;
import com.yoya.rdf.Rdf;
import com.yoya.rdf.RdfUtil;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.IRequestBuilder;
import com.yoya.rdf.router.IResponse;
import com.yoya.rdf.router.IRouter;
import com.yoya.rdf.router.impl.ServiceRouter;
import com.yoya.rdf.router.impl.SimpleRequestBuilder;
import com.yoya.rdf.router.impl.SimpleResponse;
import com.yoya.rdf.service.IRegistry;
import com.yoya.rdf.service.IService;
import com.yoya.struct.ICommandResult;
import com.yoya.struct.impl.SimpleCommandResult;

/**
 * Created by baihw on 16-6-8.
 *
 * 框架内置的一个简单服务实现。
 */
public class SimpleService implements IService{

	/**
	 * 默认的服务端口号
	 */
	public static final int							DEF_PORT			= 9999;

	/**
	 * 默认的路由工作目录：rdf.me.service
	 */
	public static final String						DEF_WORKBASE		= "rdf.me.service";

	// 日志处理对象。
	private static final ILog						_LOG				= LogManager.getLog( SimpleService.class );

	// 签名关键字：ak
	private static final String						_SIGN_KEY_AK		= "_ak_";
	// 签名关键字：timestamp
	private static final String						_SIGN_KEY_TIMESTAMP	= "_timestamp_";
	// 签名关键字：sign
	private static final String						_SIGN_KEY_SIGN		= "_sign_";

	// 是否使用签名机制保证通信安全
	private final boolean							_USESIGN;

	// 客户端调用等待超时时间。
	private final int								_WAIT_TIMEOUT;

	// 是否启用对外服务
	private final boolean							_ISENABLE;
	// 服务绑定地址
	private final String							_BINDADDRESS;
	// 服务对外地址
	private final String							_EXPORTADDRESS;

	// 服务注册中心
	private final IRegistry							_REGISTRY;
	// 服务核心实现
	private final IRpcServer						_SERVER;
	// 服务请求路由
	private final IRouter<IRequest, IResponse>		_ROUTER;

	// rpc客户端容器。
	private final Map<String, SimpleServiceClient>	_CLIENTS			= new ConcurrentHashMap<>();

	/**
	 * 构造函数
	 */
	public SimpleService(){

		Map<String, String> configMap = Rdf.me().getConfigGroup( CONFIG_GROUP );
		Objects.requireNonNull( configMap, "configMap can not be null!" );

		String registry = configMap.get( "registry" );
		if( null == registry || 0 == ( registry = registry.trim() ).length() ){
			registry = null;
		}
		//动态初始化注册中心
		if( null == registry ){
			registry = "com.yoya.rdf.service.impl.NothingRegistry";
		}else if( "mysqlRegistry".equals( registry ) ){
			registry = "com.yoya.rdf.service.impl.MysqlRegistry";
		}

		try {
			Class<?> clazz = Class.forName(registry);
			Class<?>[] parameterTypes={Map.class};
			Constructor<?> constructor=clazz.getConstructor(parameterTypes); 
			
			_REGISTRY = (IRegistry) constructor.newInstance(configMap);
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException( e.getMessage() );
		} catch (SecurityException e) {
			throw new RuntimeException( e.getMessage() );
		} catch (InstantiationException e) {
			throw new RuntimeException( e.getMessage() );
		} catch (IllegalAccessException e) {
			throw new RuntimeException( e.getMessage() );
		} catch (IllegalArgumentException e) {
			throw new RuntimeException( e.getMessage() );
		} catch (NoSuchMethodException e) {
			throw new RuntimeException( e.getMessage() );
		} catch (InvocationTargetException e) {
			throw new RuntimeException( e.getMessage() );
		}
		
		String useSign = configMap.get( "useSign" );
		if( null == useSign || 0 == ( useSign = useSign.trim() ).length() ){
			this._USESIGN = true;
		}else{
			this._USESIGN = "true".equals( useSign );
		}

		String waitTimeout = configMap.get( "waitTimeout" );
		if( null == waitTimeout || 0 == ( waitTimeout = waitTimeout.trim() ).length() ){
			this._WAIT_TIMEOUT = 1000;
		}else{
			this._WAIT_TIMEOUT = Integer.parseInt( waitTimeout );
		}

		// 获取是否需要启动网络通信服务的配置选项。
		String enable = configMap.get( "enable" );
		if( null == enable || 0 == ( enable = enable.trim() ).length() ){
			this._ISENABLE = true;
		}else{
			this._ISENABLE = "true".equals( enable );
		}

		if( this._ISENABLE ){
			String bindAddress = configMap.get( "bindAddress" );
			if( null == bindAddress || 0 == ( bindAddress = bindAddress.trim() ).length() ){
				bindAddress = null;
			}
			String exportAddress = configMap.get( "exportAddress" );
			if( null == exportAddress || 0 == ( exportAddress = exportAddress.trim() ).length() ){
				exportAddress = null;
			}
			String workBase = configMap.get( "workBase" );
			if( null == workBase || 0 == ( workBase = workBase.trim() ).length() ){
				workBase = DEF_WORKBASE;
			}

			InetSocketAddress socketAddress = RdfUtil.parseAddress( bindAddress, DEF_PORT );
			this._BINDADDRESS = RdfUtil.socketAddressToString( socketAddress );
			this._EXPORTADDRESS = null == exportAddress ? this._BINDADDRESS : exportAddress;

			this._ROUTER = new ServiceRouter();
			this._ROUTER.configWrokBase( workBase );

			_SERVER = new SimpleRpcServer( socketAddress.getHostString(), socketAddress.getPort(), new IHandler(){
				@Override
				public ICommandResult onMessage( IRequest request ){

					if( _USESIGN ){
						// 如果启用签名，此处自动做签名检测。无须应用关心。
						TreeMap<String, String> params = new TreeMap<>( request.getParameters() );
						String ak = params.get( _SIGN_KEY_AK );
						String sign = params.get( _SIGN_KEY_SIGN );
						boolean signOk = false;
						if( null != ak && null != sign ){
							String signText = buildSignText( params );
							signOk = _REGISTRY.checkSign( ak, signText, sign );
						}

						if( !signOk ){
							_LOG.warn( "invalid sign:" + sign );
							// 签名检验失败，视为非法连接，关闭客户端连接。
							throw new CloseChannelException( "签名检验出错!" );
						}
					}

					// 路由请求，获取执行结果。
					ICommandResult result = routeRequest( request );
					return result;
				}
			} );
		}else{
			this._BINDADDRESS = null;
			this._EXPORTADDRESS = null;
			this._ROUTER = null;
			this._SERVER = null;
		}

	}

	@Override
	public void start(){
		if( this._ISENABLE ){
			_SERVER.start();
			_LOG.info( "service bindAddress:".concat( this._BINDADDRESS ).concat( ", exportAddress:" ).concat( this._EXPORTADDRESS ) );

			// 向注册中心注册此服务
			InetSocketAddress socketAddress = RdfUtil.parseAddress( this._EXPORTADDRESS, DEF_PORT );
			_REGISTRY.register( Rdf.me().getAK(), socketAddress );
		}else{
			_LOG.warn( "需要对外提供服务请配置enable选项为true." );
		}
	}

	@Override
	public void stop(){
		try{
			this._SERVER.close();
		}catch( IOException e ){
			_LOG.error( e.getMessage() );
		}

		// 关闭所以客户端连接。
		this._CLIENTS.values().forEach( ( client ) -> {
			client.close();
		} );

//		this._REGISTRY.unRegister( Rdf.me().getAK(), parseAddress( this._exportAddress ) );
	}

	@Override
	public ICommandResult call( String serviceId, IRequest request ){
		Objects.requireNonNull( serviceId, "serviceId can not be null!" );
		Objects.requireNonNull( request, "request can not be null!" );

		// 如果是项目调用自己提供的服务，则不走网络，不使用签名机制，直接路由请求。
		if( null != _ROUTER && serviceId.equals( Rdf.me().getAK() ) ){ return routeRequest( request ); }

		SimpleServiceClient client = _CLIENTS.get( serviceId );
		if( null == client ){
			InetSocketAddress socketAddress = _REGISTRY.getAdress( serviceId );
			if( null == socketAddress ){ throw new RuntimeException( "unknow serviceId:".concat( serviceId ) ); }
			synchronized( _CLIENTS ){
				client = _CLIENTS.get( serviceId );
				if( null == client ){
					_CLIENTS.putIfAbsent( serviceId, new SimpleServiceClient( socketAddress, this._WAIT_TIMEOUT ) );
				}
				client = _CLIENTS.get( serviceId );
			}
		}

		if( _USESIGN ){

			final String AK = Rdf.me().getAK();
			final String TIMESTAMP = String.valueOf( System.currentTimeMillis() );
			TreeMap<String, String> params = new TreeMap<>( request.getParameters() );
			params.put( _SIGN_KEY_AK, AK );
			params.put( _SIGN_KEY_TIMESTAMP, TIMESTAMP );
			final String SIGN = signText( buildSignText( params ) );

			IRequestBuilder requestBuilder = SimpleRequestBuilder.copy( request );
			requestBuilder.addParameter( _SIGN_KEY_AK, AK );
			requestBuilder.addParameter( _SIGN_KEY_TIMESTAMP, TIMESTAMP );
			requestBuilder.addParameter( _SIGN_KEY_SIGN, SIGN );
			IRequest sendRequest = requestBuilder.build();
			return client.call( sendRequest );
		}

		return client.call( request );
	}

	// 路由服务请求，获取响应结果。
	private ICommandResult routeRequest( IRequest request ){
		IResponse response = new SimpleResponse();
		// 调用服务路由请求处理逻辑。
		_ROUTER.route( request, response );

		ICommandResult result = new SimpleCommandResult( response.getStatus(), response.getError(), response.getData() );
		return result;
	}

	// 根据参数信息构建签名字符串。
	private String buildSignText( TreeMap<String, String> sortData ){
		if( null == sortData || sortData.isEmpty() )
			return null;
		String signText = sortData.remove( _SIGN_KEY_SIGN );

		StringBuilder sb = new StringBuilder();
		sortData.forEach( ( key, value ) -> {
			sb.append( key ).append( '=' ).append( value ).append( '&' );
		} );
		if( 0 != sb.length() && '&' == sb.charAt( sb.length() - 1 ) ){
			sb.deleteCharAt( sb.length() - 1 );
		}
		if( null != signText )
			sortData.put( _SIGN_KEY_SIGN, signText );
		return sb.toString();
	}

	// 对指定文本计算签名
	private String signText( String text ){
		return _REGISTRY.sign( text );
	}

} // end class
