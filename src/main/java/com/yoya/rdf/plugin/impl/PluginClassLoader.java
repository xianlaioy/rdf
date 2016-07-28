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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.yoya.rdf.Rdf;
import com.yoya.rdf.RdfUtil;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.plugin.IPlugin;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by baihw on 16-7-6.
 *
 * 插件使用的类加载器。
 */
final class PluginClassLoader{

	// 日志处理对象。
	private static final ILog			_LOG						= LogManager.getLog( PluginClassLoader.class );

	// 共享的http通信客户端。
	protected static final OkHttpClient	_HTTPCLIENT					= new OkHttpClient();

	// 发布文件名称。
	private static final String			_DIST_FILE_NAME				= "dist.zip";
	// 发布文件签名校验文件名称。
	private static final String			_DIST_FILE_CHECKSUM_SUFFIX	= ".md5";
	// 插件实现集合中用来指定默认实现的关键字。通常默认实现是排在第一个的实现，但是如果用户默认了此名称的实现，则优先以此名称实现为默认实现。
	private static final String			_KEY_DEF_IMPL_NAME			= "default";

	// 插件唯一标识。
	private final String				_PLUGIN_ID;
	// 插件名称。
	private final String				_PLUGIN_NAME;
	// 插件实现集合。
	private final Map<String, String>	_PLUGIN_IMPLS				= new LinkedHashMap<String, String>();
	// 插件配置参数集合。
	private final Map<String, String>	_PLUGIN_PARAMS				= new LinkedHashMap<String, String>();

	// 插件默认实现名称。
	private final String				_DEF_IMPLENAME;
	// 插件主接口类。
	private final Class<?>				_PLUGIN_INTERFACE;
	// 插件实现名称与类文件映射对象容器。
	private final Map<String, Class<?>>	_PLUGIN_IMPLS_CLASSES;
	// 插件实现名称与实现对象映射容器。单例类。
	private final Map<String, IPlugin>	_PLUGIN_IMPLS_OBJS;
	// 通过create方法创建的插件实例引用存放容器，用于框架退出时调用实例对象的资源释放方法。
	private final Set<IPlugin>			_PLUGIN_CREATE_OBJS			= new HashSet<>();

	// 插件目录。
	private final File					_PLUGIN_DIR;
	// 插件发布文件
	private final File					_PLUGIN_FILE;
	// 插件发布文件签名校验文件
	private final File					_PLUGIN_CHECKSUM_FILE;

	// 插件旧版本备份目录。
	private final File					_PLUGIN_BACKUP_DIR;
	// 插件旧版本备份文件。
	private final File					_PLUGIN_BACKUP_FILE;
	// 插件旧版本备份文件签名校验文件
	private final File					_PLUGIN_BACKUP_CHECKSUM_FILE;

	// 插件发布目录。
	private final File					_PLUGIN_DIST_DIR;

	// 是否启用自动更新。
	private final boolean				_AUTOUPDATE;
	// 发布文件下载请求根路径
	private final String				_LOADER_URL;
	// 发布文件下载请求地址
	private final String				_LOADER_PLUGIN_URL;
	// 发布文件签名校验文件下载请求地址
	private final String				_LOADER_PLUGIN_CHECKSUM_URL;

	// 当前插件使用的类加载器。
	private URLClassLoader				_classLoader;

