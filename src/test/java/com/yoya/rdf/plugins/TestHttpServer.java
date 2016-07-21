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
package com.yoya.rdf.plugins;

import com.yoya.config.impl.SimpleConfig;
import com.yoya.rdf.Rdf;
import com.yoya.rdf.plugin.PluginLoader;

/**
 * 
 * @author baihw
 * @date 2016年7月18日
 **/
public class TestHttpServer{

	public static void main( String[] args ){

		// 创建一个简单的配置对象设置框架配置属性。
	    SimpleConfig config = new SimpleConfig();
	    // 设置身份密钥
	    config.putValue( "global", "AK", "rdf-registry" );
	    config.putValue( "global", "SK", "123456" );
	    // 设置插件相关配置。
//	    config.putValue( "plugin", "loader.workbase", "C:/plugins" );
	    config.putValue( "plugin", "autoUpdate", "false" );
	    config.putValue( "plugin", "names", "p1" );
	    config.putValue( "plugin", "p1.interface", "com.yoya.rdf.plugins.IHttpServer" );
	    config.putValue( "plugin", "p1.impls", "def=com.yoya.rdf.plugins.httpServer.NettyHttpServer" );
	    config.putValue( "plugin", "p1.bindAddress", "127.0.0.1:9998" );

	    // 使用自定义配置对象初始化框架。
	    Rdf.me().init( config );
		
		IHttpServer httpServer = PluginLoader.impl().getPluginImpl( IHttpServer.class ) ;
		httpServer.start(); 

	}

} // end class
