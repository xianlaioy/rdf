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

package com.yoya.rdf.log.impl;

import com.yoya.rdf.log.ILog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by baihw on 16-4-14.
 *
 * 一个简单的日志实现
 */
final class SimpleLog implements ILog{

	// 日志时间显示格式。
	static final DateTimeFormatter	LOG_DT_FORMAT	= DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss,SSS" );

	// 调用堆栈中日志调用者名称
	static final String				STACK_CALLER	= SimpleLog.class.getName();

	// 行数据分隔符号。
	static final String				LINE_SEPARATOR	= System.getProperty( "line.separator" );

	// 日志文本中使用到的字符
	static final String				SPACE			= " ";
	static final String				DOT				= ".";
	static final String				COLON			= ":";

	// 日志类别名称。
	private final String			_CATEGORY;
	// 日志类别名称最大长度。
	static final int				_CATEGORY_MAX	= 20;

	/**
	 * 构造函数
	 *
	 * @param category 日志类别名称
	 */
	SimpleLog( String category ){
		this._CATEGORY = category;
	}

	@Override
	public void debug( String msg ){
		System.out.print( msgFormat( LEVEL.DEBUG, msg ) );
	}

	@Override
	public void info( String msg ){
		System.out.print( msgFormat( LEVEL.INFO, msg ) );
	}

	@Override
	public void warn( String msg ){
		System.err.print( msgFormat( LEVEL.WARN, msg ) );
	}

	@Override
	public void error( String msg ){
		System.err.print( msgFormat( LEVEL.ERROR, msg ) );
	}

	@Override
	public String getCategory(){
		return this._CATEGORY;
	}

	/**
	 * 格式化消息
	 *
	 * @param level 消息级别
	 * @param msg 原始消息
	 * @return 统一处理后的消息
	 */
	private String msgFormat( LEVEL level, String msg ){
		StringBuilder sb = new StringBuilder();
		// 日志级别
		sb.append( level.name() );
		if( LEVEL.INFO == level || LEVEL.WARN == level )
			sb.append( SPACE );
		sb.append( SPACE );
		// 日志时间
		sb.append( LOG_DT_FORMAT.format( LocalDateTime.now() ) );
		sb.append( SPACE );
		// 日志类别
		if( _CATEGORY.length() > _CATEGORY_MAX ){
			sb.append( _CATEGORY.substring( _CATEGORY.length() - _CATEGORY_MAX ) );
		}else{
			sb.append( _CATEGORY );
		}
		// 日志调用方法及源代码行号
		StackTraceElement[] stackArr = new Throwable().getStackTrace();
		boolean skipCaller = false;
		for( StackTraceElement stack : stackArr ){
			if( skipCaller && !STACK_CALLER.equals( stack.getClassName() ) ){
				sb.append( DOT ).append( stack.getMethodName() ).append( COLON ).append( stack.getLineNumber() ).append( SPACE );
				break;
			}
			if( STACK_CALLER.equals( stack.getClassName() ) ){
				skipCaller = true;
				continue;
			}
		}
		// 日志线程
		sb.append( "[" );
		sb.append( Thread.currentThread().getName() );
		sb.append( "-" );
		sb.append( Thread.currentThread().getId() );
		sb.append( "]- " );

		sb.append( msg );

		// 日志文本中有换行时，统一进行缩进处理
//		for( int ex = 0; ( ex = sb.indexOf( "\n", ex ) ) != -1; ++ex ){
//			sb.insert( ex + 1, "   " );
//		}

		// 以换行符结束
		sb.append( LINE_SEPARATOR );
		return sb.toString();
	}
}
