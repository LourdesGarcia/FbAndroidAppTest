package com.example.android.model;
 
public class User {
 
    private int id;
    private String fb_user_id;
    private String name;
 
    public User(){}
 
    public User(String fb_user_id, String name) {
        super();
        this.fb_user_id = fb_user_id;
        this.name = name;
    }
 
    //getters & setters
 
    @Override
    public String toString() {
        return "User [id=" + id + ", fb_user_id=" + fb_user_id + ", name=" + name
                + "]";
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getFbUserId() {
        return this.fb_user_id;
    }
    
    public String getName() {
        return  this.name;
    }
    
    public void setId(int id){
    	this.id = id;
    }
    
    public void setFbUserId(String fbUserId){
    	this.fb_user_id = fbUserId;
    }
    
    public void setName(String name){
    	this.name = name;
    }
}