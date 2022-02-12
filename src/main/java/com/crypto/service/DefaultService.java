package com.crypto.service;

public interface DefaultService {

    default String getEnrichedString(String name, int neededLength) {
        int length = name.length();
        return length < neededLength
                ? name.concat(" ".repeat(neededLength - length))
                : name.substring(0, neededLength);
    }
}
