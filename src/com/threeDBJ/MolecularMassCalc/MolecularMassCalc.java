package com.threeDBJ.MolecularMassCalc;

import android.app.Activity;
import android.os.Bundle;
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

import com.admob.android.ads.AdListener;
import com.admob.android.ads.AdView;
//import com.admob.android.ads.InterstitialAd;
//import com.admob.android.ads.InterstitialAdListener;
import com.admob.android.ads.SimpleAdListener;
import com.admob.android.ads.AdManager;
//import com.admob.android.ads.InterstitialAd.Event;

public class MolecularMassCalc extends Activity {

    private EditText input;
    private TextView result;
    private Parser p;
    //private InterstitialAd mInterstitialAd;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	Log.v("testing...","made it");
        Configuration config = this.getResources().getConfiguration();
	
    AdView ad;
	if(config.orientation == 1) {
	    setContentView(R.layout.main);
	    ad = (AdView) findViewById(R.id.ad2);
		ad.setAdListener(new MMCListener());
	} else if(config.orientation == 2) {
	    setContentView(R.layout.main2);
	}
	
	this.input = (EditText) findViewById(R.id.input);
	input.setOnEditorActionListener(calcAction);
	this.result = (TextView) findViewById(R.id.result);
	this.p = new Parser();

	Button n = (Button) findViewById(R.id.calc);
	n.setOnClickListener(calcBtn);
	n = (Button) findViewById(R.id.clear);
	n.setOnClickListener(clearBtn);
	n = (Button) findViewById(R.id.percent);
	n.setOnClickListener(percentBtn);
	registerForContextMenu(n);

	//AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR }); 
	Log.v("testing...",AdManager.getTestAction());
	ad = (AdView) findViewById(R.id.ad);
	ad.setAdListener(new MMCListener());
	//mInterstitialAd = new InterstitialAd(Event.APP_START, this);
        //mInterstitialAd.requestAd(this);
    }

    /**
     * If we fail to receive an interstitial ad, we just keep going on with
     * our application loading and execution.
    @Override
    public void onFailedToReceiveInterstitial(InterstitialAd interstitialAd) {
      // we couldn't get an interstitial ad before the game. Load the game.
      //if (interstitialAd == mInterstitialAd) {
      //
      //}
    }
    */

    /**
     * If we get an interstitial ad successfully, we can show the ad. 
     
    @Override
    public void onReceiveInterstitial(InterstitialAd interstitialAd) {
      if(interstitialAd == mInterstitialAd) {
        mInterstitialAd.show(this);
      }
    }
    */

    /**
     * After the ad has been shown, it will return with an activity result where
     * ADMOB_INTENT_BOOLEAN is true.  Once you receive this result you can continue
     * application loading and execution.
     
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if(data != null && data.getBooleanExtra(InterstitialAd.ADMOB_INTENT_BOOLEAN, false)) {
	  // do stuff
      }
      super.onActivityResult(requestCode, resultCode, data);
    }
    */

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
	String res = calculate(input.getText().toString());
	if(res.length() > 11) {
	    res = res.substring(0,11);
	}
	result.setText(res);
    }

    private String calculate(String inp) {
	double res = 0,val;
	p.setInput(inp);
	while(true) {
	    try {
		val = p.nextVal();
	    } catch (Exception e) {
		Toast.makeText(MolecularMassCalc.this, e.getMessage(),Toast.LENGTH_SHORT).show();
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
    private String[] names = { "H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh", "Hs", "Mt", "Ds", "Rg" };
    private static final double[] weights = { 1.00794, 4.002602, 6.941, 9.012182, 10.811, 12.0107, 14.0067, 15.9994, 18.9984032, 20.1797, 22.98976928, 24.305, 26.9815386, 28.0855, 30.973762, 32.065, 35.453, 39.948, 39.0983, 40.078, 44.955912, 47.867, 50.9415, 51.9961, 54.938045, 55.845, 58.933195, 58.6934, 63.546, 65.38, 69.723, 72.64, 74.9216, 78.96, 79.904, 83.798, 85.4678, 87.62, 88.90585, 91.224, 92.90638, 95.96, 98.0, 101.07, 102.9055, 106.42, 107.8682, 112.411, 114.818, 118.71, 121.76, 127.6, 126.90447, 131.293, 132.9054519, 137.327, 138.90547, 140.116, 140.90765, 144.242, 145.0, 150.36, 151.964, 157.25, 158.92535, 162.5, 164.93032, 167.259, 168.93421, 173.054, 174.9668, 178.49, 180.94788, 183.84, 186.207, 190.23, 192.217, 195.084, 196.966569, 200.59, 204.3833, 207.2, 208.9804, 209.0, 210.0, 222.0, 223.0, 226.0, 227.0, 232.03806, 231.03588, 238.02891, 237.0, 244.0, 243.0, 247.0, 247.0, 251.0, 252.0, 257.0, 258.0, 259.0, 262.0, 267.0, 268.0, 271.0, 272.0, 270.0, 276.0, 281.0, 280.0 };
    
    
    private class MMCListener extends SimpleAdListener {
	
	@Override
	public void onFailedToReceiveAd(AdView adView) {
	    // TODO Auto-generated method stub
	    super.onFailedToReceiveAd(adView);
	}
	
	@Override
	public void onFailedToReceiveRefreshedAd(AdView adView) {
	    // TODO Auto-generated method stub
	    super.onFailedToReceiveRefreshedAd(adView);
	}
	
	@Override
	public void onReceiveAd(AdView adView) {
	    // TODO Auto-generated method stub
	    super.onReceiveAd(adView);
	}
	
	@Override
	public void onReceiveRefreshedAd(AdView adView) {
	    // TODO Auto-generated method stub
	    super.onReceiveRefreshedAd(adView);
	}
    	
    }
    
    public void onFailedToReceiveAd(AdView adView) {
	Log.d("MMC", "onFailedToReceiveAd");
    }

    public void onFailedToReceiveRefreshedAd(AdView adView) {
	Log.d("MMC", "onFailedToReceiveRefreshedAd");
    }

    public void onReceiveAd(AdView adView) {
	Log.d("MMC", "onReceiveAd");
    }

    public void onReceiveRefreshedAd(AdView adView) {
	Log.d("MMC", "onReceiveRefreshedAd");
    }
    

}
