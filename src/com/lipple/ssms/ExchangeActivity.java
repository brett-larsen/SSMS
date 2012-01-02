package com.lipple.ssms;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.lipple.ssms.dbms.DBHelper;
import com.lipple.ssms.entities.Contact;

public class ExchangeActivity extends OrmLiteBaseActivity<DBHelper> implements OnClickListener, android.content.DialogInterface.OnClickListener {
	
	//There should only ever be one of these at a time, so this should be safe
	private static ExchangeActivity instance = null;
	private Contact selectedContact;
	private Button exchangeButton;
	private List<String> contactData;
	//private 
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		
		instance = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exchange);

        Log.i("ExchangeActivity", "onCreate");
        selectedContact = getHelper().getSelectedContact();
        
        setupView();
		
	}

	protected void setupView() {
        ListView lv = (ListView) this.findViewById(R.id.exchangeList);
        exchangeButton = (Button) this.findViewById(R.id.button1);
        //textBox = (AutoCompleteTextView) this.findViewById(R.id.textField);
        
        Spinner spinner = (Spinner) this.findViewById(R.id.exchangeNumberSpinner);
        //fin
        if(spinner == null)
        {
        	Log.i("SSMSExchange", "Spinner is null!!!");
        }
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.selectedContact.getNumber(this)));
        
        //Set up our send message behavior
        exchangeButton.setOnClickListener(this);
        
        
        contactData = getContactData();
        
        lv.setAdapter(new ArrayAdapter<String>(this, R.layout.message, contactData));
        lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lv.setStackFromBottom(true);
        lv.setTextFilterEnabled(true);
	}

	private List<String> getContactData() {
		try {
			getHelper().getDao(Contact.class).refresh(selectedContact);//make sure we're fresh
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> data = new ArrayList<String>();
		
		data.add("Name: " + selectedContact.getDisplayName(this));
		data.add("Public RSA Modulus: " + selectedContact.getModulus());
		data.add("Public RSA Exponent: " + selectedContact.getExponent());
		data.add("Incoming Key: " + selectedContact.getContactKey());
		data.add("Outgoing Key: " + selectedContact.getMyKey());
		
		return data;
		
	}

	protected static ExchangeActivity getInstance()
	{
		return instance;
	}
	@Override
	public void onDestroy()
	{
		instance=null;
		super.onDestroy();
	}

	public void onClick(View v) {
        
        //Context context = getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("ONLY DO THIS ONCE! This button sends 2 SMS messages to synchronize your security keys. You only need to do this one time, or however often you feel like generating new security keys (for the extra paranoid).")
               .setCancelable(false)
               .setPositiveButton("OKAY", this);
        AlertDialog alert = builder.create();
        alert.show();
        
        
       
	}
	
	public void finish()
	{
		instance=null;
		super.finish();
	}

	public void onClick(DialogInterface dialog, int which) {
		
		dialog.dismiss();
		Spinner spinner = (Spinner) findViewById(R.id.exchangeNumberSpinner);
		SSMSService.getInstance().sendRequestSMS(selectedContact, (String)spinner.getSelectedItem());
	} 
}
