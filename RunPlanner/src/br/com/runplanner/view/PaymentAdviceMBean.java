package br.com.runplanner.view;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import br.com.runplanner.domain.Advice;
import br.com.runplanner.domain.PaymentMonthsAdvice;
import br.com.runplanner.domain.StatusAdvice;
import br.com.runplanner.exception.EntityNotFoundException;
import br.com.runplanner.service.AdviceService;
import br.com.runplanner.service.PaymentAdviceService;
import br.com.runplanner.view.util.Constants;

@Component("paymentAdviceMBean")
@Scope("session")
public class PaymentAdviceMBean extends BasicMBean {


	private static final String PAYMENT_FORM_PAGE = "/pages/advice/payment";
	
	private String year;
	private Advice advice;
	
	private List<PaymentMonthsAdvice> paymentList;
	private List<Advice> adviceList;	

	private List<SelectItem> adviceListSI;
	
	private PaymentAdviceService paymentAdviceService;
	private AdviceService adviceService;
	
	@Autowired
	public PaymentAdviceMBean(PaymentAdviceService paymentAdviceService,
			AdviceService adviceService) {
		this.paymentAdviceService = paymentAdviceService;
		this.adviceService = adviceService;
	}
	
	@Override
	public String goToList() {

		adviceList = adviceService.loadByStatus(StatusAdvice.PAGANDO);
		advice = adviceList.get(0);
		
		this.year = String.valueOf(new GregorianCalendar().get(GregorianCalendar.YEAR));
		
		PaymentMonthsAdvice payment = paymentAdviceService.findByAdvice(advice.getId(), year);	
		
		if ( payment==null ) {
			payment = new PaymentMonthsAdvice();
			payment.setYear(year);
		}		
		
		this.paymentList = new ArrayList<PaymentMonthsAdvice>();
		this.paymentList.add(payment);
		
		adviceListSI = new ArrayList<SelectItem>();
		for (Advice advice:adviceList) {
			adviceListSI.add(new SelectItem(advice.getId(),advice.getName()));
		}
		
		setSelectedMenu(Constants.MENU_PAYMENT);
		
		return PAYMENT_FORM_PAGE;
	}
	
	public String find() {
		
		PaymentMonthsAdvice payment = paymentAdviceService.findByAdvice(advice.getId(), year);	
		
		if ( payment==null ) {
			payment = new PaymentMonthsAdvice();
			payment.setYear(year);
		}		
		
		this.paymentList = new ArrayList<PaymentMonthsAdvice>();
		this.paymentList.add(payment);
		
		setSelectedMenu(Constants.MENU_PAYMENT);
		
		return PAYMENT_FORM_PAGE;
	}	
	
	@Override
	public String save() {
		
		PaymentMonthsAdvice payment = paymentList.get(0);
		
		if ( payment.getId()==null ) {
			payment.setAdvice(advice);
			payment.setYear(year);
			paymentAdviceService.persist(payment);
		}
		else {
			try {
				paymentAdviceService.update(payment);
			} catch (EntityNotFoundException e) {
				addMessage(FacesMessage.SEVERITY_ERROR, "template.msg.entitynotfound.edit");
				return null;
			}
		}
			
		
		addMessage(FacesMessage.SEVERITY_INFO, "template.msg.payment.save.ok");
		return PAYMENT_FORM_PAGE;
	}	
	
	@Override
	public String goToCreate() {
		return null;
	}

	@Override
	public String goToModify() {
		return null;
	}

	@Override
	public String delete() {
		return null;
	}

    public List<SelectItem> getYearList() {
    	List<SelectItem> result = new ArrayList<SelectItem>();
    	
    	GregorianCalendar date = new GregorianCalendar();
    	Integer year = date.get(GregorianCalendar.YEAR);
    	    	
    	year--;
    	result.add( new SelectItem(year,year.toString()) );
    	year++;
    	result.add( new SelectItem(year,year.toString()) );
    	year++;
    	result.add( new SelectItem(year,year.toString()) );
    	
    	return result;
    }  	
		

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}
	
	public int getYearNum() {
		return Integer.parseInt(year);
	}

	public void setYearNum(int yearNum) {
		this.year = String.valueOf(yearNum);
	}

	public List<PaymentMonthsAdvice> getPaymentList() {
		return paymentList;
	}

	public void setPaymentList(List<PaymentMonthsAdvice> paymentList) {
		this.paymentList = paymentList;
	}

	public Advice getAdvice() {
		return advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public List<Advice> getAdviceList() {
		return adviceList;
	}

	public void setAdviceList(List<Advice> adviceList) {
		this.adviceList = adviceList;
	}

	public List<SelectItem> getAdviceListSI() {
		
		return adviceListSI;
	}
	
}
