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

package com.yoya.rdf.log;

/**
 * Created by baihw on 16-4-13.
 *
 * 日志规范接口
 */
public interface ILog{

	/**
	 * 日志级别
	 */
	enum LEVEL{
		DEBUG, INFO, WARN, ERROR
	}

	/**
	 * debug级别消息
	 *
	 * @param msg 消息内容
	 */
	void debug( String msg );

	/**
	 * info级别消息
	 *
	 * @param msg 消息内容
	 */
	void info( String msg );

	/**
	 * warn级别消息
	 *
	 * @param msg 消息内容
	 */
	void warn( String msg );

	/**
	 * error级别消息
	 *
	 * @param msg 消息内容
	 */
	void error( String msg );

	/**
	 * @return 日志类别名称
	 */
	String getCategory();

}
