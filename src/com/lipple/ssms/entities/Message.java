package com.lipple.ssms.entities;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Message {

	
	@DatabaseField(generatedId=true)
	private int id;
	
	@DatabaseField(foreign=true)
	private Contact from;
	
	@DatabaseField(foreign=true)
	private Contact to;
	
	@DatabaseField
	private String message;
	
	@DatabaseField
	private Date date;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Contact getFrom() {
		return from;
	}

	public void setFrom(Contact from) {
		this.from = from;
	}

	public Contact getTo() {
		return to;
	}

	public void setTo(Contact to) {
		this.to = to;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	
}
