package com.example.ntiwapong.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;

import java.util.*;

import android.widget.*;

/**
 * Created by ntiwapong on 2/23/2017 AD.
 */

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    private static final String TAG = "ChatMessageAdapter";

    private final String CHAT_FORMAT_HOME = "HOME";
    private final String CHAT_FORMAT_CLIENT ="CLIENT";
    private final String CHAT_FORMAT_TIME = "TIME";
    private final String CHAT_FORMAT_BILL_STATEMENT = "BILL";

    private static List<ChatMessage> chatMessageList;
    private ScaleAnimation scaleAnimLeftToRight;
    private ScaleAnimation scaleAnimRightToLeft;

    public ChatMessageAdapter(Context context, int resource, List<ChatMessage> objects) {
        super(context, resource, objects);
        chatMessageList = objects;

        //Animation for left chat bubble.
        scaleAnimLeftToRight = new ScaleAnimation(0f,1f,0f,1f, Animation.ABSOLUTE,0f,Animation.RELATIVE_TO_SELF,1f);
        scaleAnimLeftToRight.setDuration(800);
        scaleAnimLeftToRight.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnimLeftToRight.setFillAfter(true);
        scaleAnimLeftToRight.setFillBefore(true);
        scaleAnimLeftToRight.setFillEnabled(true);

        //Animation for right chat bubble.
        scaleAnimRightToLeft = new ScaleAnimation(0f,1f,0f, 1f, Animation.RELATIVE_TO_SELF,1f, Animation.RELATIVE_TO_SELF, 1f);
        scaleAnimRightToLeft.setDuration(800);
        scaleAnimRightToLeft.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnimRightToLeft.setFillAfter(true);
        scaleAnimRightToLeft.setFillBefore(true);
        scaleAnimRightToLeft.setFillEnabled(true);

    }

    @Override
    public void add(ChatMessage object) {
        super.add(object);
    }

    public static List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    @Override
    public int getCount() {
        return this.chatMessageList.size();
    }

    @Override
    public ChatMessage getItem(int position) {
        return this.chatMessageList.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ChatMessage chatMessageObj = this.chatMessageList.get(position);    //current ChatMessage object we are getting View for.

        final View view;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ScaleAnimation animation = null;    //Animation for chat bubbles.

        //Determine format chat message: left chat bubble, right chat bubble, time stamp
        //Note: Time stamp updates everytime user quits and re-opens the application where the time is different from time stamp of last conversation.
        String format = chatMessageObj.getFormat();

        //View for Bill Statement
        if(CHAT_FORMAT_BILL_STATEMENT.equals(format)) {
            view = inflater.inflate(R.layout.bill_statement, null);
            return getViewBillStatement(view, chatMessageObj);
        }

        if(CHAT_FORMAT_HOME.equals(format)) {
            view = inflater.inflate(R.layout.left, null);
            animation = scaleAnimLeftToRight;
        }
        else if(CHAT_FORMAT_CLIENT.equals(format)) {
            view = inflater.inflate(R.layout.right, null);
            animation = scaleAnimRightToLeft;
        }
        else if(CHAT_FORMAT_TIME.equals(format)) {
            view = inflater.inflate(R.layout.time_format, null);
        }
        else
            view = convertView;

        String message = chatMessageObj.getMessage();
        TextView chatText = (TextView) view.findViewById(R.id.messageBox);
        chatText.setText(message);  //Populate chat text with message.


        //Only animate on the most recently added chat message && only for left/right chat bubbles where animation is not null.
        //Animate only once. set displayed to true once finished animation.
        if(position == getCount()-1 && animation != null && !getItem(position).isDisplayed()){
            view.startAnimation(animation);
            animation.setAnimationListener(new ScaleAnimation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    getItem(position).setDisplayed(true);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

        }
        return view;
    }

    /*
     * Returns view for bill statement. Information hard-coded as of now.
     */
    private View getViewBillStatement(final View view, final ChatMessage chatMessageObj){

        View row1 = view.findViewById(R.id.row1);
        View row2 = view.findViewById(R.id.row2);
        View row3 = view.findViewById(R.id.row3);
        View separator1 = view.findViewById(R.id.separator1);
        View separator2 = view.findViewById(R.id.separator2);

        final View row4 = view.findViewById(R.id.row4);
        final View row5 = view.findViewById(R.id.row5);

        final Animation fade_in = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in);

        if(!chatMessageObj.isDisplayed()) {
            row1.startAnimation(fade_in);
            row2.startAnimation(fade_in);
            row3.startAnimation(fade_in);
            separator1.startAnimation(fade_in);
            separator2.startAnimation(fade_in);

            row4.setVisibility(View.INVISIBLE);
            row5.setVisibility(View.INVISIBLE);

            fade_in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Animation fade_in2 = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                    fade_in2.setFillAfter(true);
                    fade_in2.setFillBefore(false);
                    fade_in2.setFillEnabled(true);
                    row4.startAnimation(fade_in2);
                    row5.startAnimation(fade_in2);

                    chatMessageObj.setDisplayed(true);      //set displayed to true to animate only once.
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        return view;
    }

}
