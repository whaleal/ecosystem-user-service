package com.careydevelopment.ecosystem.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vonage.client.VonageClient;
import com.vonage.client.verify.CheckResponse;
import com.vonage.client.verify.VerifyResponse;
import com.vonage.client.verify.VerifyStatus;

/**
 * Using Vonage's verification service because if the user doesn't verify, then
 * we don't get charged for the SMS.
 * 
 * If we just send a text message and check the code like we do with email, we
 * get charged no matter what.
 *
 * 短信验证
 *
 */
@Service
public class SmsService {

    private static final Logger LOG = LoggerFactory.getLogger(SmsService.class);

    private static final String BRAND_NAME = "Carey Development";

    @Value("${vonage.api.key}")
    private String apiKey;

    @Value("${vonage.api.secret}")
    private String apiSecret;

    /**
     * 创建Vonage客户端
     * @return
     */
    private VonageClient getClient() {
        VonageClient client = VonageClient.builder().apiKey(apiKey).apiSecret(apiSecret).build();

        return client;
    }

    /**
     * 取消请求
     * @param requestId
     */
    public void cancelRequest(String requestId) {
        VonageClient client = getClient();
        //获取验证客户端，之后取消验证
        client.getVerifyClient().cancelVerification(requestId);
        LOG.debug("Verification cancelled.");
    }

    /**
     * 发送验证消息给对应手机号
     * @param phoneNumber
     * @return
     */
    public String sendValidationCode(String phoneNumber) {
        LOG.debug("Sending validation code to " + phoneNumber);

        //获取Vonage
        VonageClient client = getClient();

        //发送消息给手机号
        VerifyResponse response = client.getVerifyClient().verify(phoneNumber, BRAND_NAME);

        //判断状态
        if (response.getStatus() == VerifyStatus.OK) {
            LOG.debug("Valid request: " + response.getRequestId());
            return response.getRequestId();
        } else {
            LOG.error("Problem sending SMS: " + response.getStatus() + " " + response.getErrorText());
            return null;
        }
    }

    /**
     * 检查验证码
     * @param requestId
     * @param code
     * @return boolean
     */
    public boolean checkValidationCode(String requestId, String code) {
        VonageClient client = getClient();

        CheckResponse response = client.getVerifyClient().check(requestId, code);

        if (response.getStatus() == VerifyStatus.OK) {
            LOG.debug("SMS Verification Successful");
            return true;
        } else {
            LOG.debug("Verification failed: " + response.getErrorText());
            return false;
        }
    }
}
