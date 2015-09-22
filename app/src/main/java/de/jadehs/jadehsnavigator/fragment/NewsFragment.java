/**
 * This file is part of JadeHS-Navigator.
 *
 * JadeHS-Navigator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JadeHS-Navigator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JadeHS-Navigator.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jadehs.jadehsnavigator.fragment;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.adapter.NewsPagerAdapter;
import de.jadehs.jadehsnavigator.model.RSSItem;
import de.jadehs.jadehsnavigator.model.RSSOrigin;
import de.jadehs.jadehsnavigator.response.RSSAsyncResponse;
import de.jadehs.jadehsnavigator.task.ParseRSSTask;
import de.jadehs.jadehsnavigator.util.Preferences;
import de.jadehs.jadehsnavigator.view.NewsTabLayout;

/**
 * Created by re1015 on 12.08.2015.
 */
public class NewsFragment extends Fragment implements RSSAsyncResponse {
    final String TAG = "NEWSFRAGMENT";

    private NewsTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;

    public NewsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateRSSFeeds();
    }

    public void updateRSSFeeds(){
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(isConnected) {
            try {
                /* Load RSS origins from asset file and create a list with RSS origins */
                ArrayList<RSSOrigin> origins = new ArrayList<RSSOrigin>();
                JSONObject obj = new JSONObject(loadJSONFromAsset());
                JSONArray jArray = obj.getJSONArray("origins");
                RSSOrigin rssOrigin;
                for(int i = 0; i < jArray.length(); i++){
                    JSONObject origin = jArray.getJSONObject(i);
                    rssOrigin = new RSSOrigin(origin.getLong("id"), origin.getString("title"), origin.getString("url"));
                    origins.add(rssOrigin);
                }
                /* Start parsing */
                ParseRSSTask rssTask = new ParseRSSTask(getActivity(), origins);
                rssTask.delegate = this;
                rssTask.execute();
            } catch (Exception ex) {
                Log.wtf(TAG, "Failed to parse", ex);
            }
        }else{
            Log.wtf(TAG, "NO INTERNET CONNECTION");

            getActivity().findViewById(R.id.errorOverlay).setVisibility(View.VISIBLE); // Displays the error overlay
        }
    }

    @Override
    public void processFinish(ArrayList<RSSItem> items) {
        try {
            getActivity().findViewById(R.id.progressNews).setVisibility(View.GONE);
            mViewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
            mViewPager.setAdapter(new NewsPagerAdapter(getActivity(), items));
            mSlidingTabLayout = (NewsTabLayout) getActivity().findViewById(R.id.sliding_tabs);
            mSlidingTabLayout.setViewPager(mViewPager);
        }catch (Exception ex){
            Log.wtf(TAG, "ProcessFinish() failed", ex);
        }
    }

    public String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getActivity().getAssets().open("origins.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.wtf("JSON", "ERR", ex);
            return null;
        }
        return json;
    }
}
