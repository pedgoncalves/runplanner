package br.com.runplanner.view.util;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import br.com.runplanner.domain.Pessoa;

public class CustomerDataModel extends ListDataModel<Pessoa> implements
		SelectableDataModel<Pessoa> {

	private List<Pessoa> customerList;
	
	public CustomerDataModel(List<Pessoa> customerList){
		super(customerList);
		this.customerList = customerList;
	}
	
	public Pessoa getRowData(String arg0) {
		Long id = Long.parseLong(arg0);
		
		Pessoa selectedPessoa = null;
		
		for(Pessoa p: customerList){
			if(p.getId().longValue() == id.longValue()){
				selectedPessoa = p;
				break;
			}
		}
		
		return selectedPessoa;
	}

	public Object getRowKey(Pessoa customer) {
		return customer.getId();
	}

}
