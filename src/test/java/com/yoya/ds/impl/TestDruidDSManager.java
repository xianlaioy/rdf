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

package com.yoya.ds.impl;

import com.yoya.ds.DSManager;
import com.yoya.rdf.TestRdf;

/**
 * Created by baihw on 16-5-16.
 *
 * 测试基于Druid库实现的数据源管理对象。
 */
public class TestDruidDSManager{

	public static void main( String[] args ){
		TestRdf.initRdfByRdbConfig();
		
		System.out.println( "conn:" + DSManager.impl().getConn() );
	}

} // end class
