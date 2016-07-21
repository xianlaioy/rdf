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

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.yoya.config.IConfig;
import com.yoya.rdf.plugin.PluginLoader;

/**
 * Created by baihw on 16-4-13.
 *
 * Rdf框架主文件
 */
public class Rdf{

	/**
	 * 应用唯一标识。
	 */
	public static final String					KEY_AK			= "AK";

	/**
	 * 应用超级权限访问密钥。
	 */
	public static final String					KEY_SK			= "SK";

	/**
	 * 应用使用的编码配置关键字。
	 */
	public static final String					KEY_ENCODING	= "encoding";

	/**
	 * 应用使用的主目录配置关键字。
	 */
	public static final String					KEY_HOMEDIR		= "homeDir";

	// 类路径根目录
	private static final String					_CLASSROOT;
	// web路径根目录
	private static final String					_WEBROOT;

	// 当前对象唯一实例
	private static final Rdf					_ME				= new Rdf();

	// 框架主全局配置对象
	private IConfig								_CONFIG			= null;
	// 框架主目录
	private String								_homeDir		= null;
	// 应用身份信息
	private String								_ak				= null;
	// 应用身份密钥
	private String								_sk				= null;
	// 应用使用的编码
	private String								_encoding		= null;
	// 应用使用的编码字符集
	private Charset								_charset		= null;
	
	// 初始化时获取根目录信息
	static{
		final URL rootURL = Rdf.class.getResource( "/" );
		final String rootClassPath = null == rootURL ? new File( "." ).getAbsoluteFile().getParent() : rootURL.getPath();

		String classRoot = new File( rootClassPath ).getAbsolutePath();

		// 获取webRoot目录
		int webinfoIndex = rootClassPath.indexOf( "WEB-INF" );
		String webRoot = -1 == webinfoIndex ? null : rootClassPath.substring( 0, webinfoIndex );

		// 对所有路径进行路径分割符统一处理
		final String replaceReg = "\\".equals( File.separator ) ? "\\\\+" : File.separator + "+";
		classRoot = classRoot.replaceAll( replaceReg, "/" );
		if( null != webRoot )
			webRoot = webRoot.replaceAll( replaceReg, "/" );

		_CLASSROOT = classRoot;
		_WEBROOT = webRoot;
	}

//	public static void main( String[] args ){
//		Rdf.me().init( new SimpleConfig() );
//		System.out.println( "CLASSROOT:" + Rdf.getClassRoot() );
//		System.out.println( "WEBROOT:" + Rdf.getWebRoot() );
//		System.out.println( "homeDir:" + Rdf.me().getHomeDir() );
//	}

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
	 * 执行初始化动作，不允许重复执行。
	 */
	public void init( IConfig config ){

		Objects.requireNonNull( config );

		if( null != _CONFIG )
			throw new RuntimeException( "已经初始化过，请不要重复执行！" );

		// 初始化config对象
		_CONFIG = config;

		// 检查确认应用主目录。
		String homeDir = _CONFIG.get( KEY_HOMEDIR );
		if( null == homeDir || 0 == ( homeDir = homeDir.trim() ).length() )
			homeDir = null;
		if( null == homeDir ){
			// 如果用户没有指定主目录，则探测是否是web项目，web项目使用webRoot根目录，非web项目使用classRoot根路径。
			// 在探测到的路径下建立homeDir目录用作应用的主目录。
			homeDir = null == _WEBROOT ? _CLASSROOT.concat( "/" ).concat( KEY_HOMEDIR ) : _WEBROOT.concat( "/WEB-INF/" ).concat( KEY_HOMEDIR );
		}

		// 统一不以路径分割符结尾。
		if( homeDir.endsWith( File.separator ) ){
			homeDir = homeDir.substring( 0, homeDir.length() - 1 );
		}

		// 检查主目录是否存在，如果不存在，创建之。
		File hdf = new File( homeDir );
		if( !hdf.exists() ){
			hdf.mkdirs();
		}

		// 绑定主目录变量，用于项目中获取。
		this._homeDir = homeDir;

		// 缓存常用配置变量
		this._ak = _CONFIG.get( KEY_AK );
		this._sk = _CONFIG.get( KEY_SK );
		this._encoding = _CONFIG.get( KEY_ENCODING );
		this._charset = Charset.forName( this._encoding );
	}

