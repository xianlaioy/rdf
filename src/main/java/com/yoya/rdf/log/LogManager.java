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

package com.yoya.rdf.log;

/**
 * Created by baihw on 16-3-4.
 *
 * 日志管理对象
 */
public class LogManager{

	/**
	 * 可配置参数名称：日志工厂实现类
	 */
	public static final String	CNF_KEY_LOGFACTORY	= "logFactory";

	/**
	 * 日志工厂实现类在用户未配置时使用的默认实现类名称。
	 */
	public static final String	DEF_LOGFACTORY		= "com.yoya.rdf.log.impl.SimpleLogFactory";

	// 当前使用的日志工厂类实例
	private static ILogFactory	_factory;

	/**
	 * 默认初始化当前使用的日志工厂类实例为框架自带的日志工厂类实现。
	 */
	static{
//		String factoryName = Rdf.me().getProperty( CNF_KEY_LOGFACTORY );
//		if( null == factoryName || 0 == factoryName.length() ){
//			factoryName = DEF_LOGFACTORY;
//		}

		String factoryName = DEF_LOGFACTORY;
		try{
			Class lfCla = Class.forName( factoryName );
			Object lfObj = lfCla.newInstance();
			_factory = ( ILogFactory )lfObj;
		}catch( ClassNotFoundException | InstantiationException | IllegalAccessException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 设置日志对象工厂类
	 * 
	 * @param factory 日志对象工厂类
	 */
	public synchronized static void setLogFactory( ILogFactory factory ){
		_factory = factory;
	}

	/**
	 * 获取日志对象
	 * 
	 * @param category 日志类别名称
	 * @return 日志对象
	 */
	public static ILog getLog( Class category ){
		return _factory.getLog( category.getName() );
	}

	/**
	 * 获取日志对象
	 *
	 * @param category 日志类别名称
	 * @return 日志对象
	 */
	public static ILog getLog( String category ){
		return _factory.getLog( category );
	}

}
