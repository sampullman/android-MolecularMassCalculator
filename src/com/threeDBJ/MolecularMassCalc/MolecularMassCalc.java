package com.threeDBJ.MolecularMassCalcLib;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.EditText;
import java.lang.Exception;
import java.lang.IndexOutOfBoundsException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.view.View.OnClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.content.res.Configuration;
import android.content.Context;
import android.view.ContextMenu;
import android.view.MenuItem;
import java.text.DecimalFormat;
import android.util.Log;
import android.content.Intent;

import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;

import java.text.DecimalFormat;

public class MolecularMassCalc extends Activity {

    private EditText input;
    private TextView result;
    private TextView formula;
    private Parser p;

    String inp="", res="", form="";

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

        this.p = new Parser();

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
	
    }

    public void setupUI() {
	formula = (TextView)findViewById (R.id.form_name);
        this.input = (EditText) findViewById(R.id.input);
        input.setOnEditorActionListener(calcAction);
        this.result = (TextView) findViewById(R.id.result);
        Button n = (Button) findViewById(R.id.calc);
        n.setOnClickListener(calcBtn);
        n = (Button) findViewById(R.id.clear);
        n.setOnClickListener(clearBtn);
        n = (Button) findViewById(R.id.percent);
        n.setOnClickListener(percentBtn);
        registerForContextMenu(n);
    }

    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.percent) {
            menu.setHeaderTitle("Mass Percentages");
            MenuItem item;
            String add;
            double per;
            DecimalFormat twoDForm = new DecimalFormat("#.####");
            item = menu.add(p.getCleanForm());
            for(int i=0;i<p.symbs.size();i+=1) {
                add = p.symbs.get(i);
                add += Integer.toString(p.nums.get(i))+" = ";
                per = p.vals.get(i) / p.tot;
                per *= 100.0;
                per = Double.valueOf(twoDForm.format(per));
                add += Double.toString(per) + "%";
                item = menu.add(add);
            }
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        return true;
    }

    private void dispResult() {
        String inp = input.getText().toString();
        res = calculate(inp);
        if(res.length() > 11) {
            res = res.substring(0,11);
        }
        result.setText(res);
        formula.setText ("Loading...");
        new RetrieveFormula().execute (inp);
    }

    private String calculate(String inp) {
        double res = 0,val;
        p.setInput(inp);
        while(true) {
            try {
                val = p.nextVal();
            } catch (Exception e) {
                Toast.makeText(MolecularMassCalc.this, e.getMessage(),
                               Toast.LENGTH_SHORT).show();
                return "";
            }
            if(val > 0)
                res += val;
            else break;
        }
        DecimalFormat twoDForm = new DecimalFormat("#.####");
        res = Double.valueOf(twoDForm.format(res));
        return Double.toString(res);
    }

    private OnClickListener calcBtn = new OnClickListener() {
	    public void onClick(View v) {
		dispResult();
	    }
	};

    private OnClickListener clearBtn = new OnClickListener() {
	    public void onClick(View v) {
		input.setText("");
	    }
	};

    private OnClickListener percentBtn = new OnClickListener() {
	    public void onClick(View v) {
		if(p.symbs.size() > 0)
		    openContextMenu(findViewById(R.id.percent));
	    }
	};

    private OnEditorActionListener calcAction = new OnEditorActionListener() {

	    @Override
	    public boolean onEditorAction(TextView v, int actionId,
					  KeyEvent event) {
		if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
		    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		    in.hideSoftInputFromWindow(input.getApplicationWindowToken(),
					       InputMethodManager.HIDE_NOT_ALWAYS);
		    dispResult();
		}
		return false;
	    }
        };

    private class Parser {

        String input,last;
        public ArrayList<String> symbs=new ArrayList<String>();
        public ArrayList<Double> vals=new ArrayList<Double>();
        public ArrayList<Integer> nums=new ArrayList<Integer>();
        Pattern pat1=Pattern.compile("([a-zA-Z]{1,2}).*"),pat2=Pattern.compile("([0-9]+).*");
        Matcher mat;
        public double tot=0;

        public void setInput(String s) {
            this.input = s.toLowerCase();
            symbs.clear();
            vals.clear();
            nums.clear();
            tot = 0;
            last = "";
        }

        public double nextVal() throws Exception {
            if(input == null)
                return 0;
            if(input.length() == 0)
                return 0;
            String sym="";
            double num;
            double ans;
            try {
                sym = nextSymb();
                num = nextNum();
                int ind = indexOf(symbols,sym);
                ans =  weights[ind] * num;
                symbs.add(names[ind]);
                nums.add((int)num);
                vals.add(ans);
                tot += ans;
                return ans;
            } catch (ArrayIndexOutOfBoundsException a) {
                throw new Exception("Unrecognized element '"+sym+"'");
            } catch (Exception e) {
                throw e;
            }
        }

        private String nextSymb() throws Exception {
            mat = pat1.matcher(input);
            if(!mat.matches()) throw new Exception("Invalid element");
            input = input.substring(mat.group(1).length());
            //Toast.makeText(MolecularMassCalc.this, input,Toast.LENGTH_SHORT).show();
            last = mat.group(1);
            return last;
        }

        private double nextNum() throws Exception {
            mat = pat2.matcher(input);
            if(!mat.matches()) throw new Exception("Could not parse number after "+last);
            input = input.substring(mat.group(1).length());
            //Toast.makeText(MolecularMassCalc.this, mat.group(1),Toast.LENGTH_SHORT).show();
            return Integer.parseInt(mat.group(1));
        }

        private int indexOf(String[] arr,String x) {
            for(int i=0;i<arr.length;i+=1) {
                if(arr[i].compareTo(x) == 0)
                    return i;
            }
            return -1;
        }

        public String getCleanForm() {
            String ret="";
            int n;
            for(int i=0;i<symbs.size();i+=1) {
                ret += symbs.get(i);
                n = nums.get(i);
                if(n > 1)
                    ret += Integer.toString(n);
            }
            return ret;
        }
    }

    private static final String[] symbols = { "h", "he", "li", "be", "b", "c", "n", "o", "f", "ne", "na", "mg", "al", "si", "p", "s", "cl", "ar", "k", "ca", "sc", "ti", "v", "cr", "mn", "fe", "co", "ni", "cu", "zn", "ga", "ge", "as", "se", "br", "kr", "rb", "sr", "y", "zr", "nb", "mo", "tc", "ru", "rh", "pd", "ag", "cd", "in", "sn", "sb", "te", "i", "xe", "cs", "ba", "la", "ce", "pr", "nd", "pm", "sm", "eu", "gd", "tb", "dy", "ho", "er", "tm", "yb", "lu", "hf", "ta", "w", "re", "os", "ir", "pt", "au", "hg", "tl", "pb", "bi", "po", "at", "rn", "fr", "ra", "ac", "th", "pa", "u", "np", "pu", "am", "cm", "bk", "cf", "es", "fm", "md", "no", "lr", "rf", "db", "sg", "bh", "hs", "mt", "ds", "rg" };
    private static final String[] names = { "H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh", "Hs", "Mt", "Ds", "Rg" };
    private static final double[] weights = { 1.00794, 4.002602, 6.941, 9.012182, 10.811, 12.0107, 14.0067, 15.9994, 18.9984032, 20.1797, 22.98976928, 24.305, 26.9815386, 28.0855, 30.973762, 32.065, 35.453, 39.948, 39.0983, 40.078, 44.955912, 47.867, 50.9415, 51.9961, 54.938045, 55.845, 58.933195, 58.6934, 63.546, 65.38, 69.723, 72.64, 74.9216, 78.96, 79.904, 83.798, 85.4678, 87.62, 88.90585, 91.224, 92.90638, 95.96, 98.0, 101.07, 102.9055, 106.42, 107.8682, 112.411, 114.818, 118.71, 121.76, 127.6, 126.90447, 131.293, 132.9054519, 137.327, 138.90547, 140.116, 140.90765, 144.242, 145.0, 150.36, 151.964, 157.25, 158.92535, 162.5, 164.93032, 167.259, 168.93421, 173.054, 174.9668, 178.49, 180.94788, 183.84, 186.207, 190.23, 192.217, 195.084, 196.966569, 200.59, 204.3833, 207.2, 208.9804, 209.0, 210.0, 222.0, 223.0, 226.0, 227.0, 232.03806, 231.03588, 238.02891, 237.0, 244.0, 243.0, 247.0, 247.0, 251.0, 252.0, 257.0, 258.0, 259.0, 262.0, 267.0, 268.0, 271.0, 272.0, 270.0, 276.0, 281.0, 280.0 };

    private class RetrieveFormula extends AsyncTask<String,Integer,String> {
        protected String doInBackground (String... data) {
            try {
                URL addr = new URL ("http://webbook.nist.gov/cgi/cbook.cgi?Formula="+
                                    data[0]+"&NoIon=on&Units=SI");
                URLConnection con = addr.openConnection ();
                con.setDoOutput(true);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line, res="";
                while ((line = in.readLine()) != null)
                    res += line;
                return res;
            } catch (MalformedURLException e) {
                Log.v ("make_query",e.getMessage ());
                return "Error";
            } catch (IOException e) {
                Log.v ("make_query",e.getMessage ());
                return "Error";
            }
        }

        protected void onProgressUpdate (Integer... prog) {
        }

        protected void onPostExecute (String result) {
            Log.v ("mmc", result);
            Pattern pat=Pattern.compile(".*<title>(.*)</title>.*");
            Matcher mat = pat.matcher(result);
            if (mat.matches ()) {
                if (mat.group(1).equals ("Search Results")) {
                    pat = Pattern.compile(".*?<li><a.*?>(.*?)</a>.*");
                    mat = pat.matcher(result);
                    mat.matches ();
		    form = mat.group(1);
                    Log.v ("mmc", form);
                    Log.v ("mmc", Integer.toString(mat.groupCount()));
                } else {
		    form = mat.group(1);
                    Log.v ("mmc",mat.group(1));
                }
            } else {
		form = "Not found";
	    }
	    formula.setText(form);
        }
    }
}
