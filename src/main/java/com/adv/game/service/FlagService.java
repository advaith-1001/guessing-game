package com.adv.game.service;

import com.adv.game.model.Country;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlagService {

    private static final String API_URL = "https://restcountries.com/v3.1/all?fields=name,flags";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Country> fetchAllFlags() {
        String jsonResponse = restTemplate.getForObject(API_URL, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode countriesArray = objectMapper.readTree(jsonResponse);

            List<Country> countryList = new ArrayList<>();
            for (JsonNode country : countriesArray) {
                JsonNode nameNode = country.get("name");
                JsonNode flagNode = country.get("flags");

                if (nameNode != null && flagNode != null && flagNode.get("png") != null) {
                    String name = nameNode.get("common").asText();
                    String flagUrl = flagNode.get("png").asText();
                    countryList.add(new Country(name, flagUrl));
                }
            }

            // Shuffle the list to randomize the countries
            Collections.shuffle(countryList);

            // Return only the first 15
            return countryList.stream().limit(15).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
