package com.abdallah.bakingapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.abdallah.bakingapp.R;
import com.abdallah.bakingapp.activities.RecipeDetailsActivity;
import com.abdallah.bakingapp.adapters.RecipesAdapter;
import com.abdallah.bakingapp.api.RecipeAPI;
import com.abdallah.bakingapp.models.recipe.Recipe;
import com.abdallah.bakingapp.utils.LogUtils;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.Arrays;
import java.util.List;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class RecipesFragment extends Fragment implements RecipesAdapter.ItemClickListener {

    private static final String TAG = RecipesFragment.class.getSimpleName();

    @BindView(R.id.rv_recipes) RecyclerView recipesRecyclerView;
    @BindView(R.id.progressBar) ProgressBar loadingProgressBar;
    @BindInt(R.integer.recipes_grid_span_count) int gridSpanCount;
    private Unbinder unbinder;

    private List<Recipe> recipes;
    private RecipesAdapter recipesAdapter;


    public RecipesFragment() {
    }

    /**
     * Factory method to facilitate creating instances of this fragment.
     * @return An instance of RecipesFragment
     */
    public static RecipesFragment newInstance() {
        RecipesFragment fragment = new RecipesFragment();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_recipes, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);

        setupRecipesRecyclerView();

        return fragmentView;
    }

    private void setupRecipesRecyclerView() {

        recipesRecyclerView.setHasFixedSize(true);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), gridSpanCount);
        recipesRecyclerView.setLayoutManager(layoutManager);

        recipesAdapter = new RecipesAdapter(this);
        recipesRecyclerView.setAdapter(recipesAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadRecipes();
    }

    private void loadRecipes() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        RecipeAPI.fetchRecipes(getContext(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                LogUtils.d(TAG, response);

                Gson gson = new Gson();
                Recipe[] recipesArray = gson.fromJson(response.toString(), Recipe[].class);
                recipes = Arrays.asList(recipesArray);

                recipesAdapter.swapRecipes(recipes);

                loadingProgressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());

                displayErrorSnackbar(error);

                loadingProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void displayErrorSnackbar(VolleyError error) {
        String errorMsg;
        if (error instanceof NoConnectionError) {
            errorMsg = getString(R.string.error_loading_no_internet);
        }
        else if (error instanceof NetworkError) {
            errorMsg = getString(R.string.error_loading_network_problem);
        }
        else {
            errorMsg = getString(R.string.error_loading_general);
        }
        Snackbar.make(recipesRecyclerView, errorMsg, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadRecipes();
                    }
                }).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        RecipeAPI.cancelOngoingRequests(getContext());
    }

    @Override
    public void onRecyclerViewItemClicked(int clickedItemIndex) {
        LogUtils.d(TAG, "Clicked recipe index = " + clickedItemIndex);

        Intent intent = RecipeDetailsActivity.getStartIntent(getContext(), recipes.get(clickedItemIndex));
        startActivity(intent);
    }

}
