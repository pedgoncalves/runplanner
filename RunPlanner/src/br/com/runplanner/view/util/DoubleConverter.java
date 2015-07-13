package br.com.runplanner.view.util;

import java.text.DecimalFormat;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class DoubleConverter implements Converter {  
    
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String valorTela) throws ConverterException {  
  
        if(valorTela == null || valorTela.toString().trim().equals("")){  
            return 0.0d;  
  
        } else {  
            valorTela = valorTela.replaceAll(",", ".");  
  
            try{
                double valor = Double.valueOf(valorTela);
            	
                DecimalFormat df = new DecimalFormat("#0.00");  
                df.setMaximumFractionDigits(2);  
  
                return Double.valueOf( df.format(valor).replaceAll(",", ".") );
  
            }catch (Exception e) {  
            	
            	FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            			MessagesResources.getStringFromBundle("payment.value.converter.erro",""),"");
                
                throw new ConverterException(message);
            }  
        }  
    }  
  
    public String getAsString(FacesContext arg0, UIComponent arg1, Object valorTela) throws ConverterException {  
  
        if(valorTela == null || valorTela.toString().trim().equals("")){  
            return "0.00";  
  
        } else {  
        	DecimalFormat df = new DecimalFormat("#0.00");  
            df.setMaximumFractionDigits(2);   
  
            return df.format(Double.valueOf(valorTela.toString())).replaceAll(",", ".");  
        }  
    }  
}  
