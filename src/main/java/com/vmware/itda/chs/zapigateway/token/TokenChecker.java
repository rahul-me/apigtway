package com.vmware.itda.chs.zapigateway.token;

import com.vmware.itda.chs.zapigateway.dto.BaseDto;

public interface TokenChecker {
	public BaseDto checkToken(String domain, String token);
}
