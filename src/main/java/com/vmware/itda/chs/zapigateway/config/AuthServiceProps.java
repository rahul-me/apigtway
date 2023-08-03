package com.vmware.itda.chs.zapigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.vmware.itda.chs.zapigateway.constants.TokenStrictness;

@Configuration
@ConfigurationProperties(prefix = "authservice")
public class AuthServiceProps {

	private TokenStrictness tokenStrictness;

	private String excludedPath;

	private String validateTokenPath;

	public TokenStrictness getTokenStrictness() {
		return tokenStrictness;
	}

	public void setTokenStrictness(TokenStrictness tokenStrictness) {
		this.tokenStrictness = tokenStrictness;
	}

	public String getExcludedPath() {
		return excludedPath;
	}

	public void setExcludedPath(String excludedPath) {
		this.excludedPath = excludedPath;
	}

	public String getValidateTokenPath() {
		return validateTokenPath;
	}

	public void setValidateTokenPath(String validateTokenPath) {
		this.validateTokenPath = validateTokenPath;
	}

}
