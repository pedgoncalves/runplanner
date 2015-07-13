package br.com.runplanner.service;

import org.springframework.stereotype.Service;

import br.com.runplanner.domain.PaymentData;

@Service("paymentDataService")
public class PaymentDataServiceImpl extends GenericServiceImpl<PaymentData, Long> implements PaymentDataService {


}
