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
package com.yoya.serialization;

import java.io.IOException;

/**
 * Created by baihw on 16-5-18.
 *
 * 序列化规范接口。
 */
public interface ISerialization{

	/**
	 * 执行对象序列化处理
	 * @param obj 要序列化的对象
	 * @return 序列化后的数据
	 * @throws IOException 序列化操作异常
	 */
	public byte[] serialize( Object obj ) throws IOException;

	/**
	 * 执行对象序反列化处理
	 * @param bytes 要反序列化的数据
	 * @return 序列化前的对象
	 * @throws IOException 序列化操作异常
	 */
	public Object deserialize( byte[] bytes ) throws IOException;
	
} // end class
