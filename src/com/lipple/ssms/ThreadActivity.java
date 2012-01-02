package com.lipple.ssms;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.lipple.ssms.dbms.DBHelper;
import com.lipple.ssms.entities.Contact;
import com.lipple.ssms.entities.Message;

public class ThreadActivity extends OrmLiteBaseActivity<DBHelper> implements OnClickListener{

	
	private Contact selectedContact = null;
	List<String> messages = new ArrayList<String>();
	private Button sendButton = null;
	private AutoCompleteTextView textBox = null;
	
	//There should only ever be one instance, so this hsould be safe
	private static ThreadActivity instance = null;
	
	public void onDestroy()
	{
		instance=null;
		super.onDestroy();
	}
	
    public void onCreate(Bundle savedInstanceState) {
    	instance=this;
        super.onCreate(savedInstanceState);
        
        Log.i("ThreadActivity", "onCreate");
        selectedContact = getHelper().getSelectedContact();
        try {
			getHelper().getContactDao().refresh(selectedContact);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        setContentView(R.layout.thread);
        
        if(selectedContact.getContactKey() == null || selectedContact.getContactKey().length() == 0 || selectedContact.getMyKey() == null || selectedContact.getMyKey().length() == 0)
        {
        	startActivity(new Intent(this, ExchangeActivity.class));
        }
        
        
        Spinner spinner = (Spinner) this.findViewById(R.id.numberSpinner);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.selectedContact.getNumber(this)));
        
        setupChat();
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
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	selectedContact = getHelper().getSelectedContact();
    	if(selectedContact != null && (selectedContact.getContactKey() == null || selectedContact.getContactKey().length() == 0 || selectedContact.getMyKey() == null || selectedContact.getMyKey().length() == 0))
    	{
    		finish();
    	}
    }
    protected void setupChat()
    {

    	
    	AdView ad = (AdView) this.findViewById(R.id.adView);
    	ad.loadAd(new AdRequest());
        ListView lv = (ListView) this.findViewById(R.id.messageList);
        sendButton = (Button) this.findViewById(R.id.sendButton);
        textBox = (AutoCompleteTextView) this.findViewById(R.id.textField);
        
        //Set up our send message behavior
        sendButton.setOnClickListener(this);
        
        
        messages = getMessages();
        
        lv.setAdapter(new ArrayAdapter<String>(this, R.layout.message, messages));
        lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lv.setStackFromBottom(true);
        lv.setTextFilterEnabled(true);
    }
	private List<String> getMessages() {
		List<String> list = new ArrayList<String>();
		try {
			Dao<Message,Integer> mDao = getHelper().getMessageDao();
			if(selectedContact == null)
			{
				Log.i("ThreadActivity", "selectedContact null");
			}
			List<Message> messageList = mDao.queryBuilder().where().eq("from_id", selectedContact.getId()).or().eq("to_id", selectedContact.getId()).query();
			
			Collections.sort(messageList, new ThreadComparator());
			
			for(Message message : messageList)
			{
				mDao.refresh(message);
				if(message.getFrom().equals(selectedContact))
				{
					list.add(selectedContact.getDisplayName(this) + ": " + message.getMessage());
				}else
				{
					list.add("Me: " + message.getMessage());
				}
			}
			
			
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}	
	
	protected static ThreadActivity getInstance()
	{
		return instance;
	}
	
	
    public void onClick(View v) {
        // Send a message using content of the edit text widget
        AutoCompleteTextView view = (AutoCompleteTextView) findViewById(R.id.textField);
        Spinner spinner = (Spinner) findViewById(R.id.numberSpinner);
        String message = view.getText().toString();
        if(message.trim().length() == 0)
        	return;
        if(message.length() > 60)
        {
        	int x=0;
        	do{
        		SSMSService.getInstance().sendMessage(message.substring(x, message.length() < (x+60) ? message.length() : x+60), (String)spinner.getSelectedItem(), selectedContact);       
        		x += 60;
        	}while(x<message.length());
        }else
        {        
        	SSMSService.getInstance().sendMessage(message, (String)spinner.getSelectedItem(), selectedContact);      
        }
        
        
        //SSMSService.getInstance().sendRequestSMS();
        setupChat();
        view.setText("");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.key_info:
            startActivity(new Intent(this, ExchangeActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
