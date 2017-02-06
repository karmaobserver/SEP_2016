package ftn.uns.ac.rs.tim6.controller;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ftn.uns.ac.rs.tim6.dto.AcquirerOrderDto;
import ftn.uns.ac.rs.tim6.dto.ResponseMessageDto;
import ftn.uns.ac.rs.tim6.dto.ResponseMessageDto.TransactionResult;
import ftn.uns.ac.rs.tim6.model.Account;
import ftn.uns.ac.rs.tim6.model.AcquirerOrderReservation;
import ftn.uns.ac.rs.tim6.service.AccountService;
import ftn.uns.ac.rs.tim6.service.AcquirerOrderReservationService;

@RestController
@RequestMapping("/api")
public class AcquirerOrderReservationController {

	@Autowired
	AcquirerOrderReservationService acquirerOrderReservationService;

	@Autowired
	AccountService accountService;

	@RequestMapping(value = "/acquirerOrderReservations", method = RequestMethod.GET)
	public ResponseEntity<List<AcquirerOrderReservation>> handleGetAllAcquirerOrderReservations() {
		List<AcquirerOrderReservation> acquirerOrderReservations = (List<AcquirerOrderReservation>) acquirerOrderReservationService
				.getAll();
		return new ResponseEntity<List<AcquirerOrderReservation>>(acquirerOrderReservations, HttpStatus.OK);
	}

	@RequestMapping(value = "/reservation", method = RequestMethod.POST)
	public ResponseEntity<ResponseMessageDto> handleIncomingMessage(@RequestBody AcquirerOrderDto aodto) {

		ResponseMessageDto rmdto = new ResponseMessageDto();
		Account account = accountService.findByPan(aodto.getPan());
		MathContext mc = new MathContext(10);

		System.out.println("stigli smo u banku B " + aodto.getPan());
		int res = account.getAccountBalance().compareTo(aodto.getTransactionAmount());
		BigDecimal b1 = account.getAccountBalance();

		Boolean card = cardCheck(aodto);

		if (card) {
			// TODO korak 8
			if (res == 0) { // tacno

				rmdto.setResult(TransactionResult.SUCCESSFUL);
				b1 = account.getAccountBalance().subtract(aodto.getTransactionAmount(), mc);
				account.setAccountBalance(b1);
				accountService.save(account);

			} else if (res == 1) { // ima vise

				rmdto.setResult(TransactionResult.SUCCESSFUL);
				b1 = account.getAccountBalance().subtract(aodto.getTransactionAmount(), mc);
				account.setAccountBalance(b1);
				accountService.save(account);

			} else if (res == -1) { // nema dovoljno

				rmdto.setResult(TransactionResult.INSUFFICIENT_FUNDS);
			}
			rmdto.setAcquirerOrderId(aodto.getAcquirerOrderId());
			rmdto.setAcquirerTimestamp(aodto.getTimestamp());
			rmdto.setMerchantTimestamp(aodto.getTimestamp());
			// TODO issuer order id rmdto.setPaymentId(aodto.get);
		}
		try {
			// TODO korak 9
			return new ResponseEntity<ResponseMessageDto>(rmdto, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<ResponseMessageDto>(rmdto, HttpStatus.BAD_REQUEST);
		}

	}

	private Boolean cardCheck(AcquirerOrderDto aodto) {
		// TODO provera kartice
		return true;
	}
}