	/**
	 * 构造函数
	 * 
	 * @param id 插件唯一标识
	 * @param name 插件名称
	 * @param impls 插件实现者集合
	 * @param params 插件自定义参数集合
	 * @param parentDir 插件存放目录的上级目录
	 * @param url 插件远程更新地址
	 * @param autoUpdate 是否自动检测更新
	 */
	public PluginClassLoader( String id, String name, Map<String, String> impls, Map<String, String> params, File parentDir, String url, boolean autoUpdate ){
		Objects.requireNonNull( id );
		Objects.requireNonNull( name );
		Objects.requireNonNull( impls );
		Objects.requireNonNull( parentDir );

		this._PLUGIN_ID = id;
		try{
			Class<?> idClass = Class.forName( id );
			if( null == idClass ){ throw new RuntimeException( "unknow interface:".concat( id ) ); }
			if( !idClass.isInterface() || !IPlugin.class.isAssignableFrom( idClass ) ){ throw new RuntimeException( "not IPlugin interface:".concat( id ) ); }
			this._PLUGIN_INTERFACE = idClass;
		}catch( ClassNotFoundException e ){
			throw new RuntimeException( e );
		}

		this._PLUGIN_NAME = name;
		String defImplName = null;
		Iterator<String> implKeys = impls.keySet().iterator();
		while( implKeys.hasNext() ){
			String implKey = implKeys.next();
			String implValue = impls.get( implKey );
			if( null == implKey || 0 == implKey.length() ){
				continue;
			}
			if( null == implValue || 0 == implValue.length() ){
				_LOG.warn( "skip empty impl:".concat( implKey ) );
				continue;
			}
			this._PLUGIN_IMPLS.put( implKey, implValue );
			if( null == defImplName || _KEY_DEF_IMPL_NAME.equals( implKey ) ){
				defImplName = implKey;
			}
		}
		this._DEF_IMPLENAME = defImplName;
		this._PLUGIN_IMPLS_CLASSES = new HashMap<>( impls.size() );
		this._PLUGIN_IMPLS_OBJS = new HashMap<>( impls.size() );

		// 获取插件配置数据。
		if( null != params && !params.isEmpty() ){
			this._PLUGIN_PARAMS.putAll( params );
		}

		// 检查远程访问url是否为空，如果为空，则不启用自动更新功能。
		if( null == url || 0 == ( url = url.trim() ).length() ){
			this._LOADER_URL = null;
			this._LOADER_PLUGIN_URL = null;
			this._LOADER_PLUGIN_CHECKSUM_URL = null;
		}else{
			this._LOADER_URL = url;
			this._LOADER_PLUGIN_URL = this._LOADER_URL.concat( this._PLUGIN_ID ).concat( "/" ).concat( _DIST_FILE_NAME );
			this._LOADER_PLUGIN_CHECKSUM_URL = _LOADER_PLUGIN_URL.concat( _DIST_FILE_CHECKSUM_SUFFIX );
		}
		// 如果远程地址为null，则禁用自动更新。否则根据指定参数决定是否禁用。
		this._AUTOUPDATE = ( null == this._LOADER_URL ? false : autoUpdate );

		this._PLUGIN_DIR = new File( parentDir, id );
		// // 如果当前插件目录不存在，则创建插件目录。
		if( !this._PLUGIN_DIR.exists() ){
			this._PLUGIN_DIR.mkdir();
		}
		this._PLUGIN_FILE = new File( this._PLUGIN_DIR, _DIST_FILE_NAME );
		this._PLUGIN_CHECKSUM_FILE = new File( this._PLUGIN_DIR, _DIST_FILE_NAME.concat( _DIST_FILE_CHECKSUM_SUFFIX ) );

		this._PLUGIN_BACKUP_DIR = new File( this._PLUGIN_DIR, "backup" );
		if( !this._PLUGIN_BACKUP_DIR.exists() ){
			this._PLUGIN_BACKUP_DIR.mkdir();
		}
		this._PLUGIN_BACKUP_FILE = new File( this._PLUGIN_BACKUP_DIR, _DIST_FILE_NAME );
		this._PLUGIN_BACKUP_CHECKSUM_FILE = new File( this._PLUGIN_BACKUP_DIR, _DIST_FILE_NAME.concat( _DIST_FILE_CHECKSUM_SUFFIX ) );

		this._PLUGIN_DIST_DIR = new File( this._PLUGIN_DIR, "dist" );

		// 检查是否启用自动更新。如果启用自动更新，则检查更新。否则直接初始化插件类加载器。
		if( this._AUTOUPDATE && hasUpdate() ){
			doUpdate();
		}
		initClassLoader();
	}

	/**
	 * @return 用户配置的插件名称
	 */
	public String getName(){
		return this._PLUGIN_NAME;
	}

	/**
	 * @return 插件唯一标识，当前版本为接口完整限定类名。
	 */
	public String getId(){
		return this._PLUGIN_ID;
	}

