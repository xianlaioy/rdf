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

import com.yoya.sql.IPageInfo;
import com.yoya.sql.IRecordList;

/**
 * Created by baihw on 16-4-20.
 *
 * 一个简单的分页信息实现。
 */
final class SimplePageInfo implements IPageInfo{

	// 当前页码
	private int			_pageNO;
	// 每页数据大小
	private int			_pageSize;
	// 总页数
	private int			_pageCount;
	// 总记录数
	private int			_rowCount;
	// 当前页数据
	private IRecordList	_pageData;

	void setPageNO( int pageNO ){
		this._pageNO = pageNO;
	}

	@Override
	public int getPageNO(){
		return this._pageNO;
	}

	void setPageSize( int pageSize ){
		this._pageSize = pageSize;
	}

	@Override
	public int getPageSize(){
		return this._pageSize;
	}

	void setPageCount( int pageCount ){
		this._pageCount = pageCount;
	}

	@Override
	public int getPageCount(){
		return this._pageCount;
	}

	void setRowCount( int rowCount ){
		this._rowCount = rowCount;
	}

	@Override
	public int getRowCount(){
		return this._rowCount;
	}

	void setPageData( IRecordList pageData ){
		this._pageData = pageData;
	}

	@Override
	public IRecordList getPageData(){
		return this._pageData;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append( "{\"pageSize\":" ).append( this._pageSize );
		sb.append( ",\"pageCount\":" ).append( this._pageCount );
		sb.append( ",\"rowCount\":" ).append( this._rowCount );
		sb.append( "}" );
		return sb.toString();
	}

}
