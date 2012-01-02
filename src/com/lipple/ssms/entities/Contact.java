package com.lipple.ssms.entities;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Contact {

	@DatabaseField(generatedId=true)
	private int id;
	
	@DatabaseField(index=true, unique=true)
	private String lookupKey;
	
	@DatabaseField
	private String myKey;
	
	@DatabaseField
	private String contactKey;
	
	@DatabaseField
	private String modulus;
	
	@DatabaseField
	private String exponent;
	
	@DatabaseField
	private String lastNumber;
	
	@DatabaseField(persisted=false)
	private String displayName;
	
	public Contact() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMyKey() {
		return myKey;
	}

	public void setMyKey(String myKey) {
		this.myKey = myKey;
	}

	public String getContactKey() {
		return contactKey;
	}

	public void setContactKey(String contactKey) {
		this.contactKey = contactKey;
	}

	
	public String getModulus() {
		return modulus;
	}

	public void setModulus(String modulus) {
		this.modulus = modulus;
	}

	public String getExponent() {
		return exponent;
	}

	public void setExponent(String exponent) {
		this.exponent = exponent;
	}

	public String getLastNumber() {
		return lastNumber;
	}

	public void setLastNumber(String lastNumber) {
		this.lastNumber = lastNumber;
	}

	public String getLookupKey() {
		return lookupKey;
	}

	public void setLookupKey(String lookupKey) {
		this.lookupKey = lookupKey;
	}

	public String getDisplayName(Context context)
	{
		if(displayName == null)
		{
			//List<Contact> contacts = new ArrayList<Contact>();
			// Form an array specifying which columns to return. 
			String[] projection = new String[] {
										 Contacts.DISPLAY_NAME
			                          };

			// Get the base URI for the People table in the Contacts content provider.
			Uri contactsUri =  Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
			ContentResolver cr = context.getContentResolver();

			// Make the query. 
			Cursor managedCursor = cr.query(contactsUri,
			                         projection, // Which columns to return 
			                         null,       // Which rows to return (all rows)
			                         null,       // Selection arguments (none)
			                         // Put the results in ascending order by name
			                         null);
		
		if(!managedCursor.moveToFirst())
		{
			Log.e("SSMS", "Contact no longer in phone - removing from database");
			return null;
		}else
		{
			return (displayName = managedCursor.getString(0));
		}
		}else
			return displayName;
	}
	

	
	public List<String> getNumber(Context context) {
		//List<Contact> contacts = new ArrayList<Contact>();
				// Form an array specifying which columns to return. 
				String[] projection = new String[] {
											 Phone.NUMBER
				                          };

				// Get the base URI for the People table in the Contacts content provider.
				Uri contactsUri =  ContactsContract.Data.CONTENT_URI;
				ContentResolver cr = context.getContentResolver();

				// Make the query. 
				Cursor managedCursor = cr.query(contactsUri,
				                         projection, // Which columns to return 
				                         Phone.CONTACT_ID + "=" + getContactId(context) + " AND " + Phone.TYPE + "=" + Phone.TYPE_MOBILE,       // Which rows to return (all rows)
				                         null,       // Selection arguments (none)
				                         // Put the results in ascending order by name
				                         null);
				List<String> numbers = new ArrayList<String>();
				if(lastNumber != null && lastNumber.length() > 0)
				{
					numbers.add(lastNumber);
				}
				
				while(managedCursor.moveToNext())
				{
					if(PhoneNumberUtils.isWellFormedSmsAddress(managedCursor.getString(0)))
					{
						numbers.add(PhoneNumberUtils.formatNumber(managedCursor.getString(0)));
					}
				}
				
				return numbers;
	}

	private long getContactId(Context context) {
		String[] projection = new String[] {
				 Contacts._ID
            };
		Uri uri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
		ContentResolver cr = context.getContentResolver();

		// Make the query. 
		Cursor managedCursor = cr.query(uri,
		                         projection, // Which columns to return 
		                         null,       // Which rows to return (all rows)
		                         null,       // Selection arguments (none)
		                         // Put the results in ascending order by name
		                         null);
		managedCursor.moveToNext();
		return managedCursor.getLong(0);
		
	}

	@Override
	public boolean equals(Object o) {
		Contact impostor = (Contact) o;
		if(this.id == impostor.getId())
			return true;
		return false;
	}
}
