package com.example.ntiwapong.chatapp;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.SharedPreferences.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREFKEY_ChatHistory = "ChatHistory";
    private static final String PREFKEY_Time = "ChatTime";

    private final String CHAT_FORMAT_HOME = "HOME";
    private final String CHAT_FORMAT_CLIENT ="CLIENT";
    private final String CHAT_FORMAT_TIME = "TIME";
    private final String CHAT_FORMAT_BILL_STATEMENT = "BILL";

    private ChatMessageAdapter chatMessageArrayAdapter;
    private ListView chatListView;
    private EditText chatEditText;
    private List<ChatMessage> chatMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //**Uncomment below to reset Chat Application**
        //deleteChatHistory();

        //Set to use Custom Action Bar
        View v = getLayoutInflater().inflate(R.layout.action_bar, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        getSupportActionBar().setCustomView(v,params);
        getSupportActionBar().setDisplayShowCustomEnabled(true); //show custom title
        getSupportActionBar().setDisplayShowTitleEnabled(false); //hide default title

        chatListView = (ListView) findViewById(R.id.ChatBox);   //list view to populate to chat adapter.

        //Reload previous chat message list. If none, initialize chat message list.
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String jsonChatMessageObj = sharedPrefs.getString(PREFKEY_ChatHistory, null);
        Type type = new TypeToken<ArrayList<ChatMessage>>() {}.getType();
        chatMessageList = gson.fromJson(jsonChatMessageObj, type);

        //Initialize chat message list if previous chat messages do not exist.
        if(chatMessageList == null)
            chatMessageList = new ArrayList<ChatMessage>();


        chatMessageArrayAdapter = new ChatMessageAdapter(this, android.R.layout.simple_list_item_1,chatMessageList);    //custom chat message adapter

        chatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);  //make list view scrollable
        chatListView.setAdapter(chatMessageArrayAdapter);

        //Grab and display current time - displays time stamp everytime user re-opens app.
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("h:mm a");
        String currTime = format.format(currentDate);

        chatMessageList.add(new ChatMessage(CHAT_FORMAT_TIME, currTime));
        saveToChatHistory(PREFKEY_Time, currTime);
        Log.i(TAG, "Displaying time stamp: " + currTime);

        //chat edit text
        chatEditText = (EditText) findViewById(R.id.ChatEditText);
        chatEditText.requestFocus();
        chatEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_SEND){
                    sendChatMessage();      //save message and display
                    handled = true;
                }
                return handled;
            }
        });
    }


    /*
     * This method checks validity, stores, displays, and responds to message from edit text.
     */
    private void sendChatMessage(){
        final String message = chatEditText.getText().toString();
        if(message.isEmpty() || message.matches("[ ]{1,}")){
            Log.i(TAG, "Message is empty.");    //message empty - do not display.
            return;
        }

        ChatMessage chatMessageToAdd = new ChatMessage(CHAT_FORMAT_CLIENT, message);
        chatMessageArrayAdapter.add(chatMessageToAdd);
        saveToChatHistory(PREFKEY_ChatHistory, message);

        Log.i(TAG, "Displaying chat message from client: " + message);

        //automate message set to respond after 0.8 seconds
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                respondToMessage(message);
            }
        }, 800);

        chatEditText.setText("");   //re-set chat edit text to empty
    }


    /*
     * @Params: message is client's message to respond to.
     * Acts as the company - responds to client
     * if client's chat message contains "bill" return with bill info.
     * else returns same message.
     *
     */
    private void respondToMessage(String message){

        if(!respondToMessageUtil(message)){     //message does not contain "bill"

            ChatMessage chatMessageToAdd = new ChatMessage(CHAT_FORMAT_HOME, message);
            chatMessageArrayAdapter.add(chatMessageToAdd);      //display client's message
            saveToChatHistory(PREFKEY_ChatHistory, message);   //save message to chat history
            Log.i(TAG, "Displaying chat message from home: " + message);
        }
        else{
            String intro_stmt = "Here is your current statement:";
            ChatMessage intro_statement = new ChatMessage(CHAT_FORMAT_HOME, intro_stmt);
            chatMessageArrayAdapter.add(intro_statement);
            saveToChatHistory(PREFKEY_ChatHistory, intro_stmt);
            Log.i(TAG, "Displaying chat message from home: " + intro_stmt);

            //automate message set to respond after 0.8 seconds
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    ChatMessage bill = new ChatMessage(CHAT_FORMAT_BILL_STATEMENT, "BILL");  //bill statement info hard coded for now.
                    chatMessageArrayAdapter.add(bill);
                    saveToChatHistory(PREFKEY_ChatHistory, "BILL");
                    Log.i(TAG, "Displaying Bill Statement Information");
                }
            }, 800);
        }
    }

    /*
     * @Params: message is client's message to respond to.
     * @Return: true if message contains "bill" and false otherwise.
     * Utility method to determine if message contains substring "bill"
     */
    private boolean respondToMessageUtil(String message){
        final String searchFor = "bill";
        Pattern p = Pattern.compile(searchFor, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(message);
        if(m.find())
            return true;
        return false;
    }

    /*
     * @Params: prefKey is Shared Preference Key to store to, message is chat message to store.
     * This method uses GSON Library to store message as String in JSON format to Shared Preference.
     */
    private void saveToChatHistory(String prefKey, String message){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();     //convert list to JSON using GSON Library
        String jsonChatMessageObj = gson.toJson(ChatMessageAdapter.getChatMessageList());   //store data as String in JSON format.
        if(prefKey == PREFKEY_ChatHistory) {
            editor.putString(prefKey, jsonChatMessageObj);
            Log.i(TAG, "Storing to shared preference for PREFKEY:" + prefKey + " || message:" + jsonChatMessageObj);
        }
        if(prefKey == PREFKEY_Time) {
            editor.putString(prefKey, message);
            Log.i(TAG, "Storing to shared preference for PREFKEY:" + prefKey + " || and message:" + message);
        }
        editor.apply();     //apply changes to shared preference.
    }

    //removes all data from Shared Preference.
    private void deleteChatHistory(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Editor editor = sharedPrefs.edit();

        editor.remove(PREFKEY_ChatHistory);
        editor.remove(PREFKEY_Time);
        editor.apply();
    }
}
