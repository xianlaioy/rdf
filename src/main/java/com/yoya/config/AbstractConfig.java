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

package com.yoya.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by baihw on 16-4-28.
 *
 * 配置对象抽象基类。
 */
public abstract class AbstractConfig implements IConfig{

	/**
	 * 配置数据存储容器。
	 */
	protected Map<String, Map<String, String>> _data = new LinkedHashMap<>();

	/**
	 * 增加配置项数据，非线程安全，如果需要应用在多线程环境下，请重写次方法。
	 * 
	 * @param group 配置组名称
	 * @param key 配置项名称
	 * @param value 配置值
	 */
	protected void putValue( String group, String key, String value ){
		Map<String, String> configMap = _data.get( group );
		if( null == configMap ){
			configMap = new LinkedHashMap<>();
			_data.put( group, configMap );
		}
		configMap.put( key, value );
	}

	@Override
	public String get( String key ){
		return get( DEF_GROUP_NAME, key );
	}

	@Override
	public String get( String group, String key ){
		Map<String, String> configMap = _data.get( group );
		if( null == configMap )
			return null;
		return configMap.get( key );
	}

	@Override
	public Map<String, String> getGroup( String group ){
		Map<String, String> configMap = _data.get( group );
		if( null == configMap )
			return null;
		return Collections.unmodifiableMap( configMap );
	}

	@Override
	public void watch( String group, Consumer<Map<String, String>> consumer ){
		throw new UnsupportedOperationException( "unsupported operation." );
	}

	@Override
	public String toString(){
		return _data.toString();
	}

	/**
	 * 初始化逻辑.子类可以重写此实现,用来实现不同类型的初始化数据存储对象,及默认值数据创建.
	 */
	protected void init(){
		// 默认配置数据：全局配置。
		Map<String, String> def_global = new LinkedHashMap<>();
		// "应用编码"
		def_global.put( "encoding", "UTF-8" );
		// "应用工作主目录"
		def_global.put( "homeDir", null );
		this._data.put( "global", def_global );

		// 默认配置数据：web服务
		Map<String, String> def_web = new LinkedHashMap<>();
		// "web路由管理器进行请求处理方法扫描的工作路径，通常为业务处理逻辑文件所在根路径。"
		def_web.put( "workBase", "rdf.me.handler" );
		// "web路由管理器忽略不处理的请求路径正则表达式。"
		def_web.put( "ignoreUrl", ".+(?i)\\.(html|css|js|json|ico|png|gif|woff|map)$" );
		// "过滤器名称列表，名称唯一，不可重复，对相同url进行拦截的过滤器执行顺序以列表中的排列顺序为准。"
		def_web.put( "filterNames", "loginFilter" );
		// "指定过滤器的实现类完全限定类名，需要实现IRequestFilter接口。"
		def_web.put( "loginFilter.class", "com.yoya.rdf.router.filter.impl.LoginFilter" );
		// "指定过滤器拦截的url，如果没有指定拦截的url，则拦截器会被初始化但是无法处理请求。"
		def_web.put( "loginFilter.url", "/*" );
		// "指定过滤器拦截的自定义参数：忽略登陆检查的路径。"
		def_web.put( "loginFilter.ignore", "/login" );

		this._data.put( "web", def_web );

		// 默认配置数据：session
		Map<String, String> def_session = new LinkedHashMap<>();
		// "会话管理器使用的实现名称。"
		def_session.put( "impl", null );
		// "会话最大不活动时间，超过此时间会话将失效。（单位：分钟）"
		def_session.put( "timeout", "45" );
		// "会话域，需要支持多个应用共享登陆状态时将此值设为主域。（如：www.xxx.com）"
		def_session.put( "domain", null );
		this._data.put( "session", def_session );

		// 默认配置数据：application
		Map<String, String> def_application = new LinkedHashMap<>();
		// "应用全局共享数据管理器使用的实现名称。"
		def_application.put( "impl", null );
		this._data.put( "application", def_application );

		// 默认配置数据：服务调用。
		Map<String, String> def_service = new LinkedHashMap<>();
		// "服务提供及调用管理器使用的实现名称。"
		def_service.put( "impl", "simple" );
		// "服务注册中心使用的实现名称。默认为系统提供的nothing实现"
		def_service.put( "registry", "" );
		// "是否在通信过程中使用签名机制。默认为true，如果项目处于足够安全的可信任环境，可以设置为false。"
		def_service.put( "useSign", "true" );
		// "服务调用客户端等待超时时间，单位：毫秒。默认为1分钟(60000)。"
		def_service.put( "waitTimeout", "60000" );
		// "是否开启外部服务。如果只是调用其它服务而不提供服务，可设为false。"
		def_service.put( "enable", "true" );
		// "服务绑定的主机地址及端口。默认为0.0.0.0:9999。"
		def_service.put( "bindAddress", "0.0.0.0:9999" );
		// "服务导出地址。默认为检测到绑定成功的主机地址及端口。当使用外部的负载均衡器时，应该配置为负载均衡器地址。"
		def_service.put( "exportAddress", "" );
		// "服务路由进行请求处理方法扫描的工作路径，通常为服务处理逻辑文件所在根路径。"
		def_service.put( "workBase", "rdf.me.service" );
		this._data.put( "service", def_service );

		// 默认配置数据：plugin
		Map<String, String> def_plugin = new LinkedHashMap<>();
		// "插件加载器使用的实现名称，默认为框架提供的"simple"实现。"
		def_plugin.put( "loader", null );
		// "插件加载器工作目录，通常不指定则使用应用主目录下的plugins目录，最终默认值参考具体的加载器实现文档。"
		def_plugin.put( "loader.workbase", null );
		// "插件加载器请求的网络地址，默认值参考具体的加载器实现文档。"
		def_plugin.put( "loader.url", null );
		// "插件加载器发请求时使用的的账号。"
		def_plugin.put( "loader.user", null );
		// "插件加载器发请求时使用的的密码。"
		def_plugin.put( "loader.password", null );
		// "插件自动更新功能是否开启，默认开启。"
		def_plugin.put( "autoUpdate", "true" );
		// "使用到的插件名称列表，多个以逗号隔开。"
		def_plugin.put( "names", "p1,p2" );
		// "指定名称为p1的插件门面接口名称。"
		def_plugin.put( "p1.interface", null );
		// "插件实现者列表，键值对形式对应实现者名称及实现类，多个以逗号隔开。如果有名称为default的实现视为默认实现，没有则以第1个出现的实现作为默认实现。"
		def_plugin.put( "p1.impls", null );
		this._data.put( "plugin", def_plugin );
	}

} // end class
