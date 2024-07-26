package lk.rush.firebasestorage.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import lk.rush.firebasestorage.R;
import lk.rush.firebasestorage.model.Item;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private FirebaseStorage storage;
    private ArrayList<Item> items;
    private Context context;

    public static String TAG = ItemAdapter.class.getName();

    public ItemAdapter(ArrayList<Item> items, Context context) {
        this.items = items;
        this.context = context;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_item_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.textName.setText(item.getName());
        holder.textDesc.setText(item.getDescription());
        holder.textPrice.setText(String.valueOf(item.getPrice()));
        Log.d(TAG,item.getName());
        storage.getReference("/item-images/"+item.getImage())
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i(TAG, "URI: "+uri);
//                        holder.image.setImageURI(uri);
//                        Picasso.get()
//                                .load(uri)
//                                .centerCrop()
//                                .into(holder.image);
                        Glide.with(context).load(uri).into(holder.image);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to load images."+ e.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {

        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textDesc, textPrice;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.itemText1);
            textDesc = itemView.findViewById(R.id.itemText2);
            textPrice = itemView.findViewById(R.id.itemText3);
            image = itemView.findViewById(R.id.itemImage);

            //41:10 stopped
        }
    }
}
