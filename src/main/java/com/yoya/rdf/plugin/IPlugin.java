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
package com.yoya.rdf.plugin;

import java.util.Map;

/**
 * Created by baihw on 16-5-16.
 *
 * Rdf框架插件规范接口。
 */
public interface IPlugin{

	/**
	 * 插件创建完成后调用的初始化方法
	 * 
	 * @param params 初始化参数
	 */
	void init( Map<String, String> params );

	/**
	 * 框架退出时调用的插件释放资源的逻辑处理方法
	 */
	void destroy();

} // end class
