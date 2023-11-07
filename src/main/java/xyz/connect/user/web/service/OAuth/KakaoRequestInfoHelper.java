package xyz.connect.user.web.service.OAuth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import xyz.connect.user.web.dto.params.KaKaoInfoParam;
import xyz.connect.user.web.dto.params.KakaoToken;


@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoRequestInfoHelper implements RequestInfoHelper {

    private final RestTemplate restTemplate;
    private static final String GRANT_TYPE = "authorization_code";

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    public String requestAccessToken(String code) {
        HttpHeaders httpHeaders = new HttpHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String url = "https://kauth.kakao.com/oauth/token";

        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        params.add("code", code);
        params.add("grant_type", GRANT_TYPE);
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, httpHeaders);
        log.info("url = {}", url);
        log.info("params = {}", params);
        KakaoToken response = restTemplate.postForObject(url, request, KakaoToken.class);

        return response.getAccessToken();
    }


    public KaKaoInfoParam requestInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.set("Authorization", "Bearer " + accessToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("property_keys", "[\"kakao_account.email\", \"kakao_account.profile\"]");

        HttpEntity<?> request = new HttpEntity<>(body, httpHeaders);
        return restTemplate.postForObject(url, request, KaKaoInfoParam.class);
    }


    public KaKaoInfoParam request(String code) {
        log.info("code ={}", code);
        String accessToken = requestAccessToken(code);
        KaKaoInfoParam kaKaoInfoParam = requestInfo(accessToken);
        return kaKaoInfoParam;
    }
}
