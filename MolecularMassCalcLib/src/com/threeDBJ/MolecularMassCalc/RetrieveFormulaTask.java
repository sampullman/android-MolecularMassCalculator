package com.threeDBJ.MolecularMassCalcLib;

import android.content.Context;
import android.os.AsyncTask;

import android.util.Log;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;

public class RetrieveFormulaTask extends AsyncTask<String,Integer,String> {

    MolecularMassCalc context;

    public RetrieveFormulaTask(MolecularMassCalc context) {
	this.context = context;
    }

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
	Pattern pat=Pattern.compile(".*<title>(.*)</title>.*");
	Matcher mat = pat.matcher(result);
	String form="";
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
	context.setFormulaText(form);
    }
}