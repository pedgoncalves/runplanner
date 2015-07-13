package br.com.runplanner.exception;

/**
 * Exception criada para informar que uma assessoria não pode ser deletada.
 * Ela está herdando de EntityNotFoundException por problemas arquiteturais. Senão não poderia 
 * ser lançada do metodo override deleteById.
 * Ver se deve mesmo ficar assim
 * 
 * @author Daniel
 *
 */
public class AdviceCantBeDeletedException extends EntityNotFoundException {

	private static final long serialVersionUID = -9009747366898714843L;

}
