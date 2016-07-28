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
package com.yoya.struct;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by baihw on 16-7-20.
 *
 * 全局唯一标识符对象。
 */
public final class ObjectId{

	private static final int	_charLen	= 32;
	private static final int	_byteLen	= 16;

	private final int			_time;
	private final int			_machine;
	private final int			_inc;
	private final int			_shard;
	private final int			_shardSize;
	private final boolean		_isNew;

	/**
	 * 创建一个默认的全局唯一标识
	 */
	public ObjectId(){
		_time = ( int )( System.currentTimeMillis() / 1000 );
		_machine = _genMachine;
		_inc = _nextInc.getAndIncrement();
		_shard = 0;
		_shardSize = 0;
		_isNew = true;
	}

	/**
	 * 根据指定的分区及分区大小信息创建一个唯一标识
	 * 
	 * @param shard 分区标识,取值范围:0-32767
	 * @param shardSize 分区下的存储区域大小标识,取值范围:0-65535
	 */
	public ObjectId( int shard, int shardSize ){
		if( 0 > shard || 32767 < shard )
			throw new RuntimeException( "分区大小范围在0-32767。" );
		if( 0 > shardSize || 65535 < shardSize )
			throw new RuntimeException( "子分区大小范围在0-65535。" );
		_time = ( int )( System.currentTimeMillis() / 1000 );
		_machine = _genMachine;
		_inc = _nextInc.getAndIncrement();
		_shard = shard;
		_shardSize = shardSize;
		_isNew = true;
	}

	/**
	 * 从一个已有的唯一标识字符串中解析出唯一标识对象
	 * 
	 * @param objectIdStr
	 */
	public ObjectId( String objectIdStr ){
		if( !isValid( objectIdStr ) )
			throw new RuntimeException( "无效的标识字符串，长度必须是32位，只能包含0-9,a-f,A-F字符！" );

		byte b[] = new byte[_byteLen];
		for( int i = 0; i < _byteLen; i++ ){
			b[i] = ( byte )Integer.parseInt( objectIdStr.substring( i * 2, ( i * 2 ) + 2 ), 16 );
		}

		ByteBuffer bb = ByteBuffer.wrap( b );
		_time = bb.getInt();
		_machine = bb.getInt();
		_inc = bb.getInt();
		final int tempShardSize = bb.getInt();
		_shard = ( tempShardSize >> 16 );
		_shardSize = ( tempShardSize ^ ( _shard << 16 ) );
		_isNew = false;
	}

	/**
	 * 检查指定的字符串是否为有效的唯一标识。
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isValid( String s ){
		if( null == s )
			return false;

		final int len = s.length();
		if( _charLen != len )
			return false;

		for( int i = 0; i < len; i++ ){
			char c = s.charAt( i );
			if( c >= '0' && c <= '9' )
				continue;
			if( c >= 'a' && c <= 'f' )
				continue;
			if( c >= 'A' && c <= 'F' )
				continue;
			return false;
		}
		return true;
	}

	/**
	 * 将当前标识符对象元素整合到二进行数组中。
	 * 
	 * @return
	 */
	public byte[] toByteArray(){
		byte b[] = new byte[_byteLen];
		ByteBuffer bb = ByteBuffer.wrap( b );
		bb.putInt( _time );
		bb.putInt( _machine );
		bb.putInt( _inc );
		bb.putInt( ( ( _shard << 16 ) | _shardSize ) );
		return b;
	}

	/**
	 * 获取分区信息
	 * 
	 * @return
	 */
	public int getShard(){
		return _shard;
	}

	/**
	 * 获取分区大小信息
	 * 
	 * @return
	 */
	public int getShardSize(){
		return _shardSize;
	}

	/**
	 * 获取当前唯一标识的生成日期
	 * 
	 * @return
	 */
	public Date getDate(){
		return new Date( _time * 1000L );
	}

	/**
	 * 获取当前唯一标识的生成时间戳
	 * 
	 * @return
	 */
	public long getTimestamp(){
		return _time * 1000L;
	}

	public int getMachine(){
		return _machine;
	}

	public int getInc(){
		return _inc;
	}

	/**
	 * 当前的唯一标识符对象是否为新生成的。
	 * 
	 * @return
	 */
	public boolean isNew(){
		return _isNew;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + _inc;
		result = prime * result + _machine;
		result = prime * result + _shard;
		result = prime * result + _shardSize;
		result = prime * result + _time;
		return result;
	}

	@Override
	public boolean equals( Object objId ){
		if( this == objId )
			return true;
		if( null == objId || getClass() != objId.getClass() )
			return false;

		ObjectId other = ( ObjectId )objId;
		return _time == other._time && _machine == other._machine && _inc == other._inc && _shard == other._shard && _shardSize == other._shardSize;
	}

	/**
	 * 转换为简单类型，不包含分区分片信息，长度为24位。
	 * 
	 * @return
	 */
	public String toSimple(){
		return toString().substring( 0, 24 );
	}

	/**
	 * 通过简单类型的id字符串获取唯一标识对象。
	 * 
	 * @param objectIdStr
	 * @return
	 */
	public ObjectId fromSimple( String objectIdStr ){
		return new ObjectId( objectIdStr + "00000000" );
	}

	public String toString(){
		byte[] b = toByteArray();
		StringBuilder buf = new StringBuilder( 32 );
		for( int i = 0; i < b.length; i++ ){
			int x = b[i] & 0xFF;
			String s = Integer.toHexString( x );
			if( s.length() == 1 )
				buf.append( "0" );
			buf.append( s );
		}
		return buf.toString();
	}

	protected String toHexString(){
		byte[] bs = toByteArray();
		final StringBuilder buf = new StringBuilder( 32 );
		for( final byte b : bs ){
			buf.append( String.format( "%02x", b & 0xff ) );
		}

		return buf.toString();
	}

	private static final AtomicInteger	_nextInc	= new AtomicInteger( ( new java.util.Random() ).nextInt() );
	private static final int			_genMachine;

	static{
		try{
			// build a 2-byte machine piece based on NICs info
			int machinePiece;
			{
				try{
					StringBuilder sb = new StringBuilder();
					Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
					while( e.hasMoreElements() ){
						NetworkInterface ni = e.nextElement();
						sb.append( ni.toString() );
					}
					machinePiece = sb.toString().hashCode() << 16;
				}catch( Throwable e ){
					// exception sometimes happens with IBM JVM, use random
					// _LOG.debug(e.getMessage(), e);
					machinePiece = ( new Random().nextInt() ) << 16;
				}
				// _LOG.debug( "machine piece post: " + Integer.toHexString( machinePiece ) );
			}

			// add a 2 byte process piece. It must represent not only the JVM
			// but the class loader.
			// Since static var belong to class loader there could be collisions
			// otherwise
			final int processPiece;
			{
				int processId = new java.util.Random().nextInt();
				try{
					processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
				}catch( Throwable t ){
				}

				ClassLoader loader = ObjectId.class.getClassLoader();
				int loaderId = loader != null ? System.identityHashCode( loader ) : 0;

				StringBuilder sb = new StringBuilder();
				sb.append( Integer.toHexString( processId ) );
				sb.append( Integer.toHexString( loaderId ) );
				processPiece = sb.toString().hashCode() & 0xFFFF;
				// _LOG.debug( "process piece: " + Integer.toHexString( processPiece ) );
			}

			_genMachine = machinePiece | processPiece;
			// _LOG.debug( "machine : " + Integer.toHexString( _genmachine ) );
		}catch( Exception e ){
			throw new RuntimeException( e );
		}
	}

} // end class
