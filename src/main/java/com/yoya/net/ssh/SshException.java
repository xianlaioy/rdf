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

package com.yoya.net.ssh;

/**
 * Created by baihw on 16-4-27.
 *
 * ssh客户端执行过程中需要开发人员关注并进行处理的异常对象。
 */
public class SshException extends Exception{

	// 命令执行结束的退出码。非0的退出码都视为执行出错。
	private int _exitCode = -1;

	/**
	 * 构造函数
	 */
	public SshException(){
	}

	/**
	 * 构造函数
	 * 
	 * @param msg 异常消息
	 */
	public SshException( String msg ){
		super( msg );
	}

	/**
	 * 构造函数
	 * 
	 * @param t 异常对象
	 */
	public SshException( Throwable t ){
		super( t );
	}

	/**
	 * 构造函数
	 * 
	 * @param msg 异常消息
	 * @param t 异常对象
	 */
	public SshException( String msg, Throwable t ){
		super( msg, t );
	}

	/**
	 * 构造函数
	 * 
	 * @param exitCode 退出码
	 * @param msg 异常消息
	 */
	public SshException( int exitCode, String msg ){
		super( msg );
		this._exitCode = exitCode;
	}

	/**
	 * @param exitCode 退出码
	 */
	public void setExitCode( int exitCode ){
		this._exitCode = exitCode;
	}

	/**
	 * @return 退出码。
	 */
	public int getExitCode(){
		return this._exitCode;
	}

}
