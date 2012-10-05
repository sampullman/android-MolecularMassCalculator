package com.threeDBJ.MolecularMassCalcLib;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.content.res.Configuration;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import java.text.DecimalFormat;
import android.util.Log;

import java.text.DecimalFormat;

public class MolecularMassCalc extends Activity {

    public static final int NONE=0, PERCENTAGES=1, SELECT_FORMULA=2;

    int menu_mode = NONE;
    EditText input;
    TextView result;
    TextView formula;
    FormulaParser p;
    Formula curFormula;

    String inp="", res="", form="";
    boolean resultsVisible = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("testing...","made it");
        Configuration config = this.getResources().getConfiguration();

        if(config.orientation == 1) {
            setContentView(R.layout.main);
        } else if(config.orientation == 2) {
            setContentView(R.layout.main_wide);
        }

        this.p = new FormulaParser();

	setupUI();
    }

    /** Handles a screen orientation change */
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
	int orientation = config.orientation;
        if(orientation == 1) {
            setContentView(R.layout.main);
        } else if(orientation == 2) {
            setContentView(R.layout.main_wide);
	}
	setupUI();
	input.setText(inp);
	result.setText(res);
	formula.setText(form);
	View resHolder = findViewById(R.id.result_holder);
	View percent = findViewById(R.id.percent);
	if(resultsVisible) {
	    resHolder.setVisibility(View.VISIBLE);
	    percent.setVisibility(View.VISIBLE);
	} else {
	    resHolder.setVisibility(View.GONE);
	    percent.setVisibility(View.INVISIBLE);
	}
    }

    public void setupUI() {
	this.formula = (TextView)findViewById (R.id.form_name);
        this.input = (EditText) findViewById(R.id.input);
        input.setOnEditorActionListener(calcAction);
	input.addTextChangedListener(inputChanged);
        this.result = (TextView) findViewById(R.id.result);
        Button n = (Button) findViewById(R.id.calc);
        n.setOnClickListener(calcBtn);
        registerForContextMenu(n);
        n = (Button) findViewById(R.id.clear);
        n.setOnClickListener(clearBtn);
        n = (Button) findViewById(R.id.percent);
        n.setOnClickListener(percentBtn);
        registerForContextMenu(n);
    }

    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
	MenuItem item;
        if(v.getId() == R.id.percent) {
	    menu_mode = PERCENTAGES;
            menu.setHeaderTitle("Mass Percentages");
            String add;
            item = menu.add("Formula: "+curFormula.getCleanForm());
            for(String per : curFormula.getMassPercents()) {
                item = menu.add(per);
            }
        } else if(v.getId() == R.id.calc) {
	    menu_mode = SELECT_FORMULA;
	    menu.setHeaderTitle("Resolve Ambiguous Formula");
	    for(int i=0;i<p.formulas.size();i+=1) {
		item = menu.add(0, i, 0, p.getFormula(i).getCleanForm());
	    }
	}
    }

    public boolean onContextItemSelected(MenuItem item) {
	if(menu_mode == SELECT_FORMULA) {
	    Log.e("mmc", "shiiiteee "+item.getItemId());
	    curFormula = p.getFormula(item.getItemId());
	    showResult();
	}
	menu_mode = NONE;
        return true;
    }

    public void setFormulaText(String form) {
	this.form = form;
	formula.setText(form);
    }

    private void calculate() {
	inp = input.getText().toString();
	if(inp.length() == 0) {
	    hideResult();
	    return;
	}
        p.setInput(inp);
        int nFormulas = p.parse();
	if(nFormulas == 0) {
	    Toast.makeText(this, p.error,
			   Toast.LENGTH_SHORT).show();
	} else if(nFormulas == 1) {
	    curFormula = p.getFormula();
	    showResult();
	} else {
	    curFormula = p.getFormula();
	    openContextMenu(findViewById(R.id.calc));
	}
    }

    public void showResult() {
	double resVal = curFormula.getMass();
        DecimalFormat twoDForm = new DecimalFormat("#.####");
        resVal = Double.valueOf(twoDForm.format(resVal));

	res = Double.toString(resVal);
        result.setText(res);
	if(res.length() > 0) {
	    formula.setText ("Loading...");
	    new RetrieveFormulaTask(this).execute (inp);
	}
	if(!resultsVisible) {
	    View resHolder = findViewById(R.id.result_holder);
	    resHolder.startAnimation(new ViewScaler(1.0f, 1.0f, 0.0f, 1.0f, 500, resHolder, false));
	    AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
	    fadeIn.setDuration(500);
	    fadeIn.setFillAfter(true);
	    findViewById(R.id.percent).startAnimation(fadeIn);
	    resultsVisible = true;
	}
    }

    public void hideResult() {
	if(resultsVisible) {
	    View resHolder = findViewById(R.id.result_holder);
	    resHolder.startAnimation(new ViewScaler(1.0f, 1.0f, 1.0f, 0.0f, 500, resHolder, true));
	    AlphaAnimation fadeIn = new AlphaAnimation(1f, 0f);
	    fadeIn.setDuration(500);
	    fadeIn.setFillAfter(true);
	    findViewById(R.id.percent).startAnimation(fadeIn);
	    resultsVisible = false;
	}
    }

    private OnClickListener calcBtn = new OnClickListener() {
	    public void onClick(View v) {
		calculate();
	    }
	};

    private OnClickListener clearBtn = new OnClickListener() {
	    public void onClick(View v) {
		inp = "";
		input.setText(inp);
		hideResult();
	    }
	};

    private OnClickListener percentBtn = new OnClickListener() {
	    public void onClick(View v) {
		if(curFormula != null && curFormula.nMasses > 0)
		    openContextMenu(findViewById(R.id.percent));
	    }
	};

    private OnEditorActionListener calcAction = new OnEditorActionListener() {

	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
		    calculate();
		}
		return false;
	    }
        };

    private TextWatcher inputChanged = new TextWatcher() {
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }

	    @Override
	    public void afterTextChanged(Editable s) {
		inp = s.toString();
	    }

	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	    }
	};
}
