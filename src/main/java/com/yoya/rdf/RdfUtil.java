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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
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
	 * @param value 原始值
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

} // end class
