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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.yoya.struct.ObjectId;

import java.util.Map.Entry;

/**
 * Created by baihw on 16-7-3.
 *
 * 框架全局共享工具类。
 */
public final class RdfUtil{

	/**
	 * 对指定字符串进行前后去空格，判断是否为空字符串，为空则返回null,否则返回前后去空格的字符串。
	 * 
	 * @param source 原始值
	 * @return null / 前后去空格字符串
	 */
	public static String trimEmptyToNull( String source ){
		return ( null == source || 0 == ( source = source.trim() ).length() ) ? null : source;
	}

	/**
	 * 对指定字符串进行前后去空格，判断是否为空字符串，为空则返回指定值,否则返回前后去空格的字符串。
	 * 
	 * @param source 原始值
	 * @param value 为空时的返回值
	 * @return 指定值 / 前后去空格字符串
	 */
	public static String trimEmptyToValue( String source, String value ){
		return ( null == source || 0 == ( source = source.trim() ).length() ) ? value : source;
	}

	/**
	 * @return 获取一个uuid字符串。
	 */
	public static String getNID(){
		return new ObjectId().toSimple();
	}

	/**
	 * @return 获取一个uuid字符串。
	 */
	public static String getUUID(){
		return UUID.randomUUID().toString().replace( "-", "" );
	}

	/******************************************************************************
	 ************ 集合相关
	 ******************************************************************************/

	/**
	 * 移除集合中包含指定前缀键名的键值数据
	 * 
	 * @param source 源集合
	 * @param keyPrefix 匹配键名前缀
	 * @return 被移除的键值数据
	 */
	public static Map<String, String> removeSubMap( Map<String, String> source, String keyPrefix ){
		if( null == source || source.isEmpty() )
			return null;
		if( null == keyPrefix || 0 == ( keyPrefix = keyPrefix.trim() ).length() )
			return null;

		final int kpLen = keyPrefix.length();
		Map<String, String> result = new LinkedHashMap<String, String>( 10, 1.0f );
		Iterator<Entry<String, String>> entrys = source.entrySet().iterator();
		while( entrys.hasNext() ){
			Entry<String, String> entry = entrys.next();
			String key = entry.getKey();
			if( null == key ){
				entrys.remove();
				continue;
			}
			if( key.startsWith( keyPrefix ) ){
				result.put( key.substring( kpLen ), entry.getValue() );
				entrys.remove();
			}
		}
		return result;
	}

	/******************************************************************************
	 ************ 文件相关
	 ******************************************************************************/

	/**
	 * 删除文件或目录，如果是目录则递归删除目录下的所有文件及子孙目录下所有文件
	 * 
	 * @param dir 要删除的目录
	 * @return 如果全部删除成功返回true,否则返回false。
	 */
	public static boolean fileDelete( File dir ){
		if( null == dir || !dir.exists() )
			return true;
		if( dir.isDirectory() ){
			File[] subFiles = dir.listFiles();
			for( File subFile : subFiles ){
				boolean isSuccess = fileDelete( subFile );
				if( !isSuccess )
					return false;
			}
		}
		return dir.delete();
	}

	/******************************************************************************
	 ************ 网络地址相关
	 ******************************************************************************/

	/**
	 * 任意ip绑定的标识字符串
	 */
	public static final String ANYIP = "0.0.0.0";

	/**
	 * @return 获取本机ip地址
	 */
	public static String getLocalAddress(){
		try{
			return InetAddress.getLocalHost().getHostAddress();
		}catch( UnknownHostException e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 解析指定地址字符串.
	 * 
	 * 127.0.0.1:9999 to 127.0.0.1:9999
	 * 
	 * 127.0.0.1 to 127.0.0.1:9999
	 * 
	 * :9999 to localIP:9999
	 * 
	 * 9999 to localIP:9999
	 * 
	 * 0.0.0.0:9999 to localIP:9999
	 * 
	 * @param address 地址字符串，如果只有端口，则地址使用本机ip，如果只有ip，则端口使用默认端口。
	 * @param defaultPort 默认端口号
	 * @return 解析后的地址对象
	 */
	public static InetSocketAddress parseAddress( String address, int defaultPort ){
		if( null == address )
			return new InetSocketAddress( ANYIP, defaultPort );
		String host;
		int port;
		int ndx = address.indexOf( ':' );
		if( -1 == ndx ){
			// 没有冒号，判断填写的是ip还是端口
			ndx = address.indexOf( '.' );
			if( -1 == ndx ){
				// 填写的是端口，检测本机ip地址。
				port = Integer.parseInt( address );
				host = getLocalAddress();
			}else{
				// 填写的是ip地址，使用默认端口。
				host = ANYIP.equals( address ) ? getLocalAddress() : address;
				port = defaultPort;
			}
		}else if( 0 == ndx ){
			// 只有填写端口，检测本机ip地址。
			port = Integer.parseInt( address.substring( 1 ) );
			host = getLocalAddress();
		}else{
			host = address.substring( 0, ndx );
			if( ANYIP.equals( host ) ){
				host = getLocalAddress();
			}
			port = Integer.parseInt( address.substring( ndx + 1 ) );
		}
		return new InetSocketAddress( host, port );
	}

	/**
	 * 网络地址对象转字符串
	 * 
	 * @param address 地址对象
	 * @return ip + ":" + port 的表示形式字符串
	 */
	public static String socketAddressToString( InetSocketAddress address ){
		return String.format( "%s:%d", address.getHostString(), address.getPort() );
	}

} // end class
