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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.IRequestHandler;
import com.yoya.rdf.router.IResponse;
import com.yoya.rdf.router.IRouter;
import com.yoya.rdf.router.filter.IFilterChain;
import com.yoya.rdf.router.filter.IFilterConfig;
import com.yoya.rdf.router.filter.IRequestFilter;
import com.yoya.rdf.router.filter.impl.SimpleFilterChain;
import com.yoya.rdf.router.filter.impl.SimpleFilterConfig;

/**
 * Created by baihw on 16-4-15.
 *
 * 框架内置的一个简单路由实现
 */
public class SimpleRouter implements IRouter{

	/**
	 * 过滤器配置关键字：拦截路径。
	 */
	public static final String						KEY_URL			= "url";

	/**
	 * 过滤器配置关键字：类名称
	 */
	public static final String						KEY_CLASS		= "class";

	// 日志处理对象
	private static final ILog						_LOG			= LogManager.getLog( SimpleRouter.class );

	// filter存放容器。
	private final Map<String, IRequestFilter>		_FILTERS		= new LinkedHashMap<>( 10, 1.0f );

	// 所有拦截地址对应的filter名称集合。
	private final Map<String, Set<String>>			_FILTER_GROUPS	= new LinkedHashMap<>( 10, 1.0f );

	// 请求处理器与服务地址映射容器。
	private final Map<String, RequestHandlerProxy>	_HANDLERS		= new HashMap<>( 10, 1.0f );

	// 工作基础目录：要扫描自动注册的包路径。
	private String									_workBase;

	@Override
	public void configWrokBase( String workBase ){
		Objects.requireNonNull( workBase, "workBase can not be null!" );
		this._workBase = workBase;

		// 扫描注册工作目录下的处理器。
		scanHandlers();
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
					if( RequestHandlerProxy.KEY_METHODNAMES.equals( startName ) )
						return true;
					RequestHandlerProxy handlerProxy = _HANDLERS.get( actionName );
					return null != handlerProxy && handlerProxy.hasMethod( startName );
				}
			} );

			String action = urlItem.getAction();
			if( !_HANDLERS.containsKey( action ) ){
				response.setDataByJsonCMD( IResponse.CODE_NOT_FOUND, String.format( "not found action: %s", action ) );
				return;
			}

			// 执行业务处理逻辑
			RequestHandlerProxy handlerProxy = _HANDLERS.get( action );
