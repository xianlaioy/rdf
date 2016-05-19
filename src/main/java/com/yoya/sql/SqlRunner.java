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
package com.yoya.sql;

import com.yoya.ds.DSManager;
import com.yoya.rdf.Rdf;
import com.yoya.sql.impl.SimpleSqlRunner;

/**
 * Created by baihw on 16-5-18.
 *
 * sql操作入口类
 */
public final class SqlRunner{

	// 实现类实例
	private static final ISqlRunner _IMPL;

	static{
		String implName = Rdf.me().getConfig( ISqlRunner.CONFIG_GROUP, ISqlRunner.KEY_IMPL );
		if( null == implName || "simple".equals( implName ) )
			_IMPL = new SimpleSqlRunner( DSManager.impl().getDS() );
		else
			throw new RuntimeException( "unknow impl name:".concat( implName ) );
	}

	/**
	 * @return 路由实例。
	 */
	public static ISqlRunner impl(){
		return _IMPL;
	}

} // end class