	/**
	 * 获取指定名称的接口实现实例对象。
	 * 
	 * @param implName 实现实例名称。
	 * @return 实现实例对象。
	 */
	public Object getImpl( String implName ){
		if( null == implName || 0 == ( implName = implName.trim() ).length() ){
			implName = _DEF_IMPLENAME;
		}
		if( !_PLUGIN_IMPLS.containsKey( implName ) ){ return null; }
		IPlugin result = _PLUGIN_IMPLS_OBJS.get( implName );
		if( null == result ){
			synchronized( _PLUGIN_IMPLS_OBJS ){
				result = _PLUGIN_IMPLS_OBJS.get( implName );
				if( null == result ){
					result = createImpl( implName );
					_PLUGIN_IMPLS_OBJS.put( implName, result );
				}
			}
		}
		return result;
	}

	/**
	 * 创建指定名称的接口实现实例对象。
	 * 
	 * @param implName 实现实例名称
	 * @return 实现实例对象
	 */
	public IPlugin createImpl( String implName ){
		if( null == implName || 0 == ( implName = implName.trim() ).length() ){
			implName = _DEF_IMPLENAME;
		}
		if( !_PLUGIN_IMPLS.containsKey( implName ) ){ return null; }
		Class<?> implClass = getImplClass( implName );
		if( null == implClass ){ throw new IllegalStateException( "No such impl ".concat( _PLUGIN_ID ).concat( " by name " ).concat( implName ) ); }
		IPlugin result = null;
		try{
			result = ( IPlugin )implClass.newInstance();
			if( null == result )
				return null;
			// 创建完成插件实例，调用插件实例的初始化方法。
			result.init( _PLUGIN_PARAMS );
			
			// 托管实例引用到集合中，便于后期统一进行资源释放。
			_PLUGIN_CREATE_OBJS.add( result ) ;
		}catch( InstantiationException | IllegalAccessException ex ){
			throw new RuntimeException( ex );
		}
		return result;
	}

	/**
	 * 获取指定名称对应的实现类。
	 * 
	 * @param implName 实现名称
	 * @return 实现类
	 */
	protected Class<?> getImplClass( String implName ){
		Class<?> implClass = _PLUGIN_IMPLS_CLASSES.get( implName );
		if( null == implClass ){
			synchronized( _PLUGIN_IMPLS_CLASSES ){
				implClass = _PLUGIN_IMPLS_CLASSES.get( implName );
				if( null == implClass ){
					try{
						String implClassName = this._PLUGIN_IMPLS.get( implName );
						implClass = Class.forName( implClassName, true, this._classLoader );
						if( !_PLUGIN_INTERFACE.isAssignableFrom( implClass ) ){ throw new IllegalStateException( "class " + implClassName + "is not subtype of interface:".concat( _PLUGIN_ID ) ); }
						_PLUGIN_IMPLS_CLASSES.put( implName, implClass );
					}catch( ClassNotFoundException e ){
						throw new RuntimeException( e );
					}
				}
			}
		}
		return implClass;
	}

	// 初始化类加载器
	protected void initClassLoader(){
		destroyClassLoader();

		List<URL> jarUrlList = new ArrayList<>();

		// 增加dist目录下的所有jar文件到类加载器。
		File[] files = this._PLUGIN_DIST_DIR.listFiles();
		if( null != files ){
			for( File file : files ){
				if( file.getName().endsWith( ".jar" ) ){
					try{
						URL jarUrl = file.toURI().toURL();
						jarUrlList.add( jarUrl );
					}catch( MalformedURLException e ){
						_LOG.error( e.getMessage() );
					}
				}
			}
		}
		// 检查是否存在lib目录，如果存在，增加lib目录下的所有jar文件到类加载器。
		File libDir = new File( this._PLUGIN_DIST_DIR, "lib" );
		if( libDir.exists() && libDir.isDirectory() ){
			files = libDir.listFiles();
			if( null != files ){
				for( File file : files ){
					if( file.getName().endsWith( ".jar" ) ){
						try{
							URL jarUrl = file.toURI().toURL();
							jarUrlList.add( jarUrl );
						}catch( MalformedURLException e ){
							_LOG.error( e.getMessage() );
						}
					}
				}
			}
		}

		URL[] jarUrls = jarUrlList.toArray( new URL[]{} );
		ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
		_classLoader = new URLClassLoader( jarUrls, parentLoader );

		_LOG.debug( String.format( "%s classloader jarCount:%d", _PLUGIN_ID, jarUrls.length ) );
	}

