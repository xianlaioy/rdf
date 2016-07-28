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
package com.yoya.rdf.router.session.impl;

import java.util.UUID;

/**
 * Created by baihw on 16-6-5.
 * 
 */
public class TestLocalSession{

	public static void main( String[] args ) throws InterruptedException{
		final String sessionId = UUID.randomUUID().toString().replace( "-", "" );
		LocalSession sess = LocalSession.getSession( sessionId );

		sess.setAttribute( "key1", "key1_value" );
		System.out.println( Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		sess.setAttribute( "key2", "key2_value" );
		System.out.println( Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		sess.removeAttribute( "key1" );
		System.out.println( Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		sess.removeAttribute( "key2" );
		System.out.println( Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		sess.setAttribute( "key3", "key3_value" );
		System.out.println( Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		sess.setAttribute( "key4", "key4_value" );
		System.out.println( Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );

		Thread.sleep( 980 );
		sess.setAttribute( "key5", "key5_value" );
//		System.out.println( "sleep980:" + Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		Thread.sleep( 980 );
		sess.removeAttribute( "key4" );
//		System.out.println( "sleep980:" + Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		Thread.sleep( 980 );
		System.out.println( "sleep980:" + Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		Thread.sleep( 990 );
		sess.removeAttribute( "key5" );
		System.out.println( "sleep990:" + Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		Thread.sleep( 1000 );
		sess.setAttribute( "key3", "key3_newValue" );
		System.out.println( "sleep1000:" + Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		Thread.sleep( 990 );
		System.out.println( "sleep990:" + Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
		Thread.sleep( 1010 );
		System.out.println( "sleep1010:" + Thread.currentThread().getId() + ", sess:" + sess + ", cache:" + sess.getData() );
	}

} // end class
