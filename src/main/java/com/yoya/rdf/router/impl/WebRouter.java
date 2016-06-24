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

package com.yoya.rdf.router.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.router.AbstractRouter;
import com.yoya.rdf.router.IHandlerProxy;
import com.yoya.rdf.router.IHttpRequest;
import com.yoya.rdf.router.IHttpRequestHandler;
import com.yoya.rdf.router.IHttpResponse;
import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.IResponse;
import com.yoya.rdf.router.IRouter;
import com.yoya.rdf.router.filter.IFilterChain;
import com.yoya.rdf.router.filter.IRequestFilter;
import com.yoya.rdf.router.filter.impl.SimpleFilterChain;

/**
 * Created by baihw on 16-4-15.
 *
 * 框架内置的一个简单路由实现
 */
public class WebRouter extends AbstractRouter implements IRouter{

	/**
	 * 默认的路由逻辑实现扫描包名。
	 */
	public static final String		DEF_WORKBASE	= "rdf.me.handler";

	// 日志处理对象
	private static final ILog		_LOG			= LogManager.getLog( WebRouter.class );

	// 请求处理器参数类型列表。
	private static final Class<?>[]	_PARAMETERTYPES	= new Class<?>[]{ IHttpRequest.class, IHttpResponse.class };

	/**
	 * 构造函数。
	 */
	public WebRouter( String workBase ){
		this._HANDLER_IFACE = IHttpRequestHandler.class;
		if( null == workBase || 0 == ( workBase = workBase.trim() ).length() )
			workBase = DEF_WORKBASE;
		configWrokBase( workBase );
	}

	@Override
	public void route( IRequest request, IResponse response ){
		String reqPath = request.getPath();

		try{

			// 先执行请求路径上的匹配过滤器对请求进行过滤。
			IRequestFilter[] matchFilters = matchFilters( reqPath );
			if( null != matchFilters && matchFilters.length > 0 ){
				IFilterChain afChain = new SimpleFilterChain( matchFilters );
				boolean isEnd = afChain.doFilter( request, response );
				if( isEnd ){
					// 如果过滤器中响应了数据，则直接返回，不再进行后续处理。
					return;
				}
			}

			// 解析url路径获取请求路由元素。
			UrlUtil.UrlItem urlItem = UrlUtil.parseUrl( reqPath, new Function<String, Boolean>(){
				@Override
				public Boolean apply( String actionName ){
					return _HANDLERS.containsKey( actionName );
				}
			}, new BiFunction<String, String, Boolean>(){
				@Override
				public Boolean apply( String actionName, String startName ){
					// 保留的方法关键字。
					if( SimpleHandlerProxy.KEY_METHODNAMES.equals( startName ) )
						return true;
					IHandlerProxy handlerProxy = _HANDLERS.get( actionName );
					return null != handlerProxy && handlerProxy.hasMethod( startName );
				}
			} );

			String action = urlItem.getAction();
			if( !_HANDLERS.containsKey( action ) ){
				( ( IHttpResponse )response ).setDataByJsonCMD( IResponse.CODE_NOT_FOUND, String.format( "not found action: %s", action ) );
				return;
			}

			// 执行业务处理逻辑
			IHandlerProxy handlerProxy = _HANDLERS.get( action );
//          // TODO: url参数设置进请求对象。
//		    urlItem.getParams() ;
			handlerProxy.doMethod( urlItem.getStart(), request, response );

		}catch( Exception e ){
			StringWriter errorSW = new StringWriter();
			e.printStackTrace( new PrintWriter( errorSW ) );
			String errorMsg = errorSW.toString();
			_LOG.error( errorMsg );
			( ( IHttpResponse )response ).setDataByJsonCMD( IResponse.CODE_INTERNAL_ERROR, e.getMessage(), errorMsg );
		}
	}

	@Override
	protected IHandlerProxy createProxy( Class<?> clazz ){
		return new SimpleHandlerProxy( clazz, _PARAMETERTYPES );
	}

}
