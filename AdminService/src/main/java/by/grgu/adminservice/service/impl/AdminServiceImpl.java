package by.grgu.adminservice.service.impl;

import by.grgu.adminservice.dto.AccountDTO;
import by.grgu.adminservice.service.AdminService;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {
    private final RestTemplate restTemplate;
    private static final String API_GATEWAY_URL = "http://localhost:8082/";

    public AdminServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<AccountDTO> getAllAccounts() {
        String url = API_GATEWAY_URL + "accounts";
        ResponseEntity<List<AccountDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    public ResponseEntity<Void> updateAccountStatus(String username, Map<String, String> status) {
        String url = API_GATEWAY_URL + "accounts/" + username + "/status";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        status.forEach(formData::add);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        return restTemplate.postForEntity(url, requestEntity, Void.class);
    }

    public AccountDTO getAccountData(String username) {
        String url = API_GATEWAY_URL + "accounts/" + username + "/data";
        return restTemplate.getForObject(url, AccountDTO.class);
    }
}
