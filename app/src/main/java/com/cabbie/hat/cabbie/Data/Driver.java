package com.cabbie.hat.cabbie.Data;

public class Driver {

    private String email, name, password, phoneNo;

    public Driver(String email, String password, String name, String phoneNo){
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNo = phoneNo;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getPhoneNo(){
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo){
        this.phoneNo = phoneNo;
    }

}
