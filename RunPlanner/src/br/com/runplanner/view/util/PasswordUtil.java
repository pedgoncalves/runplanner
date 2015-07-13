package br.com.runplanner.view.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PasswordUtil {

	private static int SIZE = 10;
	
	public static String gerarSenha() {
		String[] keys = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"0", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
				"N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a",
				"b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
				"p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

		List<String> chars = new ArrayList<String>();
		Random random = new Random();
		String senha = "";

		for (int i = 0; i < keys.length; i++) {
			chars.add(keys[i].toString());
		}

		for (int i = 0; i < SIZE; i++) {
			senha += chars.get(random.nextInt(chars.size()));
		}

		return senha;
	}
	
}
