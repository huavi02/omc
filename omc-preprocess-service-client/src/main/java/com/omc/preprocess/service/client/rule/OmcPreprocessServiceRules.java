package com.omc.preprocess.service.client.rule;

import java.util.Map;

/**
 * Public interface for parse data from collector service
 * 
 * @param data Original data map from collector service
 * 	e.g. {
 * 			"$agent": "Syslog_Collector",
 * 			"$class": "Syslog",
 * 			"$token0": "Jun  9 09:55:05 192.168.0.131 263030: *Jun  9 10:16:22.732: xxx.xxx.xxx.xx %SNMP-3-AUTHFAIL: Authentication failure for SNMP req from host 192.16.1.18",
 * 			"$token1": "192.168.0.131",
 * 			 ...
 * 			"$count": 15,
 * 			"$time": 1509686567
 * 			
 * 		 }
 * 
 * @return map which contains all the concatenated tokens, notice the key should start with store_[database field name] prefix if want to persist in db
 * 	e.g. {
 * 			"store_summary": "concatenate string from different event tokens"
 * 		}
 *
 *  Note: following fields are required fields:
 *  "@agent"
 *  "@class"
 *  "@summary";
 *  "@occurrence"
 *  "@identifier"
 *  "system_dupfield"
 */
public interface OmcPreprocessServiceRules {
	public Map<String, Object> parse(Map<String, Object> data);
}
