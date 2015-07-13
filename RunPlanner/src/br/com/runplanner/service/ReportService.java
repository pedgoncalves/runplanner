package br.com.runplanner.service;

import java.util.List;
import java.util.Map;

public interface ReportService {
	@SuppressWarnings("rawtypes")
	public boolean gerar(int tipoRelatorio, Map<String, Object> param, List beanList, boolean usarLogoRunPlanner);
	byte[] getSpreadsheet(Map<String, Object> param);
}
