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
package com.yoya.serialization.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.yoya.serialization.ISerialization;

/**
 * Created by baihw on 16-5-18.
 * 
 * 基于jdk的序列化实现
 */
public class JdkSerialization implements ISerialization{

	@Override
	public byte[] serialize( Object obj ) throws IOException{
		ObjectOutputStream oos = null;
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream( baos );
			oos.writeObject( obj );
			return baos.toByteArray();
		}finally{
			if( null != oos )
				try{
					oos.close();
				}catch( IOException e ){
				}
		}
	}

	@Override
	public Object deserialize( byte[] bytes ) throws IOException{
		if( null == bytes || 0 == bytes.length )
			return null;
		ObjectInputStream ois = null;
		try{
			ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
			ois = new ObjectInputStream( bais );
			return ois.readObject();
		}catch( ClassNotFoundException e ){
			throw new RuntimeException( e );
		}finally{
			if( ois != null )
				try{
					ois.close();
				}catch( IOException e ){
				}
		}
	}

} // end class
