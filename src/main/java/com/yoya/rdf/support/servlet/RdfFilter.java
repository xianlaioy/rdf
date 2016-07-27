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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import com.yoya.config.IConfig;
import com.yoya.rdf.Rdf;
import com.yoya.rdf.log.ILog;
import com.yoya.rdf.log.LogManager;
import com.yoya.rdf.router.IHttpRequest;
import com.yoya.rdf.router.IHttpResponse;
import com.yoya.rdf.router.IRouter;
import com.yoya.rdf.router.impl.SimpleHttpResponse;
import com.yoya.rdf.router.impl.WebRouter;
import com.yoya.rdf.router.session.ISession;
import com.yoya.rdf.router.session.impl.MysqlSession;
import com.yoya.rdf.service.Service;

/**
 * Created by baihw on 16-4-15.
 *
 * 其它环境支持--servlet环境提供Filter接入支持。
 */
public class RdfFilter implements Filter{

	// 日志处理对象。
	private static final ILog						_LOG			= LogManager.getLog( RdfFilter.class );

//	// 上下文环境路径长度。
//	private int		_contextPathLen	= -1;
	// 请求路径在路由时需要跳过的字符长度。
	private int										_pathSkipLen	= -1;

	// 忽略的请求地址。
	private String									_ignoreUrl		= null;

	// 路由管理器。
	private IRouter<IHttpRequest, IHttpResponse>	_ROUTER			= null;

