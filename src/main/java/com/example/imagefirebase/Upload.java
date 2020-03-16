package com.example.imagefirebase;

public class Upload {
    private String mName;
    private String mImageUrl;

    public Upload(){
        //empty contructor needed
    }

    public Upload(String name, String imageUrl){
        //if someone doesn't type a name, put it as "No name"
        if(name.trim().equals("")){
            mName = "No name";
        } else mName = name;
        mImageUrl = imageUrl;
    }

    public String getName(){
        return mName;
    }

    public void setName(String name){
        mName = name;
    }

    public String getImageUrl(){
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl){
        mImageUrl = imageUrl;
    }
}
