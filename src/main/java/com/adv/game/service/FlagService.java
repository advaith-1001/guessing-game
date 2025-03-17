package com.adv.game.service;

import com.adv.game.model.Country;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlagService {

    private static final String API_URL = "https://restcountries.com/v3.1/all";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Country> fetchAllFlags() {
        String jsonResponse = restTemplate.getForObject(API_URL, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode countriesArray = objectMapper.readTree(jsonResponse);

            List<Country> countryList = new ArrayList<>();
            for(JsonNode country : countriesArray) {
                String name = country.get("name").get("common").asText();
                String flagUrl = country.get("flags").get("png").asText();
                countryList.add(new Country(name, flagUrl));
            }

            return countryList;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
