package com.example.ar.moviesproject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display a list of movies as a grid.
 * The bulk of this class is based on Udacity's Sunshine App
 * (https://www.udacity.com)
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Movie>> {

    private MovieArrayAdapter mMovieAdapter;
    private ArrayList<Movie> movieList;
    private int mPosition = GridView.INVALID_POSITION;
    private GridView mGridView;

    private static final int LOADER_ID = 0;
    private static final String SELECTED_KEY = "selected_position";
    private static final String MOVIE_KEY = "movies";

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     *
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Movie m);
    }

    //******************
    // Lifecycle Events
    //******************

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // set true to use Settings options

        // Create movie list or retrieve it from the saved instance
        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_KEY)){
            movieList = new ArrayList<>();
        } else {
            movieList = savedInstanceState.getParcelableArrayList(MOVIE_KEY);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies(); // forces the app to load data at startup.
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_KEY, movieList);

        //Save the current position so we can seamlessly scroll back to it
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

        super.onSaveInstanceState(outState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMovieAdapter = new MovieArrayAdapter(getActivity(), movieList);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mGridView.setAdapter(mMovieAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie m = mMovieAdapter.getItem(position);

                if (m != null) {
                    // Call back to Main Activity
                    ((Callback) getActivity()).onItemSelected(m);
                }
                mPosition = position;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }


    //***********************
    // Menus
    //***********************

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovies();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //***********************
    // Loader Interface
    //***********************

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        return new MovieLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        mMovieAdapter.setData(data);
        //Once the data has been loaded, scroll to the original position in the list
        if (mPosition != GridView.INVALID_POSITION){
            mGridView.smoothScrollToPosition(mPosition);
        }

        //show data?
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        mMovieAdapter.setData(null);
    }


    //***********************
    // Helper Methods
    //***********************

    private void updateMovies(){
        Loader<List<Movie>> loader = getLoaderManager().getLoader(LOADER_ID);
        if (loader != null){
            loader.forceLoad();
        }
    }
}
