package br.com.runplanner.view.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component("rowCounter")
@Scope("request")
public class RowCounter {

	private transient int row = 0;

    public int getRow() {
        return ++row;
    }
    
}
