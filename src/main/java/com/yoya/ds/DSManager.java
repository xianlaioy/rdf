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

package com.yoya.ds;

import com.yoya.ds.impl.DruidDSManager;
import com.yoya.rdf.Rdf;

/**
 * Created by baihw on 16-5-13.
 *
 * 数据源管理对象入口
 */
public class DSManager{

	// 实现类实例
	private static final IDSManager _IMPL;

	static{
		String implName = Rdf.me().getConfig( IDSManager.CONFIG_GROUP, IDSManager.KEY_IMPL );
		if( null == implName || 0 == ( implName = implName.trim() ).length() || "druid".equals( implName ) ){
			_IMPL = new DruidDSManager();
			// 向框架注册插件以便于框架退出时回调资源释放方法。
			Rdf.me().pluginRegister( _IMPL );
		}else
			throw new RuntimeException( "unknow impl name:".concat( implName ) );
	}

	/**
	 * @return 路由实例。
	 */
	public static IDSManager impl(){
		return _IMPL;
	}

}
