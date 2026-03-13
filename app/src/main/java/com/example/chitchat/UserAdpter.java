package com.example.chitchat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdpter extends RecyclerView.Adapter<UserAdpter.viewholder> {

    Context mainActivity;
    ArrayList<Users> usersArrayList;

    public UserAdpter(Context mainActivity, ArrayList<Users> usersArrayList) {
        this.mainActivity = mainActivity;
        this.usersArrayList = usersArrayList;  // no copy needed
    }

    @NonNull
    @Override
    public UserAdpter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.user_item, parent, false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdpter.viewholder holder, int position) {
        Users users = usersArrayList.get(position);
        holder.username.setText(users.getUserName());
        holder.userstatus.setText(users.getStatus());

        // load profile pic safely
        if (users.getProfilepic() != null && !users.getProfilepic().equals("null") && !users.getProfilepic().isEmpty()) {
            Picasso.get().load(users.getProfilepic()).into(holder.userimg);
        } else {
            holder.userimg.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // open chat window
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mainActivity, chatwindo.class);
            intent.putExtra("nameeee", users.getUserName());
            intent.putExtra("reciverImg", users.getProfilepic());
            intent.putExtra("uid", users.getUserId());
            mainActivity.startActivity(intent);

            if (mainActivity instanceof MainActivity) {
                ((MainActivity) mainActivity).addMessagedUser(users);
            }

        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static class viewholder extends RecyclerView.ViewHolder {
        CircleImageView userimg;
        TextView username, userstatus;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            userimg = itemView.findViewById(R.id.userimg);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
        }
    }

    // ✅ Replace full list with new search results
    public void updateList(ArrayList<Users> list) {
        usersArrayList.clear();
        usersArrayList.addAll(list);
        notifyDataSetChanged();
    }
}
