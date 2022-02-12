package com.crypto.service.impl;

import com.crypto.dto.MembersMessageDto;
import com.crypto.dto.TaskMessageDto;
import com.crypto.service.DefaultService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ClickUpService implements DefaultService {

    private final String clickUpUrl;

    private final String apiToken;

    private final String listId;

    private final RestTemplate restTemplate;

    private final ModelMapper modelMapper;

    public ClickUpService(@Value("${clickUp.url}") String clickUpUrl,
                          @Value("${clickUp.api.token}") String apiToken,
                          @Value("${clickUp.listId}") String listId) {
        this.clickUpUrl = clickUpUrl;
        this.apiToken = apiToken;
        this.listId = listId;
        this.modelMapper = new ModelMapper();
        this.restTemplate = new RestTemplate();
    }

    public List<TaskMessageDto> getTasks() {
        HttpEntity<String> entity = getStringHttpEntity();
        ResponseEntity<Object> exchange = restTemplate
                .exchange(clickUpUrl + "list/" + listId + "/task",
                        HttpMethod.GET, entity, Object.class);
        HashMap<String, List<LinkedHashMap<String, String>>> taskMapList = modelMapper
                .map(exchange.getBody(), new TypeToken<Map<String, List<LinkedHashMap<String, String>>>>() {
                }.getType());
        List<TaskMessageDto> taskMessageDtoList = new LinkedList<>();
        taskMapList.get("tasks").forEach(task -> taskMessageDtoList.add(new TaskMessageDto(task.get("name"), task.get("url"))));
        return taskMessageDtoList;
    }

    public List<MembersMessageDto> getTeams() {
        HttpEntity<String> entity = getStringHttpEntity();
        ResponseEntity<Object> exchange = restTemplate.exchange(clickUpUrl + "/list/" + listId + "/member", HttpMethod.GET, entity, Object.class);
        LinkedHashMap<String, List<LinkedHashMap<String, String>>> teamMapList = modelMapper
                .map(exchange.getBody(), new TypeToken<LinkedHashMap<String, List<LinkedHashMap<String, String>>>>() {
                }.getType());
        List<MembersMessageDto> membersMessageDtoList = new LinkedList<>();
        List<LinkedHashMap<String, String>> linkedHashMaps = teamMapList.get("members");
        linkedHashMaps.forEach(member -> membersMessageDtoList.add(new MembersMessageDto(getEnrichedString(member.get("username"), 14), member.get("email"))));
        return membersMessageDtoList;
    }

    private HttpEntity<String> getStringHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", apiToken);
        return new HttpEntity<>("body", headers);
    }
}