	@Override
	public void init( FilterConfig filterConfig ) throws ServletException{

		// 预设允许开发人员配置的系统参数
		String configImpl = filterConfig.getInitParameter( "configImpl" );
		if( null == configImpl || 0 == ( configImpl = configImpl.trim() ).length() ){ throw new RuntimeException( "configImpl参数必须正确设置！" ); }
		if( "MysqlConfig".equals( configImpl ) ){
			configImpl = "com.yoya.config.impl.MysqlConfig";
		}
		
		//cd0281 20160727 配置类的动态加载
		Map<String,String> paramMap = new HashMap<String, String>();
		for (Enumeration<String> e = filterConfig.getInitParameterNames(); e.hasMoreElements();){
			String key = e.nextElement();
			paramMap.put(key, filterConfig.getInitParameter(key));
		}
		
		try {
			Class<?> clazz = Class.forName(configImpl);
			Class<?>[] parameterTypes={Map.class};
			Constructor<?> constructor=clazz.getConstructor(parameterTypes); 
			IConfig configObj = (IConfig) constructor.newInstance(paramMap);
			// 调用框架初始化动作。
			Rdf.me().init( configObj );
			// 检查启动服务。
			Service.impl().start();
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		// 如果将框架当作插件使用与其它框架进行集成时，由于拦截的不是根路径，所以需要计算路由时需要跳过的字符长度。
		// 此处暂时未做自动发现拦截路径的逻辑，所以暂由开发人员手工配置需要跳过的字符长度。
		// 如： 拦截路径为：/do，则跳过字符长度为3。
		String pathSkipLen = filterConfig.getInitParameter( "pathSkipLen" );
		if( null == pathSkipLen || 0 == ( pathSkipLen = pathSkipLen.trim() ).length() ){
			_pathSkipLen = 0;
		}else{
			_pathSkipLen = Integer.parseInt( pathSkipLen );
		}
		_LOG.info( "pathSkipLen: " + _pathSkipLen );

		// 开发人员配置的忽略请求地址。当使用了servlet容器时，通常不需要自己处理静态文件的访问请求，所以应该配置静态文件为忽略路径。
		// 一个示例的静态文件地址忽略配置如： “.+(?i)\.(html|css|js|json|ico|png|gif|woff|map)$”
		String ignoreUrl = Rdf.me().getConfig( "web", "ignoreUrl" );
		if( null != ignoreUrl && 0 != ( ignoreUrl = ignoreUrl.trim() ).length() ){
			this._ignoreUrl = ignoreUrl;
		}
		_LOG.info( "ignoreUrl: " + ignoreUrl );

		// 初始化路由管理器。
		_ROUTER = new WebRouter();
	}

	@Override
	public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException{

		HttpServletRequest req = ( HttpServletRequest )request;
		HttpServletResponse res = ( HttpServletResponse )response;

		// 获取请求的资源相对路径。
		String requestPath = req.getServletPath(); // req.getRequestURI();

		if( null != _ignoreUrl && ( "/".equals( requestPath ) || requestPath.matches( _ignoreUrl ) ) ){
			// 忽略根路径及开发人员指定的忽略请求路径。
			chain.doFilter( request, response );
			return;
		}

		req.setCharacterEncoding( Rdf.me().getEncoding() );
		res.setCharacterEncoding( Rdf.me().getEncoding() );

//		// 拼装basePath,存入request属性中,方便页面直接使用.
//		String path = req.getContextPath();
//		String basePath = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + path + "/";
//		req.setAttribute( "__BASEPATH__", basePath );

//		// 计算上下文环境路径长度。
//		if( -1 == _contextPathLen ){
//			String contextPath = req.getServletContext().getContextPath();
//			_contextPathLen = ( null == contextPath || "/".equals( contextPath ) ? 0 : contextPath.length() );
//			_pathSkipLen += _contextPathLen;
//		}

		// 计算框架中使用的实际请求路径
		if( 0 < _pathSkipLen ){
			requestPath = requestPath.substring( _pathSkipLen );
		}

		// 创建框架适配的请求、响应对象。
		HttpServletRequestWrapper ireq = new HttpServletRequestWrapper( req );
		ireq.setPath( requestPath );
		IHttpResponse ires = new SimpleHttpResponse();

		// 调用框架路由请求处理逻辑。
		_ROUTER.route( ireq, ires );

		// 设置响应代码
		res.setStatus( ires.getStatus() );

		// 设置响应头信息
		ires.getHeader().forEach( ( key, value ) -> {
			res.setHeader( key, value );
		} );

		// 设置cookie响应信息
		ires.getCookie().forEach( ( key, value ) -> {
			res.addCookie( new Cookie( key, value ) );
		} );

		// 设置会话标识持久化Cookie
		if( ireq.hasSession() ){
			ISession session = ireq.getSession();
			String ck_session_id;
			if( session.isNew() || null == ( ck_session_id = ireq.getCookie( ISession.KEY_SESSIONID ) ) || 0 == ( ck_session_id = ck_session_id.trim() ).length() ){
				// 设置浏览器会话标识Cookie，浏览器关闭失效。
				Cookie sessionCookie = new Cookie( ISession.KEY_SESSIONID, ireq.getSession().getId() );
//				sessionCookie.setDomain( null );
				sessionCookie.setPath( "/" );
				sessionCookie.setMaxAge( -1 );
				sessionCookie.setHttpOnly( true );
				sessionCookie.setSecure( false );
				res.addCookie( sessionCookie );
			}

			// 如果是MysqlSession，为避免频繁操作数据库，所以在请求结束时手工调用同步方法同步session数据到数据库。
			if( session instanceof MysqlSession ){
				( ( MysqlSession )session ).sync();
			}
		}

		// 根据响应数据类型进行响应处理。
		IHttpResponse.Type resType = ires.getDataType();
		if( null == resType ){
			resType = IHttpResponse.Type.TEXT;
		}

		// 根据响应数据类型设置指定的头信息。
		if( !ires.hasHeader( IHttpResponse.HEAD_CONTENT_TYPE ) )
			res.setHeader( IHttpResponse.HEAD_CONTENT_TYPE, resType.getContentType() );

		// 下载响应特殊处理
		if( IHttpResponse.Type.STREAM == resType ){
			try( InputStream inStream = ires.getDataInputStream(); ServletOutputStream outStream = response.getOutputStream(); ){
				ByteStreams.copy( inStream, outStream );
				outStream.flush();
			}
		}else{
			// 禁止浏览器缓存
			res.setHeader( "Pragma", "no-cache" );
			res.setHeader( "Cache-Control", "no-cache" );
			res.setDateHeader( "Expires", 0 );

			try( PrintWriter writer = response.getWriter(); ){
				writer.write( ires.getDataString() );
				writer.flush();
			}
		}

	}

	@Override
	public void destroy(){

		_LOG.info( "before destroy..." );

		// 检查停止服务。
		Service.impl().stop();

		// 框架退出，释放资源。
		Rdf.me().destroy();

		_LOG.info( "after destroy." );

	}

}
