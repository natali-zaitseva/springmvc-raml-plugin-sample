/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.phoenixnap.oss.sample.server.controllers;

import java.util.ArrayList;
import java.util.List;

import com.phoenixnap.oss.sample.server.rest.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.phoenixnap.oss.sample.server.exceptions.DrinkNotFoundException;
import com.phoenixnap.oss.sample.server.models.AbstractDrink;
import com.phoenixnap.oss.sample.server.models.AlcoholicDrink;
import com.phoenixnap.oss.sample.server.models.SoftDrink;
import com.phoenixnap.oss.sample.server.models.enums.DrinkTypeEnum;
import com.phoenixnap.oss.sample.server.rest.DrinkController;
import com.phoenixnap.oss.sample.server.services.DrinksServiceInterface;

/**
 * Delegate implementation of the DrinkController for use by the generated endpoint 
 * controller 
 *   
 * @author kristiang
 *
 */
@Component
public class DrinkControllerImpl implements DrinkController{

    @Autowired
    private DrinksServiceInterface drinksService;
    
    private static final Logger LOG = LoggerFactory.getLogger(DrinkController.class);

    @Override
    public ResponseEntity<DrinkCollection> getDrinks() {
        List<Drink> getDrinksResponse = new ArrayList<Drink>();
        List<AbstractDrink> drinks = this.drinksService.getDrinks();
        
        //map each drink to an appropriate response object 
        for (AbstractDrink drink : drinks){
            Drink drinkReponse = new Drink();
            drinkReponse.setName(drink.getName());
            drinkReponse.setType(drink.getDrinkTypeEnum().name());
            getDrinksResponse.add(drinkReponse);
        }
        DrinkCollection resp = new DrinkCollection(getDrinksResponse);
        LOG.info("Returning list of {} available drinks ... ", getDrinksResponse.size());
        return new ResponseEntity<DrinkCollection>(resp, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Drink> createDrink(CreateDrinkRequest createDrinkRequest) {
        LOG.debug("Entered createDrink endpoint");
        try{
            DrinkTypeEnum drinkType = DrinkTypeEnum.valueOf(String.valueOf(createDrinkRequest.getType()));
            AbstractDrink drink = null;
            switch (drinkType){
                case ALCOHOL:
                    drink = new AlcoholicDrink(createDrinkRequest.getName());                    
                    break;
                case SOFT_DRINK:
                    drink = new SoftDrink(createDrinkRequest.getName());
                    break;
                default:         
                    LOG.error("Incorrect drink type passed: [{}] ", createDrinkRequest.getType());
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            this.drinksService.addDrink(drink);            
            LOG.info("Returning from createDrink");
            return new ResponseEntity(HttpStatus.ACCEPTED);
        }catch(DrinkNotFoundException dex){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
                
    }

    @Override
    public ResponseEntity<Drink> getDrinkByName(String drinkName) {
        Drink getDrinkByIdResponse = new Drink();
        
        try{
            AbstractDrink drink = this.drinksService.getDrink(drinkName);            
            getDrinkByIdResponse.setName(drink.getName());
            getDrinkByIdResponse.setType(drink.getDrinkTypeEnum().name());
            LOG.info("Returning from getDrinkById");
            return new ResponseEntity<Drink> (getDrinkByIdResponse, HttpStatus.OK);
        }catch(DrinkNotFoundException e){
            return new ResponseEntity<Drink> (HttpStatus.NOT_FOUND);
        }            
    }

    @Override
    public ResponseEntity<DrinkUpload> updateDrinkByName(String drinkName, DrinkUpload updateDrinkByIdRequest) {
        try{
            this.drinksService.modifyDrink(drinkName.toLowerCase(), updateDrinkByIdRequest.getName());
            LOG.info("Returning from updateDrinkById");
            return new ResponseEntity(HttpStatus.OK);
        }catch(DrinkNotFoundException dex){
            return new ResponseEntity<DrinkUpload> (HttpStatus.NOT_FOUND);

        }
        
    }

    @Override
    public ResponseEntity deleteDrinkByName(String drinkName) {
        try{
            this.drinksService.deleteDrink(drinkName);
            LOG.info("Returning from deleteDrinkById");
            return new ResponseEntity(HttpStatus.ACCEPTED);
        }catch(DrinkNotFoundException dex){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