	/**
	 * 检查并更新插件。
	 */
	public void checkUpdate(){
		_LOG.debug( _PLUGIN_ID.concat( " check update." ) );
		if( this._AUTOUPDATE && hasUpdate() ){
			// 更新插件。
			doUpdate();
			// 重新初始化类加载器。
			initClassLoader();
		}
	}

	/**
	 * 检查插件是否有更新
	 */
	protected boolean hasUpdate(){
		if( null == this._LOADER_URL ){ return false; }
		try{
			String checksumRemote = null;
			if( this._PLUGIN_CHECKSUM_FILE.exists() ){
				// 如果存在本地签名校验文件，则读取签名文本。
				String checksumLocal = Files.readFirstLine( this._PLUGIN_CHECKSUM_FILE, Rdf.me().getCharset() );
				if( null != checksumLocal && 0 != ( checksumLocal = checksumLocal.trim() ).length() ){
					// 如果本地签名校验文本不为空，则获取远程签名文本进行比对。
					Request req = new Request.Builder().url( this._LOADER_PLUGIN_CHECKSUM_URL ).build();
					Response res = _HTTPCLIENT.newCall( req ).execute();
					if( !res.isSuccessful() ){
						_LOG.warn( "unexpected response:" + res );
						return false;
					}
					checksumRemote = res.body().string();
					if( null == checksumRemote || 0 == ( checksumRemote = checksumRemote.trim() ).length() ){
						_LOG.warn( "远程签名校验文本为空，无法判断一致性，跳过处理插件：".concat( this._PLUGIN_ID ) );
						return false;
					}
					if( checksumLocal.equals( checksumRemote ) ){
						_LOG.debug( this._PLUGIN_ID.concat( " already update。" ) );
						return false;
					}
				}
			}
			_LOG.debug( _PLUGIN_ID.concat( " has update." ) );
			return true;
		}catch( IOException e ){
			throw new RuntimeException( e );
		}
	}

