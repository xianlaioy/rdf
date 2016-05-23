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
package com.yoya.rdf.router.application;

import com.yoya.rdf.Rdf;
import com.yoya.rdf.router.application.impl.RdbApplication;

/**
 * Created by baihw on 16-5-19.
 *
 * 应用全局共享对象操作入口
 */
public final class Application{
	
	/**
	 * 默认的会话实现名称
	 */
	public static final String	DEF_IMPL_NAME	= "RdbApplication";

	// 实现类实例
	private static final IApplication _IMPL;

	static{
		String implName = Rdf.me().getConfig( IApplication.CONFIG_GROUP, IApplication.KEY_IMPL );
		if( null == implName || "RdbApplication".equals( implName ) )
			_IMPL = new RdbApplication();
		else
			throw new RuntimeException( "unknow impl name:".concat( implName ) );
		
		// 检查环境初始化情况。
		RdbApplication.checkInit(); 
	}

	/**
	 * @return 实现者实例。
	 */
	public static IApplication impl(){
		return _IMPL;
	}
	
} // end class
