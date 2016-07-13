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
package com.yoya.rdf.plugin.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.yoya.rdf.Rdf;
import com.yoya.rdf.RdfUtil;
import com.yoya.rdf.TestRdf;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.plugin.IPluginLoader;
import com.yoya.rdf.plugin.ISMS;
import com.yoya.rdf.plugin.PluginLoader;

/**
 * Created by baihw on 16-6-29.
 * 
 * <pre>
 *
 * 框架插件加载器的一种简单实现。
 * 
 * 1.根据插件门面接口名获取插件的限定目录空间名。
 * 2.检查本地工作目录下是否已经存在插件实现包。
 * 3.如果存在，检查是否与线上版本一致。（当前版本插件实现不允许同时出现多个不兼容版本。）
 * 4.如果网络可达并可检查出与线上版本不一致，则从线上下载实现包及签名文件到当前目录。
 * 5.删除backup目录下的旧备份文件，将当前版本实现包及签名文件备份到backup目录。
 * 6.删除dist目录下的旧文件，解压最新版实现包到dist目录。
 * 7.创建插件类加载器。从插件的本地dist目录加载实现包。
 * 8.如果本地工作目录不存在，则尝试直接从线程类加载器加载。
 * 
 * 假设一个插件的门面接口类名为:com.rdf.IHelloPlugin,则插件的目录结构如下：
 * |-plugin
 *      |- com.rdf.IHelloPlugin
 *                 |- dist.zip
 *                 |- dist.zip.md5
 *                 |- backup
 *                      |- dist.zip
 *                      |- dist.zip.md5
 *                 |- dist
 *                      |- xxxxxxxx.jar
 *                      |- lib
 *                      	|- xxxxxxxx.jar
 *                      	|- xxxxxxxx.jar
 *       |- com.xxx.xxxxPlugin
 *       .........................
 * 
 * </pre>
 */
public class SimplePluginLoader implements IPluginLoader{

	// 日志处理对象。
	private static final ILog						_LOG				= LogManager.getLog( SimplePluginLoader.class );

//	public static final MediaType					TYPE_JSON		= MediaType.parse( "application/json; charset=utf-8" );

	/**
	 * 配置项：加载器请求地址。
	 */
	public static final String						KEY_LOADER_URL		= "loader.url";

	/**
	 * 配置项：插件加载器工作目录。
	 */
	public static final String						KEY_LOADER_WORKBASE	= "loader.workbase";

	/**
	 * 配置项：是否启用自动更新功能。
	 */
	public static final String						KEY_AUTO_UPDATE		= "autoUpdate";

	/**
	 * 配置项：启用的插件名称列表。
	 */
	public static final String						KEY_NAMES			= "names";

	/**
	 * 具体的插件配置项：接口类名称。
	 */
	public static final String						ITEMKEY_INTERFACE	= "interface";

	/**
	 * 具体的插件配置项：键值对形式的实现者名称及实现类列表，多个以逗号隔开。
	 */
	public static final String						ITEMKEY_IMPLS		= "impls";

	/**
	 * 默认的加载器请求地址。
	 */
	public static final String						DEF_LOADER_URL		= "http://res.acct8.com/pluginStore/";

	// 当前加载器请求地址。
	private final String							_LOADER_URL;
	// 当前加载器工作目录地址。
	private final String							_WORK_BASE;
	// 当前加载器工作目录是否可写。
	private final boolean							_WORK_BASE_CAN_WRITE;
	// 是否自动检测升级
	private final boolean							_AUTO_UPDATE;

	// 插件加载器集合。
	private final Map<String, PluginClassLoader>	_pluginImpls		= new HashMap<>();

