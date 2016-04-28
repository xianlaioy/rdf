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

package com.yoya.sql.impl;

import com.alibaba.druid.filter.logging.LogFilter;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;

/**
 * Created by baihw on 16-4-26.
 *
 * 实现在控制台打印SQL执行日志
 */
final class ConsoleSqlReport extends LogFilter{

	// 日志对象
//	private ILog	dataSourceLogger	= LogManager.getLog( dataSourceLoggerName );
	private ILog	connectionLogger	= LogManager.getLog( connectionLoggerName );
	private ILog	statementLogger		= LogManager.getLog( statementLoggerName );
	private ILog	resultSetLogger		= LogManager.getLog( resultSetLoggerName );

	ConsoleSqlReport(){
        super.setStatementExecutableSqlLogEnable( true );
		super.setStatementCreateAfterLogEnabled( false );
		super.setStatementCloseAfterLogEnabled( false );
		super.setStatementPrepareAfterLogEnabled( false );
		super.setStatementPrepareCallAfterLogEnabled( false );
		super.setStatementExecuteQueryAfterLogEnabled( false );
		super.setStatementExecuteBatchAfterLogEnabled( false );
		super.setStatementExecuteUpdateAfterLogEnabled( false );
	}

	@Override
	protected void connectionLog( String message ){
		connectionLogger.debug( formatMessage( message ) );
	}

	@Override
	protected void statementLog( String message ){
		statementLogger.debug( formatMessage( message ) );
	}

	@Override
	protected void statementLogError( String message, Throwable error ){
		statementLogger.error( formatMessage( message ) );
	}

	@Override
	protected void resultSetLog( String message ){
		resultSetLogger.debug( formatMessage( message ) );
	}

	@Override
	protected void resultSetLogError( String message, Throwable error ){
		resultSetLogger.error( formatMessage( message ) );
	}

	@Override
	public String getDataSourceLoggerName(){
		return dataSourceLoggerName;
	}

	@Override
	public void setDataSourceLoggerName( String loggerName ){
		this.dataSourceLoggerName = loggerName;
	}

	@Override
	public String getConnectionLoggerName(){
		return connectionLoggerName;
	}

	@Override
	public void setConnectionLoggerName( String loggerName ){
		this.connectionLoggerName = loggerName;
		this.connectionLogger = LogManager.getLog( loggerName );
	}

	@Override
	public String getStatementLoggerName(){
		return statementLoggerName;
	}

	@Override
	public void setStatementLoggerName( String loggerName ){
		this.statementLoggerName = loggerName;
		this.statementLogger = LogManager.getLog( loggerName );
	}

	@Override
	public String getResultSetLoggerName(){
		return resultSetLoggerName;
	}

	@Override
	public void setResultSetLoggerName( String loggerName ){
		this.resultSetLoggerName = loggerName;
		this.resultSetLogger = LogManager.getLog( loggerName );
	}

	public boolean isConnectionLogErrorEnabled(){
		return true;
	}

	@Override
	public boolean isDataSourceLogEnabled(){
		return false;
	}

	@Override
	public boolean isConnectionLogEnabled(){
		return false;
	}

	@Override
	public boolean isStatementLogEnabled(){
		return true;
		// return statementLogger.isDebugEnabled() && super.isStatementLogEnabled();
	}

	@Override
	public boolean isResultSetLogEnabled(){
		return false;
	}

	@Override
	public boolean isResultSetLogErrorEnabled(){
		return true;
	}

	@Override
	public boolean isStatementLogErrorEnabled(){
		return true;
	}

	@Override
	public boolean isStatementParameterSetLogEnabled(){
		return false;
	}

	private static String formatMessage( String message ){
		return message.replace( '\n', ' ' );
	}

}
