package ftn.uns.ac.rs.tim6.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ftn.uns.ac.rs.tim6.dto.ResponseMessageDto;
import ftn.uns.ac.rs.tim6.dto.ResponseMessageDto.TransactionResult;
import ftn.uns.ac.rs.tim6.dto.URLDto;
import ftn.uns.ac.rs.tim6.model.Insurance;
import ftn.uns.ac.rs.tim6.model.Payment;
import ftn.uns.ac.rs.tim6.model.Payment.Status;
import ftn.uns.ac.rs.tim6.service.InsuranceService;
import ftn.uns.ac.rs.tim6.service.PaymentService;
import ftn.uns.ac.rs.tim6.util.MailService;

@RestController
@RequestMapping("/api")
public class PaymentController {

	@Autowired
	PaymentService paymentService;

	@Autowired
	InsuranceService insuranceService;

	@RequestMapping(value = "/payments", method = RequestMethod.GET)
	public ResponseEntity<List<Payment>> handleGetAllPayments() {
		List<Payment> payments = (List<Payment>) paymentService.getAll();
		return new ResponseEntity<List<Payment>>(payments, HttpStatus.OK);
	}

	@RequestMapping(value = "/incomingresult", method = RequestMethod.POST)
	public ResponseEntity<URLDto> handleIncomingMessage(@RequestBody ResponseMessageDto rmdto) {

		URLDto urldto = new URLDto();

		System.out.println("PaymentController paymentID: ");
		System.out.println(rmdto.getPaymentId());

		// TODO korak 11

		TransactionResult rezultat = rmdto.getResult();

		System.out.println("paymentID: " + rmdto.getPaymentId());
		

		if (rezultat.equals(TransactionResult.SUCCESSFUL)) {
			urldto.setUrl("https://localhost:8080/paymentSuccessful?paymentId=" + rmdto.getPaymentId());
			urldto.setStatus(Status.SUCCESSFUL);

		} else if (rezultat.equals(TransactionResult.CVC_INVALID)) {
			urldto.setUrl("https://localhost:8080/paymentFailed?paymentId=" + rmdto.getPaymentId());
			urldto.setStatus(Status.FAILED);

		} else if (rezultat.equals(TransactionResult.INSUFFICIENT_FUNDS)) {
			urldto.setUrl("https://localhost:8080/paymentFailed?paymentId=" + rmdto.getPaymentId());
			urldto.setStatus(Status.FAILED);

		} else if (rezultat.equals(TransactionResult.INVALID_DATE)) {
			urldto.setUrl("https://localhost:8080/paymentFailed?paymentId=" + rmdto.getPaymentId());
			urldto.setStatus(Status.FAILED);

		} else if (rezultat.equals(TransactionResult.ERROR)) {
			urldto.setUrl("https://localhost:8080/paymentError?paymentId=" + rmdto.getPaymentId());
			urldto.setStatus(Status.ERROR);

		} else if (rezultat.equals(TransactionResult.INVALID_CARD)) {
			urldto.setUrl("https://localhost:8080/paymentError?paymentId=" + rmdto.getPaymentId());
			urldto.setStatus(Status.FAILED);
		}
		setAndSavePayment(rmdto, urldto);

		// TODO e-mail
		if (rezultat.equals(TransactionResult.SUCCESSFUL)) {
			
			Insurance i = insuranceService.findByPaymentId(rmdto.getPaymentId());
			System.out.println("mail se kreira");
			MailService m = new MailService();
			m.sendMail(i);
			System.out.println("poslat mail");
		}

		return new ResponseEntity<URLDto>(urldto, HttpStatus.OK);
	}

	

	@RequestMapping(value = "/paymentMessage", method = RequestMethod.POST)
	public ResponseEntity<URLDto> handlePaymentID(@RequestBody Integer paymentId) {

		Payment p = paymentService.findByPaymentId(paymentId);
		URLDto urldto = new URLDto();

		urldto.setResult(p.getTransactionResult());
		urldto.setStatus(p.getPaymentStatus());

		return new ResponseEntity<URLDto>(urldto, HttpStatus.OK);
	}

	private void setAndSavePayment(ResponseMessageDto rmdto, URLDto urldto) {

		Payment p = paymentService.findByPaymentId(rmdto.getPaymentId());
		p.setTransactionResult(rmdto.getResult());
		p.setPaymentStatus(urldto.getStatus());
		paymentService.save(p);
	}

}
