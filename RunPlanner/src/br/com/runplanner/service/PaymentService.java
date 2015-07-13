package br.com.runplanner.service;

import java.util.List;

import br.com.runplanner.domain.PaymentMonths;

public interface PaymentService extends GenericService<PaymentMonths, Long> {
	PaymentMonths findByCustomerYear(Long customerId, String year);
	List<PaymentMonths> findByAdvice(Long adviceId, String year);
}
