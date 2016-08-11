package com.yoya.rdf.router.application.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.yoya.rdf.router.application.IApplication;

/**
 * 基于本地的内存application
 * @author yongda
 *
 */
public final class LocalApplication implements IApplication{

	private Map<String, Object>	_DATA;
	
	public LocalApplication() {
		this._DATA = new HashMap<>();
	}
	
	@Override
	public IApplication setAttribute(String name, Object value) {
		this._DATA.put(name, value);
		return this;
	}

	@Override
	public <T> T getAttribute(String name) {
		return getAttribute(name, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name, T defValue) {
		return this._DATA.get(name)!=null?(T)this._DATA.get(name):defValue;
	}

	@Override
	public Set<String> getAttributeNames() {
		return this._DATA.keySet();
	}

	@Override
	public IApplication removeAttribute(String name) {
		this._DATA.remove(name);
		return this;
	}
	
}
