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

package com.yoya.rdf.support.servlet;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.yoya.rdf.Rdf;
import com.yoya.rdf.router.AbstractRequest;
import com.yoya.rdf.router.IHttpRequest;
import com.yoya.rdf.router.IRequest;
import com.yoya.rdf.router.session.ISession;
import com.yoya.rdf.router.session.SessionManger;

/**
 * Created by baihw on 16-4-15.
 *
 * 基于HttpServletRequest包装的请求对象
 */
final class HttpServletRequestWrapper extends AbstractRequest implements IHttpRequest{

	/**
	 * 框架中的request对象存放于servletRequest中的属性关键字.
	 */
	static final String					KEY_IREQUEST	= "__IREQUEST__";

	// servlet容器的请求对象
	private final HttpServletRequest	_REQ;

	// 请求编码。
	private final String				_ENCODING;

	// 查询字符串
	private String						_queryString	= null;

	// Cookie存放集合
	private Map<String, String>			_cookies		= null;

	// 请求内容数据。
	private byte[]						_bodyData;
	// 请求内容数据字符串。
	private String						_bodyString;

	// 上传文件列表。
	private List<String>				_uploadFiles;

//  // 请求类型
//  private RequestMethod _requestMethod;

//	// 请求主机名
//	private String			_host;
//
//	// 请求端口
//	private int				_port;
//
//	// 请求资源地址
//	private String			_uri;
//
//	// 主求上下文路径
//	private String			_contextPath;
//
//	// url参数列表字符串
//	private String			_queryString;
//
//	// 请求客户端IP地址
//	private String			_clientIP;
//
//	// 内容类型
//	private String			_contentType;
//
//	// 内容长度
//	private int				_contentLength;

	/**
	 * 构造函数
	 * 
	 * @param request servlet容器的请求对象
	 */
	HttpServletRequestWrapper( HttpServletRequest request ){

		Objects.requireNonNull( request );

		this._REQ = request;
		this._ENCODING = Rdf.me().getEncoding();
		this._queryString = request.getQueryString();

		// 导入header数据到当前request对象中
		Enumeration<String> headerNames = request.getHeaderNames();
		while( headerNames.hasMoreElements() ){
			String headerName = headerNames.nextElement();
			String headerValue = request.getHeader( headerName );
			this._headers.put( headerName, headerValue );
		}

		// 导入parameter数据到当前request对象中
		request.getParameterMap().entrySet().forEach( ( paramsEntry ) -> {
			String key = paramsEntry.getKey();
			String[] values = paramsEntry.getValue();
			String value = null == values ? null : values[0];
			this._parameters.put( key, value );
		} );

		// 处理Forward跳转的旧请求属性数据。
		Object oldRequest = request.getAttribute( KEY_IREQUEST );
		if( null != oldRequest && ( oldRequest instanceof IRequest ) ){
			IRequest oldReq = ( IRequest )oldRequest;
			Set<String> oldAttrNames = oldReq.getAttrNames();
			if( null != oldAttrNames ){
				for( String oldAttrName : oldAttrNames ){
					this._attributes.put( oldAttrName, oldReq.getAttr( oldAttrName ) );
				}
			}
			// 使用Forward之前的请求唯一标识
			this._requestId = oldReq.getRequestId();
		}

		// 导入属性数据
		Enumeration<String> attrNames = request.getAttributeNames();
		while( attrNames.hasMoreElements() ){
			String attrName = attrNames.nextElement();
			if( null == attrName || 0 == ( attrName = attrName.trim() ).length() || attrName.equals( KEY_IREQUEST ) )
				continue;
			this._attributes.put( attrName, request.getAttribute( attrName ) );
		}

		// 将当前请求对象引用存放于servlet对象中。
		request.setAttribute( KEY_IREQUEST, this );
	}

	/**
	 * 具体的请求路径,不包括上下文环境及过滤器拦截路径。
	 * 
	 * @param path 由入口拦截器计算出的不包含上下文环境及拦截路径的真实请求路径。
	 */
	protected void setPath( String path ){
		super.setPath( path );
	}

	public String getQueryString(){
		return this._queryString;
	}

	@Override
	public Map<String, String> getCookies(){
		if( null == this._cookies ){
			_cookies = Collections.unmodifiableMap( buildCookies() );
		}
		return this._cookies;
	}

	@Override
	public String getCookie( String cookieName ){
		return getCookies().get( cookieName );
	}

	@Override
	public String getCookie( String cookieName, String defValue ){
		String value = getCookies().get( cookieName );
		return null == value ? defValue : value;
	}

	@Override
	public boolean hasCookie( String cookieName ){
		return getCookies().containsKey( cookieName );
	}

