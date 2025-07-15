package com.example.myapplication.data.volley;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

public class MySingleton {

    private static MySingleton myInstance;
    private RequestQueue myRequestQueue;
    private Context mContext;

    public MySingleton(Context mContext) {
        this.mContext = mContext;
        myRequestQueue = getMyRequestQueue();
    }

    public RequestQueue getMyRequestQueue() { // 얘가 객체를 생성한거고 위의 my singleton 객체를 생성하면 리퀘스트큐 객체가 생성되는 것임.
        if(myRequestQueue == null ) {
            Cache cache = new DiskBasedCache(mContext.getCacheDir(), 1024*1024); //
            Network network = new BasicNetwork(new HurlStack());
            myRequestQueue = new RequestQueue(cache, network);
            myRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return myRequestQueue;
    }

    public static synchronized MySingleton getInstance(Context context) { // 얘는 뭐를 위한 거지? 생성자 같음. ??
        if (myInstance == null ) {
            myInstance = new MySingleton(context);
        }
        return myInstance;
    }

    public <T> void addToRequestQueue(Request<T> request) { // 얘는 또 뭐임....
        myRequestQueue.add(request);
    }
}
