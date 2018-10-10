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
package de.jadehs.jadehsnavigator;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import de.jadehs.jadehsnavigator.adapter.NavDrawerListAdapter;
import de.jadehs.jadehsnavigator.database.DBHelper;
import de.jadehs.jadehsnavigator.fragment.AboutFragment;
import de.jadehs.jadehsnavigator.fragment.HomeFragment;
import de.jadehs.jadehsnavigator.fragment.InfoSysFragment;
import de.jadehs.jadehsnavigator.fragment.MapFragment;
import de.jadehs.jadehsnavigator.fragment.MensaplanFragment;
import de.jadehs.jadehsnavigator.fragment.NewsFragment;
import de.jadehs.jadehsnavigator.fragment.VorlesungsplanFragment;
import de.jadehs.jadehsnavigator.model.NavDrawerItem;
import de.jadehs.jadehsnavigator.util.Preferences;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private BroadcastReceiver registrationBroadcastReceiver;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /***** DATABSE SANITY CHECK ****/
        try {
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            dbHelper.getWritableDatabase();
        }catch(Exception ex){
            Log.wtf(TAG, "Err", ex);
        }

        /**** START GCM INIT ****/
        //@todo: Wird vorerst nicht implementiert, da nicht essentiell und kein Server bereitsteht..
        /*
        registrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Preferences preferences = new Preferences(getApplicationContext());
                Toast.makeText(getApplicationContext(), "DEBUG: Broadcast erhalten", Toast.LENGTH_LONG).show();
            }
        };
        */
        /*** END GCM INIT ***/

        mTitle = mDrawerTitle = getTitle();

        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        //navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1))); //home
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1))); //neuigkeiten
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1))); //infosys
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1))); //vorlesungsplan
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1))); //mensaplan
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1))); //lageplan
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1))); //about
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(6, -1))); //settings

        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        Preferences preferences = new Preferences(this);
        if (savedInstanceState == null) {
            //displayView(1);
            int index = Integer.parseInt(preferences.get("IndexPreference_list", "1"));
            displayView(index);
        }

        /*
        * @todo: Gehört zu Google Cloud Messages. Vorerst ausgeschaltet.
       if (checkPlayServices()){
           // Starte Registration..
           Intent intent = new Intent(this, RegistrationIntentService.class);
           startService(intent);
       }
       */
        /***** START FIRST TIME SETUP ****/

        if(!preferences.getBoolean("setupDone", false)){
            Log.wtf(TAG, "Setup is not yet done");
            // show the user that there is a drawer menu
            mDrawerLayout.openDrawer(Gravity.LEFT);
            // don't show this dialog again and set the flag to remind of feedback
            preferences.save("setupDone", true);
            preferences.save("feedbackReminderSeen", false);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setMessage(getApplicationContext().getString(R.string.alert_firsttimesetup))
                    .setCancelable(true)
                    .setPositiveButton(getApplicationContext().getString(R.string.positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                            } catch (Exception ex) {
                                Log.wtf(TAG, "Preference Activity failed", ex);
                            }
                        }
                    })
                    .setNegativeButton(getApplicationContext().getString(R.string.negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alert.show();
        }else if(preferences.getBoolean("setupDone", false) && !preferences.getBoolean("feedbackReminderSeen", false)){
            // remind to leave feedback
            preferences.save("feedbackReminderSeen", true);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setMessage(getApplicationContext().getString(R.string.alert_feedbackreminder))
                    .setCancelable(true)
                    .setPositiveButton(getApplicationContext().getString(R.string.positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                            } catch (Exception ex) {
                                Log.wtf(TAG, "Store Activity failed", ex);
                            }
                        }
                    })
                    .setNegativeButton(getApplicationContext().getString(R.string.negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            //alert.show();
        }else{
            // we already showed the reminder
            Log.wtf(TAG, "Setup is already done. Business as usual");
        }
        /**** END FIRST TIME SETUP ****/

    }

    /**
     * Slide menu item click listener
    */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                // open preferences (global)
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private void displayView(int position) {
        // show fragment
        Fragment fragment = null;
        String fragmentTag = "";
        switch (position) {
            case 0:
                fragment = new NewsFragment();
                fragmentTag = navMenuTitles[1];
                break;
            case 1:
                fragment = new InfoSysFragment();
                fragmentTag = navMenuTitles[2];
                break;
            case 2:
                fragment = new VorlesungsplanFragment();
                fragmentTag = navMenuTitles[3];
                break;
            case 3:
                fragment = new MensaplanFragment();
                fragmentTag = navMenuTitles[4];
                break;
            case 4:
                fragment = new MapFragment();
                fragmentTag = navMenuTitles[5];
                break;
            case 5:
                fragment = new AboutFragment();
                fragmentTag = navMenuTitles[6];
                break;
            case 6:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment, fragmentTag)
                    .addToBackStack(fragment.getTag())
                    .commit();
            mDrawerLayout.closeDrawer(mDrawerList);
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
        } else {
            Log.e(TAG, "Error while creating fragment");
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            // jump to previous fragment
            fragmentManager.popBackStackImmediate();
            FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
            setTitle(entry.getName());
        }else{
            // quit the app
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        // @todo: Gehört ebenfalls zu GCM. Implementierung erfolgt später.
        //LocalBroadcastManager.getInstance(this).registerReceiver(this.registrationBroadcastReceiver, new IntentFilter(Preferences.REGISTRATION_COMPLETE));
    }

}
