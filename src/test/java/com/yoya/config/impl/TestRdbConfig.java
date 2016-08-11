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

/**
 * Created by baihw on 16-5-12.
 *
 */
public class TestRdbConfig{

	public static void main( String[] args ){
		String url = "jdbc:h2:~/test;CIPHER=AES;MODE=MySQL;AUTO_SERVER=TRUE";
		String user = "admin";
		String password = "123 123";
		H2Config config = new H2Config( url, user, password );
		System.out.println( "config:" + config );
	}

}
