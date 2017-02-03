package ftn.uns.ac.rs.tim6.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import org.drools.runtime.StatefulKnowledgeSession;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import ftn.uns.ac.rs.tim6.dto.AgeSubCategoryDto;
import ftn.uns.ac.rs.tim6.dto.InsuranceDto;
import ftn.uns.ac.rs.tim6.dto.InsuranceInfoDto;
import ftn.uns.ac.rs.tim6.dto.InsurancePriceDto;
import ftn.uns.ac.rs.tim6.dto.MerchantDto;
import ftn.uns.ac.rs.tim6.dto.PaymentUrlIdDto;
import ftn.uns.ac.rs.tim6.dto.ResponseMessageDto.TransactionResult;
import ftn.uns.ac.rs.tim6.model.Buyer;
import ftn.uns.ac.rs.tim6.model.Home;
import ftn.uns.ac.rs.tim6.model.Insurance;
import ftn.uns.ac.rs.tim6.model.Payment;
import ftn.uns.ac.rs.tim6.model.Person;
import ftn.uns.ac.rs.tim6.model.PricelistItem;
import ftn.uns.ac.rs.tim6.model.RiskSubcategory;
import ftn.uns.ac.rs.tim6.model.Vehicle;
import ftn.uns.ac.rs.tim6.service.BuyerService;
import ftn.uns.ac.rs.tim6.service.HomeService;
import ftn.uns.ac.rs.tim6.service.InsuranceService;
import ftn.uns.ac.rs.tim6.service.PaymentService;
import ftn.uns.ac.rs.tim6.service.PersonService;
import ftn.uns.ac.rs.tim6.service.PricelistService;
import ftn.uns.ac.rs.tim6.service.RiskSubcategoryService;
import ftn.uns.ac.rs.tim6.service.VehicleService;
import ftn.uns.ac.rs.tim6.util.CheckerCertificates;
import ftn.uns.ac.rs.tim6.util.DroolsReadKnowlageBase;

@RestController
@RequestMapping("/api")
public class InsuranceController {

	@Autowired
	InsuranceService insuranceService;

	@Autowired
	RiskSubcategoryService riskSubcategoryService;

	@Autowired
	PricelistService pricelistService;

	@Autowired
	PaymentService paymentService;

	@Autowired
	PersonService personService;

	@Autowired
	BuyerService buyerService;

	@Autowired
	HomeService homeService;

	@Autowired
	VehicleService vehicleService;

	@RequestMapping(value = "/insurances", method = RequestMethod.GET)
	public ResponseEntity<List<Insurance>> handleGetAllInsurances() {
		List<Insurance> insurances = (List<Insurance>) insuranceService.getAll();
		return new ResponseEntity<List<Insurance>>(insurances, HttpStatus.OK);
	}