	// 执行更新升级逻辑。
	protected void doUpdate(){
		try{
			// 如果存在旧的插件发布压缩包，备份到backup目录。
			if( this._PLUGIN_FILE.exists() ){
				if( this._PLUGIN_BACKUP_FILE.exists() ){
					this._PLUGIN_BACKUP_FILE.delete();
				}
				Files.move( this._PLUGIN_FILE, this._PLUGIN_BACKUP_FILE );
			}

			_LOG.debug( _PLUGIN_ID.concat( " download dist.zip." ) );
			// 下载远程插件发布压缩包。
			Request downloadReq = new Request.Builder().url( this._LOADER_PLUGIN_URL ).build();
			Response downloadRes = _HTTPCLIENT.newCall( downloadReq ).execute();
			if( !downloadRes.isSuccessful() ){
				_LOG.error( "unexpected response:" + downloadRes );
				throw new RuntimeException( "file download error：" + downloadRes );
			}
			try( InputStream downloadInStream = downloadRes.body().byteStream(); OutputStream downloadOutStream = new FileOutputStream( this._PLUGIN_FILE ); ){
				ByteStreams.copy( downloadInStream, downloadOutStream );
			}

			// 压缩包下载成功后，下载覆盖压缩包签名文件。
			// 如果存在旧的插件发布压缩包签名校验文件，备份到backup目录。
			if( this._PLUGIN_CHECKSUM_FILE.exists() ){
				if( this._PLUGIN_BACKUP_CHECKSUM_FILE.exists() ){
					this._PLUGIN_BACKUP_CHECKSUM_FILE.delete();
				}
				Files.move( this._PLUGIN_CHECKSUM_FILE, this._PLUGIN_BACKUP_CHECKSUM_FILE );
			}
			_LOG.debug( _PLUGIN_ID.concat( " download dist.zip.md5." ) );
			downloadReq = new Request.Builder().url( this._LOADER_PLUGIN_CHECKSUM_URL ).build();
			downloadRes = _HTTPCLIENT.newCall( downloadReq ).execute();
			if( !downloadRes.isSuccessful() ){
				_LOG.error( "unexpected response:" + downloadRes );
				throw new RuntimeException( "file download error：" + downloadRes );
			}
			try( InputStream downloadInStream = downloadRes.body().byteStream(); OutputStream downloadOutStream = new FileOutputStream( this._PLUGIN_CHECKSUM_FILE ); ){
				ByteStreams.copy( downloadInStream, downloadOutStream );
			}
		}catch( IOException e ){
			throw new RuntimeException( e );
		}
//		if( null != checksumRemote ){
//			// 如果是已经获取到了发布文件的md5值，则不再请求，直接写入。
//			Files.write( checksumRemote, this._PLUGIN_CHECKSUM_FILE, Rdf.me().getCharset() );
//		}else{
//			// 请求下载md5签名文件。
//			downloadReq = new Request.Builder().url( this._LOADER_PLUGIN_CHECKSUM_URL ).build();
//			downloadRes = _HTTPCLIENT.newCall( downloadReq ).execute();
//			try( InputStream downloadInStream = downloadRes.body().byteStream(); OutputStream downloadOutStream = new FileOutputStream( this._PLUGIN_CHECKSUM_FILE ); ){
//				ByteStreams.copy( downloadInStream, downloadOutStream );
//			}
//		}

		// 解压缩文件。
		unzipPluginFile();
	}

	// 解压缩插件发布文件。
	protected void unzipPluginFile(){
		// 检查是否存在解压缩的发布文件，如果存在，先删除之。
		if( this._PLUGIN_DIST_DIR.exists() ){
			RdfUtil.fileDelete( this._PLUGIN_DIST_DIR );
		}
		this._PLUGIN_DIST_DIR.mkdir();

		_LOG.debug( _PLUGIN_ID.concat( " unzip." ) );
		// 解压缩插件发布文件
		try( ZipFile distZipFile = new ZipFile( this._PLUGIN_FILE ); ){
			Enumeration<? extends ZipEntry> distZipEntrys = distZipFile.entries();
			while( distZipEntrys.hasMoreElements() ){
				ZipEntry distZipEntry = distZipEntrys.nextElement();
				File entryOutFile = new File( this._PLUGIN_DIST_DIR, distZipEntry.getName() );
				if( distZipEntry.isDirectory() ){
					entryOutFile.mkdir();
				}else{
					try( InputStream entryInstream = distZipFile.getInputStream( distZipEntry ); OutputStream entryOutstram = new FileOutputStream( entryOutFile ); ){
						ByteStreams.copy( entryInstream, entryOutstram );
					}
				}
			}
		}catch( IOException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 退出时释放资源方法。
	 */
	public void destroy(){
		destroyClassLoader();
		destroyObjs();
	}

	// 释放classLoader占用的资源。
	private void destroyClassLoader(){
		if( null != this._classLoader ){
			_LOG.debug( _PLUGIN_ID.concat( " close classLoader." ) );
			try{
				this._classLoader.close();
			}catch( IOException e ){
				_LOG.warn( e.getMessage() );
			}
			this._classLoader = null;
		}
	}

	// 释放所有插件实例资源。
	private void destroyObjs(){
		// 调用通过create方法创建的实例的资源释放方法。
		_PLUGIN_CREATE_OBJS.forEach( ( plugin ) -> {
			if( null != plugin )
				plugin.destroy();
		} );
		_PLUGIN_CREATE_OBJS.clear();

		// 调用通过get方法获取的单例实例的资源释放方法。
		_PLUGIN_IMPLS_OBJS.clear();

		_PLUGIN_IMPLS.clear();
		_PLUGIN_PARAMS.clear();

		_PLUGIN_IMPLS_CLASSES.clear();
	}

} // end class
