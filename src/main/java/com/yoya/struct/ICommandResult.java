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
package com.yoya.struct;

/**
 * Created by baihw on 16-6-5.
 *
 * 统一的返回数据规范接口。
 */
public interface ICommandResult{

	/**
	 * 正常返回时的响应代码
	 */
	int CODE_OK = 200;

	/**
	 * @return 响应代码，200为正常返回。
	 */
	int getCode();

	/**
	 * @return 响应消息，code为200时为null，其它情况为对应的提示信息。
	 */
	String getMessage();

	/**
	 * @return 响应数据，code为200时为rpc调用的返回结果，其它情况为null。
	 */
	Object getData();

	/**
	 * @return 是否为成功的数据响应。
	 */
	boolean isOk();

} // end class
