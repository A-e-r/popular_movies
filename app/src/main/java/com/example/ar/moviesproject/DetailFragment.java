package com.example.ar.moviesproject;

/**
 * Created by ar on 24/10/2015.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by ar on 24/10/2015.
 *
 * Populates detail view for MovieDB
 */
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();

        // If Activity has been started with bundled movie data, fetch it and populate views
        if (intent != null && intent.hasExtra(Movie.class.getName())) {
            Log.v(LOG_TAG, "Retrieving parcelable");
            Movie m = intent.getExtras().getParcelable(Movie.class.getName());
            updateViews(rootView, m);
        } else {
            Bundle b = getArguments();
            if (b != null){
                Movie m = b.getParcelable(Movie.class.getName());
                updateViews(rootView, m);
            }
        }

        return rootView;
    }

    private void updateViews(View rootView, Movie m){
        TextView titleView = (TextView) rootView.findViewById(R.id.detail_title_view);
        TextView ratingView = (TextView) rootView.findViewById(R.id.detail_rating_view);
        TextView synopsisView = (TextView) rootView.findViewById(R.id.detail_synopsis_view);
        TextView releaseView = (TextView) rootView.findViewById(R.id.detail_release_text);
        ImageView posterView = (ImageView) rootView.findViewById(R.id.detail_poster_view);

        Picasso.with(getContext()).load(m.poster).into(posterView);
        titleView.setText(m.title);
        synopsisView.setText(m.synopsis);
        ratingView.setText(String.format(getString(R.string.votes),m.vote));
        releaseView.setText(String.format(getString(R.string.released),m.release));
    }
}