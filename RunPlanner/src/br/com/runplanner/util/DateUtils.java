package br.com.runplanner.util;

import java.text.DateFormatSymbols;
import java.util.Locale;

public class DateUtils {
	
	public static String mesPorExtenso(int mes) {
		return new DateFormatSymbols(new Locale("pt", "BR")).getMonths()[mes];
	}

}
