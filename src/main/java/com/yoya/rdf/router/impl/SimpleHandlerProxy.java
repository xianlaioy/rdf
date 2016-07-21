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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.router.IHandlerProxy;
import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.IResponse;

/**
 * Created by baihw on 16-4-15.
 *
 * 请求处理器代理执行者
 */
final class SimpleHandlerProxy implements IHandlerProxy{

	// 日志处理对象
	private static final ILog			_LOG			= LogManager.getLog( SimpleHandlerProxy.class );

	// 代理的请求处理器类
	private final Class<?>				_CLA;
	// 代理的请求处理器类字符串名称
	private final String				_CLA_STRING;

	// 请求处理器导出的外部访问方法集合。
	private final Map<String, Method>	_EXPORTMETHODS	= new ConcurrentHashMap<>();

	// 代理的请求处理器类实例
	private Object						_HANDLER;

	/**
	 * 构造函数
	 *
	 * @param clazz 请求处理器实现类
	 */
	SimpleHandlerProxy( Class<?> clazz, Class<?>[] parameterTypes ){

		Objects.requireNonNull( clazz, "handler can not be null!" );

		this._CLA = clazz;
		this._CLA_STRING = clazz.getName();

		// 初始化类实例对象
		try{
			this._HANDLER = clazz.newInstance();
		}catch( InstantiationException | IllegalAccessException e ){
			throw new IllegalStateException( e );
		}

		// 导出所有可被外部访问的方法。
		exportMethod( parameterTypes );

	}

	/**
	 * 检查两组参数类型是否匹配。
	 * 
	 * @param types1
	 * @param types2
	 * @return
	 */
	private boolean checkParamterTypes( Class<?>[] types1, Class<?>[] types2 ){
		final int typesCount = null == types1 ? 0 : types1.length;
		int i = null == types2 ? 0 : types2.length;
		if( typesCount != i )
			return false;
		if( 0 == typesCount )
			return true;
		for( i = 0; i < typesCount; i++ ){
			if( types1[i] != types2[i] )
				return false;
		}
		return true;
	}

	// 分析方法集合。
	private void exportMethod( final Class<?>[] parameterTypes ){
		synchronized( _EXPORTMETHODS ){
			this._EXPORTMETHODS.clear();

			Method[] methods = this._CLA.getDeclaredMethods();
			for( Method method : methods ){
				final String methodName = method.getName();
				// 查找符合 public void xxx( IRequest, IResponse)签名[xxx不以下划线'_'开头]的方法。导出为外部可请求的方法。
				if( '_' != methodName.charAt( 0 ) && Modifier.isPublic( method.getModifiers() ) && Void.TYPE == method.getReturnType() && 0 == method.getExceptionTypes().length ){
					if( checkParamterTypes( parameterTypes, method.getParameterTypes() ) ){
						this._EXPORTMETHODS.put( method.getName(), method );
					}
				}
			}

		}
	}

	/**
	 * 是否存在指定名称的外部可访问方法
	 *
	 * @param methodName 方法名
	 * @return true / false
	 */
	public boolean hasMethod( String methodName ){
		return _EXPORTMETHODS.containsKey( methodName );
	}

	/**
	 * 获取所有导出的外部可访问方法名称。
	 *
	 * @return 方法名称集合。
	 */
	public Set<String> getMethodNames(){
		return Collections.unmodifiableSet( _EXPORTMETHODS.keySet() );
	}

	/**
	 * 执行指定的方法处理逻辑。
	 *
	 * @param methodName 方法名
	 * @param request 请求对象
	 * @param response 响应对象
	 */
	public void doMethod( String methodName, IRequest request, IResponse response ){
		if( null == methodName ){
			methodName = DEF_METHOD;
		}
		_LOG.debug( String.format( "handler: %s, method: %s", _CLA_STRING, methodName ) );
		if( KEY_METHODNAMES.equals( methodName ) ){
			// 如果是获取所有方法名的关键字，则直接返回所有方法名的json格式数据。
			response.setData( _EXPORTMETHODS.keySet() );
			return;
		}
		if( !_EXPORTMETHODS.containsKey( methodName ) ){
			// 如果是获取所有方法名的关键字，则直接返回所有方法名的json格式数据。
			response.setError( IResponse.CODE_NOT_FOUND, String.format( "not found method name: %s", methodName ) );
			return;
		}
		Method doMethod = _EXPORTMETHODS.get( methodName );
		try{
			doMethod.invoke( _HANDLER, request, response );
		}catch( IllegalAccessException | InvocationTargetException e ){
			throw new RuntimeException( e );
		}
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append( "{'class':'" ).append( _CLA.getName() );
		sb.append( "', 'methods':" ).append( Arrays.toString( _EXPORTMETHODS.keySet().toArray() ) );
		sb.append( "}" );
		return sb.toString();
	}

}
