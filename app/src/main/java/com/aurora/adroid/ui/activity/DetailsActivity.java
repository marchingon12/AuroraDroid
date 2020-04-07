/*
 * Aurora Droid
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Droid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora Droid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Droid.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aurora.adroid.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aurora.adroid.Constants;
import com.aurora.adroid.R;
import com.aurora.adroid.manager.FavouritesManager;
import com.aurora.adroid.ui.fragment.DetailsFragment;
import com.aurora.adroid.util.Log;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends BaseActivity {

    public static final String INTENT_PACKAGE_NAME = "INTENT_PACKAGE_NAME";
    public static final String INTENT_REPO_NAME = "INTENT_REPO_NAME";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private String packageName;
    private String repoName;
    private DetailsFragment detailsFragment;
    private FavouritesManager favouritesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setupActionBar();
        favouritesManager = new FavouritesManager(this);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        packageName = getIntentPackageName(intent);
        repoName = intent.getStringExtra(INTENT_REPO_NAME);

        if (TextUtils.isEmpty(packageName)) {
            Log.e("No package name provided");
            finish();
            return;
        }
        Log.i("Getting info about %s", packageName);
        grabDetails();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_share:
                getShareIntent();
                return true;

            case R.id.action_favourites:
                if (favouritesManager.isFavourite(packageName)) {
                    favouritesManager.removeFromFavourites(packageName);
                    menuItem.setIcon(R.drawable.ic_fav);
                } else {
                    favouritesManager.addToFavourites(packageName);
                    menuItem.setIcon(R.drawable.ic_favourite_red);
                }
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.details_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.get(0) instanceof DetailsFragment) {
            FragmentManager fm = fragments.get(0).getChildFragmentManager();
            if (!fm.getFragments().isEmpty())
                fm.popBackStack();
            else
                super.onBackPressed();
        } else
            super.onBackPressed();
    }

    private void setupActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private String getIntentPackageName(Intent intent) {
        if (intent.hasExtra(INTENT_PACKAGE_NAME)) {
            return intent.getStringExtra(INTENT_PACKAGE_NAME);
        } else if (intent.getScheme() != null
                && intent.getScheme().equals("https")
                && intent.getData() != null) {
            Uri data = intent.getData();
            return data.getLastPathSegment();
        } else if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            return bundle.getString(INTENT_PACKAGE_NAME);
        }
        return null;
    }

    public void grabDetails() {
        detailsFragment = new DetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putString("PackageName", packageName);
        arguments.putString("RepoName", repoName);
        detailsFragment.setArguments(arguments);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, detailsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitAllowingStateLoss();
    }

    public void redrawButtons() {
        if (detailsFragment != null)
            detailsFragment.drawButtons();
    }

    private void getShareIntent() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, DetailsFragment.app.getName());
        i.putExtra(Intent.EXTRA_TEXT, Constants.APP_SHARE_URL + DetailsFragment.app.getPackageName());
        startActivity(Intent.createChooser(i, getString(R.string.action_share)));
    }
}