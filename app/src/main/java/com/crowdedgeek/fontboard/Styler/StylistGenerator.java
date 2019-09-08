package com.crowdedgeek.fontboard.Styler;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;


import com.crowdedgeek.fontboard.model.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class StylistGenerator {

    private static final String PREF_NAME = "stylish_position.xml";
    private ArrayList<Style> mEncoders;
    @Nullable
    private Context mContext;

    public StylistGenerator() {
        this(null);
    }

    /**
     * @param context - if context not null, sort styles
     */
    public StylistGenerator(@Nullable Context context) {
        mContext = context;
        long time = System.currentTimeMillis();
        mEncoders = StylistFactory.makeStyle();
        if (context != null) sortEncoders(context);
        System.out.println("time = " + (System.currentTimeMillis() - time));
    }

    public void swapPosition(int fromPosition, int toPosition) {
        if (mContext == null) {
            return;
        }
        SharedPreferences pref = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key1 = Integer.toHexString(mEncoders.get(fromPosition).hashCode());
        int pos1 = pref.getInt(key1, fromPosition);
        String key2 = Integer.toHexString(mEncoders.get(toPosition).hashCode());
        int pos2 = pref.getInt(key2, toPosition);

        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(key1, pos2);
        editor.putInt(key2, pos1);
        editor.apply();

        Collections.swap(mEncoders, fromPosition, toPosition);
    }

    private void sortEncoders(Context context) {
        //init position, from 0 to mEncoders.size() - 1
        final HashMap<Style, Integer> positionMap = new HashMap<>();
        for (Integer i = 0; i < mEncoders.size(); i++) {
            positionMap.put(mEncoders.get(i), i);
        }

        //get data from SharedPreferences
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (pref.getInt(Integer.toHexString(mEncoders.get(0).hashCode()), -1) == -1) {
            SharedPreferences.Editor editor = pref.edit();
            for (int index = 0; index < mEncoders.size(); index++) {
                Style style = mEncoders.get(index);
                editor.putInt(Integer.toHexString(style.hashCode()), index);
            }
            editor.apply();
        }

        for (int index = 0; index < mEncoders.size(); index++) {
            Style style = mEncoders.get(index);
            String key = Integer.toHexString(style.hashCode());
            int position = pref.getInt(key, index);
            positionMap.put(style, position);
        }

        //sort mEncoders
        Collections.sort(mEncoders, new Comparator<Style>() {
            @Override
            public int compare(Style o1, Style o2) {
                return positionMap.get(o1).compareTo(positionMap.get(o2));
            }
        });
    }

    public ArrayList<String> generate(String input) {
        ArrayList<String> result = new ArrayList<>();
        for (Style style : mEncoders) {
            String encode = style.generate(input);
            result.add(encode);
        }
        return result;
    }
}
