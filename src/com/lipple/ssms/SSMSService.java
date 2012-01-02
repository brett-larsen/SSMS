package com.lipple.ssms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteBaseService;
import com.j256.ormlite.dao.Dao;
import com.lipple.ssms.dbms.DBHelper;
import com.lipple.ssms.entities.Contact;
import com.lipple.ssms.entities.Message;

public class SSMSService extends OrmLiteBaseService<DBHelper> {
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new ServiceBinder();
    private static String TAG = "SSMSService";
    protected static int instances = 0;
    private static SSMSService instance = null;
    private Date lastPrune = null;
    private Map<Integer, Integer> reqs = new HashMap<Integer, Integer>();

	String pubModulus=null,pubExponent=null, privModulus=null, privExponent=null;
    
	

    @Override
    public void onCreate()
    {
    	super.onCreate();
    	this.getHelper();
    	load_keys();
    	instance=this;
    }
    
    private void load_keys() {
    	try{
    	File file = getFileStreamPath("priv");
    	if(!file.exists())
    	{
    		try{

    	    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    	    keyPairGenerator.initialize(512);
    	    KeyPair keyPair = keyPairGenerator.genKeyPair();
    	    
    	    System.out.println("Generated pub key:" + bytesToString(keyPair.getPublic().getEncoded()));
    	    System.out.println("Generated pub key:" + keyPair.getPublic().toString());

    	    FileOutputStream fos = openFileOutput("pub", Context.MODE_PRIVATE);
    	    DataOutputStream out = new DataOutputStream(fos);
    	    BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out));
    	    //wr.write(//keyPair.getPublic())
    	    RSAPublicKey rsaPub = (RSAPublicKey)keyPair.getPublic();
    	    wr.write(rsaPub.getModulus().toString(16) + "\n");
    	    wr.write(rsaPub.getPublicExponent() + "\n");
    	    wr.close();
    	    
    	    fos = openFileOutput("priv", Context.MODE_PRIVATE);
    	    out = new DataOutputStream(fos);
    	    wr = new BufferedWriter(new OutputStreamWriter(out));
    	    RSAPrivateKey rsaPriv = (RSAPrivateKey)keyPair.getPrivate();
    	    wr.write(rsaPriv.getModulus().toString(16) + "\n");
    	    wr.write(rsaPriv.getPrivateExponent() + "\n");
    	    wr.close();
    	    
    	    
    		}catch(Exception e)
    		{
    			e.printStackTrace();
    			this.stopSelf();
    		}
    	}
    	
    	FileInputStream fIn = openFileInput("pub");
    	DataInputStream in = new DataInputStream(fIn);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        Log.i(TAG, "pubModulus: " + (pubModulus = br.readLine()));
        Log.i(TAG, "pubExponent: " + (pubExponent = br.readLine()));
        //pub = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(modulusStr, 16), new BigInteger(expStr)));
    	fIn = openFileInput("priv");
    	in = new DataInputStream(fIn);
        br = new BufferedReader(new InputStreamReader(in));
        Log.i(TAG, "privModulusd: " + (privModulus = br.readLine()));
        Log.i(TAG, "privExponent: " + ( privExponent = br.readLine()));
        
