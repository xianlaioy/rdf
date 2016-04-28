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

package com.yoya.rdf.router.impl;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by baihw on 16-4-15.
 *
 * url处理工具类
 */
final class UrlUtil{
	/**
	 * url元素结构化对象。
	 */
	static class UrlItem{
		private String		_action;
		private String		_start;
		private String[]	_params;

		UrlItem( String action, String start, String[] params ){
			this._action = action;
			this._start = start;
			this._params = params;
		}

		String getAction(){
			return this._action;
		}

		String getStart(){
			return this._start;
		}

		String[] getParams(){
			return null == this._params ? null : this._params.clone();
		}

		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append( "{'action':'" ).append( _action );
			sb.append( "', 'start':'" ).append( _start );
			sb.append( "', 'params':" ).append( Arrays.toString( _params ) );
			sb.append( "}" );
			return sb.toString();
		}
	}

	/**
	 * 解析url元素为action,start,params三部分。
	 *
	 * @param url 例如: /User/login, /User/login/, /User/login/{:org}, /User/login/{:org}-{:id}
	 * @param hasAction 检查传入的action名称是否存在的方法
	 * @param hasStart 检查传入的action名称下对应的方法名是否存在的方法
	 * @return action, start, params对应的数据
	 */
	static UrlItem parseUrl( String url, Function<String, Boolean> hasAction, BiFunction<String, String, Boolean> hasStart ){
		Objects.requireNonNull( url, "url can not be null!" );
		if( 0 == ( url = url.trim() ).length() ){ return new UrlItem( "/", null, null ); }
		if( '/' != url.trim().charAt( 0 ) ){
			// 统一以'/'开头。
			url = '/' + url;
		}

		if( 1 == url.length() ){
			// 长度为1，说明只有一个'/'字符。通常为请求首页。
			return new UrlItem( url, null, null );
		}

		if( url.endsWith( "/" ) ){
			// 如果以'/'结尾，删除结尾的'/'字符。
			url = url.substring( 0, url.length() - 1 ).trim();
		}

		int ndx = url.lastIndexOf( "/" );
		// 处理url只有一级的情况
		if( 0 == ndx ){
			// 如果最后一次出现‘/’字符的位置是0,说明只有请求路径只能一级，如: /a, /b, /user
			return new UrlItem( url, null, null );
		}

		// 处理url有多级的情况
		// 如果直接请求到的是注册地址，则认为使用默认方法，无para参数。
		if( hasAction.apply( url ) ){
			// do default method
			return new UrlItem( url, null, null );
		}

		String[] params = null;
		// 分离出url最后一级的元素
		String lastItem = url.substring( ndx + 1 ).trim();
		url = url.substring( 0, ndx ).trim();

		// 最后一级包含'-'符号则认为一定是para参数,解析para参数
		if( lastItem.contains( "-" ) ){
			params = lastItem.split( "-" );
		}else{
			// 不包含'-'符号，根据是否存在注册对象来判断最后一级是方法名还是para参数。
			if( hasAction.apply( url ) ){
				// 如果存在注册对象，根据注册对象是否有此方法名来判断最后一级是方法名还是para参数。
				if( hasStart.apply( url, lastItem ) ){
					// 找到匹配的方法名，则将最后一级作为方法名。
					return new UrlItem( url, lastItem, null );
				}else{
					// 找不到匹配的方法名，则将最后一级作为param参数。
					return new UrlItem( url, null, new String[]{ lastItem } );
				}
			}
			// 如果不存在注册对象，则认为最后一级不可能是方法名，所以一定是para参数。
			params = new String[]{ lastItem };
		}

		// 如果最后一级是para参数，判断当前是直接命中注册对象，还是需要再进行方法名解析。
		if( hasAction.apply( url ) ){
			// 如果直接命中注册对象，则认为是调用默认方法，传递当前para参数。
			return new UrlItem( url, null, params );
		}

		// 如果最后一级是para参数，且无法命中注册对象，则再向上截取一级，作为方法名。
		ndx = url.lastIndexOf( "/" );
		if( 0 == ndx ){
			// 如果已经到了顶级目录，则标志着注册对象查找失败。返回404的注册对象名，方法名为空，最后一级作为参数。
			return new UrlItem( url, null, params );
		}

		// 此处的最后一级直接作为方法名，向上一级为注册对象。
		lastItem = url.substring( ndx + 1 ).trim();
		url = url.substring( 0, ndx ).trim();
		return new UrlItem( url, lastItem, params );
	}
}
