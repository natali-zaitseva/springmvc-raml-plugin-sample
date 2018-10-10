package com.phoenixnap.oss.sample.main;


import static org.junit.Assert.assertTrue;


import com.phoenixnap.oss.sample.client.DrinkController;
import com.phoenixnap.oss.sample.client.HealthCheckController;
import com.phoenixnap.oss.sample.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author kristiang
 * @author Aleksandar Stojsavljevic (aleksandars@ccbill.com)
 */
@Component
public class ClientRunner
        implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ClientRunner.class);

    @Autowired
    HealthCheckController hcClient;

    @Autowired
    DrinkController drinkClient;
    
    /**
     * {@inheritDoc}}
     */
    @SuppressWarnings("rawtypes")
	@Override
    public void run(String... args)
            throws Exception {
       //checks health of endpoint
       ResponseEntity<HealthCheck> healthCheck = hcClient.getHealthCheck();
       assertTrue("Health check failed! Server is not healthy.", healthCheck.getStatusCode().equals(HttpStatus.OK)); 
       LOG.info("Health Check status : {}" , healthCheck.getStatusCode());
       
       //get list of drinks (GET list)
       ResponseEntity<DrinkCollection> getDrinksResponse = drinkClient.getDrinkCollection();
       assertTrue("Failed to perform GET request.", getDrinksResponse.getStatusCode().equals(HttpStatus.OK)); 
       getDrinksResponse.getBody().getDrinks().forEach(d -> LOG.info(d.getName() + ","));
       
       //get specific drink (GET)
       ResponseEntity<Drink> drink = drinkClient.getDrinkByDrinkName("Martini");
       assertTrue("Failed to perform GET request.", drink.getStatusCode().equals(HttpStatus.OK)); 
       
       //output all details returned
       LOG.info("Drink name: [{}]", drink.getBody().getName());
       LOG.info("Drink type: [{}]", drink.getBody().getType());
       
       //create drink (POST)
       CreateDrinksRequest createDrinkRequest = new CreateDrinksRequest();
       createDrinkRequest.setName("Wine");
       createDrinkRequest.setType("ALCOHOL");
       ResponseEntity createDrinkResponse = drinkClient.createCreateDrinksRequest(createDrinkRequest);
       assertTrue("Failed to perform POST request.", createDrinkResponse.getStatusCode().equals(HttpStatus.ACCEPTED)); 
       LOG.info("Successfully created drink with name [{}]", createDrinkRequest.getName());
       
       //update drink (PUT)
        DrinkUpload updateDrinkByIdRequest = new DrinkUpload();
       updateDrinkByIdRequest.setName("Beer");
       ResponseEntity updateResponse = drinkClient.updateDrinkUpload("wine", updateDrinkByIdRequest);
       assertTrue("Failed to perform PUT request.", updateResponse.getStatusCode().equals(HttpStatus.OK));
       LOG.info("Successfully updated drink. Verifying ...");
       
       //Verify update
       ResponseEntity<Drink> updatedDrink = drinkClient.getDrinkByDrinkName(updateDrinkByIdRequest.getName());
       boolean responseOK = updatedDrink.getStatusCode().equals(HttpStatus.OK) && updatedDrink.getBody()!=null;
       assertTrue("Failed to verify PUT request. Failed to update drink", responseOK);
       LOG.info("Successfully verified drink update!");
       
       //delete drink (DELETE)
       ResponseEntity deleteResponse = drinkClient.deleteDrinkByDrinkName(updateDrinkByIdRequest.getName());
       assertTrue("Failed perform DELETE request.", deleteResponse.getStatusCode().equals(HttpStatus.ACCEPTED));
       LOG.info("Successfully deleted drink with name: [{}]", updateDrinkByIdRequest.getName());
    }
}
