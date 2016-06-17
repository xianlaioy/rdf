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
package com.yoya.rdf.router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.router.filter.IFilterConfig;
import com.yoya.rdf.router.filter.IRequestFilter;
import com.yoya.rdf.router.filter.impl.SimpleFilterConfig;

/**
 * Created by baihw on 16-6-11.
 *
 * 统一路由对象实现基类。
 */
public abstract class AbstractRouter implements IRouter{

	/**
	 * 工作目录配置项名称。
	 */
	public static final String					CNF_WORK_BASE	= "workBase";

	/**
	 * 过滤器配置关键字：拦截路径。
	 */
	public static final String					KEY_URL			= "url";

	/**
	 * 过滤器配置关键字：类名称
	 */
	public static final String					KEY_CLASS		= "class";

	// 日志处理对象
	private static final ILog					_LOG			= LogManager.getLog( AbstractRouter.class );

	// filter存放容器。
	protected final Map<String, IRequestFilter>	_FILTERS		= new LinkedHashMap<>( 10, 1.0f );

	// 所有拦截地址对应的filter名称集合。
	protected final Map<String, Set<String>>	_FILTER_GROUPS	= new LinkedHashMap<>( 10, 1.0f );

	// 请求处理器与服务地址映射容器。
	protected final Map<String, IHandlerProxy>	_HANDLERS		= new HashMap<>( 10, 1.0f );

	// 工作基础目录：要扫描自动注册的包路径。
	protected String							_workBase;

	// 处理器类型
	protected Class<?>							_HANDLER_IFACE;

	@Override
	public void configWrokBase( String workBase ){
		Objects.requireNonNull( workBase, "workBase can not be null!" );
		this._workBase = workBase;

		_LOG.info( "workBase: ".concat( workBase ) );
		// 扫描注册工作目录下的处理器。
		scanHandlers();
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

	protected IRequestFilter[] matchFilters( String url ){
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
	protected void scanHandlers(){
		try{
			ImmutableSet<ClassPath.ClassInfo> classInfos = ClassPath.from( Thread.currentThread().getContextClassLoader() ).getTopLevelClassesRecursive( this._workBase );
			for( ClassPath.ClassInfo cInfo : classInfos ){
				// 将请求处理器名称解析为url注册点名称。
				String regUrl = cInfo.getName().substring( this._workBase.length() ).replace( ".", "/" );
				Class<?> clazz = cInfo.load();
				if( _HANDLER_IFACE.isAssignableFrom( clazz ) ){
					IHandlerProxy handlerProxy = createProxy( clazz );
					_HANDLERS.put( regUrl, handlerProxy );
					_LOG.debug( String.format( "regUrl:%s, handler:%s", regUrl, handlerProxy ) );
				}
			}
		}catch( IOException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 需要子类提供的请求处理器代理对象实例。
	 * 
	 * @param clazz 请求处理器
	 * @return 代理执行对象
	 */
	protected abstract IHandlerProxy createProxy( Class<?> clazz );

} // end class
