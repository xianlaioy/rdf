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
package com.yoya.rdf.plugin;

/**
 * Created by baihw on 16-6-29.
 *
 * Rdf框架插件加载器规范接口。
 */
public interface IPluginLoader{

	/**
	 * 此组件使用的配置组名称。
	 */
	String	CONFIG_GROUP		= "plugin";

	/**
	 * 此组件使用的实现者名称配置关键字。
	 */
	String	KEY_IMPL			= "loader";

	/**
	 * 默认的插件目录名
	 */
	String	DEF_WORKBASE_NAME	= "plugins";

	/**
	 * 获取指定名称的实现对象共享实例（单例类）。
	 * 
	 * @param pluginInterface 插件接口类
	 * @param implName 实现名称，如果为null或者为空，则视为默认名称。
	 * @return 实现对象实例。
	 */
	<T> T getPluginImpl( Class<?> pluginInterface, String implName );

	/**
	 * 获取默认名称的实现对象共享实例（单例类）。
	 * 
	 * @param pluginInterface 插件接口类
	 * @return 默认名称实现对象实例。
	 */
	<T> T getPluginImpl( Class<?> pluginInterface );

	/**
	 * 创建指定名称的实现对象实例。
	 * 
	 * @param pluginInterface 插件接口类
	 * @param implName 实现名称，如果为null或者为空，则视为默认名称。
	 * @return 实现对象实例。
	 */
	<T> T createPluginImpl( Class<?> pluginInterface, String implName );

	/**
	 * 获取默认名称的实现对象实例。
	 * 
	 * @param pluginInterface 插件接口类
	 * @return 默认名称实现对象实例。
	 */
	<T> T createPluginImpl( Class<?> pluginInterface );

	/**
	 * 清理资源的方法。
	 */
	void destroy();

} // end class
