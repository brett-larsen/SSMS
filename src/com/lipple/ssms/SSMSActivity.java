package com.lipple.ssms;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import com.lipple.ssms.dbms.DBHelper;
import com.lipple.ssms.entities.Contact;
import com.lipple.ssms.entities.Message;

public class SSMSActivity extends OrmLiteBaseListActivity<DBHelper> implements OnClickListener {
	

	private List<String> contacts;

	private List<Contact> contactList;
	
	 
	private static SSMSActivity instance = null;
	private String password = "";
	private EditText input;
	private int attemptCount=0;
	private long lastSuccess = 0l;
	private DBHelper helper=null;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        instance = this;
    	helper = getHelper();

        
        setContentView(R.layout.main);
        startService(new Intent(this, SSMSService.class));
        Thread.yield();
        //try to let the service start if possible
        setup();
        

        
/*
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
            // When clicked, show a toast with the TextView text
            Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                Toast.LENGTH_SHORT).show();
          }
        });
        */
    }

    
    protected void setup()
    {
    	contacts = new ArrayList<String>();

    	contactList = new ArrayList<Contact>();
        getContacts();
        //Log.e("SSMSActivity", "Contacts:" + contactList.size());
        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, contacts));
        
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
    }

	private void getContacts() {		
		//List<Contact> contacts = new ArrayList<Contact>();
		// Form an array specifying which columns to return. 
		String[] projection = new String[] {
									 Contacts.LOOKUP_KEY
		                          };

		// Get the base URI for the People table in the Contacts content provider.
		Uri contactsUri =  ContactsContract.Contacts.CONTENT_URI;

		// Make the query. 
		Cursor managedCursor = managedQuery(contactsUri,
		                         projection, // Which columns to return 
		                         Contacts.HAS_PHONE_NUMBER + "=1 AND " + Contacts.IN_VISIBLE_GROUP + "=1",       // Which rows to return (all rows)
		                         null,       // Selection arguments (none)
		                         // Put the results in ascending order by name
		                         Data.DISPLAY_NAME + " ASC");
		
		while(managedCursor.moveToNext())
		{
			Contact contact = contactFromKey(managedCursor.getString(0));
			if(contact != null)
			{
				contactList.add(contact);
				contacts.add(contact.getDisplayName(this));
			}
			
		}
	}
	
	
	private Contact contactFromKey(String string) {
		SSMSService service=null;
		service = SSMSService.getInstance();
		int count = 15, x=0;
		if(service == null)
		{
			//Log.i("SSMSActivity", "Service null");
			do{

				service=SSMSService.getInstance();
				x++;
			}while(service == null && x<count);
			return null;
		}else
		{
			return service.getContactFromLookupKey(string);
		}
	}

	private boolean isNumber(String string) {
		if(PhoneNumberUtils.isWellFormedSmsAddress(string))
		{

			return true;
		}
		return false;
	}

	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Contact contact = this.contactList.get(position);
        try {
			getHelper().getDao(Contact.class).refresh(contact);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(contact == null)
        {
        	//Log.i("SSMSA", "contabct null");
        }
        //Log.i("SSMSActivity", "position: " + position);
        //Log.i("SSMSActivity", "Contact selected: " + contact.getDisplayName(this));
        getHelper().setSelectedContact(contact);
	        Intent intent = new Intent(this, ThreadActivity.class);
	        this.startActivity(intent);

	}
	
	protected static SSMSActivity getInstance()
	{
		return instance;
	}
	

	public void onDestroy()
	{
		instance=null;
		super.onDestroy();
	}
	
	public void onPause()
	{
		instance=null;
		super.onPause();
	}
	
	public void onResume()
	{
		instance=this;
		super.onResume();
		if(SSMSService.getInstance() != null)
		{
			SSMSService.getInstance().prune();
		}
		passwordPrompt();
		
		
		
	}


	private void passwordPrompt() {
		try {
			
			File file = getFileStreamPath("pass");
	    	if(!file.exists())
	    	{
	    		startActivity(new Intent(this, CreatePasswordActivity.class));
	    	}else
	    	{
	    		if(new Date().getTime() - lastSuccess > 600000)
	    		{
	    		FileInputStream fIn;
				
					fIn = openFileInput("pass");
				
	        	DataInputStream in = new DataInputStream(fIn);
	            BufferedReader br = new BufferedReader(new InputStreamReader(in));
	            password = br.readLine();
	            //Log.i("SSMS", "password: " + password);
	            br.close();
	            input = new EditText(this);
	            input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
	            //input.set
	            new AlertDialog.Builder(this)
	            .setTitle("Enter Password")
	            .setMessage("Enter the password or clear all the data stored in this app (" + (10-attemptCount) + " tries remaining):")
	            .setView(input)
	            .setPositiveButton("Enter", this).setNegativeButton("Clear Data", this).setCancelable(false).show();
	            
	    		}
	    	}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void onClick(DialogInterface dialog, int which) {
		if(which == DialogInterface.BUTTON_POSITIVE)
		{
		MessageDigest algorithm = null;
		try {
			algorithm = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		algorithm.reset();
		algorithm.update(input.getText().toString().trim().getBytes());
		byte messageDigest[] = algorithm.digest();
	            
		StringBuffer hexString = new StringBuffer();
		for (int i=0;i<messageDigest.length;i++) {
			hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
		}
		//String foo = messageDigest.toString();
		//Log.i("SSMS", "Entered: " + hexString.toString());
		if(hexString.toString().equals(password))
		{
			//password good
			attemptCount=0;
			lastSuccess = new Date().getTime();
			dialog.cancel();
			dialog.dismiss();
			
		}else
		{
			//password bad
			attemptCount++;
			if(attemptCount<10)
			{
				dialog.cancel();
				dialog.dismiss();
				passwordPrompt();
			}else
			{
				clearDBData();
			}
			
		}
		}else
		{
			clearDBData();
		
			CharSequence text = "Data cleared";
			int duration = Toast.LENGTH_LONG;
	
			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
			dialog.dismiss();
		}
		
	}
	
	private void clearDBData()
	{
		try{
		TableUtils.dropTable(helper.getConnectionSource(), Contact.class, true);

		TableUtils.dropTable(helper.getConnectionSource(), Message.class, true);
		DaoManager.clearCache();
		TableUtils.createTable(helper.getConnectionSource(), Contact.class);
		TableUtils.createTable(helper.getConnectionSource(), Message.class);

		
		File file = getFileStreamPath("priv");
		file.delete();
		file = getFileStreamPath("pub");
		file.delete();
		file = getFileStreamPath("pass");
		file.delete();
		finish();
		
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
	}

}