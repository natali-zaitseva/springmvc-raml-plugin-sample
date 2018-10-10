package com.phoenixnap.oss.sample.client.test.factory;

import java.util.UUID;

import com.phoenixnap.oss.sample.client.model.CreateDrinksRequest;

/**
 * Simple factory class to generate CreateDrinkRequests on the fly 
 * @author kristiang
 *
 */
public class DrinkFactory {

    public static CreateDrinksRequest getDrink(){
        CreateDrinksRequest createDrinkRequest = new CreateDrinksRequest();
        
        createDrinkRequest.setName(UUID.randomUUID().toString());
        createDrinkRequest.setType("SOFT_DRINK");
        
        return createDrinkRequest;
    }
    
}
