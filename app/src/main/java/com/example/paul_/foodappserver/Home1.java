package com.example.paul_.foodappserver;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.*;
import androidx.drawerlayout.widget.*;
import androidx.recyclerview.widget.*;

import com.example.paul_.foodappserver.Common.*;
import com.example.paul_.foodappserver.Interface.*;
import com.example.paul_.foodappserver.Model.*;
import com.example.paul_.foodappserver.ViewHolder.*;
import com.firebase.ui.database.*;
import com.google.android.gms.tasks.*;
import com.google.android.material.floatingactionbutton.*;
import com.google.android.material.navigation.*;
import com.google.android.material.snackbar.*;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;
import com.rengwuxian.materialedittext.*;
import com.squareup.picasso.*;

import java.util.*;

import info.hoang8f.widget.*;

public class Home1 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView txtFullName ;

    // Definire firebase

    FirebaseDatabase database;
    DatabaseReference categories;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseRecyclerAdapter<Category,MeniuViewHolder> adapter;

    // Adaug un nou meniu Layout

    MaterialEditText edtName;
    FButton btnUpload, btnSelect ;

    Category newCategory;

    Uri saveUri;

    DrawerLayout drawer;

    // Definire recyclerView
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Manageriere meniu");
        setSupportActionBar(toolbar);

        //Initializare firebase
        database = FirebaseDatabase.getInstance();
        categories = database.getReference("Category");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           showDialog();
            }
        });

         drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Setez numele pentru user

        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView)headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());

        //Initializez viewul
        recycler_menu = (RecyclerView)findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);
        
        loadMenu();
        
    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home1.this);
        alertDialog.setTitle("Adauga o noua categorie");
        alertDialog.setMessage("Va rugam completati toate campurile ");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout,null);

        edtName = add_menu_layout.findViewById(R.id.editName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        //Event pentru butonul de Upload

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Userul va alege o imagine din galerie si va salva url-ul ei
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadeImage();
            }
        });


        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //Setez butonul

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //aici creez noua categorie

                if(newCategory != null)
                {
                 categories.push().setValue(newCategory);
                 Snackbar.make(drawer,"Noua categorie"+newCategory.getName()+"a fost creată cu succes",Snackbar.LENGTH_SHORT)
                         .show();
                }
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        alertDialog.show();


    }

    private void uploadeImage() {
        if(saveUri != null )
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Se uploadeaza imaginea");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                          mDialog.dismiss();
                            Toast.makeText(Home1.this,"Imagine uploadata",Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // in cazul in care am uploadat si putem lua link setam valoare pentru newCategory
                                newCategory = new Category(edtName.getText().toString(),uri.toString());

                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(Home1.this, ""+e.getMessage(),Toast.LENGTH_SHORT);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded " +progress+"%");
                        }
                    });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            saveUri = data.getData();
            btnSelect.setText("Imagine selectata!");
        }
    }

    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),Common.PICK_IMAGE_REQUEST);

    }

    private void loadMenu() {
        adapter = new FirebaseRecyclerAdapter<Category, MeniuViewHolder>(
           Category.class,
           R.layout.menu_item,
                MeniuViewHolder.class,
                categories

        ) {
            @Override
            protected void populateViewHolder(MeniuViewHolder viewHolder, Category model, int position) {
            viewHolder.txtMenuName.setText(model.getName());
                Picasso.get/*with*/(/*Home1.this*/).load(model.getImage())
                    .into(viewHolder.imageView);

            viewHolder.setItemClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {
                    // trimitem categoryid si incepem un nou activity
                    Intent foodList = new Intent(Home1.this,FoodList.class);
                    foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                    startActivity(foodList);

                }
            });
            }
        };

        adapter.notifyDataSetChanged(); //Facem refresh cand adaugam o categorie noua
        recycler_menu.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_orders)
        {
            Intent orders = new Intent(Home1.this,OrderStatus.class);
            startActivity(orders);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Update / Delete pentru Categorii


    @Override
    public boolean onContextItemSelected(MenuItem item) {

       if(item.getTitle().equals(Common.UPDATE))
       {
           showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
       }
       else if (item.getTitle().equals(Common.DELETE))
       {
           deleteCategory(adapter.getRef(item.getOrder()).getKey());
       }

        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {
    //sterg toate produsele dintr-o categorie

    DatabaseReference foods = database.getReference("Food");
        Query foodInCategory = foods.orderByChild("menuId").equalTo(key);
        foodInCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    postSnapShot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    categories.child(key).removeValue();
    Toast.makeText(this,"Categorie eliminata cu succes",Toast.LENGTH_SHORT).show();

    }

    private void showUpdateDialog(final String key, final Category item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home1.this);
        alertDialog.setTitle("Update la  categorie");
        alertDialog.setMessage("Va rugam completati toate campurile ");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout,null);

        edtName = add_menu_layout.findViewById(R.id.editName);
        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);

        //Nume default

        edtName.setText(item.getName());

        //Event pentru butonul de Upload

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item); //Userul va alege o imagine din galerie si va salva url-ul ei
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadeImage();
            }
        });


        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        //Setez butonul

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Update la informatiile produsului
                item.setName(edtName.getText().toString());
                categories.child(key).setValue(item);

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void changeImage(final Category item) {
        if(saveUri != null )
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Se uploadeaza imaginea");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(Home1.this,"Imagine uploadata",Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                               item.setImage(uri.toString());

                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(Home1.this, ""+e.getMessage(),Toast.LENGTH_SHORT);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded " +progress+"%");
                        }
                    });

        }
    }
}
