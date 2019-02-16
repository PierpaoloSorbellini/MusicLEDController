package com.example.pierpaolo.arduinoledcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by pierpaolo on 08/03/18.
 */

/*
  -Create the base activity with the drawer, there is a base layout and a constrained layout that is the one where the other activity are loaded.
  The button of the toolbar is handled with a method for opening the menu on press.

  -changeToolbarTitle and openDrawer are two method used for open and change the title by child activity.
   method to change the title of the activity

  -the menu uses the variable ITEM_SELECTED to keep trace of what is the current view, and a switch to start the intent for lunch the other activity.
 */

public class BaseActivity extends AppCompatActivity {

    public int ITEM_SELECTED = 0;
    public DrawerLayout drawerLayout = null;



    // ********************************************************************************************************
    //                ON CREATE
    // ********************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
    }



    // ********************************************************************************************************
    //                SET CONTENT VIEW
    // ********************************************************************************************************
    @Override
    public void setContentView(int layoutResID) {

        // Intent for the drawer menu
        final Intent intentSearch = new Intent( getApplicationContext(), ActivitySearch.class );
        final Intent intentMainActivity = new Intent( getApplicationContext(), ActivityHome.class );
        final Intent intentMusicPlayer = new Intent( getApplicationContext(), ActivityMusicPlayer.class );
        final Intent intentSettings = new Intent( getApplicationContext(), ActivitySettings.class );
        final Intent intentBluetooth = new Intent( getApplicationContext(), ActivityBluetooth.class );
        final Intent intentManualController = new Intent( getApplicationContext(), ActivityManualController.class );

        // Get drawer layout and inflate base_activity_layout
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate( R.layout.activity_base_activity, null );

        // Getting address of the frame layout of the base_activity
        ConstraintLayout activityContainer = drawerLayout.findViewById( R.id.base_activity_content );

        // Inflate the layout passed input in the methods in the activityContainer
        getLayoutInflater().inflate( layoutResID, activityContainer, true );

        // Set the View
        super.setContentView( drawerLayout );

        // Setting the toolbar button
        ImageButton toolbarButton = findViewById( R.id.base_toolbar_button );
        toolbarButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer( GravityCompat.START );
            }

        } );

        // Setting up the navigation view
        final NavigationView navigationView = drawerLayout.findViewById( R.id.base_navigation_view );
        navigationView.getMenu().getItem( ITEM_SELECTED ).setChecked( true );
        navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.drawer_menu_home:
                        if (ITEM_SELECTED == 0) {
                            drawerLayout.closeDrawer( GravityCompat.START );
                        } else {
                            startActivity( intentMainActivity );
                        }
                        break;
                    case R.id.drawer_menu_search:
                        if (ITEM_SELECTED == 1) {
                            drawerLayout.closeDrawer( GravityCompat.START );
                        } else {
                            startActivity( intentSearch );
                        }
                        break;
                    case R.id.drawer_menu_musicplayer:
                        if (ITEM_SELECTED == 2) {
                            drawerLayout.closeDrawer( GravityCompat.START );
                        } else {
                            startActivity( intentMusicPlayer );
                        }
                        break;
                    case R.id.drawer_menu_manualcontroller:
                        if (ITEM_SELECTED == 3) {
                            drawerLayout.closeDrawer( GravityCompat.START );
                        } else {
                            startActivity( intentManualController );
                        }
                        break;
                    case R.id.drawer_menu_bluetooth:
                        if (ITEM_SELECTED == 4) {
                            drawerLayout.closeDrawer( GravityCompat.START );
                        } else {
                            startActivity( intentBluetooth );
                        }
                        break;
                    case R.id.drawer_menu_settings:
                        if (ITEM_SELECTED == 5) {
                            drawerLayout.closeDrawer( GravityCompat.START );
                        } else {
                            startActivity( intentSettings );
                        }
                        break;
                }
                return false;
            }
        } );
    }



    // ********************************************************************************************************
    //                METHOD TO CHANGE THE TITLE OF THE TOOLBAR
    // ********************************************************************************************************
    public void changeToolbarTitle(String title) {
        TextView textView = findViewById( R.id.base_toolbar_text );
        textView.setTextSize( 18 );
        if (title.length() > 27){
            textView.setTextSize( 15 );
        }
        else if (title.length() > 32) {
            textView.setTextSize( 8 );
        }
        else if ( title.length() > 40){
            textView.setTextSize( 5 );
        }
        textView.setText( title );
    }



    // ********************************************************************************************************
    //                METHOD TO OPEN THE DRAWER
    // ********************************************************************************************************
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            moveTaskToBack(true);
        }else {
            drawerLayout.openDrawer( Gravity.LEFT ); //OPEN Nav Drawer!
        }
    }

}
