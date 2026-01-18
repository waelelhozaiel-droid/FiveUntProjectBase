package com.example.wael;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class BeginActivity extends AppCompatActivity {
    BottomNavigationView bth;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_begin);

        ChangeFargment(new MenuFragment());

        bth=findViewById(R.id.bth);

        bth.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


                if(menuItem.getItemId()==R.id.btmenu1)
                {
                    ChangeFargment(new SettingFragment());
                }


                if(menuItem.getItemId()==R.id.btmenu2)
                {
                    ChangeFargment(new MenuFragment());
                }


                if(menuItem.getItemId()==R.id.btmenu3)
                {
                    ChangeFargment(new ProfaileFragment());
                }

                if(menuItem.getItemId()==R.id.btmenu4)
                {
                    ChangeFargment(new ItemFragment());
                }

                if(menuItem.getItemId()==R.id.btmenu5)
                {
                    ChangeFargment(new HomeFragment());
                }

                return true;
            }
        });


    }

    private void ChangeFargment(Fragment menuFragment) {
        FragmentManager frm =getSupportFragmentManager();
        FragmentTransaction ftm =frm.beginTransaction();
        ftm.replace(R.id.btflag,menuFragment);
        ftm.commit();
    }

}