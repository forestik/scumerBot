package com.crypto.dto;

public class TokensLeftMessageDto extends MessageDto {

    public TokensLeftMessageDto(String projectName, String userName, String tokensLeft) {
        setMessage(projectName, userName, tokensLeft);
    }

    public void setMessage(String projectName, String userName, String tokensLeft) {
        this.message = "<pre>" + projectName + " | " + userName + " | " + tokensLeft + "</pre>";
    }
}
