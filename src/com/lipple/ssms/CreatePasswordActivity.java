package com.lipple.ssms;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.lipple.ssms.dbms.DBHelper;

public class CreatePasswordActivity extends OrmLiteBaseActivity<DBHelper> implements OnClickListener {

	private CreatePasswordActivity instance = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		instance = this;
	    super.onCreate(savedInstanceState);

	    
	    setContentView(R.layout.create_password);
	    setup();
	}
	private void setup() {

		Button button = (Button)findViewById(R.id.createPasswordButton);
		button.setOnClickListener(this);
	}
	public void onClick(View v) {
		try {
		String password1 = ((EditText)findViewById(R.id.password1)).getText().toString().trim();
		String password2 = ((EditText)findViewById(R.id.password2)).getText().toString().trim();
		if(password1.equals(password2))
		{
    	    FileOutputStream fos = null;

				fos = openFileOutput("pass", Context.MODE_PRIVATE);
			
    	    DataOutputStream out = new DataOutputStream(fos);
    	    BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out));
    	    
    		MessageDigest algorithm = null;
    		try {
    			algorithm = MessageDigest.getInstance("MD5");
    		} catch (NoSuchAlgorithmException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		algorithm.reset();
    		algorithm.update(password1.getBytes());
    		byte messageDigest[] = algorithm.digest();
    	            
    		StringBuffer hexString = new StringBuffer();
    		for (int i=0;i<messageDigest.length;i++) {
    			hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
    		}
			wr.write(hexString.toString() + "\n");
			wr.close();
			finish();
			return;
		}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Context context = getApplicationContext();
			CharSequence text = e1.getMessage();
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		((EditText)findViewById(R.id.password1)).setText("");
		((EditText)findViewById(R.id.password2)).setText("");
		
		Context context = getApplicationContext();
		CharSequence text = "Passwords did not match";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
			//setup();
		
	}
}
