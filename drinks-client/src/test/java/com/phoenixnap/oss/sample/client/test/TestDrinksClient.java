package com.phoenixnap.oss.sample.client.test;

import java.util.List;

import com.phoenixnap.oss.sample.client.DrinkController;
import com.phoenixnap.oss.sample.client.model.CreateDrinksRequest;
import com.phoenixnap.oss.sample.client.model.Drink;
import com.phoenixnap.oss.sample.client.model.DrinkCollection;
import com.phoenixnap.oss.sample.client.model.DrinkUpload;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.phoenixnap.oss.sample.client.test.factory.DrinkFactory;
import com.phoenixnap.oss.sample.main.ClientLauncher;
import com.phoenixnap.oss.sample.server.ServerLauncher;

import feign.FeignException;

/**
 * Test suite that runs integration tests to ensure the automatically generated
 * DrinkClient & DrinkController are working in sync. Client requests generated
 * by the client should map directly onto an endpoint made available by the
 * controller.
 * 
 * 
 * @author kristiang
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ServerLauncher.class, ClientLauncher.class }, webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TestDrinksClient {

	@Autowired
	private DrinkController drinkClient;

	@Test
	public void getDrinksIntegrationTest() throws Exception {
		ResponseEntity<DrinkCollection> getDrinksResponse = drinkClient.getDrinkCollection();

		Assert.assertEquals(HttpStatus.OK, getDrinksResponse.getStatusCode());
		Assert.assertNotNull(getDrinksResponse.getBody());
		Assert.assertTrue(getDrinksResponse.getBody().getDrinks().size() > 0);
	}

	@Test
	public void getDrinkByIdIntegrationTest_Pass() throws Exception {
		ResponseEntity<Drink> getDrinksResponse = drinkClient.getDrinkByDrinkName("fanta");

		Assert.assertEquals(HttpStatus.OK, getDrinksResponse.getStatusCode());
		Assert.assertNotNull(getDrinksResponse.getBody());
		Assert.assertEquals("Fanta", getDrinksResponse.getBody().getName());
		Assert.assertEquals("SOFT_DRINK", getDrinksResponse.getBody().getType());
	}

	@Test
	public void getDrinkByIdIntegrationTest_Fail_NotFound() throws Exception {
		try {
			drinkClient.getDrinkByDrinkName("sprite");
			Assert.fail("The item should not be available! Are the server and client working properly?!");
		} catch (HttpClientErrorException hce) {
			Assert.assertEquals(HttpStatus.NOT_FOUND, hce.getStatusCode());
		} catch (HystrixRuntimeException hre) {
			FeignException cause = (FeignException) hre.getCause();
			Assert.assertEquals(HttpStatus.NOT_FOUND.value(), cause.status());
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void createDrinkIntegrationTest() throws Exception {
		CreateDrinksRequest createDrinkRequest = DrinkFactory.getDrink();

		ResponseEntity createDrinkResponse = drinkClient.createCreateDrinksRequest(createDrinkRequest);
		Assert.assertEquals(HttpStatus.ACCEPTED, createDrinkResponse.getStatusCode());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void updateDrinkIntegrationTest() throws Exception {
		DrinkUpload updateDrinkRequest = new DrinkUpload();
		updateDrinkRequest.setName("Beer");

		ResponseEntity updateDrinkResponse = drinkClient.updateDrinkUpload("Martini", updateDrinkRequest);
		Assert.assertEquals(HttpStatus.OK, updateDrinkResponse.getStatusCode());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void deleteDrinkIntegrationTest() throws Exception {
		ResponseEntity updateDrinkResponse = drinkClient.deleteDrinkByDrinkName("cocacola");
		Assert.assertEquals(HttpStatus.ACCEPTED, updateDrinkResponse.getStatusCode());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void endToEndDrinksIntegrationTest() {
		// get list and establish base number of items
		ResponseEntity<DrinkCollection> getDrinksResponseOrig = drinkClient.getDrinkCollection();

		// create item
		CreateDrinksRequest createDrinkRequest = DrinkFactory.getDrink();
		ResponseEntity createDrinkResponse = drinkClient.createCreateDrinksRequest(createDrinkRequest);
		Assert.assertEquals(HttpStatus.ACCEPTED, createDrinkResponse.getStatusCode());

		// get all drinks and assert they are one larger than when they started
		// off
		ResponseEntity<DrinkCollection> getDrinksResponseAfterCreate = drinkClient.getDrinkCollection();
		Assert.assertEquals(getDrinksResponseOrig.getBody().getDrinks().size() + 1, getDrinksResponseAfterCreate.getBody().getDrinks().size());

		// get created item
		ResponseEntity<Drink> getDrink = drinkClient.getDrinkByDrinkName(createDrinkRequest.getName());

		// update item
		DrinkUpload updateDrinkRequest = new DrinkUpload();
		updateDrinkRequest.setName("Beer");

		ResponseEntity updateDrinkResponse = drinkClient.getDrinkByDrinkName(getDrink.getBody().getName());
		Assert.assertEquals(HttpStatus.OK, updateDrinkResponse.getStatusCode());

		// get last updated details
		getDrink = drinkClient.getDrinkByDrinkName(updateDrinkRequest.getName());
		Assert.assertEquals(HttpStatus.OK, getDrink.getStatusCode());
		// assert the new name is retrievable
		Assert.assertNotNull(getDrink.getBody());
		Assert.assertEquals(updateDrinkRequest.getName(), getDrink.getBody().getName());

		// assert the item is not accessible by its old name
		try {
			drinkClient.getDrinkByDrinkName(createDrinkRequest.getName());
			Assert.fail("The item should not be available! Are the server and client working properly?");
		} catch (HttpClientErrorException hce) {
			Assert.assertEquals(HttpStatus.NOT_FOUND, hce.getStatusCode());
		} catch (HystrixRuntimeException hre) {
			FeignException cause = (FeignException) hre.getCause();
			Assert.assertEquals(HttpStatus.NOT_FOUND.value(), cause.status());
		}

		// delete item
		ResponseEntity deleteDrink = drinkClient.getDrinkByDrinkName(updateDrinkRequest.getName());
		Assert.assertEquals(HttpStatus.ACCEPTED, deleteDrink.getStatusCode());

		// assert the item isn't available
		try {
			drinkClient.getDrinkByDrinkName(updateDrinkRequest.getName());
			Assert.fail("The item should not be available! Are the server and client working properly?");
		} catch (HttpClientErrorException hce) {
			Assert.assertEquals(HttpStatus.NOT_FOUND, hce.getStatusCode());
		} catch (HystrixRuntimeException hre) {
			FeignException cause = (FeignException) hre.getCause();
			Assert.assertEquals(HttpStatus.NOT_FOUND.value(), cause.status());
		}

		// assert list size is same as when we started off
		ResponseEntity<DrinkCollection> getDrinksResponseFinal = drinkClient.getDrinkCollection();
		Assert.assertEquals(getDrinksResponseOrig.getBody().getDrinks().size(), getDrinksResponseFinal.getBody().getDrinks().size());
	}
}
