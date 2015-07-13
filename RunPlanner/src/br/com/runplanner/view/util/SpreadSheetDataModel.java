package br.com.runplanner.view.util;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import br.com.runplanner.domain.Spreadsheet;

public class SpreadSheetDataModel  extends ListDataModel<Spreadsheet> implements SelectableDataModel<Spreadsheet> {
	
	private List<Spreadsheet> spreadsheetList;
	
	public SpreadSheetDataModel(List<Spreadsheet> spreadsheet) {
		super(spreadsheet);
		this.spreadsheetList = spreadsheet;		
	}
	
	public Spreadsheet getRowData(String arg0) {
		Long id = Long.parseLong(arg0);
		
		Spreadsheet selected = null;
		for(Spreadsheet spread:spreadsheetList) {
			if ( spread.getId().longValue()==id.longValue() ) {
				selected = spread;
				break;
			}
		}
		
		return selected;       
        
	}

	public Object getRowKey(Spreadsheet spread) {
		return spread.getId();
	}

}
