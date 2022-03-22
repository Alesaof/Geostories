package com.example.geostories.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.geostories.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ForeignProfileActivity extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseStorage dbStorage;
    StorageReference storageRef;
    StorageReference storagePath;

    //Elementos de pantalla
    private TextView welcomeText;
    private TextView foreignProfileViewsText;
    private TextView foreignProfileViewsDoneText;
    private ImageView foreignProfilePic;
    private LinearLayout foreignProfilefirstLinearLayout;

    //Usuario actual
    private String userName;
    private long profileViews;
    private long viewsDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foreign_profile);
        Toolbar myToolbar = findViewById(R.id.toolbar_foreignProfile);
        setSupportActionBar(myToolbar);
        this.setTitle("@"+getIntent().getExtras().getString("UserVisited"));
        setUp();

    }

    private void setUp() {
        db = FirebaseFirestore.getInstance();
        DocumentReference res = db.collection("users").document(getIntent().getExtras().getString("UserVisited"));
        res.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){
                    putInfo(db, value);
                }
            }
        });
        foreignProfilefirstLinearLayout = findViewById(R.id.firstForeignProfileLinearLayout);
        db.collection("stories").whereEqualTo("userOwner", getIntent().getExtras().getString("UserVisited"))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    storieSectionInfo(task.getResult());
                } else {
                    Log.d("GeoStories", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void storieSectionInfo(QuerySnapshot result) {
        for (QueryDocumentSnapshot document : result) {
            LinearLayout secondLinearLayout = new LinearLayout(this);
            secondLinearLayout.setOrientation(LinearLayout.VERTICAL);

            TextView storieTittle = new TextView(this);
            storieTittle.setText(" ➩" + (String)document.get("storieTittle"));
            storieTittle.setTextColor((Color.parseColor("#FF0B4F6C")));
            storieTittle.setTypeface(null, Typeface.BOLD);
            /*storieTittle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ViewStorie.class);
                    intent.putExtra("ActualUser", getIntent().getExtras().getString("actualUser"));
                    intent.putExtra("ActualStorie", document.getId());
                    startActivity(intent);
                }
            });*/
            secondLinearLayout.addView(storieTittle);

            TextView storieDescription = new TextView(this);
            storieDescription.setText("      " + (String)document.get("storieDescription"));
            storieDescription.setTypeface(null, Typeface.BOLD);
            secondLinearLayout.addView(storieDescription);
            foreignProfilefirstLinearLayout.addView(secondLinearLayout);
        }
    }

    private void putInfo(FirebaseFirestore db, DocumentSnapshot value) {
        userName = value.getString("userName");
        welcomeText = findViewById(R.id.foreignProfileWelcomeText);
        welcomeText.setText("Bienvenido al perfíl de " + userName);

        profileViews = (long) value.get("profileViews");
        foreignProfileViewsText = findViewById(R.id.foreignProfileProfileViewsText);
        foreignProfileViewsText.setText("Visitas Perfil: " + profileViews);

        viewsDone = (long) value.get("viewsDone");
        foreignProfileViewsDoneText = findViewById(R.id.foreignProfileViewsDoneText);
        foreignProfileViewsDoneText.setText("Visitas Realizadas: " + viewsDone);

        foreignProfilePic = findViewById(R.id.profileImageForeignProfile);
        Glide.with(this).load(value.get("profilePicUrl")).into(foreignProfilePic);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }
}