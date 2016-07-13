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
package com.yoya.rdf.router.session;

import com.yoya.rdf.Rdf;
import com.yoya.rdf.router.session.impl.MysqlSession;

/**
 * Created by baihw on 16-5-17.
 *
 * 统一的请求对象规范接口
 */
public final class SessionManger{

	/**
	 * 默认的会话实现名称
	 */
	public static final String	DEF_IMPL_NAME	= "MysqlSession";

	/**
	 * 默认的会话超时时间：45分钟。
	 */
	public static final int		DEF_TIMEOUT		= 45;

	private final String		_IMPL_NAME;
	private final int			_TIMEOUT;

	/**
	 * 私有的构造函数，获取当前对象唯一实例请使用me()方法。
	 */
	private SessionManger(){
		String implName = Rdf.me().getConfig( ISession.CONFIG_GROUP, ISession.KEY_IMPL );
		if( null == implName || 0 == ( implName = implName.trim() ).length() ){
			_IMPL_NAME = DEF_IMPL_NAME;
		}else{
			_IMPL_NAME = implName;
		}
		if( !DEF_IMPL_NAME.equals( _IMPL_NAME ) ){
			// 当前版本只支持MysqlSession实现。
			throw new RuntimeException( "unknow impl:".concat( _IMPL_NAME ) );
		}

		int t = 0;
		String timeout = Rdf.me().getConfig( "session", "timeout" );
		if( null == timeout || 0 == ( timeout = timeout.trim() ).length() || 1 > ( t = Integer.parseInt( timeout ) ) ){
			_TIMEOUT = DEF_TIMEOUT;
		}else{
			_TIMEOUT = t;
		}

		// 检查环境初始化情况。
		MysqlSession.checkInit();

	}

	/**
	 * 当前对象唯一实例持有者
	 * 
	 * @author baihw
	 */
	private static final class SessionManagerHolder{
		private static final SessionManger _INSTANCE = new SessionManger();
	}

	/**
	 * @return 当前对象唯一实例。
	 */
	public static SessionManger me(){
		return SessionManagerHolder._INSTANCE;
	}

	/**
	 * 获取指定标识的会话对象
	 * 
	 * @param sessionId 会话唯一标识
	 * @return 会话对象
	 */
	public ISession getSession( String sessionId ){
		return new MysqlSession( sessionId, _TIMEOUT );
	}

} // end class
