package com.vmware.itda.chs.zapigateway.filter;

import java.util.Base64;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.HttpServletRequestWrapper;
import com.vmware.itda.chs.zapigateway.config.AuthServiceProps;
import com.vmware.itda.chs.zapigateway.constants.TokenStrictness;
import com.vmware.itda.chs.zapigateway.dto.BaseDto;
import com.vmware.itda.chs.zapigateway.token.TokenManager;

@Component
public class AuthFilter extends ZuulFilter {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ObjectMapper mapper = new ObjectMapper();
	
	private static String BEARER_VALUE_PREFIX = "Bearer "; 

	@Autowired
	private AuthServiceProps authServiceProps;

	@Autowired
	private TokenManager tokenManager;

	@Value("${chs.data.base.path.regex}")
	private String dataServiceBasePathRegex;

	@Value("${chs.data2.base.path.regex}")
	private String dataService2BasePathRegex;

	@Value("${chs.user.base.path.regex}")
	private String userServiceBasePathRegex;

	@Value("${tam.base.path.regex}")
	private String tamServiceBasePathRegex;

	@Value("${tam-pipa.base.path.regex:/cxs/api/v1/tam/pipa/kbdetails.}")
	private String tamPipaServiceBasePathRegex;

	@Value("${forward-to-new-tam-url}")
	private Boolean forwardToNewTamUrl;
	
	@Value("${chs.ent.base.path.regex}")
	private String entServiceBasePathRegex;

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public boolean shouldFilter() {
		if (TokenStrictness.ALWAYS_PASS == authServiceProps.getTokenStrictness()) {
			return false;
		}
		return true;
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();

		HttpServletRequest request = ctx.getRequest();

		String authHeaderValue = request.getHeader("Authorization");
		if (StringUtils.isNotBlank(authHeaderValue)
				&& TokenStrictness.PASS_IF_TOKEN_NON_NULL == authServiceProps.getTokenStrictness()) {
			return null;
		}

		String uriString = request.getRequestURI();
		if (!(uriString.matches(dataServiceBasePathRegex) || uriString.matches(dataService2BasePathRegex) 
				|| uriString.matches(userServiceBasePathRegex) || uriString.matches(tamServiceBasePathRegex) 
				|| uriString.matches(tamPipaServiceBasePathRegex) || uriString.matches(entServiceBasePathRegex))) {
			return null;
		}

		if (authHeaderValue == null) {
			authHeaderValue = "";
		}

		String domain =request.getHeader("domain");

		if (domain == null) {
			domain = "";
		}

		BaseDto tokenCheckResponse = tokenManager.checkToken(domain, authHeaderValue);
		if (tokenCheckResponse.getStatus() >= 400) {
			logger.error("Token validation failed. error message :{}", tokenCheckResponse.getMessage());
			buildRequestContextWithResponse(ctx, tokenCheckResponse);
		}

		if (uriString.matches(tamServiceBasePathRegex) && forwardToNewTamUrl != null && forwardToNewTamUrl) {

			HttpServletRequestWrapper httpServletRequestWrapper = new HttpServletRequestWrapper(ctx.getRequest()) {
				public String getRequestURI() {
					if (tamPipaServiceBasePathRegex.indexOf(".") > 0) {
						return tamPipaServiceBasePathRegex.substring(0, tamPipaServiceBasePathRegex.indexOf("."));
					}
					return tamPipaServiceBasePathRegex;
				}
			};
			ctx.setRequest(httpServletRequestWrapper);
			HttpServletRequest request1 = ctx.getRequest();
			logger.info("PreFilter: "
					+ String.format("%s request to %s", request1.getMethod(), request1.getRequestURI().toString()));
		}
		
		if (StringUtils.isNotBlank(authHeaderValue)) {
			try {
				String token = authHeaderValue.substring(BEARER_VALUE_PREFIX.length());
				String userId = getUserId(token);

				ctx.addZuulRequestHeader("userId", userId);
			} catch (Exception e) {
				logger.warn("Unable to fetch Authorization header having Bearer token, {}", e.getMessage());
			}
		}

		return null;
	}
	
	private String getUserId(String token) {
		if(Objects.nonNull(token)) {
			JSONObject payload;
			try {
				payload = new JSONObject(decode(token.split("\\.")[1]));
			return payload.getString("sub");
			} catch (JSONException e) {
				logger.error("Error getting user id from token, Error: {}", e.getMessage());
			}
		}
		return null;
	}
	
	private static String decode(String encodedString) {
	    return new String(Base64.getUrlDecoder().decode(encodedString));
	}

	private void buildRequestContextWithResponse(RequestContext ctx, BaseDto tokenCheckResponse) {
		ctx.setResponseStatusCode(tokenCheckResponse.getStatus());
		try {
			ctx.setResponseBody(mapper.writeValueAsString(tokenCheckResponse));
		} catch (JsonProcessingException e) {
			logger.error("Failed to write json for failed authentication response");
		}
		ctx.addZuulResponseHeader("Content-Type", "application/json");
		ctx.setSendZuulResponse(false);
	}

}
