package lk.rush.firebasestorage;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Document;

import java.util.ArrayList;

import lk.rush.firebasestorage.adapter.ItemAdapter;
import lk.rush.firebasestorage.model.Item;

public class ItemViewActivity extends AppCompatActivity {
public static String TAG = ItemViewActivity.class.getName();
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private ArrayList<Item> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        items = new ArrayList<>();

        RecyclerView itemView = findViewById(R.id.itemView);

        ItemAdapter itemAdapter = new ItemAdapter(items,ItemViewActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemView.setLayoutManager(linearLayoutManager);
        itemView.setAdapter(itemAdapter);



        firestore.collection("Items").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.i(TAG,"Test 2");

                            items.clear();
                            for (QueryDocumentSnapshot snapshot : task.getResult()){
                                Item item = snapshot.toObject(Item.class);
                                items.add(item);

                            }

                            itemAdapter.notifyDataSetChanged();

                    }
                });

        firestore.collection("Items").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                items.clear();
//                for (DocumentSnapshot snapshot : value.getDocuments()) {
//                    Item item = snapshot.toObject(Item.class);
//                    items.add(item);
//                }

                //Realtime changes
                for (DocumentChange change: value.getDocumentChanges()){
                    Item item = change.getDocument().toObject(Item.class);
                    switch (change.getType()){
                        case ADDED:
                            items.add(item);
                        case MODIFIED:
                            Item old = items.stream().filter(i -> i.getName().equals(item.getName())).findFirst().orElse(null);
                            if (old != null) {
                                old.setDescription(item.getDescription());
                                old.setPrice(item.getPrice());
                                old.setImage(item.getImage());
                            }
                            break;
                        case REMOVED:
                            items.remove(item);
                    }
                }

                itemAdapter.notifyDataSetChanged();
            }
        });

    }
}