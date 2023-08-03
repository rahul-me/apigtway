package com.vmware.itda.chs.zapigateway.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.itda.chs.zapigateway.config.AuthServiceProps;
import com.vmware.itda.chs.zapigateway.dto.BaseDto;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class TokenManager implements TokenChecker {

	private static final String AN_ERROR_HAS_OCCURRED = "An error has occurred";

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ObjectMapper mapper = new ObjectMapper();
	private final OkHttpClient client = new OkHttpClient();
	private final RequestBody emptyBody = RequestBody.create(new byte[0]);

	@Autowired
	private AuthServiceProps authServiceProps;

	@Value("${chs.auth.url}")
	private String chsAuthUrl;

	@Override
	public BaseDto checkToken(String domain, String authHeaderValue) {

		String url = chsAuthUrl + authServiceProps.getValidateTokenPath();
		Request request = new Request.Builder().addHeader("Authorization", authHeaderValue)
				.addHeader("domain", domain).url(url)
				.method("POST", emptyBody).build();
		try (Response response = client.newCall(request).execute()) {
			return mapper.readValue(response.body().byteStream(), BaseDto.class);
		} catch (Exception e) {
			logger.error("Exception occurred while validating token: {}", e.getMessage());
			BaseDto dto = new BaseDto();
			dto.setMessage(AN_ERROR_HAS_OCCURRED);
			dto.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return dto;
		}
	}

}
