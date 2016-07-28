package com.yoya.config.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.yoya.config.AbstractConfig;

/**
 * 20160728
 * @author yongda 基于配置文件props的配置对象实现
 */
public class PropConfig extends AbstractConfig {

	public static final String DEF_FILENAME = "rdfconfig.properties";

	public PropConfig(Map<String, String> paramMap) {
		
		String filePath = paramMap.get("propFile");
		filePath = (filePath==null||filePath.length()==0)?DEF_FILENAME:filePath;
		
		Properties prop = new Properties();
		InputStream in = getClass().getResourceAsStream("/".concat(filePath));

		try {
			prop.load(in);
			Set<?> setValue = prop.keySet();
			synchronized( _data ){
				this._data.clear();
				setValue.forEach( ( str ) -> {
					String keyStr = String.valueOf(str);
					String[] keys = keyStr.split("\\.");
					if(keys.length>1){
						int index = keyStr.indexOf(".");
						String group = keys[0];
						String key = keyStr.substring(index+1);
						String value = prop.getProperty(keyStr);
						putValue( group, key, value );
					}
				});
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
