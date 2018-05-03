package com.croxx.nbiot.service;

import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.AuthOutDTO;
import com.huawei.iotplatform.client.dto.AuthRefreshInDTO;
import com.huawei.iotplatform.client.dto.AuthRefreshOutDTO;
import com.huawei.iotplatform.client.dto.ClientInfo;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class NBIoTAuthService {


    static private String accessToken;
    static private String refreshToken;
    static private Date expirationDate;


    @Autowired
    NBIoTService nbIoTService;

    private void login() {
        try {
            NorthApiClient northApiClient = nbIoTService.getNorthApiClient();
            Authentication auth = new Authentication(northApiClient);
            AuthOutDTO authOutDTO = auth.getAuthToken();
            accessToken = authOutDTO.getAccessToken();
            refreshToken = authOutDTO.getRefreshToken();
            expirationDate = new Date(System.currentTimeMillis() + authOutDTO.getExpiresIn() * 1000);
        } catch (NorthApiException northApiException) {
            northApiException.printStackTrace();
        }
    }

    private void refresh() {
        try {
            NorthApiClient northApiClient = nbIoTService.getNorthApiClient();
            Authentication auth = new Authentication(northApiClient);
            AuthRefreshInDTO arid = new AuthRefreshInDTO();
            arid.setAppId(nbIoTService.getAppId());
            arid.setSecret(nbIoTService.getSecret());
            arid.setRefreshToken(refreshToken);
            AuthRefreshOutDTO authRefreshOutDTO = auth.refreshAuthToken(arid);

            accessToken = authRefreshOutDTO.getAccessToken();
            refreshToken = authRefreshOutDTO.getRefreshToken();
            expirationDate = new Date(System.currentTimeMillis() + authRefreshOutDTO.getExpiresIn() * 1000);
        } catch (NorthApiException northApiException) {
            northApiException.printStackTrace();
        }
    }

    public String getAccessToken() {

        if (accessToken == null || expirationDate == null || refreshToken == null) {
            login();
            return accessToken;
        } else {
            if (new Date().before(expirationDate)) {
                return accessToken;
            } else {
                refresh();
                return accessToken;
            }
        }
    }

}
