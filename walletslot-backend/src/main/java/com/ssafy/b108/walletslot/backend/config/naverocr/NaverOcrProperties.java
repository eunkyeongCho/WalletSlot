package com.ssafy.b108.walletslot.backend.config.naverocr;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.ocr")
public class NaverOcrProperties {

    private final String invokeUrl;
    private final String receiptPath;   // /custom/v1/{projectId}/{privateKey}/document/receipt
    private final String projectId;     // 콘솔 Invoke URL의 첫 세그먼트(숫자)
    private final String privateKey;    // 콘솔 Invoke URL의 두 번째 세그먼트(긴 해시)
    private final String secret;        // X-OCR-SECRET
    private final String version;       // "V2"
    private final boolean enabled;

    public NaverOcrProperties(
            String invokeUrl,
            String receiptPath,
            String projectId,
            String privateKey,
            String secret,
            String version,
            boolean enabled
    ) {
        this.invokeUrl = invokeUrl;
        this.receiptPath = receiptPath;
        this.projectId = projectId;
        this.privateKey = privateKey;
        this.secret = secret;
        this.version = version;
        this.enabled = enabled;
    }

    public String getInvokeUrl()   { return invokeUrl; }
    public String getReceiptPath() { return receiptPath; }
    public String getProjectId()   { return projectId; }
    public String getPrivateKey()  { return privateKey; }
    public String getSecret()      { return secret; }
    public String getVersion()     { return version; }
    public boolean isEnabled()     { return enabled; }
}