	@RequestMapping(value = "/price", method = RequestMethod.POST)
	public InsurancePriceDto handlePrice(@RequestBody InsuranceDto idto) throws IOException {

		// TODO Drools
		InsurancePriceDto insurancePriceDto = new InsurancePriceDto();
		DroolsReadKnowlageBase kbase = new DroolsReadKnowlageBase();
		List<PricelistItem> curentPricelistItems = pricelistService.getCurrentPricelistItems();

		LocalDate start = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(idto.getStartDate()));
		LocalDate end = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(idto.getEndDate()));
		insurancePriceDto.setDays(Days.daysBetween(start, end).getDays());
		System.out.println("dani " + insurancePriceDto.getDays());

		for (AgeSubCategoryDto a : idto.getAgeList()) {
			insurancePriceDto.setPersons(insurancePriceDto.getPersons() + a.getAgeCount()); // sabiramo
			// broj
			// osoba
			for (PricelistItem item : curentPricelistItems) {
				if (item.getRiskSubcategory().getId() == a.getAgeId()) {
					insurancePriceDto.getItems().add(item);
				}
			}
		}
		System.out.println("broj osoba " + insurancePriceDto.getPersons());

		for (RiskSubcategory risk : idto.getItemsListForDrools()) {
			if (risk != null) {
				for (PricelistItem item : curentPricelistItems) {
					if (item.getRiskSubcategory().getId() == risk.getId()) {
						insurancePriceDto.getItems().add(item);
					}
				}
			}
		}
		/* CITANJE PODATAKA OD FRONTEND-A */

		try {

			System.out.println(" ULAZIMO U DROOLS ");
			StatefulKnowledgeSession ksession = kbase.getSession();
			ksession.insert(insurancePriceDto);
			ksession.fireAllRules();

		} catch (Exception e) {
			System.out.println(" GRESKA KOD DROOLS-A ");
			e.printStackTrace();
		}

		System.out.println("izasli iz drools-a, cena: " + insurancePriceDto.getTotalPrice());

		return insurancePriceDto;
	}

	@RequestMapping(value = "/buy", method = RequestMethod.POST)
	public ResponseEntity<PaymentUrlIdDto> handleBuy(@RequestBody InsuranceInfoDto iidto) throws IOException {

		// TODO korak 2
		PaymentUrlIdDto puid = new PaymentUrlIdDto();
		RestTemplate client = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		MerchantDto mdto = setMerchantDto(iidto.getTotalPrice());

		try {

			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<MerchantDto> entity = new HttpEntity<MerchantDto>(mdto, headers);

			// connecting to URL
			CheckerCertificates checkerCertificate = new CheckerCertificates();
			checkerCertificate.doTrustToCertificates();
			URL url = new URL("https://localhost:7070/api/urlid");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			System.out.println("ResponseCoede =" + conn.getResponseCode());

			puid = client.postForObject("https://localhost:7070/api/urlid", entity, PaymentUrlIdDto.class);

			setAndSavePayment(mdto, puid);
			setAndSaveInsurance(iidto, puid);

			return new ResponseEntity<PaymentUrlIdDto>(puid, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<PaymentUrlIdDto>(puid, HttpStatus.BAD_REQUEST);
		}

	}

	private void setAndSaveInsurance(InsuranceInfoDto iidto, PaymentUrlIdDto puid) {

		System.out.println(" cuvamo insurance u bazi ");

		Insurance i = new Insurance();
		i.setStartDate(iidto.getItemsForDrools().getStartDate());
		i.setEndDate(iidto.getItemsForDrools().getEndDate());

		if (iidto.isContractor()) {
			i.setNumberOfPersons(iidto.getPersons().size() + 1);
		} else {
			i.setNumberOfPersons(iidto.getPersons().size());
		}

		i.setTotalPrice(iidto.getTotalPrice());
		i.setDiscountPrice(iidto.getDiscountPrice());
		i.setTaxPrice(iidto.getTaxPrice());
		i.setPremiumPrice(iidto.getPremiumPrice());
		i.setPricelist(pricelistService.findCurrentPriceList());
		i.setPaymentId(puid.getPaymentId());

		Buyer b = iidto.getBuyer();
		buyerService.save(b);
		i.setBuyer(b);

		insuranceService.save(i);

		List<Person> persons = iidto.getPersons();
		for (Person p : persons) {
			p.setInsurance(i);
			personService.save(p);
		}

		if (!iidto.isContractor()) {
			Person holder = iidto.getPersonHolder();
			holder.setInsurance(i);
			personService.save(holder);
		}

		Home h = iidto.getHome();
		homeService.save(h);

		Vehicle v = iidto.getVehicle();
		vehicleService.save(v);

		System.out.println(" sacuvali insurance u bazi ");
	}

	private void setAndSavePayment(MerchantDto mdto, PaymentUrlIdDto puid) {
		Payment p = new Payment();
		p.setPaymentId(puid.getPaymentId());
		p.setTransactionResult(TransactionResult.NOT_STARTED);
		p.setPaymentStatus(null);
		p.setMerchantId(mdto.getMerchantId());
		p.setMerchantOrderId(mdto.getMerchantOrderID());
		paymentService.save(p);

	}

	private MerchantDto setMerchantDto(BigDecimal d) {
		MerchantDto mdto = new MerchantDto();
		Random randomGenerator = new Random();
		mdto.setMerchantId("chuck");
		mdto.setMerchantPassword("norris");
		mdto.setAmount(d);
		mdto.setMerchantOrderID(randomGenerator.nextInt(1000));
		mdto.setMerchantTimestamp(new Timestamp(System.currentTimeMillis()));
		return mdto;
	}
}
