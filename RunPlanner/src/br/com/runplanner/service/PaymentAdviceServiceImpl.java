package br.com.runplanner.service;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.PaymentMonthsAdvice;

@Service("paymentAdviceService")
public class PaymentAdviceServiceImpl extends GenericServiceImpl<PaymentMonthsAdvice, Long> implements PaymentAdviceService {
		
	@Transactional
	public PaymentMonthsAdvice findByAdvice(Long adviceId, String year) {
		Query query = entityManager.createNamedQuery("PaymentMonthsAdvice.findByAdvice");
		query.setParameter("adviceId", adviceId);
		query.setParameter("yearStr", year);

		
		try {
			return (PaymentMonthsAdvice)query.getSingleResult();
		}
		catch (NoResultException  e) {
			return null;
		}	
	
	}

}
