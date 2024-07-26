package lk.rush.firebasestorage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreKtxRegistrar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;

import lk.rush.firebasestorage.model.Item;

public class AddItemActivity extends AppCompatActivity {

    public static final String TAG = AddItemActivity.class.getName();
    private ImageButton imageButton;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private Uri imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        imageButton = findViewById(R.id.imageButton);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));

            }
        });

        //Add new record

        findViewById(R.id.btn_add_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editTextName = findViewById(R.id.editTextText);
                EditText editTextDescription = findViewById(R.id.editTextText2);
                EditText editTextPrice = findViewById(R.id.editTextNumberDecimal);

                String name = editTextName.getText().toString();
                String description = editTextDescription.getText().toString();
                double price = Double.parseDouble(editTextPrice.getText().toString());

                String imageId = UUID.randomUUID().toString();

                Item item = new Item(name, description, price, imageId);

                ProgressDialog dialog = new ProgressDialog(AddItemActivity.this);
                dialog.setMessage("Adding new item...");
                dialog.setCancelable(false);
                dialog.show();

                firestore.collection("Items").add(item)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                if (imagePath != null) {
                                    dialog.setMessage("Uploading image...");

                                    StorageReference reference = storage.getReference("item-images").child(imageId);
                                    reference.putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            dialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                             double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                             dialog.setMessage("Uploading "+(int)progress+"%");
                                        }
                                    });

                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        imagePath = result.getData().getData();
                        Log.d(TAG,imagePath.toString());
//                        imageButton.setImageURI(imagePath);

                        Picasso.get().load(imagePath).resize(200,200).centerCrop().into(imageButton);

//                        ImageDecoder.Source source = null;
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
//                            source = ImageDecoder.createSource(getContentResolver(), imagePath);
//                        }
//                        try {
//                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
//                                Bitmap bitmap = ImageDecoder.decodeBitmap(source);
//                                imageButton.setImageBitmap(bitmap);
//                            }
//                        }catch (IOException e) {
//                            Log.e(TAG, e.getMessage());
//                        }

                    }
                }
            }
    );

}
