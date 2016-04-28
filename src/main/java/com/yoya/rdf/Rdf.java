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

import com.yoya.config.IConfig;
import com.yoya.config.impl.SimpleConfig;
import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.IResponse;
import com.yoya.rdf.router.IRouter;
import com.yoya.rdf.router.filter.IRequestFilter;
import com.yoya.rdf.router.impl.SimpleRouter;

import java.util.Objects;

/**
 * Created by baihw on 16-4-13.
 *
 * Rdf框架主文件
 */
public class Rdf{

	// 当前对象唯一实例
	private static final Rdf	_ME		= new Rdf();
	// 框架主全局配置对象
	private IConfig				_CONFIG	= null;
	// 框架全局路由对象
	private IRouter				_ROUTER	= null;

	/**
	 * 私有构造函数
	 */
	private Rdf(){
	}

	/**
	 * @return 当前对象唯一实例。
	 */
	public static Rdf me(){
		return _ME;
	}

	/**
	 * @return 框架使用的字符集编码
	 */
	public static String getEncoding(){
		return "UTF-8";
	}

	/**
	 * 获取框架全局配置属性值
	 * 
	 * @param key 属性名
	 * @return 属性值
	 */
	public static String getProperty( String key ){
		return _ME._CONFIG.get( key );
	}

	/**
	 * 使用框架类加载器加载指定名称的类
	 *
	 * @param className 包含包名的完整类名称
	 * @return 如果找不到，返回null。
	 */
	public Class loadClass( String className ){
		try{
			Class result = Class.forName( className );
			return result;
		}catch( ClassNotFoundException e ){
			return null;
		}
	}

	/**
	 * 使用框架类加载器查找并创建指定名称的类实例
	 *
	 * @param className 包含包名的完整类名称
	 * @return 如果找不到或创建失败，将抛出运行时异常。
	 */
	public Object newClass( String className ){
		try{
			Class result = Class.forName( className );
			result.newInstance();
			return result;
		}catch( ClassNotFoundException | InstantiationException | IllegalAccessException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 执行初始化动作，不允许重复执行。
	 */
	public void init(){
		if( null != _CONFIG )
			throw new RuntimeException( "已经初始化，请勿重复执行！" );
		// 初始化config对象
		_CONFIG = new SimpleConfig();
		_ROUTER = new SimpleRouter();
        String workBase = _CONFIG.get( "routeWorkBase" ) ;
		_ROUTER.configWrokBase( null == workBase ? "." : workBase );
	}

	/**
	 * 路由请求处理逻辑
	 * 
	 * @param request 请求对象
	 * @param response 响应对象
	 */
	public void route( IRequest request, IResponse response ){
		_ROUTER.route( request, response );
	}

	/**
	 * 增加一个指定请求路径拦截处理的过滤器映射信息。
	 * 
	 * @param url 拦截的路径
	 * @param filter 过滤器实例
	 */
	public void addFilter( String url, IRequestFilter filter ){
		Objects.requireNonNull( url );
		Objects.requireNonNull( filter );
		_ROUTER.addMappingFilter( url, filter );
	}

//	/**
//	 * 入口函数
//	 *
//	 * @param args
//	 */
//	public static void main( String[] args ){
//
//		String AK = System.getProperty( "rdf.AK" );
//		String SK = System.getProperty( "rdf.SK" );
//
//	}

}
