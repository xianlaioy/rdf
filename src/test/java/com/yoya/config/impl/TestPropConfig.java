package com.yoya.config.impl;

import java.util.HashMap;

import com.yoya.config.IConfig;

public class TestPropConfig {
	public static void main(String[] args) {
		IConfig config = new PropConfig(new HashMap<String,String>());
		
		System.out.println(config.toString());
		
	}
}
