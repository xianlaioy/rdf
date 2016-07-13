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
package com.yoya.rdf.plugin;

/**
 * Created by baihw on 16-7-05.
 *
 * SMS(Short Messaging Service)短讯服务支持插件规范接口。
 */
public interface ISMS extends IPlugin{

	/**
	 * 发送短信
	 * 
	 * @param templateId 短信模板唯一标识
	 * @param templateData 短信模板填充数据
	 * @param mobiles 接收手机号码列表
	 */
	void sendMessage( String templateId, String templateData, String... mobiles );

} // end class
