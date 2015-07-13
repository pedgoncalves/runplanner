package br.com.runplanner.service;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.runplanner.domain.PaymentMonths;

@Service("paymentServiceImpl")
public class PaymentServiceImpl extends GenericServiceImpl<PaymentMonths, Long> implements
	PaymentService {
	
	
	public PaymentMonths findByCustomerYear(Long customerId, String year) {
		Query query = entityManager.createNamedQuery("PaymentMonths.findByCustomerYear");
		query.setParameter("customerId", customerId);
		query.setParameter("yearStr", year);
		try{
			return (PaymentMonths)query.getSingleResult();
		} catch (NoResultException e) {
			return new PaymentMonths();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public List<PaymentMonths> findByAdvice(Long adviceId, String year) {
		Query query = entityManager.createNamedQuery("PaymentMonths.findByAdvice");
		query.setParameter("adviceId", adviceId);
		query.setParameter("yearStr", year);

		List<PaymentMonths> result = (List<PaymentMonths>)query.getResultList();
		for(PaymentMonths payment:result) {
			payment.getCustomer().getNome();
		}
		
		return result;
	}

}
