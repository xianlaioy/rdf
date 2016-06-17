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

package com.yoya.rdf.router.impl;

import java.util.Map;

import com.yoya.rdf.router.AbstractRequest;
import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.session.ISession;
import com.yoya.rdf.router.session.impl.LocalSession;

/**
 * Created by baihw on 16-4-15.
 *
 * 简单的请求对象实现
 */
public class SimpleRequest extends AbstractRequest implements IRequest{

	public SimpleRequest( String id ){
		super( id );
	}

	@Override
	protected void setPath( String path ){
		super.setPath( path );
	}

	@Override
	protected void setHeaders( Map<String, String> headers ){
		super.setHeaders( headers );
	}

	@Override
	protected void setParameters( Map<String, String> parameters ){
		super.setParameters( parameters );
	}

	@Override
	protected ISession buildSession(){
		String sid = getHeader( ISession.KEY_SESSIONID );
		return LocalSession.getSession( sid );
	}

}
