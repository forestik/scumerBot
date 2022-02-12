package com.crypto.dto;

public class TaskMessageDto extends MessageDto {

    public TaskMessageDto(String name, String url) {
        setMessage(name, url);
    }

    public void setMessage(String name, String url) {
        message = "<a href=\"" + url + "\">" + name + "</a>";
    }

}