	public SimplePluginLoader(){
		// 拷贝插件配置信息数据
		Map<String, String> configMap = new LinkedHashMap<>( Rdf.me().getConfigGroup( CONFIG_GROUP ) );

		// 设置工作目录
		String loaderWorkbase = RdfUtil.trimEmptyToNull( configMap.get( KEY_LOADER_WORKBASE ) );
		if( null == loaderWorkbase ){
			this._WORK_BASE = Rdf.me().getHomeDir().concat( "/" ).concat( DEF_WORKBASE_NAME ).concat( "/" );
		}else{
			this._WORK_BASE = loaderWorkbase.endsWith( "/" ) ? loaderWorkbase : loaderWorkbase.concat( "/" );
		}
		// 检查工具目录是否存在，如果不存在，则创建之。
		File workBase = new File( this._WORK_BASE );
		if( !workBase.exists() ){
			if( !workBase.canWrite() ){ throw new IllegalStateException( this._WORK_BASE.concat( " can not be write!" ) ); }
			workBase.mkdirs();
		}
		// 标记工作目录是否可写，如果不可写后续的检查插件更新下载等逻辑将不再执行。
		_WORK_BASE_CAN_WRITE = workBase.canWrite();

		// 设置请求路径
		String loaderUrl = RdfUtil.trimEmptyToNull( configMap.get( KEY_LOADER_URL ) );
		if( null == loaderUrl ){
			this._LOADER_URL = DEF_LOADER_URL;
		}else{
			this._LOADER_URL = loaderUrl.endsWith( "/" ) ? loaderUrl : loaderUrl.concat( "/" );
		}

//		String loaderUser = RdfUtil.trimEmptyToNull( configMap.get( "loader.user" ) );
//		String loaderPassword = RdfUtil.trimEmptyToNull( configMap.get( "loader.password" ) );

		// 检查是否需要开启自动升级功能。
		String autoUpdateValue = RdfUtil.trimEmptyToNull( configMap.get( KEY_AUTO_UPDATE ) );
		boolean autoUpate = "true".equals( autoUpdateValue );
		this._AUTO_UPDATE = autoUpate && _WORK_BASE_CAN_WRITE;

		// 设置启用的插件列表配置信息
		String nameString = RdfUtil.trimEmptyToNull( configMap.get( KEY_NAMES ) );
		if( null == nameString )
			return;
		String[] names = nameString.split( "," );
		for( String name : names ){
			if( null == name || 0 == ( name = name.trim() ).length() )
				continue;
			Map<String, String> nameConfig = RdfUtil.removeSubMap( configMap, name.concat( "." ) );
			String interfaceName = nameConfig.remove( ITEMKEY_INTERFACE );
			if( null == interfaceName || 0 == ( interfaceName = interfaceName.trim() ).length() ){
				continue;
			}

			// 实现者列表数据集合
			String implsValue = nameConfig.remove( ITEMKEY_IMPLS );
			Map<String, String> impls = parseImpls( implsValue );

			// 创建插件加载器，如果目录不可写，则将远程加载地址设为null，禁用自动检测更新功能。
			PluginClassLoader pcl = new PluginClassLoader( interfaceName, name, impls, nameConfig, workBase, this._LOADER_URL, _AUTO_UPDATE );
			_pluginImpls.put( interfaceName, pcl );
		}

		// 打印插件加载器状态信息。
		_LOG.info( String.format( "workBase:%s[canWriter:%b], loaderUrl:%s, pluginCount:%d, pluginNames:%s.", _WORK_BASE, _WORK_BASE_CAN_WRITE, _LOADER_URL, _pluginImpls.size(), _pluginImpls.keySet() ) );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getPluginImpl( Class<?> pluginInterface, String implName ){
		Objects.requireNonNull( pluginInterface );
		final String pluginId = pluginInterface.getName();
		if( !pluginInterface.isInterface() ){ throw new IllegalArgumentException( pluginId.concat( " not an interface!" ) ); }
		PluginClassLoader pcl = _pluginImpls.get( pluginId );
		if( null == pcl ){ throw new RuntimeException( pluginId.concat( " not foud!" ) ); }
		Object result = pcl.getImpl( implName );
		return null == result ? null : ( T )result;
	}

	@Override
	public <T> T getPluginImpl( Class<?> pluginInterface ){
		return getPluginImpl( pluginInterface, null );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T createPluginImpl( Class<?> pluginInterface, String implName ){
		Objects.requireNonNull( pluginInterface );
		final String pluginId = pluginInterface.getName();
		if( !pluginInterface.isInterface() ){ throw new IllegalArgumentException( pluginId.concat( " not an interface!" ) ); }
		PluginClassLoader pcl = _pluginImpls.get( pluginId );
		if( null == pcl ){ throw new RuntimeException( pluginId.concat( " not foud!" ) ); }
		Object result = pcl.createImpl( implName );
		return null == result ? null : ( T )result;
	}

	@Override
	public <T> T createPluginImpl( Class<?> pluginInterface ){
		return createPluginImpl( pluginInterface, null );
	}

	/**
	 * 需要自行注册到框架退出事件中释放资源的方法。
	 */
	public void destroy(){
		_pluginImpls.values().forEach( ( pluginClassLoader ) -> {
			pluginClassLoader.destroy();
		} );
		_pluginImpls.clear();
	}

	/**
	 * 解析以逗号隔开的多个实现者名称与实现类配置数据文本
	 * 
	 * @param implsValue 配置数据文本
	 * @return 实现者名称与实现类集合
	 */
	private Map<String, String> parseImpls( String implsValue ){
		Map<String, String> impls = new HashMap<>();
		if( null != implsValue && 0 != ( implsValue = implsValue.trim() ).length() ){
			String[] implsRows = implsValue.split( "," );
			for( String implsRow : implsRows ){
				if( null == implsRow || 0 == ( implsRow = implsRow.trim() ).length() )
					continue;
				int ndx = implsRow.indexOf( '=' );
				if( -1 == ndx || 0 == ndx || ndx == implsRow.length() - 1 ){
					_LOG.warn( "skip invalid data:".concat( implsRow ) );
					continue;
				}
				String implName = implsRow.substring( 0, ndx );
				String implClass = implsRow.substring( ndx + 1 );
				impls.put( implName, implClass );
			}
		}
		return impls;
	}

} // end class
