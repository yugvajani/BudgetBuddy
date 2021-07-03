package com.example.sarika.budgetbuddy.ui.editCategory;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sarika.budgetbuddy.UserDocInfo;
import com.example.sarika.budgetbuddy.ui.DataRetrieval;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.okhttp.internal.Internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EditViewModel extends ViewModel implements DataRetrieval {
    // TODO: Implement the ViewModel
    MutableLiveData<String[]> cat;
    public String Uid;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    int expense;
    private static final String TAG = "DocSnippets";
    UserDocInfo user;

    public EditViewModel(){
        cat=new MutableLiveData<>();
        mAuth=FirebaseAuth.getInstance();
        Uid=mAuth.getUid();
        db=FirebaseFirestore.getInstance();
    }


    public void getData(){
        DocumentReference ref = db.document("/Users/" +Uid);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot.exists()){
                                Map<String, Object> map;
                                map = documentSnapshot.getData();
                                String cat1[] = getcatname(map);
                                for(int i=0;i<cat1.length;i++)
                                    Log.d("Doc","cat1="+cat1[i]);
                                cat.setValue(cat1);
                                Log.d("Doc","mutable cat=" + cat.getValue());
                            }
                            else{
                                Log.d(TAG, "No such document");
                            }
                        }
                        else{
                            Log.d(TAG, "get failed with", task.getException());
                        }
                    }
                });
       // Log.d("Doc","returning"+cat);
     //return cat;
    }

    public String[] getcatname(Map<String, Object> map){
        int i=0;
        Log.d("Doc","map="+map);
        String[] catname = new String[map.size()];
        for(Map.Entry<String, Object> m : map.entrySet())
        {
            catname[i] = m.getKey();
            //user = (UserDocInfo) m.getValue();
            //Log.d("Doc","user"+user.getExpense());
            i++;
        }
        //cat.setValue(catname);
        return catname;
    }

    public MutableLiveData<String[]> getCat() {
        Log.d("Doc","mutable cat=" + cat.getValue());
        return cat;
    }

    public void updateData(final String name, final int budget){
        Map<String, Object> map = new HashMap<>();
        map.put("Category name", name);
        map.put("Budget", budget);
        Log.d("Doc","map in updateDAta=" + map);
        db.collection("Users").document(Uid).collection("Categories").document(String.valueOf(FieldPath.of(name))).update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        db.collection("Users").document(Uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnap = task.getResult();
                    if (documentSnap.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: naya " + documentSnap.getData());
                        Log.d(TAG, "DocumentSnapshot data: naya " + documentSnap.get(FieldPath.of(name)));
                        Map<String, Object> inner= (Map<String,Object>)documentSnap.get(FieldPath.of(name));
                        inner.put("budget", String.valueOf(budget));
                        user = new UserDocInfo(inner.get("categoryName").toString(), Integer.parseInt(inner.get("budget").toString()), Integer.parseInt(inner.get("expense").toString()));
                        Log.d(TAG, "DocumentSnapshot data: naya " + inner);
                        db.collection("Users").document(Uid).update(
                                FieldPath.of(name), user
                        )
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                    }
                }
            }
        });


    }
}