//          // TODO: url参数设置进请求对象。
//		    urlItem.getParams() ;
			handlerProxy.doMethod( urlItem.getStart(), request, response );

		}catch( Exception e ){
			StringWriter errorSW = new StringWriter();
			e.printStackTrace( new PrintWriter( errorSW ) );
			response.setDataByJsonCMD( IResponse.CODE_INTERNAL_ERROR, e.getMessage(), errorSW.toString() );
		}
	}

	/**
	 * 增加一个过滤器
	 *
	 * @param id 过滤器标识
	 * @param filter 过滤器实例
	 */
	public void addFilter( String id, IRequestFilter filter ){
		if( null == id || 0 == ( id = id.trim() ).length() )
			throw new NullPointerException( "filter name must not null!" );
		if( null == filter )
			throw new NullPointerException( "filter must not null!" );
		this._FILTERS.put( id, filter );
	}

	/**
	 * 为指定的访问路径映射对应的过滤器
	 *
	 * @param url 访问路径
	 * @param id 过滤器标识
	 */
	public void mappingFilter( String url, String id ){
		if( null == url || "".equals( url = url.trim() ) ){ throw new RuntimeException( "url must not empty!" ); }
		if( null == id || !_FILTERS.containsKey( id ) ){ throw new IllegalStateException( "filter '" + id + "' not exists!" ); }

		url = url.replaceAll( "\\*", "\\+.*" );

		Set<String> groups = _FILTER_GROUPS.get( url );
		if( null == groups ){
			groups = new LinkedHashSet<String>();
			groups.add( id );
			_FILTER_GROUPS.put( url, groups );
		}else{
			groups.add( id );
		}

	}

	/**
	 * 增加一个指定访问路径拦截映射的过滤器对象
	 *
	 * @param url 访问路径
	 * @param filter 过滤器对象
	 */
	public void addMappingFilter( String url, IRequestFilter filter ){
		final String fid = filter.getClass().getName();
		addFilter( fid, filter );
		mappingFilter( url, fid );
	}

	public void addMappingFilter( String id, Map<String, String> configMap ){
		if( null == id || "".equals( id = id.trim() ) || null == configMap || configMap.size() < 2 ){
			_LOG.error( "无效的过滤器信息,id:" + id + ", configMap:" + configMap );
			return;
		}

		if( _FILTERS.containsKey( id ) ){
			_LOG.error( "重复的过滤器标识:" + id );
			return;
		}

		String filterCla = configMap.get( KEY_CLASS );
		String filterUrl = configMap.get( KEY_URL );
		if( null == filterCla || "".equals( filterCla = filterCla.trim() ) || null == filterUrl || "".equals( filterUrl = filterUrl.trim() ) ){
			_LOG.error( "过滤器class, url属性不能为空,id:" + id );
			return;
		}

		// 创建实例
		IRequestFilter filter = null;
		try{
			Class<?> clazz = Class.forName( filterCla );
			if( IRequestFilter.class.isAssignableFrom( clazz ) ){
				filter = ( IRequestFilter )clazz.newInstance();
			}else{
				_LOG.error( clazz.getName().concat( "必须实现IRequestFilter接口!" ) );
			}
		}catch( ClassNotFoundException | InstantiationException | IllegalAccessException e ){
			throw new RuntimeException( e );
		}

		if( null == filter ){
			_LOG.error( "无法创建过滤器实例:" + filterCla );
			return;
		}

		// 调用filter的初始化方法。
		IFilterConfig filterConfig = new SimpleFilterConfig( id, configMap );
		filter.init( filterConfig );

		_FILTERS.put( id, filter );

		// 映射匹配规则。
		mappingFilter( filterUrl, id );
	}

	private IRequestFilter[] matchFilters( String url ){
		if( null == url || 0 == ( url = url.trim() ).length() || _FILTER_GROUPS.isEmpty() )
			return null;
		List<IRequestFilter> filters = new ArrayList<IRequestFilter>();
		Iterator<String> urlKeys = _FILTER_GROUPS.keySet().iterator();
		while( urlKeys.hasNext() ){
			final String urlKey = urlKeys.next();
			if( url.matches( urlKey ) ){
				Set<String> filterNames = _FILTER_GROUPS.get( urlKey );
				for( String filterName : filterNames )
					filters.add( _FILTERS.get( filterName ) );
			}
		}
		return filters.isEmpty() ? null : filters.toArray( new IRequestFilter[0] );
	}

	public void destroy(){
		_FILTERS.values().forEach( ( filter ) -> {
			filter.destroy();
		} );
		this._FILTERS.clear();
		this._FILTER_GROUPS.clear();
		this._HANDLERS.clear();
	}

	/**
	 * 扫描并注册所有的请求处理器。
	 */
	private void scanHandlers(){
		try{
			ImmutableSet<ClassPath.ClassInfo> classInfos = ClassPath.from( Thread.currentThread().getContextClassLoader() ).getTopLevelClassesRecursive( this._workBase );
			for( ClassPath.ClassInfo cInfo : classInfos ){
				// 将请求处理器名称解析为url注册点名称。
				String regUrl = cInfo.getName().substring( this._workBase.length() ).replace( ".", "/" );
				Class<?> clazz = cInfo.load();
				if( IRequestHandler.class.isAssignableFrom( clazz ) ){
					RequestHandlerProxy handlerProxy = new RequestHandlerProxy( ( Class<IRequestHandler> )clazz );
					_HANDLERS.put( regUrl, handlerProxy );
					_LOG.debug( String.format( "regUrl:%s, handler:%s", regUrl, handlerProxy ) );
				}
			}
		}catch( IOException e ){
			throw new RuntimeException( e );
		}

	}

}
