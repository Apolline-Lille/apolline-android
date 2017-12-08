package science.apolline.view.Activity;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import science.apolline.R;
import science.apolline.service.sensor.IOIOService;
import science.apolline.view.Fragment.IOIOFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 100;
    private static final int REQUEST_CODE_FINE_LOCATION = 101;
    private static final int REQUEST_CODE_COARSE_LOCATION = 102;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
        }

        permissionCheck();

        Fragment IOIOFragment = new IOIOFragment();
        replaceFragment(IOIOFragment);
    }

    private void permissionCheck() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertFine();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_FINE_LOCATION);

            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                AlertCoarse();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_COARSE_LOCATION);

            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertWriteExternalStorage();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);

            }
        }
    }

    private void AlertCoarse() {
        AlertDialog alertdialog = new AlertDialog.Builder(this).create();
        alertdialog.setTitle("Permission Access_Coarse_Location");
        alertdialog.setMessage("Cette permission est nécessaire pour prendre la géolocalisation et au bon fonctionnement de l'application");
        alertdialog.setButton(AlertDialog.BUTTON_NEGATIVE, "non", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        alertdialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MainActivity.REQUEST_CODE_COARSE_LOCATION);

            }
        });
        alertdialog.show();
    }

    private void AlertWriteExternalStorage() {
        AlertDialog alertdialog = new AlertDialog.Builder(this).create();
        alertdialog.setTitle("Permission Write_External_Storage");
        alertdialog.setMessage("Cette permission est nécessaire pour exporter les données dans un fichier");
        alertdialog.setButton(AlertDialog.BUTTON_NEGATIVE, "non", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        alertdialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.REQUEST_CODE_WRITE_EXTERNAL_STORAGE);

            }
        });
        alertdialog.show();
    }

    private void AlertFine() {
        AlertDialog alertdialog = new AlertDialog.Builder(this).create();
        alertdialog.setTitle("Permission Access_Fine_Location");
        alertdialog.setMessage("Cette permission est nécessaire pour prendre la géolocalisation et au bon fonctionnement de l'application");
        alertdialog.setButton(AlertDialog.BUTTON_NEGATIVE, "non", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        alertdialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.REQUEST_CODE_FINE_LOCATION);
            }
        });
        alertdialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]);
                    if (showRationale) {
                        AlertFine();
                    } else {
                        finish();
                    }
                }
            }
            case REQUEST_CODE_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]);
                    if (showRationale) {
                        AlertCoarse();
                    } else {
                        finish();
                    }
                }
            }
            case REQUEST_CODE_WRITE_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]);
                    if (showRationale) {
                        AlertWriteExternalStorage();
                    } else {
                        finish();
                    }
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int itemId = item.getItemId();
        int groupId = item.getGroupId();

        //

        switch (groupId) {
            case R.id.grp_capteur:
                if (itemId == R.id.nav_ioio) {
                    Fragment IOIOFragment = new IOIOFragment();
                    replaceFragment(IOIOFragment);
                }
                break;
            case R.id.grp_fonction:
                if (itemId == R.id.nav_setting) {
                    Intent intent = new Intent(this, OptionsActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.nav_info) {
                    Intent intent = new Intent(this, InformationActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_contact) {
                    Intent intent = new Intent(this, ContactActivity.class);
                    startActivity(intent);
                }
                break;
            default:
//              if (itemId == R.id.nav_share) {
//
//              } else if (itemId == R.id.nav_send) {
//
//              }
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.fragment, fragment, backStateName);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, IOIOService.class));
    }
}
