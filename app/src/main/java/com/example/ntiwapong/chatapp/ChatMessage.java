package com.example.ntiwapong.chatapp;

/**
 * Created by ntiwapong on 2/23/2017 AD.
 */


/*
 * ChatMessage class for each Chat Message Object.
 * Stores message to be displayed and its corresponding display type.
 */
public class ChatMessage {

    private String message;
    private String format;
    private boolean displayed;

    /*
     * @Params: format determines if left or right chat bubbles or time chat format.
     *          message - raw message sent by user or time.
     */
    public ChatMessage(String format, String message){
        super();
        this.message = message;
        this.format = format;
        displayed = false;
    }
    
    public String getFormat() {
        return format;
    }

    public String getMessage() {
        return message;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }
}
