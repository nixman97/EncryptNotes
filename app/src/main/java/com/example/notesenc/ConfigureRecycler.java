package com.example.notesenc;

import android.graphics.Color;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

public class ConfigureRecycler {
    public CustomAdapter getAdapter() {
        return adapter;
    }

    public ItemTouchHelper getIt() {
        return it;
    }

    private ItemTouchHelper it;
    private CustomAdapter adapter;

    public RecyclerView getNotesList() {
        return notesList;
    }

    RecyclerView notesList;

    ConfigureRecycler(AppCompatActivity activity) {
        configureRecyclerView(activity);
    }
    public void configureRecyclerView(AppCompatActivity activity) {
        notesList = activity.findViewById(R.id.notesList);
        AppDatabase database = Room.databaseBuilder(activity, AppDatabase.class, "db-contacts")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build();
        NoteDao n = database.getNoteDao();
        try {
            if (n.getFromPosition(0).getTitle().equals("") && n.getFromPosition(0).getBody().equals("")) {
                n.delete(n.getFromPosition(0));
                n.decreasePositions(0);
            }
        }
        catch (Exception ignore) {

        }


        // set up the RecyclerView
        notesList.setLayoutManager(new LinearLayoutManager(activity));
        notesList.setHasFixedSize(false);
        adapter = new CustomAdapter(notesList, (CoordinatorLayout) activity.findViewById(R.id.mainLayout), activity, n);
        adapter.setClickListener((CustomAdapter.ItemClickListener) activity);
        notesList.setAdapter(adapter);
        //configures what happens when using gestures on recyclerview
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.onItemDismiss(viewHolder.getAdapterPosition());
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    CustomAdapter.ViewHolder holder = (CustomAdapter.ViewHolder) viewHolder;
                    holder.itemView.setBackgroundColor(Color.LTGRAY);
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                CustomAdapter.ViewHolder holder = (CustomAdapter.ViewHolder) viewHolder;
                holder.itemView.setBackgroundColor(0);
            }
        };

        it = new ItemTouchHelper(callback);
        it.attachToRecyclerView(notesList);


    }
}
