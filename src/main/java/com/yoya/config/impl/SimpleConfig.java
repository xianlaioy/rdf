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

package com.yoya.config.impl;

import com.yoya.config.AbstractConfig;
import com.yoya.config.IConfig;

/**
 * Created by baihw on 16-4-15.
 *
 * 一个简单的配置对象实现，此实现为单机版，不适用于多实例集群环境。
 */
public class SimpleConfig extends AbstractConfig implements IConfig{

	public SimpleConfig(){
		// 调用抽象基类提供的默认数据初始化方法.
		super.init();
	}

	@Override
	public void putValue( String group, String key, String value ){
		super.putValue( group, key, value );
	}

} // end class
