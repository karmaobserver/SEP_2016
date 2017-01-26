package ftn.uns.ac.rs.tim6.controller;

import java.sql.Timestamp;
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

import ftn.uns.ac.rs.tim6.dto.AcquirerOrderDto;
import ftn.uns.ac.rs.tim6.dto.PaymentInfoDto;
import ftn.uns.ac.rs.tim6.dto.ResponseMessageDto;
import ftn.uns.ac.rs.tim6.dto.URLDto;
import ftn.uns.ac.rs.tim6.model.AcquirerOrder;
import ftn.uns.ac.rs.tim6.model.IssuerMessage;
import ftn.uns.ac.rs.tim6.model.PaymentRequest;
import ftn.uns.ac.rs.tim6.service.AccountService;
import ftn.uns.ac.rs.tim6.service.AcquirerOrderService;
import ftn.uns.ac.rs.tim6.service.IssuerMessageService;
import ftn.uns.ac.rs.tim6.service.PaymentRequestService;

@RestController
@RequestMapping("/api")
public class AcquirerOrderController {

	@Autowired
	AcquirerOrderService acquirerOrderService;

	@Autowired
	AccountService accountService;

	@Autowired
	PaymentRequestService paymentRequestService;

	@Autowired
	IssuerMessageService issuerMessageService;

	@RequestMapping(value = "/acquirerOrders", method = RequestMethod.GET)

	public ResponseEntity<List<AcquirerOrder>> handleGetAllAcquirerOrders() {
		List<AcquirerOrder> acquirerOrders = (List<AcquirerOrder>) acquirerOrderService.getAll();
		return new ResponseEntity<List<AcquirerOrder>>(acquirerOrders, HttpStatus.OK);
	}

	@RequestMapping(value = "/payment/pay", method = RequestMethod.POST)
	public ResponseEntity<URLDto> handlePay(@RequestBody PaymentInfoDto paymentInfo) {

		RestTemplate client = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		ResponseMessageDto rmdto = new ResponseMessageDto();
		URLDto urldto = new URLDto();

		// TODO korak 5
		AcquirerOrder acquirerOrder = setAndSaveAcquirerOrder(paymentInfo);
		AcquirerOrderDto aodto = createAcquirerOrderDto(acquirerOrder);

		try {

			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<AcquirerOrderDto> entity = new HttpEntity<AcquirerOrderDto>(aodto, headers);

			System.out.println("PRE SLANJA PCC-u");
			System.out.println(paymentInfo.getPaymentId());
			// poruka prema PCC-u i dalje u krug
			// TODO korak 6
			rmdto.setPaymentId(paymentInfo.getPaymentId());
			rmdto = client.postForObject("http://localhost:9090/api/incomingacquirerorder", entity,
					ResponseMessageDto.class);
			rmdto.setPaymentId(paymentInfo.getPaymentId());

			createAndSaveIssuerMessage(rmdto);

			System.out.println("PaymentID NAKON SETOVANJA: " + rmdto.getPaymentId());

			// TODO korak 10 + 11
			// poruka prema merchantu koja se prosledjuje od PCC-a
			HttpEntity<ResponseMessageDto> entityResponse = new HttpEntity<ResponseMessageDto>(rmdto, headers);
			urldto = client.postForObject("http://localhost:8080/api/incomingresult", entityResponse, URLDto.class);

			return new ResponseEntity<URLDto>(urldto, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<URLDto>(urldto, HttpStatus.BAD_REQUEST);
		}

	}

	private void createAndSaveIssuerMessage(ResponseMessageDto rmdto) {
		IssuerMessage im = new IssuerMessage();
		im.setAcquirerOrderId(rmdto.getAcquirerOrderId());
		im.setAcquirerTimestamp(rmdto.getAcquirerTimestamp());
		im.setIssuerOrderId(rmdto.getMerchantOrderId());
		im.setIssuerTimestamp(rmdto.getMerchantTimestamp());
		im.setTransactionResult(rmdto.getResult());
		issuerMessageService.save(im);
	}

	private AcquirerOrderDto createAcquirerOrderDto(AcquirerOrder acquirerOrder) {

		AcquirerOrderDto aodto = new AcquirerOrderDto();
		aodto.setTimestamp(acquirerOrder.getTimestamp());
		aodto.setPan(acquirerOrder.getPan());
		aodto.setSecurityCode(acquirerOrder.getSecurityCode());
		aodto.setTransactionAmount(acquirerOrder.getTransactionAmount());
		aodto.setCardHolder(acquirerOrder.getCardHolder());
		aodto.setExpDateYear(acquirerOrder.getExpDateYear());
		aodto.setExpDateMonth(acquirerOrder.getExpDateMonth());
		aodto.setAcquirerOrderId(acquirerOrder.getAcquirerOrderId());
		return aodto;
	}

	private AcquirerOrder setAndSaveAcquirerOrder(PaymentInfoDto paymentInfo) {

		PaymentRequest paymentRequest = paymentRequestService.findByPaymentId(paymentInfo.getPaymentId());
		AcquirerOrder acquirerOrder = new AcquirerOrder();
		Random randomGenerator = new Random();

		acquirerOrder.setAcquirerOrderId(randomGenerator.nextInt(1000));
		acquirerOrder.setTimestamp(new Timestamp(System.currentTimeMillis()));
		acquirerOrder.setPan(paymentInfo.getPan());
		acquirerOrder.setSecurityCode(paymentInfo.getSecurityCode());
		acquirerOrder.setTransactionAmount(paymentRequest.getAmount());
		acquirerOrder.setCardHolder("ZA SAD NEKI CARD HOLDER");
		acquirerOrder.setExpDateYear(paymentInfo.getYear());
		acquirerOrder.setExpDateMonth(paymentInfo.getMonth());
		// acquirerOrder.setAccount();
		acquirerOrderService.save(acquirerOrder);

		return acquirerOrder;
	}
}
