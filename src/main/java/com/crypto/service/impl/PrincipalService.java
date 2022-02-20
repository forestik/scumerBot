package com.crypto.service.impl;

import com.crypto.dto.PrincipalsMessageDto;
import com.crypto.dto.RatingMessageDto;
import com.crypto.entity.Principal;
import com.crypto.repo.PrincipalsRepo;
import com.crypto.service.DefaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PrincipalService implements DefaultService {

    private final PrincipalsRepo principalsRepo;

    public PrincipalService(PrincipalsRepo principalsRepo) {
        this.principalsRepo = principalsRepo;
    }

    public List<PrincipalsMessageDto> tagAll(){
        List<Principal> all = getPrincipals();
        List<PrincipalsMessageDto> principalsMessageDtoList = new LinkedList<>();
        all.forEach(principal -> principalsMessageDtoList.add(new PrincipalsMessageDto(principal.getUserName())));
        return principalsMessageDtoList;
    }

    public String tagIn(User user) {
        if (addPrincipal(user)) {
            return "Will ping you ";
        }
        return "You're already in ";
    }

    public boolean addPrincipal(User user) {
        Optional<Principal> principal = principalsRepo.findByUserName("@" + user.getUserName());
        if (principal.isEmpty()) {
            principalsRepo.save(Principal.builder()
                    .userName("@" + user.getUserName())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .rating(0)
                    .build());
        }
        return principal.isEmpty();
    }

    public String addRating(String userName) {
        Optional<Principal> principal = principalsRepo.findByUserName("@" + userName);
        if (principal.isEmpty()){
            log.info("{} not found", userName);
            return "<pre>" + userName + " not found</pre>";
        }
        else {
            Integer rating = principal.get().getRating();
            principal.get().setRating(rating + 1);
            principalsRepo.save(principal.get());
            log.info("Added {} rating to {}", 1, userName);
            return "<pre>Good job </pre>" + principal.get().getUserName() + "\r\n" +
                    "<pre>Your rating has been increased by 1 points</pre>";
        }
    }

    public String lowerRating(String userName) {
        Optional<Principal> principal = principalsRepo.findByUserName("@" + userName);
        if (principal.isEmpty()){
            log.info("{} not found", userName);
            return "<pre>" + userName + " not found</pre>";
        }
        else {
            Integer rating = principal.get().getRating();
            principal.get().setRating(rating - 1);
            principalsRepo.save(principal.get());
            log.info("Added {} rating to {}", 1, userName);
            return "<pre>Bad job </pre>" + principal.get().getUserName() + "\r\n" +
                    "<pre>Your rating has been lowered by 1 points" + "\r\n"  +
                    "You're starting to be the devil</pre>";
        }
    }


    public List<RatingMessageDto> getRating() {
        List<Principal> principals = getPrincipals();
        List<RatingMessageDto> ratingMessageDtoList = new LinkedList<>();
        principals.forEach(principal -> {
            String lastName = principal.getLastName();
            if (lastName==null){
                lastName="";
            }
            ratingMessageDtoList.add(
                    new RatingMessageDto(getEnrichedString(principal.getFirstName() + " " + lastName, 20), principal.getRating().toString()));
        });
        return ratingMessageDtoList;
    }

    private List<Principal> getPrincipals() {
        return principalsRepo.findAll();
    }
}
