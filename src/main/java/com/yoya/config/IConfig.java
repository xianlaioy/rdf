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

package com.yoya.config;

/**
 * Created by baihw on 16-4-14.
 *
 * 配置对象规范接口
 */
public interface IConfig{

	/**
	 * 默认的配置组名称字符串。
	 */
	String DEF_GROUP = "global";

	/**
	 * 获取默认组指定配置项数据
	 * 
	 * @param key 配置项
	 * @return 配置值
	 */
	String get( String key );

	/**
	 * 获取指定组下的配置项数据
	 * 
	 * @param group 配置组
	 * @param key 配置项
	 * @return 配置值
	 */
	String get( String group, String key );

}
