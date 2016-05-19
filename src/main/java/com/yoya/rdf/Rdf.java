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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.yoya.config.IConfig;
import com.yoya.rdf.plugin.IPlugin;

/**
 * Created by baihw on 16-4-13.
 *
 * Rdf框架主文件
 */
public class Rdf{

	/**
	 * 应用使用的编码配置关键字。
	 */
	public static final String			KEY_ENCODING	= "encoding";

	// 当前对象唯一实例
	private static final Rdf			_ME				= new Rdf();
	// 框架主全局配置对象
	private IConfig						_CONFIG			= null;

	// 插件管理容器
	private final Map<String, IPlugin>	_PLUGINS		= new ConcurrentHashMap<>();

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
	public String getEncoding(){
		return _CONFIG.get( KEY_ENCODING );
	}

	/**
	 * 获取框架全局配置属性值
	 *
	 * @param group 配置组
	 * @param key 属性名
	 * @param defValue 值为null时使用的默认值
	 * @return 属性值
	 */
	public String getConfig( String group, String key, String defValue ){
		String value = getConfig( group, key );
		return null == value ? defValue : value;
	}

	/**
	 * 获取框架全局配置属性值
	 *
	 * @param group 配置组
	 * @param key 属性名
	 * @return 属性值
	 */
	public String getConfig( String group, String key ){
		return _CONFIG.get( group, key );
	}

	/**
	 * 获取框架全局配置属性值
	 * 
	 * @param key 属性名
	 * @return 属性值
	 */
	public String getConfig( String key ){
		return _CONFIG.get( key );
	}

	/**
	 * 获取框架全局配置中指定名称配置组中的所有配置项集合。
	 *
	 * @param group 配置组
	 * @return 配置项集合
	 */
	public Map<String, String> getConfigGroup( String group ){
		return _CONFIG.getGroup( group );
	}

	/**
	 * 使用框架类加载器加载指定名称的类
	 *
	 * @param className 包含包名的完整类名称
	 * @return 如果找不到，返回null。
	 */
	public Class<?> loadClass( String className ){
		try{
			Class<?> result = Class.forName( className );
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
			Class<?> result = Class.forName( className );
			result.newInstance();
			return result;
		}catch( ClassNotFoundException | InstantiationException | IllegalAccessException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 执行初始化动作，不允许重复执行。
	 */
	public void init( IConfig config ){

		Objects.requireNonNull( config );

		if( null != _CONFIG )
			throw new RuntimeException( "已经初始化过，请不要重复执行！" );

		// 初始化config对象
		_CONFIG = config;
	}

	/**
	 * 插件注册
	 * 
	 * @param plugin 插件对象
	 */
	public void pluginRegister( IPlugin plugin ){
		_PLUGINS.put( plugin.toString(), plugin );
	}

	/**
	 * 调用框架关闭动作，销毁资源占用。
	 */
	public void stop(){
		_PLUGINS.values().forEach( ( plugin ) -> {
			plugin.stop();
		} );
	}

}