        if(pubModulus.length() == 0 || pubExponent.length() == 0)
        {
        	throw new Exception("FATAL: Could not load RSA keys");
        }
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
	}

	@Override
    public void onDestroy()
    {
    	instance=null;
    }
    
    public static SSMSService getInstance()
    {
    	return instance;
    }
    
	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


	
	public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        instance=this;
        //if we were started because the root activity is starting, notify it we are ready to be used now
        if(SSMSActivity.getInstance()!= null)
        {
        	SSMSActivity.getInstance().setup();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
	
	public class ServiceBinder extends Binder {
        SSMSService getService() {
            return SSMSService.this;
        }
    }

	public void dump(FileDescriptor fd, String[] args) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public String getInterfaceDescriptor() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isBinderAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean pingBinder() {
		// TODO Auto-generated method stub
		return false;
	}

	public IInterface queryLocalInterface(String descriptor) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean transact(int code, Parcel data, Parcel reply, int flags)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public synchronized void receiveMessage(Context context, String originatingAddress,
			String messageBody) {
		messageBody = messageBody.trim();
		List<Contact> contacts = getContactFromAddress(originatingAddress);
		
		for(Contact contact : contacts)
		{
		contact.setLastNumber(originatingAddress);
		try {
			getHelper().getContactDao().update(contact);
		} catch (SQLException e1) {
			
		}
		 SecretKeySpec key = new SecretKeySpec(hexStringToByteArray(contact.getContactKey()), "AES");

		 try{
		    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");

		    // encryption pass
		    cipher.init(Cipher.ENCRYPT_MODE, key);

		    //byte[] cipherText = new byte[cipher.getOutputSize(messageB.length)];


		    cipher.init(Cipher.DECRYPT_MODE, key);
		       byte[] original =
		         cipher.doFinal(hexStringToByteArray(messageBody));
		       messageBody = new String(original);
		       Log.i(TAG, "Decrypted message: " + messageBody);
		       
		 }catch(Exception e)
		 {
			 e.printStackTrace();
			 Log.e(TAG, "ERROR decrypting message - recommend clearing data and exchanging keys again");
		 }
		
		try {
			Dao<Message,Integer> mDao = getHelper().getMessageDao();
			
			Message message = new Message();
			message.setDate(new Date());
			message.setFrom(contact);
			message.setMessage(messageBody);
			mDao.create(message);
			Log.i(TAG, "Message converted");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Log.e(TAG,e.getMessage());
		}
		Log.i(TAG, "Message stored");
		prune();
		
		ThreadActivity thread;
		if((thread=ThreadActivity.getInstance()) != null && getHelper().getSelectedContact().equals(contact))
		{
			thread.setupChat();
		}
		}
		
		
	}
	
	public List<Contact> getContactFromAddress(String address)
	{
		List<Contact> contacts = new ArrayList<Contact>();
		try {
			
			Log.i(TAG, "Getting contact for sender: " + address);
	
			String[] projection = new String[] {
					 PhoneLookup.LOOKUP_KEY
	             };
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
			ContentResolver cr = getContentResolver();
	
			// Make the query. 
			Cursor managedCursor = cr.query(uri,
			                         projection, // Which columns to return 
			                         null,       // Which rows to return (all rows)
			                         null,       // Selection arguments (none)
			                         // Put the results in ascending order by name
			                         null);
			Dao<Contact, Integer> cDao;
			cDao = getHelper().getContactDao();
			while(managedCursor.moveToNext())
			{
				contacts.add(getContactFromLookupKey(managedCursor.getString(0)));
			}
			return contacts;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			return contacts;
	}

	protected void prune() {
		long now = new Date().getTime();
		if(lastPrune == null || now - lastPrune.getTime() >  6000 )
		{
			Log.i(TAG, "PRUNING");
			try {
				
				Dao<Message, Integer> mDao = getHelper().getMessageDao();
				List<Message> messageList = mDao.queryForAll();
				List<Message> deleteList = new ArrayList<Message>();
				for(Message message : messageList)
				{
					mDao.refresh(message);
					if(now - message.getDate().getTime() > 86400000)
					{
						deleteList.add(message);
					}
				}
				
				if(mDao.delete(deleteList) != deleteList.size())
				{
					lastPrune = new Date();
					throw new SQLException("Could not delete old messages");
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage(String message, String number, Contact contact) {
		byte[] messageB = message.getBytes();
		try {
			getHelper().getDao(Contact.class).refresh(contact);
			contact.setLastNumber(number);
			getHelper().getContactDao().update(contact);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		Message dbMsg = new Message();
		dbMsg.setTo(contact);
		dbMsg.setMessage(message);
		dbMsg.setDate(new Date());
		dbMsg.setFrom(null);
		try {
			if(getHelper().getDao(Message.class).create(dbMsg) != 1)
			{
				Log.e(TAG, "Error storing outgoing SMS in local database - send aborted");
				return;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * Encrypt the message
		 */
		String encryptedMessage = null;
		try{
			byte[] keyBytes = hexStringToByteArray(contact.getMyKey());
			Log.i(TAG, "keyBytes.length="+keyBytes.length);
		 SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		 Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");

		    System.out.println(new String(messageB));

		    // encryption pass
		    cipher.init(Cipher.ENCRYPT_MODE, key);

		    byte[] encrypted = cipher.doFinal(message.getBytes());
		    System.out.println("encrypted string: " + asHex(encrypted));
		    encryptedMessage = asHex(encrypted);
		}catch(Exception e)
		{
			e.printStackTrace();
			Log.i(TAG, "Encrytpion error sending user message");
		}
		//this should be very quick since pruning was  done at activity start
		prune();
		sendSMS(number, "<SSMS> MSG " + encryptedMessage);
		
		
	}
	
	
    protected void sendSMS(String phoneNumber, String message)
    {        
        //PendingIntent pi = PendingIntent.getActivity(this, 0,
       //     new Intent(this, ThreadActivity.class), 0);                
        SmsManager sms = SmsManager.getDefault();
        Log.i(TAG, "Sending message:" + message);
        sms.sendTextMessage(phoneNumber, null, message, null, null);        
    } 

    protected void sendRequestSMS(Contact contact, String number)
    {
    	try {
			getHelper().getContactDao().refresh(contact);
		
    	contact.setMyKey("");
    	contact.setContactKey("");
    	
    	getHelper().getContactDao().update(contact);
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	sendSMS(number, "<SSMS> REQ " + pubModulus + " " + pubExponent);

    }
    
    protected void sendAckReq(String number)
    {
    	sendSMS(number, "<SSMS> AREQ " + pubModulus + " " + pubExponent);
    }
    
    private String bytesToString(byte[] bytes)
    {
        StringBuffer retString = new StringBuffer();
        for (int i = 0; i < bytes.length; ++i) {
            retString.append(bytes[i]);
            retString.append(" ");
        }
        //retString = retString.delete(retString.length()-1,retString.length());
        return retString.toString();
    }


	public void processRequestMessage(Context context,
			String originatingAddress, String message) {
		try {
		
		List<Contact> contacts = getContactFromAddress(originatingAddress);
		Contact contact;
		if(contacts.size() > 0)
			contact = contacts.get(0);
		else
			return;
		getHelper().getContactDao().refresh(contact);
		contact.setLastNumber(originatingAddress);
		getHelper().getContactDao().update(contact);
		
		String[] fields = message.trim().split(" ");
		/*byte[] bytes = new byte[byteString.length];
		for(int x=0; x<byteString.length; x++)
		{
			bytes[x] = (byte) Integer.parseInt(byteString[x]);
		}*/
		contact.setModulus(fields[0]);
		contact.setExponent(fields[1]);
		


		//contact.setPubKey(message);
		getHelper().getContactDao().update(contact);
		makeMyKey(contact);
		//getHelper().getContactDao().refresh(contact);
		
		
		PublicKey publicKey = 
			    KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(contact.getModulus(), 16), new BigInteger(contact.getExponent())));
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
	    
	    Log.i(TAG, "Sending key: " + contact.getMyKey());
	    byte[] encrypted = cipher.doFinal(contact.getMyKey().getBytes());
    	sendSMS(originatingAddress, "<SSMS> ACK " + asHex(encrypted));//reply with the key we will use to encrypt our SMS
    	ExchangeActivity ex = ExchangeActivity.getInstance();
    	if(ex!=null)
    	{
    		try{
    		ex.setupView();
    		}catch(Exception e)
    		{
    			
    		}
    	}
    	sendAckReq(originatingAddress);
    	/*
    	if(contact.getContactKey() == null || contact.getContactKey().length() == 0)
    	{
    		int count = reqs.get(contact.getId());
    		if(count == 0){
    			
    			reqs.put(contact.getId(), 1);
    			sendRequestSMS(contact, originatingAddress);
    		}
    		else{
    			Log.i(TAG, "possible message loop detected, restarting service");
    			throw new Exception("ERROR: possible message loop detected");
    		}
    	}else
    	{
    		//notify exchange activity that we are synced, if it exists
    	}*/
    	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "Could not get public key from message - aborting");
			return;
		}
		
	}

	private void makeMyKey(Contact contact) {
		try {
			Dao<Contact, Integer> cDao = getHelper().getContactDao();
			Key key = generateKey();
			Log.i("SSMSServiceKEY", bytesToString(key.getEncoded()));
			Log.i("SSMSServiceKEY", asHex(key.getEncoded()));
			Log.i("SSMSServiceKEY", bytesToString(hexStringToByteArray(asHex(key.getEncoded()))));
			cDao.refresh(contact);
			contact.setMyKey(asHex(key.getEncoded()));
			cDao.update(contact);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private Key generateKey() {
		KeyGenerator generator=null;
		try {
			generator = KeyGenerator.getInstance("AES");
			generator.init(new SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return generator.generateKey();
	}
	
	/**
     * Turns array of bytes into string
     *
     * @param buf	Array of bytes to convert to hex string
     * @return	Generated hex string
     */
     public static String asHex (byte buf[]) {
      StringBuffer strbuf = new StringBuffer(buf.length * 2);
      int i;

      for (i = 0; i < buf.length; i++) {
       if (((int) buf[i] & 0xff) < 0x10)
	    strbuf.append("0");

       strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
      }

      return strbuf.toString();
     }

	public void processAcknowledge(Context context, String originatingAddress,
			String message) {
		
		try{
		List<Contact> contacts = getContactFromAddress(originatingAddress);
		Contact contact = null;
		if(contacts.size() > 0)
			contact = contacts.get(0);
		else
			return;
		getHelper().getContactDao().refresh(contact);
		
		RSAPrivateKey key = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec(new BigInteger(privModulus, 16), new BigInteger(privExponent)));
		
		//RSAPrivateKey privKey = KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec())
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);
	    byte[] plainText = cipher.doFinal(hexStringToByteArray(message.trim()));
	    System.out.println("plain : " + new String(plainText));
	    
	    contact.setContactKey(new String(plainText));
	    getHelper().getContactDao().update(contact);
	    ExchangeActivity ex = ExchangeActivity.getInstance();
	    if(ex!=null && contact.getContactKey() != null && contact.getContactKey().length() > 0 && contact.getMyKey() != null && contact.getMyKey().length() > 0)
    	{
	    	getHelper().setSelectedContact(contact);
    		ex.finish();
    	}else if(ex != null)
    	{
    		ex.setupView();
    	}
		}catch(Exception e)
		{
			e.printStackTrace();
			Log.e(TAG,"Error parsing ACK message");
		}
	}
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

	public Contact getContactFromLookupKey(String lookupKey) {
		try {
			Dao<Contact, Integer> cDao= getHelper().getContactDao();
			
			Contact contact = new Contact();
			contact.setLookupKey(lookupKey);
			List<Contact> result = cDao.queryForMatching(contact);
			if(result.size() < 1)
			{
				contact = new Contact();
				contact.setLookupKey(lookupKey);
				cDao.create(contact);
				cDao.refresh(contact);
			}else if(result.size() > 1)
			{
				Log.e(TAG, "FATAL: multiple contacts found for lookup key: " + lookupKey);
				return null;
			}else{
				contact = result.get(0);
			}
			
			return contact;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void processAckReq(Context context, String originatingAddress,
			String message) {
		try {
			
		List<Contact> contacts = getContactFromAddress(originatingAddress);
		Contact contact;
		if(contacts.size() > 0)
			contact = contacts.get(0);
		else
		{
			Log.e(TAG, "Could not find contact for " + originatingAddress);
			return;
		}
			
		getHelper().getContactDao().refresh(contact);
		
		String[] fields = message.trim().split(" ");
		/*byte[] bytes = new byte[byteString.length];
		for(int x=0; x<byteString.length; x++)
		{
			bytes[x] = (byte) Integer.parseInt(byteString[x]);
		}*/
		contact.setModulus(fields[0]);
		contact.setExponent(fields[1]);
		getHelper().getContactDao().update(contact);
		makeMyKey(contact);
		getHelper().getContactDao().refresh(contact);
		
		
		PublicKey publicKey = 
			    KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(contact.getModulus(), 16), new BigInteger(contact.getExponent())));
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
	    
	    Log.i(TAG, "Sending key: " + contact.getMyKey());
	    byte[] encrypted = cipher.doFinal(contact.getMyKey().getBytes());
    	sendSMS(originatingAddress, "<SSMS> ACK " + asHex(encrypted));//reply with the key we will use to encrypt our SMS
    	ExchangeActivity ex = ExchangeActivity.getInstance();
    	if(ex!=null && contact.getContactKey() != null && contact.getContactKey().length() > 0)
    	{
    		getHelper().setSelectedContact(contact);
    		ex.finish();
    	}
    	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "Could not get public key from message - aborting");
			return;
		}
	}
}
