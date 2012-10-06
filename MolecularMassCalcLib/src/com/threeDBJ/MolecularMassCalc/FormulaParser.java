package com.threeDBJ.MolecularMassCalcLib;

import com.threeDBJ.MolecularMassCalcLib.Formula.InvalidElementException;

import android.util.Log;

import java.util.ArrayList;

public class FormulaParser {

    public static final int SYM=0, NUM=1, NONE=2;

    String input, last, error;
    ArrayList<Formula> formulas = new ArrayList<Formula>();

    public Formula getFormula() {
	return getFormula(0);
    }

    public Formula getFormula(int i) {
	return formulas.get(i);
    }

    public void setInput(String s) {
	this.input = s.toLowerCase();
	formulas.clear();
	last = "";
    }

    public int parse() {
	try {
	    parse(this.input, new Formula(), NONE);
	    return formulas.size();
	} catch(InvalidElementException e) {
	    this.error = e.getMessage();
	} catch(Exception e) {
	    this.error = e.getMessage();
	    //this.error = "Formula not recognized.";
	}
	return 0;
    }

    public void parse(String input, Formula cur, int mode) throws Exception, InvalidElementException {
	if(input.length() == 0) {
	    if(mode == SYM) cur.addNum(1);
	    formulas.add(cur);
	    return;
	}
	char c = peek(input);
	if(Character.isLetter(c)) {
	    boolean found = false;
	    String err = "";
	    if(mode == SYM) cur.addNum(1);
	    Formula next = new Formula(cur);
	    input = pop(input);

	    if(cur.addSym(c+"")) {
		parse(input, cur, SYM);
		found = true;
	    } else {
		err = c+"";
	    }
	    if(input.length() == 0) return;
	    char n = peek(input);
	    if(Character.isLetter(n)) {
		input = pop(input);
		if(next.addSym(c+""+n)) {
		    parse(input, next, SYM);
		    found = true;
		} else {
		    err = c+""+n;
		}
	    }
	    if(!found) {
		this.error = "Unknown element: "+err;
	    }
	} else if(Character.isDigit(c)) {
	    input = consumeInt(input, cur);
	    parse(input, cur, NUM);
	} else {
	    this.error = "Invalid input: "+c;
	}
    }

    public char peek(String input) {
	return input.charAt(0);
    }

    public String pop(String input) {
	if(input.length() > 1) {
	    return input.substring(1);
	} else {
	    return "";
	}
    }

    public String consumeInt(String input, Formula f) {
	int i=1;
	while(i < input.length() && Character.isDigit(input.charAt(i))) {
	    i += 1;
	}
	f.addNum(Integer.parseInt(input.substring(0, i)));
	return input.substring(i);
    }
}