	@Override
	public String getBody(){
		if( null == this._bodyString ){
			try{
				this._bodyString = new String( getBodyData(), this._ENCODING );
			}catch( UnsupportedEncodingException e ){
				this._bodyString = new String( getBodyData() );
			}
		}
		return this._bodyString;
	}

	@Override
	public byte[] getBodyData(){
		if( null == this._bodyData ){
			int cLen = _REQ.getContentLength();
			try( InputStream inStream = _REQ.getInputStream(); DataInputStream dis = new DataInputStream( inStream ); ){
				_bodyData = new byte[cLen];
				dis.readFully( _bodyData );
			}catch( IOException e ){
				throw new RuntimeException( e );
			}
		}
		return this._bodyData;
	}

	/**
	 * 获取上传文件的相对路径集合的方法。
	 * 
	 * @param uploadDir 上传文件保存的目录名称
	 * @param maxPostSize 上传文件的大小限制
	 * @return 最终保存的相对路径，获取文件最终路径时使用getUploadFile方法传入此路径获取。
	 */
	public List<String> getUploadFiles( String uploadDir, int maxPostSize ){

		if( null != _uploadFiles )
			return _uploadFiles;

		if( null == uploadDir || 0 == ( uploadDir = uploadDir.trim() ).length() )
			throw new RuntimeException( "uploadDir can not be empty!" );

		if( !uploadDir.endsWith( "/" ) ){
			uploadDir = uploadDir.concat( "/" );
		}

		try{
			String path = HttpServletRequestWrapper.class.getResource( "/" ).toURI().getPath();
			String webRootDir = new File( path ).getParentFile().getParentFile().getCanonicalPath();

			File destDir = new File( webRootDir, uploadDir );
			if( !destDir.exists() ){
				if( !destDir.mkdirs() ){ throw new RuntimeException( "Directory " + destDir + " not exists and can not create directory." ); }
			}
			path = destDir.getCanonicalPath();

			MultipartRequest mReq = new MultipartRequest( _REQ, path, maxPostSize, _ENCODING, new DefaultFileRenamePolicy() );

			_uploadFiles = new ArrayList<>();
			Enumeration<?> fileNames = mReq.getFileNames();
			while( fileNames.hasMoreElements() ){
				String paramName = ( String )fileNames.nextElement();
				String fileName = mReq.getFilesystemName( paramName );
				if( null == fileName ){
					// 跳过未上传成功的文件。
					continue;
				}

				// 处理用户上传jsp等恶意文件。
				String fileLowerName = fileName.trim().toLowerCase();
				if( fileLowerName.endsWith( ".jsp" ) || fileLowerName.endsWith( ".jspx" ) ){
					mReq.getFile( paramName ).delete();
					continue;
				}

				// 返回上传成功后的文件相对路径。
				_uploadFiles.add( uploadDir.concat( fileName ) );
			}

			Enumeration<?> parNames = mReq.getParameterNames();
			while( parNames.hasMoreElements() ){
				String parName = ( String )parNames.nextElement();
				this._parameters.put( parName, mReq.getParameter( parName ) );
			}
			return _uploadFiles;
		}catch( Exception e ){
			throw new RuntimeException( e );
		}
	}

	/**
	 * 根据上传文件时返回的相对路径获取文件的最终路径。
	 * 
	 * @param uploadFileName 上传文件时返回的相对路径
	 * @return 最终文件路径。
	 */
	public File getUploadFile( String uploadFileName ){
		try{
			String path = HttpServletRequestWrapper.class.getResource( "/" ).toURI().getPath();
			String webRootDir = new File( path ).getParentFile().getParentFile().getCanonicalPath();

			File destFile = new File( webRootDir, uploadFileName );
			return destFile;
		}catch( Exception e ){
			throw new RuntimeException( e );
		}
	}

	protected Map<String, String> buildCookies(){
		Map<String, String> ckMap = new HashMap<String, String>();
		Cookie[] cookies = _REQ.getCookies();
		if( null != cookies ){
			for( Cookie cookie : cookies ){
				ckMap.put( cookie.getName(), cookie.getValue() );
			}
		}
		return ckMap;
	}

	@Override
	protected ISession buildSession(){
		// 构建会话对象
		// 先从参数中查看是否有会话保持标识
		String sessionId = getParameter( ISession.KEY_SESSIONID );
		if( null == sessionId || 0 == ( sessionId = sessionId.trim() ).length() ){
			// 参数中找不到改为从cookie中获取
			sessionId = getCookie( ISession.KEY_SESSIONID );
		}

		ISession session = SessionManger.me().getSession( sessionId );
		return session;

	}

}
