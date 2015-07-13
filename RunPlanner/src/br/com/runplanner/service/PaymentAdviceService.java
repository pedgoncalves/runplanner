package br.com.runplanner.service;

import br.com.runplanner.domain.PaymentMonthsAdvice;

public interface PaymentAdviceService extends GenericService<PaymentMonthsAdvice, Long> {
	PaymentMonthsAdvice findByAdvice(Long adviceId, String year);
}
