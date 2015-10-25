package com.example.ar.moviesproject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ar on 24/10/2015.
 */
public class MovieArrayAdapter extends ArrayAdapter<Movie> {

    private static final String LOG_TAG = MovieArrayAdapter.class.getSimpleName();

    public MovieArrayAdapter(Activity context, List<Movie> movies){
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Movie movie = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie, parent, false);
        }

        ImageView posterView = (ImageView) convertView.findViewById(R.id.image_view);
        Picasso.with(getContext()).load(movie.thumb).into(posterView);
        return  convertView;
    }

    public void setData(List<Movie> data){
        clear();
        if (data != null){
            for (Movie m : data){
                add(m);
            }
        }
    }
}


