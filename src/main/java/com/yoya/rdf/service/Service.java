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
package com.yoya.rdf.service;

import com.yoya.rdf.Rdf;
import com.yoya.rdf.router.IRouter;
import com.yoya.rdf.service.impl.SimpleService;

/**
 * Created by baihw on 16-6-8.
 *
 * 服务操作入口。
 */
public final class Service{

	// 实现类实例
	private static final IService _IMPL;

	static{
		String implName = Rdf.me().getConfig( IService.CONFIG_GROUP, IRouter.KEY_IMPL );
		if( null == implName || 0 == ( implName = implName.trim() ).length() || "simple".equals( implName ) )
			_IMPL = new SimpleService();
		else
			throw new RuntimeException( "unknow impl name:".concat( implName ) );
	}

	/**
	 * @return 实现者实例。
	 */
	public static IService impl(){
		return _IMPL;
	}

} // end class