	/**
	 * 调用框架关闭动作，销毁资源占用。
	 */
	@SuppressWarnings( "deprecation" )
	public void destroy(){

		// 插件资源释放
		PluginLoader.impl().destroy();

		// 反注册驱动类
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while( drivers.hasMoreElements() ){
			Driver driver = drivers.nextElement();
			try{
				DriverManager.deregisterDriver( driver );
			}catch( SQLException e ){
				e.printStackTrace();
			}
		}

		// 检查如果存在mysql对应的清理线程，则关闭之。
		try{
			com.mysql.jdbc.AbandonedConnectionCleanupThread.shutdown();
		}catch( Exception e ){
		}

		// 清理netty遗留线程。
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Thread[] threadArray = threadSet.toArray( new Thread[threadSet.size()] );
		for( Thread t : threadArray ){
//			System.out.println( "name:" + t.getName() + ", t:" + t );
			if( t.getName().contains( "Abandoned connection cleanup thread" ) || t.getName().contains( "Executor" ) ){
				synchronized( t ){
					t.stop();
				}
			}
		}

	}

	/**
	 * 框架主目录为框架中统一的一个本地工作根目录，基于框架开发的插件及第三方功能推荐使用此目录作为根目录。便于统一管理。
	 * 
	 * 主目录存放位置的确定规则为：如果是web项目，存放于WEB-INF目录下，如果不是web项目，存放于类文件存放目录。
	 * 
	 * @return 框架主目录
	 */
	public String getHomeDir(){
		return this._homeDir;
	}

	/**
	 * @return 应用唯一标识
	 */
	public String getAK(){
		return this._ak;
	}

	/**
	 * @return 应用超级权限访问密钥
	 */
	public String getSK(){
		return this._sk;
	}

	/**
	 * @return 框架使用的字符集编码
	 */
	public String getEncoding(){
		return this._encoding;
	}

	/**
	 * @return 框架使用的字符集编码
	 */
	public Charset getCharset(){
		return this._charset;
	}

	/********************************************************************************************
	 ********************************** 路径相关处理
	 ********************************************************************************************/

	/**
	 * 获取项目类路径根目录
	 * 
	 * @return 类路径根目录
	 */
	public static String getClassRoot(){
		return _CLASSROOT;
	}

	/**
	 * 在web环境中获取web项目根目录
	 * 
	 * @return webRoot根目录
	 */
	public static String getWebRoot(){
		return _WEBROOT;
	}

	/**
	 * 使用系统默认分割符(File.separator)执行文件路径修复,用于处理文件路径拼接时可能会多出来分割符问题。 转换示例如下: C:\dir1\\dir2\file.suffix -- c:\dir1\dir2\file.suffix /home///usr//file.suffix -- /home/usr/file.suffix
	 * 
	 * @param path 要修复的路径字符串
	 * @return 修正后的路径
	 */
	public static String fixedPath( final String path ){
		return fixedPath( path, File.separator );
	}

	/**
	 * 文件路径修复,用于处理文件路径拼接时可能会多出来分割符问题。 转换示例如下:
	 * 
	 * <pre>
	 * C:\dir1\\dir2\file.suffix -- c:/dir1/dir2/file.suffix 
	 * /home///usr//file.suffix -- /home/usr/file.suffix
	 * </pre>
	 * 
	 * @param path 要修复的路径字符串
	 * @param separator 路径分割符,通常为: File.separator
	 * @return 修正后的路径
	 */
	public static String fixedPath( String path, String separator ){
		if( null == path || 0 == ( path = path.trim() ).length() )
			return null;
		String reg = ( null == separator || "\\".equals( separator ) ) ? "\\\\+" : separator + "+";
		String result = path.replaceAll( reg, "/" );
		return result;
	}

	/********************************************************************************************
	 ********************************** 配置相关处理
	 ********************************************************************************************/

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

	/********************************************************************************************
	 ********************************** 插件相关处理
	 ********************************************************************************************/

//	/**
//	 * 插件注册
//	 * 
//	 * @param plugin 插件对象
//	 */
//	public void pluginRegister( IPlugin plugin ){
//		_PLUGINS.put( plugin.toString(), plugin );
//	}

//	/**
//	 * 使用框架类加载器加载指定名称的类
//	 *
//	 * @param className 包含包名的完整类名称
//	 * @return 如果找不到，返回null。
//	 */
//	public Class<?> loadClass( String className ){
//		try{
//			Class<?> result = Class.forName( className );
//			return result;
//		}catch( ClassNotFoundException e ){
//			return null;
//		}
//	}
//
//	/**
//	 * 使用框架类加载器查找并创建指定名称的类实例
//	 *
//	 * @param className 包含包名的完整类名称
//	 * @return 如果找不到或创建失败，将抛出运行时异常。
//	 */
//	public Object newClass( String className ){
//		try{
//			Class<?> result = Class.forName( className );
//			result.newInstance();
//			return result;
//		}catch( ClassNotFoundException | InstantiationException | IllegalAccessException e ){
//			throw new RuntimeException( e );
//		}
//	}

} // end class
