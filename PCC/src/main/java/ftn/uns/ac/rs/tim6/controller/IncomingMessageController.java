package ftn.uns.ac.rs.tim6.controller;

import java.util.ArrayList;
import java.util.List;
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
import ftn.uns.ac.rs.tim6.dto.ResponseMessageDto;
import ftn.uns.ac.rs.tim6.dto.ResponseMessageDto.TransactionResult;
import ftn.uns.ac.rs.tim6.model.Bank;
import ftn.uns.ac.rs.tim6.model.IncomingMessage;
import ftn.uns.ac.rs.tim6.service.BankService;
import ftn.uns.ac.rs.tim6.service.IncomingMessageService;

@RestController
@RequestMapping("/api")
public class IncomingMessageController {

	@Autowired
	IncomingMessageService incomingMessageService;

	@Autowired
	BankService bankService;

	@RequestMapping(value = "/incomingmessages", method = RequestMethod.GET)

	public ResponseEntity<List<IncomingMessage>> handleGetAllIncomingMessages() {
		List<IncomingMessage> incomingmessages = (List<IncomingMessage>) incomingMessageService.getAll();
		return new ResponseEntity<List<IncomingMessage>>(incomingmessages, HttpStatus.OK);
	}

	@RequestMapping(value = "/incomingacquirerorder", method = RequestMethod.POST)
	public ResponseEntity<ResponseMessageDto> handleIncomingMessage(@RequestBody AcquirerOrderDto aodto) {

		// TODO korak 7
		Bank bank = findBankByPan(aodto.getPan());

		// korak 7.1 nadjemo banku preko PAN-a
		// prosledjujemo pristigli zahtev

		RestTemplate client = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		ResponseMessageDto rmdto = new ResponseMessageDto();
		
		rmdto.setResult(TransactionResult.SUCCESSFUL);
		rmdto.setAcquirerOrderId(aodto.getAcquirerOrderId());
		rmdto.setAcquirerTimestamp(aodto.getTimestamp());

		try {

			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<AcquirerOrderDto> entity = new HttpEntity<AcquirerOrderDto>(aodto, headers);
			rmdto = client.postForObject("http://localhost:" + bank.getPort() + "/api/reservation", entity, ResponseMessageDto.class);
			return new ResponseEntity<ResponseMessageDto>(rmdto, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<ResponseMessageDto>(rmdto, HttpStatus.BAD_REQUEST);
		}
	}

	private Bank findBankByPan(Long pan) {
		// A BBBBB CCCCCCCCCCCC D ja uzimam prvih 6 cifara
		Long panBanke = Long.parseLong(Long.toString(pan).substring(0, 6));
		List<Bank> banke = new ArrayList<Bank>();
		banke = bankService.getAll();
		for (Bank bank : banke) {
			if (bank.getPan().longValue() == panBanke.longValue()) {
				System.out.println("NASLI SMO BANKU");
				System.out.println(bank.getPort());
				return bank;
			}
		}
		System.out.println("NISMO NASLI SMO BANKU!!!");
		return null;
	}

}
